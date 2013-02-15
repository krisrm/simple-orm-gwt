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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gmles.simpleorm.Util;
import com.gmles.simpleorm.common.column.Column;
import com.gmles.simpleorm.common.column.IDColumn;
import com.gmles.simpleorm.common.column.SimpleColumn;
import com.gmles.simpleorm.common.entity.IPersistent;
import com.gmles.simpleorm.common.entity.annotations.ColumnConstraint;
import com.gmles.simpleorm.common.entity.annotations.ManyToMany;
import com.gmles.simpleorm.common.entity.annotations.OneToMany;
import com.gmles.simpleorm.common.entity.annotations.OneToOne;
import com.gmles.simpleorm.common.entity.annotations.Transient;
import com.gmles.simpleorm.common.relation.Relation;
import com.gmles.simpleorm.config.Configuration;
import com.gmles.simpleorm.config.TreeWriter;
import com.gmles.simpleorm.exceptions.PersistentClassFormatException;
import com.gmles.simpleorm.exceptions.PersistentConfigurationException;
import com.gmles.simpleorm.session.Query;
import com.gmles.simpleorm.session.Transaction;

public class DBClass {

	private static Logger LOG = Logger.getLogger(DBClass.class.getName());

	private static HashMap<Class<?>, DBClass> dbclasses = new HashMap<Class<?>, DBClass>();
	private static HashMap<String, DBClass> names = new HashMap<String, DBClass>();
	private Class<?> myClass;
	private BeanInfo beanInfo;
	private Configuration config;

	public Configuration getConfig() {
		return config;
	}

	public void setConfig(Configuration config) {
		this.config = config;
	}

	public BeanInfo getBeanInfo() {
		return beanInfo;
	}

	public void setBeanInfo(BeanInfo beanInfo) {
		this.beanInfo = beanInfo;
	}

	private List<Column> columns = new ArrayList<Column>();
	private IDColumn idColumn;
	private Set<Relation> relations = new HashSet<Relation>();
	private Set<IPersistent> savedEntities = new HashSet<IPersistent>();
	private List<PrimitiveCollection> primcollections = new ArrayList<PrimitiveCollection>();

	public List<PrimitiveCollection> getPrimcollections() {
		return primcollections;
	}

	public void setPrimcollections(List<PrimitiveCollection> primcollections) {
		this.primcollections = primcollections;
	}

	private Set<Embedded> embedded = new HashSet<Embedded>();

