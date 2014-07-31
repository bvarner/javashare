package blv.locale;

import java.*;
import java.util.Properties;
import java.io.*;

public class Dictionary extends Properties {
	public Dictionary(File dictionary) throws IOException {
		super();
		setLocale(dictionary);
	}
	
	public void setLocale(File dictionary) throws IOException {
		FileInputStream fis = new FileInputStream(dictionary);
		load(fis);
	}
	
	public String findString(String lookup, String english) {
		return getProperty(lookup, english);
	}
}
