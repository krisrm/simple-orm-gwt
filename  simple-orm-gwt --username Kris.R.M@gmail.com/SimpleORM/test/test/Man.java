package test;

import java.util.ArrayList;
import java.util.List;

import com.gmles.simpleorm.common.entity.PersistentEntity;
import com.gmles.simpleorm.common.entity.annotations.OneToMany;

public class Man extends PersistentEntity{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3786218121464309544L;
	private List<Leg> legs = new ArrayList<Leg>();
	private List<Dog> dogs = new ArrayList<Dog>();
	
	@OneToMany
	public List<Dog> getDogs() {
		return dogs;
	}

	public void setDogs(List<Dog> dogs) {
		this.dogs = dogs;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private String name = "";
	
	public Man(){}

	@OneToMany
	public List<Leg> getLegs() {
		return legs;
	}

	public void setLegs(List<Leg> legs) {
		this.legs = legs;
	}

	@Override
	public String toString() {
		return "Man [pid="+getPersistentID()+", legs=" + legs + ", dogs=" + dogs + ", name=" + name + "]";
	}
	
	
}
