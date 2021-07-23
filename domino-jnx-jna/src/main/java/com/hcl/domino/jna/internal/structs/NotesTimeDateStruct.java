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

import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.hcl.domino.commons.structs.WrongArraySizeException;
import com.hcl.domino.commons.util.InnardsConverter;
import com.hcl.domino.commons.util.NotesDateTimeUtils;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.jna.data.JNADominoDateTime;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * JNA class for the TIMEDATE type
 * 
 * @author Karsten Lehmann
 */
public class NotesTimeDateStruct extends BaseStructure implements Serializable, IAdaptable, Cloneable {
	private static final long serialVersionUID = 549580185343880134L;
	
	/** C type : DWORD[2] */
	public int[] Innards = new int[2];
	
	public static NotesTimeDateStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesTimeDateStruct>) () -> new NotesTimeDateStruct());
	}

	public static NotesTimeDateStruct newInstance(final int Innards[]) {
		return AccessController.doPrivileged((PrivilegedAction<NotesTimeDateStruct>) () -> new NotesTimeDateStruct(Innards));
	}
	
	public static NotesTimeDateStruct newInstance(final Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<NotesTimeDateStruct>) () -> {
			NotesTimeDateStruct newObj = new NotesTimeDateStruct(peer);
			newObj.read();
			return newObj;
		});
	}
	
	public static NotesTimeDateStruct newInstance(TemporalAccessor temporal) {
		return newInstance(JNADominoDateTime.from(temporal).getInnards());
	}

	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesTimeDateStruct() {
		super();
	}
	
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("Innards"); //$NON-NLS-1$
	}
	
	/** @param Innards C type : DWORD[2]
	 * 
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesTimeDateStruct(int Innards[]) {
		super();
		if ((Innards.length != this.Innards.length)) {
			throw new WrongArraySizeException("Innards"); //$NON-NLS-1$
		}
		this.Innards = Innards;
	}
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 * 
	 * @param peer pointer
	 */
	@Deprecated
	public NotesTimeDateStruct(Pointer peer) {
		super(peer);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> clazz) {
		if (clazz == NotesTimeDateStruct.class) {
			return (T) this;
		}
		else if (clazz == Pointer.class) {
			return (T) getPointer();
		}
		return null;
	}
	
	public static class ByReference extends NotesTimeDateStruct implements Structure.ByReference {
		private static final long serialVersionUID = 4522277719976953801L;
		
	};
	public static class ByValue extends NotesTimeDateStruct implements Structure.ByValue {
		private static final long serialVersionUID = -4346454795599331918L;
		
		public static NotesTimeDateStruct.ByValue newInstance() {
			return AccessController.doPrivileged((PrivilegedAction<ByValue>) () -> new NotesTimeDateStruct.ByValue());
		}
		
		public static NotesTimeDateStruct.ByValue newInstance(final int[] innards) {
			return AccessController.doPrivileged((PrivilegedAction<ByValue>) () -> {
				NotesTimeDateStruct.ByValue newObj = new NotesTimeDateStruct.ByValue();
				newObj.Innards[0] = innards[0];
				newObj.Innards[1] = innards[1];
				return newObj;
			});
		}
	};
	
	/**
	 * Checks whether the timedate has a date portion
	 * 
	 * @return true if date part exists
	 */
	public boolean hasDate() {
        boolean hasDate=(Innards[1]!=0 && Innards[1]!=NotesConstants.ANYDAY);
		return hasDate;
	}
	
	/**
	 * Checks whether the timedate has a time portion
	 * 
	 * @return true if time part exists
	 */
	public boolean hasTime() {
        boolean hasDate=(Innards[0]!=0 && Innards[0]!=NotesConstants.ALLDAY);
		return hasDate;
	}
	
	/**
	 * Converts the time date to a calendar
	 * 
	 * @return calendar or null if data is invalid
	 */
	@SuppressWarnings("deprecation")
	public Calendar toCalendar() {
		return NotesDateTimeUtils.innardsToCalendar(this.Innards);
	}
	
	/**
	 * Converts the time date to a Java {@link Date}
	 * 
	 * @return date or null if data is invalid
	 */
	public Date toDate() {
		Calendar cal = toCalendar();
		return cal==null ? null : cal.getTime();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof NotesTimeDateStruct) {
			return Arrays.equals(this.Innards, ((NotesTimeDateStruct)o).Innards);
		}
		return false;
	}
	
	/**
	 * Sets the date/time of this timedate to the current time
	 */
	public void setNow() {
		int[] newInnards = InnardsConverter.encodeInnards(ZonedDateTime.now());
		this.Innards[0] = newInnards[0];
		this.Innards[1] = newInnards[1];
		write();
	}

	/**
	 * Sets the date part of this timedate to today and the time part to ALLDAY
	 */
	public void setToday() {
		int[] newInnards = InnardsConverter.encodeInnards(LocalDate.now());
		this.Innards[0] = newInnards[0];
		this.Innards[1] = newInnards[1];
		write();
	}

	/**
	 * Sets the date part of this timedate to tomorrow and the time part to ALLDAY
	 */
	public void setTomorrow() {
		int[] newInnards = InnardsConverter.encodeInnards(LocalDate.now().plus(1, ChronoUnit.DAYS));
		this.Innards[0] = newInnards[0];
		this.Innards[1] = newInnards[1];
		write();
	}

	/**
	 * Removes the time part of this timedate
	 */
	public void setAnyTime() {
		this.Innards[0] = NotesConstants.ALLDAY;
		write();
	}
	
	/**
	 * Checks whether the time part of this timedate is a wildcard
	 * 
	 * @return true if there is no time
	 */
	public boolean isAnyTime() {
		return this.Innards[0] == NotesConstants.ALLDAY;
	}
	
	/**
	 * Removes the date part of this timedate
	 */
	public void setAnyDate() {
		this.Innards[1] = NotesConstants.ANYDAY;
		write();
	}
	
	/**
	 * Checks whether the date part of this timedate is a wildcard
	 * 
	 * @return true if there is no date
	 */
	public boolean isAnyDate() {
		return this.Innards[1] == NotesConstants.ANYDAY;
	}
	
	/**
	 * Creates a new {@link NotesTimeDateStruct} instance with the same data as this one
	 */
	@Override
	@SuppressFBWarnings(value="CN_IDIOM_NO_SUPER_CALL", justification="Dealing with native memory")
	public NotesTimeDateStruct clone() {
		NotesTimeDateStruct clone = new NotesTimeDateStruct();
		clone.Innards[0] = this.Innards[0];
		clone.Innards[1] = this.Innards[1];
		clone.write();
		return clone;
	}
	
	/**
	 * Modifies the data by adding/subtracting values for year, month, day, hours, minutes and seconds
	 * 
	 * @param year positive or negative value or 0 for no change
	 * @param month positive or negative value or 0 for no change
	 * @param day positive or negative value or 0 for no change
	 * @param hours positive or negative value or 0 for no change
	 * @param minutes positive or negative value or 0 for no change
	 * @param seconds positive or negative value or 0 for no change
	 */
	public void adjust(int year, int month, int day, int hours, int minutes, int seconds) {
		@SuppressWarnings("deprecation")
		Calendar cal = NotesDateTimeUtils.innardsToCalendar(this.Innards);
		if (cal!=null) {
			boolean modified = false;
			
			if (NotesDateTimeUtils.hasDate(cal)) {
				if (year!=0) {
					cal.add(Calendar.YEAR, year);
					modified=true;
				}
				if (month!=0) {
					cal.add(Calendar.MONTH, month);
					modified=true;
				}
				if (day!=0) {
					cal.add(Calendar.DATE, day);
					modified=true;
				}
			}
			if (NotesDateTimeUtils.hasTime(cal)) {
				if (hours!=0) {
					cal.add(Calendar.HOUR, hours);
					modified=true;
				}
				if (minutes!=0) {
					cal.add(Calendar.MINUTE, minutes);
					modified=true;
				}
				if (seconds!=0) {
					cal.add(Calendar.SECOND, seconds);
					modified=true;
				}
			}
			
			if (modified) {
				int[] newInnards = InnardsConverter.encodeInnards(((GregorianCalendar)cal).toZonedDateTime());
				this.Innards[0] = newInnards[0];
				this.Innards[1] = newInnards[1];
				write();
			}
		}
	}
	
	public static NotesTimeDateStruct.ByReference newInstanceByReference() {
		return AccessController.doPrivileged((PrivilegedAction<ByReference>) () -> new NotesTimeDateStruct.ByReference());
	}

}
