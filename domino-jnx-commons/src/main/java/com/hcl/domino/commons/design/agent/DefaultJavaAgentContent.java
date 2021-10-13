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
package com.hcl.domino.commons.design.agent;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.hcl.domino.design.DesignElement;
import com.hcl.domino.design.agent.JavaAgentContent;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public class DefaultJavaAgentContent implements JavaAgentContent {
  private final DesignElement agent;
  private final String mainClassName;
  private final String codeFilesystemPath;
  private final Optional<String> sourceAttachmentName;
  private final Optional<String> objectAttachmentName;
  private final Optional<String> resourcesAttachmentName;
  private final List<String> embeddedJars;
  private final List<String> sharedLibraryList;

  public DefaultJavaAgentContent(DesignElement agent, final String mainClassName, final String codeFilesystemPath,
      final List<String> javaClassFileList, final List<String> sharedLibraryList) {
    this.agent = agent;
    this.mainClassName = mainClassName;
    this.codeFilesystemPath = codeFilesystemPath;

    // javaClassFileList will be a list of attachment names
    if (javaClassFileList.size() > 0) {
      this.sourceAttachmentName = Optional.of(javaClassFileList.get(0));
    } else {
      this.sourceAttachmentName = Optional.empty();
    }
    if (javaClassFileList.size() > 1) {
      this.objectAttachmentName = Optional.of(javaClassFileList.get(1));
    } else {
      this.objectAttachmentName = Optional.empty();
    }
    if (javaClassFileList.size() > 2) {
      this.resourcesAttachmentName = Optional.of(javaClassFileList.get(2));
    } else {
      this.resourcesAttachmentName = Optional.empty();
    }
    if (javaClassFileList.size() > 3) {
      this.embeddedJars = Collections.unmodifiableList(javaClassFileList.subList(3, javaClassFileList.size()));
    } else {
      this.embeddedJars = Collections.emptyList();
    }

    this.sharedLibraryList = Collections.unmodifiableList(new ArrayList<>(sharedLibraryList));
  }

  @Override
  public String getCodeFilesystemPath() {
    return this.codeFilesystemPath;
  }

  @Override
  public List<String> getEmbeddedJars() {
    return this.embeddedJars;
  }
  
  @Override
  public Optional<InputStream> getEmbeddedJar(String name) {
    // Do a basic check to make sure it's in the list
    List<String> jars = this.embeddedJars;
    if(!jars.contains(name)) {
      return Optional.empty();
    }
    return agent.getDocument()
      .getAttachment(name)
      .map(t -> {
        try {
          return t.getInputStream();
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
  }

  @Override
  public String getMainClassName() {
    return this.mainClassName;
  }

  @Override
  public Optional<String> getObjectAttachmentName() {
    return this.objectAttachmentName;
  }
  
  @Override
  public Optional<InputStream> getObjectAttachment() {
    return getObjectAttachmentName()
      .flatMap(name -> agent.getDocument().getAttachment(name))
      .map(t -> {
        try {
          return t.getInputStream();
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
  }

  @Override
  public Optional<String> getResourcesAttachmentName() {
    return this.resourcesAttachmentName;
  }
  
  @Override
  public Optional<InputStream> getResourcesAttachment() {
    return getResourcesAttachmentName()
      .flatMap(name -> agent.getDocument().getAttachment(name))
      .map(t -> {
        try {
          return t.getInputStream();
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
  }

  @Override
  public List<String> getSharedLibraryList() {
    return this.sharedLibraryList;
  }

  @Override
  public Optional<String> getSourceAttachmentName() {
    return this.sourceAttachmentName;
  }
  
  @Override
  public Optional<InputStream> getSourceAttachment() {
    return getSourceAttachmentName()
      .flatMap(name -> agent.getDocument().getAttachment(name))
      .map(t -> {
        try {
          return t.getInputStream();
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
  }
}
