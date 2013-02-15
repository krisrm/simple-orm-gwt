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

public abstract class SQLAccessor{

	protected abstract Object getObject(Object object);
	
	protected abstract void putObject(Object object, Object value);
	
	public abstract Class<?> getType();

	
	public Object get(Object object){
		return extractType(getObject(object),getType());
	}
	
	public void put(Object object, Object value){
		putObject(object, insertType(value,getType()));
	}
	
	protected Object extractType(Object r,Class<?> type){
		if(r!=null && type.isEnum()){
			return ((Enum<?>) r).ordinal();
		}
		return r;
	}
	
	protected Object insertType(Object value, Class<?> type){
		if(type.isEnum() && value!=null){
			return type.getEnumConstants()[(Integer) value];
		}
		return value;
	}

	public abstract Class<?> getComponentType();
	
	
	
}
