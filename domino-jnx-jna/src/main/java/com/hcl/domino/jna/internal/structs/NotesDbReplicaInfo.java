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
package com.hcl.domino.jna.internal.structs;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
/**
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class NotesDbReplicaInfo extends BaseStructure {
	/**
	 * ID that is same for all replica files<br>
	 * C type : TIMEDATE
	 */
	public NotesTimeDateStruct ID;
	/** Replication flags */
	public short Flags;
	/**
	 * Automatic Replication Cutoff<br>
	 * Interval (Days)
	 */
	public short CutoffInterval;
	/**
	 * Replication cutoff date<br>
	 * C type : TIMEDATE
	 */
	public NotesTimeDateStruct Cutoff;
	
	public NotesDbReplicaInfo() {
		super();
	}

	public static NotesDbReplicaInfo newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesDbReplicaInfo>) () -> new NotesDbReplicaInfo());
	}

	protected List<String> getFieldOrder() {
		return Arrays.asList("ID", "Flags", "CutoffInterval", "Cutoff");
	}
	
	/**
	 * @param ID ID that is same for all replica files<br>
	 * C type : TIMEDATE<br>
	 * @param Flags Replication flags<br>
	 * @param CutoffInterval Automatic Replication Cutoff<br>
	 * Interval (Days)<br>
	 * @param Cutoff Replication cutoff date<br>
	 * C type : TIMEDATE
	 */
	public NotesDbReplicaInfo(NotesTimeDateStruct ID, short Flags, short CutoffInterval, NotesTimeDateStruct Cutoff) {
		super();
		this.ID = ID;
		this.Flags = Flags;
		this.CutoffInterval = CutoffInterval;
		this.Cutoff = Cutoff;
	}
	
	public NotesDbReplicaInfo(Pointer peer) {
		super(peer);
	}
	
	public static NotesDbReplicaInfo newInstance(final Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<NotesDbReplicaInfo>) () -> new NotesDbReplicaInfo(peer));
	}

	public static class ByReference extends NotesDbReplicaInfo implements Structure.ByReference {
		
	};
	
	public static NotesDbReplicaInfo.ByReference newInstanceByReference() {
		return AccessController.doPrivileged((PrivilegedAction<NotesDbReplicaInfo.ByReference>) () -> new NotesDbReplicaInfo.ByReference());
	}

	public static class ByValue extends NotesDbReplicaInfo implements Structure.ByValue {
		
	};
}
