package com.hcl.domino.jna.admin;

import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import com.hcl.domino.admin.ServerStatistics;
import com.hcl.domino.jna.data.JNADominoDateTime;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Memory;

/**
 * JNA implementation of {@link ServerStatistics}
 * 
 * @since 1.37.0
 */
public class JNAServerStatistics implements ServerStatistics {

  @Override
  public void updateStatistic(String facility, String statName, Collection<Flag> flags, int value) {
    Memory facilityMem = NotesStringUtils.toLMBCS(facility, true);
    Memory statNameMem = NotesStringUtils.toLMBCS(statName, true);
    short flagsVal = DominoEnumUtil.toBitField(Flag.class, flags);
    try(DisposableMemory valueMem = new DisposableMemory(4)) {
      valueMem.setInt(0, value);
      NotesCAPI.get().StatUpdate(facilityMem, statNameMem, flagsVal, NotesConstants.VT_LONG, valueMem);
    }
  }

  @Override
  public void updateStatistic(String facility, String statName, Collection<Flag> flags,
      String value) {
    Memory facilityMem = NotesStringUtils.toLMBCS(facility, true);
    Memory statNameMem = NotesStringUtils.toLMBCS(statName, true);
    short flagsVal = DominoEnumUtil.toBitField(Flag.class, flags);
    Memory valueMem = NotesStringUtils.toLMBCS(value, true);
    NotesCAPI.get().StatUpdate(facilityMem, statNameMem, flagsVal, NotesConstants.VT_TEXT, valueMem);
  }

  @Override
  public void updateStatistic(String facility, String statName, Collection<Flag> flags,
      double value) {
    Memory facilityMem = NotesStringUtils.toLMBCS(facility, true);
    Memory statNameMem = NotesStringUtils.toLMBCS(statName, true);
    short flagsVal = DominoEnumUtil.toBitField(Flag.class, flags);
    try(DisposableMemory valueMem = new DisposableMemory(8)) {
      valueMem.setDouble(0, value);
      NotesCAPI.get().StatUpdate(facilityMem, statNameMem, flagsVal, NotesConstants.VT_NUMBER, valueMem);
    }
  }

  @Override
  public void updateStatistic(String facility, String statName, Collection<Flag> flags,
      TemporalAccessor value) {
    Memory facilityMem = NotesStringUtils.toLMBCS(facility, true);
    Memory statNameMem = NotesStringUtils.toLMBCS(statName, true);
    short flagsVal = DominoEnumUtil.toBitField(Flag.class, flags);
    try(DisposableMemory valueMem = new DisposableMemory(8)) {
      JNADominoDateTime dt = JNADominoDateTime.from(value);
      int[] innards = dt.getInnards();
      valueMem.setInt(0, innards[0]);
      valueMem.setInt(4, innards[1]);
      NotesCAPI.get().StatUpdate(facilityMem, statNameMem, flagsVal, NotesConstants.VT_TIMEDATE, valueMem);
    }
  }

  @Override
  public void resetStatistic(String facility, String statName) {
    Memory facilityMem = NotesStringUtils.toLMBCS(facility, true);
    Memory statNameMem = NotesStringUtils.toLMBCS(statName, true);
    NotesCAPI.get().StatReset(facilityMem, statNameMem);
  }

  @Override
  public void deleteStatistic(String facility, String statName) {
    Memory facilityMem = NotesStringUtils.toLMBCS(facility, true);
    Memory statNameMem = NotesStringUtils.toLMBCS(statName, true);
    NotesCAPI.get().StatDelete(facilityMem, statNameMem);
  }
}
