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
package com.hcl.domino.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * Utility class to dump memory content
 * 
 * @author Karsten Lehmann
 */
public class JNXDumpUtil {

  /**
   * Creates a log file and ensures its content is
   * written to disk when the method is done (e.g. to write something to disk before
   * a crash).<br>
   * By default we use the temp directory (system property "java.io.tmpdir").
   * By setting the system property "dominojnx.dumpdir", the output directory can be changed.
   * 
   * @param suffix filename will be "domino-jnxlog-" + suffix + uniquenr + ".txt"
   * @param content file content
   * @return created temp file or null in case of errors
   */
  public static Path writeLogFile(final String suffix, final String content) {
    return AccessController.doPrivileged((PrivilegedAction<Path>) () -> {
      OutputStream fOut = null;
      Writer fWriter = null;
      try {
        String outDirPath = AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty("dominojnx.dumpdir", null)); //$NON-NLS-1$
        if (JNXStringUtil.isEmpty(outDirPath)) {
          outDirPath = AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty("java.io.tmpdir", null)); //$NON-NLS-1$
        }
        Path outDir = Paths.get(outDirPath);
        Files.createDirectories(outDir);
        Path dmpFile = Files.createTempFile(outDir, "domino-jnxlog-"+suffix+"-", ".txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        
        fOut = Files.newOutputStream(dmpFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        fWriter = new OutputStreamWriter(fOut, StandardCharsets.UTF_8);
        fWriter.write(content);
        fWriter.flush();
        return dmpFile;
      } catch (IOException e1) {
        e1.printStackTrace();
      }
      finally {
        if (fWriter!=null) {
          try {
            fWriter.close();
          } catch (IOException e2) {
            e2.printStackTrace();
          }
        }
        if (fOut!=null) {
          try {
            fOut.close();
          } catch (IOException e3) {
            e3.printStackTrace();
          }
        }
      }
      
      return null;
    });
  }
  
  /**
   * Reads memory content at the specified pointer and produces a String with hex codes and
   * character data in case the memory contains bytes in ascii range. Calls {@link #dumpAsAscii(ByteBuffer, int, int)}
   * with cols = 8 and size = buf.remaining().
   * 
   * @param buf byte buffer
   * @return memory dump
   * @since 1.0.32
   */
  public static String dumpAsAscii(ByteBuffer buf) {
    return dumpAsAscii(buf, buf.remaining());
  }
  
  /**
   * Reads byte array content and produces a String with hex codes and character data in case the memory contains
   * bytes in ascii range. Calls {@link #dumpAsAscii(ByteBuffer, int, int)} with cols = 8 and size = data.length.
   * 
   * @param data byte array
   * @return memory dump
   * @since 1.0.32
   */
  public static String dumpAsAscii(byte[] data) {
    return dumpAsAscii(ByteBuffer.wrap(data));
  }
  
  /**
   * Reads memory content at the specified pointer and produces a String with hex codes and
   * character data in case the memory contains bytes in ascii range. Calls {@link #dumpAsAscii(ByteBuffer, int, int)}
   * with cols = 8.
   * 
   * @param buf byte buffer
   * @param size number of bytes to read
   * @return memory dump
   */
  public static String dumpAsAscii(ByteBuffer buf, int size) {
    return dumpAsAscii(buf, size, 8);
  }

  /**
   * Reads memory content at the specified pointer and produces a String with hex codes and
   * character data in case the memory contains bytes in ascii range.
   * 
   * @param buf byte buffer
   * @param size number of bytes to read
   * @param cols number of bytes written in on eline
   * @return memory dump
   */
  public static String dumpAsAscii(ByteBuffer buf, int size, int cols) {
    StringBuilder sb = new StringBuilder();
    
    int i = 0;

    size = Math.min(size, buf.limit());

    while (i < size) {
      sb.append("["); //$NON-NLS-1$
      for (int c=0; c<cols; c++) {
        if (c>0) {
          sb.append(' ');
        }
        
        if ((i+c) < size) {
          byte b = buf.get(i+c);
           if (b >=0 && b < 16)
           {
            sb.append("0"); //$NON-NLS-1$
          }
                  sb.append(Integer.toHexString(b & 0xFF));
        }
        else {
          sb.append("  "); //$NON-NLS-1$
        }
      }
      sb.append("]"); //$NON-NLS-1$
      
      sb.append("   "); //$NON-NLS-1$
      
      sb.append("["); //$NON-NLS-1$
      for (int c=0; c<cols; c++) {
        if ((i+c) < size) {
          byte b = buf.get(i+c);
          int bAsInt = (b & 0xff);
          
          if (bAsInt >= 32 && bAsInt<=126) {
            sb.append((char) (b & 0xFF));
          }
          else {
            sb.append("."); //$NON-NLS-1$
          }
        }
        else {
          sb.append(" "); //$NON-NLS-1$
        }
      }
      sb.append("]\n"); //$NON-NLS-1$

      i += cols;
    }
    return sb.toString();
  }
  
