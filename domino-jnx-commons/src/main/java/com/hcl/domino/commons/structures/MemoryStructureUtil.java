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

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.hcl.domino.commons.richtext.records.GenericBSIGRecord;
import com.hcl.domino.commons.richtext.records.GenericLSIGRecord;
import com.hcl.domino.commons.richtext.records.GenericWSIGRecord;
import com.hcl.domino.commons.richtext.structures.GenericResizableMemoryStructure;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.structures.BSIG;
import com.hcl.domino.richtext.structures.CDSignature;
import com.hcl.domino.richtext.structures.LSIG;
import com.hcl.domino.richtext.structures.MemoryStructure;
import com.hcl.domino.richtext.structures.ResizableMemoryStructure;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * Contains utility methods for working with {@link MemoryStructure} instances.
 * 
 * @author Jesse Gallagher
 * @since 1.0.34
 */
public enum MemoryStructureUtil {
  ;
  
  private static final Map<Class<? extends MemoryStructure>, StructureMap> structureMap = Collections
      .synchronizedMap(new HashMap<>());
  
  private static final Map<Class<?>, Integer> sizeMap = new ConcurrentHashMap<>();

  /**
   * Retrieves the expected size of the provided number or enum type in the
   * C-side structure.
   * 
   * @param type the number or {@link INumberEnum} type
   * @return the size in bytes of the type
   */
  public static int sizeOf(final Class<?> type) {
    if (type.isArray()) {
      return MemoryStructureUtil.sizeOf(type.getComponentType());
    }
    if (INumberEnum.class.isAssignableFrom(type)) {
      final Class<?> numberClass = MemoryStructureUtil.getNumberType(type);
      return MemoryStructureUtil.sizeOf(numberClass);
    }
    Integer cachedSize = sizeMap.get(type);
    if (cachedSize == null) {
      int size = 0;
      if (MemoryStructure.class.isAssignableFrom(type)) {
        @SuppressWarnings("unchecked")
        final StructureMap struct = getStructureMap((Class<? extends MemoryStructure>) type);
        size = struct.size();
      } else if (byte.class.equals(type) || Byte.class.equals(type)) {
        size = 1;
      } else if (short.class.equals(type) || Short.class.equals(type)) {
        size = 2;
      } else if (int.class.equals(type) || Integer.class.equals(type)) {
        size = 4;
      } else if (long.class.equals(type) || Long.class.equals(type)) {
        size = 8;
      } else if (double.class.equals(type) || Double.class.equals(type)) {
        size = 8;
      } else {
        throw new IllegalArgumentException("Cannot handle struct member type: " + type.getName());
      }
      sizeMap.put(type, size);
      return size;
    } else {
      return cachedSize;
    }
  }
  
  /**
   * Retrieves a structure map for the provided {@link MemoryStructure} class,
   * reading in all {@link StructMember}-annotated methods to determine their
   * types, sizes, and offsets.
   * 
   * @param <T> the type of structure to retrieve the map for
   * @param subtype the structure class to analyze
   * @return a {@link Map} of getter methods to implementing structure members
   * @since 1.0.34
   */
  public static synchronized <T extends MemoryStructure> StructureMap getStructureMap(Class<T> subtype) {
    if(!structureMap.containsKey(subtype)) {
      structureMap.put(subtype, generateStructureMap(subtype));
    }
    return structureMap.get(subtype);
  }

