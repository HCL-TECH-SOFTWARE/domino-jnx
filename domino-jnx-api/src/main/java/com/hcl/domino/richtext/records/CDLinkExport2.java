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
package com.hcl.domino.richtext.records;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.structures.NOTELINK;
import com.hcl.domino.richtext.structures.UNID;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Karsten Lehmann
 * @since 1.0.38
 */
@StructureDefinition(
		name = "CDLINKEXPORT2",
		members = {
				@StructureMember(name = "Header", type = WSIG.class),
				@StructureMember(name = "NoteLink", type = NOTELINK.class)
				/* Now comes the variable part:             */
				/*   Null-terminated display comment        */
				/*   Null-terminated server "hint"          */
				/*   Null-terminated anchor text (optional) */
		}
		)
public interface CDLinkExport2 extends RichTextRecord<WSIG> {
	
  @Override
  @StructureGetter("Header")
	WSIG getHeader();

	@StructureGetter("NoteLink")
	NOTELINK getNoteLink();
	
	default String getReplicaId() {
		return getNoteLink().getFile().toReplicaId();
	}

	default CDLinkExport2 setReplicaId(String replicaId) {
		getNoteLink().getFile().setFromReplicaId(replicaId);
		return this;
	}

	default CDLinkExport2 setViewUnid(String unid) {
		getNoteLink().getView().setUnid(unid);
		return this;
	}

	default String getViewUnid() {
		UNID unid = getNoteLink().getView();
		return unid.toUnidString();
	}

	default CDLinkExport2 setDocUnid(String unid) {
		getNoteLink().getNote().setUnid(unid);
		return this;
	}

	default String getDocUnid() {
		return getNoteLink().getNote().toUnidString();
	}
	
	default CDLinkExport2 setTexts(String comment, String hint, String anchor) {
		final byte[] lmbcsComment = (comment==null || comment.length()==0 ?  "" : comment).getBytes(Charset.forName("LMBCS-native")); //$NON-NLS-1$ //$NON-NLS-2$
		final byte[] lmbcsHint = (hint==null || hint.length()==0 ? "" : hint).getBytes(Charset.forName("LMBCS-native")); //$NON-NLS-1$ //$NON-NLS-2$
		//anchor text is optional
		final byte[] lmbcsAnchor = anchor==null || anchor.length()==0 ? null : anchor.getBytes(Charset.forName("LMBCS-native")); //$NON-NLS-1$
		
		int totalLen = lmbcsComment.length + 1 + lmbcsHint.length + 1;
		if (lmbcsAnchor!=null) {
			totalLen += lmbcsAnchor.length + 1;
		}
		
		this.resizeVariableData(totalLen);
		
		final ByteBuffer buf = this.getVariableData();
		buf.put(lmbcsComment);
		buf.put((byte) 0);
		buf.put(lmbcsHint);
		buf.put((byte) 0);
		
		if (lmbcsAnchor!=null) {
			buf.put(lmbcsAnchor);
			buf.put((byte) 0);
		}
		return this;
	}
	
	default String[] getTexts() {
		final ByteBuffer buf = this.getVariableData();
		final int len = buf.remaining();
		final byte[] lmbcs = new byte[len];
		buf.get(lmbcs);
		
		int idxCommentEnd=-1;
		int idxHintEnd=-1;
		int idxAnchorEnd=-1;
		
		int i;
		for (i=0; i<lmbcs.length; i++) {
			if (lmbcs[i] == 0) {
				idxCommentEnd = i;
				break;
			}
 		}
		i++;
		for (; i<lmbcs.length; i++) {
			if (lmbcs[i] == 0) {
				idxHintEnd = i;
				break;
			}
		}
		i++;
		for (; i<lmbcs.length; i++) {
			if (lmbcs[i] == 0) {
				idxAnchorEnd = i;
				break;
			}
		}
		
		String comment = idxCommentEnd==-1 ? "" : new String(lmbcs, 0, idxCommentEnd, Charset.forName("LMBCS-native")); //$NON-NLS-1$ //$NON-NLS-2$
		String hint = idxHintEnd==-1 ? "" : new String(lmbcs, idxCommentEnd+1, idxHintEnd - idxCommentEnd-1, Charset.forName("LMBCS-native")); //$NON-NLS-1$ //$NON-NLS-2$
		String anchor = idxAnchorEnd==-1 ? "" : new String(lmbcs, idxHintEnd+1, idxAnchorEnd - idxHintEnd-1, Charset.forName("LMBCS-native")); //$NON-NLS-1$ //$NON-NLS-2$
		
		return new String[] {comment, hint, anchor};
	}
	
	default String getComment() {
		return getTexts()[0];
	}
	
	default String getHint() {
		return getTexts()[1];
	}
	
	default String getAnchor() {
		return getTexts()[2];
	}
	
	default CDLinkExport2 setComment(String comment) {
		String[] texts  = getTexts();
		setTexts(comment, texts[1], texts[2]);
		return this;
	}
	
	default CDLinkExport2 setHint(String hint) {
		String[] texts  = getTexts();
		setTexts(texts[0], hint, texts[2]);
		return this;
	}
	
	default CDLinkExport2 setAnchor(String anchor) {
		String[] texts  = getTexts();
		setTexts(texts[0], texts[1], anchor);
		return this;
	}
}
