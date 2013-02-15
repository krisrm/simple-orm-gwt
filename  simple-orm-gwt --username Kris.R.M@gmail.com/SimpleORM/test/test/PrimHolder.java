package test;

import java.util.ArrayList;
import java.util.List;

import com.gmles.simpleorm.common.entity.PersistentEntity;

public class PrimHolder extends PersistentEntity{

	private static final long serialVersionUID = 1345975883449856332L;
	private List<String> stringlist = new ArrayList<String>();
	private List<Tester> longlist = new ArrayList<Tester>();
	private String dummy;
	
	public void setStringlist(List<String> stringlist) {
		this.stringlist = stringlist;
	}
	public List<String> getStringlist() {
		return stringlist;
	}
	public void setDummy(String dummy) {
		this.dummy = dummy;
	}
	public String getDummy() {
		return dummy;
	}
	public void setLonglist(List<Tester> longlist) {
		this.longlist = longlist;
	}
	public List<Tester> getLonglist() {
		return longlist;
	}
	@Override
	public String toString() {
		return "PrimHolder [stringlist=" + stringlist + ", longlist="
				+ longlist + ", dummy=" + dummy + "]";
	}

}
