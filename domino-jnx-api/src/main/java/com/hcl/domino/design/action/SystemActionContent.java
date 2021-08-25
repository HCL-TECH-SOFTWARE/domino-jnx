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
package com.hcl.domino.design.action;

import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.misc.INumberEnum;

/**
 * Represents the content of a canned system action for collections
 * and forms.
 * 
 * @author Jesse Gallagher
 * @since 1.0.33
 */
public interface SystemActionContent extends ActionContent {
  enum SystemAction implements INumberEnum<Short> {
    CATEGORIZE(DesignConstants.ACTION_SYS_CMD_CATEGORIZE),
    EDIT(DesignConstants.ACTION_SYS_CMD_EDIT),
    SEND(DesignConstants.ACTION_SYS_CMD_SEND),
    FORWARD(DesignConstants.ACTION_SYS_CMD_FORWARD),
    MOVE_TO_FOLDER(DesignConstants.ACTION_SYS_CMD_MOVE_TO_FOLDER),
    REMOVE_FROM_FOLDER(DesignConstants.ACTION_SYS_CMD_REMOVE_FROM_FOLDER),
    MARK_SEL_READ(DesignConstants.ACTION_SYS_CMD_MARK_SEL_READ),
    MARK_SEL_UNREAD(DesignConstants.ACTION_SYS_CMD_MARK_SEL_UNREAD),
    OPEN_SELECTED_NEWWND(DesignConstants.ACTION_SYS_CMD_OPEN_SELECTED_NEWWND),
    FILE_PRINT(DesignConstants.ACTION_SYS_CMD_FILE_PRINT),
    DELETE(DesignConstants.ACTION_SYS_CMD_DELETE),
    INFOBOX(DesignConstants.ACTION_SYS_CMD_INFOBOX),
    CUT(DesignConstants.ACTION_SYS_CMD_CUT),
    COPY(DesignConstants.ACTION_SYS_CMD_COPY),
    COPY_LINK_DOC(DesignConstants.ACTION_SYS_CMD_COPY_LINK_DOC),
    COPY_VIEW_TABLE(DesignConstants.ACTION_SYS_CMD_COPY_VIEW_TABLE),
    PASTE(DesignConstants.ACTION_SYS_CMD_PASTE),
    OPEN_SELECTED(DesignConstants.ACTION_SYS_CMD_OPEN_SELECTED),
    BOOKMARK(DesignConstants.ACTION_SYS_CMD_BOOKMARK);
    
    private final short value;
    private SystemAction(short value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return value;
    }

    @Override
    public Short getValue() {
      return value;
    }
  }
  
  SystemAction getAction();
}
