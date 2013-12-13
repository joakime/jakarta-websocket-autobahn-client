//
//  ========================================================================
//  Copyright (c) 2013 - Joakim Erdfelt
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

import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        Manifest manifest = findManifestFor(this.className);

        vendor = fallback(
                manifest.getMainAttributes().getValue("Bundle-Vendor"),
                manifest.getMainAttributes().getValue("Implementation-Vendor"),
                manifest.getMainAttributes().getValue("Bundle-Name"),
                UNKNOWN);
        version = fallback(
                manifest.getMainAttributes().getValue("Bundle-Version"),
                manifest.getMainAttributes().getValue("Implementation-Version"),
                UNKNOWN);
    }

    private Manifest findManifestFor(String clazz)
    {
        String resourceName = '/' + clazz.replace('.','/') + ".class";
        URL url = this.getClass().getResource(resourceName);
        System.err.printf("Manifest: %s => %s%n",resourceName,url);

        Manifest ret = new Manifest();
        Pattern pat = Pattern.compile("^jar:(file:/[^!]*)!/.*$");

        if (url == null)
        {
            return ret;
        }

        try
        {
            Matcher match = pat.matcher(url.toURI().toASCIIString());
            if (!match.matches())
            {
                return ret;
            }

            URI jarUri = new URI(match.group(1));

            try (JarFile jar = new JarFile(new File(jarUri)))
            {
                Manifest manifest = jar.getManifest();
                if (manifest != null)
                {
                    ret = manifest;
                }
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }

        return ret;
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

    private String fallback(String... vals)
    {
        for (String val : vals)
        {
            if (val != null)
            {
                return val;
            }
        }

        return UNKNOWN;
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
        String ua = String.format("%s / %s / %s",vendor,className,version);

        // Special HACK for broken tyrus implementation
        if (className.contains(".tyrus."))
        {
            ua = ua.replace(' ','+');
        }

        // All other implementations
        final String UNSAFE = " %$&+,/:;=?@<>#%";
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