  /**
   * Generates a structure map for the provided {@link MemoryStructure} class,
   * reading in all {@link StructMember}-annotated methods to determine their
   * types, sizes, and offsets.
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
          final int size = sizeOf(type) * member.length();
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
   * implementation class.
   * 
   * @param type the {@link INumberEnum} class object
   * @return the {@link Number} contained by the enum
   */
  @SuppressWarnings("unchecked")
  public static Class<? extends Number> getNumberType(final Class<?> type) {
    // Guaranteed to have an interface like INumberEnum<Integer>
    final ParameterizedType inumtype = Arrays.stream(type.getGenericInterfaces())
        .filter(t -> t instanceof ParameterizedType)
        .map(ParameterizedType.class::cast)
        .filter(t -> INumberEnum.class.equals(t.getRawType()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Unable to find INumberEnum interface"));
    return (Class<? extends Number>) inumtype.getActualTypeArguments()[0];
  }

  /**
   * Retrieves numerical array class for the provided {@link INumberEnum}
   * array implementation class.
   * 
   * @param type the {@link INumberEnum} class object
   * @return the array class equivalent to the enum
   * @since 1.0.35
   */
  public static Class<?> getNumberArrayType(final Class<?> type) {
    Class<?> component = type.getComponentType();
    // Guaranteed to have an interface like INumberEnum<Integer>
    final ParameterizedType inumtype = Arrays.stream(component.getGenericInterfaces())
        .filter(t -> t instanceof ParameterizedType)
        .map(ParameterizedType.class::cast)
        .filter(t -> INumberEnum.class.equals(t.getRawType()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Unable to find INumberEnum interface"));
    Class<?> numType = (Class<?>)inumtype.getActualTypeArguments()[0];
    
    if(Byte.class.equals(numType)) {
      return byte[].class;
    } else if(Short.class.equals(numType)) {
      return short[].class;
    } else if(Integer.class.equals(numType)) {
      return int[].class;
    } else {
      return long[].class;
    }
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
      return (I) java.lang.reflect.Proxy.newProxyInstance(subtype.getClassLoader(),
          new Class<?>[] { subtype }, new MemoryStructureProxy(structure, subtype, null));
    } else {
      // Always wrap in a resizable structure to account for variable data
      return MemoryStructureUtil.forStructure(subtype,
          new GenericResizableMemoryStructure(structure.getData().slice().order(ByteOrder.nativeOrder()), subtype));
    }
  }
  
  /**
   * Generates a new rich-text proxy object backed by the provided {@link MemoryStructure}
   * implementation.
   * 
   * @param <I>        the {@link RichTextRecord} sub-interface to proxy
   * @param subtype    a class representing {@code I}
   * @param recordType the matched {@link RecordType} for the record
   * @param structure  the implementation structure
   * @return a new proxy object
   * @since 1.0.45
   */
  @SuppressWarnings("unchecked")
  public static final <I extends RichTextRecord<?>> I forRichTextStructure(Class<I> subtype, RecordType recordType, MemoryStructure structure) {
    return (I) java.lang.reflect.Proxy.newProxyInstance(subtype.getClassLoader(),
        new Class<?>[] { subtype }, new MemoryStructureProxy(structure, subtype, recordType));
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
    final StructureMap struct = getStructureMap(subtype);
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
        return forStructure(subtype, new GenericBSIGRecord(buf, subtype));
      } else if (WSIG.class.isAssignableFrom(sigType)) {
        return forStructure(subtype, new GenericWSIGRecord(buf, subtype));
      } else if (LSIG.class.isAssignableFrom(sigType)) {
        return forStructure(subtype, new GenericLSIGRecord(buf, subtype));
      } else {
        // This can intentionally fall through, since it shouldn't happen
      }
    } else if (ResizableMemoryStructure.class.isAssignableFrom(subtype)) {
      return forStructure(subtype,
          new GenericResizableMemoryStructure(buf.slice().order(ByteOrder.nativeOrder()), subtype));
    }
    return forStructure(subtype, () -> buf.slice().order(ByteOrder.nativeOrder()));
  }

  /**
   * Determines whether the provided method is defined in {@code encapsulation} or any of its parent interfaces.
   * 
   * <p>Parent interfaces are determined by checking only the first "extends" interface in the class.</p>
   * 
   * @param thisMethod the method to look up
   * @param encapsulated the class to query
   * @return {@code true} if the method is defined in the interface or a parent; {@code false} otherwise
   */
  @SuppressWarnings("unchecked")
  public static boolean isInLineage(final Method thisMethod, final Class<? extends MemoryStructure> encapsulated) {
    if (thisMethod.getDeclaringClass().equals(encapsulated)) {
      return true;
    } else {
      final Class<?> sup = encapsulated.getInterfaces()[0];
      if (sup != null && !sup.equals(MemoryStructure.class) && MemoryStructure.class.isAssignableFrom(sup)) {
        return MemoryStructureUtil.isInLineage(thisMethod, (Class<? extends MemoryStructure>) sup);
      }
      return false;
    }
  }
  
  /**
   * Determines whether the provided method return type is an {@link Optional} representing a class
   * either equal to or compatible with {@code desiredClass}.
   * 
   * @param returnType the method return type to check
   * @param desiredClass the target class to check for
   * @return {@code true} if the return type is a compatible {@code Optional};
   *         {@code false} otherwise
   */
  public static boolean isOptionalOf(Type returnType, Class<?> desiredClass) {
    if(!(returnType instanceof ParameterizedType)) {
      return false;
    }
    
    ParameterizedType t = (ParameterizedType)returnType;
    if(!Optional.class.isAssignableFrom((Class<?>)t.getRawType())) {
      return false;
    }
    Type paramType = t.getActualTypeArguments()[0];
    if(paramType instanceof Class && desiredClass.isAssignableFrom((Class<?>)paramType)) {
      // Then we're good
      return true;
    }
    
    return false;
  }
  
  /**
   * Inspects the incoming value and "expands" it if necessary for an equivalent positive value in
   * the target size. For example, if {@code newVal} is a {@code Byte} and {@code member} represents
   * a {@code short}, this will return an equivalent positive value to {@code newVal}'s signed
   * negative scope.
   * 
   * <p>Note: this is intended for use specifically for cases of writing enumerated values, which
   * are assumed to be positive.</p>
   * 
   * @param newVal the incoming value to convert
   * @param member the struct member the value is going to be written to
   * @return a potentially-resized version of {@code newVal} for storage
   * @since 1.1.2
   */
  public static Number matchPrimitiveSizeForEnum(Number newVal, StructMember member) {
    if(newVal instanceof Byte && Short.TYPE.equals(member.type)) {
      return Byte.toUnsignedInt((Byte)newVal);
    } else if(newVal instanceof Short && Integer.TYPE.equals(member.type)) {
      return Short.toUnsignedInt((Short)newVal);
    } else if(newVal instanceof Integer && Long.TYPE.equals(member.type)) {
      return Integer.toUnsignedLong((Integer)newVal);
    }
    return newVal;
  }
}
