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
package com.hcl.domino.jna.data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;

import com.hcl.domino.commons.data.DateFormat;
import com.hcl.domino.commons.data.DateTimeStructure;
import com.hcl.domino.commons.data.DefaultDominoDateTime;
import com.hcl.domino.commons.data.TimeFormat;
import com.hcl.domino.commons.data.ZoneFormat;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.JNANotesConstants;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.structs.IntlFormatStruct;
import com.hcl.domino.jna.internal.structs.NotesTFMTStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.ptr.ShortByReference;

/**
 * Wrapper class for the TIMEDATE C API data structure.
 * 
 * <p>This implementation uses native memory and API calls only for constant-creation
 * methods and conversion, but not for internal storage.</p>
 * 
 * @author Karsten Lehmann
 */
public class JNADominoDateTime extends DefaultDominoDateTime {
	private NotesTimeDateStruct m_structReused;
	
	/**
	 * Method to create a {@link DominoDateTime} with ANYDAY/ALLDAY
	 * 
	 * @return date/time
	 */
	public static DominoDateTime createWildcardDateTime() {
		NotesTimeDateStruct struct = NotesTimeDateStruct.newInstance();
		NotesCAPI.get().TimeConstant(NotesConstants.TIMEDATE_WILDCARD, struct);
		struct.read();
		
		return new JNADominoDateTime(struct);
	}

	/**
	 * Method to create a {@link DominoDateTime} with the minimum value
	 * 
	 * @return date/time
	 */
	public static DominoDateTime createMinimumDateTime() {
		NotesTimeDateStruct struct = NotesTimeDateStruct.newInstance();
		NotesCAPI.get().TimeConstant(NotesConstants.TIMEDATE_MINIMUM, struct);
		struct.read();
		
		return new JNADominoDateTime(struct);
	}
	
	/**
	 * Method to create a {@link DominoDateTime} with the maximum value
	 * 
	 * @return date/time
	 */
	public static DominoDateTime createMaximumDateTime() {
		NotesTimeDateStruct struct = NotesTimeDateStruct.newInstance();
		NotesCAPI.get().TimeConstant(NotesConstants.TIMEDATE_MAXIMUM, struct);
		struct.read();
		
		return new JNADominoDateTime(struct);
	}
	
	/**
	 * Returns a {@link JNADominoDateTime} instance for the provided {@link TemporalAccessor} value.
	 * If {@code temporal} is already a {@code JNADominoDateTime}, it is returned directly.
	 * 
	 * @param temporal the {@link TemporalAccessor} value to interpret
	 * @return a {@link JNADominoDateTime} corresponding to the provided value, or {@code null}
	 * 		if {@code temporal} is null
	 */
	public static JNADominoDateTime from(TemporalAccessor temporal) {
		if(temporal == null) {
			return null;
		} else if(temporal instanceof JNADominoDateTime) {
			return (JNADominoDateTime)temporal;
		} else {
			return new JNADominoDateTime(temporal);
		}
	}

	/**
	 * Creates a new date/time object and sets it to the current date/time
	 */
	public JNADominoDateTime() {
		super();
	}
	
	/**
	 * Creates a new date/time object and sets it to a date/time specified as
	 * innards array
	 * 
	 * @param innards innards array
	 */
	public JNADominoDateTime(int innards[]) {
		super(innards);
	}

	/**
	 * Creates a new date/time object and sets it to the specified {@link ZonedDateTime}
	 * 
	 * @param dt zoned date/time value
	 */
	public JNADominoDateTime(TemporalAccessor dt) {
		super(dt);
	}

	/**
	 * Creates a new date/time object and sets it to the specified time in milliseconds since
	 * GMT 1/1/70
	 * 
	 * @param timeMs the milliseconds since January 1, 1970, 00:00:00 GMT
	 */
	public JNADominoDateTime(long timeMs) {
		super(timeMs);
	}

