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
package com.hcl.domino.jna.internal.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoException;
import com.hcl.domino.commons.mime.MimeBodyPartAttachment;
import com.hcl.domino.commons.org.apache.commons.mail.EmailAttachment;
import com.hcl.domino.commons.org.apache.commons.mail.EmailException;
import com.hcl.domino.commons.org.apache.commons.mail.HtmlEmail;
import com.hcl.domino.commons.util.MimeTypes;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentValueConverter;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.mime.MimeData;
import com.hcl.domino.mime.MimeReader;
import com.hcl.domino.mime.MimeReader.ReadMimeDataType;
import com.hcl.domino.mime.MimeWriter.WriteMimeDataType;
import com.hcl.domino.mime.attachments.IMimeAttachment;

import jakarta.activation.DataSource;
import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;

/**
 * Converter to read and write {@link MimeData} from/to document items
 * via the MIME stream C API.
 * 
 * @author Karsten Lehmann
 */
public class MimeDataDocumentValueConverter implements DocumentValueConverter {

	@Override
	public boolean supportsRead(Class<?> valueType) {
		return MimeData.class.isAssignableFrom(valueType);
	}

	@Override
	public boolean supportsWrite(Class<?> valueType, Object value) {
		return MimeData.class.isAssignableFrom(valueType);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(Document doc, String itemName, Class<T> valueType, T defaultValue) {
		if (!doc.hasItem(itemName)) {
			return defaultValue;
		}
		
		MimeReader mimeReader = doc.getParentDatabase().getParentDominoClient().getMimeReader();

		try {
			MimeMessage mimeMessage = mimeReader.readMIME(doc, itemName,
					EnumSet.of(ReadMimeDataType.MIMEHEADERS));

			MimeData mimeData = new MimeData();
			populateMIMEData(mimeMessage, mimeData);
			
			return (T) mimeData;
		} catch (IOException e) {
			Database db = doc.getParentDatabase();
			throw new DominoException(MessageFormat.format("Error reading MIMEData from item {0} of document with UNID {1} in database {2}!!{3}",
					itemName, doc.getUNID(), db.getServer(), db.getRelativeFilePath()), e);
		} catch (MessagingException e) {
			Database db = doc.getParentDatabase();
			throw new DominoException(MessageFormat.format("Error reading MIMEData from item {0} of document with UNID {1} in database {2}!!{3}",
					itemName, doc.getUNID(), db.getServer(), db.getRelativeFilePath()), e);
		}
	}

	/**
	 * Recursively traverse the MIME structure reading HTML/plaintext
	 * content and information about inlines/attachments
	 * 
	 * @param content return value of {@link MimeMessage#getContent()}
	 * @param retMimeData {@link MimeData} to populate with html/plaintext/inlines/attachments
	 * @throws MessagingException for errors parsing the MIME data
	 * @throws IOException for general I/O errors
	 */
	private void populateMIMEData(Object content, MimeData retMimeData) throws MessagingException, IOException {
		if (content==null) {
			return;
		}
		
		if (content instanceof Multipart) {
			Multipart multipart = (Multipart) content;

			for (int i=0; i<multipart.getCount(); i++) {
				BodyPart currBodyPart = multipart.getBodyPart(i);

				populateMIMEData(currBodyPart, retMimeData);	
			}
		}
		else if (content instanceof Part) {
			Part part = (Part) content;
			
			String disposition = part.getDisposition();
			
			if (part.isMimeType("text/html") //$NON-NLS-1$
					&& (StringUtil.isEmpty(disposition) || "inline".equals(disposition))) { //$NON-NLS-1$
				Object htmlContent = part.getContent();
				if (htmlContent instanceof String) {
					retMimeData.setHtml((String) htmlContent);
				}
				return;
			}
			else if (part.isMimeType("text/plain") //$NON-NLS-1$
					&& (StringUtil.isEmpty(disposition) || "inline".equals(disposition))) { //$NON-NLS-1$
				Object textContent = part.getContent();
				if (textContent instanceof String) {
					retMimeData.setPlainText((String) textContent);
				}
				return;
			}
			
			if (!part.isMimeType("multipart/related") && //$NON-NLS-1$
					!part.isMimeType("multipart/mixed") && //$NON-NLS-1$
					!part.isMimeType("multipart/alternative")) { //$NON-NLS-1$
				//either inline file or attachment
				MimeBodyPartAttachment mimeAtt = new MimeBodyPartAttachment(part);

				String[] currContentIdArr = part.getHeader("Content-ID"); //$NON-NLS-1$
				String currContentId = currContentIdArr!=null && currContentIdArr.length>0 ? currContentIdArr[0] : null;

				if (StringUtil.isEmpty(currContentId)) {
					retMimeData.attach(mimeAtt);
				}
				else {
					if (currContentId.startsWith("<")) { //$NON-NLS-1$
						currContentId = currContentId.substring(1);
					}
					if (currContentId.endsWith(">")) { //$NON-NLS-1$
						currContentId = currContentId.substring(0, currContentId.length()-1);
					}
					
					retMimeData.embed(currContentId, mimeAtt);
				}
			}
			else {
				Object bodyContent = part.getContent();
				if (bodyContent!=null) {
					populateMIMEData(bodyContent, retMimeData);
				}
			}
		}
		else if (content instanceof InputStream) {
			((InputStream)content).close();
		}
	}

	@Override
	public <T> List<T> getValueAsList(Document doc, String itemName, Class<T> valueType, List<T> defaultValue) {
		T val = getValue(doc, itemName, valueType, null);
		if (val==null) {
			return defaultValue;
		}
		else {
			return Arrays.asList(val);
		}
	}

	@Override
	public <T> void setValue(Document doc, Set<ItemFlag> itemFlags, String itemName, T newValue) {
		MimeData mimeData = (MimeData) newValue;

		String html = mimeData.getHtml();
		String text = mimeData.getPlainText();


		try {
			HtmlEmail mail = new HtmlEmail();

			//add some required fields required by Apache Commons Email (will not be written to the doc)
			mail.setFrom("mr.sender@acme.com", "Mr. Sender"); //$NON-NLS-1$ //$NON-NLS-2$
			mail.addTo("mr.receiver@acme.com", "Mr. Receiver"); //$NON-NLS-1$ //$NON-NLS-2$
			mail.setHostName("acme.com"); //$NON-NLS-1$

			mail.setCharset("UTF-8"); //$NON-NLS-1$

			//add embeds
			for (String currCID : mimeData.getContentIds()) {
				Optional<IMimeAttachment> currAtt = mimeData.getEmbed(currCID);
				if(currAtt.isPresent()) {
					MimeAttachmentDataSource dataSource = new MimeAttachmentDataSource(currAtt.get());
					mail.embed(dataSource, currAtt.get().getFileName(), currCID);
				}
			}
			
			//add attachments
			for (IMimeAttachment currAtt : mimeData.getAttachments()) {
				MimeAttachmentDataSource dataSource = new MimeAttachmentDataSource(currAtt);

				mail.attach(dataSource, currAtt.getFileName(), null, EmailAttachment.ATTACHMENT);
			}

			if (!StringUtil.isEmpty(text)) {
				mail.setTextMsg(text);
			}
			mail.setHtmlMsg(html);

			mail.buildMimeMessage();
			MimeMessage mimeMsg = mail.getMimeMessage();
			
			DominoClient client = doc.getParentDatabase().getParentDominoClient();

			while (doc.hasItem(itemName)) {
				doc.removeItem(itemName);
			}

			client.getMimeWriter().writeMime(doc, itemName, mimeMsg, EnumSet.of(WriteMimeDataType.BODY));

		} catch (EmailException | IOException | MessagingException e) {
			Database db = doc.getParentDatabase();
			throw new DominoException(MessageFormat.format(
					"Error writing MIME content to item {0} of document with UNID {1} of database {2}!!{3}", itemName,
					doc.getUNID(), db.getServer(), db.getRelativeFilePath()), e);
		}
	}

	/**
	 * Implementation of {@link DataSource} that reads its data from
	 * a {@link IMimeAttachment}.
	 * 
	 * @author Karsten Lehmann
	 */
	private static class MimeAttachmentDataSource implements DataSource {
		private IMimeAttachment m_att;

		public MimeAttachmentDataSource(IMimeAttachment att) {
			m_att = att;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return m_att.getInputStream();
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			throw new IOException("cannot do this");
		}

		@Override
		public String getContentType() {
			try {
				String contentType = m_att.getContentType();
				
				if (StringUtil.isEmpty(contentType)) {
					String fileName = getName();
					if (!StringUtil.isEmpty(fileName)) {
						contentType = MimeTypes.getMimeType(fileName);
					}
				}
				
				if (StringUtil.isEmpty(contentType)) {
					contentType = "application/octet-stream"; //$NON-NLS-1$
				}
				
				return contentType;
				
			} catch (IOException e) {
				throw new DominoException("Error reading content type from MIME attachment", e);
			}
		}

		@Override
		public String getName() {
			try {
				return m_att.getFileName();
			} catch (IOException e) {
				throw new DominoException("Error reading content type from MIME attachment", e);
			}
		}

	}
}
