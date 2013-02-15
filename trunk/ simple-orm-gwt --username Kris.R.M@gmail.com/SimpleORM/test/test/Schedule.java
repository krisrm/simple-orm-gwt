package test;

import java.util.Arrays;

public class Schedule {

	private Boolean[] available = new Boolean[7];
	
	public Schedule(){
		
	}

	public void setAvailable(Boolean[] available) {
		this.available = available;
	}

	public Boolean[] getAvailable() {
		return available;
	}

	@Override
	public String toString() {
		return "Schedule [available=" + Arrays.toString(available) + "]";
	}
	
	
	
}
