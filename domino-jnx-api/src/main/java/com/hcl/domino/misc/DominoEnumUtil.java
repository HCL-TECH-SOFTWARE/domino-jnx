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
package com.hcl.domino.misc;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;

/**
 * This class contains utility methods for dealing with {@link INumberEnum}
 * Domino enums.
 *
 * @author Jesse Gallagher
 */
public enum DominoEnumUtil {
  ;

  /**
   * Converts the provided collection of number-backed enum values to a bitfield
   * numerical value.
   *
   * @param <N>    the boxed primitive type that backs the enum
   * @param <T>    the number-backed enum type
   * @param clazz  the number-backed enum class
   * @param values a collection of enum values to convert to a bitfield
   * @return a boxed primitive value containing the bitfield
   */
  @SuppressWarnings("unchecked")
  public static <N extends Number, T extends Enum<T> & INumberEnum<N>> N toBitField(final Class<T> clazz,
      final Collection<T> values) {
    Long result = Long.valueOf(0);
    if (values != null) {
      for (final T enumVal : values) {
        result |= enumVal.getLongValue();
      }
    }

    // Boil the value down to the right size
    final Class<N> numberClass = (Class<N>) clazz.getEnumConstants()[0].getValue().getClass();
    if (numberClass.equals(Byte.class)) {
      return (N) Byte.valueOf(result.byteValue());
    } else if (numberClass.equals(Short.class)) {
      return (N) Short.valueOf(result.shortValue());
    } else if (numberClass.equals(Integer.class)) {
      return (N) Integer.valueOf(result.intValue());
    } else {
      return (N) result;
    }
  }

