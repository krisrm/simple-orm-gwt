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

package com.gmles.simpleorm.common.column;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.gmles.simpleorm.common.SQLAccessor;
import com.gmles.simpleorm.common.DBClass;
import com.gmles.simpleorm.config.Configuration;

public abstract class Column {

	protected SQLAccessor property;
	public SQLAccessor getProperty() {
		return property;
	}

	public void setProperty(SQLAccessor property) {
		this.property = property;
	}

	protected DBClass parent;

	public Column(DBClass parent, SQLAccessor property)  {
		this.parent = parent;
		this.property = property;
	}

	public DBClass getParent() {
		return parent;
	}

	public void setParent(DBClass parent) {
		this.parent = parent;
	}

//	private Method findSetter(Method getter) throws SecurityException {
//		Method setter = null;
//		try {
//			setter = parent.getMappedClass().getMethod(
//					"set" + getter.getName().substring(3),
//					getter.getReturnType());
//		} catch (NoSuchMethodException e) {
//			throw new ClassFormatError(
//					"You need to provide a setter for the property defined by "
//							+ getter.getName() + " in class " + parent.getMappedClass());
//		}
//		return setter;
//	}
	public abstract String getName();
	public String getQualifiedName(){
		return parent.getTableName()+parent.getConfig().separator()+getName();

	}
	public abstract String getColumnString();
	
	public abstract void initialize(ResultSet r, Object child) throws SQLException;

}
