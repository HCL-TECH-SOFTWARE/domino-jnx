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
package lotus.notes;

public class AgentSecurityContext {
  public AgentSecurityContext(ThreadGroup paramThreadGroup, boolean paramBoolean) {
    
  }
  
  public final boolean classLoaderAllowed() {
    return false;
  }
  
  public final boolean execAllowed() {
    return false;
  }
  
  public final boolean linkAllowed() {
    return false;
  }
  
  public final boolean netAllowed() {
    return false;
  }
  
  public final boolean propAllowed() {
    return false;
  }
  
  public final boolean readAllowed() {
    return false;
  }
  
  public final boolean writeAllowed() {
    return false;
  }
}
