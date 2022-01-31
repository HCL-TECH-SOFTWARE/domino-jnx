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
package com.hcl.domino.commons.design.agent;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.hcl.domino.commons.design.AbstractDesignAgentImpl;
import com.hcl.domino.data.Document;
import com.hcl.domino.design.agent.DesignJavaAgent;

/**
 * Implementation of {@link DesignJavaAgent}
 */
public class DesignJavaAgentImpl extends AbstractDesignAgentImpl<DesignJavaAgent> implements DesignJavaAgent {
  private JavaAgentAndLibrarySupport designSupport;
  
  public DesignJavaAgentImpl(Document doc) {
    super(doc);
    this.designSupport = new JavaAgentAndLibrarySupport(this);
  }

  @Override
  public void setJavaCompilerSource(String target) {
    getDocument().replaceItemValue("$JavaCompilerSource", target); //$NON-NLS-1$
  }

  @Override
  public String getJavaCompilerSource() {
    return getDocument().get("$JavaCompilerSource", String.class, "");  //$NON-NLS-1$ //$NON-NLS-2$
  }
  
  @Override
  public void setJavaCompilerTarget(String target) {
    getDocument().replaceItemValue("$JavaCompilerTarget", target); //$NON-NLS-1$
  }
  
  @Override
  public String getJavaCompilerTarget() {
    return getDocument().get("$JavaCompilerTarget", String.class, ""); //$NON-NLS-1$ //$NON-NLS-2$
  }

  @Override
  public DesignJavaAgent initJavaContent() {
    designSupport.initJavaContent();
    return this;
  }

  @Override
  public String getCodeFilesystemPath() {
    return designSupport.getCodeFilesystemPath();
  }
  
  @Override
  public DesignJavaAgent setCodeFilesystemPath(String path) {
    designSupport.setCodeFilesystemPath(path);
    return this;
  }
  
  @Override
  public List<String> getEmbeddedJarNames() {
    return designSupport.getEmbeddedJarNames();
  }
  
  @Override
  public DesignJavaAgent setEmbeddedJars(Map<String, InputStream> embeddedJars) {
    designSupport.setEmbeddedJars(embeddedJars);
    return this;
  }
  
  @Override
  public DesignJavaAgent setEmbeddedJar(String fileName, InputStream in) {
    designSupport.setEmbeddedJar(fileName, in);
    return this;
  }
  
  @Override
  public DesignJavaAgent removeEmbeddedJar(String fileNameToRemove) {
    designSupport.removeEmbeddedJar(fileNameToRemove);
    return this;
  }
  
  @Override
  public DesignJavaAgent setSourceAttachment(InputStream in) {
    designSupport.setSourceAttachment(in);
    return this;
  }
  
  @Override
  public DesignJavaAgent setObjectAttachment(InputStream in) {
    designSupport.setObjectAttachment(in);
    return this;
  }
  
  @Override
  public DesignJavaAgent setResourceAttachment(InputStream in) {
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
  public DesignJavaAgent setMainClassName(String name) {
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
  public DesignJavaAgent setSharedLibraryList(List<String> libs) {
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
  
  @Override
  public boolean isCompileDebug() {
    return designSupport.isCompileDebug();
  }
  
  @Override
  public DesignJavaAgent setCompileDebug(boolean debug) {
    designSupport.setCompileDebug(debug);
    return this;
  }
}
