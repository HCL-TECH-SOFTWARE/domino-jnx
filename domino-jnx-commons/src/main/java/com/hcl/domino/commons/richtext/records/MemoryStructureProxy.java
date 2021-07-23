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
package com.hcl.domino.commons.richtext.records;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.hcl.domino.commons.NotYetImplementedException;
import com.hcl.domino.commons.data.DefaultDominoDateTime;
import com.hcl.domino.commons.richtext.structures.GenericResizableMemoryStructure;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.structures.BSIG;
import com.hcl.domino.richtext.structures.CDSignature;
import com.hcl.domino.richtext.structures.LSIG;
import com.hcl.domino.richtext.structures.MemoryStructure;
import com.hcl.domino.richtext.structures.OpaqueTimeDate;
import com.hcl.domino.richtext.structures.ResizableMemoryStructure;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * {@link InvocationHandler} for {@link MemoryStructure} sub-interfaces.
 * <p>
 * This implementation looks for {@link StructureGetter} annotations on called
 * methods and, when found, handles
 * invocations by reading values from the record's data buffer, converting as
 * appropriate.
 * </p>
 * <p>
 * This mechanism handles getters for signed and unsigned integer number types,
 * as well as scalar
 * {@link INumberEnum} types.
 * </p>
 *
 * @author Jesse Gallagher
 * @since 1.0.2
 */
public class MemoryStructureProxy implements InvocationHandler {
  private static class StructMember {
    private final String name;
    private final int offset;
    private final Class<?> type;
    private final boolean unsigned;
    private final int length;
    private final BiFunction<ByteBuffer, Integer, Object> reader;
    private final TriConsumer<ByteBuffer, Integer, Object> writer;

    public StructMember(final String name, final int offset, final Class<?> clazz, final boolean unsigned, final boolean bitfield,
        final int length) {
      this.name = name;
      this.offset = offset;
      this.type = clazz;
      this.unsigned = unsigned;
      this.length = length;
      this.reader = MemoryStructureProxy.reader(clazz, unsigned, bitfield, length);
      this.writer = MemoryStructureProxy.writer(clazz, unsigned, bitfield, length);
    }

    @Override
    public String toString() {
      return MessageFormat.format("StructMember [name={0}, offset={1}, type={2}, unsigned={3}, length={4}]", this.name, this.offset, //$NON-NLS-1$
          this.type, this.unsigned, this.length);
    }
  }

  private static class StructureMap {
    final List<StructMember> members = new ArrayList<>();
    final Map<Method, StructMember> getterMap = new HashMap<>();
    final Map<Method, StructMember> setterMap = new HashMap<>();
    final Map<Method, Method> synthSetterMap = new HashMap<>();

    void add(final StructMember member, final List<Method> getters, final List<Method> setters,
        final Map<Method, Method> synthSetters) {
      this.members.add(member);
      getters.forEach(m -> this.getterMap.put(m, member));
      setters.forEach(m -> {
        this.setterMap.put(m, member);
      });
      this.synthSetterMap.putAll(synthSetters);
    }

    int size() {
      return this.members.stream()
          .mapToInt(m -> MemoryStructureProxy.sizeOf(m.type) * m.length)
          .sum();
    }
  }

  @FunctionalInterface
  private interface TriConsumer<A, B, C> {
    void accept(A a, B b, C c);
  }

  private static final Map<Class<? extends MemoryStructure>, StructureMap> structureMap = Collections
      .synchronizedMap(new HashMap<>());

  private static final boolean JAVA_8;
  static {
    final String javaVersion = AccessController
        .doPrivileged((PrivilegedAction<String>) () -> System.getProperty("java.version", "")); //$NON-NLS-1$ //$NON-NLS-2$
    JAVA_8 = javaVersion.startsWith("1.8.0"); //$NON-NLS-1$
  }

