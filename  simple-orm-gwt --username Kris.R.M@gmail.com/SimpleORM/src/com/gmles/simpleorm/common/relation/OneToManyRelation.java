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

public class OneToManyRelation extends Relation {

	private static Logger LOG = Logger.getLogger(OneToManyRelation.class
			.getName());

	private boolean called;

	protected OneToManyRelation(String relationName, DBClass classA,
			PropertyDescriptor classAProperty) throws IntrospectionException, PersistentClassFormatException {
		super(relationName, classA, classAProperty);
		if (!Collection.class.isAssignableFrom(this.classAProperty
				.getPropertyType())) {
			PropertyDescriptor cap = this.classAProperty;
			DBClass dba = this.classA;

			this.classAProperty = this.classBProperty;
			this.classA = this.classB;

			this.classBProperty = cap;
			this.classB = dba;
		}
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

	@SuppressWarnings("unchecked")
	public <T extends IPersistent> void save(DBClass root, T obj, Connection c,
			boolean deep) throws SQLException {
		try {
			PropertyDescriptor relatedProperty = relatedProperty(root);
			if (relatedProperty == null)
				return;
			if (Collection.class.isAssignableFrom(relatedProperty
					.getPropertyType())) {
				Collection<? extends IPersistent> allRelated = (Collection<? extends IPersistent>) relatedProperty
						.getReadMethod().invoke(obj);

				if (allRelated == null)
					return;

				String idcol = classB.getIDColumn().getQualifiedName();
				String query = "UPDATE " + classB.getTableName() + " SET "
						+ classAColumnName() + "= NULL WHERE "
						+ classAColumnName() + "=" + obj.getPersistentID();
				Statement s = c.createStatement();
				//LOG.info(query);
				s.executeUpdate(query);

				for (IPersistent related : allRelated) {

					if (related == null) {
						LOG.warning("null entry in collection '"
								+ relatedProperty.getName() + "' in '"
								+ root.getTableName() + "'");
						continue;
					}
					if (deep)
						relatedClass(root).save(related, c, this, true);
					if (related.getPersistentID() != null) {
						query = "UPDATE " + classB.getTableName() + " SET "
								+ classAColumnName() + "="
								+ obj.getPersistentID() + " WHERE " + idcol
								+ "=" + related.getPersistentID();

						s = c.createStatement();
						//LOG.info(query);
						s.executeUpdate(query);
					} else {
						LOG.warning("Couldn't save id for '"
								+ relatedClass(root).getTableName()
								+ "' while saving +'" + root.getTableName()
								+ "'; id was null.");
					}
				}
			} else {
				IPersistent related = (IPersistent) relatedProperty
						.getReadMethod().invoke(obj);
				if (related == null) {
					if (deep) {
						String query = "UPDATE " + classB.getTableName()
								+ " SET " + classAColumnName()
								+ "= NULL WHERE " + classAColumnName() + "="
								+ obj.getPersistentID();
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
							+ classAColumnName() + "="
							+ related.getPersistentID() + " WHERE " + idcol
							+ "=" + obj.getPersistentID();

					Statement s = c.createStatement();
					//LOG.info(query);
					s.executeUpdate(query);
				} else {
					LOG.warning("Couldn't save id for '"
							+ relatedClass(root).getTableName()
							+ "' while saving +'" + root.getTableName()
							+ "'; id was null.");
				}
			}

		} catch (IllegalArgumentException e) {
			LOG.log(Level.SEVERE, "Error saving relation", e);
		} catch (IllegalAccessException e) {
			LOG.log(Level.SEVERE, "Error saving relation", e);
		} catch (InvocationTargetException e) {
			LOG.log(Level.SEVERE, "Error saving relation", e);
		}

	}

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
		collectionRelate(this.classAProperty, objectA, objectB);
		fieldRelate(this.classBProperty, objectB, objectA);
	}

	@Override
	public TreeWriter getGen() {
		return new TreeWriter(classAColumnName());
	}

	@Override
	public boolean containedBy(DBClass dbClass) {
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
