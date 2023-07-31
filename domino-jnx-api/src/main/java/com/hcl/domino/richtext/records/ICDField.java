package com.hcl.domino.richtext.records;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.design.format.FieldListDelimiter;
import com.hcl.domino.design.format.FieldListDisplayDelimiter;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;

/**
 * Represents common behavior among old and new CDField types
 * @since 1.27.0
 */
public interface ICDField {
  enum Flag implements INumberEnum<Short> {
    /** Field contains read/writers */
    READWRITERS(RichTextConstants.FREADWRITERS),
    /** Field is editable, not read only */
    EDITABLE(RichTextConstants.FEDITABLE),
    /** Field contains distinguished names */
    NAMES(RichTextConstants.FNAMES),
    /** Store DV, even if not spec'ed by user */
    STOREDV(RichTextConstants.FSTOREDV),
    /** Field contains document readers */
    READERS(RichTextConstants.FREADERS),
    /** Field contains a section */
    SECTION(RichTextConstants.FSECTION),
    /** can be assumed to be clear in memory, V3 &amp; later */
    SPARE3(RichTextConstants.FSPARE3),
    /** IF CLEAR, CLEAR AS ABOVE */
    V3FAB(RichTextConstants.FV3FAB),
    /** Field is a computed field */
    COMPUTED(RichTextConstants.FCOMPUTED),
    /** Field is a keywords field */
    KEYWORDS(RichTextConstants.FKEYWORDS),
    /** Field is protected */
    PROTECTED(RichTextConstants.FPROTECTED),
    /** Field name is simply a reference to a shared field note */
    REFERENCE(RichTextConstants.FREFERENCE),
    /** sign field */
    SIGN(RichTextConstants.FSIGN),
    /** seal field */
    SEAL(RichTextConstants.FSEAL),
    /** standard UI */
    KEYWORDS_UI_STANDARD(RichTextConstants.FKEYWORDS_UI_STANDARD),
    /** checkbox UI */
    KEYWORDS_UI_CHECKBOX(RichTextConstants.FKEYWORDS_UI_CHECKBOX),
    /** radiobutton UI */
    KEYWORDS_UI_RADIOBUTTON(RichTextConstants.FKEYWORDS_UI_RADIOBUTTON),
    /** allow doc editor to add new values */
    KEYWORDS_UI_ALLOW_NEW(RichTextConstants.FKEYWORDS_UI_ALLOW_NEW);

    private final short value;

    Flag(final short value) {
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
  
  Optional<ItemDataType> getFieldType();
  
  String getDefaultValueFormula();
  
  String getDescription();
  
  String getInputTranslationFormula();
  
  String getInputValidationFormula();
  
  /**
   * @return an {@link Optional} describing the value formula for field choices,
   *         or an empty one if the values are defined by a an explicit text list
   */
  Optional<String> getTextValueFormula();
  
  FieldListDisplayDelimiter getListDisplayDelimiter();

  Set<FieldListDelimiter> getListDelimiters();
  
  String getName();
  
  /**
   * @return an {@link Optional} describing the explicit text value options for
   *         this field,
   *         or an empty one if the values are defined by a formula
   */
  Optional<List<String>> getTextValues();
  
  Set<Flag> getFlags();
}
