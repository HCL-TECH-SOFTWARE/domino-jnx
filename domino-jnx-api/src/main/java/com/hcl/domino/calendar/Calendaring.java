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
package com.hcl.domino.calendar;

import java.io.IOException;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoDateTime;

/**
 * Interface to access the Calendaring and Scheduling APIs of Domino.<br>
 * <br>
 * <i>Please note that this interface is not feature complete. We currently support
 * creating, updating and reading appointments, but we do not yet support Domino's
 * meeting workflow (accept/decline etc.)</i>
 * 
 * @author Tammo Riedinger
 */
public interface Calendaring {
	/**
	 * Creates a calendar entry.<br>
	 * <br>
	 * This supports either a single entry, or a recurring entry which may contain multiple
	 * VEVENTS represenging both the series and exception data. The iCalendar input must only
	 * contain data for a single UID.For meetings, ATTENDEE PARTSTAT data is ignored.<br>
	 * If the mailfile owner is the meeting organizer, invitations will be sent out to meeting
	 * participants (unless {@link CalendarWrite#DISABLE_IMPLICIT_SCHEDULING} is specified)
	 * 
	 * The following errors will be thrown by this method:
	 * <ul>
	 * <li>NOERROR - on success</li>
	 * <li>{@code ERR_NULL_DBHANDLE} - The database handle is NULL</li>
	 * <li>ERR_MISC_INVALID_ARGS - Unexpected arguments provided</li>
	 * <li>{@code ERR_NO_CALENDAR_FOUND} - Unable to find the entry because the required view does not exist in this database</li>
	 * <li>ERR_EXISTS				An entry already exists</li>
	 * <li>ERR_CS_PROFILE_NOOWNER - Calendar Profile does not specify owner</li>
	 * <li>ERR_UNEXPECTED_METHOD - Provided iCalendar contains a method (no method expected here)</li>
	 * <li>{@code ERR_ICAL2NOTE_CONVERT} - Error interpreting iCalendar input</li>
	 * <li>ERR_MISC_UNEXPECTED_ERROR - Unexpected internal error</li>
	 * <li>{@code ERR_IMPLICIT_SCHED_FAILED} - Entry was updated, but errors were encountered sending notices to meeting participants</li>
	 * </ul>	
	 * 
	 * @param dbMail The database where the entry will be created.
	 * @param iCal The iCalendar data representing the entry to create
	 * @param flags {@link CalendarWrite} flags to control non-default behavior
	 * @return the UID of the created iCalendar, or an empty string if it could not be created
	 */
	String createCalendarEntry(Database dbMail, String iCal, Collection<CalendarWrite> flags);
	
