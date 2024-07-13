/*
 * ==========================================================================
 * Copyright (C) 2024 HCL America, Inc. ( http://www.hcl.com/ )
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
package com.hcl.maven;

import org.apache.maven.model.Dependency;

public class DependencyInfo {
    final String groupId;
    final String artifactId;
    final String updatedArtifactId;

    public DependencyInfo(String groupId, String artifactId, String updatedArtifactId) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.updatedArtifactId = updatedArtifactId;
    }

    public String getKey() {
        return groupId + ":" + artifactId;
    }

    public boolean updateDependency(Dependency dependency) {
        if (dependency.getArtifactId().equals(artifactId)) {
            dependency.setArtifactId(updatedArtifactId);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + " -> " + updatedArtifactId;
    }
}
