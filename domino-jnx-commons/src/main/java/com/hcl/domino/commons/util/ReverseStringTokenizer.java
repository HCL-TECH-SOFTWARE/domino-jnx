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
package com.hcl.domino.commons.util;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * This class acts like a {@link StringTokenizer}, e.g. it splits up a string into several parts
 * based on a delimiter, but the class returns the tokens in reverse order and supports empty tokens
 */
public class ReverseStringTokenizer {
    private String s;
    private String delim;
    private int pos = 0;
    
    /**
     * Creates a new instance
     * 
     * @param s string to be tokenized
     * @param delim delimier
     */
    public ReverseStringTokenizer(String s, String delim) {
        this.s = s;
        this.delim = delim;
        
        if ("".equals(s)) { //$NON-NLS-1$
			this.pos = -1;
		} else {
			this.pos = s.length();
		}
    }
    
    /**
     * Method to check if there are more tokens
     * 
     * @return true if there are tokens to read
     */
    public boolean hasMoreTokens() {
        return (this.pos != -1);
    }
    
    /**
     * Returns the next token
     * 
     * @return Token
     * @throws NoSuchElementException if there are no more tokens left
     */
    public String nextToken() throws NoSuchElementException {
        if (this.pos == -1) {
			throw new NoSuchElementException();
		}
        
        int nextDelimPos = this.s.lastIndexOf(this.delim, this.pos-1);
        
        if (nextDelimPos == -1) {
            String retVal = this.s.substring(0, this.pos);
            this.pos = -1;
            return retVal;
        }
        else {
        		String retVal = this.s.substring(nextDelimPos+1, this.pos);
        		this.pos = nextDelimPos;
            
            return retVal;
        }
        
    }
}