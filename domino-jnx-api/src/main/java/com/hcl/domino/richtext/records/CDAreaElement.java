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
package com.hcl.domino.richtext.records;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.CDPoint;
import com.hcl.domino.richtext.structures.CDRect;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * Rich text record of type CDAREAELEMENT
 */
@StructureDefinition(name = "CDAREAELEMENT", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Flags", type = int.class),
    @StructureMember(name = "Shape", type = CDAreaElement.Shape.class),
    @StructureMember(name = "TabIndex", type = short.class, unsigned = true),
    @StructureMember(name = "AccessKey", type = short.class, unsigned = true),
    @StructureMember(name = "Reserved", type = byte[].class, length = 16)
})
public interface CDAreaElement extends RichTextRecord<WSIG> {

  enum Shape implements INumberEnum<Short> {
    RECTANGLE((short)RichTextConstants.AREA_SHAPE_RECT),
    CIRCLE((short)RichTextConstants.AREA_SHAPE_CIRCLE),
    POLYGON((short)RichTextConstants.AREA_SHAPE_POLYGON),
    DEFAULT((short)RichTextConstants.AREA_SHAPE_DEFAULT);
    
    private final short value;
    
    private Shape(short value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return value;
    }

    @Override
    public Short getValue() {
      return value;
    }
  }
  
  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("Flags")
  int getFlags();
  
  @StructureSetter("Flags")
  CDAreaElement setFlags(int flags);
  
  @StructureGetter("Shape")
  Shape getShape();
  
  @StructureSetter("Shape")
  CDAreaElement setShape(Shape shape);
  
  @StructureGetter("Shape")
  short getShapeRaw();
  
  @StructureSetter("Shape")
  CDAreaElement setShapeRaw(short shape);
  
  @StructureGetter("TabIndex")
  int getTabIndex();
  
  @StructureSetter("TabIndex")
  CDAreaElement setTabIndex(int tabIndex);
  
  @StructureGetter("AccessKey")
  int getAccessKey();
  
  @StructureSetter("AccessKey")
  CDAreaElement setAccessKey(int accessKey);
  
  default  Optional<CDRect> getRectangle() {
    if (getShape() == Shape.RECTANGLE) {
      final MemoryStructureWrapperService wrapper = MemoryStructureWrapperService.get();
      
      return Optional.of(wrapper.wrapStructure(CDRect.class, this.getVariableData()));
    }
    
    return Optional.empty();
  }
  
  default  Optional<CDRect> getCircle() {
    if (getShape() == Shape.CIRCLE) {
      final MemoryStructureWrapperService wrapper = MemoryStructureWrapperService.get();
      
      return Optional.of(wrapper.wrapStructure(CDRect.class, this.getVariableData()));
    }
    
    return Optional.empty();
  }
  
  default  Optional<List<CDPoint>> getPolygon() {
    if (getShape() == Shape.POLYGON) {
      final MemoryStructureWrapperService wrapper = MemoryStructureWrapperService.get();
      
      ByteBuffer buf = this.getVariableData();
      //first get number of points
      short numOfPoints = buf.getShort();
      final List<CDPoint> result = new ArrayList<>(numOfPoints);
      for (int i=0; i<numOfPoints; i++) {
        int structLen = wrapper.sizeOf(CDPoint.class);
        final ByteBuffer structBuf = buf.slice().order(ByteOrder.nativeOrder());
        structBuf.limit(structLen);
        result.add(wrapper.wrapStructure(CDPoint.class, structBuf));

        buf.position(buf.position()+structLen);
        buf = buf.slice().order(ByteOrder.nativeOrder());
      }
      
      return Optional.of(result);
    }
    
    return Optional.empty();
  }
}
