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
import javax.websocket.OnOpen;
import javax.websocket.Session;

@ClientEndpoint
public class OnUpdateReports
{
    private static final Logger LOG = Logger.getLogger(OnUpdateReports.class.getName());
    private CountDownLatch latch = new CountDownLatch(1);

    public void awaitClose() throws InterruptedException
    {
        latch.await(15,TimeUnit.SECONDS);
    }

    @OnClose
    public void onClose(CloseReason reason)
    {
        LOG.log(Level.FINE,"onClose({0})",reason);
        LOG.info("Reports updated.");
        LOG.info("Test suite finished!");
        latch.countDown();
    }

    @OnOpen
    public void onOpen(Session session)
    {
        LOG.fine("Updating reports ...");
    }
}
