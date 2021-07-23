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
package com.hcl.domino.commons.errors;

import com.hcl.domino.commons.errors.errorcodes.IAgntsErr;
import com.hcl.domino.commons.errors.errorcodes.IBsafeErr;
import com.hcl.domino.commons.errors.errorcodes.IClErr;
import com.hcl.domino.commons.errors.errorcodes.IDbdrvErr;
import com.hcl.domino.commons.errors.errorcodes.IDirErr;
import com.hcl.domino.commons.errors.errorcodes.IEventErr;
import com.hcl.domino.commons.errors.errorcodes.IFtErr;
import com.hcl.domino.commons.errors.errorcodes.IGlobalErr;
import com.hcl.domino.commons.errors.errorcodes.IHtmlErr;
import com.hcl.domino.commons.errors.errorcodes.IMailMiscErr;
import com.hcl.domino.commons.errors.errorcodes.IMiscErr;
import com.hcl.domino.commons.errors.errorcodes.INetErr;
import com.hcl.domino.commons.errors.errorcodes.INifErr;
import com.hcl.domino.commons.errors.errorcodes.INsfErr;
import com.hcl.domino.commons.errors.errorcodes.IOdsErr;
import com.hcl.domino.commons.errors.errorcodes.IOsErr;
import com.hcl.domino.commons.errors.errorcodes.IRegErr;
import com.hcl.domino.commons.errors.errorcodes.IRouteErr;
import com.hcl.domino.commons.errors.errorcodes.ISecErr;
import com.hcl.domino.commons.errors.errorcodes.ISrvErr;
import com.hcl.domino.commons.errors.errorcodes.IXmlErr;

/**
 * Notes/Domino R9 error codes and messages
 *
 * @author Karsten Lehmann
 */
public interface INotesErrorConstants extends IAgntsErr, IBsafeErr, IClErr, IDbdrvErr, IDirErr,
    IEventErr, IFtErr, IGlobalErr, IHtmlErr, IMailMiscErr, IMiscErr, INetErr, INifErr, INsfErr, IOdsErr,
    IOsErr, IRegErr, IRouteErr, ISecErr, ISrvErr, IXmlErr {

}
