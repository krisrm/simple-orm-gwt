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
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gmles.simpleorm.common.DBClass;
import com.gmles.simpleorm.common.entity.IPersistent;
import com.gmles.simpleorm.config.Configuration;
import com.gmles.simpleorm.config.TreeWriter;
import com.gmles.simpleorm.exceptions.PersistentClassFormatException;
import com.gmles.simpleorm.exceptions.PersistentConfigurationException;

public class OneToOneRelation extends Relation {

	private static Logger LOG = Logger.getLogger(OneToOneRelation.class
			.getName());

	private boolean called;

	protected OneToOneRelation(String relationName, DBClass classA,
			PropertyDescriptor classAProperty) throws IntrospectionException, PersistentClassFormatException {
		super(relationName, classA, classAProperty);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getDropStatement(DBClass root) {
		return "";
	}

	@Override
	public String getCreateStatement(DBClass root) {
		return "";
	}

	@Override
	public String getAlterStatement(DBClass root)
			throws PersistentConfigurationException {
		if (called)
			return "";
		this.called = true;
		return "ALTER TABLE " + classB.getTableName() + " ADD " + relationName
				+ "_" + classA.getTableName() + " "
				+ classA.getConfig().getDialect().mapLong();
	}

	@Override
	public <T extends IPersistent> void save(DBClass root, T obj, Connection c,
			boolean deep) throws SQLException {
		try {
			PropertyDescriptor prop = relatedProperty(root);
			if (prop == null)
				return;
			Method m = prop.getReadMethod();
			IPersistent related = (IPersistent) m.invoke(obj);
			if (related == null) {
				if (deep) {
					String query = "UPDATE " + classB.getTableName() + " SET "
							+ classAColumnName() + "= NULL WHERE "
							+ classAColumnName() + "=" + obj.getPersistentID();
					Statement s = c.createStatement();
					//LOG.info(query);
					s.executeUpdate(query);
				}
				return;
			}

			if (deep)
				relatedClass(root).save(related, c, this, true);
			String idcol = classB.getIDColumn().getQualifiedName();

			if (obj.getPersistentID() != null) {
				String query = "UPDATE " + classB.getTableName() + " SET "
						+ classAColumnName() + "=" + obj.getPersistentID()
						+ " WHERE " + idcol + "=" + related.getPersistentID();

				Statement s = c.createStatement();
				//LOG.info(query);
				s.executeUpdate(query);
			} else {
				LOG.warning("Couldn't save id for '"
						+ relatedClass(root).getTableName()
						+ "' while saving +'" + root.getTableName()
						+ "'; id was null.");
			}

		} catch (IllegalArgumentException e) {
			LOG.log(Level.SEVERE, "Error saving relation", e);
		} catch (IllegalAccessException e) {
			LOG.log(Level.SEVERE, "Error saving relation", e);
		} catch (InvocationTargetException e) {
			LOG.log(Level.SEVERE, "Error saving relation", e);
		}

	}

	@Override
	public String classAColumnName() {
		return relationName + "_" + classA.getTableName();
	}

	@Override
	public String classBColumnName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void relate(IPersistent objectA, IPersistent objectB,
			ResultSet results) {
		fieldRelate(this.classAProperty, objectA, objectB);
		fieldRelate(this.classBProperty, objectB, objectA);
	}

	@Override
	public TreeWriter getGen() {
		return new TreeWriter(classAColumnName());
	}

	@Override
	public boolean containedBy(DBClass dbClass) {
		// TODO Auto-generated method stub
		return dbClass.equals(this.classB);
	}

	@Override
	public String buildLeftJoin(DBClass source) {
		DBClass other = classA == source? classB: classA;
		
		return " LEFT JOIN " + other.getTableName() + " ON "
				+ other.getTableName() + "."
				+ other.getIDColumn().getQualifiedName() + "="
				+ source.getTableName() + "."
				+ source.getIDColumn().getQualifiedName() + " "
				+ other.buildLoadString();
	}

}
