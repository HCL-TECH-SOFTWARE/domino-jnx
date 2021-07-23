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
package com.hcl.domino.jna.data;

import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.hcl.domino.DominoException;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.data.Document.IAttachmentProducer;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNADatabaseAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.HANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.ShortByReference;

/**
 * Utility class to create database objects with binary data
 * 
 * @author Karsten Lehmann
 */
public class JNADatabaseObjectProducer {
	/** found out via trial and error; 0xFFFFFFFF minus some header */
	private static final long MAX_OBJECTSIZE = 4294966925L; // 0xFFFFFE8D
	
	private JNADatabaseObjectProducer() {
	}
	
	public static class ObjectInfo {
		private long objectSize;
		private int objectId;
		
		public long getObjectSize() {
			return objectSize;
		}
		
		public void setObjectSize(long objectSize) {
			this.objectSize = objectSize;
		}
		
		public int getObjectId() {
			return objectId;
		}
		
		public void setObjectId(int objectId) {
			this.objectId = objectId;
		}
		
	}
	
	/**
	 * Creates a new object in the database. The file content is produced on-the-fly
	 * in an {@link IAttachmentProducer}.
	 * 
	 * @param db parent database
	 * @param noteClass class of note to create
	 * @param objectType object type, e.g. {@link NotesConstants#OBJECT_FILE}
	 * @param producer interface to produce the object data on-the-fly
	 * @return database object size and ID (RRV)
	 */
	public static ObjectInfo createDbObject(JNADatabase db, short noteClass, short objectType, IAttachmentProducer producer) {
		JNADatabaseAllocations dbAllocations = (JNADatabaseAllocations) db.getAdapter(APIObjectAllocations.class);
		dbAllocations.checkDisposed();
		
		//use a default initial object size of 1000 bytes if nothing is specified
		long estimatedSize = producer.getSizeEstimation();
		
		final long initialObjectSize;
		if (estimatedSize > MAX_OBJECTSIZE) {
			initialObjectSize = MAX_OBJECTSIZE;
		}
		else if (estimatedSize < 1) {
			initialObjectSize = 1000000;
		}
		else {
			initialObjectSize = estimatedSize;
		}
		
		//in-memory buffer to collect data from the producer
		final int copyBufferSize = 5000000;
		final byte[] buffer = new byte[copyBufferSize];
		final AtomicInteger currBufferOffset = new AtomicInteger(0);

		final AtomicLong currFileSize = new AtomicLong(0);

		//allocate memory buffer used to transfer written data to the NSF binary object
		final DHANDLE.ByReference retCopyBufferHandle = DHANDLE.newInstanceByReference();
		short result = Mem.OSMemAlloc((short) 0, copyBufferSize, retCopyBufferHandle);
		NotesErrorUtils.checkResult(result);

		return LockUtil.lockHandles(dbAllocations.getDBHandle(), retCopyBufferHandle,
				(dbHandleByVal, retBufferHandleByVal) -> {
					try {
						final IntByReference rtnRRV = new IntByReference();

						//allocate binary object with initial size
						short privs = 0;
						
						short allocObjResult = NotesCAPI.get().NSFDbAllocObjectExtended2(dbHandleByVal,
								(int) (initialObjectSize & 0xffffffff),
								noteClass, privs, objectType, rtnRRV);
						NotesErrorUtils.checkResult(allocObjResult);

						try {
							try (OutputStream nsfObjectOutputStream = new OutputStream() {

								@Override
								public void write(int b) throws IOException {

									//write byte value at current buffer array position
									int iCurrBufferOffset = currBufferOffset.get();
									buffer[iCurrBufferOffset] = (byte) (b & 0xff);

									//check if buffer full
									if ((iCurrBufferOffset+1) == copyBufferSize) {
										//check if we need to grow the NSF object
										long newObjectSize = currFileSize.get() + copyBufferSize;
										if (newObjectSize > initialObjectSize) {
											int newRRV = resizeObjectWithData(dbHandleByVal, rtnRRV.getValue(),
													noteClass, privs, objectType, newObjectSize);
											
											if (newRRV != rtnRRV.getValue()) {
												//remove current object
												short freeObjResult = NotesCAPI.get().NSFDbFreeObject(dbHandleByVal, rtnRRV.getValue());
												NotesErrorUtils.checkResult(freeObjResult);

												rtnRRV.setValue(newRRV);
											}
										}

										//copy buffer array data into memory buffer
										Pointer ptrBuffer = Mem.OSLockObject(retBufferHandleByVal);
										try {
											ptrBuffer.write(0, buffer, 0, copyBufferSize);
										}
										finally {
											Mem.OSUnlockObject(retBufferHandleByVal);
										}

										//write memory buffer to NSF object
										short result = NotesCAPI.get().NSFDbWriteObject(
												dbHandleByVal,
												rtnRRV.getValue(),
												retBufferHandleByVal,
												(int) (currFileSize.get() & 0xffffffff),
												copyBufferSize);
										NotesErrorUtils.checkResult(result);

										//increment NSF object offset by bufferSize
										currFileSize.addAndGet(copyBufferSize);
										//reset currBufferOffset
										currBufferOffset.set(0);
									}
									else {
										//buffer not full yet
										
										//increment buffer offset
										currBufferOffset.incrementAndGet();
									}
								}

							}) {
								
								producer.produceAttachment(nsfObjectOutputStream);
								
							}
							
							long finalFileSize;
							int iCurrBufferOffset = currBufferOffset.get();
							if (iCurrBufferOffset>0) {
								//we need to write the remaining buffer data to the NSF object
								
								//set the correct total filesize
								finalFileSize = currFileSize.get() + iCurrBufferOffset;
								int newRRV = resizeObjectWithData(dbHandleByVal, rtnRRV.getValue(),
										noteClass, privs, objectType, finalFileSize);

								if (newRRV != rtnRRV.getValue()) {
									//remove current object
									short freeObjResult = NotesCAPI.get().NSFDbFreeObject(dbHandleByVal, rtnRRV.getValue());
									NotesErrorUtils.checkResult(freeObjResult);

									rtnRRV.setValue(newRRV);
								}

								//copy buffer array data into memory buffer
								Pointer ptrBuffer = Mem.OSLockObject(retBufferHandleByVal);
								try {
									ptrBuffer.write(0, buffer, 0, iCurrBufferOffset);
								}
								finally {
									Mem.OSUnlockObject(retBufferHandleByVal);
								}
								
								//write memory buffer to NSF object
								short writeObjResult = NotesCAPI.get().NSFDbWriteObject(
										dbHandleByVal,
										rtnRRV.getValue(),
										retBufferHandleByVal,
										(int) (currFileSize.get() & 0xffffffff),
										iCurrBufferOffset);
								NotesErrorUtils.checkResult(writeObjResult);

								currFileSize.set(finalFileSize);
							}
							else if (initialObjectSize != currFileSize.get()) {
								//shrink data object to the actual size
								finalFileSize = currFileSize.get();
								
								//make sure the object has the right size
								int newRRV = resizeObjectWithData(dbHandleByVal, rtnRRV.getValue(),
										noteClass, privs, objectType, currFileSize.get());

								if (newRRV != rtnRRV.getValue()) {
									//remove current object
									short freeObjResult = NotesCAPI.get().NSFDbFreeObject(dbHandleByVal, rtnRRV.getValue());
									NotesErrorUtils.checkResult(freeObjResult);

									rtnRRV.setValue(newRRV);
								}
							}
							else {
								finalFileSize = currFileSize.get();
							}
							
							ObjectInfo objInfo = new ObjectInfo();
							objInfo.setObjectId(rtnRRV.getValue());
							objInfo.setObjectSize(finalFileSize);
							return objInfo;

						}
						catch (Exception e) {
							//delete the object in case of errors
							short freeObjResult = NotesCAPI.get().NSFDbFreeObject(dbHandleByVal, rtnRRV.getValue());
							NotesErrorUtils.checkResult(freeObjResult);
							throw new DominoException(0, "Error creating binary NSF DB object", e);
						}

					}
					finally {
						//free copy buffer
						Mem.OSMemFree(retBufferHandleByVal);
					}
				});
	}

