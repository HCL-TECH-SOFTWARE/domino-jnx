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
package com.hcl.domino.richtext.records;

import java.util.Collection;
import java.util.Set;

import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.OpaqueTimeDate;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.38
 */
@StructureDefinition(
  name = "CDQUERYBYFIELD",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "dwFlags", type = CDQueryByField.Flag.class, bitfield = true),
    @StructureMember(name = "wDataType", type = ItemDataType.class),
    @StructureMember(name = "wOperator", type = CDQueryByField.Operator.class),
    @StructureMember(name = "Date1", type = OpaqueTimeDate.class),
    @StructureMember(name = "Date2", type = OpaqueTimeDate.class),
    @StructureMember(name = "Number1", type = double.class),
    @StructureMember(name = "Number2", type = double.class),
    @StructureMember(name = "wFieldNameLen", type = short.class, unsigned = true),
    @StructureMember(name = "wValueLen", type = short.class, unsigned = true)
  }
)
public interface CDQueryByField extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Integer> {
    /** Search on modified and created date */
    BYDATE(NotesConstants.QUERYBYFIELD_FLAG_BYDATE),
    /** Search by author */
    BYAUTHOR(NotesConstants.QUERYBYFIELD_FLAG_BYAUTHOR);

    private final int value;

    Flag(final int value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Integer getValue() {
      return this.value;
    }
  }
  enum Operator implements INumberEnum<Short> {
    /**   Greater than value  */
    GREATER(NotesConstants.QUERYBYFIELD_OP_GREATER),
    /**   Less than value  */
    LESS(NotesConstants.QUERYBYFIELD_OP_LESS),
    /**   Not equal to value  */
    NOTEQUAL(NotesConstants.QUERYBYFIELD_OP_NOTEQUAL),
    /**   Between Date1 and Date2  */
    BETWEEN(NotesConstants.QUERYBYFIELD_OP_BETWEEN),
    /**   Not between Date1 and Date2  */
    NOTWITHIN(NotesConstants.QUERYBYFIELD_OP_NOTWITHIN),
    /**   Equal to value  */
    EQUAL(NotesConstants.QUERYBYFIELD_OP_EQUAL),
    /**   Contains value  */
    CONTAINS(NotesConstants.QUERYBYFIELD_OP_CONTAINS),
    /**   In the last n days  */
    INTHELAST(NotesConstants.QUERYBYFIELD_OP_INTHELAST),
    /**   In the next n days  */
    INTHENEXT(NotesConstants.QUERYBYFIELD_OP_INTHENEXT),
    /**   Older than n days  */
    OLDERTHAN(NotesConstants.QUERYBYFIELD_OP_OLDERTHAN),
    /**   Due more than n days from now  */
    DUEIN(NotesConstants.QUERYBYFIELD_OP_DUEIN),
    /**   Does not contain  */
    DOESNOTCONTAIN(NotesConstants.QUERYBYFIELD_OP_DOESNOTCONTAIN);

    private final short value;

    Operator(final short value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Short getValue() {
      return this.value;
    }
  }

  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("dwFlags")
  Set<Flag> getFlags();
  
  @StructureSetter("dwFlags")
  CDQueryByField setFlags(Collection<Flag> flags);
  
  @StructureGetter("wDataType")
  ItemDataType getDataType();
  
  @StructureSetter("wDataType")
  CDQueryByField setDataType(ItemDataType type);
  
  @StructureGetter("wOperator")
  Operator getOperator();
  
  @StructureSetter("wOperator")
  CDQueryByField setOperator(Operator operator);
  
  @StructureGetter("Date1")
  DominoDateTime getDate1();
  
  @StructureSetter("Date1")
  CDQueryByField setDate1(DominoDateTime date2);

  @StructureGetter("Date2")
  DominoDateTime getDate2();
  
  @StructureSetter("Date2")
  CDQueryByField setDate2(DominoDateTime date2);
  
  @StructureGetter("Number1")
  double getNumber1();
  
  @StructureSetter("Number1")
  CDQueryByField setNumber1(double val);
  
  @StructureGetter("Number2")
  double getNumber2();
  
  @StructureSetter("Number2")
  CDQueryByField setNumber2(double val);
  
  @StructureGetter("wFieldNameLen")
  int getFieldNameLength();
  
  @StructureSetter("wFieldNameLen")
  CDQueryByField setFieldNameLength(int len);
  
  @StructureGetter("wValueLen")
  int getValueLength();
  
  @StructureSetter("wValueLen")
  CDQueryByField setValueLength(int len);
  
  default String getFieldName() {
    return StructureSupport.extractStringValue(
      this,
      0,
      getFieldNameLength()
    );
  }
  
  default CDQueryByField setFieldName(String fieldName) {
    return StructureSupport.writeStringValue(
      this,
      0,
      getFieldNameLength(),
      fieldName,
      this::setFieldNameLength
    );
  }
  
  default String getValue() {
    return StructureSupport.extractStringValue(
      this,
      getFieldNameLength(),
      getValueLength()
    );
  }
  
  default CDQueryByField setValue(String value) {
    return StructureSupport.writeStringValue(
      this,
      getFieldNameLength(),
      getValueLength(),
      value,
      this::setValueLength
    );
  }
}
