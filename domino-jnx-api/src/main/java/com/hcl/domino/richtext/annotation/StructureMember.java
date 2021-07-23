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
package com.hcl.domino.richtext.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.structures.MemoryStructure;

@Retention(RUNTIME)
public @interface StructureMember {
	/**
	 * @return the name of the member in the structure
	 */
	String name();
	/**
	 * Defines the Java type of the structure member. This may be one of:
	 * 
	 * <ul>
	 *   <li>A primitive number type</li>
	 *   <li>An {@code enum} that implements {@link INumberEnum}</li>
	 *   <li>A direct sub-interface of {@link MemoryStructure}</li>
	 * </ul>
	 * 
	 * <p>If the value is unsigned in C, then {@link #unsigned()} should be {@code true}
	 * and this type should be one primitive number size higher than the byte size of
	 * the storage type. For example, an unsigned {@code WORD} is represented by {@code int}.</p>
	 * 
	 * <p>If the value is a bit field in C, such as a "Flags" field, then {@link #bitfield}
	 * should be {@code true} and this type should be the type of a single flag, and not a
	 * collection type.</p>
	 * 
	 * @return the Java type of the member
	 */
	Class<?> type();
	/**
	 * @return whether the value of the structure member is an unsigned integer type
	 */
	boolean unsigned() default false;
	/**
	 * @return whether the storage represents a bitfield type, such as a Flags field
	 */
	boolean bitfield() default false;
	/**
	 * Specifies the length of the embedded structure array, when {@link #type()} is an
	 * array type.
	 * 
	 * @return the length of the structure array
	 */
	int length() default 1;
}
