package com.hcl.domino.commons.design;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.hcl.domino.richtext.records.CDFrame;
import com.hcl.domino.richtext.records.CDFrameset;
import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * Represents and provides utility access to the records that make up a Frameset.
 * 
 * @author Jesse Gallagher
 * @since 1.0.34
 */
public class DominoFramesetFormat {
  private final List<RichTextRecord<?>> records;

  public DominoFramesetFormat(List<RichTextRecord<?>> records) {
    this.records = new ArrayList<>(records);
  }
  
  public Optional<CDFrameset> getFramesetRecord() {
    return records.stream()
      .filter(CDFrameset.class::isInstance)
      .map(CDFrameset.class::cast)
      .findFirst();
  }
  
  public Stream<CDFrame> getFrameRecords() {
    return records.stream()
      .filter(CDFrame.class::isInstance)
      .map(CDFrame.class::cast);
  }

}