	public Set<Relation> getRelations() {
		return relations;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public DBClass(Configuration c, Class<? extends IPersistent> mappedClass)
			throws IntrospectionException {
		this(c, mappedClass, "");
		
	}

	private DBClass(Configuration c, Class<? extends IPersistent> mappedClass,
			String columnPrefix) throws IntrospectionException {
		this.beanInfo = Introspector.getBeanInfo(mappedClass, Object.class);
		this.myClass = mappedClass;
		dbclasses.put(mappedClass, this);
		names.put(this.getTableName().toLowerCase(), this);
		this.config = c;
	}

	public void build() throws PersistentClassFormatException {
		try {
			for (PropertyDescriptor property : Arrays.asList(beanInfo
					.getPropertyDescriptors())) {
				Method method = property.getReadMethod();
				if (method == null) {
					LOG.severe("Property " + property.getName()
							+ " has no read method!");
					continue;
				}
				if (method.getAnnotation(Transient.class) == null) {
					if (method.getName().equals("getPersistentID")) {
						this.idColumn = new IDColumn(this);
						columns.add(0, idColumn);
					} else if (property.getPropertyType().isArray()) {
						//LOG.info("Creating embedded array "
						//		+ property.getName());
						Object instance = property.getReadMethod().invoke(
								myClass.newInstance());
						this.embedded.add(new EmbeddedArray(this, instance,
								new PropertyAccessor(property), property
										.getName()));
					} else if (Util.isPrimitive(method)) {
						columns.add(new SimpleColumn(this,
								new PropertyAccessor(property), method
										.getAnnotation(ColumnConstraint.class),
								property.getName()));
					} else if (Util.isPrimitiveCollection(method)) {
						primcollections.add(new PrimitiveCollection(this,
								property));
					} else if (relationName(method) != null) {
						String name = relationName(method);
						mapRelation(name, property);
					} else {
						if (config.specifyEmbedded() && method
							.getAnnotation(com.gmles.simpleorm.common.entity.annotations.Embedded.class) == null){
							LOG.info("Property " + property.getName() + " in "
									+ getTableName() + " is marked transient");
							continue;
						}
						
						if (property.getPropertyType().equals(this.myClass)) {
							LOG.severe("Type " + this.myClass.getName()
									+ " contains itself in field "
									+ property.getName());
							break;
						}
						//LOG.info("Creating embedded class "
						//		+ property.getName());
						embedded.add(new EmbeddedClass(this,
								new PropertyAccessor(property), property
								.getName()));
					}

				}
			}
			if (this.idColumn == null)
				throw new PersistentClassFormatException("The class "
						+ this.getTableName()
						+ " does not implement IPersistent");
		} catch (Exception e) {
			LOG.log(Level.SEVERE,
					"Error building DBClass '" + this.getTableName() + "'", e);
			throw new PersistentClassFormatException(e);
		}
	}

	public String relationName(Method m) {

		if (m.getAnnotation(ManyToMany.class) != null) {
			return m.getAnnotation(ManyToMany.class).name();
		} else if (m.getAnnotation(OneToMany.class) != null) {
			return m.getAnnotation(OneToMany.class).name();
		} else if (m.getAnnotation(OneToOne.class) != null) {
			return m.getAnnotation(OneToOne.class).name();
		}
		return null;
	}

	public void mapRelation(String relationName, PropertyDescriptor property)
			throws IntrospectionException, PersistentClassFormatException {
		LOG.fine("Mapping Relation in " + this.getTableName() + "."
				+ property.getName());
		
		for (Relation r : relations) {
			LOG.fine("Found relation " + r.toString());
			LOG.finest("STORED:" + r.getRelationName() + "    INCOMING R:" + relationName);
			if (r.getRelationName().equals(relationName)) {
				LOG.finest("CLASSB:" + r.getClassB() + " THIS: " +this + " 2 " + r.getClassA().getMappedClass() + " 3 " + Util.extractCollection(property));
				if (r.getClassB().equals(this)
						&& r.getClassA().getMappedClass()
								.equals(Util.extractCollection(property))) {
					// We've already created this relation from its counterpart
					// dbclass, so
					// Just complete the relation.
					LOG.fine("Binding existing relation " + r.toString());
					r.setClassBProperty(property);
					return;
				} else if (r.getClassA().equals(this)
						&& r.getClassB().getMappedClass()
						.equals(Util.extractCollection(property))) {
					// We've already created this relation from its counterpart
					// dbclass, so
					// Just complete the relation.
					LOG.fine("Binding existing relation " + r.toString());
					r.setClassAProperty(property);
					return;
				}
			}
		}
		Relation.createRelation(relationName, this, property);
	}

	public String toString() {
		return getTableName();
	}

	public void setMappedClass(Class<? extends IPersistent> myClass) {
		this.myClass = myClass;
	}

	public Class<?> getMappedClass() {
		return myClass;
	}

	public static DBClass getDBClass(Class<? extends IPersistent> key)
			throws IntrospectionException, PersistentClassFormatException {
		DBClass r = dbclasses.get(key);

		if (r == null) {
			throw new PersistentClassFormatException("Could not find class "
					+ key + " in your mapping class.");
			// r = new DBClass(key);
			// dbclasses.put(key, r);
		}

		return r;
	}

	public Column getColumn(String columnName) {
		for (Column column : columns) {
			if (column.getQualifiedName() != null
					&& column.getQualifiedName().equalsIgnoreCase(columnName))
				return column;
		}
		return null;
	}

	public IPersistent parse(ResultSet results) throws SQLException {
		IPersistent newobj = null;
		try {
			newobj = (IPersistent) getMappedClass().newInstance();
			for (Column column : this.columns) {
				column.initialize(results, newobj);
			}
			for (Embedded cls : this.embedded) {
				cls.initialize(results, newobj);
			}
		} catch (InstantiationException e) {
			LOG.log(Level.SEVERE, "Error instantiating loaded object", e);
		} catch (IllegalAccessException e) {
			LOG.log(Level.SEVERE,
					"Error accessing result-set while loading objects", e);
		}
		return newobj;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends IPersistent> List<T> parseResultSet(Transaction t, Query queries[])
			throws SQLException, PersistentConfigurationException {
		Map<DBClass, Map<Long, IPersistent>> dbClassToIDmap = new HashMap<DBClass, Map<Long, IPersistent>>();
		List<T> orderedList = new ArrayList<T>();

		String result = "||";

		for(Query query: queries){
			Map<Integer, DBClass> columnOfDBClassID = new HashMap<Integer, DBClass>();

			//LOG.info(query.getQuery());
			
			PreparedStatement statement = t.getConnection().prepareStatement(query.getQuery());
			for (int i = 1; i <= query.getObjects().length; i++) {
				statement.setObject(i, query.getObjects()[i - 1]);
			}
			ResultSet resultSet = statement.executeQuery();
			
			Map<DBClass, IPersistent> DBClassToCurrentInstance = new HashMap<DBClass, IPersistent>();
			Map<Long, Map<PrimitiveCollection, Map<Long, Object>>> collections = new HashMap<Long, Map<PrimitiveCollection, Map<Long, Object>>>();
			Set<Relation> relations = new HashSet<Relation>();
			
			ResultSetMetaData rsm = resultSet.getMetaData();

			//iterate over resultset finding all DBClasses in result by finding IDCloumns
			for (int i = 1; i <= rsm.getColumnCount(); ++i) {
				String tableName = parseTableName(rsm.getColumnName(i)
						.toLowerCase());
				result += rsm.getColumnName(i) + "||";
				DBClass d = names.get(tableName);
				if (d != null) {
					Column col = d.getColumn(rsm.getColumnName(i));
					
					if (col instanceof IDColumn) {
						
						//Found a dbclass id
						columnOfDBClassID.put(i, d);
						if(dbClassToIDmap.get(d)==null)dbClassToIDmap.put(d, new HashMap<Long, IPersistent>());
					}
				}
			}
			
			if (!dbClassToIDmap.keySet().contains(this)) {
				throw new SQLException(
						"ResultSet doesn't contain PersistentID column for "
								+ this.getTableName());
			}
			
			//build relations in order given by starting at end
			//and creating exactly one incoming edge to the next
			//nearest node in the order in the result set.
			//e.g. a man has legs, a dog has legs, a man has dogs.
			//if we load a leg, does it belong to the man or the dog?
			//we chose the nearest id to the left in the resultset for relation,
			//so man join dog join leg would give the legs to the dog,
			//while man join leg join dog would give the legs to the man.
			for(int i=rsm.getColumnCount();i>1;i--){
				DBClass d1 = columnOfDBClassID.get(i);
				if(d1==null)continue;
				for(int j=i-1;j>0;j--){
					boolean found = false;
					DBClass d2 = columnOfDBClassID.get(j);
					if(d2==null)continue;
					for(Relation r: d1.getRelations()){
						if(r.getClassA()==d2 || r.getClassB()==d2){
							relations.add(r);
							found = true;
							break;
						}
					}
					if(found)break;
				}
			}
			
			//Start building all instances
			int resultCount = 0;
			while (resultSet.next()) {
				
				//Logging data
				resultCount++;
				String s = "|";
				for (int i = 1; i <= rsm.getColumnCount(); ++i) {
					s += rsm.getColumnName(i) + ":" + resultSet.getObject(i) + "|";
				}
				//LOG.fine(s);
				
				//Clear the current map of instance list
				//(We assume at most one of any entity in each result)
				DBClassToCurrentInstance.clear();

				//Make an instance for each id that we haven't seen before
				for (Integer i : columnOfDBClassID.keySet()) {
					Long id = resultSet.getLong(i);
					if (id != null && id > 0) {
						DBClass d = columnOfDBClassID.get(i);
						
						IPersistent obj = dbClassToIDmap.get(d).get(id);
						
						if (obj == null) {
							//if we don't have this instance yet, make a new one and store it in our map of all instances
							obj = d.parse(resultSet);
							dbClassToIDmap.get(d).put(id, obj);
							
							//If we're looking at an instance that's of the type 
							//being loaded, put it in the ordered list to return.
							if (d == this){
								orderedList.add((T) obj);
							}
						}
						
						//Try to parse any primitive collections
						Map<PrimitiveCollection, Map<Long, Object>> cols = collections
								.get(id);
						if(cols==null){
							//Store a map of primitive collections for it
							cols = new HashMap<PrimitiveCollection, Map<Long, Object>>();
							collections.put(id, cols);
							for (PrimitiveCollection c : d.getPrimcollections()) {
								cols.put(c, new HashMap<Long, Object>());
							}
						}
						for (Entry<PrimitiveCollection, Map<Long, Object>> e : cols.entrySet()) {
							e.getKey().initialize(e.getValue(), resultSet, obj);
						}
						
						//Note the current instance for this DBClass
						DBClassToCurrentInstance.put(d, obj);
					}
				}

				for (Relation r : relations) {
					
					//for each relation, try to get an instance for each of
					//that relation's dbclasses, then let the relation put them
					//together properly.
					r.relate(DBClassToCurrentInstance.get(r.getClassA()),
							DBClassToCurrentInstance.get(r.getClassB()), resultSet);
				}
			}
			LOG.fine("Number of rows: " + resultCount + " Number of objects: "
					+ orderedList.size());
			//LOG.info("Result Columns: " + result);
			resultSet.close();
			statement.close();
		}
		// List<T> r = new ArrayList<T>();
		// Map<Long,IPersistent> items = instances.get(this);
		// for(Entry<Long,IPersistent> entry: items.entrySet()){
		// r.add((T) entry.getValue());
		// }
		// return r;
		for (T object : orderedList) {
			object.onLoaded();
		}
		return orderedList;
	}

	public String buildDrop() {
		return "DROP TABLE IF EXISTS " + getTableName();
	}

	public List<String> buildAllDrop(){
		List<String> r = new ArrayList<String>();
		r.add(this.buildDrop());
		r.addAll(this.buildRelationDrop());
		return r;
	}
	public List<String> buildAllCreate() throws PersistentConfigurationException{
		List<String> r = new ArrayList<String>();
		r.add(this.buildCreate());
		r.addAll(this.buildRelationCreate());
		return r;
	}

	public String buildCreate() {
		String r = "CREATE TABLE " + getTableName() + " (";
		for (Column c : columns) {
			String createStr = c.getColumnString();
			if (!createStr.isEmpty())
				r += createStr + ", ";
		}
		for (Embedded cls : embedded) {
			for (Column column : cls.getAllColumns()) {
				r += column.getColumnString() + ", ";
			}
		}
		r = trimTrailingComma(r);
		r += ", PRIMARY KEY (" + idColumn.getQualifiedName() + "))";
		return r;
	}

	public List<String> buildRelationStatements()
			throws PersistentClassFormatException,
			PersistentConfigurationException {
		List<String> r = new ArrayList<String>();

		if (columnsEmpty()) {
			// we can't actually save new objects if this is the case, since
			// we're not allowed to insert "blank" objects.
			// TODO maybe eventually add a "dummy column" that doesn't save or
			// load, but exists to put null in when saving
			throw new PersistentClassFormatException(
					"You must have at least 1 simple column in order to save an object.");
		}
		
		r.addAll(buildRelationDrop());
		r.addAll(buildRelationCreate());
		for (Relation rc : relations) {
			r.add(rc.getAlterStatement(this));
		}

		return r;
	}

	private List<String> buildRelationCreate() throws PersistentConfigurationException {
		List<String> r = new ArrayList<String>();
		for (Relation rc : relations) {
			r.add(rc.getCreateStatement(this));
		}
		for (PrimitiveCollection pc : primcollections) {
			r.add(pc.getCreateStatement());
		}
		return r;
	}
	
	public List<String> buildRelationDrop(){
		List<String> r = new ArrayList<String>();
		for (Relation rc : relations) {
			r.add(rc.getDropStatement(this));
		}
		for (PrimitiveCollection pc : primcollections) {
			r.add(pc.getDropStatement());
		}
		return r;
	}
	

	public List<String> buildUpdates(Transaction t) throws SQLException {
		List<String> r = new ArrayList<String>();
		
		
		ResultSet rs = null;
		try {
		rs = t.getConnection().createStatement().executeQuery("SELECT * FROM " + getTableName() + " WHERE false");
		} catch (SQLException e){
			//we couldn't find the table in the database; make it in full
			r.add(this.buildCreate());
			
		}
		if (rs != null){
			ResultSetMetaData rsm = rs.getMetaData();
		}
		//TODO make a list of all the columns that need adding, and make alter statements for them.
		//TODO figure out which relation columns are missing
		return r;
	}

	

	public String getTableName() {

		return myClass.getSimpleName();
	}

	public <T extends IPersistent> void saveShallow(T obj, Connection c)
			throws SQLException {
		save(obj, c, null, false);
	}

	public <T extends IPersistent> void save(T obj, Connection c)
			throws SQLException {
		save(obj, c, null, true);
	}

	public <T extends IPersistent> void save(T obj, Connection c, Relation r,
			boolean deep) throws SQLException {

		if (obj.getPersistentID() == null || obj.getPersistentID() <= 0) {
			insert(obj, c, r, deep);
		} else {
			update(obj, c, r, deep);
		}
	}

	public <T extends IPersistent> void insert(T obj, Connection c, Relation r,
			boolean deep) throws SQLException {

		String s = "";
		List<Object> values = new ArrayList<Object>();
		s = "INSERT INTO " + getTableName() + " (";
		for (Column col : columns) {
			if (!col.equals(idColumn)) {
				String name = col.getQualifiedName();
				if (name != null)
					s += name + ", ";
			}
		}
		for (Embedded cls : this.embedded) {
			Map<String, Object> objs = cls.vals(obj);
			for (Entry<String, Object> e : objs.entrySet()) {
				s += e.getKey() + ", ";
			}
		}
		s = trimTrailingComma(s) + ") VALUES (";
		for (Column col : columns) {
			if (!col.equals(idColumn)) {
				Object val = col.getProperty().get(obj);
				s += "?, ";
				values.add(val);

			}
		}
		for (Embedded cls : this.embedded) {
			Map<String, Object> objs = cls.vals(obj);
			for (Entry<String, Object> e : objs.entrySet()) {
				s += "?, ";
				values.add(e.getValue());
			}
		}
		s = trimTrailingComma(s) + ")";

		//LOG.info(s);
		setObjPersistentID(obj, c, s, values);

		saveRelations(obj, c, r, deep);

	}

	private <T extends IPersistent> void setObjPersistentID(T obj,
			Connection c, String s, List<Object> values) throws SQLException {
		ResultSet generatedKeys = null;
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(s, Statement.RETURN_GENERATED_KEYS);
			int i = 1;
			for (Object o : values) {
				ps.setObject(i, o);
				i++;
			}
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Save failed!");
			}

			generatedKeys = ps.getGeneratedKeys();
			generatedKeys.next();

			obj.setPersistentID(generatedKeys.getLong(idColumn
					.getQualifiedName()));
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Exception while saving", e);
		} finally {
			if (generatedKeys != null)
				generatedKeys.close();
			if (ps != null)
				ps.close();

		}
	}

