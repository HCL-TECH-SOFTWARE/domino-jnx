/*
 * ==========================================================================
 * Copyright (C) 2019-2021 HCL America, Inc. ( http://www.hcl.com/ )
 *                            All rights reserved.
 * ==========================================================================
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may
 * not use this file except in compliance with the License.  You may obtain a
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.
 *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the  specific language  governing permissions  and limitations
 * under the License.
 * ==========================================================================
 */
package com.hcl.domino.jnx.console.internal;

/**
 * Utility class to contains OS type parsing and string utility functions
 */
class ConsoleUtils {
    public static final String COLON = ":";

    public static int getOSType(String value) {
        int n = 0;
        if (value.regionMatches(true, 0, "Windows", 0, 7)) {
            n = 0;
        } else if (value.regionMatches(true, 0, "Linux", 0, 5)) {
            n = 1;
        } else if (value.regionMatches(true, 0, "AIX", 0, 3)) {
            n = 2;
        } else if (value.regionMatches(true, 0, "OS/400", 0, 6)) {
            n = 3;
        } else if (value.regionMatches(true, 0, "MAC", 0, 3)) {
            n = 4;
        } else {
            System.out.println("WARN: Unsupported platform OS name: " + value);
        }
        return n;
    }

    public static void enableSocksProxy(String string, String string2) {
    	//TODO changing global props might not be a good idea
        System.getProperties().setProperty("socksProxyHost", string);
        System.getProperties().setProperty("socksProxyPort", string2);
        System.getProperties().setProperty("socksProxySet", "true");
    }

    public static void disableSocksProxy() {
    	//TODO changing global props might not be a good idea
        System.getProperties().remove("socksProxySet");
        System.getProperties().remove("socksProxyHost");
        System.getProperties().remove("socksProxyPort");
    }
    
    public static String changeColonToNative(String string) {
        return ConsoleUtils.changeColonInSource(string, false);
    }

    private static String changeColonInSource(String string, boolean bl) {
        int n;
        StringBuffer stringBuffer = new StringBuffer(256);
        String string2 = "&colon;";
        String string3 = COLON;
        String string4 = bl ? string3 : string2;
        String string5 = bl ? string2 : string3;
        int n2 = 0;
        while ((n = string.indexOf(string4, n2)) != -1) {
            stringBuffer.append(string.substring(n2, n));
            stringBuffer.append(string5);
            n2 = n + string4.length();
        }
        if (n2 != string.length()) {
            stringBuffer.append(string.substring(n2, string.length()));
        }
        return stringBuffer.toString();
    }
}

