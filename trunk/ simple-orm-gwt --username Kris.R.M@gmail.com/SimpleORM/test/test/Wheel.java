package test;

import com.gmles.simpleorm.common.entity.PersistentEntity;

public class Wheel extends PersistentEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4999068242302820616L;

	@Override
	public String toString() {
		return "Wheel [size=" + size + ", getPersistentID()="
				+ getPersistentID() + "]";
	}

	private int size;

	public void setSize(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}
	
}
