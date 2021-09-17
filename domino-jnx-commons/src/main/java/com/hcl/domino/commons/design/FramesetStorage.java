package com.hcl.domino.commons.design;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import com.hcl.domino.DominoException;
import com.hcl.domino.design.FramesetLayout;
import com.hcl.domino.design.FramesetLayout.FrameSizeUnit;
import com.hcl.domino.design.frameset.FrameSizingType;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.records.CDFrame;
import com.hcl.domino.richtext.records.CDFrameset;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.structures.FramesetLength;

/**
 * Utility class that handles CD records storage of frameset layouts
 * 
 * @author Karsten Lehmann
 */
public enum FramesetStorage {
  INSTANCE;

  /**
   * Expects to {@link ListIterator} to be positioned on a {@link CDFrameset} record.
   * The method parses all CD records belonging to the frameset.
   * 
   * @param recordsIt CD records
   * @return frameset
   */
  public static FramesetLayoutImpl readFrameset(ListIterator<RichTextRecord<?>> recordsIt) {
    CDFrameset framesetRecord = (CDFrameset) recordsIt.next();

    //collect all CD records that describe the frameset (probably just the one CDFrameset record)
    List<RichTextRecord<?>> framesetRecords = new ArrayList<>();
    framesetRecords.add(framesetRecord);

    while (recordsIt.hasNext()) {
      RichTextRecord<?> record = recordsIt.next();

      if (record instanceof CDFrame || record instanceof CDFrameset) {
        //stop when the first frame/subframeset starts
        recordsIt.previous();
        break;
      }
      else {
        framesetRecords.add(record);
      }
    }

    FramesetLayoutImpl frameset = new FramesetLayoutImpl(framesetRecords);

    //read frameset elements

    //either row or column count is set
    int rowCount = framesetRecord.getRowCount();
    int colCount = framesetRecord.getColumnCount();
    int rowOrColCount = rowCount>colCount ? rowCount : colCount;
    List<FramesetLength> lengths = framesetRecord.getLengths();
    
    if (rowOrColCount > 0) {
      int count = 0;

      while (recordsIt.hasNext()) {
        RichTextRecord<?> record = recordsIt.next();

        if (record instanceof CDFrame) {
          //position iterator on CDFrame
          recordsIt.previous();
          FrameImpl frame = parseFrame(recordsIt);
          
          FramesetLength currLength = lengths.get(count);
          //transfer initial size from lengths list
          frame.size = currLength.getValue();
          if (currLength.getType()==FrameSizingType.PERCENTAGE) {
            frame.sizeUnit = Optional.of(FrameSizeUnit.PERCENTAGE);
          }
          else if (currLength.getType()==FrameSizingType.PIXELS) {
            frame.sizeUnit = Optional.of(FrameSizeUnit.PIXELS);
          }
          else if (currLength.getType()==FrameSizingType.RELATIVE) {
            frame.sizeUnit = Optional.of(FrameSizeUnit.RELATIVE);
          }
          else {
            frame.sizeUnit = Optional.empty();
          }
          frameset.addContent(frame);

          count++;
          if (count == rowOrColCount) {
            //we have read all frameset elements
            break;
          }
        }
        else if (record instanceof CDFrameset) {
          //rewind to previous position
          recordsIt.previous();
          FramesetLayoutImpl subFrameset = readFrameset(recordsIt);
          FramesetLength currLength = lengths.get(count);
          //transfer initial size from lengths list
          subFrameset.size = currLength.getValue();
          if (currLength.getType()==FrameSizingType.PERCENTAGE) {
            subFrameset.sizeUnit = Optional.of(FrameSizeUnit.PERCENTAGE);
          }
          else if (currLength.getType()==FrameSizingType.PIXELS) {
            subFrameset.sizeUnit = Optional.of(FrameSizeUnit.PIXELS);
          }
          else if (currLength.getType()==FrameSizingType.RELATIVE) {
            subFrameset.sizeUnit = Optional.of(FrameSizeUnit.RELATIVE);
          }
          else {
            subFrameset.sizeUnit = Optional.empty();
          }

          frameset.addContent(subFrameset);

          count++;
          if (count == rowOrColCount) {
            //we have read all frameset elements
            break;
          }
        }
      }

      if (count < rowOrColCount) {
        throw new IllegalStateException(
            MessageFormat.format("Missing frameset element records. Found {0}, expected {1}",
                count, rowOrColCount));
      }
    }

    return frameset;
  }


  /**
   * Expects the {@link ListIterator} to be positions on a {@link CDFrame} record.
   * The method parses all CD records belonging to the frame.
   * 
   * @param recordsIt CD records
   * @return frame
   */
  private static FrameImpl parseFrame(ListIterator<RichTextRecord<?>> recordsIt) {
    List<RichTextRecord<?>> frameRecords = new ArrayList<>();
    CDFrame frameRecord = (CDFrame) recordsIt.next();
    frameRecords.add(frameRecord);

    while (recordsIt.hasNext()) {
      RichTextRecord<?> nextRecord = recordsIt.next();
      if (nextRecord instanceof CDFrame || nextRecord instanceof CDFrameset) {
        //gone too far
        recordsIt.previous();
        break;
      }
      else {
        frameRecords.add(nextRecord);
      }

    }

    return new FrameImpl(frameRecords);
  }

  /**
   * Writes the CD records for the frameset and its content to a {@link RichTextWriter}
   * 
   * @param frameset frameset
   * @param rtWriter target writer
   */
  public static void writeFrameset(FramesetLayout frameset, RichTextWriter rtWriter) {
    IFramesetRecordAccess recordAccess = frameset.getAdapter(IFramesetRecordAccess.class);
    if (recordAccess==null) {
      throw new DominoException("Frameset does not provide an adapter for IFramesetRecordAccess");
    }

    List<RichTextRecord<?>> records = recordAccess.getRecords();
    records.forEach(rtWriter::addRichTextRecord);
  }

  public interface IFramesetRecordAccess {

    public List<RichTextRecord<?>> getRecords();

  }
}
