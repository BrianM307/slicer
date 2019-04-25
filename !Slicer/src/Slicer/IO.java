package Slicer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.awt.Component;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import Slicer.Errors.Error;



/*
 * This program takes an input GCODE file and turns it into multiple JPG files.
 * these JPG files, when converted to an SVG and imported into Fusion360, will
 * be used to print the data strips for the 3D Printer Project.
 */

public class IO extends JPanel{

	private static BufferedImage myImage;
	private static Graphics myBuffer;
	
	private static int imageBlocks = 0;
	
	private static int delay = 0;
	private static double startTime = 0;
	private static double totalTime = 0;
	
	private static boolean flop = true;
	
	private static int patternPos = 0;
	private static int distanceRemaining =0;
	
	private static ArrayList<Pattern> patternX = new ArrayList<Pattern>();
	private static ArrayList<Pattern> patternY = new ArrayList<Pattern>();

	private static ArrayList<Double> XPoint = new ArrayList<Double>();
	private static ArrayList<Double> YPoint = new ArrayList<Double>();
	private static ArrayList<Double> DeltaZ = new ArrayList<Double>();
	private static ArrayList<Boolean> isExtrude = new ArrayList<Boolean>();

	private static ArrayList<Double> anglesList = new ArrayList<Double>();
	private static ArrayList<Double> distancesList = new ArrayList<Double>();

	private static final double[] anglesQ1 = new double[89];
	private static final double[] angles = new double[(anglesQ1.length*4)+4];

	private static double lastZ = 0.0;
	private static int drawType;
	
	public static boolean inDisplayPhase = false;
	
	//monitors-------------------------
	private static int pctRead = 0;
	private static int pctInterpolate = 0;
	private static int pctWrite = 0;
	private static int codeLines = 0;
	//monitors end--------------
	
	private static double layerCount =0;
	private static double deltaLayerColor = 0;
	
	//scanner
	private static String outFile;
	private static boolean write = true;
	private static Scanner infile;
	private static Scanner infileCt;
	private static FileWriter scanOutput;
	

	public IO()
	{
		getPanelGraphics();

		slicerThread calc = new slicerThread(this);
		Thread t = new Thread(calc);
		t.start();
	}

