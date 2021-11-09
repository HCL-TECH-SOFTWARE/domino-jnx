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
package com.hcl.domino.commons.design;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.hcl.domino.commons.design.agent.JavaAgentAndLibrarySupport;
import com.hcl.domino.data.Document;
import com.hcl.domino.design.JavaLibrary;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public class JavaLibraryImpl extends AbstractScriptLibrary<JavaLibrary> implements JavaLibrary {
  private JavaAgentAndLibrarySupport designSupport;

  public JavaLibraryImpl(final Document doc) {
    super(doc);
    this.designSupport = new JavaAgentAndLibrarySupport(this);
  }

  @Override
  public JavaLibrary initJavaContent() {
    designSupport.initJavaContent();
    return this;
  }

  @Override
  public String getCodeFilesystemPath() {
    return designSupport.getCodeFilesystemPath();
  }

  @Override
  public JavaLibrary setCodeFilesystemPath(String path) {
    designSupport.setCodeFilesystemPath(path);
    return this;
  }

  @Override
  public List<String> getEmbeddedJarNames() {
    return designSupport.getEmbeddedJarNames();
  }

  @Override
  public JavaLibrary setEmbeddedJars(Map<String, InputStream> embeddedJars) {
    designSupport.setEmbeddedJars(embeddedJars);
    return this;
  }

  @Override
  public JavaLibrary setEmbeddedJar(String fileName, InputStream in) {
    designSupport.setEmbeddedJar(fileName, in);
    return this;
  }

  @Override
  public JavaLibrary removeEmbeddedJar(String fileNameToRemove) {
    designSupport.removeEmbeddedJar(fileNameToRemove);
    return this;
  }

  @Override
  public JavaLibrary setSourceAttachment(InputStream in) {
    designSupport.setSourceAttachment(in);
    return this;
  }

  @Override
  public JavaLibrary setObjectAttachment(InputStream in) {
    designSupport.setObjectAttachment(in);
    return this;
  }

  @Override
  public JavaLibrary setResourceAttachment(InputStream in) {
    designSupport.setResourceAttachment(in);
    return this;
  }

  @Override
  public Optional<InputStream> getEmbeddedJar(String name) {
    return designSupport.getEmbeddedJar(name);
  }

  @Override
  public String getMainClassName() {
    return designSupport.getMainClassName();
  }

  @Override
  public JavaLibrary setMainClassName(String name) {
    designSupport.setMainClassName(name);
    return this;
  }

  @Override
  public Optional<String> getObjectAttachmentName() {
   return designSupport.getObjectAttachmentName();
  }

  @Override
  public Optional<InputStream> getObjectAttachment() {
    return designSupport.getObjectAttachment();
  }

  @Override
  public Optional<String> getResourcesAttachmentName() {
    return designSupport.getResourcesAttachmentName();
  }
  
  @Override
  public Optional<InputStream> getResourcesAttachment() {
    return designSupport.getResourcesAttachment();
  }

  @Override
  public List<String> getSharedLibraryList() {
    return designSupport.getSharedLibraryList();
  }

  @Override
  public JavaLibrary setSharedLibraryList(List<String> libs) {
    designSupport.setSharedLibraryList(libs);
    return this;
  }

  @Override
  public Optional<String> getSourceAttachmentName() {
    return designSupport.getSourceAttachmentName();
  }

  @Override
  public Optional<InputStream> getSourceAttachment() {
   return designSupport.getSourceAttachment();
  }

}
