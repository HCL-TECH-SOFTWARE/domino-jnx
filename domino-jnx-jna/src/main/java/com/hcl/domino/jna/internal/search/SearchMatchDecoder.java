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
package com.hcl.domino.jna.internal.search;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Formatter;

import com.hcl.domino.data.Database.DocFlags;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.jna.data.JNADominoDateTime;
import com.hcl.domino.jna.internal.NotesOriginatorIdData;
import com.hcl.domino.jna.internal.search.NotesSearch.JNASearchMatch;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Pointer;

/**
 * Utility class to decode the SEARCH_MATCH structure and copy its values
 * into a {@link JNASearchMatch}.
 * 
 * @author Karsten Lehmann
 */
public class SearchMatchDecoder {

	/**
	 * Decodes the SEARCH_MATCH_LARGE structure starting at the specified memory address
	 * 
	 * @param ptr memory pointer
	 * @return object with search match data
	 */
	public static JNASearchMatch decodeSearchMatchLarge(Pointer ptr) {
		int gid_file_innards0 = ptr.getInt(0);
		int gid_file_innards1 = ptr.getInt(4);
		
		int gid_note_innards0 = ptr.getInt(8);
		int gid_note_innards1 = ptr.getInt(12);

		int noteId = ptr.getInt(16);
		
		int oid_file_innards0 = ptr.getInt(20);
		int oid_file_innards1 = ptr.getInt(24);
		
		int oid_note_innards0 = ptr.getInt(28);
		int oid_note_innards1 = ptr.getInt(32);
		int seq = ptr.getInt(36);
		int seqTimeInnards0 = ptr.getInt(40);
		int seqTimeInnards1 = ptr.getInt(44);
		
		short noteClass = ptr.getShort(48);
		byte seRetFlags = ptr.getByte(50);
		byte privileges = ptr.getByte(51);
//		int filler = ptr.getInt(52); //seems to have alignment or to be DWORD instead of WORD
		int summaryLength = ptr.getInt(56);

//		typedef struct {
//			GLOBALINSTANCEID ID;                /* identity of the note within the file */
//			ORIGINATORID OriginatorID;        /* identity of the note in the universe */
//			WORD NoteClass;                                /* class of the note */
//			BYTE SERetFlags;                        /* MUST check for SE_FMATCH! */
//			BYTE Privileges;                        /* note privileges */
//			WORD filler;                                /* to ease mapping to SEARCH_MATCH */
//			DWORD SummaryLength;                /* length of the summary information */
//			                                            /* 58 bytes to here */
//			                                            /* now comes an ITEM_TABLE_LARGE with Summary Info */
//			                                            /* now comes the optional $TUA item value.  It is in host format. */
//			} SEARCH_MATCH_LARGE;
			
		SearchMatchImpl match = new SearchMatchImpl();
		match.setGIDFileInnards(new int[] {gid_file_innards0, gid_file_innards1});
		match.setGIDNoteInnards(new int[] {gid_note_innards0, gid_note_innards1});
		match.setNoteId(noteId);
		match.setOIDFileInnards(new int[] {oid_file_innards0, oid_file_innards1});
		match.setOIDNoteInnards(new int[] {oid_note_innards0, oid_note_innards1});
		match.setSeq(seq);
		match.setSeqTimeInnards(new int[] {seqTimeInnards0, seqTimeInnards1});
		match.setNoteClass(noteClass);
		match.setSeRetFlags(seRetFlags);
		match.setPrivileges(privileges);
		match.setSummaryLength(summaryLength);
		return match;
	}
	
