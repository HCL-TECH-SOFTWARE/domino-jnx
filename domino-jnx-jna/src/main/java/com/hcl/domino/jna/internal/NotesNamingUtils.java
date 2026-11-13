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
package com.hcl.domino.jna.internal;

import static com.hcl.domino.commons.data.types.JNXTypeUtil.word;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.data.types.StatusImpl;
import com.hcl.domino.commons.data.types.TypedConstants;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.util.PlatformUtils;
import com.hcl.domino.jna.data.JNAUserNamesList;
import com.hcl.domino.jna.data.types.WordByReference;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.utils.ThreadBuffers;
import com.hcl.domino.util.JNXStringUtil;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Notes name utilities to convert between various Name formats and evaluate
 * user groups on a server.
 * 
 * @author Karsten Lehmann
 */
public class NotesNamingUtils {
	private static final int MAX_STRINGCACHE_SIZE = 500;
	
	/**
	 * Names-list-building functions appear to be extremely thread-sensitive, so this lock is used
	 * around each related C call.
	 */
	private static final Object BUILDNAMESLIST_LOCK = new Object();
	
	private static Map<Integer, String> m_nameAbbrCache = Collections.synchronizedMap(new LinkedHashMap<Integer, String>(16,0.75f, true) {
		private static final long serialVersionUID = -5818239831757810895L;

		@Override
		protected boolean removeEldestEntry (Map.Entry<Integer,String> eldest) {
			if (size() > MAX_STRINGCACHE_SIZE) {
				return true;
			}
			else {
				return false;
			}
		}
	});
	private static Map<Integer, String> m_nameCanonicalCache = Collections.synchronizedMap(new LinkedHashMap<Integer, String>(16,0.75f, true) {
		private static final long serialVersionUID = -5818239831757810895L;

		@Override
		protected boolean removeEldestEntry (Map.Entry<Integer,String> eldest) {
			if (size() > MAX_STRINGCACHE_SIZE) {
				return true;
			}
			else {
				return false;
			}
		}
	});
	
	/**
	 * This function converts a distinguished name in abbreviated format to canonical format.
	 * A fully distinguished name is in canonical format - it contains all possible naming components.
	 * The abbreviated format of a distinguished name removes the labels from the naming components.
	 * 
	 * @param name name to convert
	 * @return canonical name
	 */
	public static String toCanonicalName(String name) {
		return toCanonicalName(name, null);
	}
	
	/**
	 * This function converts a list of distinguished names in abbreviated format to canonical format.
	 * A fully distinguished name is in canonical format - it contains all possible naming components.
	 * The abbreviated format of a distinguished name removes the labels from the naming components.
	 * 
	 * @param names list of names
	 * @return list of names in canonical format
	 */
	public static List<String> toCanonicalNames(Collection<String> names) {
		if (names==null) {
			return null;
		}
		
		List<String> namesCanonical = new ArrayList<>(names.size());
		for (String currName : names) {
			namesCanonical.add(toCanonicalName(currName));
		}
		return namesCanonical;
	}
	
	/**
	 * This function converts a distinguished name in abbreviated format to canonical format.
	 * A fully distinguished name is in canonical format - it contains all possible naming components.
	 * The abbreviated format of a distinguished name removes the labels from the naming components.
	 * 
	 * @param name name to convert
	 * @param templateName name to be used when the input name is in common name format
	 * @return canonical name
	 */
    public static String toCanonicalName(String name, String templateName) {
      if (name == null) {
        return null;
      }
      if (name.length() == 0) {
        return name;
      }

      String cacheKey =
          name + ((templateName != null && templateName.length() > 0) ? ("|" + templateName) : ""); //$NON-NLS-1$ //$NON-NLS-2$
      String abbrName = m_nameCanonicalCache.get(System.identityHashCode(cacheKey.intern()));
      if (abbrName != null) {
        return abbrName;
      }

      Memory templateNameMem =
          templateName == null ? null : NotesStringUtils.toLMBCS(templateName, true); // used when
                                                                                      // abbrName is
                                                                                      // only a
                                                                                      // common name
      Memory inNameMem = NotesStringUtils.toLMBCS(name, true);
      WordByReference outLength = new WordByReference();

      ByteBuffer outNameMem = ThreadBuffers.BUFFER_USERNAME.get();
      NotesCAPI.get().DNCanonicalize(0, templateNameMem, inNameMem, outNameMem, TypedConstants.MAXUSERNAME, outLength).orElseThrow();

      String sOutName = NotesStringUtils.fromLMBCS(outNameMem, outLength.getValue());

      m_nameCanonicalCache.put(System.identityHashCode(cacheKey.intern()), sOutName);

      return sOutName;
    }
	
