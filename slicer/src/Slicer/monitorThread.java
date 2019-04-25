package Slicer;

public class monitorThread implements Runnable{

	private Monitor panel;

	public monitorThread(Monitor aPanel)
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