	/**
	 * This will modify an existing calendar entry.<br>
	 * <br>
	 * This supports either single entries or recurring entries, but recurring entries will only
	 * support updates for a single instance specified via RECURRENCE-ID that may not include a
	 * RANGE (This may be permitted in the future but for now will return an error).<br>
	 * <br>
	 * The iCalendar input may only contain a single VEVENT and must contain a UID.<br>
	 * By default, attachments and custom data (for fields contained in $CSCopyItems) will be
	 * maintained from the existing calendar entry.  Similarly, description will also be maintained
	 * if it is not specified in the iCalendar content that is updating.<br>
	 * <br>
	 * Both of these behaviors can be canceled via the CAL_WRITE_COMPLETE_REPLACE flag.<br>
	 * If the mailfile owner is the meeting organizer, appropriate notices will be sent out
	 * to meeting participants (unless {@link CalendarWrite#DISABLE_IMPLICIT_SCHEDULING} is specified).<br>
	 * <br>
	 * The following errors will be thrown by this method:
	 * <ul>
	 * <li>NOERROR - on success</li>
	 * <li>{@code ERR_NULL_DBHANDLE} - The database handle is NULL</li>
	 * <li>ERR_MISC_INVALID_ARGS - Unexpected arguments provided</li>
	 * <li>ERR_CALACTION_INVALID - This calendar entry is not in a state where updating it is supported.</li>
	 * <li>{@code ERR_NO_CALENDAR_FOUND} - Unable to find the entry because the required view does not exist in this database</li>
	 * <li>ERR_NOT_FOUND - There are no entries that match the specified UID or UID/recurid in the database</li>
	 * <li>ERR_NOT_YET_IMPLEMENTED - This update is not yet supported (update range or multiple VEVENTs?)</li>
	 * <li>ERR_CS_PROFILE_NOOWNER - Calendar Profile does not specify owner</li>
	 * <li>ERR_UNEXPECTED_METHOD - Provided iCalendar contains a method (no method expected here)</li>
	 * <li>ERR_ICAL2NOTE_OUTOFDATE - iCalendar input is out of date in regards to sequence information.</li>
	 * <li>{@code ERR_ICAL2NOTE_CONVERT} - Error interpereting iCalendar input</li>
	 * <li>ERR_MISC_UNEXPECTED_ERROR - Unexpected internal error</li>
	 * <li>{@code ERR_IMPLICIT_SCHED_FAILED} - Entry was updated, but errors were encountered sending notices to meeting participants</li>
	 * </ul>
	 * 
	 * @param dbMail The database containing the entry to update
	 * @param iCal The iCalendar data representing the updated entry
	 * @param uid If non-NULL, this value MUST match the UID value in the iCalendar input. If present,or else this returns ERR_InvalidVEventPropertyFound. If the iCalendar input has no UID this value will be used.
	 * @param recurId If non-NULL, this value MUST match the RECURRENCE-ID value in the iCalendar input if present, or else this returns ERR_InvalidVEventPropertyFound. If the iCalendar input has no RECURRENCE-ID this value will be used.
	 * @param comments If non-NULL, this text will be sent as comments on any notices sent to meeting participants as a result of this call. that will be included on the notices. Can be NULL.
	 * @param flags {@link CalendarWrite} flags to control non-default behavior. Supported: CAL_WRITE_MODIFY_LITERAL, {@link CalendarWrite#DISABLE_IMPLICIT_SCHEDULING}, {@link CalendarWrite#IGNORE_VERIFY_DB}.
	 */
	void updateCalendarEntry(Database dbMail, String iCal, String uid, String recurId, String comments,
			Collection<CalendarWrite> flags);
	
	/**
	 * This is a convinience method that returns a UID for a calendar entry document.<br>
	 * NOTEID-&gt;UID is a many to one mapping since one or several documents may represent
	 * a calendar entry (especially if it repeats) and its related notices.
	 * <br>
	 * As such, the UID output will be the same for all documents that refer to the same calendar entry.<br>
	 * This method may incur a document open, so there could be a performance impact.
	 * <br>
	 * The following errors will be thrown by this method:
	 * <ul>
	 * <li>NOERROR - on success</li>
	 * <li>{@code ERR_NULL_DBHANDLE} - The database handle is NULL</li>
	 * <li>ERR_INVALID_NOTE - Note is not valid or is not a calendar note</li>
	 * <li>ERR_MISC_INVALID_ARGS - Unexpected arguments provided</li>
	 * <li>ERR_VALUE_LENGTH - The value is too long for the allocated buffer</li>
	 * </ul>
	 * 
	 * @param document 		the document
	 * @return UID
	 */
	String getUIDfromDocument(Document document);
	
	/**
	 * This is a convinience method that returns a UID from a NOTEID.<br>
	 * NOTEID-&gt;UID is a many to one mapping since one or several notes may represent
	 * a calendar entry (especially if it repeats) and its related notices.
	 * <br>
	 * As such, the UID output will be the same for all notes that refer to the same calendar entry.<br>
	 * This method may incur a note open, so there could be performance impact.
	 * <br>
	 * The following errors will be thrown by this method:
	 * <ul>
	 * <li>NOERROR - on success</li>
	 * <li>{@code ERR_NULL_DBHANDLE} - The database handle is NULL</li>
	 * <li>ERR_INVALID_NOTE - Note is not valid or is not a calendar note</li>
	 * <li>ERR_MISC_INVALID_ARGS - Unexpected arguments provided</li>
	 * <li>ERR_VALUE_LENGTH - The value is too long for the allocated buffer</li>
	 * </ul>
	 * 
	 * @param dbMail The database containing the note referenced by noteid.
	 * @param noteId note id
	 * @return UID
	 */
	String getUIDfromNoteID(Database dbMail, int noteId);
	
