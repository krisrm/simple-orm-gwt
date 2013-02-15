package test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;

import com.gmles.simpleorm.common.entity.PersistentEntity;

public class TestTypes extends PersistentEntity{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9081770938828893174L;

	private Tester test = Tester.ONE;
	private BigDecimal ammount = null;
	private String[][] array1 = new String[3][3];
	private Tester[] array2 = {Tester.ONE,Tester.TWO,Tester.THREE};
	private Coordinate[] poly = new Coordinate[5];
	private Timestamp time = new Timestamp(System.currentTimeMillis());
	
	
	public TestTypes(){
		
	}

	@Override
	public String toString() {
		String arr1 = "";
		for(String[] s: array1){
			arr1+=Arrays.toString(s)+" ";
		}
		return "TestTypes [test=" + test + ", ammount=" + ammount + ", array1="
				+ arr1 + ", array2="
				+ Arrays.toString(array2) + ", poly="+
				Arrays.toString(poly)+"]";
	}

	public void setTest(Tester test) {
		this.test = test;
	}

	public Tester getTest() {
		return test;
	}

	public void setAmmount(BigDecimal ammount) {
		this.ammount = ammount;
	}

	public BigDecimal getAmmount() {
		return ammount;
	}

	public void setArray1(String[][] array1) {
		this.array1 = array1;
	}

	public String[][] getArray1() {
		return array1;
	}

	public void setArray2(Tester[] array2) {
		this.array2 = array2;
	}

	public Tester[] getArray2() {
		return array2;
	}

	public void setPoly(Coordinate[] poly) {
		this.poly = poly;
	}

	public Coordinate[] getPoly() {
		return poly;
	}

	public void setTime(Timestamp time) {
		this.time = time;
	}

	public Timestamp getTime() {
		return time;
	}

	

}