	/**
	 * Constructs a new date/time by merging the date and time part of two other {@link JNADominoDateTime} objects
	 * 
	 * @param date date part
	 * @param time time part
	 */
	public JNADominoDateTime(JNADominoDateTime date, JNADominoDateTime time) {
		super(new int[] { date.getInnards()[0], time.getInnards()[1] });
	}
	
	public JNADominoDateTime(NotesTimeDateStruct struct) {
		super(struct.Innards);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> clazz) {
		if (NotesTimeDateStruct.class.equals(clazz)) {
			return (T) lazilyCreateStruct();
		}
		else if (int[].class.equals(clazz)) {
			return (T) getInnards();
		}
		else if (LocalDate.class.equals(clazz)) {
			return (T) toLocalDate();
		} else if(LocalTime.class.equals(clazz)) {
			return (T) toLocalTime();
		} else if(OffsetDateTime.class.equals(clazz)) {
			return (T) toOffsetDateTime();
		}
		
		return null;
	}
	
	private NotesTimeDateStruct lazilyCreateStruct() {
		if (m_structReused==null) {
			m_structReused = NotesTimeDateStruct.newInstance();
		}
		m_structReused.Innards = new int[] { m_innards0, m_innards1 };
		m_structReused.write();
		return m_structReused;
	}
	
	/**
	 * Converts a {@link JNADominoDateTime} to string
	 * 
	 * @return string with formatted timedate
	 */
	@Override
	public String toString() {
		return toString(DateFormat.FULL, TimeFormat.FULL, ZoneFormat.ALWAYS, DateTimeStructure.DATETIME);
	}
	
	/**
	 * Converts a {@link JNADominoDateTime} to string with formatting options.
	 * 
	 * @param dFormat how to format the date part
	 * @param tFormat how to format the time part
	 * @param zFormat how to format the timezone
	 * @param dtStructure overall structure of the result, e.g. {@link DateTimeStructure} for date only
	 * @return string with formatted timedate
	 */
	public String toString(DateFormat dFormat, TimeFormat tFormat, ZoneFormat zFormat, DateTimeStructure dtStructure) {
		return toString((DominoIntlFormat) null, dFormat, tFormat, zFormat, dtStructure);
	}
	
	// TODO maybe implement the following methods, since it seems in many places only valid JNADominoDateTime instances are expected
	/*public boolean isValid() {
		NotesTimeDateStruct struct = lazilyCreateStruct();
		
		if (struct.Innards==null || struct.Innards.length<2) {
			return false;
		}
		else if (struct.Innards[0]<0 || struct.Innards[1]<0) {
			return false;
		}
		return true;
	}
	
	public boolean isMinumum() {
		NotesTimeDateStruct struct = lazilyCreateStruct();
		
		if (isValid()
				&& struct.Innards[0]==0 && struct.Innards[1]==0) {
			return true;
		}
		return false;
	}
	
	public boolean isMaximum() {
		NotesTimeDateStruct struct = lazilyCreateStruct();
		
		if (isValid()
				&& struct.Innards[0]==0 && struct.Innards[1]==0xffffff) {
			return true;
		}
		return false;
	}*/
	
