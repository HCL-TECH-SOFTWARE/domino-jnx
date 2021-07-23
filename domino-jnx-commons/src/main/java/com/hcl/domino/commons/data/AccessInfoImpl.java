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
package com.hcl.domino.commons.data;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.hcl.domino.data.Database.AccessInfo;
import com.hcl.domino.security.AclFlag;
import com.hcl.domino.security.AclLevel;

/**
 * @since 1.0.18
 */
public class AccessInfoImpl implements AccessInfo {
  private final AclLevel aclLevel;
  private final Set<AclFlag> aclFlags;

  public AccessInfoImpl(final AclLevel aclLevel, final Collection<AclFlag> aclFlags) {
    this.aclLevel = aclLevel;
    this.aclFlags = aclFlags == null ? Collections.emptySet() : Collections.unmodifiableSet(EnumSet.copyOf(aclFlags));
  }

  @Override
  public Set<AclFlag> getAclFlags() {
    return this.aclFlags;
  }

  @Override
  public AclLevel getAclLevel() {
    return this.aclLevel;
  }

  @Override
  public String toString() {
    return String.format("AccessInfoImpl [aclLevel=%s, aclFlags=%s]", this.aclLevel, this.aclFlags); //$NON-NLS-1$
  }
}