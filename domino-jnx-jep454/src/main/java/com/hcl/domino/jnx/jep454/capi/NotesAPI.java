package com.hcl.domino.jnx.jep454.capi;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public class NotesAPI {
  
  public static final MethodHandle NotesInitExtended;
  public static final MethodHandle NotesTerm;
  
  public static final MethodHandle NotesInitThread;
  public static final MethodHandle NotesTermThread;
  
  public static final MethodHandle SECKFMGetUserName;
  
  static {
    Linker linker = Linker.nativeLinker();
    SymbolLookup notes = SymbolLookup.libraryLookup("notes", Arena.global());
    
    {
      MemorySegment addr = notes.findOrThrow("NotesInitExtended");
      FunctionDescriptor sig = FunctionDescriptor.of(ValueLayout.JAVA_SHORT, linker.canonicalLayouts().get("int"), ValueLayout.ADDRESS);
      NotesInitExtended = linker.downcallHandle(addr, sig);
    }
    {
      MemorySegment addr = notes.findOrThrow("NotesTerm");
      FunctionDescriptor sig = FunctionDescriptor.ofVoid();
      NotesTerm = linker.downcallHandle(addr, sig);
    }
    
    {
      MemorySegment addr = notes.findOrThrow("NotesInitThread");
      FunctionDescriptor sig = FunctionDescriptor.ofVoid();
      NotesInitThread = linker.downcallHandle(addr, sig);
    }
    {
      MemorySegment addr = notes.findOrThrow("NotesTermThread");
      FunctionDescriptor sig = FunctionDescriptor.ofVoid();
      NotesTermThread = linker.downcallHandle(addr, sig);
    }
    
    {
      MemorySegment addr = notes.findOrThrow("SECKFMGetUserName");
      FunctionDescriptor sig = FunctionDescriptor.of(ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS);
      SECKFMGetUserName = linker.downcallHandle(addr, sig);
    }
  }

}
