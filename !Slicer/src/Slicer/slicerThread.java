package Slicer;

public class slicerThread implements Runnable{

	private IO panel;

	public slicerThread(IO aPanel)
	{
		panel = aPanel;
	}
	
	public void run() {
		calc();
	}
	
	public void calc()
	{
		while(1 == 1)
		{
			panel.updatePanel();
		}
	}
}
