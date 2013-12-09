//
//  ========================================================================
//  Copyright (c) 1995-2013 Mort Bay Consulting Pty. Ltd.
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

import java.io.PrintStream;

/**
 * Get the metadata for the archive.
 */
public class ImplMetadata
{
    private static final String UNKNOWN = "[unknown/unpackaged]";
    private final String className;
    private final String vendor;
    private final String version;

    public ImplMetadata(Class<?> clazz)
    {
        this.className = clazz.getName();

        Package pkg = clazz.getPackage();
        if (pkg != null)
        {
            vendor = fallback(pkg.getImplementationVendor());
            version = fallback(pkg.getImplementationVersion());
        }
        else
        {
            vendor = UNKNOWN;
            version = UNKNOWN;
        }
    }

    public ImplMetadata(Object obj)
    {
        this(obj.getClass());
    }

    public void dump(PrintStream out)
    {
        out.printf("Implementation: %s%n",className);
        out.printf("        Vendor: %s%n",vendor);
        out.printf("       Version: %s%n",version);
    }

    private String fallback(String val)
    {
        if (val == null)
        {
            return UNKNOWN;
        }
        return val;
    }

    public String getVendor()
    {
        return vendor;
    }

    public String getVersion()
    {
        return version;
    }

    public String toSafeUserAgent()
    {
        final String UNSAFE = " %$&+,/:;=?@<>#%";
        String ua = String.format("%s/%s",vendor,version);
        StringBuilder ret = new StringBuilder();
        for (char c : ua.toCharArray())
        {
            if (c > 128 || c < 0 || UNSAFE.indexOf(c) >= 0)
            {
                // encode
                ret.append(String.format("%%%02X",(byte)(c & 0xFF)));
            }
            else
            {
                // as-is
                ret.append(c);
            }
        }
        return ret.toString();
    }
}
