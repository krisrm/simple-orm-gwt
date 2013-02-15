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

import java.beans.PropertyDescriptor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

import com.gmles.simpleorm.Util;
import com.gmles.simpleorm.common.entity.IPersistent;
import com.gmles.simpleorm.common.entity.annotations.ColumnConstraint;
import com.gmles.simpleorm.config.Configuration;
import com.gmles.simpleorm.config.TreeWriter;
import com.gmles.simpleorm.dialects.DatabaseDialect;
import com.gmles.simpleorm.exceptions.PersistentConfigurationException;

public class PrimitiveCollection {

	private static Logger LOG = Logger.getLogger(PrimitiveCollection.class.getName());
	private PropertyAccessor property;
	private DBClass parent;
	private String idCol;
	private String valCol;
	private String indexCol;
	private ColumnConstraint constraint;
	private String name;
	String colStart;

	public PrimitiveCollection(DBClass parent, PropertyDescriptor property) throws PersistentConfigurationException {
		this.parent = parent;
		this.property = new CollectionAccessor(property);
		this.name=property.getName();
		this.constraint=property.getReadMethod().getAnnotation(ColumnConstraint.class);
		colStart = getTableName()+parent.getConfig().separator();
		idCol = colStart+"id ";
		indexCol = colStart+"index ";
		valCol = colStart+"val ";
	}
	
	
	public void save(IPersistent obj, Connection c) throws SQLException {
		if (obj == null) // this shouldn't happen...
			return;
		
		Collection<?> col = null;
		col = (Collection<?>) property.get(obj);
		if (col == null){
			//nothing to save, and don't touch
			return;
		}
		
		String deleteQ = "DELETE FROM " + getTableName() + " WHERE " + idCol + "=" + obj.getPersistentID();
		c.createStatement().executeUpdate(deleteQ);
		
		if (col.isEmpty()){
			//drop everything and continue; the collection has been cleared in memory
			return;
		}
		String insertQ = "INSERT INTO "+getTableName() + " VALUES ";
		
		for (int i = 0; i < col.size(); i++){
			insertQ += "(?, ?, ?), ";
		}
		
		insertQ = Util.trimTrailingComma(insertQ);		
		//LOG.info(insertQ);
		PreparedStatement p = c.prepareStatement(insertQ);
		int pos =1;
		int i = 1;
		for (Object o : col){
			p.setLong(pos, obj.getPersistentID());
			p.setLong(pos+1, i);
			p.setObject(pos+2, o);
			pos+=3;
			i++;
		}
		
		p.executeUpdate();
	}

	public String getDropStatement() {
		return "DROP TABLE IF EXISTS "+ this.getTableName();
	}
	public String getCreateStatement() throws PersistentConfigurationException {
		
		String r = "CREATE TABLE "+getTableName() +" (";
		DatabaseDialect dialect = parent.getConfig().getDialect();
		r+=idCol + dialect.mapIdType() + ", ";
		r+=indexCol + dialect.mapLong() + ", ";
		r+=valCol + mapPrimitiveType();
		r+=")";
		return r;
	}


	private String getTableName() {
		return "COL_" +parent.getTableName() + "_"+this.name ;
	}

	
	private String mapPrimitiveType() throws PersistentConfigurationException {
		DatabaseDialect dialect = parent.getConfig().getDialect();
		Class<?> c = property.getComponentType();
		return dialect.getMap(c, constraint);
	}


	@SuppressWarnings("unchecked")
	public void initialize(Map<Long, Object> value, ResultSet r, IPersistent obj) throws SQLException, PersistentConfigurationException {
		Long index = null;
		Object val = null;

		ResultSetMetaData rsm = r.getMetaData();
		for (int i = 1; i <= rsm.getColumnCount(); i++) {
			String cname = parent.parseColumnName(rsm.getColumnName(i));
			if (this.getTableName()
					.equalsIgnoreCase(parent.parseTableName(rsm.getColumnName(i)))){

				if(cname.equalsIgnoreCase("index")){
					index = (Long) r.getObject(i);
				}else if(cname.equalsIgnoreCase("val")){
					val = r.getObject(i);
				}
			}
		}
		if(index!=null){
			Collection<Object> c = (Collection<Object>) this.property.get(obj);
			if(value.containsKey(index))return;
			value.put(index, val);
			if(c!=null)c.add(val);
		}
	}


	public TreeWriter getGen() {
		TreeWriter d = new TreeWriter(getTableName());
		
		TreeWriter id = new TreeWriter("id",colStart+"id");
		d.addChild(id);
		
		TreeWriter index = new TreeWriter("index",colStart+"index");
		d.addChild(index);
		
		TreeWriter value = new TreeWriter("val",colStart+"val");
		d.addChild(value);
		return d;
	}


	public String buildLeftJoin() {
		return " LEFT JOIN " + this.getTableName() + " ON " + this.getTableName() + "." + this.idCol + "=" +parent.getTableName() + "." + parent.getIDColumn().getQualifiedName();
	}
}
