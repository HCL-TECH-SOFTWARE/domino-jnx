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
package com.hcl.domino.jnx.example.swt.bean;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.hcl.domino.DominoClient;
import com.hcl.domino.jnx.example.swt.App;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class DominoContextBean {
  @FunctionalInterface
  public static interface DominoClientCallable<T> {
    T apply(DominoClient client) throws Exception;
  }
  @FunctionalInterface
  public static interface DominoClientConsumer {
    void apply(DominoClient client) throws Exception;
  }

  @Produces
  public DominoClient getClient() {
    return App.client;
  }

  @Produces
  public ExecutorService getExecutorService() {
    return App.getExecutor();
  }
  
  public static <T> T exec(DominoClientCallable<T> task) {
    try {
      return App.getExecutor().submit(() -> {
        return task.apply(App.client);
      }).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }
  
  public static Future<?> submit(DominoClientConsumer task) {
    return App.getExecutor().submit(() -> {
      try {
        task.apply(App.client);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }
}