	/**
	 * This is a convinience method that returns a UID from a UNID.<br>
	 * <br>
	 * UNID-&gt;UID is a many to one mapping since one or several notes may represent a
	 * calendar entry (especially if it repeats) and its related notices.<br>
	 * As such, the UID output will be the same for all notes that refer to the same
	 * calendar entry.<br>
	 * This method may incur a note open, so there could be performance impact.
	 * <br>
	 * The following errors will be thrown by this method:
	 * <ul>
	 * <li>NOERROR - on success</li>
	 * <li>{@code ERR_NULL_DBHANDLE} - The database handle is NULL</li>
	 * <li>ERR_INVALID_NOTE - Note is not valid or is not a calendar note</li>
	 * <li>ERR_MISC_INVALID_ARGS - Unexpected arguments provided</li>
	 * <li>ERR_VALUE_LENGTH - The value is too long for the allocated buffer</li>
	 * </ul>
	 * 
	 * @param dbMail The database containing the note referenced by unid.
	 * @param unid UNID of a calendar note
	 * @return UID
	 */
	String getUIDFromUNID(Database dbMail, String unid);
	
	/**
	 * This is a convenience method that returns an Apptunid value that corresponds to a UID.<br>
	 * 
	 * @param uid UID of the icalendar entry
	 * @return ApptUnid
	 */
	String getApptUnidFromUID(String uid);
	
	/**
	 * This is a method to get a document for an entry on the calendar.<br>
	 * <br>
	 * The intent is that the note handle can be used to get information about an
	 * entry or instance or to write additional information to the entry or
	 * instance (beyond what is defined in iCalendar and/or available in this API).
	 * When opening a recurring entry, a valid recurrence ID MUST also be provided.<br>
	 * <br>
	 * A note representing the single instance will be returned. This might cause notes to be created or modified as a side effect.
	 * <br>
	 * The following errors will be thrown by this method:
	 * <ul>
	 * <li>NOERROR - on success</li>
	 * <li>{@code ERR_NULL_DBHANDLE} - The database handle is NULL</li>
	 * <li>{@code ERR_NO_CALENDAR_FOUND} - Unable to find the entry because the required view does not exist in this database</li>
	 * <li>ERR_NOT_FOUND - There are no entries that match the specified UID or UID/recurid in the database</li>
	 * <li>ERR_MISC_INVALID_ARGS - Unexpected arguments provided</li>
	 * <li>ERR_TDI_CONV - The recurrence ID specified cannot be interpreted</li>
	 * <li>ERR_MISC_UNEXPECTED_ERROR - Unexpected internal error</li>
	 * </ul>
	 *  
	 * @param dbMail The database containing the entry to open.
	 * @param uid The UID of the entry to get a note handle for.
	 * @param recurId The RECURRENCE-ID of the instance to get a note handle for. Timezones not permitted (time values must be in UTC time). NULL for single entries.  Must be present for recurring entries.
	 * @param flags {@link CalendarDocumentOpen} flags to control non-default behavior. Supported: {@link CalendarDocumentOpen#HANDLE_NOSPLIT}.
	 * @return note
	 */
	Document openCalendarEntryDocument(Database dbMail, String uid, String recurId,
			Collection<CalendarDocumentOpen> flags);
	
	/**
	 * This will return complete iCalendar data for the specified entry.<br>
	 * <br>
	 * For recurring entries, this may result in multiple VEVENTs in the returned
	 * iCalendar data.<br>
	 * In this case, the first VEVENT represents the recurrence set and additional
	 * entries represent exceptions to the recurrence set.<br>
	 * <br>
	 * All instances that differ from the recurrence set will be returned as additional
	 * VEVENTs containing the exceptional data. There is no concept of 'runs' of
	 * instances or RANGE of instances.<br>
	 * Alternatively, a specific instance may be requested using <code>recurId</code>
	 * and only the data for that instance will be returned.<br>
	 * Returned data will not include rich text description.<br>
	 * All participants of a meeting will be returned as PARTSTAT=NEEDS-ACTION even if they have responded.
	 * 
	 * @param dbMail The database from which entries are returned.
	 * @param uid The UID of the entry to be returned.
	 * @param recurId NULL for single entries or to read data for an entire recurring series. If populated, this is the RECURRENCE-ID of the specific instance to read.
	 * @param flags {@link CalendarRead} flags to control non-default behavior
	 * @return iCalendar data
	 */
	String readCalendarEntry(Database dbMail, String uid, String recurId, Collection<CalendarRead> flags);
	
