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
package com.hcl.domino.jna.internal.structs;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * CONSOLE_ENTRY structure
 */
public class NotesConsoleEntryStruct extends Structure {
	/** C type : QUEUE_ENTRY_HEADER */
	public int nextEntry;
	public int prevEntry;
	/** What type of data is this? */
	public short type;
	public short signals;
	public int consoleBufferID;
	/** length of the data */
	public int length;
	
	public NotesConsoleEntryStruct() {
		super();
	}
	
	public static NotesConsoleEntryStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesConsoleEntryStruct>) () -> {
			return new NotesConsoleEntryStruct();
		});
	}
	
	@SuppressWarnings("nls")
  @Override
  protected List<String> getFieldOrder() {
		return Arrays.asList("nextEntry", "prevEntry", "type", "signals", "consoleBufferID", "length");
	}

	public NotesConsoleEntryStruct(int nextEntry, int prevEntry, short Type, short Signals, int ConsoleBufferID, int Length) {
		super();
		this.nextEntry = nextEntry;
		this.prevEntry = prevEntry;
		this.type = Type;
		this.signals = Signals;
		this.consoleBufferID = ConsoleBufferID;
		this.length = Length;
	}
	
	public static NotesConsoleEntryStruct newInstance(int nextEntry, int prevEntry, short type, short signals, int consoleBufferID, int length) {
		return AccessController.doPrivileged((PrivilegedAction<NotesConsoleEntryStruct>) () -> {
			return new NotesConsoleEntryStruct(nextEntry, prevEntry, type, signals, consoleBufferID, length);
		});
	}
	
	public NotesConsoleEntryStruct(Pointer peer) {
		super(peer);
	}
	
	public static NotesConsoleEntryStruct newInstance(Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<NotesConsoleEntryStruct>) () -> {
			return new NotesConsoleEntryStruct(peer);
		});
	}
	
	public static class ByReference extends NotesConsoleEntryStruct implements Structure.ByReference {
		
	};
	public static class ByValue extends NotesConsoleEntryStruct implements Structure.ByValue {
		
	};
}
