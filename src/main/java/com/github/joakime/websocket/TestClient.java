//
//  ========================================================================
//  Copyright (c) - Joakim Erdfelt
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package com.github.joakime.websocket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.WebSocketContainer;

public class TestClient
{
    public static void main(String[] args)
    {
        LoggingUtil.config();

        TestClient test = new TestClient();

        try
        {
            test.parse(args);
        }
        catch (Throwable t)
        {
            test.usage();
            t.printStackTrace(System.err);
            System.exit(-1);
        }

        try
        {
            test.run();
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
        }
        System.exit(0);
    }

    private Logger LOG = Logger.getLogger(TestClient.class.getName());
    private URI wsURI;
    private List<Integer> caseNumbers = new ArrayList<>();
    private WebSocketContainer container;
    private String userAgent;

    public TestClient()
    {
        System.err.printf("javax.websocket / autobahn testsuite / client%n");
        ImplMetadata meta = new ImplMetadata(this.getClass());
        meta.dump(System.err);
    }

    public void parse(String[] args)
    {
        for (String arg : args)
        {
            if (arg.startsWith("ws://") || arg.startsWith("wss://"))
            {
                if (wsURI != null)
                {
                    throw new RuntimeException("Can only specify 1 websocket URI");
                }
                try
                {
                    wsURI = new URI(arg);
                }
                catch (URISyntaxException e)
                {
                    throw new RuntimeException("Invalid WebSocket URI",e);
                }
                continue;
            }

            try
            {
                Integer casenum = Integer.parseInt(arg);
                caseNumbers.add(casenum);
            }
            catch (NumberFormatException e)
            {
                throw new RuntimeException(String.format("Invalid case number argument: %s",arg));
            }
        }

        if (wsURI == null)
        {
            throw new RuntimeException("You must specify a destination websocket URI");
        }
    }

    private int requestCaseCount() throws DeploymentException, IOException, InterruptedException
    {
        URI countUri = wsURI.resolve("/getCaseCount");
        LOG.log(Level.FINE,"get case count uri: {0}",countUri);

        OnGetCaseCount onCaseCount = new OnGetCaseCount();

        container.connectToServer(onCaseCount,countUri);
        onCaseCount.awaitMessage();
        onCaseCount.awaitClose();
        if (onCaseCount.hasCaseCount())
        {
            return onCaseCount.getCaseCount();
        }
        throw new IllegalStateException("Unable to get Case Count");
    }

    private void requestUpdateReports() throws DeploymentException, IOException, InterruptedException
    {
        System.err.printf("Updating Reports%n");
        URI updateUri = wsURI.resolve(String.format("/updateReports?agent=%s",userAgent));
        LOG.log(Level.FINE,"update reports uri: {0}",updateUri);

        OnUpdateReports onUpdateReports = new OnUpdateReports();
        container.connectToServer(onUpdateReports,updateUri);
        onUpdateReports.awaitClose();
    }

    private void run() throws DeploymentException, IOException, InterruptedException
    {
        LOG.info("Initializing:");

        container = ContainerProvider.getWebSocketContainer();
        if (container == null)
        {
            throw new RuntimeException(String.format("No javax.websocket.WebSocketContainer found"));
        }

        System.err.println("Got javax.websocket.WebSocketContainer");

        ImplMetadata meta = new ImplMetadata(container);
        meta.dump(System.err);

        userAgent = meta.toSafeUserAgent();

        System.err.println("Running test suite ...");
        System.err.printf("Using Fuzzing Server at %s%n",wsURI);
        System.err.printf("User Agent: %s%n",userAgent);

        if (caseNumbers.size() >= 1)
        {
            int caseCount = caseNumbers.size();
            System.err.printf("Running %d specific test case%s%n",caseCount,(caseCount > 1)?"s":"");
            for (int caseNum : caseNumbers)
            {
                caseCount--;
                System.err.printf("Running case %d (%d left)%n",caseNum,caseCount);
                runCase(caseNum);
            }
        }
        else
        {
            int caseCount = requestCaseCount();
            System.err.printf("Running all %d cases%n",caseCount);
            for (int caseNum = 1; caseNum < caseCount; caseNum++)
            {
                System.err.printf("Running case %d (%d left)%n",caseNum,caseCount - caseNum - 1);
                runCase(caseNum);
            }
        }

        requestUpdateReports();
    }

    private void runCase(int caseNumber) throws DeploymentException, IOException, InterruptedException
    {
        URI caseUri = wsURI.resolve(String.format("/runCase?case=%s&agent=%s",caseNumber,userAgent));
        LOG.log(Level.FINE,"run case uri: {0}",caseUri);

        OnEchoMessage onEchoMessage = new OnEchoMessage(caseNumber);
        container.connectToServer(onEchoMessage,caseUri);
        onEchoMessage.awaitClose();
    }

    public void usage()
    {
        String mainClass = TestClient.class.getName();
        System.err.printf("USAGE: java -cp [classpath] %s [websocket-uri] [testcases]%n",mainClass);
        System.err.printf("   eg: java -cp [classpath] %s ws://localhost:9001/%n",mainClass);
        System.exit(-1);
    }
}
