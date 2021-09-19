package com.hcl.domino.commons.design;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.ListIterator;

import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.Frameset;
import com.hcl.domino.design.FramesetLayout;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.records.CDFrameset;
import com.hcl.domino.richtext.records.CDFramesetHeader;
import com.hcl.domino.richtext.records.CDFramesetHeader.Version;
import com.hcl.domino.richtext.records.RecordType.Area;
import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * Implementation of the frameset design element
 * 
 * @author Karsten Lehmann
 */
public class FramesetImpl extends AbstractDesignElement<Frameset> implements Frameset, IDefaultNamedDesignElement {
  private FramesetLayout framesetLayout;
  private List<RichTextRecord<?>> headerRecords;

  public FramesetImpl(Document doc) {
    super(doc);

    //collect all header records for this named frameset, which probably is only one CDFramesetHeader record
    headerRecords = new ArrayList<>();

    List<RichTextRecord<?>> records = doc.getRichTextItem(DesignConstants.ITEM_NAME_FRAMESET, Area.FRAMESETS);
    ListIterator<RichTextRecord<?>> recordsIt = records.listIterator();
    if (recordsIt.hasNext()) {
      //first record is expected to be of type CDFramesetHeader
      RichTextRecord<?> firstRecord = recordsIt.next();

      if (!(firstRecord instanceof CDFramesetHeader)) {
        throw new IllegalStateException(
            MessageFormat.format("Unexpected CD record found. Expected CDFramesetHeader and found {0}", firstRecord.getType()));
      }

      CDFramesetHeader headerRecord = (CDFramesetHeader) firstRecord;
      headerRecords.add(headerRecord);
      //check for supported version
      if (!headerRecord.getVersion().isPresent() || headerRecord.getVersion().get() != Version.VERSION2) {
        throw new IllegalStateException(
            MessageFormat.format("Unsupported value received for the frameset version: {0}", headerRecord.getVersion()));
      }
    }
    else {
      CDFramesetHeader headerRecord = MemoryStructureUtil.newStructure(CDFramesetHeader.class, 0);
      headerRecord.getHeader().setSignature(RichTextConstants.SIG_CD_FRAMESETHEADER);
      headerRecord.getHeader().setLength(MemoryStructureUtil.sizeOf(CDFramesetHeader.class));

      headerRecord.setVersion(Version.VERSION2);
      headerRecords.add(headerRecord);
    }

    //collect additional records before the first CDFrameset (probably none)
    while (recordsIt.hasNext()) {
      RichTextRecord<?> record = recordsIt.next();
      if (record instanceof CDFrameset) {
        //go back to CDFrameset
        recordsIt.previous();
        break;
      }
      else {
        headerRecords.add(record);
      }
    }

    if (recordsIt.hasNext()) {
      //parse frameset layout (recursively)
      this.framesetLayout = FramesetStorage.readFrameset(recordsIt);
    }
    else {
      this.framesetLayout = new FramesetLayoutImpl();
    }
  }

  @Override
  public void initializeNewDesignNote() {
    this.setFlags("#34CQ"); //$NON-NLS-1$
    Document doc = getDocument();
    doc.replaceItemValue("$DesignerVersion", "8.5.3"); //$NON-NLS-1$ //$NON-NLS-2$
    doc.replaceItemValue("$Comment", EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), ""); //$NON-NLS-1$ //$NON-NLS-2$
    
  }

  @Override
  public boolean save() {
    if (framesetLayout.isLayoutDirty()) {
      serialize();
    }
    getDocument().sign();
    
    return super.save();
  }
  
  /**
   * Writes the frameset header and all frameset/frame CD records to the design document
   * 
   * @param rtWriter 
   */
  private void serialize() {
    Document doc = getDocument();
    while (doc.hasItem(DesignConstants.ITEM_NAME_FRAMESET)) {
      doc.removeItem(DesignConstants.ITEM_NAME_FRAMESET);
    }

    try (RichTextWriter rtWriter = doc.createRichTextItem(DesignConstants.ITEM_NAME_FRAMESET);) {
      headerRecords.forEach(rtWriter::addRichTextRecord);
      FramesetStorage.writeFrameset(framesetLayout, rtWriter);
      framesetLayout.resetLayoutDirty();
    }
  }

  @Override
  public FramesetLayout getLayout() {
    return framesetLayout;
  }
}
