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
package com.hcl.domino.data;

import com.hcl.domino.misc.INumberEnum;

/**
 * Represents the class of a database, which affects its storage characteristics and
 * on-disk structure.
 * 
 * @author Jesse Gallagher
 */
public enum DatabaseClass implements INumberEnum<Short> {
	/** The type of the database is determined by the filename extension.
	 * The extensions and their database classes are .NSX (NSFTESTFILE), .NSF (NOTEFILE),
	 * .DSK (DESKTOP), .NCF (NOTECLIPBOARD), .NTF (TEMPLATEFILE), .NSG (GIANTNOTEFILE),
	 * .NSH (HUGENOTEFILE), NTD (ONEDOCFILE), NS2 (V2NOTEFILE), NTM (ENCAPSMAILFILE). */
	BY_EXTENSION((short) 0),
	/** A test database */
	NSFTESTFILE((short) 0xff00),
	/** A standard Domino database */
	NOTEFILE((short) 0xff01),
	/** A Notes desktop (folders, icons, etc.) */
	DESKTOP((short) 0xff02),
	/** A Notes clipboard (used for cutting and pasting) */
	NOTECLIPBOARD((short) 0xff03),
	/** A database that contains every type of note (forms, views, ACL, icon, etc.) except data notes */
	TEMPLATEFILE((short) 0xff04),
	/** A standard Domino database, with size up to 1 GB. This was used in Notes Release 3 when the size of a previous version of a database had been limited to 200 MB */
	GIANTNOTEFILE((short) 0xff05),
	/** A standard Domino database, with size up to 1 GB. This was used in Notes Release 3 when the size of a previous version of a database had been limited to 300 MB */
	HUGENOTEFILE((short) 0xff06),
	/** One document database with size up to 10MB. Specifically used by alternate
	 * mail to create an encapsulated database. Components of the document are
	 * further limited in size. It is not recommended that you use this database
	 * class with NSFDbCreate. If you do, and you get an error when saving the
	 * document, you will need to re-create the database using DBCLASS_NOTEFILE */
	ONEDOCFILE((short) 0xff07),
	/** Database was created as a Notes Release 2 database */
	V2NOTEFILE((short) 0xff08),
	/** One document database with size up to 5MB. Specifically used by alternate
	 * mail to create an encapsulated database. Components of the document are
	 * further limited in size. It is not recommended that you use this database
	 * class with NSFDbCreate. If you do, and you get an error when saving the
	 * document, you will need to re-create the database using DBCLASS_NOTEFILE */
	ENCAPSMAILFILE((short) 0xff09),
	/** Specifically used by alternate mail. Not recomended for use with NSFDbCreate */
	LRGENCAPSMAILFILE((short) 0xff0a),
	/** Database was created as a Notes Release 3 database. */
	V3NOTEFILE((short) 0xff0b),
	/** Object store */
	OBJSTORE((short) 0xff0c),
	/** One document database with size up to 10MB. Specifically used by Notes
	 * Release 3 alternate mail to create an encapsulated database. Not
	 * recomended for use with NSFDbCreate */
	V3ONEDOCFILE((short) 0xff0d),
	/** Database was created specifically for Domino and Notes Release 4 */
	V4NOTEFILE((short) 0xff0e),
	/** Database was created specifically for Domino and Notes Release 5 */
	V5NOTEFILE((short) 0xff0f),
	/** Database was created specifically for Domino and Notes Release Notes/Domino 6 */
	V6NOTEFILE((short) 0xff10),
	/** Database was created specifically for Domino and Notes Release Notes/Domino 8 */
	V8NOTEFILE((short) 0xff11),
	/** Database was created specifically for Domino and Notes Release Notes/Domino 8.5 */
	V85NOTEFILE((short) 0xff12),
	/** Database was created specifically for Domino and Notes Release Notes/Domino 9 */
	V9NOTEFILE((short) 0xff13),
	/** Database was created specifically for Domino and Notes Release Notes/Domino 10 */
	V10NOTEFILE((short) 0xff14),
	/** Database was created specifically for Domino and Notes Release Notes/Domino 12 */
	V12NOTEFILE((short) 0xff15)
	;
	
	private final short value;
	
	DatabaseClass(short value) {
		this.value = value;
	}

	@Override
	public Short getValue() {
		return value;
	}

	@Override
	public long getLongValue() {
		return value;
	}

}
