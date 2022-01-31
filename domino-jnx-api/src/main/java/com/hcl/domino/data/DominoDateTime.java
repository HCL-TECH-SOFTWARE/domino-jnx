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
package com.hcl.domino.data;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;
import java.util.Optional;

/**
 * Represents a Domino date/time value. As a {@link Temporal} representation,
 * these values have the following
 * characteristics:
 * <ul>
 * <li>An instance may represent either a date, a time, or a date/time pair with
 * an optional UTC offset.</li>
 * <li>They can store times to a resolution of 10ms.</li>
 * <li>Time-zone information is stored as an offset only, not as a descriptive
 * zone name, similar to
 * {@link OffsetDateTime}.</li>
 * <li>Time zone offsets must be in 15-minute increments.</li>
 * </ul>
 * <p>
 * Implementations of this class are expected to be immutable.
 * </p>
 *
 * @author Jesse Gallagher
 */
public interface DominoDateTime extends IAdaptable, Temporal, Cloneable, DominoTimeType, Comparable<DominoDateTime> {

  DominoDateTime clone();

  /**
   * @return if this value has a date component
   */
  boolean hasDate();

  /**
   * @return if this value has a time component
   */
  boolean hasTime();

  /**
   * Determines whether the underlying date/time value is invalid. For example,
   * retrieving the
   * creation date of a document with an arbitrarily-set UNID may result in an
   * unexpressable
   * time.
   *
   * @return {@code true} if the time value is valid and can be expressed
   *         properly; {@code false}
   *         otherwise
   */
  boolean isValid();

  /**
   * Converts the value to a {@link LocalDate} object if applicable
   *
   * @return a new {@link LocalDate} object representing the same value as this
   *         object
   * @throws DateTimeException if unable to convert to an {@code LocalDate}
   */
  LocalDate toLocalDate();

  /**
   * Converts the value to a {@link LocalTime} object if applicable
   *
   * @return a new {@link LocalTime} object representing the same value as this
   *         object
   * @throws DateTimeException if unable to convert to an {@code LocalTime}
   */
  LocalTime toLocalTime();

  /**
   * Converts the value to a {@link OffsetDateTime} object if applicable
   *
   * @return a new {@link OffsetDateTime} object representing the same value as
   *         this object
   * @throws DateTimeException if unable to convert to an {@code OffsetDateTime}
   */
  OffsetDateTime toOffsetDateTime();

  /**
   * Converts the value to a standard JDK {@link Temporal} implementation based on
   * the internal value. Expected to be one of:
   * <ul>
   * <li>{@link OffsetDateTime}</li>
   * <li>{@link LocalDate}</li>
   * <li>{@link LocalTime}</li>
   * </ul>
   *
   * @return an {@link Optional} describing a new {@link Temporal} instance
   *         representing the same
   *         value as this object, or an empty one if the date/time value is
   *         invalid
   */
  Optional<Temporal> toTemporal();

}
