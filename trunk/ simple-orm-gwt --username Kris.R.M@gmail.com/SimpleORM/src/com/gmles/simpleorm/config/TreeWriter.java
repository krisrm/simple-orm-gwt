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

import java.util.ArrayList;
import java.util.List;

public class TreeWriter {
	private List<TreeWriter> children = new ArrayList<TreeWriter>();
	private String name;
	private boolean prefix = true;
	private boolean throwaway=false;
	private String displayName=null;
	
	
	public TreeWriter(String name, String displayName){
		this(name);
		this.displayName=displayName;
	}
	public TreeWriter(String name){
		this.name=name;
	}
	public TreeWriter(String name, boolean prefix, boolean throwaway){
		this(name);
		this.prefix=prefix;
	}	
	public void addChild(TreeWriter child){
		this.children.add(child);
	}
	public String writeString(){
		return writeString(0,"");
	}
	public String writeString(int d, String p){
		String s ="";
		if(throwaway)p="";
		String np = p+(this.displayName==null?this.name:this.displayName);
		if(d>0){
			s+=t(d)+"public static final String "+this.name.toUpperCase()+" = \""+np+"\";\n";
		}
		if(this.children.size()==0)return s;
		s+= t(d)+"public class "+name.toLowerCase()+"{\n";
		for(TreeWriter child: children){
			s+=child.writeString(d+1,prefix?np+".":p);
		}
		s+=t(d)+"}\n";
		return s;
	}
	private static String t(int depth){
		String s="";
		for(int i=0;i<depth;i++){
			s+="\t";
		}
		return s;
	}
	
}
