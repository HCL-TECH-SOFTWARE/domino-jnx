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
package com.hcl.domino.misc;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
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
  
  private static final Map<Class<?>, Collection<?>> SERVICE_CACHE = new ConcurrentHashMap<>();
  private static final Map<Class<?>, Object> REQUIRED_SERVICE_CACHE = new ConcurrentHashMap<>();

  /**
   * Finds a single implementation of the provided service class using the
   * provided classloader, throwing an exception if none can be found.
   * 
   * <p>The {@link ClassLoader} parameter is intended for signaling the
   * expected loader for a service and is not intended for multi-classloader
   * environments. This method will store retrieved instances in an internal
   * cache and will not look them up again, regardless of {@code cl}
   * parameter.</p>
   * 
   * <p>Service implementations looked up by this method are expected to
   * be stateless.</p>
   *
   * @param <T>          the type of service to load
   * @param serviceClass a {@link Class} object representing {@code <T>}
   * @param cl           the {@link ClassLoader} to use to load services
   * @return an instance of the provided service class
   * @throws NoSuchElementException if no implementation can be found
   */
  @SuppressWarnings("unchecked")
  public static <T> T findRequiredService(final Class<T> serviceClass, final ClassLoader cl) {
    return (T)REQUIRED_SERVICE_CACHE.computeIfAbsent(serviceClass, c -> {
      final Iterable<?> services = AccessController
          .doPrivileged((PrivilegedAction<Iterable<?>>) () -> ServiceLoader.load(c, cl));
      return services.iterator().next();
    });
  }

  /**
   * Finds services implementing the provided service class using the context
   * classloader.
   * 
   * <p>Service implementations looked up by this method are expected to
   * be stateless.</p>
   *
   * @param <T>          the type of service to load
   * @param serviceClass a {@link Class} object representing {@code <T>}
   * @return a {@link Stream} of service implementations
   */
  @SuppressWarnings("unchecked")
  public static <T> Stream<T> findServices(final Class<T> serviceClass) {
    return (Stream<T>)SERVICE_CACHE.computeIfAbsent(serviceClass, c -> {
      final Iterable<T> services = AccessController
          .doPrivileged((PrivilegedAction<Iterable<T>>) () -> ServiceLoader.load(serviceClass));
      return StreamSupport.stream(services.spliterator(), false).collect(Collectors.toList());
    }).stream();
  }
}
