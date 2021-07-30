/*
 * Attribute.java
 * Date: 2018
 * 
 * Description:
 * 		This file contains the class definition for Attribute.
 * 
 * 		The Attribute class maintains information related to one particular attribute, including its
 * 			name and whether it is continuous (with its start and end values) or discrete (with its
 * 			list of values).
 * 
 */

public class Attribute {
	public String name;
	public double values[];
	public double start, end;
	public boolean cont;
	
	public Attribute(String n, double s, double e, double v[], boolean c)
	{
		name = n;
		start = s;
		end = e;
		values = v;
		cont = c;
	}
}