	/**
	 * Gets a summary of calendar entries for a range of times
	 * 
	 * @param dbMail The database from which entries are returned.
	 * @param start the start time of the range
	 * @param end the end time of the range. An exception occurs if the end time is not greater than the start time
	 * @param retICal if not null, we return a summary in iCalendar format of the entries from the start date to the end date, inclusive. An exception occurs if the range contains no entries.
	 * @param retUIDs if not null, we return a list of UIDs found within the range
	 * @throws IOException if writing iCalendar data fails
	 */
	void readRange(Database dbMail, TemporalAccessor start, TemporalAccessor end,
			Appendable retICal, List<String> retUIDs) throws IOException;
	
	/**
	 * Gets a summary of calendar entries for a range of times
	 * 
	 * @param dbMail The database from which entries are returned.
	 * @param start the start time of the range
	 * @param end the end time of the range. An exception occurs if the end time is not greater than the start time
	 * @param skipCount the number of entries to skip from the beginning of the range. This parameter can be used in conjunction with <i>entriesprocessed</i> to read the entries in a series of calls
	 * @param maxRead the maximum number of entries to read
	 * @param retICal if not null, we return a summary in iCalendar format of the entries from the start date to the end date, inclusive. An exception occurs if the range contains no entries.
	 * @param retUIDs if not null, we return a list of UIDs found within the range
	 * @throws IOException if writing iCalendar data fails
	 */
	void readRange(Database dbMail, TemporalAccessor start, TemporalAccessor end, int skipCount, int maxRead,
			Appendable retICal, List<String> retUIDs) throws IOException;
	
	/**
	 * Gets a summary of calendar entries for a range of times
	 * 
	 * @param dbMail The database from which entries are returned.
	 * @param start the start time of the range
	 * @param end the end time of the range. An exception occurs if the end time is not greater than the start time
	 * @param skipCount the number of entries to skip from the beginning of the range. This parameter can be used in conjunction with <i>entriesprocessed</i> to read the entries in a series of calls
	 * @param maxRead the maximum number of entries to read
	 * @param readMask flags that control what properties about the calendar entries will be returned
	 * @param retICal if not null, we return a summary in iCalendar format of the entries from the start date to the end date, inclusive. An exception occurs if the range contains no entries.
	 * @param retUIDs if not null, we return a list of UIDs found within the range
	 * @throws IOException if writing iCalendar data fails
	 */
	void readRange(Database dbMail, TemporalAccessor start, TemporalAccessor end, int skipCount, int maxRead,
			Collection<CalendarReadRange> readMask, Appendable retICal, List<String> retUIDs) throws IOException;
	
	/**
	 * This is a convenience method that returns a RECURRENCE-ID (in UTC time) from a {@link TemporalAccessor} object.
	 * 
	 * @param td Input time/date object
	 * @return RECURRENCE-ID
	 */
	String getRecurrenceID(TemporalAccessor td);
	
	/**
	 * Retrieve the unapplied notices that exist for a participant of calendar entry representing a meeting.<br>
	 * <br>
	 * This will return things like: Reschedules, informational updates, cancelations, confirmations, etc.<br>
	 * <br>
	 * Notices will only be returned if the initial invitation has already been responded to, otherwise
	 * this method will return ERR_INVITE_NOT_ACCEPTED.<br>
	 * <br>
	 * For recurring meetings, notices that apply to any instances in the series will be returned, with
	 * the exception of instances where the initial invitation has not yet been responded to.<br>
	 * <br>
	 * Calendar entries that are not meetings will return ERR_INVALID_NOTE.<br>
	 * <br>
	 * We do not currently support getting unprocessed calendar entries if you are the owner (such as
	 * a counter proposal request or a request for updated information), so this will return
	 * ERR_NOT_YET_IMPLEMENTED.<br>
	 * <br>
	 * Note: For recurring meetings, it is possible that multiple notices will contain current information
	 * for a particular occurence, so it is not possible to guarantee that there is a single "most current"
	 * notice.<br>
	 * <br>
	 * For example, the subject might be changed for a single instance, and then the time may be changed
	 * across instances.<br>
	 * <br>
	 * Because only one notice will have the current subject and another notice will have the current
	 * time but NOT the current subject, both notices will be returned and both must be processed to
	 * guarantee accuracy.<br>
	 * <br>
	 * Process returned notices via the CalNoticeAction method.
	 * 
	 * @param dbMail The database to search for calendar entries
	 * @param uid The UID of the entry to return notices for.
	 * @param retNoteIds return list of note ids or NULL
	 * @param retUNIDs return list of UNIDs or NULL
	 * @return number of notices
	 */
	int getUnappliedNotices(Database dbMail, String uid, List<Integer> retNoteIds, List<String> retUNIDs);
	
