package test;

import java.util.ArrayList;
import java.util.List;

import com.gmles.simpleorm.common.entity.PersistentEntity;
import com.gmles.simpleorm.common.entity.annotations.ManyToMany;

public class Student extends PersistentEntity {


	/**
	 * 
	 */
	private static final long serialVersionUID = 919448944242158956L;

	@Override
	public String toString() {
		String teachers = "";
		for(Teacher teacher: getTeachers()){
			teachers+=teacher.getPersistentID()+", ";
		}
		return "Student [name=" + name + ", teachers=" + (teachers.equals("")?"":teachers.substring(0,teachers.length()-2))
				+ ", getPersistentID()=" + getPersistentID() + "]";
	}

	private String name;
	
	private List<Teacher> teachers = new ArrayList<Teacher>();

	public void setTeachers(List<Teacher> teachers) {
		this.teachers = teachers;
	}

	@ManyToMany
	public List<Teacher> getTeachers() {
		return teachers;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	
	
	
}
