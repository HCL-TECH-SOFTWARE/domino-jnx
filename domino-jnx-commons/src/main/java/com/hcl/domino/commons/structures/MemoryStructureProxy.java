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
package com.hcl.domino.commons.structures;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.EnumSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import com.hcl.domino.commons.NotYetImplementedException;
import com.hcl.domino.commons.data.DefaultDominoDateTime;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.structures.MemoryStructure;
import com.hcl.domino.richtext.structures.OpaqueTimeDate;

/**
 * {@link InvocationHandler} for {@link MemoryStructure} sub-interfaces.
 * 
 * <p>
 * This implementation looks for {@link StructureGetter} annotations on called
 * methods and, when found, handles invocations by reading values from the
 * record's data buffer, converting as appropriate.
 * </p>
 * <p>
 * This mechanism handles getters for signed and unsigned integer number types,
 * as well as scalar {@link INumberEnum} types.
 * </p>
 *
 * @author Jesse Gallagher
 * @since 1.0.2
 */
public class MemoryStructureProxy implements InvocationHandler {
  private static final boolean JAVA_8;
  static {
    final String javaVersion = AccessController
        .doPrivileged((PrivilegedAction<String>) () -> System.getProperty("java.version", "")); //$NON-NLS-1$ //$NON-NLS-2$
    JAVA_8 = javaVersion.startsWith("1.8.0"); //$NON-NLS-1$
  }

  /**
   * Generates a reading {@link BiFunction} that reads the provided number or enum
   * type from
   * a byte buffer, taking into account whether the number type is unsigned in C.
   * 
   * @param type     the number or enum value type to read
   * @param unsigned whether the number value is unsigned
   * @param length   the length of an array-type value
   * @return a {@link BiFunction} that can be applied to a {@link ByteBuffer} and
   *         offset
   */
  @SuppressWarnings({ "unchecked" })
  static BiFunction<ByteBuffer, Integer, Object> reader(final Class<?> type, final boolean unsigned, final int length) {
    if (byte.class.equals(type) || Byte.class.equals(type)) {
      if (unsigned) {
        return (buf, offset) -> (short) Byte.toUnsignedInt(buf.get(offset));
      } else {
        return ByteBuffer::get;
      }
    } else if (byte[].class.equals(type)) {
      if (unsigned) {
        return (buf, offset) -> {
          final short[] result = new short[length];
          for (int i = 0; i < length; i++) {
            result[i] = (short) Byte.toUnsignedInt(buf.get(offset + i));
          }
          return result;
        };
      } else {
        return (buf, offset) -> {
          final byte[] result = new byte[length];
          for (int i = 0; i < length; i++) {
            result[i] = buf.get(offset + i);
          }
          return result;
        };
      }
    } else if (short.class.equals(type) || Short.class.equals(type)) {
      if (unsigned) {
        return (buf, offset) -> Short.toUnsignedInt(buf.getShort(offset));
      } else {
        return ByteBuffer::getShort;
      }
    } else if (short[].class.equals(type)) {
      if (unsigned) {
        return (buf, offset) -> {
          final int[] result = new int[length];
          for (int i = 0; i < length; i++) {
            result[i] = Short.toUnsignedInt(buf.getShort(offset + i * 2));
          }
          return result;
        };
      } else {
        return (buf, offset) -> {
          final short[] result = new short[length];
          for (int i = 0; i < length; i++) {
            result[i] = buf.getShort(offset + i * 2);
          }
          return result;
        };
      }
    } else if (int.class.equals(type) || Integer.class.equals(type)) {
      if (unsigned) {
        return (buf, offset) -> Integer.toUnsignedLong(buf.getInt(offset));
      } else {
        return ByteBuffer::getInt;
      }
    } else if (int[].class.equals(type)) {
      if (unsigned) {
        return (buf, offset) -> {
          final long[] result = new long[length];
          for (int i = 0; i < length; i++) {
            result[i] = Integer.toUnsignedLong(buf.getShort(offset + i * 4));
          }
          return result;
        };
      } else {
        return (buf, offset) -> {
          final int[] result = new int[length];
          for (int i = 0; i < length; i++) {
            result[i] = buf.getInt(offset + i * 4);
          }
          return result;
        };
      }
    } else if (long.class.equals(type) || Long.class.equals(type)) {
      if (unsigned) {
        throw new NotYetImplementedException("Unsigned long values not yet implemented");
      } else {
        return ByteBuffer::getLong;
      }
    } else if (long[].class.equals(type)) {
      if (unsigned) {
        throw new NotYetImplementedException("Unsigned long values not yet implemented");
      } else {
        return (buf, offset) -> {
          final long[] result = new long[length];
          for (int i = 0; i < length; i++) {
            result[i] = buf.getLong(offset + i * 8);
          }
          return result;
        };
      }
    } else if (double.class.equals(type) || Double.class.equals(type)) {
      return ByteBuffer::getDouble;
    } else if (double[].class.equals(type)) {
      return (buf, offset) -> {
        final double[] result = new double[length];
        for (int i = 0; i < length; i++) {
          result[i] = buf.getDouble(offset + i * 8);
        }
        return result;
      };
    } else if (MemoryStructure.class.isAssignableFrom(type)) {
      return (buf, offset) -> {
        final ByteBuffer b = buf.slice().order(ByteOrder.nativeOrder());
        b.position(offset);
        b.limit(offset + MemoryStructureUtil.sizeOf(type));
        final ByteBuffer subBuffer = b.slice().order(ByteOrder.nativeOrder());
        return MemoryStructureUtil.forStructure((Class<? extends MemoryStructure>) type, () -> subBuffer);
      };
    } else if (type.isArray() && MemoryStructure.class.isAssignableFrom(type.getComponentType())) {
      return (buf, offset) -> {
        final ByteBuffer b = buf.slice().order(ByteOrder.nativeOrder());

        final int structSize = MemoryStructureUtil.sizeOf(type.getComponentType());
        final Object resultArray = Array.newInstance(type.getComponentType(), length);
        for (int i = 0; i < length; i++) {
          b.position(offset + i * structSize);
          b.limit(b.position() + structSize);
          final ByteBuffer subBuffer = b.slice().order(ByteOrder.nativeOrder());
          Array.set(resultArray, i,
              MemoryStructureUtil.forStructure((Class<? extends MemoryStructure>) type.getComponentType(), () -> subBuffer));
        }
        return resultArray;
      };
    } else {
      throw new IllegalArgumentException("Cannot handle struct member type: " + type.getName());
    }
  }

