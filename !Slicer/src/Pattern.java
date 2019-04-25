package Slicer;

import java.awt.Graphics;

public class Pattern {
	
	private double length;
	String pattern;
	private boolean Chanel0 = false, Chanel1 = false, Chanel2 = false, Chanel3 = false, Chanel4 = false, Chanel5 = false, Chanel6 = false, Chanel7 = false, Chanel8 = false, isNegative;
	
	public Pattern()
	{
		
	}
	public Pattern(String patternIn, boolean isNegIn, double lengthIn)
	{
		length = lengthIn;
		pattern = patternIn;
		
		isNegative = isNegIn;
		int add = 9-patternIn.length();
		String pattern = "";
		for (int i = 0; i<add; i++)
		{
			pattern = pattern + "0";
		}
		pattern = pattern + patternIn;
		
		//System.out.println(pattern + " PATTERN IN");
		
		if(pattern.charAt(0) == '1')
		{
			Chanel0 = true;
			//System.out.println("check");
		}
		if(pattern.charAt(1) == '1')
		{
			Chanel1 = true;
		}
		//System.out.println(pattern.charAt(2) + " char at 2");
		if(pattern.charAt(2) == '1')
		{
			Chanel2 = true;
			//System.out.println("check");
		}
		if(pattern.charAt(3) == '1')
		{
			Chanel3 = true;
		}
		if(pattern.charAt(4) == '1')
		{
			Chanel4 = true;
		}
		if(pattern.charAt(5) == '1')
		{
			Chanel5 = true;
		}
		if(pattern.charAt(6) == '1')
		{
			Chanel6 = true;
		}
		if(pattern.charAt(7) == '1')
		{
			Chanel7 = true;
		}
		if(pattern.charAt(8) == '1')
		{
			Chanel8 = true;
		}
	}
	
	public String toString()
	{
		return(Chanel0 + ", " + Chanel1 + ", " + Chanel2 + ", " + Chanel3 + ", " + Chanel4 + ", " + Chanel5 + ", " + Chanel6 + ", " + Chanel7 + ", " + Chanel8 + ", ");
	}
	
	public void draw(Graphics g)
	{
		
	}

	public double getLength() {
		return length;
	}
	public void setLength(double length) {
		this.length = length;
	}
	public boolean isNegative() {
		return isNegative;
	}

	public void setNegative(boolean isNegative) {
		this.isNegative = isNegative;
	}

	public boolean isChanel0() {
		return Chanel0;
	}

	public void setChanel0(boolean chanel0) {
		Chanel0 = chanel0;
	}

	public boolean isChanel1() {
		return Chanel1;
	}

	public void setChanel1(boolean chanel1) {
		Chanel1 = chanel1;
	}

	public boolean isChanel2() {
		return Chanel2;
	}

	public void setChanel2(boolean chanel2) {
		Chanel2 = chanel2;
	}

	public boolean isChanel3() {
		return Chanel3;
	}

	public void setChanel3(boolean chanel3) {
		Chanel3 = chanel3;
	}

	public boolean isChanel4() {
		return Chanel4;
	}

	public void setChanel4(boolean chanel4) {
		Chanel4 = chanel4;
	}

	public boolean isChanel5() {
		return Chanel5;
	}

	public void setChanel5(boolean chanel5) {
		Chanel5 = chanel5;
	}

	public boolean isChanel6() {
		return Chanel6;
	}

	public void setChanel6(boolean chanel6) {
		Chanel6 = chanel6;
	}

	public boolean isChanel7() {
		return Chanel7;
	}

	public void setChanel7(boolean chanel7) {
		Chanel7 = chanel7;
	}

	public boolean isChanel8() {
		return Chanel8;
	}

	public void setChanel8(boolean chanel8) {
		Chanel8 = chanel8;
	}
	
}
