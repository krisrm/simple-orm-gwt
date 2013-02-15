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

package com.gmles.simpleorm.session;

import java.beans.IntrospectionException;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.gmles.simpleorm.common.DBClass;
import com.gmles.simpleorm.common.entity.IPersistent;
import com.gmles.simpleorm.config.Configuration;
import com.gmles.simpleorm.exceptions.PersistentClassFormatException;
import com.gmles.simpleorm.exceptions.PersistentConfigurationException;

public class ORMSession {

	private static final String ERR_CONFIGURATION = "Problem with configuration file";
	private static final String ERR_INTROSPECTION = "Introspection fails on persistent class - check that it satisfies Bean spec.";
	private static final String ERR_CLASS_FORMAT = "A class in your model was not formatted correctly";

	private static Logger LOG = Logger.getLogger(ORMSession.class.getName());

	private Configuration config;
	private DataSource source;
	private Connection connection;

	public ORMSession(Configuration conf, DataSource source)
			throws PersistentConfigurationException {
		config = conf;
		this.source = source;
	}

	public Configuration getConfig() {
		return config;
	}

	public void setConfig(Configuration config) {
		this.config = config;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}

	public Transaction startTransaction() throws SQLException {
		return new Transaction(getConnection());
	}

	public Connection getConnection() throws SQLException {
		if (source != null)
			return source.getConnection();
		try {
			return config.getJdbcConnection();
		} catch (PersistentConfigurationException e) {
			LOG.log(Level.SEVERE, "Couldn't load dev driver.", e);
		}
		return null;
	}

	public <T extends IPersistent> List<T> load(Class<T> cls, String query,
			Object... objects) throws SQLException {
		Transaction t = startTransaction();
		List<T> ret = load(t, cls, query, objects);
		t.commit();
		return ret;
	}
	
	public <T extends IPersistent> List<T> load(Class<T> cls, Query...queries) throws SQLException {
		Transaction t = startTransaction();
		List<T> ret = load(t, cls, queries);
		t.commit();
		return ret;
	}