  /**
   * Generates a reading {@link TriConsumer} that reads the provided number or
   * enum type from
   * a byte buffer, taking into account whether the number type is unsigned in C.
   * 
   * @param type     the number or enum value type to read
   * @param unsigned whether the number value is unsigned
   * @param length   the length of the array-type member
   * @return a {@link BiFunction} that can be applied to a {@link ByteBuffer} and
   *         offset
   */
  static TriConsumer<ByteBuffer, Integer, Object> writer(final Class<?> type, final boolean unsigned,
      final int length) {
    if (byte.class.equals(type) || Byte.class.equals(type)) {
      return (buf, offset, newVal) -> buf.put(offset, ((Number) newVal).byteValue());
    } else if (byte[].class.equals(type)) {
      if (unsigned) {
        return (buf, offset, newVal) -> {
          final short[] val = (short[]) newVal;
          for (int i = 0; i < Math.min(length, val.length); i++) {
            buf.put(offset + i, (byte) val[i]);
          }
          for (int i = Math.min(length, val.length); i < val.length; i++) {
            buf.put(offset + i, (byte) 0);
          }
        };
      } else {
        return (buf, offset, newVal) -> {
          final byte[] val = (byte[]) newVal;
          for (int i = 0; i < Math.min(length, val.length); i++) {
            buf.put(offset + i, val[i]);
          }
          for (int i = Math.min(length, val.length); i < val.length; i++) {
            buf.put(offset + i, (byte) 0);
          }
        };
      }
    } else if (short.class.equals(type) || Short.class.equals(type)) {
      return (buf, offset, newVal) -> buf.putShort(offset, ((Number) newVal).shortValue());
    } else if (short[].class.equals(type)) {
      if (unsigned) {
        return (buf, offset, newVal) -> {
          final int[] val = (int[]) newVal;
          for (int i = 0; i < Math.min(length, val.length); i++) {
            buf.putShort(offset + i * 2, (short) val[i]);
          }
          for (int i = Math.min(length, val.length); i < val.length; i++) {
            buf.putShort(offset + i * 2, (short) 0);
          }
        };
      } else {
        return (buf, offset, newVal) -> {
          final short[] val = (short[]) newVal;
          for (int i = 0; i < Math.min(length, val.length); i++) {
            buf.putShort(offset + i * 2, val[i]);
          }
          for (int i = Math.min(length, val.length); i < val.length; i++) {
            buf.putShort(offset + i * 2, (short) 0);
          }
        };
      }
    } else if (int.class.equals(type) || Integer.class.equals(type)) {
      return (buf, offset, newVal) -> buf.putInt(offset, ((Number) newVal).intValue());
    } else if (int[].class.equals(type)) {
      if (unsigned) {
        return (buf, offset, newVal) -> {
          final long[] val = (long[]) newVal;
          for (int i = 0; i < Math.min(length, val.length); i++) {
            buf.putInt(offset + i * 4, (int) val[i]);
          }
          for (int i = Math.min(length, val.length); i < val.length; i++) {
            buf.putInt(offset + i * 4, 0);
          }
        };
      } else {
        return (buf, offset, newVal) -> {
          final int[] val = (int[]) newVal;
          for (int i = 0; i < Math.min(length, val.length); i++) {
            buf.putInt(offset + i * 4, val[i]);
          }
          for (int i = Math.min(length, val.length); i < val.length; i++) {
            buf.putInt(offset + i * 4, 0);
          }
        };
      }
    } else if (long.class.equals(type) || Long.class.equals(type)) {
      return (buf, offset, newVal) -> buf.putInt(offset, ((Number) newVal).intValue());
    } else if (long[].class.equals(type)) {
      if (unsigned) {
        throw new NotYetImplementedException("Unsigned long values not yet implemented");
      } else {
        return (buf, offset, newVal) -> {
          final long[] val = (long[]) newVal;
          for (int i = 0; i < Math.min(length, val.length); i++) {
            buf.putLong(offset + i * 8, val[i]);
          }
          for (int i = Math.min(length, val.length); i < val.length; i++) {
            buf.putLong(offset + i * 8, 0);
          }
        };
      }
    } else if (double.class.equals(type) || Double.class.equals(type)) {
      return (buf, offset, newVal) -> buf.putDouble(offset, ((Number) newVal).doubleValue());
    } else if (double[].class.equals(type)) {
      return (buf, offset, newVal) -> {
        final double[] val = (double[]) newVal;
        for (int i = 0; i < Math.min(length, val.length); i++) {
          buf.putDouble(offset + i * 8, val[i]);
        }
        for (int i = Math.min(length, val.length); i < val.length; i++) {
          buf.putDouble(offset + i * 8, 0);
        }
      };
    } else if (MemoryStructure.class.isAssignableFrom(type)) {
      return (buf, offset, newVal) -> {
        final ByteBuffer b = buf.slice().order(ByteOrder.nativeOrder());
        b.position(offset);
        b.put(((MemoryStructure) newVal).getData());
      };
    } else if (type.isArray() && MemoryStructure.class.isAssignableFrom(type.getComponentType())) {
      return (buf, offset, newVal) -> {
        if (Array.getLength(newVal) != length) {
          throw new IllegalArgumentException(
              MessageFormat.format("Incompatible inner array length: expected {0}, received {1}", length, Array.getLength(newVal)));
        }
        final ByteBuffer b = buf.slice().order(ByteOrder.nativeOrder());
        b.position(offset);
        for (int i = 0; i < length; i++) {
          final MemoryStructure struct = (MemoryStructure) Array.get(newVal, i);
          b.put(struct.getData());
        }
      };
    } else {
      throw new IllegalArgumentException(MessageFormat.format("Cannot handle struct member type: {0}", type.getName()));
    }
  }