  /**
   * Generates a new proxy object backed by the provided {@link MemoryStructure}
   * implementation.
   * 
   * @param <I>       the {@link MemoryStructure} sub-interface to proxy
   * @param subtype   a class representing {@code I}
   * @param structure the implementation structure
   * @return a new proxy object
   */
  @SuppressWarnings("unchecked")
  public static final <I extends MemoryStructure> I forStructure(final Class<I> subtype, final MemoryStructure structure) {
    if (structure instanceof ResizableMemoryStructure) {
      return (I) java.lang.reflect.Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
          new Class<?>[] { subtype }, new MemoryStructureProxy(structure, subtype));
    } else {
      // Always wrap in a resizable structure to account for variable data
      return MemoryStructureProxy.forStructure(subtype,
          new GenericResizableMemoryStructure(structure.getData().slice().order(ByteOrder.nativeOrder()), subtype));
    }
  }

  /**
   * Generates a structure map for the provided {@link MemoryStructure} class,
   * reading in all
   * {@link StructMember}-annotated methods to determine their types, sizes, and
   * offsets.
   * 
   * @param clazz the structure class to analyze
   * @return a {@link Map} of getter methods to implementing structure members
   */
  private static StructureMap generateStructureMap(final Class<? extends MemoryStructure> clazz) {
    return AccessController.doPrivileged((PrivilegedAction<StructureMap>) () -> {
      final StructureMap result = new StructureMap();

      final StructureDefinition def = clazz.getAnnotation(StructureDefinition.class);
      if (def != null) {
        final List<Method> methods = Arrays.asList(clazz.getDeclaredMethods());

        int offset = 0;
        for (final StructureMember member : def.members()) {
          final List<Method> getters = methods.stream()
              .filter(m -> {
                final StructureGetter ann = m.getAnnotation(StructureGetter.class);
                if (ann == null) {
                  return false;
                }
                return ann.value().equals(member.name());
              })
              .collect(Collectors.toList());

          final List<Method> setters = methods.stream()
              .filter(m -> {
                final StructureSetter ann = m.getAnnotation(StructureSetter.class);
                if (ann == null) {
                  return false;
                }
                return ann.value().equals(member.name());
              })
              .filter(m -> !m.isSynthetic())
              .collect(Collectors.toList());
          final Map<Method, Method> synthSetters = new HashMap<>();
          setters.forEach(setter -> {
            final Method synthSetter = methods.stream()
                .filter(Method::isSynthetic)
                .filter(m -> m.getName().equals(setter.getName()))
                .filter(m -> m.getParameterCount() == setter.getParameterCount())
                .filter(m -> {
                  final Class<?>[] thisParams = m.getParameterTypes();
                  final Class<?>[] setParams = setter.getParameterTypes();
                  for (int i = 0; i < thisParams.length; i++) {
                    if (!thisParams[i].isAssignableFrom(setParams[i])) {
                      return false;
                    }
                  }
                  return true;
                })
                .findFirst()
                .orElse(null);
            if (synthSetter != null) {
              synthSetters.put(synthSetter, setter);
            }
          });

          final Class<?> type = member.type();
          final int size = MemoryStructureProxy.sizeOf(type);
          final StructMember mem = new StructMember(member.name(), offset, type, member.unsigned(), member.bitfield(),
              member.length());
          result.add(mem, getters, setters, synthSetters);

          offset += size;
        }
      }

      return result;
    });
  }

  /**
   * Retrieves the {@link Number} subclass for the provided {@link INumberEnum}
   * implementation
   * class.
   * 
   * @param type the {@link INumberEnum} class object
   * @return the {@link Number} contained by the enum
   */
  @SuppressWarnings("unchecked")
  private static Class<? extends Number> getNumberType(final Class<?> type) {
    // Guaranteed to have an interface like INumberEnum<Integer>
    final ParameterizedType inumtype = Arrays.stream(type.getGenericInterfaces())
        .filter(t -> t instanceof ParameterizedType)
        .map(ParameterizedType.class::cast)
        .filter(t -> INumberEnum.class.equals(t.getRawType()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Unable to find INumberEnum interface"));
    return (Class<? extends Number>) inumtype.getActualTypeArguments()[0];
  }

  @SuppressWarnings("unchecked")
  private static boolean isInLineage(final Method thisMethod, final Class<? extends MemoryStructure> encapsulated) {
    if (thisMethod.getDeclaringClass().equals(encapsulated)) {
      return true;
    } else {
      final Class<?> sup = encapsulated.getInterfaces()[0];
      if (sup != null && !sup.equals(MemoryStructure.class) && MemoryStructure.class.isAssignableFrom(sup)) {
        return MemoryStructureProxy.isInLineage(thisMethod, (Class<? extends MemoryStructure>) sup);
      }
      return false;
    }
  }

  /**
   * Generates a new proxy object backed by a newly-allocated memory
   * {@link ByteBuffer}.
   * 
   * @param <I>                the {@link MemoryStructure} sub-interface to proxy
   * @param subtype            a class representing {@code I}
   * @param variableDataLength the amount of additional space, in bytes, to
   *                           allocate after the structure
   * @return a new proxy object
   */
  public static final <I extends MemoryStructure> I newStructure(final Class<I> subtype, final int variableDataLength) {
    final StructureMap struct = MemoryStructureProxy.structureMap.computeIfAbsent(subtype,
        MemoryStructureProxy::generateStructureMap);
    final ByteBuffer buf = ByteBuffer.allocate(struct.size() + variableDataLength);
    // Special handling for CD records and resizable types
    if (RichTextRecord.class.isAssignableFrom(subtype)) {
      // The subtype is then known to implement RichTextRecord<...> with a given SIG
      // type
      @SuppressWarnings("unchecked")
      final Class<? extends CDSignature<?, ?, ?>> sigType = Arrays.stream(subtype.getGenericInterfaces())
          .filter(ParameterizedType.class::isInstance)
          .map(ParameterizedType.class::cast)
          .filter(t -> RichTextRecord.class.equals(t.getRawType()))
          .map(t -> (Class<? extends CDSignature<?, ?, ?>>) t.getActualTypeArguments()[0])
          .findFirst()
          .orElseThrow(() -> new IllegalStateException("Could not find CDSignature type for " + subtype.toString()));
      if (BSIG.class.isAssignableFrom(sigType)) {
        return MemoryStructureProxy.forStructure(subtype, new GenericBSIGRecord(buf, subtype));
      } else if (WSIG.class.isAssignableFrom(sigType)) {
        return MemoryStructureProxy.forStructure(subtype, new GenericWSIGRecord(buf, subtype));
      } else if (LSIG.class.isAssignableFrom(sigType)) {
        return MemoryStructureProxy.forStructure(subtype, new GenericLSIGRecord(buf, subtype));
      } else {
        // This can intentionally fall through, since it shouldn't happen
      }
    } else if (ResizableMemoryStructure.class.isAssignableFrom(subtype)) {
      return MemoryStructureProxy.forStructure(subtype,
          new GenericResizableMemoryStructure(buf.slice().order(ByteOrder.nativeOrder()), subtype));
    }
    return MemoryStructureProxy.forStructure(subtype, () -> buf.slice().order(ByteOrder.nativeOrder()));
  }

  /**
   * Generates a reading {@link BiFunction} that reads the provided number or enum
   * type from
   * a byte buffer, taking into account whether the number type is unsigned in C.
   * 
   * @param type     the number or enum value type to read
   * @param unsigned whether the number value is unsigned
   * @param bitfield whether the value should be considered a flag-style bitfield
   * @param length   the length of an array-type value
   * @return a {@link BiFunction} that can be applied to a {@link ByteBuffer} and
   *         offset
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static BiFunction<ByteBuffer, Integer, Object> reader(final Class<?> type, final boolean unsigned, final boolean bitfield,
      final int length) {
    if (INumberEnum.class.isAssignableFrom(type)) {
      final Class<?> numberClass = MemoryStructureProxy.getNumberType(type);

      if (bitfield) {
        return (buf, offset) -> {
          final Number val = (Number) MemoryStructureProxy.reader(numberClass, unsigned, false, length).apply(buf, offset);
          return DominoEnumUtil.valuesOf((Class) type, val.longValue());
        };
      } else {
        return (buf, offset) -> {
          final Number val = (Number) MemoryStructureProxy.reader(numberClass, unsigned, false, length).apply(buf, offset);
          final Optional<?> opt = DominoEnumUtil.valueOf((Class) type, val.longValue());
          if (!opt.isPresent()) {
            throw new NoSuchElementException(MessageFormat.format("Unable to find {0} value for {1}", type.getName(), val));
          }
          return opt.get();
        };
      }
    }

    if (byte.class.equals(type) || Byte.class.equals(type)) {
      if (unsigned) {
        return (buf, offset) -> (short) (buf.get(offset) & 0xFFFF);
      } else {
        return ByteBuffer::get;
      }
    } else if (byte[].class.equals(type)) {
      if (unsigned) {
        return (buf, offset) -> {
          final short[] result = new short[length];
          for (int i = 0; i < length; i++) {
            result[i] = (short) (buf.get(offset + i) & 0xFFFF);
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
        b.limit(offset + MemoryStructureProxy.sizeOf(type));
        final ByteBuffer subBuffer = b.slice().order(ByteOrder.nativeOrder());
        return MemoryStructureProxy.forStructure((Class<? extends MemoryStructure>) type, () -> subBuffer);
      };
    } else if (type.isArray() && MemoryStructure.class.isAssignableFrom(type.getComponentType())) {
      return (buf, offset) -> {
        final ByteBuffer b = buf.slice().order(ByteOrder.nativeOrder());

        final int structSize = MemoryStructureProxy.sizeOf(type.getComponentType());
        final Object resultArray = Array.newInstance(type.getComponentType(), length);
        for (int i = 0; i < length; i++) {
          b.position(offset + i * structSize);
          b.limit(b.position() + structSize);
          final ByteBuffer subBuffer = b.slice().order(ByteOrder.nativeOrder());
          Array.set(resultArray, i,
              MemoryStructureProxy.forStructure((Class<? extends MemoryStructure>) type.getComponentType(), () -> subBuffer));
        }
        return resultArray;
      };
    } else {
      throw new IllegalArgumentException("Cannot handle struct member type: " + type.getName());
    }
  }

  /**
   * Retrieves the expected size of the provided number or enum type in the
   * C-side structure.
   * 
   * @param type the number or {@link INumberEnum} type
   * @return the size in bytes of the type
   */
  public static int sizeOf(final Class<?> type) {
    if (type.isArray()) {
      return MemoryStructureProxy.sizeOf(type.getComponentType());
    }
    if (INumberEnum.class.isAssignableFrom(type)) {
      final Class<?> numberClass = MemoryStructureProxy.getNumberType(type);
      return MemoryStructureProxy.sizeOf(numberClass);
    }
    if (MemoryStructure.class.isAssignableFrom(type)) {
      @SuppressWarnings("unchecked")
      final StructureMap struct = MemoryStructureProxy.structureMap.computeIfAbsent((Class<? extends MemoryStructure>) type,
          MemoryStructureProxy::generateStructureMap);
      return struct.size();
    }

    if (byte.class.equals(type) || Byte.class.equals(type)) {
      return 1;
    } else if (short.class.equals(type) || Short.class.equals(type)) {
      return 2;
    } else if (int.class.equals(type) || Integer.class.equals(type)) {
      return 4;
    } else if (long.class.equals(type) || Long.class.equals(type)) {
      return 8;
    } else if (double.class.equals(type) || Double.class.equals(type)) {
      return 8;
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
   * @param bitfield whether the structure member is a flags-type bitfield
   * @param length   the length of the array-type member
   * @return a {@link BiFunction} that can be applied to a {@link ByteBuffer} and
   *         offset
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static TriConsumer<ByteBuffer, Integer, Object> writer(final Class<?> type, final boolean unsigned,
      final boolean bitfield, final int length) {
    if (INumberEnum.class.isAssignableFrom(type)) {
      final Class<?> numberClass = MemoryStructureProxy.getNumberType(type);

      if (bitfield) {
        return (buf, offset, newVal) -> {
          // It's possible that the setter sets a single value or a collection
          if (Collection.class.isInstance(newVal)) {
            // Assume newVal is a Collection of enums
            final Number numVal = newVal == null ? 0 : DominoEnumUtil.toBitField((Class) type, (Collection) newVal);
            MemoryStructureProxy.writer(numberClass, unsigned, false, length).accept(buf, offset, numVal);
          } else {
            // Assume it's a single enum
            final Number numVal = newVal == null ? 0 : ((INumberEnum) newVal).getValue();
            MemoryStructureProxy.writer(numberClass, unsigned, false, length).accept(buf, offset, numVal);
          }
        };
      } else {
        return (buf, offset, newVal) -> {
          final Number numVal = newVal == null ? 0 : ((INumberEnum) newVal).getValue();
          MemoryStructureProxy.writer(numberClass, unsigned, false, length).accept(buf, offset, numVal);
        };
      }
    }

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

  public MemoryStructureProxy(final MemoryStructure record, final Class<? extends MemoryStructure> encapsulated) {
    this.record = record;
    this.encapsulated = encapsulated;
  }

  @Override
  public Object invoke(final Object self, final Method thisMethod, final Object[] args) throws Throwable {
    final StructureMap struct = MemoryStructureProxy.structureMap.computeIfAbsent(this.encapsulated,
        MemoryStructureProxy::generateStructureMap);

    if (thisMethod.isAnnotationPresent(StructureGetter.class)) {
      final StructMember member = struct.getterMap.get(thisMethod);
      if (member != null) {
        final ByteBuffer buf = this.record.getData();
        if (member.type.isPrimitive() && INumberEnum.class.isAssignableFrom(thisMethod.getReturnType())) {
          // Handle the case where a member declared as a primitive is nonetheless
          // requested as an enum
          final Number val = (Number) member.reader.apply(buf, member.offset);
          @SuppressWarnings("unchecked")
          final Optional<? extends INumberEnum<?>> result = DominoEnumUtil
              .valueOf((Class<? extends INumberEnum<?>>) thisMethod.getReturnType(), val);
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
        } else if (member.type.equals(OpaqueTimeDate.class) && DominoDateTime.class.equals(thisMethod.getReturnType())) {
          final OpaqueTimeDate dt = (OpaqueTimeDate) member.reader.apply(buf, member.offset);
          return new DefaultDominoDateTime(dt.getInnards());
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
        if (member.type.isPrimitive() && INumberEnum.class.isAssignableFrom(thisMethod.getParameterTypes()[0])) {
          // Handle the case where a member declared as a primitive is nonetheless set as
          // an enum
          final Object newVal = args[0];
          final Number val = newVal == null ? 0 : ((INumberEnum<?>) newVal).getValue();
          member.writer.accept(buf, member.offset, val);
        } else if (member.type.equals(OpaqueTimeDate.class) && DominoDateTime.class.equals(thisMethod.getParameterTypes()[0])) {
          final int[] innards = ((DominoDateTime) args[0]).getAdapter(int[].class);
          member.writer.accept(buf, member.offset, innards);
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

    if (MemoryStructureProxy.isInLineage(thisMethod, this.encapsulated)) {
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
   * differences between Java 8 and future
   * versions.
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
                  void.class,
                  new Class[0]),
              this.encapsulated)
          .bindTo(self)
          .invokeWithArguments(args);
    }
  }

}
