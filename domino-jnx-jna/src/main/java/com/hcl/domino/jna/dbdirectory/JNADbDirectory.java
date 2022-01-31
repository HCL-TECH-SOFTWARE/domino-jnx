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
package com.hcl.domino.jna.dbdirectory;

import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.dbdirectory.DbDirectory;
import com.hcl.domino.dbdirectory.DirEntry;
import com.hcl.domino.dbdirectory.DirectorySearchQuery;
import com.hcl.domino.dbdirectory.DirectorySearchQuery.SearchFlag;
import com.hcl.domino.dbdirectory.FileType;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.internal.gc.allocations.JNADbDirectoryAllocations;

/**
 * This class scans a local or remote Domino data directory for subdirectories or databases (and other files) of
 * various kinds.
 * 
 * @author Tammo Riedinger
 */
public class JNADbDirectory extends BaseJNAAPIObject<JNADbDirectoryAllocations> implements DbDirectory {
	
	public JNADbDirectory(IAPIObject<?> parent) {
		super(parent);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected JNADbDirectoryAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		return new JNADbDirectoryAllocations(parentDominoClient, parentAllocations, this, queue);
	}
		
	
	@Override
	public List<DirEntry> listFiles(String server) {
		return listFiles(server, ""); //$NON-NLS-1$
	}
	
	@Override
	public List<DirEntry> listFiles(String server, String directory) {
		return listFiles(server, directory, null);
	}
	
	@Override
	public List<DirEntry> listFiles(String server, String directory, String formula) {
		final ArrayList<DirEntry> lookupResult = new ArrayList<>();
		
		DirectorySearchQuery query=query().withServer(server)
				.withDirectory(directory)
				.withFormula(formula)
				.withFlags(EnumSet.of(SearchFlag.FILETYPE, SearchFlag.SUMMARY))
				.withFileTypes(EnumSet.of(FileType.DIRS));
		
		query.forEach(0, -1, (entry, l) -> lookupResult.add(entry));
			
		return lookupResult;
	}
	
	@Override
	public DirectorySearchQuery query() {
		return new JNADirectorySearchQuery(getParentDominoClient());
	}
}
