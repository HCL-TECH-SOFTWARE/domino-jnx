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
package com.hcl.domino.test.richtext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.MemoryStructure;
import com.hcl.domino.richtext.structures.OpaqueTimeDate;

/**
 * Validates struct definition classes for sanity (matching member names, legal return types, etc.)
 * 
 * @since 1.0.24
 */
@SuppressWarnings("nls")
public class TestStructAnnotations {
  private static final Set<String> structNames = Collections.synchronizedSet(new TreeSet<>(String.CASE_INSENSITIVE_ORDER));
	
	public static class StructClassesProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return new Reflections(
					ConfigurationBuilder.build()
					.forPackages(DominoClient.class.getPackage().getName())
					.setExpandSuperTypes(false)
					.useParallelExecutor()
					.addScanners(new TypeAnnotationsScanner())
				).getTypesAnnotatedWith(StructureDefinition.class)
				.stream()
				.map(Arguments::of);
		}
	}
	
	@ParameterizedTest
	@ArgumentsSource(StructClassesProvider.class)
	public void testGetterSetterSanity(Class<?> c) {
		assertTrue(c.isInterface(), "StructureDefinition type must be an interface");
		
		// Overall structure name must be set
		StructureDefinition def = c.getAnnotation(StructureDefinition.class);
		String structName = def.name();
		assertFalse(isEmpty(structName), "Struct name cannot be empty");
		assertFalse(structNames.contains(structName), "Struct name must be unique");
		structNames.add(structName);
		
		// Must have members and all members must have a unique name
		StructureMember[] members = def.members();
		assertFalse(members == null || members.length == 0, "No StructureMembers");
		Objects.requireNonNull(members);

		Map<String, StructureMember> memberValues = new HashMap<>();
		for(int i = 0; i < members.length; i++) {
			String name = members[i].name();
			assertFalse(isEmpty(name), "Member " + i + " name is empty");
			assertFalse(memberValues.containsKey(name), "Member " + name + " is duplicated");
			assertTrue(members[i].length() >= 1, "Member length must be at least 1");
			if(members[i].length() > 1) {
				assertTrue(members[i].type().isArray(), "Multi-value members must have an array type");
			}
			assertFalse(members[i].bitfield() && !INumberEnum.class.isAssignableFrom(members[i].type()), "Only INumberEnum members can be bitfields");
			
			memberValues.put(name, members[i]);
		}
		
		// Getters and Setters must uniquely refer to member elements
		Arrays.stream(c.getDeclaredMethods())
			.filter(m -> !m.isSynthetic()) // Skip generics from parent
			.forEach(method -> {
				
			StructureGetter getter = method.getAnnotation(StructureGetter.class);
			if(getter != null) {
				String name = getter.value();
				assertFalse(isEmpty(name), method.getName() + ": Member name is empty");
				assertTrue(memberValues.containsKey(name), method.getName() + ": Refers to undefined member " + name);
				assertEquals(0, method.getParameterCount(), method.getName() + ": Invalid getter parameter count: " + method.getParameterCount());
				
				// Return type must match the expected value
				Type returnType = method.getGenericReturnType();
				assertTrue(isCompatibleType(returnType, memberValues.get(name), false), method.getName() + ": Return type " + returnType + " incompatible with " + memberValues.get(name).type().getName());
				assertFalse(method.isDefault(), method.getName() + ": Should not have both a @StructureGetter annotation and a default implementation");
			}
			
			StructureSetter setter = method.getAnnotation(StructureSetter.class);
			if(setter != null) {
				String name = setter.value();
				assertFalse(isEmpty(name), method.getName() + ": Member name is empty");
				assertTrue(memberValues.containsKey(name), method.getName() + ": Refers to undefined member " + name);
				assertEquals(1, method.getParameterCount(), method.getName() + ": Invalid getter parameter count: " + method.getParameterCount());

        // MemoryStructure members should not have setters
				assertFalse(
				  MemoryStructure.class.isAssignableFrom(memberValues.get(name).type()) && !OpaqueTimeDate.class.isAssignableFrom(memberValues.get(name).type()),
				  method.getName() + ": MemoryStructure members should not have setters"
				);
        
				// Parameter type must match the expected value
				Type paramType = method.getGenericParameterTypes()[0];
				assertTrue(isCompatibleType(paramType, memberValues.get(name), true), method.getName() + ": Parameter type " + paramType + " incompatible with " + memberValues.get(name).type().getName());
				
				
				// Return type must be the original class
				Class<?> returnType = method.getReturnType();
				assertTrue(c.equals(returnType), "#" + method.getName() + ": Invalid return type " + returnType.getName());
        assertFalse(method.isDefault(), method.getName() + ": Should not have both a @StructureSetter annotation and a default implementation");
        assertFalse(method.isAnnotationPresent(StructureGetter.class), method.getName() + ": Cannot be marked as both a @StructureGetter and @StructureSetter");
			}
			
			assertFalse(!Modifier.isStatic(method.getModifiers()) && !method.isDefault() && getter == null && setter == null,
				method + ": Has no implemenation or annotations"
			);
		});
	}
	
	private boolean isCompatibleType(Type methodType, StructureMember member, boolean isSetter) {
		Class<?> structType = member.type();
		boolean unsigned = member.unsigned();
		boolean bitfield = member.bitfield();
		
		if(isEnumCompatible(structType, methodType)) {
			return true;
		}
		// Check for Optional enum return types
		if(methodType instanceof ParameterizedType) {
		  ParameterizedType pType = (ParameterizedType)methodType;
		  if(Optional.class.isAssignableFrom((Class<?>)pType.getRawType())) {
		    Type paramType = pType.getActualTypeArguments()[0];
		    if(paramType instanceof Class && INumberEnum.class.isAssignableFrom((Class<?>)paramType)) {
		      return isEnumCompatible(structType, paramType);
		    }
		  }
		}
		// Check for primitives returned for enum types
		if(INumberEnum.class.isAssignableFrom(structType)) {
		  if(methodType instanceof Class && ((Class<?>)methodType).isPrimitive()) {
		    return true;
		  }
		}

		// Known special support for DominoDateTime
		if(DominoDateTime.class.equals(methodType) && OpaqueTimeDate.class.equals(member.type())) {
			return true;
		}
		
		// Check for arrays of structures or INumberEnums
		if(structType.isArray() && MemoryStructure.class.isAssignableFrom(structType.getComponentType()) && structType.equals(methodType)) {
		  return true;
		} else if(structType.isArray() && INumberEnum.class.isAssignableFrom(structType.getComponentType()) && structType.equals(methodType)) {
		  return true;
		}

		if(bitfield) {
			if(!Collection.class.isAssignableFrom(toClass(methodType))) {
				return false;
			}
			Type paramType = ((ParameterizedType)methodType).getActualTypeArguments()[0];
			return isEnumCompatible(structType, paramType);
		} else if(MemoryStructure.class.isAssignableFrom(structType) && structType.equals(methodType)) {
		  return true;
		} else if(byte.class.equals(structType)) {
			if(unsigned) {
				if(isSetter) {
					return isAtLeast(short.class, methodType);
				}
				return short.class.equals(methodType) || Short.class.equals(methodType);
			} else {
				return byte.class.equals(methodType) || Byte.class.equals(methodType);
			}
		} else if(byte[].class.equals(structType)) {
		  if(unsigned) {
        if(isSetter) {
          return isAtLeast(short[].class, methodType);
        }
        return short[].class.equals(methodType) || Short[].class.equals(methodType);
      } else {
        return byte[].class.equals(methodType) || Byte[].class.equals(methodType);
      }
		} else if(short.class.equals(structType)) {
			if(unsigned) {
				if(isSetter) {
					return isAtLeast(int.class, methodType);
				}
				return int.class.equals(methodType) || Integer.class.equals(methodType);
			} else {
				return short.class.equals(methodType) || Short.class.equals(methodType);
			}
		} else if(short[].class.equals(structType)) {
		  if(unsigned) {
        if(isSetter) {
          return isAtLeast(int[].class, methodType);
        }
        return int[].class.equals(methodType) || Integer[].class.equals(methodType);
      } else {
        return short[].class.equals(methodType) || Short[].class.equals(methodType);
      }
		} else if(int.class.equals(structType)) {
			if(unsigned) {
				return long.class.equals(methodType) || Long.class.equals(methodType);
			} else {
				return int.class.equals(methodType) || Integer.class.equals(methodType);
			}
		} else if(int[].class.equals(structType)) {
		  if(unsigned) {
		    if(isSetter) {
          return isAtLeast(long[].class, methodType);
        }
        return long[].class.equals(methodType) || Long[].class.equals(methodType);
      } else {
        return int[].class.equals(methodType) || Integer[].class.equals(methodType);
      }
		} else if(long.class.equals(structType)) {
			return long.class.equals(methodType) || Long.class.equals(methodType);
		} else if(long[].class.equals(structType)) {
		  return long[].class.equals(methodType) || Long[].class.equals(methodType);
		} else if(double.class.equals(structType)) {
			return double.class.equals(methodType) || Double.class.equals(methodType);
		} else if(double[].class.equals(structType)) {
		  return double[].class.equals(methodType) || Double[].class.equals(methodType);
		}
		return false;
	}
	
	private boolean isAtLeast(Class<?> representationType, Type setterType) {
		if(short.class.equals(representationType)) {
			return short.class.equals(setterType) || Short.class.equals(setterType)
				|| int.class.equals(setterType) || Integer.class.equals(setterType)
				|| long.class.equals(setterType) || Long.class.equals(setterType);
		} else if(short[].class.equals(representationType)) {
		  return short[].class.equals(setterType) || Short[].class.equals(setterType)
        || int[].class.equals(setterType) || Integer[].class.equals(setterType)
        || long[].class.equals(setterType) || Long[].class.equals(setterType);
		} else if(int.class.equals(representationType)) {
			return int.class.equals(setterType) || Integer.class.equals(setterType)
				|| long.class.equals(setterType) || Long.class.equals(setterType);
		} else if(int[].class.equals(representationType)) {
		  return int[].class.equals(setterType) || Integer[].class.equals(setterType)
        || long[].class.equals(setterType) || Long[].class.equals(setterType);
		} else if(long.class.equals(representationType)) {
		  return long.class.equals(setterType) || Long.class.equals(setterType);
		} else if(long[].class.equals(representationType)) {
		  return long[].class.equals(setterType) || Long[].class.equals(setterType);
		}
		return false;
	}
	
	private boolean isEnumCompatible(Class<?> structType, Type paramType) {
		Class<?> paramClass = toClass(paramType);
		if(!(INumberEnum.class.isAssignableFrom(paramClass) || INumberEnum.class.isAssignableFrom(structType))) {
		  // Then we're not working with an enum at all
		  return false;
		}
    if(structType.equals(paramClass)) {
      return true;
    }
		if(INumberEnum.class.isAssignableFrom(paramClass)) {
			Class<?> numType = Arrays.stream(paramClass.getGenericInterfaces())
				.filter(i -> INumberEnum.class.equals(toClass(i)))
				.map(i -> toClass(((ParameterizedType)i).getActualTypeArguments()[0]))
				.findFirst()
				.get();
			if(Byte.class.equals(numType)) {
				return byte.class.equals(structType);
			} else if(Short.class.equals(numType)) {
				return short.class.equals(structType);
			} else if(Integer.class.equals(numType)) {
				return int.class.equals(structType);
			} else if(Long.class.equals(numType)) {
				return long.class.equals(structType);
			}
		}
		return false;
	}
	
	private Class<?> toClass(final Type type) {
		if(type instanceof Class) {
			return (Class<?>)type;
		} else if(type instanceof AnnotatedType) {
			return toClass(((AnnotatedType)type).getType());
		} else if(type instanceof ParameterizedType) {
			return (Class<?>)((ParameterizedType) type).getRawType();
		} else {
			throw new IllegalArgumentException("Cannot find class for " + type + ", " + type.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	private boolean isEmpty(String val) {
		return val == null || val.isEmpty();
	}
}
