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

import java.nio.ByteBuffer;
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
public class OnEchoMessage
{
    private static final Logger LOG = Logger.getLogger(OnEchoMessage.class.getName());
    private final int currentCaseId;
    private CountDownLatch latch = new CountDownLatch(1);

    public OnEchoMessage(int currentCaseId)
    {
        this.currentCaseId = currentCaseId;
    }

    public void awaitClose() throws InterruptedException
    {
        latch.await(5,TimeUnit.SECONDS);
    }

    @OnMessage(maxMessageSize=20000000)
    public ByteBuffer onBinary(ByteBuffer buf)
    {
        LOG.log(Level.FINE,"onBinary(ByteBuffer.remaining={0})",(buf==null)?"<null>":Integer.toString(buf.remaining()));
        return buf;
    }

    @OnClose
    public void onClose(CloseReason reason)
    {
        LOG.log(Level.FINE,"onClose({0})",reason);
        latch.countDown();
    }

    @OnOpen
    public void onOpen(Session session)
    {
        LOG.log(Level.FINE,"onOpen({0})",session);
        LOG.log(Level.FINE,"Executing test case {0}",currentCaseId);
    }

    @OnMessage(maxMessageSize=20000000)
    public String onText(String message)
    {
        LOG.log(Level.FINE,"onText(String.length={0})",(message==null)?"<null>":Integer.toString(message.length()));
        // Echo the data back.
        return message;
    }
}
