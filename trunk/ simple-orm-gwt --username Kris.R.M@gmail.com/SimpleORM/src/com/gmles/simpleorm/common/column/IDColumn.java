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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.gmles.simpleorm.common.DBClass;
import com.gmles.simpleorm.common.IDAccessor;
import com.gmles.simpleorm.common.entity.IPersistent;
import com.gmles.simpleorm.config.Configuration;

public class IDColumn extends Column{

	public IDColumn(DBClass parent) throws SecurityException, NoSuchMethodException {
		super(parent,new IDAccessor());
	}

	@Override
	public String getName() {
		return "PersistentID";
	}
	
	@Override
	public String getColumnString() {
		return getQualifiedName() + " " + parent.getConfig().getDialect().mapIdType();
	}
	
	@Override
	public void initialize(ResultSet r, Object object) throws SQLException {
		ResultSetMetaData rsm = r.getMetaData();
		for(int i=1; i<=rsm.getColumnCount();i++){
			if(this.parent.getTableName().equalsIgnoreCase(parent.parseTableName(rsm.getColumnName(i))) && this.getQualifiedName().equalsIgnoreCase(rsm.getColumnName(i))){
				((IPersistent) object).setPersistentID(r.getLong(i));
				return;
			}
		}
	}



}