	/**
	 * Retrieve invitations in a mailfile that have not yet been responded to.<br>
	 * <br>
	 * This returns the number of new invitations as well as optional NOTEID and/or UNID lists.<br>
	 * This returns only invitations (and delegated invitations), and not reschedules, information
	 * updates, cancels, etc.<br>
	 * <br>
	 * This method does not filter out any invitations that have since been canceled/rescheduled,
	 * or are otherwise out of date.<br>
	 * <br>
	 * Once the invitation is accepted, other notices that apply to that meeting can be discovered
	 * with a call to {@link #getUnappliedNotices(Database, String, List, List)}
	 * must be used (on a per-UID level).<br>
	 * Only invitations for meetings that are current (at least one instance starts within the
	 * last day or in the future) are returned, although the starting time can be specified by
	 * the caller to override the default.A caller can retrieve only invitations that have arrived
	 * since a prior call to {@link #getNewInvitations}
	 * by using tdSince and ptdretUntil.If <code>uid</code> is provided, invitations only for a
	 * particular meeting will be returned.<br>
	 * <br>
	 * This is useful if you are looking for an invitation or invitations that correspond to an
	 * updated notice that has arrived.<br>
	 * <br>
	 * Note: Multiple invitations might exist for a particular UID if that meeting is recurring
	 * and you were added to an instance or instances after the initial creation.<br>
	 * The returned notices are not guaranteed to be in any particular order.
	 * 
	 * @param dbMail The database from which entries are returned.
	 * @param start Optional: If provided, only invitations for meetings that occur on or after this time will be returned.Passing in NULL will use the default value (one day before current time).
	 * @param uid Optional: If present only invitations with a matching UID will be returned. Note: For some repeating meetings there could be multiple invites for the same UID (for separate instances).
	 * @param since Optional: Only return invitations that have been received/modified since the provided time.Passing in NULL will return invitations regardless of when they arrived.
	 * @param retUntil Optional: If provided, this is populated with the time of this method call, which can then be used as the ptdSince argument of a subsequent call.
	 * @param retNoteIds return list of note ids or NULL
	 * @param retUNIDs return list of UNIDs or NULL
	 * @return number of invitations
	 */
    int getNewInvitations(Database dbMail, TemporalAccessor start, String uid, TemporalAccessor since,
			AtomicReference<DominoDateTime> retUntil, List<Integer> retNoteIds, List<String> retUNIDs);
	
	/**
	 * This will return iCalendar data representing a notice with the specified NOTIED.<br>
	 * <br>
	 * A notice may not yet be applied to the calendar entries itself, but an application
	 * may want to read the notice (and process it).<br>
	 * <br>
	 * Examples of notices include invitations, reschedules, information updates, confirmations,
	 * cancelations, counterproposals, requests for information, acceptances, declines,
	 * tenative acceptances, etc.
	 * 
	 * @param dbMail The database from which entries are returned.
	 * @param noteId The NOTEID of the notice to be returned.
	 * @param flags {@link CalendarRead} flags to control non-default behavior. Supported: {@link CalendarRead#HIDE_X_LOTUS}, {@link CalendarRead#INCLUDE_X_LOTUS}.
	 * @return iCalendar data
	 */
	String readNotice(Database dbMail, int noteId, Collection<CalendarRead> flags);
	
