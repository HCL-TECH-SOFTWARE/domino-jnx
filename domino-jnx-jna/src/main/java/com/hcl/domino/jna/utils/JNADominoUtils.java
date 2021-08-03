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
package com.hcl.domino.jna.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.function.Function;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.hcl.domino.DominoClient;
import com.hcl.domino.admin.idvault.UserId;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.naming.Names;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import org.xml.sax.SAXException;

public enum JNADominoUtils {
	;

	/**
	 * Constructs a network path of a database (server!!path with proper encoding)
	 * 
	 * @param client   DominoClient
	 * @param server   server or null
	 * @param filePath filepath
	 * @return LMBCS encoded path
	 */
	public static DisposableMemory constructNetPath(DominoClient client, String server, String filePath) {
		if (server == null) {
			server = ""; //$NON-NLS-1$
		}
		Objects.requireNonNull(filePath, "filePath is null");

		server = Names.toCanonical(server);

		Memory dbServerLMBCS = NotesStringUtils.toLMBCS(server, true);
		Memory dbFilePathLMBCS = NotesStringUtils.toLMBCS(filePath, true);
		DisposableMemory retFullNetPath = new DisposableMemory(NotesConstants.MAXPATH);
		try {
			short result = NotesCAPI.get().OSPathNetConstruct(null, dbServerLMBCS, dbFilePathLMBCS, retFullNetPath);
			NotesErrorUtils.checkResult(result);

			// reduce length of retDbPathName
			int newLength = 0;
			for (int i = 0; i < retFullNetPath.size(); i++) {
				byte b = retFullNetPath.getByte(i);
				if (b == 0) {
					newLength = i;
					break;
				}
			}
			byte[] retFullNetPathArr = retFullNetPath.getByteArray(0, newLength);

			DisposableMemory reducedFullNetPathMem = new DisposableMemory(newLength + 1);
			reducedFullNetPathMem.write(0, retFullNetPathArr, 0, retFullNetPathArr.length);
			reducedFullNetPathMem.setByte(newLength, (byte) 0);
			return reducedFullNetPathMem;
		} finally {
			retFullNetPath.dispose();
		}
	}

	/**
	 * Utility method to provide synchronized access to a {@link UserId}'s backing
	 * pointer.
	 * 
	 * <p>
	 * This method expects that the implementation will be able to provide either a
	 * {@link PointerByReference} or a {@link Long} representing the
	 * {@code KFHANDLE*} value (which is to say, a {@code void**}).
	 * </p>
	 * 
	 * @param <T>      the type of value to return
	 * @param userId   the user ID to access
	 * @param consumer a {@link Function} processing the pointer from the ID
	 * @return the value produced by {@code consumer}
	 * @throws IllegalArgumentException if the {@link UserId} implementation cannot
	 *                                  provide a {@link PointerByReference}
	 */
	public static <T> T accessKFC(UserId userId, Function<PointerByReference, T> consumer) {
		{
			// Check for a JNA-native PointerByReference first, since synchronization is
			// better with it
			PointerByReference phKFC = userId.getAdapter(PointerByReference.class);
			if (phKFC != null) {
				synchronized (phKFC) {
					return consumer.apply(phKFC);
				}
			}
		}
		{
			// Failing that, check for a generic native pointer-to-pointer value
			Long longPhKFC = userId.getAdapter(Long.class);
			if (longPhKFC != null) {
				Pointer pphKFC = new Pointer(longPhKFC);
				Pointer hKFC = pphKFC.getPointer(0);
				PointerByReference phKFC = new PointerByReference(hKFC);
				synchronized (userId) {
					return consumer.apply(phKFC);
				}
			}
		}
		throw new IllegalArgumentException(
				MessageFormat.format("Unable to retrieve pointer from ID of type {0}", userId.getClass().getName()));
	}

	/**
	 * Parses the provided XML string into a DOM Document object.
	 * 
	 * @param xml the string representing the XML document
	 * @return a {@link org.w3c.dom.Document Document} parsed from the string
	 * @throws ParserConfigurationException if there is a problem creating the DOM
	 *                                      parser
	 * @throws SAXException                 if there is a problem parsing the
	 *                                      provided string
	 * @since 1.0.24
	 */
	public static org.w3c.dom.Document parseXml(String xml) throws ParserConfigurationException, SAXException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		try (InputStream is = new ByteArrayInputStream(xml.getBytes())) {
			return builder.parse(is);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
