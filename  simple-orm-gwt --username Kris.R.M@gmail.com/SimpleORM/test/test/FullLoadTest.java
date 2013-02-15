package test;

import java.beans.IntrospectionException;
import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.ConfigurationException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gmles.simpleorm.s;
import com.gmles.simpleorm.common.entity.IPersistent;
import com.gmles.simpleorm.config.Configuration;
import com.gmles.simpleorm.exceptions.PersistentClassFormatException;
import com.gmles.simpleorm.exceptions.PersistentConfigurationException;
import com.gmles.simpleorm.session.ORMSession;

public class FullLoadTest {

	private static ORMSession session;

	@BeforeClass
	public static void setup() throws ConfigurationException, SQLException,
			ClassNotFoundException, IntrospectionException,
			PropertyVetoException, PersistentConfigurationException,
			PersistentClassFormatException {

		session = new ORMSession(Configuration.makeConfiguration(null),null);
		session.buildSchema();

	}

	@AfterClass
	public static void tearDown() throws SQLException {
		session.close();
	}

	@Test
	public void fullLoad() throws IntrospectionException, SQLException {
		PrimHolder ph = new PrimHolder();
		List<String> phl = new ArrayList<String>();
		phl.add("test1");
		phl.add("test2");
		phl.add("test3");
		ph.setStringlist(phl);

		session.save(PrimHolder.class, ph);

		System.out.println(session.loadFull(PrimHolder.class));

	}

	@Test
	public void loadFullOneToOne() throws SQLException {
		Car c1 = new Car();
		c1.setModel("Accord");
		Car c2 = new Car();
		c2.setModel("Mustang");

		Engine e1 = new Engine();
		e1.setType("Honda");
		e1.setCar(c1);
		Engine e2 = new Engine();
		e2.setType("Ford");
		e2.setCar(c2);

		session.save(Engine.class, e1);
		session.save(Engine.class, e2);

		List<Engine> engines = session.loadFull(Engine.class);
		for (Engine e : engines) {
			System.out.println(e + " " + e.getCar());
		}
	}

	@Test
	public void loadFullOneToMany() throws SQLException {
		Car c1 = new Car();
		c1.setModel("Accord");
		Car c2 = new Car();
		c2.setModel("Mustang");

		Wheel w1 = new Wheel();
		Wheel w2 = new Wheel();
		Wheel w3 = new Wheel();
		w1.setSize(18);
		w2.setSize(20);
		w3.setSize(21);

		c1.addWheel(w1);
		c2.addWheel(w2);
		c1.addWheel(w3);

		session.save(Car.class, c1);
		session.save(Car.class, c2);
		//session.save(Wheel.class, w3);

		List<Car> cars = session.loadFull(Car.class);

		for (Car car : cars) {
			System.out.println(car);
			for (Wheel wheel : car.getWheels()) {
				System.out.println(wheel);
			}
		}
	}
	
	@Test
	public void loadFullManyToMany() throws SQLException {
		Teacher t1 = new Teacher();
		t1.setName("Prof. P");
		Teacher t2 = new Teacher();
		t2.setName("Mr. L");

		Student s1 = new Student();
		s1.setName("Jill");
		Student s2 = new Student();
		s2.setName("Joe");
		Student s3 = new Student();
		s3.setName("Bob");

		t1.addStudent(s1);
		t1.addStudent(s2);
		t1.addStudent(s3);
		t2.addStudent(s1);

		session.save(Teacher.class, t1);
		System.out.println("==================");
		session.save(Teacher.class, t2);
		

		List<Teacher> teachers = session.loadFull(Teacher.class);
		for (Teacher teacher : teachers) {
			System.out.println("Loaded Teacher: " + teacher);
			for (Student student : teacher.getStudents()) {
				System.out.println("Loaded Student: " + student);
			}
		}
		
	}
}