	public <T extends IPersistent> List<T> load(Transaction t, Class<T> cls,
			String query, Object... objects) throws SQLException{
		return load(t,cls,new Query(query,objects));
	}
	public <T extends IPersistent> List<T> load(Transaction t, Class<T> cls,
			Query...queries) throws SQLException {
		
		DBClass dbc;
		try {
			dbc = DBClass.getDBClass(cls);
			List<T> items = dbc.parseResultSet(t,queries);
			return items;
		} catch (IntrospectionException e) {
			LOG.log(Level.SEVERE, ERR_INTROSPECTION, e);
		} catch (PersistentConfigurationException e) {
			LOG.log(Level.SEVERE, ERR_CONFIGURATION, e);
		} catch (PersistentClassFormatException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Saves an object without saving children, except for updating ids of
	 * immediate children in relations. If the field expressing that relation is
	 * null, it is not saved - that is, no rows are updated. This allows for
	 * precise shallow manipulation of objects with relations - to avoid saving
	 * relations at all, simply ensure they are null. To save relations, make
	 * sure the object in the collection or field has an assigned PersistentID;
	 * this will cause the corresponding updates without saving any deeper in
	 * the structure.
	 */

	public <T extends IPersistent> T saveShallow(Class<T> cls, T obj)
			throws SQLException {
		Transaction t = startTransaction();
		T ret = saveShallow(t, cls, obj);
		t.commit();
		return ret;
	}

	public <T extends IPersistent> T saveShallow(Transaction t, Class<T> cls,
			T obj) throws SQLException {
		DBClass dbc = null;
		try {
			dbc = DBClass.getDBClass(cls);
			dbc.saveShallow(obj, t.getConnection());
			return obj;
		} catch (IntrospectionException e) {
			LOG.log(Level.SEVERE, ERR_INTROSPECTION, e);
		} catch (PersistentClassFormatException e) {
			LOG.log(Level.SEVERE, ERR_CLASS_FORMAT, e);
		}
		return null;
	}

	public <T extends IPersistent> T save(Class<T> cls, T obj)
			throws SQLException {
		Transaction t = startTransaction();
		T ret = save(t, cls, obj);
		t.commit();
		return ret;
	}

	public <T extends IPersistent> T save(Transaction t, Class<T> cls, T obj)
			throws SQLException {
		DBClass dbc = null;
		try {
			dbc = DBClass.getDBClass(cls);
			dbc.save(obj, t.getConnection());
			return obj;
		} catch (IntrospectionException e) {
			LOG.log(Level.SEVERE, ERR_INTROSPECTION, e);
		} catch (PersistentClassFormatException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void execute(String sql) throws SQLException {
		Transaction t = startTransaction();
		execute(t, sql);
		t.commit();
	}

	public void execute(Transaction t, String sql) throws SQLException {
		Statement st = t.getConnection().createStatement();
		LOG.info("Executing: '" + sql + "'.");
		int delete = st.executeUpdate(sql);
		if (delete == 0) {
			LOG.warning("No rows affected by query.");
		}
		if (st != null)
			st.close();
	}
	
	public void executePrepared(String sql, Object... args) throws SQLException{
		Transaction t = startTransaction();
		executePrepared(sql,args);
		t.commit();
	}
	
	public void executePrepared(Transaction t, String sql, Object... args) throws SQLException{
		PreparedStatement st = t.getConnection().prepareStatement(sql);
		LOG.info("Executing prepared: '" + sql +"'.");
		for (int i = 1; i <= args.length; i++){
			st.setObject(i, args[i-1]);
		}
		int delete = st.executeUpdate();
		if (delete == 0) {
			LOG.warning("No rows affected by query.");
		}
		if (st != null)
			st.close();
	}
	
	public ResultSet query(String sql) throws SQLException {
		Transaction t = startTransaction();
		ResultSet r = query(t,sql);
		t.commit();
		return r;
	}
	public ResultSet query(Transaction t, String sql) throws SQLException {
		Statement s = t.getConnection().createStatement();
		LOG.info("Executing Query: '" + sql + "'.");
		ResultSet r = s.executeQuery(sql);
		if (s != null)
			s.close();
		return r;
	}

	public <T extends IPersistent> List<T> loadFull(
			Class<? extends IPersistent> loadClass) throws SQLException {
		return loadFull(loadClass, null);
	}

	public <T extends IPersistent> List<T> loadFull(
			Class<? extends IPersistent> loadClass, String whereClausePrep,
			Object... preparedObjects) throws SQLException {
		Transaction t = startTransaction();
		List<T> items = null;
		try {
			String query = "SELECT * FROM "
					+ loadString(loadClass)
					+ (whereClausePrep == null ? "" : " WHERE "
							+ whereClausePrep);
			LOG.info(query);
			Query q = new Query(query,preparedObjects);
			Query queries[] = {q};

			DBClass dbc = DBClass.getDBClass(loadClass);
			items = dbc.parseResultSet(t,queries);
		} catch (IntrospectionException e) {
			LOG.log(Level.SEVERE, ERR_INTROSPECTION, e);
		} catch (PersistentConfigurationException e) {
			LOG.log(Level.SEVERE, ERR_CONFIGURATION, e);
		} catch (PersistentClassFormatException e) {
			LOG.log(Level.SEVERE, ERR_CLASS_FORMAT, e);
		} finally {
			t.commit();
		}

		return items;
	}

	public String loadString(Class<? extends IPersistent> loadClass)
			throws IntrospectionException, PersistentClassFormatException {
		DBClass dbClass = DBClass.getDBClass(loadClass);
		return dbClass.getTableName() + dbClass.buildFullLoadString();
	}

	
	private void executeUpdates(Transaction t, List<String> statements) throws SQLException{
		
		while (statements.contains(""))
			statements.remove("");
		
		// sort statements so all drops are done, then all creates, then all
		// alters. This should work in general
		Collections.sort(statements, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				if (o1.startsWith("DROP"))
					return -1;
				if (o1.startsWith("CREATE") && o2.startsWith("DROP"))
					return 1;
				if (o1.startsWith("CREATE") && o2.startsWith("ALTER"))
					return -1;
				if (o1.startsWith("ALTER"))
					return 1;
				return 0;
			}
		});
		Connection c = t.getConnection();
		for (String s : statements) {
			LOG.info(s);
			if (s != null)
				c.createStatement().execute(s);
			
		}
		
		t.commit();
	}
	
	public void dropSchema() throws SQLException, PersistentClassFormatException, PersistentConfigurationException{
		Transaction t = startTransaction();
		List<DBClass> mapped = config.getMappedDBClasses();
		List<String> statements = new ArrayList<String>();
		LOG.info("Dropping all tables in schema");
		for (DBClass dbc: mapped){
			statements.add(dbc.buildDrop());
			statements.addAll(dbc.buildRelationDrop());
		}
		executeUpdates(t,statements);
		LOG.info("Schema successfully dropped");
	}
	

	public void updateSchema() throws SQLException,
	PersistentClassFormatException, PersistentConfigurationException {
		//TODO add the appropriate alter statements. Needs to handle alters of db classes (if anything was added to the schema), and alters to relationships between db classes
		Transaction t = startTransaction();
		List<DBClass> mapped = config.getMappedDBClasses();
		List<String> statements = new ArrayList<String>();
		LOG.info("Updating Schema");
		
		for (DBClass dbc : mapped) {
			statements.addAll(dbc.buildAllCreate());
		}
		
		executeUpdates(t,statements);
		statements.clear();
		t = startTransaction();
		for (DBClass dbc : mapped) {
			statements.addAll(dbc.buildUpdates(t));
		}
		executeUpdates(t,statements);
		LOG.info("Schema successfully updated");
		
	}

	// copied from schemabuilder
	public void buildSchema() throws SQLException,
			PersistentClassFormatException, PersistentConfigurationException {
		Transaction t = startTransaction();
		List<DBClass> mapped = config.getMappedDBClasses();
		List<String> statements = new ArrayList<String>();
		LOG.info("Rebuilding Schema");
		for (DBClass dbc : mapped) {
			statements.add(dbc.buildDrop());
			statements.add(dbc.buildCreate());
			statements.addAll(dbc.buildRelationStatements());
		}
		executeUpdates(t,statements);
		LOG.info("Schema successfully built");
		
	}

	

	public void generateAutoCompleteClass(File output)
			throws PersistentClassFormatException,
			PersistentConfigurationException {
		config.generateAutoCompleteClass(output);
	}

	
	public void close() throws SQLException {
		if (connection != null && !connection.isClosed()) {
			LOG.info("Closing session " + this);
			connection.commit();
			connection.close();
			LOG.info("Closed session " + this);
		}
		connection = null;
	}

	public static void main(String[] args) throws SQLException,
	PersistentClassFormatException, PersistentConfigurationException {
		
//		new ORMSession(Configuration.defaultConfiguration(), null).dropSchema();
		new ORMSession(Configuration.defaultConfiguration(), null).updateSchema();
	}
}
