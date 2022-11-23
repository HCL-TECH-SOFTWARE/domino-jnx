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
package com.hcl.domino.jna.data;

import static com.hcl.domino.commons.util.NotesErrorUtils.checkResult;
import static java.text.MessageFormat.format;

import java.lang.ref.ReferenceQueue;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoException;
import com.hcl.domino.UserNamesList;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.ListUtil;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.PlatformUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.exception.ObjectDisposedException;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.NotesNamingUtils;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.callbacks.NotesCallbacks;
import com.hcl.domino.jna.internal.callbacks.Win32NotesCallbacks;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNAAclAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNADatabaseAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.HANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.security.Acl;
import com.hcl.domino.security.AclAccess;
import com.hcl.domino.security.AclEntry;
import com.hcl.domino.security.AclFlag;
import com.hcl.domino.security.AclLevel;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.ShortByReference;

/**
 * Container to access the ACL of a database
 * 
 * @author Tammo Riedinger
 */
public class JNAAcl extends BaseJNAAPIObject<JNAAclAllocations> implements Acl {
	JNAAcl(IAPIObject<?> parent, DHANDLE hAcl) {
		super(parent);
		
		init(hAcl);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected JNAAclAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		
		return new JNAAclAllocations(parentDominoClient, parentAllocations, this, queue);
	}
	
	/**
	 * Clones the ACL data and returns a new {@link JNAAcl} that has
	 * the {@link DominoClient} as its parent object.
	 * 
	 * @return acl clone
	 */
	public JNAAcl cloneDetached() {
		checkDisposed();
		
		DHANDLE aclHandle = getAllocations().getAclHandle();
		
		return LockUtil.lockHandle(aclHandle, (hAclByVal) -> {
			DHANDLE.ByReference retNewHandle = DHANDLE.newInstanceByReference();
			
			short result = NotesCAPI.get().ACLCopy(hAclByVal, retNewHandle);
			NotesErrorUtils.checkResult(result);
			
			return new JNAAcl(getParentDominoClient(), retNewHandle);
		});
	}
	
	private void init(DHANDLE hAcl) {
		getAllocations().setAclHandle(hAcl);
	}

	@Override
	public List<String> getRoles() {
		Map<Integer,String> rolesMap = getRolesByIndex();
		
		ArrayList<Integer> indexList=new ArrayList<>(rolesMap.keySet());
		Collections.sort(indexList);
		
		ArrayList<String> rolesList=new ArrayList<>();
		for (Integer idx:indexList) {
			rolesList.add(rolesMap.get(idx));
		}

		return rolesList;
	}
	
	/**
	 * Returns the role names hashed by their internal position
	 * 
	 * @return roles
	 */
	private Map<Integer,String> getRolesByIndex() {
		checkDisposed();
		
		Map<Integer,String> roles = new HashMap<>();

		JNAAclAllocations allocations=getAllocations();
		
		DHANDLE hAcl=allocations.getAclHandle();
		hAcl.checkDisposed();

		DisposableMemory retPrivName = new DisposableMemory(NotesConstants.ACL_PRIVSTRINGMAX);
		
		try {
			LockUtil.lockHandle(hAcl, (hAclByVal)-> {
				short result;
				
				// Privilege names associated with privilege numbers 0 through 4 are privilege levels compatible with versions of Notes prior to Release 3
				for (int i=5; i<NotesConstants.ACL_PRIVCOUNT; i++) {
					retPrivName.clear();
					
					result = NotesCAPI.get().ACLGetPrivName(hAclByVal, (short) (i & 0xffff), retPrivName);
					if ((result & NotesConstants.ERR_MASK)==1060)  { //Error "The name is not in the list" => no more entries
						break;
					}
	
					checkResult(result);
	
					String role = NotesStringUtils.fromLMBCS(retPrivName, -1);
					if (!StringUtil.isEmpty(role)) {
						roles.put(i, role);
					}
				}
				return 0;
			});
		}
		finally {
			retPrivName.dispose();
		}

		return roles;
	}
	