  /**
   * Reads memory content of two {@link ByteBuffer} objects and produces a String with hex codes and
   * character data in case the memory contains bytes in ascii range.
   * 
   * @param headline1 headline for first bytebuffer
   * @param buf1 first byte buffer
   * @param headline2 headline for second bytebuffer
   * @param buf2 second byte buffer
   * @param size number of bytes to read
   * @param cols number of bytes written in on eline
   * @return memory dump
   */
  public static String dumpAsAscii(String headline1, ByteBuffer buf1, String headline2, ByteBuffer buf2, int size, int cols) {
    String dumpBuf1 = dumpAsAscii(buf1, size, cols);
    String dumpBuf2 = dumpAsAscii(buf2, size, cols);
    
    try (Scanner scannerBuf1 = new Scanner(new StringReader(dumpBuf1));
        Scanner scannerBuf2 = new Scanner(new StringReader(dumpBuf2));) {
      
      List<String> lines1 = new ArrayList<>();
      List<String> lines2 = new ArrayList<>();
      
      int maxLine1Len = 0;
      int maxLine2Len = 0;
      
      while (scannerBuf1.hasNext() || scannerBuf2.hasNext()) {
        String line1 = scannerBuf1.hasNext() ? scannerBuf1.nextLine() : ""; //$NON-NLS-1$
        maxLine1Len = Math.max(maxLine1Len, line1.length());
        
        lines1.add(line1);
        
        String line2 = scannerBuf2.hasNext() ? scannerBuf2.nextLine() : ""; //$NON-NLS-1$
        maxLine2Len = Math.max(maxLine2Len, line2.length());
        
        lines2.add(line2);
      }
      
      StringBuilder sb = new StringBuilder();
      
      //write header
      sb.append(headline1.substring(0, Math.min(headline1.length(), maxLine1Len)));
      if (headline1.length() < maxLine1Len) {
        sb.append(JNXStringUtil.repeat(' ', maxLine1Len - headline1.length()));
      }
      
      sb.append(" | "); //$NON-NLS-1$
      
      sb.append(headline2.substring(0, Math.min(headline2.length(), maxLine2Len)));
      if (headline2.length() < maxLine2Len) {
        sb.append(JNXStringUtil.repeat(' ', maxLine2Len - headline2.length()));
      }
      
      sb.append(" | Different?"); //$NON-NLS-1$

      sb.append("\n"); //$NON-NLS-1$
      
      for (int i=0; i<lines1.size(); i++) {
        String currLine1 = lines1.get(i);
        if (currLine1.length() < maxLine1Len) { //$NON-NLS-1$
          currLine1 += JNXStringUtil.repeat(' ', maxLine1Len-currLine1.length());
        }
        
        String currLine2 = lines2.get(i);
        if (currLine2.length() < maxLine2Len) { //$NON-NLS-1$
          currLine2 += JNXStringUtil.repeat(' ', maxLine2Len-currLine2.length());
        }
        
        sb.append(currLine1);
        sb.append(" | "); //$NON-NLS-1$
        sb.append(currLine2);
        
        sb.append(" |"); //$NON-NLS-1$
        
        if (!currLine1.equals(currLine2)) {
          sb.append(" X"); //$NON-NLS-1$
        }
        
        sb.append("\n"); //$NON-NLS-1$
      }
      
      return sb.toString();
    }
  }
  
  /**
   * Reads richtext records and produces a string with record type, hex codes and
   * character data.
   * 
   * @param rt richtext records
   * @return record dump
   */
  public static String dumpAsAscii(Collection<RichTextRecord<?>> rt) {
    StringBuilder sb = new StringBuilder();
    rt.forEach((record) -> {
      String txt = dumpAsAscii(record);
      sb.append(txt);
    });

    return sb.toString();
  }
  
  /**
   * Produces a string with richtext record type, hex codes and character data
   * 
   * @param record richtext record
   * @return record dump
   */
  public static String dumpAsAscii(RichTextRecord<?> record) {
	  return new StringBuilder()
        .append(record.getType())
        .append(MessageFormat.format(" ({0} bytes total, {1} bytes variable length)", record.getCDRecordLength(), record.getVariableData().capacity())) //$NON-NLS-1$
        .append('\n')
        .append(dumpAsAscii(record.getData()))
        .append("\n\n") //$NON-NLS-1$
        .toString();
  }
}
