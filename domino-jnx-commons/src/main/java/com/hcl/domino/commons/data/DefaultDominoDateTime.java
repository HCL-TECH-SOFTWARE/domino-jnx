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
package com.hcl.domino.commons.data;

import java.text.MessageFormat;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQuery;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.Optional;

import com.hcl.domino.commons.util.InnardsConverter;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.misc.NotesConstants;

public class DefaultDominoDateTime implements DominoDateTime {

  /**
   * Returns a {@link DefaultDominoDateTime} instance for the provided
   * {@link TemporalAccessor} value.
   * If {@code temporal} is already a {@code JNADominoDateTime}, it is returned
   * directly.
   * 
   * @param temporal the {@link TemporalAccessor} value to interpret
   * @return a {@link DefaultDominoDateTime} corresponding to the provided value,
   *         or {@code null}
   *         if {@code temporal} is null
   */
  public static DefaultDominoDateTime from(final TemporalAccessor temporal) {
    if (temporal == null) {
      return null;
    } else if (temporal instanceof DefaultDominoDateTime) {
      return (DefaultDominoDateTime) temporal;
    } else {
      return new DefaultDominoDateTime(temporal);
    }
  }

  protected final int m_innards0;
  protected final int m_innards1;

  /**
   * Creates a new date/time object and sets it to the current date/time
   */
  public DefaultDominoDateTime() {
    this(OffsetDateTime.now());
  }

  /**
   * Creates a new date/time object and sets it to a date/time specified as
   * innards array
   * 
   * @param innards innards array
   */
  public DefaultDominoDateTime(final int innards[]) {
    this.m_innards0 = innards[0];
    this.m_innards1 = innards[1];
  }

  /**
   * Creates a new date/time object and sets it to the specified time in
   * milliseconds since
   * GMT 1/1/70
   * 
   * @param timeMs the milliseconds since January 1, 1970, 00:00:00 GMT
   */
  public DefaultDominoDateTime(final long timeMs) {
    this(InnardsConverter.encodeInnards(ZonedDateTime.ofInstant(Instant.ofEpochMilli(timeMs), ZoneId.of("UTC")))); //$NON-NLS-1$
  }

  /**
   * Creates a new date/time object and sets it to the specified
   * {@link ZonedDateTime}
   * 
   * @param dt zoned date/time value
   */
  public DefaultDominoDateTime(final TemporalAccessor dt) {
    int[] innards;
    if (dt instanceof ZonedDateTime) {
      innards = InnardsConverter.encodeInnards((ZonedDateTime) dt);
    } else if (dt instanceof OffsetDateTime) {
      innards = InnardsConverter.encodeInnards((OffsetDateTime) dt, null);
    } else if (dt instanceof LocalDate) {
      innards = InnardsConverter.encodeInnards((LocalDate) dt);
    } else if (dt instanceof LocalTime) {
      innards = InnardsConverter.encodeInnards((LocalTime) dt);
    } else if (dt instanceof Instant) {
      innards = InnardsConverter.encodeInnards(OffsetDateTime.ofInstant((Instant) dt, ZoneId.of("UTC")), null); //$NON-NLS-1$
    } else if (dt instanceof DefaultDominoDateTime) {
      innards = ((DefaultDominoDateTime) dt).getInnards();
    } else {
      final Instant instant = Instant.from(dt);
      innards = InnardsConverter.encodeInnards(OffsetDateTime.ofInstant(instant, ZoneId.of("UTC")), null); //$NON-NLS-1$
    }
    this.m_innards0 = innards[0];
    this.m_innards1 = innards[1];
  }

  /**
   * Creates a new {@link DominoDateTime} instance with the same data as this one
   */
  @Override
  public DefaultDominoDateTime clone() {
    return new DefaultDominoDateTime(this.getInnards());
  }

