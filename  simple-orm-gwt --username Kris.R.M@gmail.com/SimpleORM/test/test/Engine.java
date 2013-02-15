package test;

import com.gmles.simpleorm.common.entity.PersistentEntity;
import com.gmles.simpleorm.common.entity.annotations.OneToOne;

public class Engine extends PersistentEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4658306084409281436L;
	private String type;
	private Car car;
	
	
	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setCar(Car car) {
		this.car = car;
	}

	@OneToOne
	public Car getCar() {
		return car;
	}

	@Override
	public String toString() {
		return "Engine [type=" + type + ", car=" + car.getPersistentID() + "]";
	}
	
}