	@Override
	public void addRole(String role) {
		if (role==null) {
			throw new IllegalArgumentException("Cannot add role with empty name");
		}
		
		checkDisposed();

		String roleStripped = role;
		
		if (roleStripped.startsWith("[")) { //$NON-NLS-1$
			roleStripped = roleStripped.substring(1);
		}
		
		if (roleStripped.endsWith("]")) { //$NON-NLS-1$
			roleStripped = roleStripped.substring(0, roleStripped.length()-1);
		}
		
		if (roleStripped.length()==0) {
			throw new IllegalArgumentException("Cannot add role with empty name");
		}
		
		if (roleStripped.length() >= NotesConstants.ACL_PRIVNAMEMAX) {
			throw new IllegalArgumentException(format("Role name length (content within brackets) cannot exceed {0} characters", (NotesConstants.ACL_PRIVNAMEMAX-1)));
		}

		String roleWithBrackets = "[" + role + "]"; //$NON-NLS-1$ $NON-NLS-2$
		
		List<String> roles = getRoles();
		if (roles.contains(roleWithBrackets)) {
			return;
		}
		
		Map<Integer,String> rolesByIndex = getRolesByIndex();
		int freeIndex = -1;
		
		for (int i=5; i<NotesConstants.ACL_PRIVCOUNT; i++) {
			if (!rolesByIndex.containsKey(i) || "".equals(rolesByIndex.get(i))) { //$NON-NLS-1$
				freeIndex = i;
				break;
			}
		}
		
		if (freeIndex==-1) {
			throw new DominoException(0, "No more space available to add role");
		}
		
		JNAAclAllocations allocations=getAllocations();
		
		DHANDLE hAcl=allocations.getAclHandle();
		hAcl.checkDisposed();
		
		Memory roleStrippedMem = NotesStringUtils.toLMBCS(roleStripped, true);
		
		final int fFreeIndex = freeIndex;
		
		short result=LockUtil.lockHandle(hAcl, (hAclByVal)-> {
			return NotesCAPI.get().ACLSetPrivName(hAclByVal, (short) (fFreeIndex & 0xffff), roleStrippedMem);
		});
		
		checkResult(result);
	}
	
	@Override
	public void removeRole(String role) {
		if (role==null || role.length()==0) {
			throw new IllegalArgumentException("Cannot remove role with empty name");
		}
		
		checkDisposed();
		
		if (!role.startsWith("[")) { //$NON-NLS-1$
			role = "[" + role; //$NON-NLS-1$
		}
		if (!role.endsWith("]")) { //$NON-NLS-1$
			role = role + "]"; //$NON-NLS-1$
		}

		Map<Integer,String> rolesByIndex = getRolesByIndex();
		int roleIndex = -1;
		
		for (int i=5; i<NotesConstants.ACL_PRIVCOUNT; i++) {
			if (role.equals(rolesByIndex.get(i))) {
				roleIndex = i;
				break;
			}
		}

		if (roleIndex==-1) {
			//nothing to do
			return;
		}
		
		JNAAclAllocations allocations=getAllocations();
		
		DHANDLE hAcl=allocations.getAclHandle();
		hAcl.checkDisposed();
		
		int byteOffsetWithBit = roleIndex / 8;
		int bitToCheck = (int) Math.pow(2, roleIndex % 8);

		int removedRoleIndex = roleIndex;
		
		short result=LockUtil.lockHandle(hAcl, (hAclByVal)-> {
			short updateResult;
			
			for (JNAAclEntry currAclEntry : getNotesEntriesByName().values()) {
				String currName = currAclEntry.getName();
			
				byte[] currPrivileges = currAclEntry.getPrivilegesArray();
				if ((currPrivileges[byteOffsetWithBit] & bitToCheck) == bitToCheck) {
					byte[] newPrivileges = currPrivileges.clone();
					newPrivileges[byteOffsetWithBit] = (byte) ((newPrivileges[byteOffsetWithBit] - bitToCheck & 0xff));
					
					Memory currNameMem = NotesStringUtils.toLMBCS(currName, true);
					
					DisposableMemory newPrivilegesMem = new DisposableMemory(newPrivileges.length);
					newPrivilegesMem.write(0, newPrivileges, 0, newPrivileges.length);
					
					try {
						updateResult = NotesCAPI.get().ACLUpdateEntry(hAclByVal, currNameMem, NotesConstants.ACL_UPDATE_PRIVILEGES, null, (short) 0,
									newPrivilegesMem, (short) 0);

						checkResult(updateResult);
					}
					finally {
						newPrivilegesMem.dispose();
					}
				}
			}
			
			Memory emptyStrMem = NotesStringUtils.toLMBCS("", true); //$NON-NLS-1$
			
			return NotesCAPI.get().ACLSetPrivName(hAclByVal, (short) (removedRoleIndex & 0xffff), emptyStrMem);
		});
		
		checkResult(result);
	}
	