	/**
	 * Decodes the SEARCH_MATCH structure starting at the specified memory address
	 * 
	 * @param ptr memory pointer
	 * @return object with search match data
	 */
	public static JNASearchMatch decodeSearchMatch(Pointer ptr) {
		int gid_file_innards0 = ptr.getInt(0);
		int gid_file_innards1 = ptr.getInt(4);
		
		int gid_note_innards0 = ptr.getInt(8);
		int gid_note_innards1 = ptr.getInt(12);
		
		int noteId = ptr.getInt(16);
		
		int oid_file_innards0 = ptr.getInt(20);
		int oid_file_innards1 = ptr.getInt(24);
		
		int oid_note_innards0 = ptr.getInt(28);
		int oid_note_innards1 = ptr.getInt(32);
		int seq = ptr.getInt(36);
		int seqTimeInnards0 = ptr.getInt(40);
		int seqTimeInnards1 = ptr.getInt(44);
		
		short noteClass = ptr.getShort(48);
		byte seRetFlags = ptr.getByte(50);
		byte privileges = ptr.getByte(51);
		short summaryLength = ptr.getShort(52);
		
		SearchMatchImpl match = new SearchMatchImpl();
		match.setGIDFileInnards(new int[] {gid_file_innards0, gid_file_innards1});
		match.setGIDNoteInnards(new int[] {gid_note_innards0, gid_note_innards1});
		match.setNoteId(noteId);
		match.setOIDFileInnards(new int[] {oid_file_innards0, oid_file_innards1});
		match.setOIDNoteInnards(new int[] {oid_note_innards0, oid_note_innards1});
		match.setSeq(seq);
		match.setSeqTimeInnards(new int[] {seqTimeInnards0, seqTimeInnards1});
		match.setNoteClass(noteClass);
		match.setSeRetFlags(seRetFlags);
		match.setPrivileges(privileges);
		match.setSummaryLength((int) (summaryLength & 0xffff));
		return match;
	}
	
	public static class SearchMatchImpl implements NotesSearch.JNASearchMatch {
		//global instance id
		private int[] gid_file_innards;
		private int[] gid_note_innards;
		private int noteId;
		
		//originator id
		private int[] oid_file_innards;
		private int[] oid_note_innards;
		private int seq;
		private int[] seqTimeInnards;
		
		//other data
		private short noteClass;
		private byte seRetFlags;
		private byte privileges;
		private int summaryLength;
		
		private JNADominoDateTime m_dbCreated;
		private JNADominoDateTime m_nodeModified;
		private JNADominoDateTime m_seqTime;
		private NotesOriginatorIdData m_oidData;
		
		private EnumSet<DocumentClass> noteClassAsEnum;
		private EnumSet<DocFlags> m_flagsAsEnum;
		private String m_unid;
		
		@Override
		public int[] getGIDFileInnards() {
			return gid_file_innards;
		}
		
		void setGIDFileInnards(int[] gid_file_innards) {
			this.gid_file_innards = gid_file_innards;
		}
		
		@Override
		public int[] getGIDNoteInnards() {
			return gid_note_innards;
		}
		
		void setGIDNoteInnards(int[] gid_note_innards) {
			this.gid_note_innards = gid_note_innards;
		}
		
		@Override
		public int getNoteID() {
			return noteId;
		}
		
		void setNoteId(int noteId) {
			this.noteId = noteId;
		}
		
		@Override
		public int[] getOIDFileInnards() {
			return oid_file_innards;
		}
		
		void setOIDFileInnards(int[] oid_file_innards) {
			this.oid_file_innards = oid_file_innards;
		}
		
		@Override
		public int[] getOIDNoteInnards() {
			return oid_note_innards;
		}
		
		void setOIDNoteInnards(int[] oid_note_innards) {
			this.oid_note_innards = oid_note_innards;
		}
		
		@Override
		public int getSequenceNumber() {
			return seq;
		}
		
		void setSeq(int seq) {
			this.seq = seq;
		}
		
		@Override
		public int[] getSeqTimeInnards() {
			return seqTimeInnards;
		}
		
		void setSeqTimeInnards(int[] seqTimeInnards) {
			this.seqTimeInnards = seqTimeInnards;
		}
		
		@Override
		public EnumSet<DocumentClass> getDocumentClass() {
			if (noteClassAsEnum==null) {
				noteClassAsEnum = DominoEnumUtil.valuesOf(DocumentClass.class, noteClass);
			}
			return noteClassAsEnum;
		}
		
		void setNoteClass(short noteClass) {
			this.noteClass = noteClass;
		}
		
		void setSeRetFlags(byte seRetFlags) {
			this.seRetFlags = seRetFlags;
		}
		
		void setPrivileges(byte privileges) {
			this.privileges = privileges;
		}
		
		@Override
		public int getSummaryLength() {
			return summaryLength;
		}
		
		void setSummaryLength(int summaryLength) {
			this.summaryLength = summaryLength;
		}

