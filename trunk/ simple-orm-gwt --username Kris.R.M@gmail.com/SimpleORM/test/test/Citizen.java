package test;

import com.gmles.simpleorm.common.entity.PersistentEntity;

public class Citizen extends PersistentEntity{

	
	@Override
	public String toString() {
		return "Citizen [firstName=" + firstName + ", lastName=" + lastName
				+ ", home=" + home + ", schedule=" + schedule + "]";
	}
	private static final long serialVersionUID = -1410256044831067114L;
	private String firstName;
	private String lastName;
	private Location home = new Location();
	private Schedule schedule;
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public Location getHome() {
		return home;
	}
	public void setHome(Location home) {
		this.home = home;
	}
	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}
	public Schedule getSchedule() {
		return schedule;
	}
	
}
