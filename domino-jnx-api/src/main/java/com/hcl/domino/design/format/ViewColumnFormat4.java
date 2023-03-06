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
package com.hcl.domino.design.format;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.records.CurrencyFlag;
import com.hcl.domino.richtext.records.CurrencyType;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;
import com.hcl.domino.richtext.structures.NFMT;
import com.hcl.domino.richtext.structures.ResizableMemoryStructure;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
@StructureDefinition(
  name = "VIEW_COLUMN_FORMAT4",
  members = {
    @StructureMember(name = "Signature", type = short.class),
    @StructureMember(name = "NumberFormat", type = NFMT.class),
    @StructureMember(name = "NumSymPref", type = NumberPref.class),
    @StructureMember(name = "NumSymFlags", type = byte.class),
    @StructureMember(name = "DecimalSymLength", type = int.class, unsigned = true),
    @StructureMember(name = "MilliSepSymLength", type = int.class, unsigned = true),
    @StructureMember(name = "NegativeSymLength", type = int.class, unsigned = true),
    @StructureMember(name = "MilliGroupSize", type = short.class, unsigned = true),
    @StructureMember(name = "Unused1", type = int.class),
    @StructureMember(name = "Unused2", type = int.class),
    @StructureMember(name = "CurrencyPref", type = NumberPref.class),
    @StructureMember(name = "CurrencyType", type = CurrencyType.class),
    @StructureMember(name = "CurrencyFlags", type = CurrencyFlag.class, bitfield = true),
    @StructureMember(name = "CurrencySymLength", type = int.class, unsigned = true),
    @StructureMember(name = "ISOCountry", type = int.class, unsigned = true),
    @StructureMember(name = "NumberPreference", type = short.class),
    @StructureMember(name = "bUnused", type = byte.class),
    @StructureMember(name = "Unused3", type = int.class),
    @StructureMember(name = "Unused4", type = int.class),
    
    //followed by var data with strings for the custom decimal symbol, milli separator, negative symbol and currency symbol
  }
)
public interface ViewColumnFormat4 extends ResizableMemoryStructure {

  public static ViewColumnFormat4 newInstanceWithDefaults() {
    ViewColumnFormat4 fmt = MemoryStructureWrapperService.get().newStructure(ViewColumnFormat4.class, 0);

    //TODO add defaults
    
    return fmt;
  }
  
  @StructureGetter("CurrencyFlags")
  Set<CurrencyFlag> getCurrencyFlags();

  @StructureSetter("CurrencyFlags")
  ViewColumnFormat4 setCurrencyFlags(Collection<CurrencyFlag> flags);

  default ViewColumnFormat4 setCurrencyFlag(CurrencyFlag flag, boolean b) {
    Set<CurrencyFlag> oldFlags = getCurrencyFlags();
    if (b) {
      if (!oldFlags.contains(flag)) {
        Set<CurrencyFlag> newFlags = new HashSet<>(oldFlags);
        newFlags.add(flag);
        setCurrencyFlags(newFlags);
      }
    }
    else {
      if (oldFlags.contains(flag)) {
        Set<CurrencyFlag> newFlags = oldFlags
            .stream()
            .filter(currFlag -> !flag.equals(currFlag))
            .collect(Collectors.toSet());
        setCurrencyFlags(newFlags);
      }
    }
    return this;
  }
  
  @StructureGetter("CurrencyPref")
  Optional<NumberPref> getCurrencyPreference();
  
  /**
   * Retrieves the currency pref as a raw {@code byte}.
   * 
   * @return the currency pref as a {@code byte}
   * @since 1.24.0
   */
  @StructureGetter("CurrencyPref")
  byte getCurrencyPreferenceRaw();

  @StructureSetter("CurrencyPref")
  ViewColumnFormat4 setCurrencyPreference(NumberPref pref);

  /**
   * Sets the currency pref as a raw {@code byte}.
   * 
   * @param pref the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("CurrencyPref")
  ViewColumnFormat4 setCurrencyPreferenceRaw(byte pref);

  @StructureGetter("CurrencySymLength")
  long getCurrencySymbolLength();

  @StructureSetter("CurrencySymLength")
  ViewColumnFormat4 setCurrencySymbolLength(long len);

  @StructureGetter("CurrencyType")
  Optional<CurrencyType> getCurrencyType();

  /**
   * Retrieves the currency type as a raw {@code byte}.
   * 
   * @return the currency type as a {@code byte}
   * @since 1.24.0
   */
  @StructureGetter("CurrencyType")
  byte getCurrencyTypeRaw();

