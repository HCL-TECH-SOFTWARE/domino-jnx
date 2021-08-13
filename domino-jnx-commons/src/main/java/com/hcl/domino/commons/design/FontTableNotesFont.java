package com.hcl.domino.commons.design;

import java.nio.ByteBuffer;
import java.text.MessageFormat;

import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.data.NotesFont;
import com.hcl.domino.richtext.records.CDFontTable;
import com.hcl.domino.richtext.structures.CDFace;
import com.hcl.domino.richtext.structures.FontStyle;

/**
 * This {@link NotesFont} implementation reads its custom font information
 * from a CDFONTTABLE record.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public class FontTableNotesFont extends AbstractNotesFont {
  private final CDFontTable fontTable;

  public FontTableNotesFont(FontStyle fontStyle, CDFontTable fontTable) {
    super(fontStyle);
    this.fontTable = fontTable;
  }
  
  @Override
  protected String lookUpFaceID(int faceId) {
    ByteBuffer facesData = fontTable.getVariableData();
    int faceSize = MemoryStructureUtil.sizeOf(CDFace.class);
    for(int i = 0; i < fontTable.getFontCount(); i++) {
      facesData.position(i * faceSize);
      ByteBuffer faceData = facesData.slice();
      faceData.limit(faceSize);
      CDFace face = MemoryStructureUtil.forStructure(CDFace.class, () -> faceData);
      if(face.getFace() == faceId) {
        return face.getName();
      }
    }
    
    throw new IllegalStateException(MessageFormat.format("Unable to find face ID {0} in font table", faceId));
  }

}
