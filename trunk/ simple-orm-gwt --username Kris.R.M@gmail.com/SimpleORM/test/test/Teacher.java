package test;

import java.util.ArrayList;
import java.util.List;

import com.gmles.simpleorm.common.entity.PersistentEntity;
import com.gmles.simpleorm.common.entity.annotations.ManyToMany;

public class Teacher extends PersistentEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8649302709935151018L;
	private String name;

	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String toString(){
		return "Name: " + name + " ID: " +getPersistentID();
	}
	
	public void setStudents(List<Student> students) {
		this.students = students;
	}
	
	@ManyToMany
	public List<Student> getStudents() {
		return students;
	}

	private List<Student> students = new ArrayList<Student>();

	public void addStudent(Student s) {
		if (students == null){
			students = new ArrayList<Student>();
		}
		students.add(s);
	}
	
}
