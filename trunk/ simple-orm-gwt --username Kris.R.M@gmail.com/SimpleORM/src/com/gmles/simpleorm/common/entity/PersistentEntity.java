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

package com.gmles.simpleorm.common.entity;

import java.io.Serializable;

public abstract class PersistentEntity implements IPersistent, Serializable{

	private static final long serialVersionUID = 3342396458959795613L;
	private Long persistentID;

	@Override
	public Long getPersistentID() {
		return this.persistentID;
	}

	@Override
	public void setPersistentID(Long persistentID) {
		this.persistentID = persistentID;
		
	}
	
	@Override
	public void onLoaded(){
		
	}

}
