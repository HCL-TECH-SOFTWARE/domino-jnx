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

import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.zone.ZoneRules;
import java.util.Optional;
import com.hcl.domino.commons.data.DateFormat;
import com.hcl.domino.commons.data.DateTimeStructure;
import com.hcl.domino.commons.data.DefaultDominoDateTime;
import com.hcl.domino.commons.data.TimeFormat;
import com.hcl.domino.commons.data.ZoneFormat;
import com.hcl.domino.commons.util.InnardsConverter;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.JNANotesConstants;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.capi.NotesCAPI12;
import com.hcl.domino.jna.internal.structs.IntlFormatStruct;
import com.hcl.domino.jna.internal.structs.NotesTFMTStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeStruct;
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
      super(toInnards(dt));
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
		try {
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
  				retTextBuffer.close();
  				outBufLength = outBufLength * 2;
  				retTextBuffer = new DisposableMemory(outBufLength);
  
  				continue;
  			}
  			else {
  				txt = NotesStringUtils.fromLMBCS(retTextBuffer, retTextLength.getValue());
  				break;
  			}
  		}
		} finally {
		  retTextBuffer.close();
		}
		
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
		try(
		    DisposableMemory dateTimeStrLMBCSPtr = new DisposableMemory(Native.POINTER_SIZE);
		    DisposableMemory retTimeDateMem = new DisposableMemory(JNANotesConstants.timeDateSize);
		) {
	        dateTimeStrLMBCSPtr.setPointer(0, dateTimeStrLMBCS);
	        
	        IntlFormatStruct intlStruct = intl==null ? null : intl.getAdapter(IntlFormatStruct.class);
    		NotesTimeDateStruct retTimeDate = NotesTimeDateStruct.newInstance(retTimeDateMem);
    		
    		short result = NotesCAPI.get().ConvertTextToTIMEDATE(intlStruct, null, dateTimeStrLMBCSPtr, NotesConstants.MAXALPHATIMEDATE, retTimeDate);
    		NotesErrorUtils.checkResult(result);
    		retTimeDate.read();
    		int[] innards = retTimeDate.Innards;
    		JNADominoDateTime td = new JNADominoDateTime(innards);
    		return td;
		}
	}
	
    @Override
    public String toISOString() {
      try(
          DisposableMemory td = new DisposableMemory(8);
          DisposableMemory mem = new DisposableMemory(256)
      ) {
        mem.setByte(0, (byte)0);
        td.setInt(0, m_innards0);
        td.setInt(4, m_innards1);
        NotesErrorUtils.checkResult(NotesCAPI12.get().ConvertTIMEDATEtoRFC3339Date(td, mem, (short)256));
        // ISO times are ASCII-safe, so no need to do an LMBCS conversion
        String isoTime = mem.getString(0, StandardCharsets.US_ASCII.name());
        if(isoTime.endsWith("-1Z")) {
          // Then it's date-only
          // e.g. 2023-04-05T-1:-1:-1.-1Z
          int tIndex = isoTime.indexOf('T');
          return isoTime.substring(0, tIndex);
        } else if(isoTime.startsWith("00-")) {
          // Then it's time-only
          // e.g. 00-1--1--1T09:26:43.75Z
          int tIndex = isoTime.indexOf('T');
          return isoTime.substring(tIndex+1, isoTime.length()-1);
        } else {
          return isoTime;
        }
      }
    }
    
    @Override
    public int[] getInnards() {
      return lazilyCreateStruct().Innards;
    }
    
    @Override
    public Optional<Temporal> toTemporal() {
      NotesTimeStruct time = NotesTimeStruct.newInstance();
      time.GM = lazilyCreateStruct();
      time.write();
      if(NotesCAPI.get().TimeGMToLocal(time)) {
        return Optional.empty();
      }
      time.read();
      
      LocalDate localDate = null;
      LocalTime localTime = null;
      
      if(!InnardsConverter.isAnyDate(time.GM.Innards)) {
        localDate = LocalDate.of(time.year, time.month, time.day);
      }
      if(!InnardsConverter.isAnyTime(time.GM.Innards)) {
        localTime = LocalTime.of(time.hour, time.minute, time.second, time.hundredth * 10 * 1000 * 1000);
      }
      
      if(localDate == null && localTime == null) {
        return Optional.empty();
      } else if(localDate == null) {
        return Optional.of(localTime);
      } else if(localTime == null) {
        return Optional.of(localDate);
      } else {
        // Then compose them based on the offset.
        // Offset is the base offset of the zone regardless of DST, in the opposite
        // direction that Java expects.
        // Non-integer zones are expressed as minutes * 100 + hours, e.g. "3002"
        int offsetMinutes = -time.zone / 100;
        int offsetHours = -(time.zone % 100 - (time.dst == 0 ? 0 : 1));
        ZoneOffset offset = ZoneOffset.ofHoursMinutes(offsetHours, offsetMinutes);
        OffsetDateTime odt = OffsetDateTime.of(localDate, localTime, offset);
        return Optional.of(odt);
      }
    }
    
    private static int[] toInnards(TemporalAccessor dt) {
      int[] innards;
      if (dt instanceof ZonedDateTime) {
        ZonedDateTime zdt = (ZonedDateTime)dt;
        ZoneRules rules = zdt.getZone().getRules();
        Instant inst = zdt.toInstant();
        
        int dstOffsetSeconds = 0;
        Duration dstOffset = rules.getDaylightSavings(inst);
        boolean isDst = !dstOffset.isZero();
        if(isDst) {
          // Notes makes some assumptions about what is and is not a valid
          // DST moment based on the current runtime's context. In particular,
          // it will mangle DST times from the opposite hemisphere of the
          // running machine. In these cases, we want to just treat it
          // as a zone-less offset.
          // To determine this, we'll find out whether it falls within
          // the first Sunday in April and the last Sunday in October. This is
          // the internal assumption of start -> end DST.
          ZonedDateTime aprilSunday = zdt.withMonth(4).withDayOfMonth(1);
          DayOfWeek aprilSundayDay = aprilSunday.getDayOfWeek();
          aprilSunday = aprilSunday.plus(7-aprilSundayDay.getValue(), ChronoUnit.DAYS);
          
          ZonedDateTime octoberSunday = zdt.withMonth(10).withDayOfMonth(31);
          DayOfWeek octoberSundayDay = octoberSunday.getDayOfWeek();
          octoberSunday = octoberSunday.minus(7-octoberSundayDay.getValue(), ChronoUnit.DAYS);
          
          boolean lotusDst = (zdt.equals(aprilSunday) || zdt.isAfter(aprilSunday))
              && (zdt.equals(octoberSunday) || zdt.isBefore(octoberSunday));
          
          // With this in hand, find out whether a mid-range day in the current system's zone
          // would have that
          ZonedDateTime midNorthernSummer = ZonedDateTime.of(zdt.getYear(), 7, 1, 0, 0, 0, 0, ZoneId.systemDefault());
          boolean midNorthernDst = midNorthernSummer.getZone().getRules().isDaylightSavings(midNorthernSummer.toInstant());
          if(midNorthernDst != lotusDst) {
            return toInnards(zdt.toOffsetDateTime());
          }

          // Otherwise, continue on to determine the offset
          dstOffsetSeconds = (int)dstOffset.get(ChronoUnit.SECONDS);
          
          if(dstOffsetSeconds != 0 && dstOffsetSeconds != 3600) {
            // Notes assumes a positive DST movement. For a negative movement,
            // it punts, alters the time, and skips the DST flag.
            // Moreover, it assumes DST is one hour. If it's not, it ignores
            // the DST state
            return toInnards(((ZonedDateTime)dt).toOffsetDateTime());
          }
        }
        
        NotesTimeStruct time = NotesTimeStruct.newInstance();
        time.year = dt.get(ChronoField.YEAR);
        time.month = dt.get(ChronoField.MONTH_OF_YEAR);
        time.day = dt.get(ChronoField.DAY_OF_MONTH);
        int weekday = dt.get(ChronoField.DAY_OF_WEEK) + 1;
        time.weekday = weekday == 8 ? 1 : weekday;
        time.hour = dt.get(ChronoField.HOUR_OF_DAY);
        time.minute = dt.get(ChronoField.MINUTE_OF_HOUR);
        time.second = ((ZonedDateTime)dt).getSecond();
        time.hundredth = ((ZonedDateTime)dt).getNano() / 10_000_000;
        
        time.dst = dstOffsetSeconds != 0 ? 1 : 0;
        int offsetSeconds = dt.get(ChronoField.OFFSET_SECONDS) - dstOffsetSeconds;
        int offsetMinutes = (offsetSeconds / 60) % 60;
        if(offsetMinutes % 15 != 0) {
          throw new IllegalArgumentException("Unable to express offsets not in 15-minute increments");
        }
        
        int offsetHours = offsetSeconds / 60 / 60;
        time.zone = -((offsetMinutes * 100) + offsetHours);
        
        time.write();
        if(NotesCAPI.get().TimeLocalToGM(time)) {
          throw new RuntimeException("Unable to create TIMEDATE");
        }
        time.read();
        time.GM.read();
        return time.GM.Innards;
      } else if (dt instanceof OffsetDateTime) {
        NotesTimeStruct time = NotesTimeStruct.newInstance();
        time.write();

        int offsetSeconds = dt.get(ChronoField.OFFSET_SECONDS);
        int offsetMinutes = (offsetSeconds / 60) % 60;
        if(offsetMinutes % 15 != 0) {
          throw new IllegalArgumentException("Unable to express offsets not in 15-minute increments");
        }
        int offsetHours = offsetSeconds / 60 / 60;
        
        time.year = dt.get(ChronoField.YEAR);
        time.month = dt.get(ChronoField.MONTH_OF_YEAR);
        time.day = dt.get(ChronoField.DAY_OF_MONTH);
        int weekday = dt.get(ChronoField.DAY_OF_WEEK) + 1;
        time.weekday = weekday == 8 ? 1 : weekday;
        time.hour = dt.get(ChronoField.HOUR_OF_DAY);
        time.minute = dt.get(ChronoField.MINUTE_OF_HOUR);
        time.second = ((OffsetDateTime)dt).getSecond();
        time.hundredth = ((OffsetDateTime)dt).getNano() / 10_000_000;
        time.dst = 0;
        
        time.zone = -((offsetMinutes * 100) + offsetHours);
        time.write();
        if(NotesCAPI.get().TimeLocalToGM(time)) {
          throw new RuntimeException("Unable to create TIMEDATE");
        }
        time.read();
        time.GM.read();
        return time.GM.Innards;
      } else if (dt instanceof LocalDate) {
        innards = InnardsConverter.encodeInnards((LocalDate) dt);
      } else if (dt instanceof LocalTime) {
        innards = InnardsConverter.encodeInnards((LocalTime) dt);
      } else if (dt instanceof Instant) {
        innards = InnardsConverter
            .encodeInnards(OffsetDateTime.ofInstant((Instant) dt, ZoneId.of("UTC")), null); //$NON-NLS-1$
      } else if (dt instanceof DefaultDominoDateTime) {
        innards = ((DefaultDominoDateTime) dt).getInnards();
      } else {
        final Instant instant = Instant.from(dt);
        innards = InnardsConverter
            .encodeInnards(OffsetDateTime.ofInstant(instant, ZoneId.of("UTC")), null); //$NON-NLS-1$
      }
      return innards;
    }
}
