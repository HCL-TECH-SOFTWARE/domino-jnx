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
package com.hcl.domino.jna.naming;

import static com.hcl.domino.commons.util.NotesErrorUtils.checkResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.jna.JNADominoClient;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.naming.UserDirectoryQuery;
import com.sun.jna.Memory;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.0.2
 */
public class JNAUserDirectoryQuery implements UserDirectoryQuery {
	private final JNADominoClient client;
	private final String serverName;
	private boolean exhaustive = false;
	private boolean forceUpdate = false;
	private Collection<String> namespaces = Collections.singleton(NotesConstants.USERNAMESSPACE);
	private List<String> names;
	private List<String> items;
	
	public JNAUserDirectoryQuery(JNADominoClient client, String serverName) {
		this.client = client;
		this.serverName = serverName;
	}

	@Override
	public UserDirectoryQuery exhaustive() {
		this.exhaustive = true;
		return this;
	}

	@Override
	public UserDirectoryQuery forceUpdate() {
		this.forceUpdate = true;
		return this;
	}

	@Override
	public UserDirectoryQuery namespaces(Collection<String> namespaces) {
		this.namespaces = namespaces;
		return this;
	}

	@Override
	public UserDirectoryQuery names(Collection<String> names) {
		this.names = names == null ? Collections.emptyList() : new ArrayList<>(names);
		return this;
	}

	@Override
	public UserDirectoryQuery items(Collection<String> items) {
		this.items = items == null ? Collections.emptyList() : new ArrayList<>(items);
		return this;
	}

	@Override
	public Stream<List<Map<String, List<Object>>>> stream() {
		if(items == null || items.isEmpty()) {
			throw new IllegalArgumentException("names cannot be empty");
		}

		List<String> namesLocal;
		short flags = 0;
		if(this.exhaustive) {
			flags |= NotesConstants.NAME_LOOKUP_EXHAUSTIVE;
		}
		if(this.names == null || this.names.isEmpty()) {
			flags |= NotesConstants.NAME_LOOKUP_ALL;
			namesLocal = Arrays.asList(""); //$NON-NLS-1$
		} else {
			namesLocal = this.names.stream().map(StringUtil::toString).collect(Collectors.toList());
		}
		if(this.forceUpdate) {
			flags |= NotesConstants.NAME_LOOKUP_UPDATE;
		}
		Memory serverName = NotesStringUtils.toLMBCS(this.serverName, true);
		Collection<String> namespacesLocal = this.namespaces == null ? Arrays.asList("") : this.namespaces; //$NON-NLS-1$
		Memory namespaces = NotesStringUtils.toLMBCS(namespacesLocal);
		short nameCount = (short) (namesLocal.size() & 0xffff);
		Memory names = NotesStringUtils.toLMBCS(namesLocal);
		Memory items = NotesStringUtils.toLMBCS(this.items);
		
		DHANDLE.ByReference rethBuffer = DHANDLE.newInstanceByReference();
		checkResult(NotesCAPI.get().NAMELookup2(
			serverName,
			flags,
			(short)(namespacesLocal.size() & 0xffff),
			namespaces,
			nameCount,
			names,
			(short)(this.items == null ? 0 : (this.items.size() & 0xffff)),
			items,
			rethBuffer
		));
		
		JNAUserDirectoryQueryIterator iter = new JNAUserDirectoryQueryIterator(client, rethBuffer, this.items);
		Spliterator<List<Map<String, List<Object>>>> spliterator = Spliterators.spliterator(iter, nameCount+namespacesLocal.size(), 0);
		return StreamSupport.stream(spliterator, false);
	}

}
