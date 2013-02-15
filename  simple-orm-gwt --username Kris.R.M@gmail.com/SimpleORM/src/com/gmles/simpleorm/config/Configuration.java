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

package com.gmles.simpleorm.config;

import java.beans.IntrospectionException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.ConfigurationException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.gmles.simpleorm.common.DBClass;
import com.gmles.simpleorm.common.PrimitiveCollection;
import com.gmles.simpleorm.common.entity.IPersistent;
import com.gmles.simpleorm.common.relation.ManyToManyRelation;
import com.gmles.simpleorm.common.relation.Relation;
import com.gmles.simpleorm.dialects.DatabaseDialect;
import com.gmles.simpleorm.exceptions.PersistentClassFormatException;
import com.gmles.simpleorm.exceptions.PersistentConfigurationException;

@XmlRootElement(name = "config")
public class Configuration {

	private static Logger LOG = Logger.getLogger(Configuration.class.getName());


	private List<DBClass> mappedClasses;

	@XmlElement(name = "id-separator")
	private String separator = "$";
	
	@XmlElement(name = "specify-embedded")
	private boolean specifyEmbedded = false;
	
	public String separator() {
		return separator;
	}

	@XmlElement(name = "mapping-class")
	private String mapclassname;

	@XmlElement(name = "dialect-class")
	private String dialectclassname;

	@XmlElement(name = "jdbc")
	JDBCConfig jdbc;

	private DatabaseDialect dialect;

	@XmlElement(name = "auto-complete-output")
	private String filename;
	@XmlElement(name = "auto-complete-package")
	private String pack;

	@XmlElement(name = "driver-class")
	private String driver;

	@SuppressWarnings("unchecked")
	public List<DBClass> getMappedDBClasses()
			throws PersistentClassFormatException,
			PersistentConfigurationException {
		if (mappedClasses != null)
			return mappedClasses;
		List<DBClass> r = new ArrayList<DBClass>();
		try {
			for (Field f : Arrays.asList(Class.forName(mapclassname)
					.getFields())) {
				if (IPersistent.class.isAssignableFrom(f.getType())) {
					r.add(new DBClass(this,(Class<? extends IPersistent>) f
							.getType()));
				} else {
					throw new ConfigurationException("Class "
							+ f.getType().getName() + " must implement "
							+ IPersistent.class.getSimpleName());
				}
			}
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			LOG.log(Level.SEVERE, "Error parsing configuration file", e);
			throw new PersistentConfigurationException(e);
		} catch (IntrospectionException e) {
			LOG.log(Level.SEVERE, "Introspection exception parsing DBClasses",
					e);
			throw new PersistentClassFormatException(e);
		} catch (SecurityException e) {
			LOG.log(Level.SEVERE, "Security Exception", e);
		} catch (ClassNotFoundException e) {
			LOG.log(Level.SEVERE, "Class not found", e);
		}

		for (DBClass dc : r) {
			dc.build();
		}
		mappedClasses = r;
		generateAutoCompleteClass(null);
		return mappedClasses;
	}

	public void generateAutoCompleteClass(File output)
			throws PersistentClassFormatException,
			PersistentConfigurationException {
		if (output == null && filename == null) {
			LOG.info("No autocomplete output specified; autocomplete class must be generated manually");
			return;
		}
		if (mappedClasses == null) {
			getMappedDBClasses();
		}
		TreeWriter s = new TreeWriter("s", false, false);
		Set<Relation> allRelations = new HashSet<Relation>();
		Set<PrimitiveCollection> allCollections = new HashSet<PrimitiveCollection>();
		for (DBClass db : mappedClasses) {
			s.addChild(db.getGen());
			allRelations.addAll(db.getRelations());
			allCollections.addAll(db.getPrimcollections());
		}
		for (Relation rel : allRelations) {
			if (rel instanceof ManyToManyRelation) {
				s.addChild(rel.getGen());
			}
		}
		for (PrimitiveCollection col : allCollections) {
			s.addChild(col.getGen());
		}

		try {
			FileWriter fw = output == null ? new FileWriter(filename)
					: new FileWriter(output);
			BufferedWriter writer = new BufferedWriter(fw);
			writer.write("package " + pack + ";\n\n" + s.writeString());
			writer.close();
		} catch (FileNotFoundException e) {
			LOG.log(Level.SEVERE, "Error parsing configuration file", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.log(Level.SEVERE, "Error parsing configuration file", e);
		}
	}

	public Connection getJdbcConnection() throws SQLException, PersistentConfigurationException{
		// if (source == null) {
		// source = new ComboPooledDataSource();
		// Properties connectionProps = new Properties();
		// connectionProps.put("user", jdbc.getJdbcUsername());
		// connectionProps.put("password", jdbc.getJdbcPassword());
		// source.setProperties(connectionProps);
		// source.setJdbcUrl(jdbc.getJdbcURL());
		// if(driver!=null) source.setDriverClass(driver);
		//
		// }
		//
		// return source.getConnection();
		if (driver == null)
			throw new PersistentConfigurationException(
					"Driver class was not provided in persist.xml");
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			throw new PersistentConfigurationException(
			"Driver class given in persist.xml could not be loaded");
		}
		Properties connectionProps = new Properties();
		connectionProps.put("user", jdbc.getJdbcUsername());
		connectionProps.put("password", jdbc.getJdbcPassword());
		return DriverManager.getConnection(jdbc.getJdbcURL(), connectionProps);

	}
	
	public static Configuration defaultConfiguration() throws PersistentConfigurationException{
		return makeConfiguration(null);
	}
	
	public static Configuration makeConfiguration(InputStream xmlconfig) throws PersistentConfigurationException {
		Configuration instance = null;
		if (xmlconfig == null) {
			try {
				xmlconfig = new FileInputStream("persist.xml");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				LOG.log(Level.SEVERE, "Error parsing configuration file", e);
			}
		}

		JAXBContext context;
		try {
			context = JAXBContext.newInstance(Configuration.class);
			Unmarshaller u = context.createUnmarshaller();
			instance = (Configuration) u.unmarshal(xmlconfig);
			instance.getMappedDBClasses();
		} catch (JAXBException e) {
			LOG.log(Level.SEVERE, "Error parsing configuration file", e);
		} catch (PersistentClassFormatException e) {
			
		} catch (PersistentConfigurationException e) {
			throw e;
		}

		return instance;
	}

	public DatabaseDialect getDialect() {
		if (dialect == null) {
			try {
				this.dialect = (DatabaseDialect) Class
						.forName(dialectclassname).newInstance();
			} catch (InstantiationException e) {
				LOG.log(Level.SEVERE, "Error parsing configuration file", e);
			} catch (IllegalAccessException e) {
				LOG.log(Level.SEVERE, "Error parsing configuration file", e);
			} catch (ClassNotFoundException e) {
				LOG.log(Level.SEVERE, "Error parsing configuration file", e);
			}
		}
		return dialect;
	}

	public boolean specifyEmbedded() {
		return specifyEmbedded;
	}

}