  private final MemoryStructure record;
  private final Class<? extends MemoryStructure> encapsulated;
  private final RecordType recordType;

  /**
   * Constructs a new {@code MemoryStructureProxy} instance for the provided memory layout.
   * 
   * @param record the {@link MemoryStructure} instance to provide memory access
   * @param encapsulated the interface to represent by this proxy
   * @param recordType the rich-text {@link RecordType} matched for this record; may be {@code null}
   */
  public MemoryStructureProxy(final MemoryStructure record, final Class<? extends MemoryStructure> encapsulated, RecordType recordType) {
    this.record = record;
    this.encapsulated = encapsulated;
    this.recordType = recordType;
  }

  @Override
  public Object invoke(final Object self, final Method thisMethod, final Object[] args) throws Throwable {
    final StructureMap struct = MemoryStructureUtil.getStructureMap(this.encapsulated);

    if (thisMethod.isAnnotationPresent(StructureGetter.class)) {
      final StructMember member = struct.getterMap.get(thisMethod);
      if (member != null) {
        final ByteBuffer buf = this.record.getData();
        if (member.type.isPrimitive() && INumberEnum.class.isAssignableFrom(thisMethod.getReturnType())) {
          // Handle the case where a member declared as a primitive is nonetheless
          // requested as an enum
          final Number val = (Number) member.reader.apply(buf, member.offset);
          @SuppressWarnings("unchecked")
          final Optional<? extends INumberEnum<?>> result = DominoEnumUtil.valueOf((Class<? extends INumberEnum<?>>) thisMethod.getReturnType(), val);
          return result.orElse(null);
        } else if (member.type.isPrimitive() && Collection.class.isAssignableFrom(thisMethod.getReturnType())) {
          // Same as above, but for enum collections. TestStructAnnotations in core
          // assures this will be a compatible type
          final Number val = (Number) member.reader.apply(buf, member.offset);
          @SuppressWarnings("rawtypes")
          final Class enumType = (Class) ((ParameterizedType) thisMethod.getGenericReturnType()).getActualTypeArguments()[0];
          @SuppressWarnings("unchecked")
          final Object result = DominoEnumUtil.valuesOf(enumType, val.longValue());
          return result;
        } else if(member.type.isPrimitive() && MemoryStructureUtil.isOptionalOf(thisMethod.getGenericReturnType(), INumberEnum.class)) {
          // Same as above, but for Optionals of single enums
          final Number val = (Number) member.reader.apply(buf, member.offset);
          @SuppressWarnings("rawtypes")
          final Class enumType = (Class) ((ParameterizedType) thisMethod.getGenericReturnType()).getActualTypeArguments()[0];
          @SuppressWarnings("unchecked")
          final Optional<? extends INumberEnum<?>> result = DominoEnumUtil.valueOf((Class<? extends INumberEnum<?>>) enumType, val);
          return result;
        } else if(member.type.isPrimitive() && Boolean.TYPE.equals(thisMethod.getReturnType())) {
          final Number val = (Number) member.reader.apply(buf, member.offset);
          return val.longValue() != 0;
        } else if (member.type.equals(OpaqueTimeDate.class) && DominoDateTime.class.equals(thisMethod.getReturnType())) {
          final OpaqueTimeDate dt = (OpaqueTimeDate) member.reader.apply(buf, member.offset);
          return new DefaultDominoDateTime(dt.getInnards());
        } else if(INumberEnum.class.isAssignableFrom(member.type)) {
          Number val = (Number)member.reader.apply(buf, member.offset);
          if(thisMethod.getReturnType().isPrimitive()) {
            // Return the number directly
            return val;
          } else if(member.bitfield) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Set<?> result = DominoEnumUtil.valuesOf((Class)member.type, val.longValue());
            return result;
          } else {
            @SuppressWarnings("unchecked")
            Optional<?> opt = DominoEnumUtil.valueOf((Class<? extends INumberEnum<?>>)member.type, val.longValue());
            if(MemoryStructureUtil.isOptionalOf(thisMethod.getGenericReturnType(), member.type)) {
              // Pass through the optional result
              return opt;
            } else {
              // Then unwrap the Optional coming from the reader
              return opt
                  .orElseThrow(() -> new NoSuchElementException(MessageFormat.format("Unable to find {0} value for {1}", member.type.getName(), val)));
            }
          }
        } else if(member.type.isArray() && INumberEnum.class.isAssignableFrom(member.type.getComponentType())) {
          if(!thisMethod.getReturnType().isArray()) {
            throw new IllegalStateException("Getters for array members must have array return types");
          }
          // Same as above, but for arrays
          Object arrayVal = member.reader.apply(buf, member.offset);
          if(thisMethod.getReturnType().getComponentType().isPrimitive()) {
            // Then return directly
            return arrayVal;
          } else if(member.bitfield) {
            throw new UnsupportedOperationException("Bitfield array members not supported");
          } else {
            Object result = Array.newInstance(member.type.getComponentType(), member.length);
            for(int i = 0; i < member.length; i++) {
              Number val = (Number)Array.get(arrayVal, i);
              @SuppressWarnings("unchecked")
              Optional<?> opt = DominoEnumUtil.valueOf((Class<? extends INumberEnum<?>>)member.type.getComponentType(), val);
              Array.set(result, i, opt.orElseThrow(() -> new NoSuchElementException(MessageFormat.format("Unable to find {0} value for {1}", member.type.getName(), val))));
            }
            return result;
          }
        } else {
          return member.reader.apply(buf, member.offset);
        }
      } else {
        throw new IllegalStateException(MessageFormat.format("Generated structure map failed to include method: {0}", thisMethod));
      }
    } else if (thisMethod.isAnnotationPresent(StructureSetter.class)) {
      final StructMember member = struct.setterMap.get(thisMethod);
      if (member != null) {
        final ByteBuffer buf = this.record.getData();
        Class<?> paramType = thisMethod.getParameterTypes()[0];
        if (member.type.isPrimitive() && INumberEnum.class.isAssignableFrom(paramType)) {
          // Handle the case where a member declared as a primitive is nonetheless set as
          // an enum
          final Object newVal = args[0];
          Number val = newVal == null ? 0 : ((INumberEnum<?>) newVal).getValue();
          val = MemoryStructureUtil.matchPrimitiveSizeForEnum(val, member);
          member.writer.accept(buf, member.offset, val);
        } else if(member.type.isPrimitive() && Boolean.TYPE.isAssignableFrom(paramType)) {
          final Object newVal = args[0];
          int val = newVal == null ? 0 : (Boolean)newVal ? 1 : 0;
          member.writer.accept(buf, member.offset, val);
        } else if (member.type.equals(OpaqueTimeDate.class) && DominoDateTime.class.equals(paramType)) {
          final int[] innards = ((DominoDateTime) args[0]).getAdapter(int[].class);
          member.writer.accept(buf, member.offset, innards);
        } else if(INumberEnum.class.isAssignableFrom(member.type) && paramType.isPrimitive()) {
          // Handle the case where a member declared as an enum is set as a primitive
          member.writer.accept(buf, member.offset, args[0]);
        } else if(INumberEnum.class.isAssignableFrom(member.type)) {
          final Object newVal = args[0];
          Number numVal;
          if(member.bitfield) {
            // Read the existing value and preserve possible undocumented flags
            Number existing = (Number)member.reader.apply(buf, member.offset);
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Number antimask = ~(DominoEnumUtil.toBitField((Class)member.type, EnumSet.allOf((Class)member.type)).longValue());
            long savedMask = existing.longValue() & antimask.longValue();
            
            // It's possible that the setter sets a single value or a collection
            if (Collection.class.isInstance(newVal)) {
              // Assume newVal is a Collection of enums
              @SuppressWarnings({ "rawtypes", "unchecked" })
              Number result = newVal == null ? 0 : DominoEnumUtil.toBitField((Class) member.type, (Collection) newVal);
              numVal = result.longValue() | savedMask;
            } else {
              // Assume it's a single enum
              @SuppressWarnings("rawtypes")
              Number result = newVal == null ? 0 : ((INumberEnum) newVal).getValue();
              numVal = result.longValue() | savedMask;
            }
          } else {
            @SuppressWarnings("rawtypes")
            Number result = newVal == null ? 0 : ((INumberEnum) newVal).getValue();
            numVal = result;
          }
          member.writer.accept(buf, member.offset, numVal);
        } else if(member.type.isArray() && INumberEnum.class.isAssignableFrom(member.type.getComponentType())) {
          // Same as above, but for arrays
          Object newVal = args[0];
          Class<?> numArrayType = MemoryStructureUtil.getNumberArrayType(member.type);
          Class<?> numType = numArrayType.getComponentType();
          Object numArray;
          if(newVal.getClass().getComponentType().isPrimitive()) {
            // If it's a primitive array, pass it through as-is
            numArray = newVal;
          } else {
            // Otherwise, unwrap the enum values to their raw values
            numArray = Array.newInstance(numType, member.length);
            int count = Math.min(Array.getLength(newVal), member.length);
            for(int i = 0; i < count; i++) {
              INumberEnum<?> val = (INumberEnum<?>)Array.get(newVal, i);
              if(byte.class.equals(numType)) {
                Array.setByte(numArray, i, val.getValue().byteValue());
              } else if(short.class.equals(numType)) {
                Array.setShort(numArray, i, val.getValue().shortValue());
              } else if(int.class.equals(numType)) {
                Array.setInt(numArray, i, val.getValue().intValue());
              } else {
                Array.setLong(numArray, i, val.getValue().longValue());
              }
            }
          }
          member.writer.accept(buf, member.offset, numArray);
        } else {
          member.writer.accept(buf, member.offset, args[0]);
        }

        if (void.class.equals(thisMethod.getReturnType()) || Void.class.equals(thisMethod.getReturnType())) {
          return null;
        }
        if (this.encapsulated.equals(thisMethod.getReturnType())) {
          return self;
        } else {
          throw new IllegalStateException(
              MessageFormat.format("Unhandled proxy return type: {0}", thisMethod.getGenericReturnType()));
        }
      }
    }
    final Method synthMember = struct.synthSetterMap.get(thisMethod);
    if (synthMember != null) {
      return this.invoke(self, synthMember, args);
    }
    
