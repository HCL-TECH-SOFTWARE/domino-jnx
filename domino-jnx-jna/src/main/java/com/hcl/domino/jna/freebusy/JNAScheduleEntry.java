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
package com.hcl.domino.jna.freebusy;

import java.text.MessageFormat;
import java.time.temporal.TemporalAccessor;
import java.util.EnumSet;
import java.util.Optional;

import com.hcl.domino.data.DominoDateRange;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.freebusy.ScheduleAttr;
import com.hcl.domino.freebusy.ScheduleEntry;
import com.hcl.domino.jna.data.JNADominoDateRange;
import com.hcl.domino.jna.data.JNADominoDateTime;
import com.hcl.domino.jna.internal.structs.NotesSchedEntryExtStruct;
import com.hcl.domino.jna.internal.structs.NotesSchedEntryStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDatePairStruct;

/**
 * Entry of a schedule list retrieved via {@link JNASchedule#extractScheduleList(TemporalAccessor, TemporalAccessor)}
 * 
 * @author Tammo Riedinger
 */
public class JNAScheduleEntry implements ScheduleEntry {
	/** UNID of the entry */
	private String m_unid;
	/** Interval of the entry */	
	private DominoDateRange m_interval;
	/** {@link ScheduleAttr} attributes defined by Notes */
	private EnumSet<ScheduleAttr> m_attr;
	/** Application specific attributes */
	private byte m_userAttr;
	/** ApptUNID of the entry */
	private String apptUnid;
	/** Size of this entry (for future ease of expansion) */
	private int dwEntrySize;
	/** Longitude coordinate value */
	private double nLongitude;
	/** Latitude coordinate value */
	private double nLatitude;

	public JNAScheduleEntry(NotesSchedEntryStruct entry) {
		this.m_unid = entry.Unid==null ? null : entry.Unid.toString();
		NotesTimeDatePairStruct intervalTDPair = entry.Interval;
		if (intervalTDPair!=null && intervalTDPair.Lower!=null && intervalTDPair.Upper!=null) {
			this.m_interval = new JNADominoDateRange(
				new JNADominoDateTime(intervalTDPair.Lower),
				new JNADominoDateTime(intervalTDPair.Upper)
			);
		}
		int attrAsInt = entry.Attr & 0xff;
		this.m_attr = EnumSet.noneOf(ScheduleAttr.class);
		for (ScheduleAttr currAttr : ScheduleAttr.values()) {
			if ((attrAsInt & currAttr.getValue())==currAttr.getValue()) {
				this.m_attr.add(currAttr);
			}
		}
		this.m_userAttr = entry.UserAttr;
	}

	public JNAScheduleEntry(NotesSchedEntryExtStruct entryExt) {
		this.m_unid = entryExt.Unid==null ? null : entryExt.Unid.toString();
		NotesTimeDatePairStruct intervalTDPair = entryExt.Interval;
		if (intervalTDPair!=null && intervalTDPair.Lower!=null && intervalTDPair.Upper!=null) {
			this.m_interval = new JNADominoDateRange(
				new JNADominoDateTime(intervalTDPair.Lower),
				new JNADominoDateTime(intervalTDPair.Upper)
			);
		}
		int attrAsInt = entryExt.Attr & 0xff;
		this.m_attr = EnumSet.noneOf(ScheduleAttr.class);
		for (ScheduleAttr currAttr : ScheduleAttr.values()) {
			if ((attrAsInt & currAttr.getValue())==currAttr.getValue()) {
				this.m_attr.add(currAttr);
			}
		}
		this.m_userAttr = entryExt.UserAttr;
		this.apptUnid = entryExt.ApptUnid==null ? null : entryExt.ApptUnid.toString();
		this.dwEntrySize = entryExt.dwEntrySize;
		this.nLongitude = entryExt.nLongitude;
		this.nLatitude = entryExt.nLatitude;
	}

	@Override
	public String getUnid() {
		return m_unid;
	}

	@Override
	public Optional<DominoDateTime> getFrom() {
		return Optional.ofNullable(m_interval==null ? null : m_interval.getStartDateTime());
	}
	
	@Override
	public Optional<DominoDateTime> getUntil() {
		return Optional.ofNullable(m_interval==null ? null : m_interval.getEndDateTime());
	}

	@Override
	public EnumSet<ScheduleAttr> getAttributes() {
		return m_attr;
	}

	@Override
	public boolean hasAttribute(ScheduleAttr attr) {
		return m_attr!=null && m_attr.contains(attr);
	}

	@Override
	public String getApptUnid() {
		return apptUnid;
	}

	@Override
	public boolean isPenciled() {
		return m_attr!=null && m_attr.contains(ScheduleAttr.PENCILED);
	}

	@Override
	public boolean isRepeatEvent() {
		return m_attr!=null && m_attr.contains(ScheduleAttr.REPEATEVENT);
	}

	@Override
	public boolean isBusy() {
		return m_attr!=null && m_attr.contains(ScheduleAttr.BUSY);
	}
	
	@Override
	public boolean isAppointment() {
		return m_attr!=null && m_attr.contains(ScheduleAttr.APPT);
	}
	
	@Override
	public boolean isNonWork() {
		return m_attr!=null && m_attr.contains(ScheduleAttr.NONWORK);
	}
	
	@Override
	public String toString() {
		return MessageFormat.format("JNAScheduleEntry [UNID={0}, from={1}, until={2}]", m_unid, (m_interval==null ? "null" : m_interval.getStartDateTime().toString()), (m_interval==null ? "null" : m_interval.getEndDateTime().toString())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
