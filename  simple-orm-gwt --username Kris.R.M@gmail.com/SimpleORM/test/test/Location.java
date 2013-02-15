package test;

public class Location {
	private Address address = new Address();
	private Coordinate coord = new Coordinate();
	public void setAddress(Address address) {
		this.address = address;
	}
	public Address getAddress() {
		return address;
	}
	public void setCoord(Coordinate coord) {
		this.coord = coord;
	}
	public Coordinate getCoord() {
		return coord;
	}
	@Override
	public String toString() {
		return "Location [address=" + address + ", coord=" + coord + "]";
	}
}