    // Special handling for rich-text records with applied RecordTypes
    if(this.recordType != null && "getType".equals(thisMethod.getName()) && thisMethod.getParameterCount() == 0) { //$NON-NLS-1$
      return EnumSet.of(this.recordType);
    }

    if (MemoryStructureUtil.isInLineage(thisMethod, this.encapsulated)) {
      if (thisMethod.isDefault()) {
        return this.invokeDefault(self, thisMethod, args);
      }
    }

    if ("toString".equals(thisMethod.getName()) && thisMethod.getParameterCount() == 0) { //$NON-NLS-1$
      final StringBuilder result = new StringBuilder();
      result.append(this.encapsulated.getName() + "@" + Integer.toHexString(System.identityHashCode(self))); //$NON-NLS-1$
      return result.toString();
    }

    // Special handling for data-getter methods
    if ("getData".equals(thisMethod.getName()) && thisMethod.getParameterCount() == 0) { //$NON-NLS-1$
      return this.record.getData().duplicate().order(ByteOrder.nativeOrder());
    }

    try {
      return thisMethod.invoke(this.record, args);
    } catch (final IllegalArgumentException e) {
      throw new IllegalArgumentException(MessageFormat.format("Unable to invoke method on object of type {0}: {1}",
          this.record.getClass().getName(), thisMethod.toString()), e);
    }
  }

  /**
   * Invokes a default method from an interface, accounting for access-control
   * differences between Java 8 and future versions.
   * 
   * @param self       the invocation context object
   * @param thisMethod the default interface method to invoke
   * @return the result of the method invocation
   * @throws Throwable if there is an exception invoking the method
   */
  private Object invokeDefault(final Object self, final Method thisMethod, final Object[] args) throws Throwable {
    if (MemoryStructureProxy.JAVA_8) {
      final Constructor<Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class);
      constructor.setAccessible(true);
      return constructor.newInstance(this.encapsulated)
          .in(this.encapsulated)
          .unreflectSpecial(thisMethod, this.encapsulated)
          .bindTo(self)
          .invokeWithArguments(args);
    } else {
      return MethodHandles.lookup()
          .findSpecial(
              this.encapsulated,
              thisMethod.getName(),
              MethodType.methodType(
                  thisMethod.getReturnType(),
                  thisMethod.getParameterTypes()
              ),
              this.encapsulated)
          .bindTo(self)
          .invokeWithArguments(args);
    }
  }

}
