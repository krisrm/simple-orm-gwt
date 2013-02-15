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
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertyAccessor extends SQLAccessor {

	Logger LOG = Logger.getLogger(SQLAccessor.class.getName());
	
	protected PropertyDescriptor descriptor;
	
	public PropertyAccessor(PropertyDescriptor property){
		this.descriptor=property;
	}
	
	@Override
	protected Object getObject(Object object){
		try {
			return object==null?null:descriptor.getReadMethod().invoke(object);
		} catch (IllegalArgumentException e) {
			LOG.log(Level.SEVERE,"Error accessing getter for "+descriptor.getName()+" with val "+object,e);
		} catch (IllegalAccessException e) {
			LOG.log(Level.SEVERE,"Error accessing getter for "+descriptor.getName()+" with val "+object,e);
		} catch (InvocationTargetException e) {
			LOG.log(Level.SEVERE,"Error accessing getter for "+descriptor.getName()+" with val "+object,e);
		}
		return null;
	}

	@Override
	protected void putObject(Object object, Object value){
		try {
			descriptor.
			getWriteMethod().
			invoke(object, value);
		} catch (IllegalArgumentException e) {
			LOG.log(Level.SEVERE,"Error accessing setter for "+descriptor.getName(),e);
		} catch (IllegalAccessException e) {
			LOG.log(Level.SEVERE,"Error accessing setter for "+descriptor.getName(),e);
		} catch (InvocationTargetException e) {
			LOG.log(Level.SEVERE,"Error accessing setter for "+descriptor.getName(),e);
		}
	}

	@Override
	public Class<?> getType() {
		return descriptor.getPropertyType();
	}

	@Override
	public Class<?> getComponentType() {
		return null;
	}

}