	@Override
	public void renameRole(String oldName, String newName) {
		if (oldName==null || newName==null) {
			throw new IllegalArgumentException("Neither the former role name, nor the new can be empty");
		}
		
		checkDisposed();
		
		String oldNameStripped = oldName;
		if (oldNameStripped.startsWith("[")) { //$NON-NLS-1$
			oldNameStripped = oldNameStripped.substring(1);
		}
		if (oldNameStripped.endsWith("]")) { //$NON-NLS-1$
			oldNameStripped = oldNameStripped.substring(0, oldNameStripped.length()-1);
		}

		String newNameStripped = newName;
		if (newNameStripped.startsWith("[")) { //$NON-NLS-1$
			newNameStripped = newNameStripped.substring(1);
		}
		if (newNameStripped.endsWith("]")) { //$NON-NLS-1$
			newNameStripped = newNameStripped.substring(0, newNameStripped.length()-1);
		}
		
		if (oldNameStripped.length()==0 || newNameStripped.length()==0) {
			throw new IllegalArgumentException("Neither the former role name, nor the new can be empty");
		}

		if (newNameStripped.length() >= NotesConstants.ACL_PRIVNAMEMAX) {
			throw new IllegalArgumentException(format("Role name length (content within brackets) cannot exceed {0} characters", (NotesConstants.ACL_PRIVNAMEMAX-1)));
		}

		if (oldNameStripped.equals(newNameStripped)) {
			return; // nothing to do
		}

		String oldNameWithBrackets = "[" + oldNameStripped + "]"; //$NON-NLS-1$ //$NON-NLS-2$
		String newNameWithBrackets = "[" + newNameStripped + "]"; //$NON-NLS-1$ //$NON-NLS-2$
		
		Map<Integer,String> rolesByIndex = getRolesByIndex();
		int roleIndex = -1;
		int newRoleIndex = -1;
		
		for (Entry<Integer,String> currEntry : rolesByIndex.entrySet()) {
			Integer currIndex = currEntry.getKey();
			String currRole = currEntry.getValue();
			
			if (currRole.equalsIgnoreCase(oldNameWithBrackets)) {
				roleIndex = currIndex;
			}
			if (currRole.contentEquals(newNameWithBrackets)) {
				newRoleIndex = currIndex;
			}
		}
		
		if (roleIndex==-1) {
			throw new DominoException(0, format("Role not found in ACL: {0}", oldName));
		}
		if (newRoleIndex!=-1) {
			throw new DominoException(0, format("Role already exists in ACL: {0}", newName));
		}
		
		DHANDLE hAcl=getAllocations().getAclHandle();
		hAcl.checkDisposed();
		
		Memory newNameStrippedMem = NotesStringUtils.toLMBCS(newNameStripped, true);
		
		final int fRenameIdx=roleIndex;
		short result=LockUtil.lockHandle(hAcl, (hAclByValue) -> {
			return NotesCAPI.get().ACLSetPrivName(hAclByValue, (short) (fRenameIdx & 0xffff), newNameStrippedMem);
		});
		checkResult(result);
	}
	
