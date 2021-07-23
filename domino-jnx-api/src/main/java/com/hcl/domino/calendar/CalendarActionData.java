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

import java.time.temporal.Temporal;
import java.util.List;

import com.hcl.domino.data.IAdaptable;

/**
 * Interface to be used for data to be passed to calendar actions.
 * 
 * @author Tammo Riedinger
 */
public interface CalendarActionData extends IAdaptable {

	String getDelegateTo();

	/**
	 * Sets the name of the delegated user if {@link CalendarProcess#DELEGATE} is used
	 * 
	 * @param delegateTo name either in abbreviated or canonical format
	 * @return the modified instance
	 */
	CalendarActionData withDelegateTo(String delegateTo);

	Temporal getChangeToStart();

	/**
	 * Sets the new start time for {@link CalendarProcess#COUNTER}
	 * 
	 * @param changeToStart new start time
	 * @return the modified instance
	 */
	CalendarActionData withChangeToStart(Temporal changeToStart);

	Temporal getChangeToEnd();

	/**
	 * Sets the new end time for {@link CalendarProcess#COUNTER}
	 * 
	 * @param changeToEnd new end time
	 * @return the modified instance
	 */
	CalendarActionData withChangeToEnd(Temporal changeToEnd);

	boolean isKeepInformed();

	/**
	 * Sets whether the users wants to be kept informed, e.g. when cancelling
	 * an invivation via {@link CalendarProcess#CANCEL}
	 * 
	 * @param keepInformed true to be kept informed
	 * @return the modified instance
	 */
	CalendarActionData withKeepInformed(boolean keepInformed);

	List<String> getAddNamesRequired();

	/**
	 * Sets a new list of required attendees
	 * 
	 * @param addNamesReq attendees, either in canonical or abbreviated format
	 * @return the modified instance
	 */
	CalendarActionData withAddNamesRequired(String... addNamesReq);

	List<String> getAddNamesOptional();
	
	/**
	 * Sets a new list of optional attendees
	 * 
	 * @param addNamesOpt attendees, either in canonical or abbreviated format
	 * @return the modified instance
	 */
	CalendarActionData withAddNamesOptional(String... addNamesOpt);

	List<String> getAddNamesFYI();

	/**
	 * Sets a new list of FYI attendees
	 * 
	 * @param addNamesFYI attendees, either in canonical or abbreviated format
	 * @return the modified instance
	 */
	CalendarActionData withAddNamesFYI(String... addNamesFYI);

	List<String> getRemoveNames();

	/**
	 * Sets a new list of attendees to be removed
	 * 
	 * @param removeNames attendees, either in canonical or abbreviated format
	 * @return the modified instance
	 */
	CalendarActionData withRemoveNames(String... removeNames);
}