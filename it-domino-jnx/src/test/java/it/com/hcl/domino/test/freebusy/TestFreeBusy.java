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
package it.com.hcl.domino.test.freebusy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.DominoDateRange;
import com.hcl.domino.freebusy.ScheduleOptions;
import com.hcl.domino.freebusy.Schedules;
import com.ibm.commons.util.StringUtil;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestFreeBusy extends AbstractNotesRuntimeTest {
	public static final String FREEBUSY_USERS = "FREEBUSY_USERS"; //$NON-NLS-1$

	@Test
	@EnabledIfEnvironmentVariable(named = FREEBUSY_USERS, matches = ".+")
	public void testFreeTimeSearch() {
		String usersEnv = System.getenv(FREEBUSY_USERS);
		
		List<String> names = Arrays.asList(StringUtil.splitString(usersEnv, ','));
		
		DominoClient client = getClient();
		TemporalAccessor from = Instant.now().minus(1, ChronoUnit.DAYS);
		TemporalAccessor until = Instant.now().plus(3, ChronoUnit.DAYS);
		
		List<DominoDateRange> range = client.getFreeBusy().freeTimeSearch(null, null, false, from, until, 60, names);
		System.out.println("Freetime received: " + range);
	}
	
	@Test
	@EnabledIfEnvironmentVariable(named = FREEBUSY_USERS, matches = ".+")
	public void testReadSchedules() {
		String usersEnv = System.getenv(FREEBUSY_USERS);
		
		List<String> names = Arrays.asList(StringUtil.splitString(usersEnv, ','));
		
		DominoClient client = getClient();
		TemporalAccessor from = Instant.now().minus(1, ChronoUnit.DAYS);
		TemporalAccessor until = Instant.now().plus(3, ChronoUnit.DAYS);
		
		Schedules schedules = client.getFreeBusy().retrieveSchedules(null, EnumSet.of(
				ScheduleOptions.EACHPERSON
				), from, until, names);
		System.out.println("Schedules received: "+schedules);
	}
}
