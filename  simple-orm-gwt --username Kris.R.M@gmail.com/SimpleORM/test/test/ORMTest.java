package test;

import static org.junit.Assert.assertTrue;

import java.beans.IntrospectionException;
import java.beans.PropertyVetoException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.naming.ConfigurationException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gmles.simpleorm.s;
import com.gmles.simpleorm.config.Configuration;
import com.gmles.simpleorm.exceptions.PersistentClassFormatException;
import com.gmles.simpleorm.exceptions.PersistentConfigurationException;
import com.gmles.simpleorm.session.ORMSession;
import com.gmles.simpleorm.session.Query;

public class ORMTest {

	private static ORMSession session;

	@BeforeClass
	public static void setup() throws ConfigurationException, SQLException,
			ClassNotFoundException, IntrospectionException,
			PropertyVetoException, PersistentConfigurationException, PersistentClassFormatException {
		session = new ORMSession(Configuration.makeConfiguration(null),null);
		session.buildSchema();

	}

	@AfterClass
	public static void tearDown() throws SQLException {
		session.close();
	}

	public static void printResults(String query) throws SQLException {
		Connection c;

			c = session.getConnection();
			Statement s = c.createStatement();
			System.out.println(query);
			s.execute(query);
			ResultSet r = s.getResultSet();
			while (r.next()) {
				for (int i = 1; i <= r.getMetaData().getColumnCount(); i++) {
					System.out.print(r.getObject(i) + " | ");
				}
				System.out.print("\n");
			}
			s.close();
			r.close();
			c.close();

	}

	@Test
	public void saveEngine() throws SQLException {
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
		printResults("SELECT * FROM ENGINE");
		printResults("SELECT * FROM CAR");

		e2.setType("Broken");
		c2.setModel("Pinto");

		session.save(Engine.class, e2);
		printResults("SELECT * FROM ENGINE");
		printResults("SELECT * FROM CAR");

		// printResults(String.format("SELECT * FROM %s JOIN %s ON Engine.R_Car = Car.PersistentID",
		// S.ENGINE,S.CAR));
		String query = String.format("SELECT * FROM %s JOIN %s ON %s = %s",
				s.ENGINE, s.CAR, s.engine.R_CAR, s.car.PERSISTENTID);
		printResults(query);
		List<Engine> engines = session.load(Engine.class, query);
		for (Engine e : engines) {
			System.out.println(e + " " + e.getCar());
		}
		
		
		
	}