	/**
	 * This function converts a distinguished name in canonical format to abbreviated format.
	 * A fully distinguished name is in canonical format - it contains all possible naming components.
	 * The abbreviated format of a distinguished name removes the labels from the naming components.
	 * 
	 * @param name name to convert
	 * @return abbreviated name
	 */
	public static String toAbbreviatedName(String name) {
		if (name==null) {
			return null;
		}
		if (name.length()==0) {
			return name;
		}
		
		final String cacheKey = name;
		String abbrName = m_nameAbbrCache.get(System.identityHashCode(cacheKey.intern()));
		
		if (abbrName==null) {
		  List<String> st = new ArrayList<>();
		  JNXStringUtil.splitString(name, '/', true, st);
			StringBuilder sb=new StringBuilder(name.length());
			for(final String currToken : st) {
				int iPos = currToken.indexOf("="); //$NON-NLS-1$
				if (sb.length()>0) {
					sb.append("/"); //$NON-NLS-1$
				}
				
				if (iPos!=-1) {
					sb.append(currToken.substring(iPos+1));
				}
				else {
					sb.append(currToken);
				}
			}
			
			abbrName = sb.toString();
			m_nameAbbrCache.put(System.identityHashCode(cacheKey.intern()), abbrName);
		}

		return abbrName;
	}

	/**
	 * This function converts a list of distinguished names in canonical format to abbreviated format.
	 * A fully distinguished name is in canonical format - it contains all possible naming components.
	 * The abbreviated format of a distinguished name removes the labels from the naming components.
	 * 
	 * @param names list of names to convert
	 * @return list of abbreviated names
	 */
	public static List<String> toAbbreviatedNames(Collection<String> names) {
		if (names==null) {
			return null;
		}
		
		List<String> namesAbbr = new ArrayList<>(names.size());
		for (String currName : names) {
			namesAbbr.add(toAbbreviatedName(currName));
		}
		return namesAbbr;
	}
	
	/**
	 * Method to compare two Notes names. We compare the abbreviated forms of both names
	 * ignoring the case
	 * 
	 * @param p_sNotesName1 Notes name 1
	 * @param p_sNotesName2 Notes name 2
	 * @return true if equal
	 */
	public static boolean equalNames(String p_sNotesName1, String p_sNotesName2) {
		String sNotesName1Abbr = toAbbreviatedName(p_sNotesName1);
		String sNotesName2Abbr = toAbbreviatedName(p_sNotesName2);
		
		if (sNotesName1Abbr==null) {
			return sNotesName2Abbr==null;
		}
		else {
			return sNotesName1Abbr.equalsIgnoreCase(sNotesName2Abbr);
		}
	}

	/**
	 * Extracts the common name part of an abbreviated or canonical name
	 * 
	 * @param name abbreviated or canonical name
	 * @return common name
	 */
	public static String toCommonName(String name) {
		int iPos = name.indexOf('/');
		String firstPart = iPos==-1 ? name : name.substring(0, iPos);
		if (JNXStringUtil.startsWithIgnoreCase(firstPart, "cn=")) { //$NON-NLS-1$
			return firstPart.substring(3);
		}
		else {
			return firstPart;
		}
	}
	
	/**
	 * Extracts the common name part of a list of abbreviated or canonical names
	 * 
	 * @param names list of abbreviated or canonical name
	 * @return list of common names
	 */
	public static List<String> toCommonNames(Collection<String> names) {
		if (names==null) {
			return null;
		}
		
		List<String> namesAbbr = new ArrayList<>(names.size());
		for (String currName : names) {
			namesAbbr.add(toCommonName(currName));
		}
		return namesAbbr;
	}
	
