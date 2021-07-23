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
package com.hcl.domino.jnx.example.resources;

import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;

import com.hcl.domino.DominoClient;
import com.hcl.domino.dbdirectory.DirEntry;
import com.hcl.domino.dbdirectory.DirectorySearchQuery.SearchFlag;
import com.hcl.domino.dbdirectory.FileType;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("listFiles")
public class ListFilesResource {
	@Inject
	private DominoClient client;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<Node> get(
		@QueryParam("serverName") String serverName
	) {
		Node rootNode = new Node(""); //$NON-NLS-1$
		client.openDbDirectory()
			.query()
			.withServer(serverName)
			.withFileTypes(EnumSet.of(FileType.RECURSE, FileType.DBREPL))
			.withFlags(EnumSet.noneOf(SearchFlag.class))
			.stream()
			.forEach(entry -> add(rootNode, entry));
		return rootNode.children;
	}
	
	public static enum NodeType {
		DB, DIR
	}
	public static class Node {
		private final String name;
		private final String title;
		private final NodeType type;
		private final String filePath;
		private final String template;
		private final Map<String, Object> properties;
		private Collection<Node> children = new TreeSet<>(Comparator.comparing(Node::getName));
		
		public Node(String name) {
			this.name = name;
			this.type = NodeType.DIR;
			this.title = null;
			this.filePath = null;
			this.properties = null;
			this.template = null;
		}
		public Node(DirEntry dirEntry) {
			this.name = dirEntry.getFileName();
			this.type = NodeType.DB;
			String info = (String)dirEntry.getProperties().get("$Info"); //$NON-NLS-1$
			String title = ""; //$NON-NLS-1$
			String template = ""; //$NON-NLS-1$
			StringTokenizer tokenizer = new StringTokenizer(info, "\n"); //$NON-NLS-1$
			if(tokenizer.hasMoreTokens()) {
				title = tokenizer.nextToken();
			}
			if(tokenizer.hasMoreTokens()) {
				template = tokenizer.nextToken();
				if(template.startsWith("#")) { //$NON-NLS-1$
					template = template.substring(2);
				} else {
					template = ""; //$NON-NLS-1$
				}
			}
			this.title = title;
			this.template = template;
			this.filePath = dirEntry.getPhysicalFilePath();
			this.properties = dirEntry.getProperties();
		}
		
		public String getName() {
			return name;
		}
		public NodeType getType() {
			return type;
		}
		public String getTitle() {
			return title;
		}
		public String getFilePath() {
			return filePath;
		}
		public String getTemplate() {
			return template;
		}
		public Map<String, Object> getProperties() {
			return properties;
		}
		public Collection<Node> getChildren() {
			return children;
		}
	}
	
	public static void add(Node rootNode, DirEntry dirEntry) {
		String normalizedName = String.valueOf(dirEntry.getProperties().get("$Path")).replace('\\', '/'); //$NON-NLS-1$
		Node target = rootNode;
		String[] parts = normalizedName.split("\\/"); //$NON-NLS-1$
		for(int i = 0; i < parts.length-1; i++) {
			int fi = i;
			Node searching = target;
			target = searching.children.stream()
				.filter(n -> n.name.equals(parts[fi]))
				.findFirst()
				.orElseGet(() -> {
					Node newNode = new Node(parts[fi]);
					searching.children.add(newNode);
					return newNode;
				});
		}
		
		String title = (String)dirEntry.getProperties().get("$Info"); //$NON-NLS-1$
		if(title != null && !title.isEmpty()) {
			int lineIndex = title.indexOf('\n');
			if(lineIndex > 0) {
				title = title.substring(0, lineIndex);
			}
		}
		
		target.children.add(new Node(dirEntry));
	}
}