	private Map<String, JNAAclEntry> getNotesEntriesByName() {
		checkDisposed();
		
		JNAAclAllocations allocations=getAllocations();
		
		DHANDLE hAcl=allocations.getAclHandle();
		hAcl.checkDisposed();
		
		final LinkedHashMap<String, JNAAclEntry> aclAccessInfoByName = new LinkedHashMap<>();
		
		final Map<Integer,String> rolesByIndex = getRolesByIndex();
		
		NotesCallbacks.ACLENTRYENUMFUNC callback = (enumFuncParam, nameMem, accessLevelShort, privileges, accessFlag) -> {

			String name = NotesStringUtils.fromLMBCS(nameMem, -1);
			AclLevel accessLevel = DominoEnumUtil.valueOf(AclLevel.class, Short.toUnsignedInt(accessLevelShort))
				.orElseThrow(() -> new IllegalStateException(MessageFormat.format("Cannot identify access level for {0}", Short.toUnsignedInt(accessLevelShort))));

			int iAccessFlag = accessFlag & 0xffff;
			EnumSet<AclFlag> retFlags = EnumSet.noneOf(AclFlag.class);
			for (AclFlag currFlag : AclFlag.values()) {
				if ((iAccessFlag & currFlag.getValue()) == currFlag.getValue()) {
					retFlags.add(currFlag);
				}
			}

			byte[] privilegesArr = privileges.getByteArray(0, 10);

			List<String> entryRoles = new ArrayList<>();

			// Privilege names associated with privilege numbers 0 through 4 are privilege levels compatible with versions of Notes prior to Release 3
			for (int i = 5; i < NotesConstants.ACL_PRIVCOUNT; i++) {
				// convert position to byte/bit position of byte[10]
				int byteOffsetWithBit = i / 8;
				byte byteValueWithBit = privilegesArr[byteOffsetWithBit];
				int bitToCheck = (int) Math.pow(2, i % 8);

				boolean enabled = (byteValueWithBit & bitToCheck) == bitToCheck;
				if (enabled) {
					String currRole = rolesByIndex.get(i);
					entryRoles.add(currRole);
				}
			}

			JNAAclEntry access = new JNAAclEntry(name, accessLevel, entryRoles, privilegesArr, retFlags);
			aclAccessInfoByName.put(name, access);
		};
		
		short result;
		if (PlatformUtils.isWin32()) {
			//make sure to call the C function with a StdCallCallback implementation on Win32
			Win32NotesCallbacks.ACLENTRYENUMFUNCWin32 callbackWin32 = (enumFuncParam, nameMem, accessLevelShort, privileges, accessFlag) -> {
				callback.invoke(enumFuncParam, nameMem, accessLevelShort, privileges, accessFlag);
			};
			result = LockUtil.lockHandle(hAcl, (hAclByValue) -> {
				return NotesCAPI.get().ACLEnumEntries(hAclByValue, callbackWin32, null);
			});
		}
		else {
			result = LockUtil.lockHandle(hAcl, (hAclByValue) -> {
				return NotesCAPI.get().ACLEnumEntries(hAclByValue, callback, null);
			});
		}
		
		checkResult(result);
		
		return aclAccessInfoByName;
	}

	@Override
	public List<AclEntry> getEntries() {
		return new ArrayList<>(getNotesEntriesByName().values());
	}

	@Override
	public Optional<AclEntry> getEntry(String name) {
		if (name==null || name.length()==0) {
			throw new IllegalArgumentException("Cannot search for entry with empty name");
		}
		
		Map<String, JNAAclEntry> entriesByName = getNotesEntriesByName();
		
		JNAAclEntry entry = entriesByName.get(name);
		if (entry==null) {
			for (JNAAclEntry currEntry : entriesByName.values()) {
				if (NotesNamingUtils.equalNames(currEntry.getName(), name)) {
					return Optional.of(currEntry);
				}
			}
		}
		return Optional.ofNullable(entry);
	}
	
	@Override
	public void addEntry(String name, AclLevel accessLevel, List<String> roles, Collection<AclFlag> accessFlags) {
		if (name==null || name.length()==0) {
			throw new IllegalArgumentException("Cannot add entry with empty name");
		}
		
		List<String> rolesFormatted;
		if (roles==null) {
			rolesFormatted = Collections.emptyList();
		}
		else if (roles.isEmpty()) {
			rolesFormatted = roles;
		}
		else {
			boolean rolesOk = true;
			
			for (String currRole : roles) {
				if (!currRole.startsWith("[")) { //$NON-NLS-1$
					rolesOk = false;
					break;
				}
				else if (!currRole.endsWith("]")) { //$NON-NLS-1$
					rolesOk = false;
					break;
				}
			}
			
			if (rolesOk) {
				rolesFormatted = roles;
			}
			else {
				rolesFormatted = new ArrayList<>();
				for (String currRole : roles) {
					if (!currRole.startsWith("[")) { //$NON-NLS-1$
						currRole = "[" + currRole; //$NON-NLS-1$
					}
					if (!currRole.endsWith("]")) { //$NON-NLS-1$
						currRole = currRole + "]"; //$NON-NLS-1$
					}
					rolesFormatted.add(currRole);
				}
			}
		}
		
		String nameCanonical = NotesNamingUtils.toCanonicalName(name);
		
		Map<Integer,String> rolesByIndex = getRolesByIndex();
		
		byte[] privilegesArr = new byte[NotesConstants.ACL_PRIVCOUNT / 8];

		for (int i=5; i<NotesConstants.ACL_PRIVCOUNT; i++) {
			String currRole = rolesByIndex.get(i);
			if (currRole!=null) {
				if (ListUtil.containsIgnoreCase(rolesFormatted, currRole)) {
					int byteOffsetWithBit = i / 8;
					int bitToCheck = (int) Math.pow(2, i % 8);
					
					privilegesArr[byteOffsetWithBit] = (byte) ((privilegesArr[byteOffsetWithBit] | bitToCheck) & 0xff);
				}
			}
		}
		
		DHANDLE hAcl=getAllocations().getAclHandle();
		hAcl.checkDisposed();
		
		Memory nameCanonicalMem = NotesStringUtils.toLMBCS(nameCanonical, true);

		short accessFlagsAsShort = DominoEnumUtil.toBitField(AclFlag.class, accessFlags);
		
		DisposableMemory privilegesMem = new DisposableMemory(privilegesArr.length);
		try {
			privilegesMem.write(0, privilegesArr, 0, privilegesArr.length);
			
			short result=LockUtil.lockHandle(hAcl, (hAclByValue)-> {
				return NotesCAPI.get().ACLAddEntry(hAclByValue, nameCanonicalMem, (short) (accessLevel.getValue() & 0xffff), privilegesMem, accessFlagsAsShort);
			});
			checkResult(result);
		}
		finally {
			privilegesMem.dispose();
		}
	}

