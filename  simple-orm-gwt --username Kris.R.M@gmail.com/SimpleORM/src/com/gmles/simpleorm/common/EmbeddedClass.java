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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import com.gmles.simpleorm.Util;
import com.gmles.simpleorm.common.column.SimpleColumn;
import com.gmles.simpleorm.common.entity.annotations.ColumnConstraint;
import com.gmles.simpleorm.common.entity.annotations.Transient;
import com.gmles.simpleorm.config.Configuration;
import com.gmles.simpleorm.exceptions.PersistentConfigurationException;

public class EmbeddedClass extends Embedded{
	private static Logger LOG = Logger.getLogger(EmbeddedClass.class.getName());
	private BeanInfo info;

	
	public EmbeddedClass(DBClass dbClass, SQLAccessor property, String name) throws IntrospectionException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException, PersistentConfigurationException {
		super(dbClass,property,name);
		this.info = Introspector.getBeanInfo(this.property.getType(), Object.class);
		for(PropertyDescriptor prop:this.info.getPropertyDescriptors()){
			//LOG.info("Examining: "+prop.getName()+" in "+property.getClass().getSimpleName());
			String subName = this.name+this.owner.getConfig().separator() + prop.getName();
			Method method = prop.getReadMethod();
			if (method!=null && method.getAnnotation(Transient.class) == null) {
				if (Util.isPrimitive(method)) {
					//LOG.info("Creating embedded column "+prop.getName());
					columns.add(new SimpleColumn(this.owner, new PropertyAccessor(prop),method.getAnnotation(ColumnConstraint.class), subName));
				}else if(prop.getPropertyType().isArray()){
					//LOG.info("Creating embedded array as "+name);
					Object instance = prop.getReadMethod().invoke(property.getType().newInstance());
					this.embedded.add(new EmbeddedArray(owner, instance, new PropertyAccessor(prop), subName));
				} else {
					if(prop.getPropertyType().equals(this.property.getType())){
						LOG.severe("Type "+this.property.getType().getName()+" contains itself in field "+prop.getName());
						break;
					}
					//LOG.info("Creating embedded class "+prop.getName());
					embedded.add(new EmbeddedClass(this.owner,new PropertyAccessor(prop),subName));
				}

			}
		}
	}
}
