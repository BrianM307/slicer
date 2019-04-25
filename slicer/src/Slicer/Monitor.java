package Slicer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Monitor extends JPanel implements MouseListener {

	private static BufferedImage myImage;
	private static Graphics myBuffer;
	private static int delay = 10;
	private static IO panel;
	private static int tempRate = 0;
	private static double totRate = 0;
	private static double ctRate = 0;
	private static boolean lastPhaseTrue = false;
	private static double startTime = 0;
	private static double totalTime = 0;

	public Monitor(IO inPanel)
	{
		panel = inPanel;
		getPanelGraphics();

		monitorThread calc = new monitorThread(this);
		Thread t = new Thread(calc);
		t.start();
	}
	private Graphics getPanelGraphics(){
		if (null == myImage){
			myImage = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
		} 

		Graphics g = myImage.getGraphics();
		//		g.setColor(Color.black);
		//		g.fillRect(0,0,500,500);
		return g;
	}
	public void paintComponent(Graphics g) {
		g.drawImage(myImage, 0, 0, 1000, 1000, null);
	}
	public void draw()
	{
		//System.out.println("access");
		Graphics g = getPanelGraphics();
		g.setColor(Color.gray);
		g.fillRect(0, 0, 1000, 1000);

		draw1(g);

		repaint();
	}
	public static void draw1(Graphics g)
	{

		
		
		g.setColor(Color.white);
		g.fillRect(20,40,396,20);
		int length = panel.getPctRead()*4;
		g.setColor(Color.red);
		g.fillRect(20,40,length,20);
		
		g.setColor(Color.white);
		g.fillRect(20,110,396,20);
		length = panel.getPctInterpolate()*4;
		g.setColor(Color.red);
		g.fillRect(20,110,length,20);
		
		g.setColor(Color.white);
		g.fillRect(20,180,396,20);
		length = panel.getPctWrite()*4;
		if(panel.getPctInterpolate()==99)
		{
			g.setFont(new Font("Serif", Font.BOLD, 15));
			g.drawString(panel.getPctWrite() + "%",420,190);
			g.setColor(Color.red);
			g.fillRect(20,180,length,20);
		}
		
		g.setColor(Color.white);
		g.setFont(new Font("Serif", Font.BOLD, 20));
		g.drawString("Read and Interpret", 10, 30);
		g.drawString("Interpolate", 10, 100);
		g.drawString("Generate Datastrips and Write", 10, 170);
		
		//rate calc
		
		double rate = 0;
		if(panel.getPatternPos() != tempRate)
		{
			rate = (panel.getPatternPos()-tempRate)/((double)delay/1000); //rate per 20 ms
			tempRate = panel.getPatternPos();
		}
		
		g.setFont(new Font("Serif", Font.BOLD, 30));
		g.drawString("Data Strip Generation Rate", 470, 30);
		if(rate == 0)
		{
			g.drawString("--.-- DP/second", 470, 80);
		}
		else
		{
			totRate += rate;
			ctRate++;
			g.drawString(rate + " DP/second", 470, 80);
		}
		int tempAvg = (int) (totRate/ctRate);
		if(rate == 0)
		{
			g.drawString("--.-- Avg DP/second", 470, 110);
		}
		else
		{
			g.drawString(tempAvg + "", 470, 110);
			g.setFont(new Font("Serif", Font.BOLD, 14));
			g.drawString("Avg DP/sececond", 540, 110);
		}
		
		g.setFont(new Font("Serif", Font.BOLD, 20));
		g.drawString(panel.getTotTime()/1000 + "s total gen time - debug", 470, 140);
		
		g.setColor(new Color(70,70,70));
		g.fillRect(50,300,900,900);
		g.setColor(new Color(180,180,180));
		for(double i = 0; i<9;i+=0.5)
		{
			g.drawRect(50,300,(int) (i*100),900);
			g.drawRect(50,300,900,(int) (i*100));
		}
		panel.calcPoints(g, panel.getPatternPos());
		
		
	}
	public void updatePanel()  {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					draw();
					repaint();
					try {
						Thread.sleep((long) (delay));
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
				}
			});	  
		}catch (Exception e) {
			e.printStackTrace();
		}

	}
	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mousePressed(MouseEvent arg0) {
		int x, y;
		
	}
	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
