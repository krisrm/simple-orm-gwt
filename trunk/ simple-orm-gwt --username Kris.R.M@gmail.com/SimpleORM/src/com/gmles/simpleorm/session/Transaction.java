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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Transaction {
	private static Logger LOG = Logger.getLogger(Transaction.class.getName());

	
	private Connection connection;

	public Transaction(Connection connection) throws SQLException{
		this.connection=connection;
		connection.setAutoCommit(false);
	}

	public void commit(){
		if(connection==null)return;
		try{
			connection.commit();
		}catch(SQLException e){
			LOG.log(Level.SEVERE,"Sql exception, rolling back transaction.",e);
			try {
				connection.rollback();
			} catch (SQLException e1) {
				LOG.log(Level.SEVERE,"Could not roll back transaction.",e);
			}
		}finally{
			if(connection!=null){
				try {
					connection.setAutoCommit(true);
					connection.close();
				} catch (SQLException e) {
					LOG.log(Level.SEVERE,"Couldn't close connection.",e);
				}
			}
		}
	}

	public Connection getConnection() {
		return connection;
	}
}
