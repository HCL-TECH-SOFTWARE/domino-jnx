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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

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
	public void testHTMLRenderingItem() throws Exception {
		withResourceDxl("/dxl/testHtmlRendering", (database) -> {
			Document doc = database.createDocument().replaceItemValue("Form", "RenderForm");
			
			String uuidInBodyRT = UUID.randomUUID().toString();
			String uuidInForm = "d35f528e-8cfc-4d97-9078-428da6673f51";
			
			String itemName = "body";
			
			try (InputStream in = getClass().getResourceAsStream("/images/file-icon.gif");
					RichTextWriter rtWriter = doc.createRichTextItem(itemName);) {
		        if (in == null) {
		          throw new IllegalStateException("Default icon file not found");
		        }
		        
				TextStyle textStyle = rtWriter.createTextStyle("test");
				FontStyle fontStyle = rtWriter.createFontStyle().setBold(true);

				rtWriter.addText("Hello world. "+uuidInBodyRT, textStyle, fontStyle);
				rtWriter.addImage(in);
			}

			DominoClient client = getClient();
			RichTextHTMLConverter rtConverter = client.getRichTextHtmlConverter();

			Collection<String> convOptions = Arrays.asList(
					HtmlConvertOption.FontConversion.toOption("1"),
					HtmlConvertOption.ForceOutlineExpand.toOption("1"),
					HtmlConvertOption.ForceSectionExpand.toOption("1"));
			
			{
				HtmlConversionResult convResult = rtConverter.renderItem(doc, "body")
					.options(convOptions)
					.convert();

				String html = convResult.getHtml();
//				System.out.println("Single item:\n"+convResult.getHTML());

				assertTrue(html.indexOf(uuidInBodyRT) != -1);
				assertTrue(html.indexOf(uuidInForm) == -1);
				assertEquals(1, convResult.getImages().size());
				
				EmbeddedImage img = convResult.getImages().get(0);
				
				AtomicBoolean sizeGreaterZero = new AtomicBoolean();
				AtomicBoolean anyImgData = new AtomicBoolean();
				
				rtConverter.readEmbeddedImage(doc, itemName, convOptions, img.getItemIndex(), img.getItemOffset(),
						new HTMLImageReader() {
							
							@Override
							public int setSize(int size) {
								if (size>0) {
									sizeGreaterZero.set(Boolean.TRUE);
								}
								return 0;
							}
							
							@Override
							public Action read(byte[] data) {
								if (data!=null && data.length>0) {
									anyImgData.set(Boolean.TRUE);
									return Action.Stop;
								}
								else {
									return Action.Continue;
								}
							}
						});
				
				assertTrue(sizeGreaterZero.get());
				assertTrue(anyImgData.get());
			}
			
//			System.out.println("*************************");
			
			{
				HtmlConversionResult convResult = rtConverter.render(doc)
					.options(convOptions)
					.convert();

				String html = convResult.getHtml();
//				System.out.println("Whole doc with form:\n"+convResult.getHTML());

				assertTrue(html.indexOf(uuidInBodyRT) != -1);
				assertTrue(html.indexOf(uuidInForm) != -1);
				assertEquals(1, convResult.getImages().size());
			}
		});
	}
	
	@Test
	public void testDatabase() throws Exception {
		withResourceDxl("/dxl/testHtmlRendering", (database) -> {
			RichTextHTMLConverter conv = database.getParentDominoClient().getRichTextHtmlConverter();
			
			String html = conv.render(database)
				.convert()
				.getHtml();
			assertNotNull(html, "html should not be null");
			assertFalse(StringUtil.isEmpty(html), "html should not be empty");
			assertTrue(html.contains("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\">"), "Converted HTML should use the default frameset");
		});
	}
	
	@Test
	public void testForm() throws Exception {
		withResourceDxl("/dxl/testHtmlRendering", (database) -> {
			Document doc = database.queryFormula("$TITLE='RenderForm'", null, null, null, Collections.singleton(DocumentClass.FORM))
					.getDocuments()
					.findFirst()
					.orElseThrow(() -> new IllegalStateException("Unable to find RenderForm form"));
			
			RichTextHTMLConverter conv = database.getParentDominoClient().getRichTextHtmlConverter();
			
			String html = conv.render(doc)
				.convert()
				.getHtml();
			assertNotNull(html, "html should not be null");
			assertFalse(StringUtil.isEmpty(html), "html should not be empty");
			assertTrue(html.contains("Render form - "), "HTML should contain expected content");
		});
	}
	
	@Test
	public void testOption() throws Exception {
		withResourceDxl("/dxl/testHtmlRendering", (database) -> {
			Document doc = database.queryFormula("Form='Multiline'", null, null, null, Collections.singleton(DocumentClass.DOCUMENT))
					.getDocuments()
					.findFirst()
					.orElseThrow(() -> new IllegalStateException("Unable to find Multiline document"));
			
			RichTextHTMLConverter conv = database.getParentDominoClient().getRichTextHtmlConverter();
			
			{
				String html = conv.render(doc)
					.option(XMLCompatibleHTML, "0")
					.convert()
					.getHtml();
				assertNotNull(html, "html should not be null");
				assertFalse(StringUtil.isEmpty(html), "html should not be empty");
				assertTrue(html.contains("<br>"), "HTML should contain expected content");
			}
			{
				String html = conv.render(doc)
					.option(XMLCompatibleHTML, "1")
					.convert()
					.getHtml();
				assertNotNull(html, "html should not be null");
				assertFalse(StringUtil.isEmpty(html), "html should not be empty");
				assertTrue(html.contains("<br />"), "HTML should contain expected content");
			}
		});
	}
	
	@Test
	public void testStringOption() throws Exception {
		withResourceDxl("/dxl/testHtmlRendering", (database) -> {
			Document doc = database.queryFormula("Form='Multiline'", null, null, null, Collections.singleton(DocumentClass.DOCUMENT))
					.getDocuments()
					.findFirst()
					.orElseThrow(() -> new IllegalStateException("Unable to find Multiline document"));
			
			RichTextHTMLConverter conv = database.getParentDominoClient().getRichTextHtmlConverter();
			
			{
				String html = conv.render(doc)
					.option("XMLCompatibleHTML", "0")
					.convert()
					.getHtml();
				assertNotNull(html, "html should not be null");
				assertFalse(StringUtil.isEmpty(html), "html should not be empty");
				assertTrue(html.contains("<br>"), "HTML should contain expected content");
			}
			{
				String html = conv.render(doc)
					.option("XMLCompatibleHTML", "1")
					.convert()
					.getHtml();
				assertNotNull(html, "html should not be null");
				assertFalse(StringUtil.isEmpty(html), "html should not be empty");
				assertTrue(html.contains("<br />"), "HTML should contain expected content");
			}
		});
	}
	
	@Test
	public void testStringOptions() throws Exception {
		withResourceDxl("/dxl/testHtmlRendering", (database) -> {
			Document doc = database.queryFormula("Form='Multiline'", null, null, null, Collections.singleton(DocumentClass.DOCUMENT))
					.getDocuments()
					.findFirst()
					.orElseThrow(() -> new IllegalStateException("Unable to find Multiline document"));
			
			RichTextHTMLConverter conv = database.getParentDominoClient().getRichTextHtmlConverter();
			
			{
				String html = conv.render(doc)
					.options(Collections.singleton("XMLCompatibleHTML=0"))
					.convert()
					.getHtml();
				assertNotNull(html, "html should not be null");
				assertFalse(StringUtil.isEmpty(html), "html should not be empty");
				assertTrue(html.contains("<br>"), "HTML should contain expected content");
			}
			{
				String html = conv.render(doc)
					.options(Collections.singleton("XMLCompatibleHTML=1"))
					.convert()
					.getHtml();
				assertNotNull(html, "html should not be null");
				assertFalse(StringUtil.isEmpty(html), "html should not be empty");
				assertTrue(html.contains("<br />"), "HTML should contain expected content");
			}
		});
	}
}
