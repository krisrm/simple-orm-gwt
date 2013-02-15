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
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gmles.simpleorm.Util;
import com.gmles.simpleorm.common.DBClass;
import com.gmles.simpleorm.common.entity.IPersistent;
import com.gmles.simpleorm.common.entity.annotations.ManyToMany;
import com.gmles.simpleorm.common.entity.annotations.OneToMany;
import com.gmles.simpleorm.common.entity.annotations.OneToOne;
import com.gmles.simpleorm.config.TreeWriter;
import com.gmles.simpleorm.exceptions.PersistentClassFormatException;
import com.gmles.simpleorm.exceptions.PersistentConfigurationException;

public abstract class Relation {

	private static Logger LOG = Logger.getLogger(Relation.class.getName());
	
	private static Set<Relation> allRelations = new HashSet<Relation>();

	protected String relationName;
	protected DBClass classA;
	protected DBClass classB;
	protected PropertyDescriptor classAProperty;
	protected PropertyDescriptor classBProperty;

	protected boolean used;
	
	@SuppressWarnings("unchecked")
	protected Relation(String relationName, DBClass classA, PropertyDescriptor classAProperty) throws IntrospectionException, PersistentClassFormatException {
		this.relationName = relationName;
		this.classA = classA;
			this.classB = DBClass.getDBClass((Class<? extends IPersistent>) Util
					.extractCollection(classAProperty));
		classA.getRelations().add(this);
		classB.getRelations().add(this);
		this.classAProperty = classAProperty;
	}
	
	public static Relation createRelation(String name, DBClass dbclass, PropertyDescriptor property) throws IntrospectionException, PersistentClassFormatException{
		Method m = property.getReadMethod();
		Relation r = null;
		if(m.getAnnotation(ManyToMany.class)!=null){
			r= new ManyToManyRelation(name,dbclass, property);
		}else if(m.getAnnotation(OneToMany.class)!=null){
			r= new OneToManyRelation(name, dbclass, property);
		}else if(m.getAnnotation(OneToOne.class)!=null){
			r= new OneToOneRelation(name, dbclass, property);
		}
		allRelations.add(r);
		return r;
	}

	public void setClassBProperty(PropertyDescriptor property){
		this.classBProperty=property;
	}
	
	public abstract TreeWriter getGen();
	
	public String toString(){
		String s = relationName + " " + classA + " - " + classB;
		return s;
	}

	public abstract String getDropStatement(DBClass dbClass) ;
	public abstract String getCreateStatement(DBClass dbClass) throws PersistentConfigurationException ;
	public abstract String getAlterStatement(DBClass dbClass) throws PersistentConfigurationException ;
	
	public abstract <T extends IPersistent> void save(DBClass root, T obj, Connection c, boolean deep) throws SQLException;

	
	protected DBClass relatedClass(DBClass root) {
		if (root == classA)
			return classB;
		if (root == classB)
			return classA;
		return null;
	}

	protected PropertyDescriptor relatedProperty(DBClass root) {
		if (root == classA){
			return classAProperty;
			}
		if (root == classB){
			return classBProperty;
		}
		return null;
	}
	
	public abstract String classAColumnName();
	public abstract String classBColumnName();

	public DBClass getClassA() {
		return classA;
	}

	public void setClassA(DBClass classA) {
		this.classA = classA;
	}

	public DBClass getClassB() {
		return classB;
	}

	public void setClassB(DBClass classB) {
		this.classB = classB;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void collectionRelate(PropertyDescriptor property,IPersistent object, IPersistent other){
		if(property!=null && object!=null && other!=null){
			try {
				Collection c = (Collection) property.getReadMethod().invoke(object);
				//TODO need to think of a better way to handle this - maybe make duplicates optional?
				if(!c.contains(other))c.add(other);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				LOG.log(Level.SEVERE,"Error relating initialized objects via collection",e);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				LOG.log(Level.SEVERE,"Error relating initialized objects via collection",e);
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				LOG.log(Level.SEVERE,"Error relating initialized objects via collection",e);
			}
			
		}
	}
	
	protected void fieldRelate(PropertyDescriptor property, IPersistent object, IPersistent other){
		try {
			if(object!=null && property!=null)property.getWriteMethod().invoke(object, other);
		
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			LOG.log(Level.SEVERE,"Error relating initialized objects",e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			LOG.log(Level.SEVERE,"Error relating initialized objects",e);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			LOG.log(Level.SEVERE,"Error relating initialized objects",e);
		}
	}

	public abstract void relate(IPersistent objectA,IPersistent objectB, ResultSet results);

	public abstract  boolean containedBy(DBClass dbClass);

	public abstract String buildLeftJoin(DBClass source);

	public Object getRelationName() {
		return this.relationName;
	}

	public void markUsedByJoin(boolean b) {
		this.used = b;
	}

	public boolean isUsedByJoin() {
		return used;
	}
	public static void markUnusedByJoin(){
		for (Relation r : allRelations)
			r.markUsedByJoin(false);
	}

	public void setClassAProperty(PropertyDescriptor property) {
		this.classAProperty = property;
	}

	

}
