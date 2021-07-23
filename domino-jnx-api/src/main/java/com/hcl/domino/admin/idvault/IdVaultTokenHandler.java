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
package com.hcl.domino.admin.idvault;

import java.util.Optional;

import com.hcl.domino.security.UserTokenHandler;

/**
 * This service interface represents a provider that is able to process a token
 * supplied to {@link IdVault#getUserIdWithToken(Object, String)} and retrieve
 * a {@link UserId} implementation.
 * 
 * @param <T> the class consumed by this handler
 * @author Jesse Gallagher
 * @since 1.0.19
 */
public interface IdVaultTokenHandler<T> extends UserTokenHandler<T> {
	/**
	 * Processes the provided token object to look up a {@link UserId} representation.
	 * 
	 * <p>This method is potentially called when {@link #canProcess(Object)} returns {@code true}
	 * and should either:</p>
	 * 
	 * <ol>
	 *   <li>Return an {@code Optional} describing a user ID if found,</li>
	 *   <li>Return an empty optional if the token was processable but no matching user exists (e.g. if
	 *     this object represents one of many potential directories), or</li>
	 *   <li>Throw an exception if the token was invalid or otherwise unprocessable.</li>
	 * </ol>
	 * 
	 * @param token the token to process
	 * @param serverName the name of the server to look up on (may be ignored by implementations)
	 * @param contextIdVault the {@link IdVault} instance calling the method
	 * @return an {@link Optional} describing a {@link UserId}, or an empty one if a matching user cannot be located
	 */
	Optional<UserId> getUserId(T token, String serverName, IdVault contextIdVault);
}
