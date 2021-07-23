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
package com.hcl.domino.jna.mq;

import com.hcl.domino.commons.errors.INotesErrorConstants;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.jna.JNADominoClient;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.mq.MessageQueue;
import com.hcl.domino.mq.MessageQueues;
import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;

public class JNAMessageQueues implements MessageQueues {
	private JNADominoClient m_client;
	
	public JNAMessageQueues(JNADominoClient client) {
		m_client=client;
	}
	
	@Override
	public MessageQueue open(String queueName, boolean createOnFail) {
		Memory queueNameMem = NotesStringUtils.toLMBCS(queueName, true);

		IntByReference retQueue = new IntByReference();
		short result = NotesCAPI.get().MQOpen(queueNameMem, createOnFail ? NotesConstants.MQ_OPEN_CREATE : 0, retQueue);
		NotesErrorUtils.checkResult(result);

		return new JNAMessageQueue(this.m_client, queueName, retQueue.getValue());
	}

	@Override
	public boolean hasQueue(String queueName) {
		Memory queueNameMem = NotesStringUtils.toLMBCS(queueName, true);

		IntByReference retQueue = new IntByReference();
		
		short result = NotesCAPI.get().MQOpen(queueNameMem, 0, retQueue);
		if (result==INotesErrorConstants.ERR_NO_SUCH_MQ) {
			return false;
		}
		else if (result==0) {
			result = NotesCAPI.get().MQClose(retQueue.getValue(), 0);
			NotesErrorUtils.checkResult(result);
			return true;
		}
		else {
			NotesErrorUtils.checkResult(result);
			return false;
		}
	}

	@Override
	public MessageQueue createAndOpen(String queueName, int quota) {
		Memory queueNameMem = NotesStringUtils.toLMBCS(queueName, true);

		short result = NotesCAPI.get().MQCreate(queueNameMem, (short) (quota & 0xffff), 0);
		NotesErrorUtils.checkResult(result);

		IntByReference retQueue = new IntByReference();
		result = NotesCAPI.get().MQOpen(queueNameMem, 0, retQueue);
		NotesErrorUtils.checkResult(result);

		return new JNAMessageQueue(this.m_client, queueName, retQueue.getValue());
	}
}
