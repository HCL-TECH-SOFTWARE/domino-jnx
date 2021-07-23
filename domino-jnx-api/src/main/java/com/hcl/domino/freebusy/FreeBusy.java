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
import java.util.Collection;
import java.util.List;

import com.hcl.domino.data.DominoDateRange;

/**
 * Includes O.o.o info
 *
 * @author t.b.d
 */
public interface FreeBusy {

  /**
   * This routine searches the schedule database (locally or on a specified
   * server) for free time periods
   * common to a specified list of people.
   *
   * @param apptUnid     This is the UNID of an appointment to ignore for the
   *                     purposes of calculating free time. This is useful when
   *                     you need to move an appointment to a time which overlaps
   *                     it. Can be null
   * @param apptOrigDate This is the date of the original date of the appointment
   *                     to ignore for free time calculations. Note that the only
   *                     reason that this is here is for compatibility with
   *                     Organizer 2.x gateway.
   * @param findFirstFit If this value is equal to TRUE then this routine will
   *                     return just the first free time interval that fits the
   *                     duration. The size of this interval will equal to
   *                     duration.
   * @param from         specifies the start of the range over which the free time
   *                     search should be performed. In typical scheduling
   *                     applications, this might be a range of 1 day or 5 days
   * @param until        specifies the end of the range over which the free time
   *                     search should be performed. In typical scheduling
   *                     applications, this might be a range of 1 day or 5 days
   * @param duration     How much free time you are looking for, in minutes (max
   *                     65535).
   * @param names        list of distinguished names whose schedule should be
   *                     searched, either in abbreviated or canonical format
   * @return timedate pairs indicating runs of free time
   */
  List<DominoDateRange> freeTimeSearch(String apptUnid, TemporalAccessor apptOrigDate,
      boolean findFirstFit, TemporalAccessor from, TemporalAccessor until, int duration, Collection<String> names);

  /**
   * Synchronously retrieves a local or remote schedule by asking the caller's
   * home server for the schedule.<br>
   * <br>
   * The ONLY time that local busy time is used is when the client is in the
   * Disconnected mode
   * which is specified through the location document.<br>
   * <br>
   * Otherwise, the API will route ALL lookup requests to the users home server
   * for processing.
   *
   * @param apptUnid Ignore this UNID in computations
   * @param options  option flags
   * @param from     specifies the start of the range over which the schedule
   *                 lookup should be performed. In typical scheduling
   *                 applications, this might be a range of 1 day or 5 days
   * @param until    specifies the end of the range over which the schedule lookup
   *                 should be performed. In typical scheduling applications, this
   *                 might be a range of 1 day or 5 days
   * @param names    list of distinguished names whose schedule should be
   *                 searched, either in abbreviated or canonical format
   * @return schedules list
   */
  Schedules retrieveSchedules(String apptUnid, Collection<ScheduleOptions> options,
      TemporalAccessor from, TemporalAccessor until, Collection<String> names);

}
