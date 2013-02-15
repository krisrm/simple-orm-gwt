/**
 * @author GML Enterprise Solutions
 * Copyright 2011 GML Enterprise Solutions
 * 
 * This file is part of SimpleORM.
 *
 * SimpleORM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *   
 * SimpleORM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SimpleORM.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.gmles.simpleorm.dialects;


public class PGSQL extends DatabaseDialect{


	@Override
	public String mapString(Integer size) {
		if (size == null || size <= 0)
			return "text";
		return "varchar("+size+")";
	}


	@Override
	public String mapBoolean() {
		return "boolean";
	}


	@Override
	public String mapInteger() {
		return "integer";
	}


	@Override
	public String mapShort() {
		return "smallint";
	}


	@Override
	public String mapLong() {
		return "bigint";
	}


	@Override
	public String mapChar() {
		return "char(1)";
	}


	@Override
	public String mapFloat() {
		return "real";
	}


	@Override
	public String mapDouble() {
		return "double precision";
	}


	@Override
	public String mapByteArray(Integer maxSize) {
		if (maxSize == null || maxSize <=0)
			return "bit varying(16384)";
		//multiply by 8 because of bits
		return "bit varying("+maxSize*8+")";
	}


	@Override
	public String mapDate() {
		return "DATE";
	}


	@Override
	public String mapIdType() {
		return "BIGSERIAL";
	}


	@Override
	public String mapEnum() {
		return "INTEGER";
	}


	@Override
	public String mapBigDecimal() {
		return "NUMERIC";
	}


	@Override
	public String mapTimestamp() {
		return "TIMESTAMP";
	}

}