	@Test
	public void saveWheels() {
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

		try {
			session.save(Car.class, c1);
			session.save(Car.class, c2);
			session.save(Wheel.class, w3);
			printResults("SELECT * FROM CAR");
			printResults("SELECT * FROM WHEEL");
			List<Car> cars = session.load(Car.class, "select * from " + s.CAR
					+ ", " + s.WHEEL + " where " + s.wheel.R_CAR + "="
					+ s.car.PERSISTENTID);

			for (Car car : cars) {
				System.out.println(car);
				for (Wheel wheel : car.getWheels()) {
					System.out.println(wheel);
				}
			}
			List<Wheel> wheels = session.load(Wheel.class, "select * from "
					+ s.WHEEL + " where " + s.wheel.PERSISTENTID + "=3");
			for (Wheel wheel : wheels) {
				System.out.println("Wheel! " + wheel);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		assertTrue(c1.getPersistentID() > 0);
		assertTrue(c2.getPersistentID() > 0);
		assertTrue(w1.getPersistentID() > 0);
		assertTrue(w2.getPersistentID() > 0);
		assertTrue(w3.getPersistentID() > 0);

	}

	@Test
	public void saveTeacher() throws SQLException {

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
		printResults("SELECT * FROM TEACHER");
		printResults("SELECT * FROM R_TEACHER_STUDENT");
		printResults("SELECT * FROM STUDENT");

		List<Teacher> teachers = session.load(Teacher.class, "Select * from "
				+ s.TEACHER + ", " + s.STUDENT + ", " + s.R_TEACHER_STUDENT
				+ " where " + s.student.PERSISTENTID + "="
				+ s.r_teacher_student.R_STUDENT + " and "
				+ s.teacher.PERSISTENTID + "=" + s.r_teacher_student.R_TEACHER);
		for (Teacher teacher : teachers) {
			System.out.println("Loaded Teacher: " + teacher);
			for (Student student : teacher.getStudents()) {
				System.out.println("Loaded Student: " + student);
			}
		}
	}

	@Test
	public void saveCitizen() throws SQLException {
		Citizen obj = new Citizen();
		Schedule sched = new Schedule();
		for (int i = 0; i < 7; i++) {
			sched.getAvailable()[i] = (i % 2 == 0 ? true : false);
		}
		obj.setSchedule(sched);
		obj.setFirstName("Jim");
		obj.setLastName("Sanders");
		Location loc = new Location();
		Address addr = new Address();
		addr.setCity("Edmonton");
		addr.setPcode("T48 2ZM");
		addr.setProvince("Alberta");
		addr.setStreet("10144 super-street");
		loc.setAddress(addr);
		Coordinate c = new Coordinate();
		c.setLat(153.444);
		c.setLon(-85.12);
		loc.setCoord(c);
		obj.setHome(loc);
		session.save(Citizen.class, obj);
		List<Citizen> l = session.load(Citizen.class, "Select * from "
				+ s.CITIZEN);
		for (Citizen cit : l) {
			System.out.println("Loaded Citizen: " + cit.toString());
		}
	}

	@Test
	public void savePrimCols() throws SQLException {

		PrimHolder t1 = new PrimHolder();
		PrimHolder t2 = new PrimHolder();

		List<String> s1 = new ArrayList<String>();
		s1.add("AB");
		s1.add("CD");
		s1.add("EF");
		s1.add("GH");
		List<String> s2 = new ArrayList<String>();
		s2.add("AB1");
		s2.add("CD2");
		s2.add("EF3");
		s2.add("GH4");

		t1.setStringlist(s1);
		t2.setStringlist(s2);
		session.save(PrimHolder.class, t1);
		session.save(PrimHolder.class, t2);

		s1.remove(1);
		s1.remove(1);
		t1.setStringlist(s1);
		session.save(PrimHolder.class, t1);

		List<Tester> l1 = new ArrayList<Tester>();
		l1.add(Tester.ONE);
		l1.add(Tester.TWO);
		l1.add(Tester.THREE);
		l1.add(Tester.TWO);
		t1.setLonglist(l1);
		session.save(PrimHolder.class, t1);
		List<PrimHolder> prims = session.load(PrimHolder.class,
				"Select * from (" + s.PRIMHOLDER + " left join "
						+ s.COL_PRIMHOLDER_LONGLIST + " on ("
						+ s.col_primholder_longlist.ID + "="
						+ s.primholder.PERSISTENTID + ")) left join "
						+ s.COL_PRIMHOLDER_STRINGLIST + " on ("
						+ s.primholder.PERSISTENTID + "="
						+ s.col_primholder_stringlist.ID + ") order by "
						+ s.col_primholder_longlist.INDEX + ", "
						+ s.col_primholder_stringlist.INDEX + " asc");
		for (PrimHolder p : prims) {
			System.out.println("Loaded: " + p);
		}
	}

	@Test
	public void saveTypes() throws SQLException {
		TestTypes t = new TestTypes();
		t.setTest(Tester.TWO);
		t.getArray1()[2][2] = "Happy days!";
		t.getArray1()[0][0] = "Shall it be?";
		for (int i = 0; i < 5; i++) {
			t.getPoly()[i] = new Coordinate();
		}
		t.getPoly()[0].setLat(0d);
		t.getPoly()[0].setLon(0d);
		t.getPoly()[4].setLat(1d);
		t.getPoly()[4].setLon(1d);
		t.setAmmount(new BigDecimal(
				"12345101025021301240123124125001250.124012512501238719283744254321"));
		session.save(TestTypes.class, t);
		List<TestTypes> ts = session.load(TestTypes.class, "SELECT * from "
				+ s.TESTTYPES);
		for (TestTypes test : ts) {
			System.out.println("Loaded " + test.toString());
		}
	}
	
	@Test
	public void multiLoad() throws SQLException{
		Man man = new Man();
		man.setName("Dave");
		
		Leg right = new Leg();
		right.setName("Right");
		Leg left = new Leg();
		left.setName("left");
		man.getLegs().add(left);
		man.getLegs().add(right);
		
		Dog dog = new Dog();
		dog.setName("Danger-Dog");
		
		Leg rf = new Leg();
		rf.setName("Right-Front");
		Leg lf = new Leg();
		lf.setName("Left-Front");
		Leg rb = new Leg();
		rb.setName("Right-Back");
		Leg lb = new Leg();
		lb.setName("Left-Back");
		dog.getLegs().add(rf);
		dog.getLegs().add(rb);
		dog.getLegs().add(lf);
		dog.getLegs().add(lb);
		
		man.getDogs().add(dog);
		
		session.save(Man.class, man);
		System.out.println(session.load(Man.class, new Query(
				"Select * from "+s.MAN+
				" left join "+s.DOG+" on ("+s.man.PERSISTENTID+"="+s.dog.R_MAN+")"+
				" left join "+s.LEG+" on ("+s.dog.PERSISTENTID+"="+s.leg.R_DOG+")"
				),
				new Query(
				"Select * from "+s.MAN+
				" left join "+s.LEG+" on ("+s.man.PERSISTENTID+"="+s.leg.R_MAN+")")));
		
	}

}
