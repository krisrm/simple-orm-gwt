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

import java.beans.IntrospectionException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import com.gmles.simpleorm.Util;
import com.gmles.simpleorm.common.column.SimpleColumn;
import com.gmles.simpleorm.config.Configuration;
import com.gmles.simpleorm.exceptions.PersistentConfigurationException;

public class EmbeddedArray extends Embedded {
	private static Logger LOG = Logger.getLogger(EmbeddedArray.class.getName());

	
	public EmbeddedArray(DBClass dbClass, Object ownerInstance, SQLAccessor property, String name) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException, IntrospectionException, PersistentConfigurationException  {
		super(dbClass,property,name);
		Class<?> type=property.getType();
		Integer length = Array.getLength(ownerInstance);
		String subName=this.name+owner.getConfig().separator();
		Class<?> ctype = type.getComponentType();
		if (Util.isPrimitive(ctype)) {
			//LOG.info("Creating embedded primitive array as "+name);
			for(int i=0; i< length;i++){
				columns.add(new SimpleColumn(this.owner, new ArrayAccessor(i,ctype),null, subName+i));
			}
		}else if(type.getComponentType().isArray()){
			//LOG.info("Creating embedded array of arrays as "+name);
			for(int i=0; i< length;i++){
				Object arrayInstance = Array.get(ownerInstance, 0);
				EmbeddedArray subArray = new EmbeddedArray(owner,arrayInstance,new ArrayAccessor(i,ctype),subName+i);
				embedded.add(subArray);
			}
		} else {
			//LOG.info("Creating embedded class as "+name);
			for(int i=0; i<length;i++)
				embedded.add(new EmbeddedClass(this.owner,new ArrayAccessor(i,ctype), subName + i));
		}

	}


}