  @Override
  public int compareTo(final DominoDateTime o) {
    if (this.hasDate() && o.hasDate()) {
      if (this.hasTime() && o.hasTime()) {
        return this.toOffsetDateTime().compareTo(o.toOffsetDateTime());
      } else {
        return this.toLocalDate().compareTo(o.toLocalDate());
      }
    } else if (this.hasTime() && o.hasTime()) {
      return this.toLocalTime().compareTo(o.toLocalTime());
    } else {
      // Incompatible operands
      return 0;
    }
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof DefaultDominoDateTime) {
      return Arrays.equals(this.getInnards(), ((DefaultDominoDateTime) o).getInnards());
    } else if (o instanceof DominoDateTime) {
      return this.toOffsetDateTime().equals(((DominoDateTime) o).toOffsetDateTime());
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getAdapter(final Class<T> clazz) {
    if (int[].class.equals(clazz)) {
      return (T) this.getInnards();
    }
    return null;
  }

  /**
   * Returns a copy of the internal Innards values
   * 
   * @return innards
   */
  public int[] getInnards() {
    return new int[] { this.m_innards0, this.m_innards1 };
  }

  @Override
  public long getLong(final TemporalField field) {
    // TODO modify to check without conversion for compatible types
    return this.toTemporal().get().getLong(field);
  }

  @Override
  public boolean hasDate() {
    final boolean hasDate = this.m_innards1 != 0 && this.m_innards1 != NotesConstants.ANYDAY;
    return hasDate;
  }

  @Override
  public int hashCode() {
    final int[] innards = this.getInnards();
    return Arrays.hashCode(innards);
  }

  @Override
  public boolean hasTime() {
    final boolean hasTime = this.m_innards0 != 0 && this.m_innards0 != NotesConstants.ALLDAY;
    return hasTime;
  }

  @Override
  public boolean isSupported(final TemporalField field) {
    if (ChronoField.YEAR.equals(field) ||
        ChronoField.MONTH_OF_YEAR.equals(field) ||
        ChronoField.DAY_OF_MONTH.equals(field) ||
        ChronoField.HOUR_OF_DAY.equals(field) ||
        ChronoField.MINUTE_OF_HOUR.equals(field) ||
        ChronoField.SECOND_OF_MINUTE.equals(field) ||
        ChronoField.MILLI_OF_SECOND.equals(field) ||
        ChronoField.NANO_OF_SECOND.equals(field)) {

      return true;
    }
    return false;
  }

  @Override
  public boolean isSupported(final TemporalUnit unit) {
    if (ChronoUnit.YEARS.equals(unit) ||
        ChronoUnit.MONTHS.equals(unit) ||
        ChronoUnit.DAYS.equals(unit) ||
        ChronoUnit.HOURS.equals(unit) ||
        ChronoUnit.MINUTES.equals(unit) ||
        ChronoUnit.SECONDS.equals(unit) ||
        ChronoUnit.MILLIS.equals(unit)) {

      return true;
    }
    return false;
  }

  @Override
  public boolean isValid() {
    return this.toTemporal().isPresent();
  }

  @Override
  public Temporal plus(final long amountToAdd, final TemporalUnit unit) {
    Temporal temporal = this.toTemporal().get();
    temporal = temporal.plus(amountToAdd, unit);
    return new DefaultDominoDateTime(temporal);
  }

  @Override
  public <R> R query(final TemporalQuery<R> query) {
    return this.toTemporal().get().query(query);
  }

  @Override
  public LocalDate toLocalDate() {
    final Temporal temporal = this.toTemporal().orElse(null);
    if (temporal instanceof OffsetDateTime) {
      return ((OffsetDateTime) temporal).toLocalDate();
    } else if (temporal instanceof ZonedDateTime) {
      return ((ZonedDateTime) temporal).toLocalDate();
    } else if (temporal instanceof LocalDate) {
      return (LocalDate) temporal;
    } else {
      throw new DateTimeException(MessageFormat.format("Cannot create an LocalDate from a {0}", temporal.getClass().getName()));
    }
  }

  @Override
  public LocalTime toLocalTime() {
    final Temporal temporal = this.toTemporal().orElse(null);
    if (temporal instanceof OffsetDateTime) {
      return ((OffsetDateTime) temporal).toLocalTime();
    } else if (temporal instanceof ZonedDateTime) {
      return ((ZonedDateTime) temporal).toLocalTime();
    } else if (temporal instanceof LocalTime) {
      return (LocalTime) temporal;
    } else {
      throw new DateTimeException(MessageFormat.format("Cannot create an LocalTime from a {0}", temporal.getClass().getName()));
    }
  }

  @Override
  public OffsetDateTime toOffsetDateTime() {
    final Temporal temporal = this.toTemporal().orElse(null);
    if (temporal instanceof OffsetDateTime) {
      return (OffsetDateTime) temporal;
    } else if (temporal instanceof ZonedDateTime) {
      return ((ZonedDateTime) temporal).toOffsetDateTime();
    } else if (temporal != null) {
      throw new DateTimeException(
          MessageFormat.format("Cannot create an OffsetDateTime from a {0}", temporal.getClass().getName()));
    } else {
      throw new DateTimeException("Cannot create an OffsetDateTime: Date object is invalid");
    }
  }

  @Override
  public String toString() {
    return this.toTemporal()
        .map(Object::toString)
        .orElse(super.toString());
  }

  @Override
  public Optional<Temporal> toTemporal() {
    final int[] innards = this.getInnards();
    return Optional.ofNullable(InnardsConverter.decodeInnards(innards));
  }

  @Override
  public long until(final Temporal endExclusive, final TemporalUnit unit) {
    final Temporal temporal = this.toTemporal().get();
    return temporal.until(endExclusive, unit);
  }

  @Override
  public Temporal with(final TemporalField field, final long newValue) {
    Temporal temporal = this.toTemporal().get();
    temporal = temporal.with(field, newValue);
    return new DefaultDominoDateTime(temporal);
  }
}
