//
//  ========================================================================
//  Copyright (c) 2013 - Joakim Erdfelt
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://opensource.org/licenses/EPL-1.0
//
//      The Apache License v2.0 is available at
//      http://opensource.org/licenses/Apache-2.0
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package com.github.joakime.websocket;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

@ClientEndpoint
public class OnGetCaseCount
{
    private static final Logger LOG = Logger.getLogger(OnGetCaseCount.class.getName());
    private Integer casecount = null;
    private CountDownLatch messageLatch = new CountDownLatch(1);
    private CountDownLatch closeLatch = new CountDownLatch(1);

    public void awaitMessage() throws InterruptedException
    {
        messageLatch.await(1,TimeUnit.SECONDS);
    }
    
    public void awaitClose() throws InterruptedException
    {
        closeLatch.await(1,TimeUnit.SECONDS);
    }

    public int getCaseCount()
    {
        return casecount.intValue();
    }

    public boolean hasCaseCount()
    {
        return (casecount != null);
    }

    @OnClose
    public void onClose(CloseReason reason)
    {
        LOG.log(Level.FINE,"onClose({0})",reason);
        closeLatch.countDown();
    }

    @OnOpen
    public void onOpen(Session session)
    {
        LOG.log(Level.FINE,"onOpen({0})",session);
    }

    @OnMessage
    public void onCaseCount(int count)
    {
        LOG.log(Level.FINE,"onCaseCount({0})",count);
        casecount = count;
        messageLatch.countDown();
    }
}