	@Override
	public void removeEntry(String name) {
		if (name==null || name.length()==0) {
			throw new IllegalArgumentException("Cannot remove entry with empty name");
		}
		
		checkDisposed();
		
		DHANDLE hAcl=getAllocations().getAclHandle();
		hAcl.checkDisposed();
		
		short result=LockUtil.lockHandle(hAcl, (hAclByValue)-> {
			String nameCanonical = NotesNamingUtils.toCanonicalName(name);
			Memory nameCanonicalMem = NotesStringUtils.toLMBCS(nameCanonical, true);

			return NotesCAPI.get().ACLDeleteEntry(hAclByValue, nameCanonicalMem);
		});
		
		checkResult(result);
	}
	
	@Override
	public void updateEntry(String name, String newName, AclLevel newAccessLevel, List<String> newRoles, Collection<AclFlag> newFlags) {
		if (name==null || name.isEmpty()) {
			throw new IllegalArgumentException("Cannot update entry with empty name");
		}
		
		int updateFlags = 0;
		
		AclEntry oldAclEntry = getEntry(name).orElse(null);
		if (oldAclEntry==null) {
			if (newName==null) {
				newName = name;
			}
			addEntry(newName, newAccessLevel, newRoles, newFlags);
			return;
		}
		
		Memory oldAclEntryNameMem = "-default-".equalsIgnoreCase(oldAclEntry.getName()) ? null : NotesStringUtils.toLMBCS(oldAclEntry.getName(), true); //$NON-NLS-1$
		
		Memory newNameMem = null;
		
		if("".equals(newName)) { //$NON-NLS-1$
			throw new IllegalArgumentException("Cannot set the entry name to an empty string");
		} else if (newName != null) {
			newName = NotesNamingUtils.toCanonicalName(newName);
			
			if (!NotesNamingUtils.equalNames(oldAclEntry.getName(), newName)) {
				updateFlags = updateFlags | NotesConstants.ACL_UPDATE_NAME;
				
				newNameMem = NotesStringUtils.toLMBCS(newName, true);
			}
		}
		
		int iNewAccessLevel = oldAclEntry.getAclLevel().getValue();
		// TODO somehow it seems this flag always needs to be set
		// 			otherwise the level will be reset to NOACCESS, in case the level did not change
		updateFlags = updateFlags | NotesConstants.ACL_UPDATE_LEVEL;
		if (newAccessLevel!=null && !newAccessLevel.equals(oldAclEntry.getAclLevel())) {
			updateFlags = updateFlags | NotesConstants.ACL_UPDATE_LEVEL;
			
			iNewAccessLevel = newAccessLevel.getValue();
		}
		
		DisposableMemory newPrivilegesMem = null;
		
		if (newRoles!=null && !newRoles.equals(oldAclEntry.getRoles())) {
			updateFlags = updateFlags | NotesConstants.ACL_UPDATE_PRIVILEGES;
			
			List<String> newRolesFormatted;
			if (newRoles.isEmpty()) {
				newRolesFormatted = newRoles;
			}
			else {
				boolean rolesOk = true;
				
				for (String currRole : newRoles) {
					if (!currRole.startsWith("[")) { //$NON-NLS-1$
						rolesOk = false;
						break;
					}
					else if (!currRole.endsWith("]")) { //$NON-NLS-1$
						rolesOk = false;
						break;
					}
				}
				
				if (rolesOk) {
					newRolesFormatted = newRoles;
				}
				else {
					newRolesFormatted = new ArrayList<>();
					for (String currRole : newRoles) {
						if (!currRole.startsWith("[")) { //$NON-NLS-1$
							currRole = "[" + currRole; //$NON-NLS-1$
						}
						if (!currRole.endsWith("]")) { //$NON-NLS-1$
							currRole = currRole + "]"; //$NON-NLS-1$
						}
						newRolesFormatted.add(currRole);
					}
				}
			}
			
			Map<Integer,String> rolesByIndex = getRolesByIndex();
			byte[] newPrivilegesArr = new byte[NotesConstants.ACL_PRIVCOUNT / 8];
			
			for (int i=5; i<NotesConstants.ACL_PRIVCOUNT; i++) {
				String currRole = rolesByIndex.get(i);
				if (currRole!=null) {
					if (ListUtil.containsIgnoreCase(newRolesFormatted, currRole)) {
						int byteOffsetWithBit = i / 8;
						int bitToCheck = (int) Math.pow(2, i % 8);
						
						newPrivilegesArr[byteOffsetWithBit] = (byte) ((newPrivilegesArr[byteOffsetWithBit] | bitToCheck) & 0xff);
					}
				}
			}
			
			newPrivilegesMem = new DisposableMemory(NotesConstants.ACL_PRIVCOUNT / 8);
			newPrivilegesMem.write(0, newPrivilegesArr, 0, newPrivilegesArr.length);
		}
		
		short newFlagsAsShort = DominoEnumUtil.toBitField(AclFlag.class, oldAclEntry.getAclFlags());
		if (newFlags!=null && !newFlags.equals(oldAclEntry.getAclFlags())) {
			updateFlags = updateFlags | NotesConstants.ACL_UPDATE_FLAGS;
			
			newFlagsAsShort = DominoEnumUtil.toBitField(AclFlag.class, newFlags);
		}
		
		try {
			DHANDLE hAcl=getAllocations().getAclHandle();
			hAcl.checkDisposed();
			
			final int fUpdateFlags=updateFlags;
			final Memory fNewNameMem=newNameMem;
			final int fiNewAccessLevel = iNewAccessLevel;
			final Memory fNewPrivilegesMem=newPrivilegesMem;
			final short fNewFlagsAsShort=newFlagsAsShort;
			
			checkResult(LockUtil.lockHandle(hAcl, (hAclByValue) -> {
				return NotesCAPI.get().ACLUpdateEntry(hAclByValue, oldAclEntryNameMem, (short) (fUpdateFlags & 0xffff),
						fNewNameMem, (short) (fiNewAccessLevel & 0xffff),
						fNewPrivilegesMem, fNewFlagsAsShort);
			}));
		}
		finally {
			if (newPrivilegesMem!=null) {
				newPrivilegesMem.dispose();
			}
		}
	}

