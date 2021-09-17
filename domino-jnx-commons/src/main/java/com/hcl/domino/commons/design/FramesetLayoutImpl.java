package com.hcl.domino.commons.design;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.hcl.domino.commons.design.FramesetStorage.IFramesetRecordAccess;
import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.design.ClassicThemeBehavior;
import com.hcl.domino.design.Frame;
import com.hcl.domino.design.FrameContent;
import com.hcl.domino.design.FramesetLayout;
import com.hcl.domino.design.frameset.FrameSizingType;
import com.hcl.domino.richtext.records.CDFrameset;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.FramesetLength;

/**
 * Implementation for {@link FramesetLayout}
 * 
 * @author Karsten Lehmann
 */
public class FramesetLayoutImpl implements FramesetLayout {
  private Optional<FramesetLayout> parent = Optional.empty();
  private List<RichTextRecord<?>> framesetRecords;
  private List<FrameContent<?>> contentList;
  private boolean layoutDirty;
  
  Optional<FramesetOrientation> orientation = Optional.empty();
  int size;
  Optional<FrameSizeUnit> sizeUnit;
  
  public FramesetLayoutImpl() {
    this(Collections.emptyList());
    initDefaults();
  }

  public FramesetLayoutImpl(List<RichTextRecord<?>> framesetRecords) {
    this.framesetRecords = framesetRecords;
    this.contentList = new ArrayList<>();
    
    //find current orientation
    this.framesetRecords
    .stream()
    .filter(CDFrameset.class::isInstance)
    .map(CDFrameset.class::cast)
    .forEach((record) -> {
      int rowCount = record.getRowCount();
      int colCount = record.getColumnCount();
      if (rowCount > colCount) {
        orientation = Optional.of(FramesetOrientation.TOP_TO_BOTTOM);
      }
      else {
        orientation = Optional.of(FramesetOrientation.LEFT_TO_RIGHT);
      }
    });
  }

  void initDefaults() {
    withFramesetRecord((record) -> {
      record.setBorderEnable((byte) 1);
      record.setFrameBorderWidth(7);
      record.setThemeSetting(ClassicThemeBehavior.USE_DATABASE_SETTING);
    });
    
    orientation = Optional.of(FramesetOrientation.LEFT_TO_RIGHT);
  }

  @Override
  public Optional<FramesetLayout> getParent() {
    return parent;
  }

  @Override
  public void setParent(FramesetLayout parent) {
    this.parent = Optional.ofNullable(parent);
  }
  
  private Optional<CDFrameset> getFramesetRecord() {
    return framesetRecords
        .stream()
        .filter(CDFrameset.class::isInstance)
        .map(CDFrameset.class::cast)
        .findFirst();
  }

  private void withFramesetRecord(Consumer<CDFrameset> consumer) {
    Optional<CDFrameset> optFramesetRecord = getFramesetRecord();
    CDFrameset framesetRecord;
    if (!optFramesetRecord.isPresent()) {
      framesetRecord = MemoryStructureUtil.newStructure(CDFrameset.class, 0);
      framesetRecords.add(0, framesetRecord);
    }
    else {
      framesetRecord = optFramesetRecord.get();
    }

    consumer.accept(framesetRecord);
  }

  void addContent(FrameContent<?> content) {
    withFramesetRecord((record) -> {
      this.contentList.add(content);
      content.setParent(this);
      
      //update row/col count in structure
      if (!orientation.isPresent() || orientation.get() == FramesetOrientation.LEFT_TO_RIGHT) {
        record.setColumnCount(contentList.size());
        record.setRowCount(0);
      }
      else if (orientation.get() == FramesetOrientation.TOP_TO_BOTTOM) {
        record.setColumnCount(0);
        record.setRowCount(contentList.size());
      }
      layoutDirty = true;
    });
  }

  @Override
  public Stream<FrameContent<?>> getContent() {
    return contentList.stream();
  }

  @Override
  public Optional<FramesetOrientation> getOrientation() {
    if (!orientation.isPresent()) {
      Optional<CDFrameset> optFrameset = getFramesetRecord();
      if (optFrameset.isPresent()) {
        CDFrameset frameset = optFrameset.get();
        int rowCount = frameset.getRowCount();
        int colCount = frameset.getColumnCount();
        if (rowCount > colCount) {
          orientation = Optional.of(FramesetOrientation.TOP_TO_BOTTOM);
        }
        else if (rowCount < colCount) {
          orientation = Optional.of(FramesetOrientation.LEFT_TO_RIGHT);
        }
      }
    }
    return orientation;
  }

  @Override
  public FramesetLayout setOrientation(FramesetOrientation orientation) {
    this.orientation = Optional.of(orientation);

    withFramesetRecord((record) -> {
      if (orientation == FramesetOrientation.LEFT_TO_RIGHT) {
        record.setColumnCount(contentList.size());
        record.setRowCount(0);
      }
      else if (orientation == FramesetOrientation.TOP_TO_BOTTOM) {
        record.setColumnCount(0);
        record.setRowCount(contentList.size());
      }
    });
    return this;
  }

  @Override
  public FrameContent<?> getContent(int pos) {
    return contentList.get(pos);
  }

  @Override
  public int getCount() {
    return contentList.size();
  }

  @Override
  public void deleteFrameContent(FrameContent<?> content) {
    int idx = contentList.indexOf(content);
    if (idx==-1) {
      throw new IllegalArgumentException("Content not found in frameset");
    }
    FrameContent<?> deletedContent = contentList.remove(idx);
    deletedContent.setParent(null);
    layoutDirty = true;
  }

