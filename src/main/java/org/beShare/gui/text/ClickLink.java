package org.beShare.gui.text;

public class ClickLink {
	int 	s;
	int 	e;
	String 	location;
	
	public ClickLink(int start, int end, String URL){
		s = start;
		e = end;
		location = URL;
	}
	
	public int getStart(){
		return s;
	}
	
	public int getEnd(){
		return e;
	}
	
	public String getURL(){
		return location;
	}
}