	/**
	 * Checks whether a Notes name matches a wildcard string, e.g. "Karsten Lehmann / Mindoo" would match
	 * "* / Mindoo".
	 * 
	 * @param name notes name (abbreviated or canonical)
	 * @param wildcard (abbreviated or canonical)
	 * @return true if match
	 */
	public static boolean nameMatchesWildcard(String name, String wildcard) {
		if ("*".equals(wildcard)) { //$NON-NLS-1$
			return true;
		}
		
		String nameAbbr = toAbbreviatedName(name);
		String wildcardAbbr = toAbbreviatedName(wildcard);
		
		List<String> nameSt = new ArrayList<>();
		JNXStringUtil.splitString(nameAbbr, '/', true, nameSt);
		Collections.reverse(nameSt);
		
		List<String> wildcards = new ArrayList<>();
		JNXStringUtil.splitString(wildcardAbbr, '/', true, wildcards);
		Collections.reverse(wildcards);
		Iterator<String> wildcardSt = wildcards.iterator();
		
		for(final String currNameToken : nameSt) {
			
			if (!wildcardSt.hasNext()) {
				return false;
			}
			else {
				String currWildcardToken = wildcardSt.next();
				if ("*".equals(currWildcardToken)) { //$NON-NLS-1$
					if (wildcardSt.hasNext()) {
						throw new IllegalArgumentException("The wildcard * can only be the leftmost part of the wildcard pattern");
					}
					
					return true;
				}
				else if (!currNameToken.equalsIgnoreCase(currWildcardToken)) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * This function converts a distinguished name in canonical format to abbreviated format.
	 * A fully distinguished name is in canonical format - it contains all possible naming components.
	 * The abbreviated format of a distinguished name removes the labels from the naming components.
	 * 
	 * @param name name to convert
	 * @param templateName name to be used when the input name is in common name format
	 * @return abbreviated name
	 */
    public static String toAbbreviatedName(String name, String templateName) {
      if (name == null) {
        return null;
      }
      if (name.length() == 0) {
        return name;
      }

      String cacheKey =
          name + ((templateName != null && templateName.length() > 0) ? ("|" + templateName) : ""); //$NON-NLS-1$ //$NON-NLS-2$
      String abbrName = m_nameAbbrCache.get(System.identityHashCode(cacheKey.intern()));
      if (abbrName != null) {
        return abbrName;
      }

      Memory templateNameMem = templateName == null || templateName.length() == 0 ? null
          : NotesStringUtils.toLMBCS(templateName, true); // used when abbrName is only a common
                                                          // name
      Memory inNameMem = NotesStringUtils.toLMBCS(name, true);

      ByteBuffer outNameMem = ThreadBuffers.BUFFER_USERNAME.get();
      WordByReference outLength = new WordByReference();

      NotesCAPI.get().DNAbbreviate(0, templateNameMem, inNameMem, outNameMem, TypedConstants.MAXUSERNAME, outLength).orElseThrow();

      String sOutName = NotesStringUtils.fromLMBCS(outNameMem, outLength.getValue());

      m_nameAbbrCache.put(System.identityHashCode(cacheKey.intern()), sOutName);

      return sOutName;
    }

	/**
	 * Writes the specified names list in null terminated LMBCS strings to a {@link ByteArrayOutputStream}
	 * 
	 * @param names names to write
	 * @param bOut target output stream
	 */
	private static void storeAsUserNamesList(List<String> names, ByteArrayOutputStream bOut) {
		//convert to canonical format
		List<String> namesCanonical = new ArrayList<>(names.size());
		for (int i=0; i<names.size(); i++) {
			namesCanonical.add(toCanonicalName(names.get(i)));
		}
		
		for (int i=0; i<namesCanonical.size(); i++) {
			String currName = namesCanonical.get(i);
			Memory currNameLMBCS = NotesStringUtils.toLMBCS(currName, true);
			
			try {
				bOut.write(currNameLMBCS.getByteArray(0, (int) currNameLMBCS.size()));
			} catch (IOException e) {
				throw new DominoException(StatusImpl.NOERROR, "Error writing to ByteArrayOutputStream");
			}
		}
	}

	/**
	 * Programatically creates a {@link JNAUserNamesList}
	 * 
	 * @param parentObj parent API object used for memory management
	 * @param names names for names list, similar to result of @UserNamesList formula, e.g. usernames, wildcards, groups and roles; either abbreviated or canonical
	 * @return names list
	 */
    public static JNAUserNamesList writeNewNamesList(IAPIObject<?> parentObj, List<String> names) {
      ByteArrayOutputStream bOut = new ByteArrayOutputStream();
      storeAsUserNamesList(names, bOut);
      
      int structSize;
      if (PlatformUtils.isWindows()) {
        structSize = JNANotesConstants.winNamesListHeaderSize64;
      } else if (PlatformUtils.isMac()) {
        structSize = JNANotesConstants.macNamesListHeaderSize64;
      } else {
        structSize = JNANotesConstants.linuxNamesListHeaderSize64;
      }
      
      DHANDLE.ByReference retHandle = DHANDLE.newInstanceByReference();
      Mem.OSMemAlloc((short) 0, structSize + bOut.size(), retHandle);
      
      // write the data
      LockUtil.lockHandle(retHandle, (handleByVal) -> {
        Pointer ptr = Mem.OSLockObject(handleByVal);
        try {
          // All the structure variants start with WORD NumNames
          ptr.setShort(0, (short) names.size());
      
          byte[] namesDataArr = bOut.toByteArray();
          ptr.write(structSize, namesDataArr, 0, namesDataArr.length);
        } finally {
          Mem.OSUnlockObject(handleByVal);
        }
        return 0;
      });
      DHANDLE handle64 = retHandle;
      JNAUserNamesList namesList = new JNAUserNamesList(parentObj, handle64);
      return namesList;
    }
	
	/**
	 * Computes a {@link JNAUserNamesList} structure with all name variants, wildcards and groups for
	 * the specified user
	 * 
	 * @param parent API parent object for memory management
	 * @param server name of server, either abbreviated or canonical or null/empty string for local
	 * @param userName username, either abbreviated or canonical
	 * @return names list
	 */
	public static JNAUserNamesList buildNamesList(IAPIObject<?> parent, String server, String userName) {
		Objects.requireNonNull(userName, "Name cannot be null");
		
		if (server==null || "".equals(server)) { //$NON-NLS-1$
			return buildNamesList(parent, userName);
		}

		//make sure that server and username are canonical
		userName = toCanonicalName(userName);
		server = toCanonicalName(server);
		
		Memory userNameLMBCS = NotesStringUtils.toLMBCS(userName, true);
		Memory serverNameLMBCS = NotesStringUtils.toLMBCS(server, true);
		
		boolean bDontLookupAlternateNames = false;
		short fDontLookupAlternateNames = (short) (bDontLookupAlternateNames ? 1 : 0);
		Pointer pLookupFlags = null;

		DHANDLE.ByReference rethNamesList = DHANDLE.newInstanceByReference();
		synchronized(BUILDNAMESLIST_LOCK) {
			NotesCAPI.get().CreateNamesListFromSingleName(serverNameLMBCS,
					fDontLookupAlternateNames, pLookupFlags, userNameLMBCS, rethNamesList).orElseThrow();
		}
		
		return new JNAUserNamesList(parent, rethNamesList);
	}
	
	/**
	 * Expand one or more target names (e.g., might contain an alternate name) into a list of
	 * names (including any groups the target names belong to) by any given server name.
	 * 
	 * @param parent API parent object for memory management
	 * @param server name of server, either abbreviated or canonical or null/empty string for local
	 * @param names names to expand
	 * @return names list
	 */
	public static JNAUserNamesList createNamesListFromNames(IAPIObject<?> parent, String server, String[] names) {
		server = toCanonicalName(server);
		
		try(DisposableMemory ptrArrMem = new DisposableMemory(Native.POINTER_SIZE * names.length)) {
    		Memory[] namesMem = new Memory[names.length];
    		
    		for (int i=0; i<names.length; i++) {
    			namesMem[i] = NotesStringUtils.toLMBCS(names[i], true);
    			ptrArrMem.setPointer(i, namesMem[i]);
    		}
    		
    		Memory serverNameLMBCS = NotesStringUtils.toLMBCS(server, true);
    		
    		PointerByReference ptrRef = new PointerByReference();
    		ptrRef.setValue(ptrArrMem);
    		
    		LMBCSStringArray sArr = new LMBCSStringArray(names);
    		
    		DHANDLE.ByReference rethNamesList = DHANDLE.newInstanceByReference();
    		synchronized(BUILDNAMESLIST_LOCK) {
    			NotesCAPI.get().CreateNamesListFromNamesExtend(serverNameLMBCS, word(names.length), sArr, rethNamesList).orElseThrow();
    		}
    		
    		JNAUserNamesList newList =  new JNAUserNamesList(parent, rethNamesList);
    		return newList;
		}
	}
	
	/**
	 * Expand a target group/subtree name into a list of names (including any groups
	 * or subtrees the target names belong to) by any given server name
	 * 
	 * @param parent API parent object for memory management
	 * @param server name of server, either abbreviated or canonical or null/empty string for local
	 * @param group group name
	 * @return names list
	 */
	public static JNAUserNamesList createNamesListFromGroupName(IAPIObject<?> parent, String server, String group) {
		server = toCanonicalName(server);
		
		Memory groupLMBCS = NotesStringUtils.toLMBCS(group, true);
		Memory serverNameLMBCS = NotesStringUtils.toLMBCS(server, true);
		
		DHANDLE.ByReference rethNamesList = DHANDLE.newInstanceByReference();
		synchronized(BUILDNAMESLIST_LOCK) {
			NotesCAPI.get().CreateNamesListFromGroupNameExtend(serverNameLMBCS, groupLMBCS, rethNamesList).orElseThrow();
		}
		
		JNAUserNamesList newList =  new JNAUserNamesList(parent, rethNamesList);
		return newList;
	}
	
	/**
	 * Computes a {@link JNAUserNamesList} structure with all name variants, wildcards and groups for
	 * the specified user on a remote server
	 * 
	 * @param parent API parent object for memory management
	 * @param userName username, either abbreviated or canonical
	 * @return names list
	 */
	public static JNAUserNamesList buildNamesList(IAPIObject<?> parent, String userName) {
		Objects.requireNonNull(userName, "Name cannot be null");
		
		//make sure that username is canonical
		userName = toCanonicalName(userName);
		
		Memory userNameLMBCS = NotesStringUtils.toLMBCS(userName, true);
		
		DHANDLE.ByReference rethNamesList = DHANDLE.newInstanceByReference();
		
		//synchronizing the NSFBuildNamesList call because we had reproducible multithreading issues
		//under heavy application load
        synchronized (BUILDNAMESLIST_LOCK) {
          NotesCAPI.get().NSFBuildNamesList(userNameLMBCS, 0, rethNamesList).orElseThrow();
        }
		
		JNAUserNamesList newList =  new JNAUserNamesList(parent, rethNamesList);
		return newList;
	}
	
	/**
	 * Computes the usernames list for the specified user, which is his name, name wildcards
	 * and all his groups and nested groups
	 * 
	 * @param parent API parent object for memory management
	 * @param server name of server, either abbreviated or canonical or null/empty string for local
	 * @param userName username in abbreviated or canonical format
	 * @return usernames list
	 */
	public static List<String> getUserNamesList(IAPIObject<?> parent, String server, String userName) {
		JNAUserNamesList namesList = buildNamesList(parent, server, userName);
		List<String> names = namesList.toList();
		namesList.dispose();
		
		return names;
	}

	/**
	 * Computes the usernames list for the specified user, which is his name, name wildcards
	 * and all his groups and nested groups
	 * 
	 * @param parent API parent object for memory management
	 * @param userName username in canonical format
	 * @return usernames list
	 */
	public static List<String> getUserNamesList(IAPIObject<?> parent, String userName) {
		JNAUserNamesList namesList = buildNamesList(parent, userName);
		List<String> names = namesList.toList();
		namesList.dispose();
		
		return names;
	}
	
	/**
	 * Enum of available user privileges
	 * 
	 * @author Karsten Lehmann
	 */
	public enum Privileges {

		/** Set if names list has been authenticated via Notes (e.g. user is allowed to open a database) */
		Authenticated(0x0001),

		/**	Set if names list has been authenticated using external password -- Triggers "maximum password access allowed" feature */
		PasswordAuthenticated(0x0002),

		/**	Set if user requested full admin access and it was granted */
		FullAdminAccess(0x0004);

		private int m_flag;

		Privileges(int flag) {
			m_flag = flag;
		}

		public int getValue() {
			return m_flag;
		}
	};
	
	/**
	 * Internal helper function that modifies the Authenticated flag of a {@link JNAUserNamesList}
	 * in order to grant access to certain C API functionality (e.g. when opening a database).
	 * 
	 * @param namesList names list
	 * @param privileges new privileges
	 */
    public static void setPrivileges(JNAUserNamesList namesList, EnumSet<Privileges> privileges) {
      int bitMask = 0;
      for (Privileges currPrivilege : Privileges.values()) {
        if (privileges.contains(currPrivilege)) {
          bitMask = bitMask | currPrivilege.getValue();
        }
      }

      short bitMaskAsShort = (short) (bitMask & 0xffff);

      /*
       * Use different header implementations based on architecture, because we have
       * //different alignments and data types:
       * typedef struct {
       * WORD NumNames;
       * LICENSEID License;
       * #if defined(UNIX) || defined(OS2_2x) || defined(W32)
       * DWORD Authenticated;
       * #else
       * WORD Authenticated;
       * #endif
       * } NAMES_LIST;
       */

      DHANDLE handle = namesList.getAdapter(DHANDLE.class);
      if (handle == null || handle.isNull()) {
        throw new DominoException(StatusImpl.NOERROR,
            MessageFormat.format("Missing DHANDLE value in names list: {0}", handle));
      }

      final int fBitMask = bitMask;

      LockUtil.lockHandle(handle, (handleByVal) -> {
        Pointer namesListBufferPtr = Mem.OSLockObject(handleByVal);

        try {
          if (PlatformUtils.isWindows()) {
            WinNotesNamesListHeader64Struct namesListHeader =
                WinNotesNamesListHeader64Struct.newInstance(namesListBufferPtr);
            namesListHeader.read();
            namesListHeader.Authenticated = fBitMask;
            namesListHeader.write();
            namesListHeader.read();
          } else if (PlatformUtils.isMac()) {
            MacNotesNamesListHeader64Struct namesListHeader =
                MacNotesNamesListHeader64Struct.newInstance(namesListBufferPtr);
            namesListHeader.read();

            namesListHeader.Authenticated = bitMaskAsShort;
            namesListHeader.write();
            namesListHeader.read();
          } else {
            LinuxNotesNamesListHeader64Struct namesListHeader =
                LinuxNotesNamesListHeader64Struct.newInstance(namesListBufferPtr);
            namesListHeader.read();

            // setting authenticated flag for the user is required when running on the server
            namesListHeader.Authenticated = fBitMask;
            namesListHeader.write();
            namesListHeader.read();
          }
        } finally {
          Mem.OSUnlockObject(handleByVal);
        }
        return 0;
      });

    }
	
	/**
	 * Reads which privileges have been set in the names list by method {@link #setPrivileges(JNAUserNamesList, EnumSet)}
	 * 
	 * @param namesList names list
	 * @return privileges
	 */
    public static EnumSet<Privileges> getPrivileges(JNAUserNamesList namesList) {
      /*
       * Use different header implementations based on architecture, because we have
       * //different alignments and data types:
       * typedef struct {
       * WORD NumNames;
       * LICENSEID License;
       * #if defined(UNIX) || defined(OS2_2x) || defined(W32)
       * DWORD Authenticated;
       * #else
       * WORD Authenticated;
       * #endif
       * } NAMES_LIST;
       */

      int authenticated;

      DHANDLE handle = namesList.getAdapter(DHANDLE.class);
      if (handle == null || handle.isNull()) {
        throw new DominoException(StatusImpl.NOERROR,
            MessageFormat.format("Missing DHANDLE value in names list: {0}", handle));
      }

      authenticated = LockUtil.lockHandle(handle, (handleByVal) -> {
        Pointer namesListBufferPtr = Mem.OSLockObject(handleByVal);

        try {
          if (PlatformUtils.isWindows()) {
            WinNotesNamesListHeader64Struct namesListHeader =
                WinNotesNamesListHeader64Struct.newInstance(namesListBufferPtr);
            namesListHeader.read();
            return namesListHeader.Authenticated;
          } else if (PlatformUtils.isMac()) {
            MacNotesNamesListHeader64Struct namesListHeader =
                MacNotesNamesListHeader64Struct.newInstance(namesListBufferPtr);
            namesListHeader.read();

            return (int) namesListHeader.Authenticated;
          } else {
            LinuxNotesNamesListHeader64Struct namesListHeader =
                LinuxNotesNamesListHeader64Struct.newInstance(namesListBufferPtr);
            namesListHeader.read();

            // setting authenticated flag for the user is required when running on the server
            return namesListHeader.Authenticated;
          }
        } finally {
          Mem.OSUnlockObject(handleByVal);
        }
      });

      EnumSet<Privileges> privileges = EnumSet.noneOf(Privileges.class);

      for (Privileges currPrivilege : Privileges.values()) {
        if ((authenticated & currPrivilege.getValue()) == currPrivilege.getValue()) {
          privileges.add(currPrivilege);
        }
      }
      return privileges;
    }
}