	@Override
	public String getAdminServer() {
		checkDisposed();
		
		DHANDLE hAcl=getAllocations().getAclHandle();
		hAcl.checkDisposed();
		
		final DisposableMemory retSrvName = new DisposableMemory(NotesConstants.MAXPATH);
		retSrvName.clear();
		
		try {
			short result=LockUtil.lockHandle(hAcl, (hAclByValue)-> {
				return NotesCAPI.get().ACLGetAdminServer(hAclByValue, retSrvName);
			});
			checkResult(result);
			
			return NotesStringUtils.fromLMBCS(retSrvName, -1);
		}
		finally {
			retSrvName.dispose();
		}
		
	}
	
	@Override
	public void setAdminServer(final String server) {
		checkDisposed();
		
		DHANDLE hAcl=getAllocations().getAclHandle();
		hAcl.checkDisposed();
		
		short result=LockUtil.lockHandle(hAcl, (hAclByValue)-> {
			Memory serverCanonicalMem = NotesStringUtils.toLMBCS(NotesNamingUtils.toCanonicalName(server), true);

			return NotesCAPI.get().ACLSetAdminServer(hAclByValue, serverCanonicalMem);
		});
		
		checkResult(result);
	}
	
	private int getAclFlags() {
		checkDisposed();
		
		DHANDLE hAcl=getAllocations().getAclHandle();
		hAcl.checkDisposed();
		
		IntByReference retFlags = new IntByReference();
		
		short result=LockUtil.lockHandle(hAcl, (hAclByValue)-> {
			return NotesCAPI.get().ACLGetFlags(hAclByValue, retFlags);
		});
		
		checkResult(result);
		
		return retFlags.getValue();
	}

