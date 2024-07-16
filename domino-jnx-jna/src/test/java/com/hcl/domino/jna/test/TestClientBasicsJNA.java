package com.hcl.domino.jna.test;

import java.util.concurrent.atomic.AtomicReference;
import com.hcl.domino.exception.DominoInitException;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestClientBasicsJNA extends AbstractJNARuntimeTest {

  @Test
  public void testMissingThreadInit() throws Exception {
    withTempDb((db) -> {
      Object lock = new Object();

      AtomicReference<Exception> ex = new AtomicReference<>();

      synchronized (lock) {
        // try to access C API in a non-initialized thread
        Thread t = new Thread(() -> {
          synchronized (lock) {
            try {
              DisposableMemory mem = new DisposableMemory(20);

              NotesCAPI.get().Cstrlen(mem);
            } catch (Exception e) {
              ex.set(e);
            } finally {
              lock.notify();
            }

          }
        });
        t.start();

        lock.wait();
      }

      Assertions.assertNotNull(ex.get());
      Assertions.assertInstanceOf(DominoInitException.class, ex.get());
    });
  }
}
