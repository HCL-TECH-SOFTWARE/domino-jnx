/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
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
package com.hcl.domino.commons.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.mail.Session;

/**
 * Helper class to 'guess' a mime type by a file extension
 *
 * @author Karsten Lehmann
 */
public class MimeUtil {
  private static final Logger log = Logger.getLogger(MimeUtil.class.getPackage().getName());

  private static Map<String, String> m_mimeTypesForExtension;
  private static Map<String, String> m_extensionForMimeType;
  private static volatile boolean m_initialized;
  private static final String mimeTypeFileName = "mime.types"; //$NON-NLS-1$
  
  private static Session mailSession;
  private static final Object mailLock = new Object();

  /**
   * The method tries to find an extension for the specified mimetype
   * 
   * @param mimeType mime type
   * @return extension or <code>null</code> if unknown mimetype
   */
  public static String getExtension(final String mimeType) {
    MimeUtil.hashMimeTypes();

    String extension = MimeUtil.m_extensionForMimeType.get(mimeType);
    if (extension != null) {
      return extension;
    } else {
      // handle something like text/html
      int iPos = mimeType.indexOf(" "); //$NON-NLS-1$
      if (iPos > -1) {
        extension = MimeUtil.m_extensionForMimeType.get(mimeType.substring(0, iPos).trim());
        if (extension != null) {
          return extension;
        }
      }
      iPos = mimeType.indexOf(";"); //$NON-NLS-1$
      if (iPos > -1) {
        extension = MimeUtil.m_extensionForMimeType.get(mimeType.substring(0, iPos).trim());
        if (extension != null) {
          return extension;
        }
      }

      return null;
    }
  }

  /**
   * Returns a mime type for the specified filename
   * 
   * @param fileUrl filename
   * @return mimetype, if unknown we return "application/octet-stream"
   */
  public static String getMimeType(final String fileUrl) {
    MimeUtil.hashMimeTypes();

    final int iPos = fileUrl.lastIndexOf("."); //$NON-NLS-1$
    if (iPos > -1) {
      final String mimeType = MimeUtil.m_mimeTypesForExtension.get(fileUrl.substring(iPos + 1));
      if (mimeType != null) {
        return mimeType;
      }
    }
    final String mimeType = MimeUtil.m_mimeTypesForExtension.get(fileUrl.substring(iPos + 1));
    if (mimeType != null) {
      return mimeType;
    }

    // old manual code, use JDK functionality
    final FileNameMap fileNameMap = URLConnection.getFileNameMap();
    final String type = fileNameMap.getContentTypeFor(fileUrl);

    return StringUtil.isEmpty(type) ? "application/octet-stream" : type; //$NON-NLS-1$
  }

  private static void hashMimeTypes() {
    if (!MimeUtil.m_initialized) {
      synchronized (MimeUtil.class) {
        if (!MimeUtil.m_initialized) {
          MimeUtil.m_mimeTypesForExtension = new HashMap<>();
          MimeUtil.m_extensionForMimeType = new HashMap<>();

          BufferedReader bReader = null;
          try {
            final InputStream in = MimeUtil.class.getResourceAsStream(MimeUtil.mimeTypeFileName);
            if (in == null) {
              throw new IllegalStateException("Resource mime.types not found");
            }
            bReader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = bReader.readLine()) != null) {
              if (!line.startsWith("#")) { //$NON-NLS-1$
                line = line.trim();
                line = line.replace("\t", " "); //$NON-NLS-1$ //$NON-NLS-2$
                final int iPos = line.indexOf(" "); //$NON-NLS-1$
                if (iPos > -1) {
                  final String currMimeType = line.substring(0, iPos).trim();
                  final String currExtensions = line.substring(iPos).trim();

                  if (!StringUtil.isEmpty(currMimeType) && !StringUtil.isEmpty(currExtensions)) {
                    final String[] currExtensionParts = currExtensions.split(" "); //$NON-NLS-1$

                    // use first extension as primary one
                    MimeUtil.m_extensionForMimeType.put(currMimeType, currExtensionParts[0]);

                    for (final String currExt : currExtensionParts) {
                      MimeUtil.m_mimeTypesForExtension.put(currExt, currMimeType);
                    }
                  }
                }
              }
            }
          } catch (final IOException e) {
            MimeUtil.log.log(Level.SEVERE,
                MessageFormat.format("Could not read mime types from file {0}", MimeUtil.mimeTypeFileName), e);
          } finally {
            if (bReader != null) {
              try {
                bReader.close();
              } catch (final IOException ignore) {
              }
            }
          }
          MimeUtil.m_initialized = true;
        }
      }
    }
  }
  
  /**
   * Retrieves a shared Jakarta Mail {@link Session} object
   * used for MIME operations, creating a new one based on the
   * system properties and a null authenticator if not yet
   * configured.
   * 
   * @return a shared default {@link Session}
   * @since 1.53.0
   */
  public static Session getMailSession() {
    synchronized (mailLock) {
      Session set = mailSession;
      if (set != null) {
        return set;
      }

      set = mailSession = Session.getInstance(System.getProperties(), null);
      return set;
    }
  }
  
  /**
   * Sets the Jakarta Mail {@link Session} to use for MIME operations.
   * 
   * @param session the Jakarta {@link Session} to use, or {@code null}
   *        to un-set an existing value
   * @since 1.53.0
   */
  public static void setMailSession(Session session) {
    mailSession = session;
  }
}
