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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utility class to coordinate service loading for JNX.
 * 
 * @author Jesse Gallagher
 * @since 1.0.12
 */
public enum JNXServiceFinder {
	;

	/**
	 * Finds services implementing the provided service class using the context classloader.
	 * 
	 * @param <T> the type of service to load
	 * @param serviceClass a {@link Class} object representing {@code <T>}
	 * @return a {@link Stream} of service implementations
	 */
	public static <T> Stream<T> findServices(Class<T> serviceClass) {
		Iterable<T> services = AccessController.doPrivileged((PrivilegedAction<Iterable<T>>)() -> ServiceLoader.load(serviceClass));
		return StreamSupport.stream(services.spliterator(), false);
	}
	
	/**
	 * Finds services implementing the provided service class using the provided classloader.
	 * 
	 * @param <T> the type of service to load
	 * @param serviceClass a {@link Class} object representing {@code <T>}
	 * @param cl the {@link ClassLoader} to use to load services
	 * @return a {@link Stream} of service implementations
	 */
	public static <T> Stream<T> findServices(Class<T> serviceClass, ClassLoader cl) {
		Iterable<T> services = AccessController.doPrivileged((PrivilegedAction<Iterable<T>>)() -> ServiceLoader.load(serviceClass, cl));
		return StreamSupport.stream(services.spliterator(), false);
	}
	
	/**
	 * Finds a single implementation of the provided service class using the provided classloader,
	 * throwing an exception if none can be found.
	 * 
	 * @param <T> the type of service to load
	 * @param serviceClass a {@link Class} object representing {@code <T>}
	 * @param cl the {@link ClassLoader} to use to load services
	 * @return an instance of the provided service class
	 * @throws IllegalStateException if no implementation can be found
	 */
	public static <T> T findRequiredService(Class<T> serviceClass, ClassLoader cl) {
		return JNXServiceFinder.findServices(serviceClass, cl)
			.findFirst()
			.orElseThrow(() -> new IllegalStateException(MessageFormat.format("No implementation for {0} found", serviceClass)));
	}
}
