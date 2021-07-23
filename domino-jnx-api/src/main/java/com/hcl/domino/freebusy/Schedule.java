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
package com.hcl.domino.freebusy;

import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Optional;

import com.hcl.domino.DominoException;
import com.hcl.domino.data.DominoDateRange;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.IAdaptable;

/**
 * Schedule object to read busy and free time info for a single Domino user
 */
public interface Schedule extends IAdaptable {

  /**
   * Retrieves a user's busy times stored in this schedule
   *
   * @param unidIgnore UNID to ignore in busy time calculations or null
   * @param from       specifies the start of the range over which the free time
   *                   search should be performed. In typical scheduling
   *                   applications, this might be a range of 1 day or 5 days
   * @param until      specifies the end of the range over which the free time
   *                   search should be performed. In typical scheduling
   *                   applications, this might be a range of 1 day or 5 days
   * @return busy times
   */
  List<DominoDateRange> extractBusyTimeRange(String unidIgnore, TemporalAccessor from, TemporalAccessor until);

  /**
   * This routine retrieves one or more free time ranges from a schedule.<br>
   * It will only return 64k of free time ranges.<br>
   * Note: submitting a range or time that is in the past is not supported.
   *
   * @param unidIgnore   UNID to ignore in busy time calculation or null
   * @param findFirstFit If true then only the first fit is used
   * @param from         specifies the start of the range over which the free time
   *                     search should be performed. In typical scheduling
   *                     applications, this might be a range of 1 day or 5 days
   * @param until        specifies the end of the range over which the free time
   *                     search should be performed. In typical scheduling
   *                     applications, this might be a range of 1 day or 5 days
   * @param duration     How much free time you are looking for, in minutes (max
   *                     65535).
   * @return timedate pairs indicating runs of free time
   */
  List<DominoDateRange> extractFreeTimeRange(String unidIgnore,
      boolean findFirstFit, TemporalAccessor from, TemporalAccessor until, int duration);

  /**
   * This retrieves the schedule list from a schedule. A schedule list contains
   * more
   * appointment details than just from/until times that can be read via
   * {@link #extractBusyTimeRange(String, TemporalAccessor, TemporalAccessor)},
   * e.g. the UNID/ApptUNID of the appointments.
   *
   * @param from  specifies the start of the range over which the free time search
   *              should be performed. In typical scheduling applications, this
   *              might be a range of 1 day or 5 days
   * @param until specifies the end of the range over which the free time search
   *              should be performed. In typical scheduling applications, this
   *              might be a range of 1 day or 5 days
   * @return schedule list
   */
  List<ScheduleEntry> extractScheduleList(TemporalAccessor from, TemporalAccessor until);

  /**
   * Returns the owner's mail file replica ID
   *
   * @return replica id
   */
  String getDbReplicaId();

  /**
   * Returns an exception if loading the schedule failed
   *
   * @return an {@link Optional} describing the loading exception, or an empty one
   *         if
   *         there was no error
   */
  Optional<DominoException> getError();

  /**
   * Lower bound of the interval
   *
   * @return an {@link Optional} describing the lower bound, or an empty one if
   *         this
   *         is not available
   */
  Optional<DominoDateTime> getFrom();

  /**
   * Returns the owner of this schedule in canonical format
   *
   * @return owner
   */
  String getOwner();

  /**
   * Upper bound of the interval
   *
   * @return an {@link Optional} describing the upper bound, or an empty one if
   *         this
   *         is not available
   */
  Optional<DominoDateTime> getUntil();

}