	// returns whether or not there are any simple columns that we need to save
	private boolean columnsEmpty() {
		return columns.isEmpty() && this.embedded.isEmpty();
	}

	private String trimTrailingComma(String s) {
		return Util.trimTrailingComma(s);
	}

	private <T extends IPersistent> void saveRelations(T obj, Connection c,
			Relation r, boolean deep) throws SQLException {
		savedEntities.add(obj);
		for (PrimitiveCollection primcol : primcollections) {
			primcol.save(obj, c);
		}
		for (Relation rel : relations) {
			if (rel != r || r == null)
				rel.save(this, obj, c, deep);
		}
	}

	public <T extends IPersistent> void update(T obj, Connection c, Relation r,
			boolean deep) throws SQLException {
		String s = "UPDATE " + getTableName() + " SET ";
		ArrayList<Object> values = new ArrayList<Object>();
		for (Column col : columns) {
			if (!col.equals(idColumn)) {
				String name = col.getQualifiedName();
				if (name != null)
					s += name + " = ";
				Object val = col.getProperty().get(obj);
				s += "?, ";
				values.add(val);
			}
		}
		for (Embedded cls : this.embedded) {
			Map<String, Object> objs = cls.vals(obj);
			for (Entry<String, Object> e : objs.entrySet()) {
				s += e.getKey() + " = ?, ";
				values.add(e.getValue());
			}
		}

		s = trimTrailingComma(s) + " WHERE " + idColumn.getQualifiedName()
				+ " = ?";

		//LOG.info(s);

		ResultSet generatedKeys = null;
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(s, Statement.RETURN_GENERATED_KEYS);
			int i = 1;
			for (Object o : values) {
				ps.setObject(i, o);
				i++;
			}
			ps.setObject(i, obj.getPersistentID());
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Save failed!");
			}

