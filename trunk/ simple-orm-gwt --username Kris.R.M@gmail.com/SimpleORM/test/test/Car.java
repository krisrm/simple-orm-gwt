package test;

import java.util.ArrayList;
import java.util.List;

import com.gmles.simpleorm.common.entity.PersistentEntity;
import com.gmles.simpleorm.common.entity.annotations.OneToMany;
import com.gmles.simpleorm.common.entity.annotations.OneToOne;

public class Car extends PersistentEntity {

	private static final long serialVersionUID = 4987617968300158273L;
	private Engine engine;
	private List<Wheel> wheels = new ArrayList<Wheel>();
	private String model;
	
	@OneToOne
	public Engine getEngine() {
		return engine;
	}

	public void setEngine(Engine e) {
		engine = e;
	}
	public void setWheels(List<Wheel> wheels) {
		this.wheels = wheels;
	}

	@OneToMany
	public List<Wheel> getWheels() {
		return wheels;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getModel() {
		return model;
	}

	public void addWheel(Wheel w) {
		if (wheels == null){
			wheels = new ArrayList<Wheel>();
		}
		wheels.add(w);
	}

	@Override
	public String toString() {
		return "Car [engine=" + (engine==null?"":engine.getPersistentID()) + ", wheels=" + wheels + ", model="
				+ model + "]";
	}

	

}