  @Override
  public FramesetLayout flipHorizontally() {
    withFramesetRecord((record) -> {
      Collections.reverse(contentList);
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public boolean isLayoutDirty() {
    return layoutDirty || contentList.stream().anyMatch(FrameContent::isLayoutDirty);
  }

  @Override
  public void resetLayoutDirty() {
    layoutDirty = false;
    contentList.stream().forEach((content) -> {content.resetLayoutDirty(); }); 
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getAdapter(Class<T> clazz) {
    if (FramesetStorage.IFramesetRecordAccess.class == clazz) {
      return (T) new IFramesetRecordAccess() {

        @Override
        public List<RichTextRecord<?>> getRecords() {
          List<RichTextRecord<?>> allRecords = new ArrayList<>();

          //make sure we have a CDFrameset record
          withFramesetRecord((record) -> {
            framesetRecords.forEach(allRecords::add);

            List<FramesetLength> lengths  = new ArrayList<>();
            
            for (FrameContent<?> currContent : contentList) {
              IFramesetRecordAccess subRecordAccess = currContent.getAdapter(IFramesetRecordAccess.class);
              if (subRecordAccess!=null) {
                List<RichTextRecord<?>> subRecords = subRecordAccess.getRecords();
                subRecords.forEach(allRecords::add);
              }
              
              //rebuild list of frameset lengths based on current size
              int currSize = currContent.getSize();
              Optional<FrameSizeUnit> currSizeUnit = currContent.getSizeUnit();
              
              FramesetLength currLength = MemoryStructureUtil.newStructure(FramesetLength.class, 0);
              currLength.setValue(currSize);
              
              if (!currSizeUnit.isPresent() || currSizeUnit.get()==FrameSizeUnit.RELATIVE) {
                currLength.setType(FrameSizingType.RELATIVE);
              }
              else if (currSizeUnit.get()==FrameSizeUnit.PERCENTAGE) {
                currLength.setType(FrameSizingType.PERCENTAGE);
              }
              else if (currSizeUnit.get()==FrameSizeUnit.PIXELS) {
                currLength.setType(FrameSizingType.PIXELS);
              }
              lengths.add(currLength);
            }
            
            //set new lengths; this also sets row/col counts
            if (!orientation.isPresent() ||
                orientation.get() == FramesetOrientation.LEFT_TO_RIGHT) {
              
              record.setLengths(lengths, false); // isRow = false
            }
            else {
              record.setLengths(lengths, true); // isRow = true
            }
            
            
          });
          return allRecords;
        }
      };
    }
    return null;
  }

  @Override
  public Frame createFrame() {
    return new FrameImpl();
  }
  
  @Override
  public FramesetLayout createFrameset() {
    return new FramesetLayoutImpl();
  }

  @Override
  public FramesetLayout initColumns(FrameContent<?>... frames) {
    setOrientation(FramesetOrientation.LEFT_TO_RIGHT);
    contentList.clear();
    if (frames!=null) {
      for (FrameContent<?> currContent : frames) {
        contentList.add(currContent);
      }
    }
    return this;
  }

  @Override
  public FramesetLayout initRows(FrameContent<?>... frames) {
    setOrientation(FramesetOrientation.TOP_TO_BOTTOM);
    contentList.clear();
    if (frames!=null) {
      for (FrameContent<?> currContent : frames) {
        contentList.add(currContent);
      }
    }
    return this; 
  }

  @Override
  public int getSize() {
    return size;
  }
  
  @Override
  public Optional<FrameSizeUnit> getSizeUnit() {
    return sizeUnit;
  }
  
  @Override
  public FramesetLayout setSize(int amount, FrameSizeUnit unit) {
    this.size = amount;
    this.sizeUnit = Optional.of(unit);
    return this;
  }

  @Override
  public FramesetLayout replace(FrameContent<?> oldContent, FrameContent<?> newContent) {
    int idx = contentList.indexOf(oldContent);
    if (idx==-1) {
      throw new IllegalArgumentException("Content not found in frameset");
    }
    contentList.set(idx, newContent);
    oldContent.setParent(null);
    newContent.setParent(this);
    layoutDirty = true;
    return this;
  }

  @Override
  public boolean isBorderEnabled() {
    return getFramesetRecord()
        .map((record) -> { return record.getBorderEnable() == 1;})
        .orElse(false);
  }

  @Override
  public FramesetLayout setBorderEnabled(boolean b) {
    withFramesetRecord((record) -> {
      record.setBorderEnable((byte) (b ? 1 : 0));
    });
    return this;
  }

  @Override
  public int getFrameBorderWidth() {
    return getFramesetRecord()
        .map((record) -> { return record.getFrameBorderWidth(); })
        .orElse(0);
  }

  @Override
  public FramesetLayout setFrameBorderWidth(int width) {
    withFramesetRecord((record) -> {
      record.setFrameBorderWidth(width);
    });
    return this;
  }

  @Override
  public int getFrameSpacingWidth() {
    return getFramesetRecord()
        .map((record) -> { return record.getFrameSpacingWidth(); })
        .orElse(0);
  }

  @Override
  public FramesetLayout setFrameSpacingWidth(int width) {
    withFramesetRecord((record) -> {
      record.setFrameSpacingWidth(width);
    });
    return this;
  }

  @Override
  public ColorValue getFrameBorderColor() {
    return getFramesetRecord().get().getFrameBorderColor();
  }
  
  @Override
  public FramesetLayout setFrameBorderColor(ColorValue color) {
    withFramesetRecord((record) -> {
      record.getFrameBorderColor().copyFrom(color);
    });
    return this;
  }
  
  @Override
  public FrameContent<?> splitIntoColumns(Frame frame) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FrameContent<?> splitIntoRows(Frame frame) {
    // TODO Auto-generated method stub
    return null;
  }


}
