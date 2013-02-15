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

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import com.gmles.simpleorm.common.entity.annotations.ColumnConstraint;



public abstract class DatabaseDialect{

	public abstract String mapString(Integer size);

	//primitives
	public abstract String mapBoolean();
	public abstract String mapInteger();
	public abstract String mapShort();
	public abstract String mapLong();	
	public abstract String mapChar();
	public abstract String mapFloat();
	public abstract String mapDouble();
	public abstract String mapByteArray(Integer maxSize);
	public abstract String mapEnum();
	public abstract String mapBigDecimal();
	//"complex"
	public abstract String mapDate();
	public abstract String mapTimestamp();

	public  abstract String mapIdType();

	public String getMap(Class<?> c, ColumnConstraint con){
		int size = con==null?0:con.length();
		if (c.equals(String.class)) {
			return this.mapString(size);
		}else if(c.equals(Date.class)){
			return this.mapDate();
		}else if (c.equals(Integer.class) || c.toString().equals("int")) {
			return this.mapInteger();
		} else if (c.equals(Float.class) || c.toString().equals("float")) {
			return this.mapFloat();
		} else if (c.toString().equals("char")) {
			return this.mapChar();
		} else if (c.equals(Short.class) || c.toString().equals("short")) {
			return this.mapShort();
		} else if (c.equals(Long.class) || c.toString().equals("long")) {
			return this.mapLong();
		} else if (c.equals(Double.class) || c.toString().equals("double")) {
			return this.mapDouble();
		} else if (c.equals(Boolean.class) || c.toString().equals("boolean")) {
			return this.mapBoolean();
		} else if (c.isArray()
				&& c.getComponentType().toString().equals("byte")) {
			return this.mapByteArray(size);
		} else if (c.isEnum()){
			return this.mapEnum();
		} else if (c.equals(BigDecimal.class)){
			return this.mapBigDecimal();
		} else if (c.equals(Timestamp.class)){
			return this.mapTimestamp();
		}
		return null;
	}

	
}
