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

package com.gmles.simpleorm;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Date;
import java.util.Collection;

public class Util {

	public static Class<?> extractCollection(Method m) {
		if (Collection.class.isAssignableFrom(m.getReturnType())) {
			
			Type[] types = ((ParameterizedType) m.getGenericReturnType())
			.getActualTypeArguments();
			return (Class<?>) types[types.length - 1];
			
		}
		
		return m.getReturnType();
		
	}
	public static Class<?> extractCollection(PropertyDescriptor property) {
		return extractCollection(property.getReadMethod());
	}

	public static boolean isPrimitiveCollection(Method m){
		if (!Collection.class.isAssignableFrom(m.getReturnType()))
			return false;
		return isPrimitive(extractCollection(m));
	}
	
	
	public static boolean isPrimitive(Class<?> c) {
		return c.equals(String.class)
		|| Date.class.isAssignableFrom(c)
		|| c.equals(Integer.class)
		|| c.toString().equals("int")
		|| c.equals(Float.class)
		|| c.toString().equals("float")
		|| c.toString().equals("char")
		|| c.equals(Short.class)
		|| c.toString().equals("short")
		|| c.equals(Long.class)
		|| c.toString().equals("long")
		|| c.equals(Double.class)
		|| c.toString().equals("double")
		|| c.equals(Boolean.class)
		|| c.toString().equals("boolean")
		|| (c.isArray() && c.getComponentType().toString()
				.equals("byte"))
		|| c.isEnum()
		|| c.equals(BigDecimal.class)
		|| c.equals(Timestamp.class);
	
	}
	public static boolean isPrimitive(Method m) {
		Class<?> c = m.getReturnType();
		return isPrimitive(c);
		
	}
	
	public static String trimTrailingComma(String r) {
		if (r.endsWith(", "))
			r = r.substring(0, r.length() - 2);
		return r;
	}

}