	/**
	 * This will return iCalendar data representing a notice with the specified NOTIED.<br>
	 * <br>
	 * A notice may not yet be applied to the calendar entries itself, but an application
	 * may want to read the notice (and process it).<br>
	 * <br>
	 * Examples of notices include invitations, reschedules, information updates, confirmations,
	 * cancelations, counterproposals, requests for information, acceptances, declines,
	 * tenative acceptances, etc.
	 * 
	 * @param dbMail The database from which entries are returned.
	 * @param unid The UNID of the notice to be returned.
	 * @param flags {@link CalendarRead} flags to control non-default behavior. Supported: {@link CalendarRead#HIDE_X_LOTUS}, {@link CalendarRead#INCLUDE_X_LOTUS}.
	 * @return iCalendar data
	 */
	String readNotice(Database dbMail, String unid, Collection<CalendarRead> flags);

	/**
	 * Perform an action on a calendar entry.<br>
	 * <br>
	 * For instance, change the response of an accepted meeting to counter or delegate.<br>
	 * This must be applied to meetings (with the exception of {@link CalendarProcess#DELETE},
	 * which can be applied to any calendar entry).<br>
	 * This makes the appropriate modifications to the invitee calendar and also sends appropriate notices out.

	 * @param dbMail The database containing calendar entries to act on
	 * @param uid The UID of the entry to act on
	 * @param recurId The RECURRENCE-ID of the instance to act on. May be specified for recurring meetings (omission acts on all). MUST be NULL for single meetings. Timezones not permitted (time values must be in UTC time)
	 * @param action The action to perform as defined in {@link CalendarProcess} values
	 * @param scope {@link CalendarRangeRepeat} as defined above (ignored for non-repeating entries)
	 * @param comment Comments to include on the outgoing notice(s) to organizer or participants (can be NULL).
	 * @param data Conveys any additional information required to perform <code>action</code> - NULL for actions that do not require additional information to perform, required for {@link CalendarProcess#DELEGATE}, {@link CalendarProcess#DECLINE} and {@link CalendarProcess#COUNTER} and {@link CalendarProcess#UPDATEINVITEES}.
	 * @param flags Flags - Only {@link CalendarActionOptions#UPDATE_ALL_PARTICIPANTS} is allowed (and only for {@link CalendarProcess#UPDATEINVITEES}
	 */
	void entryAction(Database dbMail, String uid, String recurId, Collection<CalendarProcess> action,
			CalendarRangeRepeat scope, String comment, CalendarActionData data, Collection<CalendarActionOptions> flags);
	
	/**
	 * Process a calendar notice.<br>
	 * This makes the appropriate modifications to the calendar entry and also sends appropriate notices out.
	 * 
	 * @param dbMail The database containing the notice to act on.
	 * @param noteId The noteid of the notice to act on.
	 * @param action The action to perform as defined in {@link CalendarProcess} values.
	 * @param comment Comments to include on the outgoing notice(s) to organizer or participants (can be NULL).
	 * @param data Conveys any additional information required to perform <code>action</code> - NULL for actions that do not require additional information to perform, required for {@link CalendarProcess#DELEGATE} and {@link CalendarProcess#COUNTER}
	 * @param flags Flags - (a {@link CalendarActionOptions} value).
	 */
	void noticeAction(Database dbMail, int noteId, Collection<CalendarProcess> action,
			String comment, CalendarActionData data, Collection<CalendarActionOptions> flags);

	/**
	 * Process a calendar notice.<br>
	 * This makes the appropriate modifications to the calendar entry and also sends appropriate notices out.
	 * 
	 * @param dbMail The database containing the notice to act on.
	 * @param unid The UNID of the notice to act on
	 * @param action The action to perform as defined in {@link CalendarProcess} values.
	 * @param comment Comments to include on the outgoing notice(s) to organizer or participants (can be NULL).
	 * @param data Conveys any additional information required to perform <code>action</code> - NULL for actions that do not require additional information to perform, required for {@link CalendarProcess#DELEGATE} and {@link CalendarProcess#COUNTER}
	 * @param flags Flags - (a {@link CalendarActionOptions} value).
	 */
	void noticeAction(Database dbMail, String unid, Collection<CalendarProcess> action,
			String comment, CalendarActionData data, Collection<CalendarActionOptions> flags);
	
	/**
	 * Use this method to start building the data object to be passed along
	 * calendar actions;
	 * 
	 * @return		the data instance
	 */
	CalendarActionData buildActionData();
}
