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

package com.gmles.simpleorm.common.relation;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gmles.simpleorm.common.DBClass;
import com.gmles.simpleorm.common.entity.IPersistent;
import com.gmles.simpleorm.config.Configuration;
import com.gmles.simpleorm.config.TreeWriter;
import com.gmles.simpleorm.exceptions.PersistentClassFormatException;
import com.gmles.simpleorm.exceptions.PersistentConfigurationException;


public class ManyToManyRelation extends Relation {

	private static Logger LOG = Logger.getLogger(ManyToManyRelation.class.getName());
	
	private boolean called;
	private boolean calledCreate;

	protected ManyToManyRelation(String relationName, DBClass classA,
			PropertyDescriptor classAProperty) throws IntrospectionException, PersistentClassFormatException {
		super(relationName, classA, classAProperty);
	}

	public String getRelationTableName() {
		
		return relationName + "_" + classA.getTableName() + "_" + classB.getTableName();
	}

	@Override
	public String getDropStatement(DBClass root) {
		if (called)
			return "";
		this.called = true;
		return "DROP TABLE IF EXISTS " + getRelationTableName();
	}


	@Override
	public String getCreateStatement(DBClass root) throws PersistentConfigurationException {
		if (calledCreate)
			return "";
		this.calledCreate = true;
		String r = "CREATE TABLE " + getRelationTableName() + "(";
		String type = classA.getConfig().getDialect().mapLong();
		r += classAColumnName() + " " + type
				+ ", ";
		r += classBColumnName() + " " + type
				+ ")";

		return r;
	}

	@Override
	public String getAlterStatement(DBClass root) {
		return "";
	}

	@Override
	public <T extends IPersistent> void save(DBClass root, T obj, Connection c, boolean deep) throws SQLException {
		try {
			PropertyDescriptor property = relatedProperty(root);
			if(property==null || property.getReadMethod()==null)return;
			@SuppressWarnings("unchecked")
			Collection<? extends IPersistent> allRelated = (Collection<? extends IPersistent>) property.getReadMethod().invoke(obj);
			//TODO factor out root.getTableName
			String query = "DELETE FROM " + getRelationTableName() + " WHERE " + relationName + "_" + root.getTableName() + "=" + obj.getPersistentID() ;
			Statement s = null;
			if(deep || allRelated!=null){
				s = c.createStatement();
				//LOG.info(query);
				s.executeUpdate(query);
			}
			
			if (allRelated == null)
				return;

			for (IPersistent related : allRelated) {

				if (related == null){
					LOG.warning("null entry in collection '"+property.getName()+"' in '"+root.getTableName()+"'");
					continue;
				}
				if(deep)relatedClass(root).save(related, c,this, true);
				if(related.getPersistentID()!=null){
				query = "INSERT INTO " + getRelationTableName() + " VALUES (" + obj.getPersistentID() + ", " + related.getPersistentID() + ")"; 

				s = c.createStatement();
				//LOG.info(query);
				s.executeUpdate(query);
				}else{
					LOG.warning("Couldn't save id for '"+relatedClass(root).getTableName()+"' while saving +'"+root.getTableName()+"'; id was null.");
				}
			}

		} catch (IllegalArgumentException e) {
			LOG.log(Level.SEVERE,"Error saving relation",e);
		} catch (IllegalAccessException e) {
			LOG.log(Level.SEVERE,"Error saving relation",e);
		} catch (InvocationTargetException e) {
			LOG.log(Level.SEVERE,"Error saving relation",e);
		}
		
	}

	@Override
	public String classAColumnName() {
		return relationName + "_" + classA.getTableName();
	}

	@Override
	public String classBColumnName() {
		return relationName + "_" + classB.getTableName();
	}

	@Override
	public void relate(IPersistent objectA,IPersistent objectB, ResultSet results) {
		collectionRelate(this.classAProperty,objectA, objectB);
		collectionRelate(this.classBProperty, objectB, objectA);
	}


	@Override
	public TreeWriter getGen() {
		TreeWriter t = new TreeWriter(getRelationTableName(),true,true);
		t.addChild(new TreeWriter(classAColumnName()));
		t.addChild(new TreeWriter(classBColumnName()));
		return t;
	}

	@Override
	public boolean containedBy(DBClass dbClass) {
		// Always in own table
		return false;
	}

	@Override
	public String buildLeftJoin(DBClass source) {
		DBClass other = source == classA? classB: classA;
		if (true)
			return "";
		String r = " " + other.buildLoadString();
		r += " LEFT JOIN " + this.getRelationTableName() + 
			" ON " + other.getTableName() + "." + other.getIDColumn().getQualifiedName() + "=" + this.getRelationTableName()+ "."+ (other == classA ? classAColumnName() : classBColumnName());
		
		return r;
	}

}
