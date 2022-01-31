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
package com.hcl.domino.commons.admin;

import java.text.MessageFormat;
import java.util.Optional;

import com.hcl.domino.admin.idvault.IdVault;
import com.hcl.domino.admin.idvault.IdVaultTokenHandler;
import com.hcl.domino.admin.idvault.UserId;
import com.hcl.domino.misc.JNXServiceFinder;

public interface IDefaultIdVault extends IdVault {
  @SuppressWarnings("unchecked")
  @Override
  default UserId getUserIdWithToken(final Object token, final String serverName) {
    return JNXServiceFinder.findServices(IdVaultTokenHandler.class)
        .map(h -> (IdVaultTokenHandler<Object>) h)
        .filter(handler -> handler.canProcess(token))
        .map(handler -> handler.getUserId(token, serverName, this))
        .filter(Optional::isPresent)
        .findFirst()
        .map(Optional::get)
        .orElseThrow(() -> new UnsupportedOperationException(
            MessageFormat.format("No {0} implementation found to handle token of type {1}",
                IdVaultTokenHandler.class.getSimpleName(), token == null ? "null" : token.getClass().getName()) //$NON-NLS-1$
        ));
  }
}
