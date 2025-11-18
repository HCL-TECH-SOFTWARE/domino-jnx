package com.hcl.domino.jnx.jep454;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import com.hcl.domino.DominoProcess;
import com.hcl.domino.exception.DominoInitException;
import com.hcl.domino.jnx.jep454.capi.NotesAPI;

public class JEPDominoProcess implements DominoProcess {

  @Override
  public void initializeProcess(String[] initArgs) throws DominoInitException {
    try(Arena arena = Arena.ofConfined()) {
      // Build a native array
      // String lengths + nulls + final null byte
      
      MemorySegment[] strings = Arrays.stream(initArgs)
          .map(a -> a == null ? "" : a)
          .map(a -> arena.allocateFrom(a, StandardCharsets.US_ASCII))
          .toArray(MemorySegment[]::new);
      MemorySegment seg = arena.allocate(MemoryLayout.sequenceLayout(strings.length, ValueLayout.ADDRESS));
      for(int i = 0; i < strings.length; i++) {
        seg.setAtIndex(AddressLayout.ADDRESS, i, strings[i]);
      }
      
      short result = (short)NotesAPI.NotesInitExtended.invokeExact(initArgs.length, seg);
      if(result != 0) {
        throw new RuntimeException("Unexpected status 0x" + Integer.toHexString(result));
      }
    } catch (Throwable e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public void initializeProcess(String[] initArgs, boolean nativeProcessInit,
      boolean nativeThreadInit) throws DominoInitException {
    // TODO Auto-generated method stub
    initializeProcess(initArgs);
  }

  @Override
  public DominoThreadContext initializeThread() {
    try {
      NotesAPI.NotesInitThread.invoke();
      return this::terminateThread;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public DominoThreadContext initializeThread(boolean nativeInit, boolean nativeTerm) {
    // TODO Auto-generated method stub
    return initializeThread();
  }

  @Override
  public String switchToId(Path idPath, String password, boolean dontSetEnvVar) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  @Override
  public void terminateProcess() {
    try {
      NotesAPI.NotesTerm.invoke();
    } catch (Throwable e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public void terminateThread() {
    try {
      NotesAPI.NotesTermThread.invoke();
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void terminateThread(boolean nativeTerm) {
    // TODO Auto-generated method stub
    terminateThread();
  }

}
