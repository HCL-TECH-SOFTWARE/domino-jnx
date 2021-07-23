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
package com.hcl.domino.jna.test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.GZIPInputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClient.Encryption;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.DominoProcess;
import com.hcl.domino.data.Database;
import com.hcl.domino.dxl.DxlImporter;
import com.hcl.domino.dxl.DxlImporter.DXLImportOption;
import com.hcl.domino.dxl.DxlImporter.XMLValidationOption;
import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.StreamUtil;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressWarnings("nls")
@SuppressFBWarnings("UI_INHERITANCE_UNSAFE_GETRESOURCE")
public abstract class AbstractJNARuntimeTest {
	private static ThreadLocal<DominoClient> threadClient = new ThreadLocal<>();

	private static boolean initialized = false;
	
	public DominoClient getClient() {
		return threadClient.get();
	}
	
	public DominoClient reloadClient() throws IOException {
		if (threadClient.get() != null) {
			threadClient.get().close();
			
			threadClient.set(DominoClientBuilder.newDominoClient().build());
		}
		else {
			threadClient.set(DominoClientBuilder.newDominoClient().build());
		}
		
		return getClient();
	}
	
	@BeforeAll
	public static void initRuntime() {
		if(!initialized) {
			initialized = true;
			initRuntime(true);
		}
	}
	
	public static void initRuntime(boolean addShutdownHook) {
		String notesProgramDir = System.getenv("Notes_ExecDirectory");
		String notesIniPath = System.getenv("NotesINI");
		if (StringUtil.isNotEmpty(notesProgramDir)) {
			String[] initArgs = new String[] {
					notesProgramDir,
					StringUtil.isEmpty(notesIniPath) ? "" : ("=" + notesIniPath) //$NON-NLS-1$ 
			};
			
			DominoProcess.get().initializeProcess(initArgs);
		} else {
			throw new IllegalStateException("Unable to locate Notes runtime");
		}

		//prevent ID password prompt
		String idFilePath = System.getenv("Notes_IDPath");
		String idPassword = System.getenv("Notes_IDPassword");
		if (!StringUtil.isEmpty(idPassword)) {
			DominoProcess.get().switchToId(StringUtil.isEmpty(idFilePath) ? null : Paths.get(idFilePath), idPassword, true);
		}

		if(addShutdownHook) {
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				DominoProcess.get().terminateProcess();
			}));
		}
	}

	@BeforeEach
	public void initClient() throws IOException {
		if (threadClient.get() == null) {
			DominoProcess.get().initializeThread();
			threadClient.set(DominoClientBuilder.newDominoClient().build());
		}
	}

	@AfterEach
	public void termClient() throws IOException {
		if(threadClient.get() != null) {
			threadClient.get().close();
			threadClient.set(null);
			DominoProcess.get().terminateThread();
		}
	}
	
	@FunctionalInterface
	protected static interface DatabaseConsumer {
		void accept(Database database) throws Exception;
	}
	
	protected void withTempDb(DatabaseConsumer c) throws Exception {
		DominoClient client = getClient();
		Path tempDest = Files.createTempFile(getClass().getName(), ".nsf"); //$NON-NLS-1$
		Files.delete(tempDest);
		Database database = client.createDatabase(null, tempDest.toString(), false, true, Encryption.None);
		assertNotEquals(null, database);
		try {
			c.accept(database);
		} finally {
			database.close();
			try {
				client.deleteDatabase(null, tempDest.toString());
			} catch(Throwable t) {
				System.err.println("Unable to delete database " + tempDest + ": " + t);
			}
		}
	}
	
	protected void withResourceDxl(String resDirPath, DatabaseConsumer c) throws Exception {
		withTempDb(database -> {
			DxlImporter importer = getClient().createDxlImporter();
			importer.setInputValidationOption(XMLValidationOption.NEVER);
			importer.setDesignImportOption(DXLImportOption.REPLACE_ELSE_CREATE);
			importer.setReplicaRequiredForReplaceOrUpdate(false);
			getResourceFiles(resDirPath).stream()
				.filter(Objects::nonNull)
				.map(name -> PathUtil.concat("/", name, '/'))
				.map(name ->
					StringUtil.endsWithIgnoreCase(name, ".xml") ?
						(InputStream)getClass().getResourceAsStream(name) :
					StringUtil.endsWithIgnoreCase(name, ".xml.gz") ?
						call(() -> new GZIPInputStream(getClass().getResourceAsStream(name))) :
						null
				)
				.filter(Objects::nonNull)
				.forEach(is -> {
					try {
						importer.importDxl(is, database);
					} catch (IOException e) {
						throw new RuntimeException(e);
					} finally {
						StreamUtil.close(is);
					}
				});
			
			c.accept(database);
		});
	}
	
	private static List<String> getResourceFiles(final String path) throws IOException {
		List<String> result = new ArrayList<>();
		final File jarFile = new File(AbstractJNARuntimeTest.class.getProtectionDomain().getCodeSource().getLocation().getPath());

		if (jarFile.isFile()) { // Run with JAR file
			final JarFile jar = new JarFile(jarFile);
			final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
			while (entries.hasMoreElements()) {
				final String name = entries.nextElement().getName();
				if (name.startsWith(path + "/")) { //filter according to the path
					result.add(name);
				}
			}
			jar.close();
		} else if(jarFile.isDirectory()) {
			File subDir = new File(jarFile, path);
			if(subDir != null && subDir.exists()) {
				String[] paths = subDir.list();
				if(paths != null) {
					for(String name : paths) {
						result.add(path + "/" + name);
					}
				}
			}
		} else { // Run with IDE
			final URL url = AbstractJNARuntimeTest.class.getResource("/" + path);
			if (url != null) {
				try {
					final File apps = new File(url.toURI());
					File[] files = apps.listFiles();
					if(files != null) {
						for (File app : files) {
							result.add(app.getPath());
						}
					}
				} catch (URISyntaxException ex) {
					// never happens
				}
			}
		}
		return result;
	}
	
	private static <T> T call(final Callable<T> callable) {
		try {
			return callable.call();
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