  @StructureSetter("CurrencyType")
  ViewColumnFormat4 setCurrencyType(CurrencyType type);

  /**
   * Sets the currency type as a raw {@code byte}.
   * 
   * @param type the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("CurrencyType")
  ViewColumnFormat4 setCurrencyTypeRaw(byte type);

  @StructureGetter("DecimalSymLength")
  long getDecimalSymbolLength();

  @StructureSetter("DecimalSymLength")
  ViewColumnFormat4 setDecimalSymbolLength(long len);

  @StructureGetter("ISOCountry")
  long getISOCountry();

  @StructureSetter("ISOCountry")
  ViewColumnFormat4 setISOCountry(long countryCode);

  @StructureGetter("MilliGroupSize")
  int getMilliGroupSize();

  @StructureSetter("MilliGroupSize")
  ViewColumnFormat4 setMilliGroupSize(int size);

  @StructureGetter("MilliSepSymLength")
  long getMilliSeparatorLength();

  @StructureSetter("MilliSepSymLength")
  ViewColumnFormat4 setMilliSeparatorLength(long len);

  @StructureGetter("NegativeSymLength")
  long getNegativeSymbolLength();

  @StructureSetter("NegativeSymLength")
  ViewColumnFormat4 setNegativeSymbolLength(long len);

  @StructureGetter("NumberFormat")
  NFMT getNumberFormat();
  
  @StructureGetter("NumberPreference")
  short getNumberPreference();
  
  @StructureGetter("NumSymFlags")
  byte getNumberSymbolFlags();

  @StructureGetter("NumSymPref")
  Optional<NumberPref> getNumberSymbolPreference();

  /**
   * Retrieves the number symbol pref as a raw {@code byte}.
   * 
   * @return the number symbol pref as a {@code byte}
   * @since 1.24.0
   */
  @StructureGetter("NumSymPref")
  byte getNumberSymbolPreferenceRaw();

  @StructureSetter("NumSymPref")
  ViewColumnFormat4 setNumberSymbolPreference(NumberPref pref);

  /**
   * Sets the number symbol pref as a raw {@code byte}.
   * 
   * @param pref the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("NumSymPref")
  ViewColumnFormat4 setNumberSymbolPreferenceRaw(byte pref);

  @StructureGetter("Signature")
  short getSignature();

  @StructureSetter("Signature")
  ViewColumnFormat4 setSignature(short signature);

  default String getCurrencySymbol() {
    return StructureSupport.extractStringValue(this,
        this.getDecimalSymbolLength() + this.getMilliSeparatorLength() + this.getNegativeSymbolLength(),
        this.getCurrencySymbolLength());
  }

  default ViewColumnFormat4 setCurrencySymbol(final String sym) {
    return StructureSupport.writeStringValue(
        this,
        this.getDecimalSymbolLength() + this.getMilliSeparatorLength() + this.getNegativeSymbolLength(),
        this.getCurrencySymbolLength(),
        sym,
        this::setCurrencySymbolLength);
  }

  default String getDecimalSymbol() {
    return StructureSupport.extractStringValue(this,
        0,
        this.getDecimalSymbolLength());
  }
  
  default ViewColumnFormat4 setDecimalSymbol(final String symbol) {
    return StructureSupport.writeStringValue(
        this,
        0,
        this.getDecimalSymbolLength(),
        symbol,
        this::setDecimalSymbolLength);
  }

  default String getMilliSeparator() {
    return StructureSupport.extractStringValue(this,
        this.getDecimalSymbolLength(),
        this.getMilliSeparatorLength());
  }

  default ViewColumnFormat4 setMilliSeparator(final String sep) {
    return StructureSupport.writeStringValue(
        this,
        this.getDecimalSymbolLength(),
        this.getMilliSeparatorLength(),
        sep,
        this::setMilliSeparatorLength);
  }

  default String getNegativeSymbol() {
    return StructureSupport.extractStringValue(this,
        this.getDecimalSymbolLength() + this.getMilliSeparatorLength(),
        this.getNegativeSymbolLength());
  }

  default ViewColumnFormat4 setNegativeSymbol(final String sym) {
    return StructureSupport.writeStringValue(
        this,
        this.getDecimalSymbolLength() + this.getMilliSeparatorLength(),
        this.getNegativeSymbolLength(),
        sym,
        this::setNegativeSymbolLength);
  }
}
