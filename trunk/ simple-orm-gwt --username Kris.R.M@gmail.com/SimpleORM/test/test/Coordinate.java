package test;

import java.util.Arrays;

public class Coordinate {
	private Double lat;
	private Double lon;
	private Schedule[] items = new Schedule[2];
	
	public Coordinate(){
		items[0]=new Schedule();
		items[1]=new Schedule();
		for(int i=0; i<7; i++){
			items[0].getAvailable()[i]=true;
			items[1].getAvailable()[i]=false;
		}
	}
	
	public Double getLat() {
		return lat;
	}
	public void setLat(Double lat) {
		this.lat = lat;
	}
	public Double getLon() {
		return lon;
	}
	public void setLon(Double lon) {
		this.lon = lon;
	}

	@Override
	public String toString() {
		return "Coordinate [lat=" + lat + ", lon=" + lon + ", items="
				+ Arrays.toString(items) + "]";
	}
	public void setItems(Schedule[] items) {
		this.items = items;
	}
	public Schedule[] getItems() {
		return items;
	}
}
