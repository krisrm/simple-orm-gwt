package test;

import java.util.ArrayList;
import java.util.List;

import com.gmles.simpleorm.common.entity.PersistentEntity;
import com.gmles.simpleorm.common.entity.annotations.OneToMany;

public class Dog extends PersistentEntity{


	private static final long serialVersionUID = 3580945754398482707L;
	
	private List<Leg> legs = new ArrayList<Leg>();
	private String name = "";
	
	@OneToMany
	public List<Leg> getLegs() {
		return legs;
	}

	public void setLegs(List<Leg> legs) {
		this.legs = legs;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Dog(){
	
	}

	@Override
	public String toString() {
		return "Dog [legs=" + legs + ", name=" + name + "]";
	}
	
}
