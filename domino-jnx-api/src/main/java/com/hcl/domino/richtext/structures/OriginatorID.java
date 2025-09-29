package com.hcl.domino.richtext.structures;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.util.JNXStringUtil;

/**
 * @since 1.48.0
 */
@StructureDefinition(
  name = "ORIGINATORID",
  members = {
    @StructureMember(name = "File", type = OpaqueTimeDate.class),
    @StructureMember(name = "Note", type = OpaqueTimeDate.class),
    @StructureMember(name = "Sequence", type = int.class),
    @StructureMember(name = "SequenceTime", type = OpaqueTimeDate.class),
  }
)
public interface OriginatorID extends MemoryStructure {
  @StructureGetter("File")
  OpaqueTimeDate getFile();
  
  @StructureGetter("Note")
  OpaqueTimeDate getNote();
  
  @StructureGetter("Sequence")
  int getSequence();
  
  @StructureSetter("Sequence")
  OriginatorID setSequence(int sequence);
  
  @StructureGetter("SequenceTime")
  OpaqueTimeDate getSequenceTime();
  
  /**
   * Computes the hex UNID from the OID data
   *
   * @return UNID
   */
  default String getUNID() {
    final ByteBuffer data = this.getData();
    return JNXStringUtil.toUNID(data.getLong(), data.getLong());
  }
  
  default OriginatorID setUNID(String unid) {
    ByteBuffer data = this.getData().order(ByteOrder.LITTLE_ENDIAN);
    LongBuffer longBuffer = data.asLongBuffer();
    
    String firstPart = unid.substring(0, 16);
    long firstPartAsLong = new BigInteger(firstPart, 16).longValue();
    longBuffer.put(0, firstPartAsLong);
    
    String secondPart = unid.substring(16);
    long secondPartAsLong = new BigInteger(secondPart, 16).longValue();
    longBuffer.put(1, secondPartAsLong);

    return this;
  }
}