	public static void main(String[] args)
	{
		//user prompts
		//String dtIn = JOptionPane.showInputDialog("Draw Type (1 model recreation, 2 control sheet generator)");
		//drawType = (int)Double.parseDouble(dtIn);
		drawType = 2;
		if(drawType == 2)
		{
			int ex = JOptionPane.showConfirmDialog(null, "Do you not want to output the images generated as files?", new String("Do you want file outputs?"),JOptionPane.YES_NO_OPTION);
			if(ex == 0)
			{
				write = false;
				delay = 1;
			}
			else
			{
				outFile = JOptionPane.showInputDialog("Output file folder name without file extension");
				
				System.out.println("Clearing Directory " + outFile);
				
				File outFileFile = new File(outFile);
				File[] files = outFileFile.listFiles();
				if(files!=null) 
				{
			        for(File f: files) 
			        {
			        	f.delete();
			        }
			    }
				System.out.println("Directory Cleared");
			}
		}
		IO in = new IO();
		String file = JOptionPane.showInputDialog("Input file name without file extension");
		try {
			infile = new Scanner(new File(file+".gcode"));
			infileCt = new Scanner(new File(file+".gcode"));
		} catch (FileNotFoundException e) {
			try {
			infile = new Scanner(new File("input.gcode"));
			infileCt = new Scanner(new File("input.gcode"));
			}catch(Exception f)
			{
				System.out.println("Def input file not found, please include def input file in topmost directory");
			}
			System.out.println("File not found, Please rename file \"input.gcode\" or simple file name and place in topmost directory of !Slicer Project folder\nusing def example file");
		}
		//System.out.println(infile.toString());
		try {
			scanOutput = new FileWriter(new File("GCodeOut.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Monitor Frame
		if(drawType == 2)
		{
			JFrame frame1 = new JFrame("!Slicer Monitor - Brian Minnick");
			frame1.setSize(1000, 1000);
			frame1.setLocation(980, 0);
			frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame1.setContentPane(new Monitor(in));
			frame1.setVisible(true);
		}
		
		int tempLocation = 0;
		
		while(infileCt.hasNext() == true)
		{
			codeLines++;
			infileCt.nextLine();
		}
		//System.out.println(codeLines);
		while(infile.hasNext() == true)
		{
			//System.out.println(tempLocation);
			checkGCode(tempLocation, infile.nextLine());
			
			pctRead = (int)(((double)tempLocation/codeLines)*100);
			tempLocation++;
		}
		try {
			scanOutput.close();
			System.out.println("Closed");
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(XPoint.size());
		System.out.println(YPoint.size());
		System.out.println(isExtrude.size());
		System.out.println(DeltaZ.size());

		if(!((XPoint.size() == YPoint.size()) && (XPoint.size() == isExtrude.size()) && (XPoint.size() == DeltaZ.size())))
		{
			System.out.println("CRITICAL ERROR - ArrayLists are not sized correctly, can not continue the computation");
		}
		else
		{
			interpolate();
			//Continue with the calculations if all ArrayLists are of equal size
			JFrame frame = new JFrame("!Slicer Output - Brian Minnick");
			frame.setSize(1000, 1000);
			frame.setLocation(0, 0);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setContentPane(in);
			frame.setVisible(true);
			
			doublePoints();
			for (int i = 0; i<angles.length;i++)
			{
				angles[i]=i;
			}
		}
	}

	public static int getPctRead() {
		return pctRead;
	}
	
	public static int getTotalDPL() {
		return XPoint.size();
	}

	public static void setPctRead(int pctRead) {
		IO.pctRead = pctRead;
	}

	public static int getPatternPos() {
		return patternPos;
	}
	
	public static double getTotTime() {
		return totalTime;
	}

	public static int getPctInterpolate() {
		return pctInterpolate;
	}

	public static void setPctInterpolate(int pctInterpolate) {
		IO.pctInterpolate = pctInterpolate;
	}

	public static int getPctWrite() {
		return pctWrite;
	}

	public static void setPctWrite(int pctWrite) {
		IO.pctWrite = pctWrite;
	}

	private static void interpolate()
	{
		int pct = 0;
		//		for(int i = 0; i<angles.length; i++)
		//		{
		//			System.out.print(angles[i] + ", ");
		//		}

		for (int i = 1; i<XPoint.size(); i++)
		{
			boolean isNegative = false;
			double choice = 0; //angle choice
			
			double slope = (YPoint.get(i) - YPoint.get(i-1))/(XPoint.get(i)-XPoint.get(i-1));
			double angle = Math.toDegrees(Math.atan(slope));
			choice = round(angle);
			
			/*
			 * Distance Calculations
			 */
			
			double x = (XPoint.get(i)-XPoint.get(i-1));
			double y = (YPoint.get(i)-YPoint.get(i-1));
			double innerDistance = (x*x) + (y*y);
			double distance;
			if(innerDistance >0)
			{
				distance = Math.sqrt(innerDistance);
			}
			else
			{
				isNegative = true;
				distance = Math.sqrt(Math.abs(innerDistance));
			}
			//double distance = Math.sqrt((x*x) - (y*y))*1000;
			
			/*
			 * Fix Domain of ArcTangent function
			 */
			
			double dx = Math.abs(XPoint.get(i) - XPoint.get(i-1));	
			double dy = Math.abs(YPoint.get(i) - YPoint.get(i-1));
			
			if(dx>0)
			{
				if(dy>0)
				{
					
				}
				if(dy<0)
				{
					choice+=360;
				}
			}
			if(dx<0)
			{
				if(dy>0)
				{
					choice = (choice + 360);
				}
				if(dy<0)
				{
					choice+=180;
				}
			}
			
			double Sdx = (dx/distance)*100;
			double Sdy = (dy/distance)*100; //Scaled delta x and delta y distances to create 
			
			String patternTypeX = Long.toBinaryString((long)Sdx);
			String patternTypeY = Long.toBinaryString((long)Sdy);
			
			patternX.add(new Pattern(patternTypeX, isNegative, distance/2));
			patternY.add(new Pattern(patternTypeY, isNegative, distance/2));
			
			//System.out.println(Sdx + " " + Sdy + " " + distance);
			//System.out.println(patternTypeX + " " + patternTypeY + " " + distance);


			//				if (angle <= angles[0]) 
			//				{
			//			        choice = angles[0]; 
			//				}
			//			    if (angle >= angles[angles.length - 1]) 
			//			    {
			//			        choice = angles[angles.length - 1]; 
			//			    }
			//			    
			//			    int start = 0, end = angles.length, mid = 0;
			//			    
			//			    while(start < end)
			//			    {
			//			    	mid = (start+end)/2;
			//			    	
			//			    	if(Math.abs(angles[mid]-angle) < 1.0)
			//			    	{
			//			    		choice = angles[mid];
			//			    		break;
			//			    	}
			//			    	
			//			    	if(angle < angles[mid])
			//			    	{
			//			    		end = mid-1;
			//			    	}
			//			    	if(angle > angles[mid])
			//			    	{
			//			    		start = mid+1;
			//			    	}
			//			    }
			
			anglesList.add(choice);

			distancesList.add(distance);
			int tempPCT = (int)(((double)i/XPoint.size())*100);
			if(tempPCT != pct)
			{
				System.out.println(tempPCT + " Percent Finished");
				pct=tempPCT;
				//System.out.println(i + " Lines Finished");
				pctInterpolate = tempPCT;
			}
			//System.out.println(pctInterpolate);
		}
		//System.out.println(distancesList);
		//System.out.println(anglesList);
		System.out.println("Done!");
	}
	
	public static double round(double n)
	{
		int N = (int)n;
		double dec = n-N;
		if(dec>0.5)
		{
			return N+1;
		}else
			return N;
	}

	private static void doublePoints()
	{
		for(int i = 0; i < XPoint.size(); i++)
		{
			XPoint.set(i, XPoint.get(i)*2);
			YPoint.set(i, YPoint.get(i)*2);
		}
	}

	public void calcPoints(Graphics g)
	{
		double stopAt = 3000000.0;
		double currentZ = 0.0;
		int xOff = 200;
		int yOff = 200;
		deltaLayerColor = 206/layerCount;
		double totalBlue = 50;
		Color lineGradient = new Color(0,0,(int)totalBlue);
		//System.out.println("access");
		for (int i = 1; i<XPoint.size(); i++)
		{
			//System.out.println("access");
			if(isExtrude.get(i-1) == true)
			{
				//System.out.println("access");
				g.setColor(lineGradient);
				g.drawLine((int)Math.round(XPoint.get(i-1))+xOff, (int)Math.round(YPoint.get(i-1))+yOff, (int)Math.round(XPoint.get(i))+xOff, (int)Math.round(YPoint.get(i))+yOff);
			}
			else
			{
				//System.out.println("access");
				g.setColor(Color.red);
				g.drawLine((int)Math.round(XPoint.get(i-1))+xOff, (int)Math.round(YPoint.get(i-1))+yOff, (int)Math.round(XPoint.get(i))+xOff, (int)Math.round(YPoint.get(i))+yOff);
			}
			if(DeltaZ.get(i) > 0.0)
			{
				lineGradient = new Color(0,0,(int)(totalBlue));
				totalBlue += deltaLayerColor;
				currentZ += DeltaZ.get(i);
				//System.out.println(totalBlue);
			}
			//			if(currentZ >= stopAt)
			//			{
			//				break;
			//			}
		}
	}
	public void calcPoints(Graphics g, int gStop)
	{
		double stopAt = 3000000.0;
		double currentZ = 0.0;
		int xOff = 150;
		int yOff = 150;
		deltaLayerColor = 206/layerCount;
		double totalBlue = 50;
		Color lineGradient = new Color(0,0,(int)totalBlue);
		//System.out.println("access");
		for (int i = 1; i<XPoint.size(); i++)
		{
			//System.out.println("access");
			if(isExtrude.get(i-1) == true)
			{
				//System.out.println("access");
				if(i<gStop)
				{
					g.setColor(Color.green);
				}
				else
					g.setColor(lineGradient);
				g.drawLine((int)Math.round(XPoint.get(i-1))+xOff, (int)Math.round(YPoint.get(i-1))+yOff, (int)Math.round(XPoint.get(i))+xOff, (int)Math.round(YPoint.get(i))+yOff);
			}
			else
			{
				//System.out.println("access");
				if(i<gStop)
				{
					g.setColor(Color.green);
				}
				else
					g.setColor(Color.red);
				g.drawLine((int)Math.round(XPoint.get(i-1))+xOff, (int)Math.round(YPoint.get(i-1))+yOff, (int)Math.round(XPoint.get(i))+xOff, (int)Math.round(YPoint.get(i))+yOff);
			}
			if(DeltaZ.get(i) > 0.0)
			{
				lineGradient = new Color(0,0,(int)(totalBlue));
				totalBlue += deltaLayerColor;
				currentZ += DeltaZ.get(i);
				//System.out.println(totalBlue);
			}
			//			if(currentZ >= stopAt)
			//			{
			//				break;
			//			}
		}
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
		if(drawType == 1) //model recreation
		{
			//System.out.println("access");
			Graphics g = getPanelGraphics();
			g.setColor(Color.gray);
			g.fillRect(0, 0, 1000, 1000);
	
			calcPoints(g);
			
			repaint();
			File outputfile = new File("recent.png");
			try {
				ImageIO.write(myImage, "png", outputfile);
			} catch (IOException e) {
				System.out.println("========== Output write fail! ==========");
				e.printStackTrace();
			}
		}
		else if (drawType == 2) //Data strip generator
		{
			Graphics g = getPanelGraphics();
			g.setColor(Color.white);
			g.fillRect(0,0,1000,1000);
			nextPanel(g);		
			repaint();
		}
	}

	public static void nextPanel(Graphics g)
	{
		inDisplayPhase = true;
		int multFact = 4;
		int tempLength = 0;
		int xOff = 20; //X Chanel start mm away from left edge (60 for dimensional accuracy)
		int deltaX = 2;
		int drawLength=0;
		int width = 3;
		int patternStop = 200;
		boolean incrementPatternPos = true;
		
		imageBlocks++;
		
		if(flop)
		{
			startTime = System.currentTimeMillis();
			flop = false;
		}
		
//		if(distanceRemaining > 0)//check to see if there is a block to continue with
//		{
//			tempLength += distanceRemaining;//set length of first box to remaining distance from previous strip's box
//		}
//		else
//		{
//			tempLength += (patternX.get(patternPos)).getLength();
//		}
		
		while (tempLength<patternStop && patternPos < patternX.size()) //ALL LENGTHS IN MILIMETERS SCALED DOWN TO PIXELS
		{
			incrementPatternPos = true;
			drawLength=0;
			int nextLength = (int)((patternX.get(patternPos)).getLength());
			while(nextLength<1 && patternPos < patternX.size()-1)
			{
				patternPos++;
				nextLength = (int)((patternX.get(patternPos)).getLength());
			}

			//=========================================
			if (distanceRemaining > 0)
			{
				if(distanceRemaining > patternStop)//remaining distance is greater than the length of the panel
				{
					incrementPatternPos = false; //do not increment pattern position
					drawLength = patternStop; // set length of this rectangle to strip length
					distanceRemaining -= patternStop;//add spill over to distance remaining
				}
				else //distance remaining is less than total length of this panel
				{
					drawLength = distanceRemaining; //length of the next rectangle is the spill over distance from the last panel
					distanceRemaining = 0; //no spill-over distance remains
				}
			}
			if((tempLength + (patternX.get(patternPos)).getLength())>patternStop) //if total length greater than allowed length of panel
			{
				incrementPatternPos = false;
				drawLength = patternStop-tempLength; // set length of this rectangle to remaining distance on panel
				distanceRemaining = (int) ((tempLength + (patternX.get(patternPos)).getLength())-patternStop);//add spill over to distance remaining
			}
			else //total length not greater than allowed length
			{
				//tempLength += (patternX.get(patternPos)).getLength(); //add length of box to total length
				drawLength = (int) (patternX.get(patternPos)).getLength(); //set length of this rectangle to the total length of rectangle
				distanceRemaining = 0;
			}
		
//			
//			if(tempLength > 200)
//			{
//				distanceRemaining = tempLength-patternStop;
//			}
//			else
//			{
//				distanceRemaining = 0;
//			}
			tempLength += drawLength;
			//System.out.println(tempLength);
			if((patternX.get(patternPos)).isChanel0())
			{
				g.setColor(Color.black);
				g.drawRect(xOff*multFact, tempLength*multFact, deltaX*multFact, drawLength*multFact);
			}
			if((patternX.get(patternPos)).isChanel1())
			{
				g.setColor(Color.black);
				g.drawRect((xOff*multFact + (1*(deltaX+width)))*multFact, tempLength*multFact, deltaX*multFact, drawLength*multFact);
			}
			if((patternX.get(patternPos)).isChanel2())
			{
				g.setColor(Color.black);
				g.drawRect((xOff*multFact + (2*(deltaX+width)))*multFact, tempLength*multFact, deltaX*multFact, drawLength*multFact);
			}
			if((patternX.get(patternPos)).isChanel3())
			{
				g.setColor(Color.black);
				g.drawRect((xOff*multFact + (3*(deltaX+width)))*multFact, tempLength*multFact, deltaX*multFact, drawLength*multFact);
			}
			if((patternX.get(patternPos)).isChanel4())
			{
				g.setColor(Color.black);
				g.drawRect((xOff*multFact + (4*(deltaX+width)))*multFact, tempLength*multFact, deltaX*multFact, drawLength*multFact);
			}
			if((patternX.get(patternPos)).isChanel5())
			{
				g.setColor(Color.black);
				g.drawRect((xOff*multFact + (5*(deltaX+width)))*multFact, tempLength*multFact, deltaX*multFact, drawLength*multFact);
			}
			if((patternX.get(patternPos)).isChanel6())
			{
				g.setColor(Color.black);
				g.drawRect((xOff*multFact + (6*(deltaX+width)))*multFact, tempLength*multFact, deltaX*multFact, drawLength*multFact);
			}
			if((patternX.get(patternPos)).isChanel7())
			{
				g.setColor(Color.black);
				g.drawRect((xOff*multFact + (7*(deltaX+width)))*multFact, tempLength*multFact, deltaX*multFact, drawLength*multFact);
			}
			if((patternX.get(patternPos)).isChanel8())
			{
				g.setColor(Color.black);
				g.drawRect((xOff*multFact + (8*(deltaX+width)))*multFact, tempLength*multFact, deltaX*multFact, drawLength*multFact);
			}
			/*
			 * YYYY
			 */
			if((patternY.get(patternPos)).isChanel0())
			{
				g.setColor(Color.black);
				g.drawRect((xOff*multFact + (9*(deltaX+width)))*multFact, tempLength*multFact, deltaX*multFact, drawLength*multFact);
			}
			if((patternY.get(patternPos)).isChanel1())
			{
				g.setColor(Color.black);
				g.drawRect((xOff*multFact + (10*(deltaX+width)))*multFact, tempLength*multFact, deltaX*multFact, drawLength*multFact);
			}
			if((patternY.get(patternPos)).isChanel2())
			{
				g.setColor(Color.black);
				g.drawRect((xOff*multFact + (11*(deltaX+width)))*multFact, tempLength*multFact, deltaX*multFact, drawLength*multFact);
			}
			if((patternY.get(patternPos)).isChanel3())
			{
				g.setColor(Color.black);
				g.drawRect((xOff*multFact + (12*(deltaX+width)))*multFact, tempLength*multFact, deltaX*multFact, drawLength*multFact);
			}
			if((patternY.get(patternPos)).isChanel4())
			{
				g.setColor(Color.black);
				g.drawRect((xOff*multFact + (13*(deltaX+width)))*multFact, tempLength*multFact, deltaX*multFact, drawLength*multFact);
			}
			if((patternY.get(patternPos)).isChanel5())
			{
				g.setColor(Color.black);
				g.drawRect((xOff*multFact + (14*(deltaX+width)))*multFact, tempLength*multFact, deltaX*multFact, drawLength*multFact);
			}
			if((patternY.get(patternPos)).isChanel6())
			{
				g.setColor(Color.black);
				g.drawRect((xOff*multFact + (15*(deltaX+width)))*multFact, tempLength*multFact, deltaX*multFact, drawLength*multFact);
			}
			if((patternY.get(patternPos)).isChanel7())
			{
				g.setColor(Color.black);
				g.drawRect((xOff*multFact + (16*(deltaX+width)))*multFact, tempLength*multFact, deltaX*multFact, drawLength*multFact);
			}
			if((patternY.get(patternPos)).isChanel8())
			{
				g.setColor(Color.black);
				g.drawRect((xOff*multFact + (17*(deltaX+width)))*multFact, tempLength*multFact, deltaX*multFact, drawLength*multFact);
			}
			if(incrementPatternPos)
			{
				patternPos++;
			}
			if(isExtrude.get(patternPos))
			{
				g.drawRect((xOff*multFact + (-1*(deltaX+width)))*multFact, tempLength*multFact, deltaX*multFact, drawLength*multFact);
			}
			pctWrite = (int)(((double)patternPos/patternX.size())*100);
			if(patternPos==patternX.size())
			{
				System.out.println(imageBlocks + " image blocks detected, reset initiated");
				//patternPos = 0;
				imageBlocks = 0;
				delay = 100;
				totalTime = System.currentTimeMillis()-startTime;
			}
		}
		if(write)
		{
			File outputfile = new File("./" + outFile + "/image" + imageBlocks + ".jpg");
			try {
				ImageIO.write(myImage, "jpg", outputfile);
			} catch (IOException e) {
				System.out.println("========== Output write fail! ==========");
				e.printStackTrace();
			}
		}
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

	private static String checkGCode(int location, String codeLine)
	{
		if(codeLine.contains("LAYER_COUNT"))
		{
			layerCount = Double.parseDouble(codeLine.substring(13));
		}

		if (codeLine.contains("FLAVOR") == true)
		{
			if ((codeLine.contains("MARLIN") == false))
			{
				return "Incorrect Flavor";
			}

		}

		for (int i = 0; i<codeLine.length(); i++)
		{
			if(codeLine.charAt(i) == ';')
			{
				String temp = codeLine;
				codeLine = codeLine.substring(0, i);
				if(codeLine.length() == 0)
				{
					return" Comment \" " + temp + "\" Comment";
				}
			}
			else if(codeLine.charAt(i) == 'F')//feedrate
			{
				int index = i;
				while ((index<codeLine.length()) && !(codeLine.charAt(index) == ' '))
				{
					index++;
				}
				if(index == codeLine.length())
				{
					codeLine = codeLine.substring(0, i) + codeLine.substring(index);
				}
				else
					codeLine = codeLine.substring(0, i) + codeLine.substring(index+1);
			}
		}

		if (codeLine.length() == 0)
		{
			return "";
		}

		if (codeLine.contains("M83") == true)
		{
			return "Please set Extruder to Absolute mode";
		}
		/*
		 * Start code Recognition
		 */
		if(((codeLine.contains("G1") == true) && ((codeLine.contains("X") == true) || (codeLine.contains("Y") == true))) || ((codeLine.contains("G0") == true) && ((codeLine.contains("X") == true) || (codeLine.contains("Y") == true))))
		{
			int index  = 4;
			while (!(codeLine.charAt(index) == ' '))
			{
				index++;
			}
			index++;
			XPoint.add(Double.parseDouble(codeLine.substring(4, index)));
			index++;
			int index2 = index;
			while ((index<codeLine.length()) && (!(codeLine.charAt(index) == ' ')))
			{
				index++;
			}
			YPoint.add(Double.parseDouble(codeLine.substring(index2, index)));

			if(codeLine.length() > index + 3)
			{
				index++;
				index2 = index;
				index++;
				if(codeLine.charAt(index-1) == 'E')
				{
					while ((index<codeLine.length()) && (!(codeLine.charAt(index) == ' ')))
					{
						index++;
					}
					index++;
					index2 = index;
					index++;
				}
				if(codeLine.length() > index + 2)
				{
					if(codeLine.charAt(index-1) == 'Z')
					{
						while ((index<codeLine.length()) && (!(codeLine.charAt(index) == ' ')))
						{
							index++;
						}
						isExtrude.add(true);
						DeltaZ.add((Double.parseDouble(codeLine.substring(index2+1, index))));
						lastZ = Double.parseDouble(codeLine.substring(index2+1, index-1));
						return ("G1 or G0 Command detected - point added to X, Y, E, and Z ArrayLst");
					}
				}
				else
					DeltaZ.add(0.0);
				isExtrude.add(true);
				return ("G1 or G0 Command detected - point added to X, Y, E ArrayLst");
			}
			else
			{
				DeltaZ.add(0.0);
				isExtrude.add(false);
				return ("G1 or G0 Command detected - point added to X and Y ArrayLst");
			}
		}
		return codeLine + "             Error - Reached end of computation without resolving line type.";
	}
}
