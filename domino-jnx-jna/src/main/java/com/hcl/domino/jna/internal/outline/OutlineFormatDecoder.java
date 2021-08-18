package com.hcl.domino.jna.internal.outline;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import com.hcl.domino.commons.design.outline.DominoOutlineFormat;
import com.hcl.domino.commons.misc.ODSTypes;
import com.hcl.domino.commons.richtext.records.MemoryStructureProxy;
import com.hcl.domino.design.format.SiteMapEntry;
import com.hcl.domino.design.format.SiteMapHeaderFormat;
import com.hcl.domino.design.format.SiteMapOutlineHeader;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.richtext.structures.MemoryStructure;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class OutlineFormatDecoder {
  
  public static DominoOutlineFormat decodeOutlineFormat(Pointer dataPtr, int valueLength) {
    
    DominoOutlineFormat result = new DominoOutlineFormat();
    ByteBuffer data = dataPtr.getByteBuffer(0, valueLength);
    SiteMapHeaderFormat header = readMemory(data, ODSTypes._OUTLINE_FORMAT, SiteMapHeaderFormat.class);
    short items = header.getItems();
    short numEntries = header.getEntries();
    
    SiteMapOutlineHeader outlineHeader = readMemory(data, ODSTypes._OUTLINE_FORMAT, SiteMapOutlineHeader.class);
    int flags = outlineHeader.getFlags();
    
    short[] itemEntries = new short[items];
    for (int i=0; i<items; i++) {
      itemEntries[i] = readBuffer(data, 2).getShort();
    }
    for (int i=0; i<items; i++) {
      for (int j=0; j<itemEntries[i]; j++) {

        SiteMapEntry sitemapEntry = readMemory(data, ODSTypes._OUTLINE_FORMAT, SiteMapEntry.class);
        int fixedSize  = sitemapEntry.getEntryFixedSize();
        short varSize  = sitemapEntry.getEntryVarSize();
        int id = sitemapEntry.getId();
        short titleSize =  sitemapEntry.getTitleSize();
        short aliasSize  = sitemapEntry.getAliasSize();
        short entryType = sitemapEntry.getEntryType();
        short entryClass = sitemapEntry.getEntryClass();
        short entryDesignType = sitemapEntry.getEntryDesignType();
        short imageSize  = sitemapEntry.getImagesSize();
        short popupSize = sitemapEntry.getPopupSize();
        short onclickSize = sitemapEntry.getOnClickSize();
        short sourceSize = sitemapEntry.getSourceSize();
        
        //for now just read title
        String title = StandardCharsets.UTF_8.decode(readBuffer(data, titleSize)).toString();
        result.addTitle(title);
       
        //skip rest of variable size for now
        int  skipVarSize  = varSize - title.length();
        readBuffer(data, skipVarSize);
      }
    }
    
    
    
        
    return  result;
  }
  
  @SuppressWarnings("unused")
  private static <T extends MemoryStructure> T readMemory(PointerByReference ppData, short odsType, Class<T> struct) {
        
        // Straight-read variant
        T result = MemoryStructureProxy.newStructure(struct, 0);
        int len = MemoryStructureProxy.sizeOf(struct);
        result.getData().put(ppData.getValue().getByteBuffer(0, len));
        ppData.setValue(ppData.getValue().share(len));
        
        return result;
    }

  @SuppressWarnings("unused")
    private static <T extends MemoryStructure> T odsReadMemory(Pointer data, short odsType, Class<T> struct) {
    // TODO determine if any architectures need ODSReadMemory. On x64 macOS, it seems harmful.
    //    Docs just say "Intel", but long predate x64. On Windows, it says it should be harmless, but
    //    care has to be taken on "UNIX", which is everything else.
    //    Additionally, not all structures here have ODS numbers
      PointerByReference ppData = new PointerByReference(data);
    Memory mem = new Memory(MemoryStructureProxy.sizeOf(struct));
    NotesCAPI.get().ODSReadMemory(ppData, odsType, mem, (short)1);
    return MemoryStructureProxy.forStructure(struct, () -> mem.getByteBuffer(0, mem.size()));
      
    }
    
    /**
     * Reads a structure from the provided ByteBuffer, incrementing its position the size of the struct.
     * 
     * @param <T> the class of structure to read
     * @param data the containing data buffer
     * @param odsType the ODS type, or {@code -1} if not known
     * @param struct a {@link Class} represening {@code <T>}
     * @return the read structure
     */
    private static <T extends MemoryStructure> T readMemory(ByteBuffer data, short odsType, Class<T> struct) {
      T result = MemoryStructureProxy.newStructure(struct, 0);
    int len = MemoryStructureProxy.sizeOf(struct);
    byte[] bytes = new byte[len];
    data.get(bytes);
    result.getData().put(bytes);
    return result;
    }
    
    private static ByteBuffer readBuffer(ByteBuffer buf, long len) {
      ByteBuffer result = subBuffer(buf, (int)len);
      buf.position(buf.position()+(int)len);
      return result;
    }
    private static ByteBuffer subBuffer(ByteBuffer buf, int len) {
      ByteBuffer tempBuf = buf.slice().order(ByteOrder.nativeOrder());
    tempBuf.limit(len);
    return tempBuf;
    }

}
