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

package com.gmles.simpleorm.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gmles.simpleorm.common.column.Column;
import com.gmles.simpleorm.config.Configuration;
import com.gmles.simpleorm.exceptions.PersistentConfigurationException;

public abstract class Embedded {
	private static Logger LOG = Logger.getLogger(EmbeddedArray.class.getName());
	protected SQLAccessor property;
	protected String name;
	protected List<Column> columns = new ArrayList<Column>();
	protected List<Embedded> embedded = new ArrayList<Embedded>();
	protected DBClass owner;
	
	public Embedded(DBClass dbClass,SQLAccessor accessor,String name){
		this.owner=dbClass;
		this.property=accessor;
		this.name=name;
	}
	
	public void initialize(ResultSet r, Object object) throws SQLException {
		try{
			if(object==null)return;
			Object child = this.property.get(object);
			if(child==null)child = this.property.getType().newInstance();
			this.property.put(object, child);
			for(Column column: this.columns){
				column.initialize(r, child);
			}
			for(Embedded em: this.embedded){
				em.initialize(r, child);
			}
		} catch (InstantiationException e) {
			LOG.log(Level.SEVERE,"Error accessing persistent object",e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public String getQualifiedName() throws PersistentConfigurationException{
		return this.owner.getTableName()+owner.getConfig().separator()+this.name;
	}
	
	public List<Column> getAllColumns() {
		ArrayList<Column> cols = new ArrayList<Column>();
		cols.addAll(this.columns);
		for(Embedded cls: this.embedded){
			cols.addAll(cls.getAllColumns());
		}
		return cols;
	}
	
	
	public Map<String, Object> vals(Object obj){
		HashMap<String, Object> map = new HashMap<String, Object>();
		Object child = property.get(obj);
		for (Column col : columns) {
			String name = col.getQualifiedName();
			Object val = col.getProperty().get(child);
			map.put(name, val);
		}
		for(Embedded cls: embedded){
			map.putAll(cls.vals(child));
		}
		return map;
	}
}