		private static EnumSet<DocFlags> toDocFlags(byte flagsAsByte) {
			EnumSet<DocFlags> flags = EnumSet.noneOf(DocFlags.class);
			boolean isTruncated = (flagsAsByte & NotesConstants.SE_FTRUNCATED) == NotesConstants.SE_FTRUNCATED;
			if (isTruncated) {
				flags.add(DocFlags.Truncated);
			}
			boolean isNoAccess = (flagsAsByte & NotesConstants.SE_FNOACCESS) == NotesConstants.SE_FNOACCESS;
			if (isNoAccess) {
				flags.add(DocFlags.NoAccess);
			}
			boolean isTruncatedAttachment = (flagsAsByte & NotesConstants.SE_FTRUNCATT) == NotesConstants.SE_FTRUNCATT;
			if (isTruncatedAttachment) {
				flags.add(DocFlags.TruncatedAttachments);
			}
			boolean isNoPurgeStatus = (flagsAsByte & NotesConstants.SE_FNOPURGE) == NotesConstants.SE_FNOPURGE;
			if (isNoPurgeStatus) {
				flags.add(DocFlags.NoPurgeStatus);
			}
			boolean isPurged = (flagsAsByte & NotesConstants.SE_FPURGED) == NotesConstants.SE_FPURGED;
			if (isPurged) {
				flags.add(DocFlags.Purged);
			}
			boolean isMatch = (flagsAsByte & NotesConstants.SE_FMATCH) == NotesConstants.SE_FMATCH;
			if (isMatch) {
				flags.add(DocFlags.Match);
			} else {
				flags.add(DocFlags.NoMatch);
			}
			boolean isSoftDeleted = (flagsAsByte & NotesConstants.SE_FSOFTDELETED) == NotesConstants.SE_FSOFTDELETED;
			if (isSoftDeleted) {
				flags.add(DocFlags.SoftDeleted);
			}
			
			return flags;
		}
		
		@Override
		public EnumSet<DocFlags> getFlags() {
			if (m_flagsAsEnum==null) {
				m_flagsAsEnum = toDocFlags(this.seRetFlags);
			}
			return m_flagsAsEnum;
		}

		@Override
		public boolean matchesFormula() {
			//use flags byte directly, quicker than creating the EnumSet first
			return ((this.seRetFlags & NotesConstants.SE_FMATCH) == NotesConstants.SE_FMATCH);
		}
		
		@Override
		public boolean isLargeSummary() {
			return ((this.seRetFlags & NotesConstants.SE_FLARGESUMMARY) == NotesConstants.SE_FLARGESUMMARY);
		}
		
		@Override
		public JNADominoDateTime getSequenceTime() {
			if (m_seqTime==null) {
				m_seqTime = new JNADominoDateTime(getSeqTimeInnards());
			}
			return m_seqTime;
		}

		@Override
		public NotesOriginatorIdData getOIDData() {
			if (m_oidData==null) {
				m_oidData = new NotesOriginatorIdData(getUNID(), getSequenceNumber(), getSeqTimeInnards());
			}
			return m_oidData;
		}

		@Override
		public String getUNID() {
			if (m_unid==null) {
				Formatter formatter = new Formatter();
				formatter.format("%08x", this.oid_file_innards[1]); //$NON-NLS-1$
				formatter.format("%08x", this.oid_file_innards[0]); //$NON-NLS-1$
				
				formatter.format("%08x", this.oid_note_innards[1]); //$NON-NLS-1$
				formatter.format("%08x", this.oid_note_innards[0]); //$NON-NLS-1$
				
				m_unid = formatter.toString().toUpperCase();
				formatter.close();
			}
			return m_unid;
		}

		@Override
		public DominoDateTime getDbCreated() {
			if (m_dbCreated==null) {
				m_dbCreated = new JNADominoDateTime(getGIDFileInnards());
			}
			return m_dbCreated;
		}

		@Override
		public DominoDateTime getLastModified() {
			if (m_nodeModified==null) {
				m_nodeModified = new JNADominoDateTime(getGIDNoteInnards());
			}
			return m_nodeModified;
		}
		
		@Override
		public String toString() {
			return MessageFormat.format(
				"SearchMatch [unid={0}, seq={1}, seqtime={2}, noteid={3}, class={4},flags={5}, modified={6}]", //$NON-NLS-1$
				getUNID(), getSequenceNumber(), getSequenceTime(), getNoteID(), getDocumentClass(), getFlags(), getLastModified()
			);
		}
	}
}