  /**
   * Given a Domino number-style enum, returns the enum constant for the provided
   * <code>int</code>
   *
   * @param <N>   subclass of {@link Number} to store enum values
   * @param <T>   the number-backed enum type
   * @param clazz the number-backed enum class
   * @param value the value to convert
   * @return an {@link Optional} describing the corresponding enum value, or an
   *         empty one if none match
   */
  public static <N extends Number, T extends Enum<T> & INumberEnum<N>> Optional<T> valueOf(final Class<T> clazz, final int value) {
    for (final T enumVal : clazz.getEnumConstants()) {
      if(!enumVal.isSkipInLookup()) {
        final long enumValue = enumVal.getLongValue();
        if (enumValue == value) {
          return Optional.of(enumVal);
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Given a Domino number-style enum, returns the enum constant for the provided
   * <code>long</code>
   *
   * @param <N>   subclass of {@link Number} to store enum values
   * @param <T>   the number-backed enum type
   * @param clazz the number-backed enum class
   * @param value the value to convert
   * @return an {@link Optional} describing the corresponding enum value, or an
   *         empty one if none match
   */
  public static <N extends Number, T extends Enum<T> & INumberEnum<N>> Optional<T> valueOf(final Class<T> clazz, final long value) {
    for (final T enumVal : clazz.getEnumConstants()) {
      if(!enumVal.isSkipInLookup()) {
        final long enumValue = enumVal.getLongValue();
        if (enumValue == value) {
          return Optional.of(enumVal);
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Given a Domino number-style enum, returns the enum constant for the provided
   * <code>Number</code>
   *
   * @param <T>   the number-backed enum type
   * @param clazz the number-backed enum class
   * @param value the value to convert
   * @return an {@link Optional} describing the corresponding enum value, or an
   *         empty one if none match
   */
  public static <T extends INumberEnum<?>> Optional<T> valueOf(final Class<T> clazz, final Number value) {
    for (final T enumVal : clazz.getEnumConstants()) {
      if(!enumVal.isSkipInLookup()) {
        final long enumValue = enumVal.getLongValue();
        if (enumValue == value.longValue()) {
          return Optional.of(enumVal);
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Given a Domino number-style enum, returns the enum constant for the provided
   * <code>short</code>
   *
   * @param <N>   subclass of {@link Number} to store enum values
   * @param <T>   the number-backed enum type
   * @param clazz the number-backed enum class
   * @param value the value to convert
   * @return an {@link Optional} describing the corresponding enum value, or an
   *         empty one if none match
   */
  public static <N extends Number, T extends Enum<T> & INumberEnum<N>> Optional<T> valueOf(final Class<T> clazz,
      final short value) {
    for (final T enumVal : clazz.getEnumConstants()) {
      if(!enumVal.isSkipInLookup()) {
        final long enumValue = enumVal.getLongValue();
        if (enumValue == value) {
          return Optional.of(enumVal);
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Given a Domino number-style bitfield enum, returns the matching enum
   * constants for the provided
   * <code>int</code>
   *
   * @param <N>   subclass of {@link Number} to store enum values
   * @param <T>   the number-backed enum type
   * @param clazz the number-backed enum class
   * @param value the value to convert
   * @return an {@link EnumSet} of matching enum values
   */
  public static <N extends Number, T extends Enum<T> & INumberEnum<N>> EnumSet<T> valuesOf(final Class<T> clazz, final int value) {
    final EnumSet<T> result = EnumSet.noneOf(clazz);
    final long val = value;
    for (final T enumVal : clazz.getEnumConstants()) {
      if(!enumVal.isSkipInLookup()) {
        final long enumValue = enumVal.getLongValue();
        if ((val & enumValue) == enumValue) {
          result.add(enumVal);
        }
      }
    }
    return result;
  }

  /**
   * Given a Domino number-style bitfield enum, returns the matching enum
   * constants for the provided
   * <code>long</code>
   *
   * @param <N>   subclass of {@link Number} to store enum values
   * @param <T>   the number-backed enum type
   * @param clazz the number-backed enum class
   * @param value the value to convert
   * @return an {@link EnumSet} of matching enum values
   */
  public static <N extends Number, T extends Enum<T> & INumberEnum<N>> EnumSet<T> valuesOf(final Class<T> clazz, final long value) {
    final EnumSet<T> result = EnumSet.noneOf(clazz);
    for (final T enumVal : clazz.getEnumConstants()) {
      if(!enumVal.isSkipInLookup()) {
        final long enumValue = enumVal.getLongValue();
        if ((value & enumValue) == enumValue) {
          result.add(enumVal);
        }
      }
    }
    return result;
  }

  /**
   * Given a Domino number-style bitfield enum, returns the matching enum
   * constants for the provided
   * <code>short</code>
   *
   * @param <N>   subclass of {@link Number} to store enum values
   * @param <T>   the number-backed enum type
   * @param clazz the number-backed enum class
   * @param value the value to convert
   * @return an {@link EnumSet} of matching enum values
   */
  public static <N extends Number, T extends Enum<T> & INumberEnum<N>> EnumSet<T> valuesOf(final Class<T> clazz,
      final short value) {
    final EnumSet<T> result = EnumSet.noneOf(clazz);
    final long val = value;
    for (final T enumVal : clazz.getEnumConstants()) {
      if(!enumVal.isSkipInLookup()) {
        final long enumValue = enumVal.getLongValue();
        if ((val & enumValue) == enumValue) {
          result.add(enumVal);
        }
      }
    }
    return result;
  }
  
  /**
   * Given a Domino string-style enum, returns the enum constant for the provided string.
   *
   * @param <T>   the string-backed enum type
   * @param clazz the string-backed enum class
   * @param value the value to convert
   * @return an {@link Optional} describing the corresponding enum value, or an
   *         empty one if none match
   */
  public static <T extends Enum<T> & IStringEnum> Optional<T> valueOfString(Class<T> clazz, String value) {
    if(value == null) {
      return Optional.empty();
    }
    for(T enumVal : clazz.getEnumConstants()) {
      if(Objects.equals(value, enumVal.getStringValue())) {
        return Optional.of(enumVal);
      }
    }
    return Optional.empty();
  }
}