	/**
	 * Converts a {@link JNADominoDateTime} to string with formatting options.
	 * 
	 * @param intl the internationalization settings in effect. Can be <code>null</code>, in which case this function works with the client/server default settings for the duration of the call.
	 * @param dFormat how to format the date part
	 * @param tFormat how to format the time part
	 * @param zFormat how to format the timezone
	 * @param dtStructure overall structure of the result, e.g. {@link DateTimeStructure} for date only
	 * @return string with formatted timedate
	 */
	public String toString(DominoIntlFormat intl, DateFormat dFormat, TimeFormat tFormat, ZoneFormat zFormat, DateTimeStructure dtStructure) {
		NotesTimeDateStruct struct = lazilyCreateStruct();
		
		if (struct.Innards==null || struct.Innards.length<2)
		 {
			return ""; //$NON-NLS-1$
		}
		if (struct.Innards[0]==0 && struct.Innards[1]==0)
		 {
			return "MINIMUM"; //$NON-NLS-1$
		}
		if (struct.Innards[0]==0 && struct.Innards[1]==0xffffff)
		 {
			return "MAXIMUM"; //$NON-NLS-1$
		}
		
		
		IntlFormatStruct intlStruct = intl==null ? null : intl.getAdapter(IntlFormatStruct.class);
		NotesTFMTStruct tfmtStruct = NotesTFMTStruct.newInstance();
		tfmtStruct.Date = dFormat==null ? NotesConstants.TDFMT_FULL : dFormat.getValue();
		tfmtStruct.Time = tFormat==null ? NotesConstants.TTFMT_FULL : tFormat.getValue();
		tfmtStruct.Zone = zFormat==null ? NotesConstants.TZFMT_ALWAYS : zFormat.getValue();
		tfmtStruct.Structure = dtStructure==null ? NotesConstants.TSFMT_DATETIME : dtStructure.getValue();
		tfmtStruct.write();
		
		String txt;
		int outBufLength = 40;
		DisposableMemory retTextBuffer = new DisposableMemory(outBufLength);
		while (true) {
			ShortByReference retTextLength = new ShortByReference();
			short result = NotesCAPI.get().ConvertTIMEDATEToText(intlStruct, tfmtStruct.getPointer(), struct, retTextBuffer, (short) retTextBuffer.size(), retTextLength);
			if (result==1037) { // "Invalid Time or Date Encountered", return empty string like Notes UI does
				return ""; //$NON-NLS-1$
			}
			if (result!=1033) { // "Output Buffer Overflow"
				NotesErrorUtils.checkResult(result);
			}

			if (result==1033 || (retTextLength.getValue() >= retTextBuffer.size())) {
				retTextBuffer.dispose();
				outBufLength = outBufLength * 2;
				retTextBuffer = new DisposableMemory(outBufLength);

				continue;
			}
			else {
				txt = NotesStringUtils.fromLMBCS(retTextBuffer, retTextLength.getValue());
				break;
			}
		}

		retTextBuffer.dispose();
		return txt;
	}
	
	/**
	 * Parses a timedate string to a {@link JNADominoDateTime}
	 * 
	 * @param dateTimeStr timedate string
	 * @return timedate
	 */
	public static JNADominoDateTime fromString(String dateTimeStr) {
		return fromString((DominoIntlFormat) null, dateTimeStr);
	}
	
	/**
	 * Parses a timedate string to a {@link JNADominoDateTime}
	 * 
	 * @param intl international settings to be used for parsing
	 * @param dateTimeStr timedate string
	 * @return timedate
	 */
	public static JNADominoDateTime fromString(DominoIntlFormat intl, String dateTimeStr) {
		Memory dateTimeStrLMBCS = NotesStringUtils.toLMBCS(dateTimeStr, true);
		//convert method expects a pointer to the date string in memory
		Memory dateTimeStrLMBCSPtr = new Memory(Native.POINTER_SIZE);
		dateTimeStrLMBCSPtr.setPointer(0, dateTimeStrLMBCS);
		
		IntlFormatStruct intlStruct = intl==null ? null : intl.getAdapter(IntlFormatStruct.class);
		
		DisposableMemory retTimeDateMem = new DisposableMemory(JNANotesConstants.timeDateSize);
		NotesTimeDateStruct retTimeDate = NotesTimeDateStruct.newInstance(retTimeDateMem);
		
		short result = NotesCAPI.get().ConvertTextToTIMEDATE(intlStruct, null, dateTimeStrLMBCSPtr, NotesConstants.MAXALPHATIMEDATE, retTimeDate);
		NotesErrorUtils.checkResult(result);
		retTimeDate.read();
		int[] innards = retTimeDate.Innards;
		JNADominoDateTime td = new JNADominoDateTime(innards);
		retTimeDateMem.dispose();
		return td;
	}
}
