package test;

import com.gmles.simpleorm.common.entity.PersistentEntity;

public class Leg extends PersistentEntity {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6957111026203679336L;
	private String name = "";
	
	public Leg(){}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Leg [name=" + name + "]";
	}
	
	
}
