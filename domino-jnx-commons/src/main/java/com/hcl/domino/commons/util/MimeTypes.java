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

/**
 * Helper class to 'guess' a mime type by a file extension
 * 
 * @author Karsten Lehmann
 */
public class MimeTypes {
	private static final Logger log = Logger.getLogger(MimeTypes.class.getPackage().getName());

	private static Map<String,String> m_mimeTypesForExtension;
	private static Map<String,String> m_extensionForMimeType;
	private static volatile boolean m_initialized;
	private static final String mimeTypeFileName="mime.types"; //$NON-NLS-1$
	
	private static void hashMimeTypes() {
		if (!m_initialized) {
			synchronized (MimeTypes.class) {
				if (!m_initialized) {
					m_mimeTypesForExtension=new HashMap<>();
					m_extensionForMimeType=new HashMap<>();

					BufferedReader bReader=null;
					try {
						InputStream in=MimeTypes.class.getResourceAsStream(mimeTypeFileName);
						if (in==null) {
							throw new IllegalStateException("Resource mime.types not found");
						}
						bReader=new BufferedReader(new InputStreamReader(in));
						String line;
						while ((line=bReader.readLine())!=null) {
							if (!line.startsWith("#")) { //$NON-NLS-1$
								line=line.trim();
								line=line.replace("\t", " "); //$NON-NLS-1$ //$NON-NLS-2$
								int iPos=line.indexOf(" "); //$NON-NLS-1$
								if (iPos>-1) {
									String currMimeType=line.substring(0, iPos).trim();
									String currExtensions=line.substring(iPos).trim();
									
									if (!StringUtil.isEmpty(currMimeType) && !StringUtil.isEmpty(currExtensions))  {
										String[] currExtensionParts=currExtensions.split(" "); //$NON-NLS-1$
										
										//use first extension as primary one
										m_extensionForMimeType.put(currMimeType, currExtensionParts[0]);
										
										for (String currExt : currExtensionParts) {
											m_mimeTypesForExtension.put(currExt, currMimeType);
										}
									}
								}
							}
						}
					}
					catch (IOException e) {
						log.log(Level.SEVERE, MessageFormat.format("Could not read mime types from file {0}", mimeTypeFileName), e);
					}
					finally {
						if (bReader!=null) {
							try {
								bReader.close();
							} catch (IOException ignore) {}
						}
					}
					m_initialized=true;
				}
			}
		}
	}

	/**
	 * The method tries to find an extension for the specified mimetype
	 * 
	 * @param mimeType mime type
	 * @return extension or <code>null</code> if unknown mimetype
	 */
	public static String getExtension(String mimeType) {
		hashMimeTypes();
		
		String extension=m_extensionForMimeType.get(mimeType);
		if (extension!=null) {
			return extension;
		} else {
			//handle something like text/html
			int iPos=mimeType.indexOf(" "); //$NON-NLS-1$
			if (iPos>-1) {
				extension=m_extensionForMimeType.get(mimeType.substring(0, iPos).trim());
				if (extension!=null) {
					return extension;
				}
			}
			iPos=mimeType.indexOf(";"); //$NON-NLS-1$
			if (iPos>-1) {
				extension=m_extensionForMimeType.get(mimeType.substring(0, iPos).trim());
				if (extension!=null) {
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
	public static String getMimeType(String fileUrl) {
		hashMimeTypes();

		int iPos=fileUrl.lastIndexOf("."); //$NON-NLS-1$
		if (iPos>-1) {
			String mimeType=m_mimeTypesForExtension.get(fileUrl.substring(iPos+1));
			if (mimeType!=null) {
				return mimeType;
			}
		}
		String mimeType=m_mimeTypesForExtension.get(fileUrl.substring(iPos+1));
		if (mimeType!=null) {
			return mimeType;
		}
		
		//old manual code, use JDK functionality
		FileNameMap fileNameMap = URLConnection.getFileNameMap();
		String type = fileNameMap.getContentTypeFor(fileUrl);

		return StringUtil.isEmpty(type) ? "application/octet-stream" : type; //$NON-NLS-1$
	}
}
