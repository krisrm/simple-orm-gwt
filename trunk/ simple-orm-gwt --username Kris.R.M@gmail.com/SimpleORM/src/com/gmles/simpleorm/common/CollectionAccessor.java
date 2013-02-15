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
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class CollectionAccessor extends PropertyAccessor{

	public CollectionAccessor(PropertyDescriptor property) {
		super(property);
		if(!Collection.class.isAssignableFrom(property.getPropertyType())){
			throw new IllegalArgumentException("Passed property must be a collection.");
		}
	}
	
	@SuppressWarnings("unused")
	@Override
	public void putObject(Object object, Object val){
		ArrayList<Object> real = new ArrayList<Object>();
		for(Object o: (Collection<?>)val){
			real.add(insertType(val,getComponentType()));
		}
		super.putObject(object,real);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object getObject(Object object){
		return new CollectionProxy<Object,Object>((Collection<Object>) super.getObject(object)){

			@Override
			public Object proxyToReal(Object o) {
				return insertType(o,getComponentType());
			}

			@Override
			public Object realToProxy(Object o) {
				return extractType(o,getComponentType());
			}
			
		};
	}
	
	@Override
	public Class<?> getComponentType() {	
		Type[] types = ((ParameterizedType) descriptor.getReadMethod().getGenericReturnType())
		.getActualTypeArguments();
		return (Class<?>) types[types.length - 1];
	}


}
abstract class CollectionProxy<T1,T2> implements Collection<T2>{

	Collection<T1> real;
	public CollectionProxy(Collection<T1> real){
		this.real=real;
	}

	@Override
	public boolean add(T2 e) {
		return real.add(proxyToReal(e));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean addAll(Collection<? extends T2> arg0) {
		Boolean changed = false;
		for(Object o: arg0){
			changed = changed || real.add(proxyToReal((T2) o));
		}
		return changed;
	}

	@Override
	public void clear() {
		real.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object arg0) {
		return real.contains(proxyToReal((T2) arg0));
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsAll(Collection<?> arg0) {
		Boolean contains = true;
		for(Object o: arg0){
			contains = contains && real.contains(proxyToReal((T2) o));
		}
		return contains;
	}

	@Override
	public boolean isEmpty() {
		return real.isEmpty();
	}

	@Override
	public Iterator<T2> iterator() {
		final Iterator<T1> r = real.iterator();
		Iterator<T2> iterator = new Iterator<T2>(){

			@Override
			public boolean hasNext() {
				// TODO Auto-generated method stub
				return r.hasNext();
			}

			@Override
			public T2 next() {
				// TODO Auto-generated method stub
				return realToProxy(r.next());
			}

			@Override
			public void remove() {
				r.remove();
			}
			
		};
		return iterator;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object arg0) {
		return real.remove(proxyToReal((T2) arg0));
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean removeAll(Collection<?> arg0) {
		Boolean changed = false;
		for(Object o: arg0){
			changed = changed || real.remove(proxyToReal((T2) o));
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		Boolean changed = false;
		for(T1 o: real){
			if(!arg0.contains(realToProxy(o))){
				real.remove(o);
				changed=true;
			}
		}
		return changed;
	}

	@Override
	public int size() {
		return real.size();
	}

	@Override
	public Object[] toArray() {
		Object objects[] = new Object[real.size()];
		int i=0;
		for(T1 o: real){
			objects[i]=realToProxy(o);
			i++;
		}
		return objects;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T2[] toArray(Object[] arg0) {
		Object array[] = arg0;
		if(array.length<real.size()){
			array = (Object[]) Array.newInstance(arg0.getClass().getComponentType(), real.size());
		}
		int i=0;
		for(T1 o: real){
			array[i]=realToProxy(o);
			i++;
		}
		return (T2[]) array;
	}
	
	public abstract T1 proxyToReal(T2 o);
	
	public abstract T2 realToProxy(T1 o);
	
}