			generatedKeys = ps.getGeneratedKeys();
			generatedKeys.next();

			obj.setPersistentID(generatedKeys.getLong(idColumn
					.getQualifiedName()));
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Error saving object", e);
		} finally {
			if (generatedKeys != null)
				generatedKeys.close();
			if (ps != null)
				ps.close();

		}
		saveRelations(obj, c, r, deep);

	}

	public void addColumn(Column col) {
		columns.add(col);
	}

	public IDColumn getIDColumn() {
		return idColumn;
	}

	public TreeWriter getGen() {
		TreeWriter d = new TreeWriter(getTableName());
		for (Column column : getColumns()) {
			TreeWriter m = new TreeWriter(column.getName(),
					column.getQualifiedName());
			d.addChild(m);
		}
		for (Embedded cls : embedded) {
			for (Column column : cls.getAllColumns()) {
				TreeWriter m = new TreeWriter(column.getName(),
						column.getQualifiedName());
				d.addChild(m);
			}
		}
		for (Relation r : this.relations) {
			if (r.containedBy(this)) {
				d.addChild(r.getGen());
			}
		}
		return d;
	}

	public String parseColumnName(String columnName) {
		String s = config.separator();
		int i = columnName.indexOf(s) + s.length();
		if (i <= 0)
			return columnName;
		return columnName.substring(i, columnName.length());
	}

	public String parseTableName(String columnName) {
		String s = config.separator();
		int i = columnName.indexOf(s);
		if (i <= 0)
			return null;
		return columnName.substring(0, i);
	}

	public String buildFullLoadString() {
		Relation.markUnusedByJoin();
		String r = buildLoadString();
		Relation.markUnusedByJoin();
		return r;
	}

	public String buildLoadString() {
		String r = "";
		for (Relation rel : relations) {
			if (!rel.isUsedByJoin()) {
				rel.markUsedByJoin(true);
				r += rel.buildLeftJoin(this);
			}
		}
		for (PrimitiveCollection col : primcollections) {
			r += col.buildLeftJoin();
		}
		return r;
	}

}