	@Override
	public boolean isUniformAccess() {
		return (getAclFlags() & NotesConstants.ACL_UNIFORM_ACCESS) == NotesConstants.ACL_UNIFORM_ACCESS;
	}

	@Override
	public void setUniformAccess(boolean uniformAccess) {
		int aclFlags=getAclFlags();
		
		boolean isSet = (aclFlags & NotesConstants.ACL_UNIFORM_ACCESS) == NotesConstants.ACL_UNIFORM_ACCESS;
		if (uniformAccess == isSet) {
			return;
		}
		
		int newFlags = aclFlags;
		if (uniformAccess) {
			newFlags = newFlags | NotesConstants.ACL_UNIFORM_ACCESS;
		}
		else {
			newFlags -= NotesConstants.ACL_UNIFORM_ACCESS;
		}
		
		DHANDLE hAcl=getAllocations().getAclHandle();
		hAcl.checkDisposed();
		
		final int fNewFlags=newFlags;
		
		short result=LockUtil.lockHandle(hAcl, (hAclByValue)-> {
			return NotesCAPI.get().ACLSetFlags(hAclByValue, fNewFlags);
		});

		checkResult(result);
	}

	@Override
	public void save() {
		IAPIObject<?> parent = getParent();
		if (!(parent instanceof JNADatabase)) {
			throw new UnsupportedOperationException("This ACL instance cannot be saved");
		}
		
		// TODO clarify what happens when between modification of the ACL and saving e.g. a replication
		//			was performed, that also change the ACL (can it be detected, will there be an error?)
		checkDisposed();
		
		JNAAclAllocations allocations=getAllocations();
		
		HANDLE dbHandle=((JNADatabaseAllocations)allocations.getParentAllocations()).getDBHandle();
		dbHandle.checkDisposed();
		
		DHANDLE hAcl=allocations.getAclHandle();
		hAcl.checkDisposed();
		
		short result=LockUtil.lockHandles(dbHandle,hAcl, (dbHandleByValue, hAclByValue) -> {
			return NotesCAPI.get().NSFDbStoreACL(dbHandleByValue, hAclByValue, 0, (short) 0);
		});

		checkResult(result);
	}
	
	@Override
	public AclAccess lookupAccess(final String userName) {
		checkDisposed();
		
		UserNamesList namesList=NotesNamingUtils.buildNamesList(getParent(), userName);
		try {
			return lookupAccess(namesList);
		}
		finally {
			if (namesList instanceof BaseJNAAPIObject) {
				((BaseJNAAPIObject<?>)namesList).dispose();
			}
		}
	}
	