	/**
	 * Method to resize a database object. If the object needs to grow, we allocate a new object,
	 * transfer the data and return the new object id.
	 * 
	 * @param dbHandleByVal database handle
	 * @param objectId object ID of the existing object
	 * @param noteClass object class
	 * @param objectType object type
	 * @param newSize new object size
	 * @return new object ID (RRV); same as <code>rrv</code> if object is shrinked
	 */
	private static int resizeObjectWithData(HANDLE.ByValue dbHandleByVal, int objectId, short noteClass, short privs,
			short objectType, long newSize) {
		
		if (newSize > MAX_OBJECTSIZE) {
			throw new IllegalArgumentException(MessageFormat.format("Max DB object size exceeded ({0}>{1})",
					Long.toString(newSize), Long.toString(MAX_OBJECTSIZE)));
		}
		
		IntByReference retSize = new IntByReference();
		ShortByReference retClass = new ShortByReference();
		ShortByReference retPrivileges = new ShortByReference();
		
		//read current size of database object
		short result = NotesCAPI.get().NSFDbGetObjectSize(dbHandleByVal, objectId, objectType, retSize, retClass,
				retPrivileges);
		NotesErrorUtils.checkResult(result);
		
		long currentSize = Integer.toUnsignedLong(retSize.getValue());
		
		if (currentSize == newSize) {
			//size is ok
			return objectId;
		}
		else if (currentSize > newSize) {
			//shrink object (keeps data)
			result = NotesCAPI.get().NSFDbReallocObject(dbHandleByVal, objectId, 
					(int) (newSize & 0xffffffff));
			NotesErrorUtils.checkResult(result);
			return objectId;
		}
		else {
			//create a new object and copy the data
			IntByReference rtnNewRRV = new IntByReference();
			result = NotesCAPI.get().NSFDbAllocObjectExtended2(dbHandleByVal,
					(int) (newSize & 0xffffffff), noteClass, privs, objectType, rtnNewRRV);
			NotesErrorUtils.checkResult(result);
			
			try {
				final int copyBufferSize = 20000000;
				
				AtomicLong currOffset = new AtomicLong(0);
				
				for (; currOffset.get() < currentSize; currOffset.addAndGet(copyBufferSize)) {
					long bytesToCopy = Math.min(copyBufferSize, currentSize - currOffset.get());
					
					if (bytesToCopy > 0) {
						//handle to receive object data
						DHANDLE.ByReference retCopyBufferHandle = DHANDLE.newInstanceByReference();
						
						short readResult = NotesCAPI.get().NSFDbReadObject(
								dbHandleByVal,
								objectId,
								(int) (currOffset.get() & 0xffffffff),
								(int) (bytesToCopy & 0xffffffff),
								retCopyBufferHandle);
						NotesErrorUtils.checkResult(readResult);
						
						LockUtil.lockHandle(retCopyBufferHandle, (retCopyBufferHandleByVal) -> {
							try {
								short writeResult = NotesCAPI.get().NSFDbWriteObject(
										dbHandleByVal,
										rtnNewRRV.getValue(),
										retCopyBufferHandleByVal,
										(int) (currOffset.get() & 0xffffffff),
										(int) (bytesToCopy  &0xffffffff));
								NotesErrorUtils.checkResult(writeResult);
							}
							finally {
								Mem.OSMemFree(retCopyBufferHandleByVal);
							}
							return null;
						});
					}
				}
				
				return rtnNewRRV.getValue();
			}
			catch (Exception e) {
				//delete the object in case of errors
				short freeObjResult = NotesCAPI.get().NSFDbFreeObject(dbHandleByVal, rtnNewRRV.getValue());
				NotesErrorUtils.checkResult(freeObjResult);
				throw new DominoException(0, "Error creating binary NSF DB object", e);
			}
			
		}
	}
}
