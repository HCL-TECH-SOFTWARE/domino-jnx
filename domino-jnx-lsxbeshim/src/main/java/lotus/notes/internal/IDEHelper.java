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
package lotus.notes.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class IDEHelper {
	/**
	 * This method is called by the DXL exporter to list the files included in a %%source%%.jar file
	 * it copied to the filesystem.
	 * 
	 * @param jarPath a filesystem path to an extracted JAR file
	 * @return a string array of file names in the JAR, minus any beneath META-INF
	 * @throws IOException if there is a problem reading the extracted JAR
	 */
	static String[] listJar(String jarPath) throws IOException {
		Path jar = Paths.get(jarPath);
		if(Files.isRegularFile(jar)) {
			List<String> result = new ArrayList<>();
			try(
				InputStream is = Files.newInputStream(jar);
				ZipInputStream zis = new ZipInputStream(is)
			) {
				ZipEntry entry = null;
				while((entry = zis.getNextEntry()) != null) {
					if(!entry.isDirectory() && !entry.getName().startsWith("META-INF/")) { //$NON-NLS-1$
						result.add(entry.getName());
					}
				}
			}
			return result.toArray(new String[result.size()]);
		} else {
			// Must return a value or else stdout from native says the method "failed"
			return new String[0];
		}
	}
	
	/**
	 * This method is called by the DXL exporter to export the contents of the %%source%%.jar file
	 * it copied to the filesystem to the given destination directory.
	 * 
	 * @param zipPath a filesystem path to an extracted JAR file
	 * @param destPath a filesystem path to a temporary directory to house the files
	 * @return whether the file was successfully extracted
	 * @throws IOException if there is a problem reading the extracted file
	 */
	static boolean extractJar(String zipPath, String destPath) throws IOException {
		Path zipFilePath = Paths.get(zipPath);
		if(!Files.isRegularFile(zipFilePath)) {
			return false;
		}
		Path result = Paths.get(destPath);
		Files.createDirectories(result);
		
		try(InputStream is = Files.newInputStream(zipFilePath)) {
			try(ZipInputStream zis = new ZipInputStream(is, StandardCharsets.UTF_8)) {
				ZipEntry entry;
				while((entry = zis.getNextEntry()) != null) {
					String name = entry.getName();

					Path subFile;
					try {
						subFile = result.resolve(name);
					} catch(InvalidPathException e) {
						// This occurs with non-ASCII characters on Unix sometimes
						String urlName = URLEncoder.encode(name, "UTF-8") //$NON-NLS-1$
							.replace("%2F", "/"); //$NON-NLS-1$ //$NON-NLS-2$
						subFile = result.resolve(urlName);
					}
					
					if(entry.isDirectory()) {
						Files.createDirectories(subFile);
					} else {
						Files.createDirectories(subFile.getParent());
						Files.copy(zis, subFile);
					}
				}
			}
		}
		return true;
	}
	
	@SuppressWarnings("unused")
	private static void addFiles(String paramString1, String paramString2, ZipOutputStream paramZipOutputStream) throws IOException {
		// NOP
	}
	
	static boolean createJar(String paramString1, String paramString2) throws IOException {
		// NOP
		return false;
	}
	
	static boolean compile(String paramString1, String[] paramArrayOfString, String paramString2, int paramInt, boolean paramBoolean1, boolean paramBoolean2, String paramString3) {
		// NOP
		return false;
	}
}