	@Override
	public AclAccess lookupAccess(UserNamesList namesList) {
		if (!(namesList instanceof BaseJNAAPIObject)) {
			throw new IllegalArgumentException("This type of names-list cannot be handled: Expected instance of BaseAPIObject");
		}
		
		if (((BaseJNAAPIObject<?>)namesList).isDisposed()) {
			throw new ObjectDisposedException(namesList);
		}
		
		DHANDLE hNamesList=((BaseJNAAPIObject<?>)namesList).getAdapter(DHANDLE.class);
		if (hNamesList==null) {
			throw new IllegalArgumentException("Cannot aqcuire native handle of names-list");
		}
		
		checkDisposed();
		
		DHANDLE hAcl=getAllocations().getAclHandle();
		hAcl.checkDisposed();
		
		ShortByReference retAccessLevel = new ShortByReference();
		final Memory retPrivileges = new Memory(10);
		final ShortByReference retAccessFlags = new ShortByReference();
		final DHANDLE.ByReference rethPrivNames = DHANDLE.newInstanceByReference();

		checkResult(LockUtil.lockHandles(hAcl, hNamesList, (hAclByValue, hNamesListByValue) -> {
			return Mem.OSLockObject(hNamesListByValue, pNamesList -> {
				return NotesCAPI.get().ACLLookupAccess(hAclByValue, pNamesList, retAccessLevel, 
						retPrivileges, retAccessFlags, rethPrivNames);
			}
					);
		}));

		List<String> roles;
		if (rethPrivNames.isNull()) {
			roles = Collections.emptyList();
		}
		else {
			roles = LockUtil.lockHandle(rethPrivNames, (hPrivNamesByValue)-> {
				Pointer pPrivNames = Mem.OSLockObject(hPrivNamesByValue);
				
				try {
					ShortByReference retTextLength = new ShortByReference();
					Memory retTextPointer = new Memory(Native.POINTER_SIZE);
					
					int numEntriesAsInt = Short.toUnsignedInt(NotesCAPI.get().ListGetNumEntries(pPrivNames, 0));
					
					ArrayList<String> allRoles = new ArrayList<>(numEntriesAsInt);
					short localResult;
					for (int i=0; i<numEntriesAsInt; i++) {
						localResult = NotesCAPI.get().ListGetText(pPrivNames, false, (char) i, retTextPointer, retTextLength);
						checkResult(localResult);
						
						String currRole = NotesStringUtils.fromLMBCS(retTextPointer.getPointer(0), retTextLength.getValue() & 0xffff);
						allRoles.add(currRole);
					}
					return allRoles;
				}
				finally {
					Mem.OSUnlockObject(hPrivNamesByValue);
					Mem.OSMemFree(hPrivNamesByValue);
				}
			});
		}

		int iAccessLevel = retAccessLevel.getValue();
		AclLevel accessLevel = DominoEnumUtil.valueOf(AclLevel.class, iAccessLevel)
			.orElseThrow(() -> new IllegalStateException(MessageFormat.format("Cannot identify access level for {0}", iAccessLevel)));

		int iAccessFlag = retAccessFlags.getValue() & 0xffff;
		EnumSet<AclFlag> retFlags = EnumSet.noneOf(AclFlag.class);
		for (AclFlag currFlag : AclFlag.values()) {
			if ((iAccessFlag & currFlag.getValue()) == currFlag.getValue()) {
				retFlags.add(currFlag);
			}
		}

		return new JNAAclAccess(accessLevel, roles, retFlags);
	}
	
	private static class JNAAclAccess implements AclAccess {
		private AclLevel m_accessLevel;
		private EnumSet<AclFlag> m_accessFlags;
		private List<String> m_roles;
		
		private JNAAclAccess(AclLevel accessLevel, List<String> roles, EnumSet<AclFlag> accessFlags) {
			m_accessLevel = accessLevel;
			m_roles = roles;
			m_accessFlags = accessFlags;
		}
		
		@Override
		public List<String> getRoles() {
			return m_roles;
		}
		
		@Override
		public AclLevel getAclLevel() {
			return m_accessLevel;
		}
		
		@Override
		public EnumSet<AclFlag> getAclFlags() {
			return m_accessFlags;
		}
		
		public boolean isPerson() {
			return m_accessFlags.contains(AclFlag.PERSON);
		}
		
		public boolean isGroup() {
			return m_accessFlags.contains(AclFlag.GROUP);
		}
		
		public boolean isServer() {
			return m_accessFlags.contains(AclFlag.SERVER);
		}
		
		public boolean isAdminServer() {
			return m_accessFlags.contains(AclFlag.ADMIN_SERVER);
		}
		
		@Override
		public String toString() {
			return format("JNAAclAccess [level={0}, roles={1}, flags={2}]", m_accessLevel, m_roles, m_accessFlags); //$NON-NLS-1$
		}
	}
	
	public static class JNAAclEntry extends JNAAclAccess implements AclEntry {
		private String m_name;
		private byte[] m_privilegesArr;

		public JNAAclEntry(String name, AclLevel accessLevel, List<String> roles, byte[] privilegesArr, EnumSet<AclFlag> accessFlags) {
			super(accessLevel, roles, accessFlags);
			m_name = name;
			m_privilegesArr = privilegesArr;
		}
		
		@Override
		public String getName() {
			return m_name;
		}
	
		byte[] getPrivilegesArray() {
			return m_privilegesArr;
		}

		@Override
		public String toString() {
			return format(
				"JNAAclEntry [name={0}, level={1}, roles={2}, flags={3}]", //$NON-NLS-1$
				m_name, getAclLevel(), getRoles(), getAclFlags()
			);
		}

	}
}
