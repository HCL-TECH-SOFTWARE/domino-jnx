/*
 * ==========================================================================
 * Copyright (C) 2019-2021 HCL America, Inc. ( http://www.hcl.com/ )
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
package it.com.hcl.domino.test.html;

import static com.hcl.domino.html.HtmlConvertOption.XMLCompatibleHTML;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database.Action;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.html.EmbeddedImage;
import com.hcl.domino.html.EmbeddedImage.HTMLImageReader;
import com.hcl.domino.html.HtmlConversionResult;
import com.hcl.domino.html.HtmlConvertOption;
import com.hcl.domino.html.RichTextHTMLConverter;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.TextStyle;
import com.hcl.domino.richtext.structures.FontStyle;
import com.ibm.commons.util.StringUtil;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestHTMLRendering extends AbstractNotesRuntimeTest {

  @Test
  public void testDatabase() throws Exception {
    this.withResourceDxl("/dxl/testHtmlRendering", database -> {
      final RichTextHTMLConverter conv = database.getParentDominoClient().getRichTextHtmlConverter();

      final String html = conv.render(database)
          .convert()
          .getHtml();
      Assertions.assertNotNull(html, "html should not be null");
      Assertions.assertFalse(StringUtil.isEmpty(html), "html should not be empty");
      Assertions.assertTrue(html.contains("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\">"),
          "Converted HTML should use the default frameset");
    });
  }

  @Test
  public void testForm() throws Exception {
    this.withResourceDxl("/dxl/testHtmlRendering", database -> {
      final Document doc = database.queryFormula("$TITLE='RenderForm'", null, null, null, Collections.singleton(DocumentClass.FORM))
          .getDocuments()
          .findFirst()
          .orElseThrow(() -> new IllegalStateException("Unable to find RenderForm form"));

      final RichTextHTMLConverter conv = database.getParentDominoClient().getRichTextHtmlConverter();

      final String html = conv.render(doc)
          .convert()
          .getHtml();
      Assertions.assertNotNull(html, "html should not be null");
      Assertions.assertFalse(StringUtil.isEmpty(html), "html should not be empty");
      Assertions.assertTrue(html.contains("Render form - "), "HTML should contain expected content");
    });
  }

  @Test
  public void testHTMLRenderingItem() throws Exception {
    this.withResourceDxl("/dxl/testHtmlRendering", database -> {
      final Document doc = database.createDocument().replaceItemValue("Form", "RenderForm");

      final String uuidInBodyRT = UUID.randomUUID().toString();
      final String uuidInForm = "d35f528e-8cfc-4d97-9078-428da6673f51";

      final String itemName = "body";

      try (InputStream in = this.getClass().getResourceAsStream("/images/file-icon.gif");
          RichTextWriter rtWriter = doc.createRichTextItem(itemName);) {
        if (in == null) {
          throw new IllegalStateException("Default icon file not found");
        }

        final TextStyle textStyle = rtWriter.createTextStyle("test");
        final FontStyle fontStyle = rtWriter.createFontStyle().setBold(true);

        rtWriter.addText("Hello world. " + uuidInBodyRT, textStyle, fontStyle);
        rtWriter.addImage(in);
      }

      final DominoClient client = this.getClient();
      final RichTextHTMLConverter rtConverter = client.getRichTextHtmlConverter();

      final Collection<String> convOptions = Arrays.asList(
          HtmlConvertOption.FontConversion.toOption("1"),
          HtmlConvertOption.ForceOutlineExpand.toOption("1"),
          HtmlConvertOption.ForceSectionExpand.toOption("1"));

      {
        final HtmlConversionResult convResult = rtConverter.renderItem(doc, "body")
            .options(convOptions)
            .convert();

        final String html = convResult.getHtml();
        // System.out.println("Single item:\n"+convResult.getHTML());

        Assertions.assertTrue(html.indexOf(uuidInBodyRT) != -1);
        Assertions.assertTrue(html.indexOf(uuidInForm) == -1);
        Assertions.assertEquals(1, convResult.getImages().size());

        final EmbeddedImage img = convResult.getImages().get(0);

        final AtomicBoolean sizeGreaterZero = new AtomicBoolean();
        final AtomicBoolean anyImgData = new AtomicBoolean();

        rtConverter.readEmbeddedImage(doc, itemName, convOptions, img.getItemIndex(), img.getItemOffset(),
            new HTMLImageReader() {

              @Override
              public Action read(final byte[] data) {
                if (data != null && data.length > 0) {
                  anyImgData.set(Boolean.TRUE);
                  return Action.Stop;
                } else {
                  return Action.Continue;
                }
              }

              @Override
              public int setSize(final int size) {
                if (size > 0) {
                  sizeGreaterZero.set(Boolean.TRUE);
                }
                return 0;
              }
            });

        Assertions.assertTrue(sizeGreaterZero.get());
        Assertions.assertTrue(anyImgData.get());
      }

      // System.out.println("*************************");

      {
        final HtmlConversionResult convResult = rtConverter.render(doc)
            .options(convOptions)
            .convert();

        final String html = convResult.getHtml();
        // System.out.println("Whole doc with form:\n"+convResult.getHTML());

        Assertions.assertTrue(html.indexOf(uuidInBodyRT) != -1);
        Assertions.assertTrue(html.indexOf(uuidInForm) != -1);
        Assertions.assertEquals(1, convResult.getImages().size());
      }
    });
  }

  @Test
  public void testOption() throws Exception {
    this.withResourceDxl("/dxl/testHtmlRendering", database -> {
      final Document doc = database
          .queryFormula("Form='Multiline'", null, null, null, Collections.singleton(DocumentClass.DOCUMENT))
          .getDocuments()
          .findFirst()
          .orElseThrow(() -> new IllegalStateException("Unable to find Multiline document"));

      final RichTextHTMLConverter conv = database.getParentDominoClient().getRichTextHtmlConverter();

      {
        final String html = conv.render(doc)
            .option(XMLCompatibleHTML, "0")
            .convert()
            .getHtml();
        Assertions.assertNotNull(html, "html should not be null");
        Assertions.assertFalse(StringUtil.isEmpty(html), "html should not be empty");
        Assertions.assertTrue(html.contains("<br>"), "HTML should contain expected content");
      }
      {
        final String html = conv.render(doc)
            .option(XMLCompatibleHTML, "1")
            .convert()
            .getHtml();
        Assertions.assertNotNull(html, "html should not be null");
        Assertions.assertFalse(StringUtil.isEmpty(html), "html should not be empty");
        Assertions.assertTrue(html.contains("<br />"), "HTML should contain expected content");
      }
    });
  }

  @Test
  public void testStringOption() throws Exception {
    this.withResourceDxl("/dxl/testHtmlRendering", database -> {
      final Document doc = database
          .queryFormula("Form='Multiline'", null, null, null, Collections.singleton(DocumentClass.DOCUMENT))
          .getDocuments()
          .findFirst()
          .orElseThrow(() -> new IllegalStateException("Unable to find Multiline document"));

      final RichTextHTMLConverter conv = database.getParentDominoClient().getRichTextHtmlConverter();

      {
        final String html = conv.render(doc)
            .option("XMLCompatibleHTML", "0")
            .convert()
            .getHtml();
        Assertions.assertNotNull(html, "html should not be null");
        Assertions.assertFalse(StringUtil.isEmpty(html), "html should not be empty");
        Assertions.assertTrue(html.contains("<br>"), "HTML should contain expected content");
      }
      {
        final String html = conv.render(doc)
            .option("XMLCompatibleHTML", "1")
            .convert()
            .getHtml();
        Assertions.assertNotNull(html, "html should not be null");
        Assertions.assertFalse(StringUtil.isEmpty(html), "html should not be empty");
        Assertions.assertTrue(html.contains("<br />"), "HTML should contain expected content");
      }
    });
  }

  @Test
  public void testStringOptions() throws Exception {
    this.withResourceDxl("/dxl/testHtmlRendering", database -> {
      final Document doc = database
          .queryFormula("Form='Multiline'", null, null, null, Collections.singleton(DocumentClass.DOCUMENT))
          .getDocuments()
          .findFirst()
          .orElseThrow(() -> new IllegalStateException("Unable to find Multiline document"));

      final RichTextHTMLConverter conv = database.getParentDominoClient().getRichTextHtmlConverter();

      {
        final String html = conv.render(doc)
            .options(Collections.singleton("XMLCompatibleHTML=0"))
            .convert()
            .getHtml();
        Assertions.assertNotNull(html, "html should not be null");
        Assertions.assertFalse(StringUtil.isEmpty(html), "html should not be empty");
        Assertions.assertTrue(html.contains("<br>"), "HTML should contain expected content");
      }
      {
        final String html = conv.render(doc)
            .options(Collections.singleton("XMLCompatibleHTML=1"))
            .convert()
            .getHtml();
        Assertions.assertNotNull(html, "html should not be null");
        Assertions.assertFalse(StringUtil.isEmpty(html), "html should not be empty");
        Assertions.assertTrue(html.contains("<br />"), "HTML should contain expected content");
      }
    });
  }
}
