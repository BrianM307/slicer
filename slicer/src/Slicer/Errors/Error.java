package Slicer.Errors;

public class Error {
	
	String output;
	int location;
	int code;
	
	int NO_ERROR = 0;
	int INCORRECT_FLAVOR = 1;
	
	
	
	String[] errors = {
			"0 - No Errors Found",
			"1 - Incorrect Flavor of GCode, Please use Marlin",
			"2 - ",
			"3 - ",
			"4 - ",
			"5 - ",
			"6 - ",
			"7 - ",
			"8 - ",
			"9 - ",
			"10 - ",
			};
	
	public Error()
	{
		output = "";
		location = 0;
		code = 000;
	}
	public Error(int Loc, int inCode)
	{
		location = Loc;
		code = inCode;
		output = errors[this.code];
	}
	
	public String toString()
	{
		return String.format("Location = %6s, %60s", Integer.toString(location), output);
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
		
		output = errors[this.code];
	}
	public String getOutput() {
		return output;
	}
	public int getLocation() {
		return location;
	}
	public void setLocation(int loc)
	{
		this.location = loc;
	}
	
}
