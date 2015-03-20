package ballTracking;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;

class GraphicsPanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener{
     private static final long serialVersionUID = 1L;
     private BufferedImage image;
     
     private boolean debug = false;
     
     List<Obj> objects;
     
     private int startX;
     private int endX;
     private int startY;
     private int endY;
     
     private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
     
     private int[] rect = new int[4];
     private float[] averageHSV, minHSV, maxHSV;
     
     private boolean drawRect = false;
     private Color rectColor = new Color(0, 0, 255, 50);
     
     
     // Create a constructor method
     public GraphicsPanel(){
          super();
          
          objects = new ArrayList<Obj>();
          
          this.addMouseListener(this);
          this.addMouseMotionListener(this);
     }
     
     /*
      * Converts/writes a Mat into a BufferedImage.
      * 
      * @param matrix Mat of type CV_8UC3 or CV_8UC1
      * @return BufferedImage of type TYPE_3BYTE_BGR or TYPE_BYTE_GRAY
      */
     public boolean matToBufferedImage(Mat matrix) {
          MatOfByte mb = new MatOfByte();
          Highgui.imencode(".jpg", matrix, mb);
          try {
               this.image = ImageIO.read(new ByteArrayInputStream(mb.toArray()));
          } catch (IOException e) {
               e.printStackTrace();
               return false; // Error
          }
       return true; // Successful
     }
     
     public void addObject(Obj object) {
    	 objects.add(object);
     }
     
     public void addObjects(List<Obj> newObjects) {
    	 objects.addAll(newObjects);
     }
     
     public void clearObjects() {
    	 objects.clear();
     }
     
     public void setObjects(List<Obj> newObjects) {
    	 clearObjects();
    	 addObjects(newObjects);
     }
     
     public int getRGB(int x, int y) {
    	 return image.getRGB(x, y);
     }
     
     public int getRed(int x, int y) {
    	 Color color = new Color(getRGB(x, y));
    	 return color.getRed();
     }
     
     public int getGreen(int x, int y) {
    	 Color color = new Color(getRGB(x, y));
    	 return color.getGreen();
     }
     
     public int getBlue(int x, int y) {
    	 Color color = new Color(getRGB(x, y));
    	 return color.getBlue();
     }
     
     public float[] getRawHSV(int x, int y) {
    	 return Color.RGBtoHSB(getRed(x, y), getGreen(x, y), getBlue(x, y), null);
     }
     
     public float[] getHSV(int x, int y) {
    	 float[] hsvVals = getRawHSV(x, y);
    	 hsvVals[0] = hsvVals[0] * 360;
    	 hsvVals[1] = hsvVals[1] * 255;
    	 hsvVals[2] = hsvVals[2] * 255;
    	 
    	 return hsvVals;
     }
     
     public float[] getAverageHSV(int x, int y, int width, int height) {
    	 float[] hsvVals = {0, 0, 0};
    	 
    	 for (int row = x; row < x + width; row++) {
    		 for (int col = y; col < y + height; col++) {
    			 float[] nextHSV = getHSV(x, y);
    			 hsvVals[0] += nextHSV[0];
    			 hsvVals[1] += nextHSV[1];
    			 hsvVals[2] += nextHSV[2];
    		 }
    	 }
    	 
    	 hsvVals[0] = hsvVals[0] / (width * height);
    	 hsvVals[1] = hsvVals[1] / (width * height);
    	 hsvVals[2] = hsvVals[2] / (width * height);
    	 
    	 return hsvVals;
     }
     
     public float[] getAverageHSV(int[] rect) {
    	 return getAverageHSV(rect[0], rect[1], rect[2], rect[3]);
     }
     
     public void updateAverageHSV() {
    	 averageHSV = getAverageHSV(rect);
     }
     
     public String getAverageHSVAsString() {
    	 return "h: " + averageHSV[0] + "; s: " + averageHSV[1] + "; v: " + averageHSV[2];
     }
     
     public float[] getMinHSV(int x, int y, int width, int height) {
    	 float[] hsvVals = {360, 255, 255};
    	 
    	 for (int row = x; row < x + width; row++) {
    		 for (int col = y; col < y + height; col++) {
    			 float[] nextHSV = getHSV(row, col);
    			 if (nextHSV[0] < hsvVals[0]) hsvVals[0] = nextHSV[0];
    			 if (nextHSV[1] < hsvVals[1]) hsvVals[1] = nextHSV[1];
    			 if (nextHSV[2] < hsvVals[2]) hsvVals[2] = nextHSV[2];
    		 }
    	 }
    	 return hsvVals;
     }
     
     public float[] getMinHSV(int[] rect) {
    	 return getMinHSV(rect[0], rect[1], rect[2], rect[3]);
     }
     
     public float[] getMinHSV() {
    	 return minHSV;
     }
     
     public void updateMinHSV() {
    	 minHSV = getMinHSV(rect);
     }
     
     public float[] getMaxHSV(int x, int y, int width, int height) {
    	 float[] hsvVals = {0, 0, 0};
    	 
    	 for (int row = x; row < x + width; row++) {
    		 for (int col = y; col < y + height; col++) {
    			 float[] nextHSV = getHSV(row, col);
    			 if (nextHSV[0] > hsvVals[0]) hsvVals[0] = nextHSV[0];
    			 if (nextHSV[1] > hsvVals[1]) hsvVals[1] = nextHSV[1];
    			 if (nextHSV[2] > hsvVals[2]) hsvVals[2] = nextHSV[2];
    		 }
    	 }
    	 return hsvVals;
     }
     
     public float[] getMaxHSV(int[] rect) {
    	 return getMaxHSV(rect[0], rect[1], rect[2], rect[3]);
     }
     
     public float[] getMaxHSV() {
    	 return maxHSV;
     }
     
     public void updateMaxHSV() {
    	 maxHSV = getMaxHSV(rect);
     }
     
     public void setMinMaxHSV(int tolerance) {
    	 setMinH(averageHSV[0] - tolerance * 4);
    	 setMinS(averageHSV[1] - tolerance);
    	 setMinV(averageHSV[2] - tolerance);
    	 
    	 setMaxH(averageHSV[0] + tolerance * 4);
    	 setMaxS(averageHSV[1] + tolerance);
    	 setMaxV(averageHSV[2] + tolerance);
     }
     
     public void setMinH(float h) {
    	 if (h > 360) h = 360;
    	 else if (h < 0) h = 0;
    	 minHSV[0] = h;
     }
     
     public void setMinS(float s) {
    	 if (s > 255) s = 255;
    	 else if (s < 0) s = 0;
    	 minHSV[1] = s;
     }
     
     public void setMinV(float v) {
    	 if (v > 255) v = 255;
    	 else if (v < 0) v = 0;
    	 minHSV[2] = v;
     }
     
     public void setMaxH(float h) {
    	 if (h > 360) h = 360;
    	 else if (h < 0) h = 0;
    	 maxHSV[0] = h;
     }
     
     public void setMaxS(float s) {
    	 if (s > 255) s = 255;
    	 else if (s < 0) s = 0;
    	 maxHSV[1] = s;
     }
     
     public void setMaxV(float v) {
    	 if (v > 255) v = 255;
    	 else if (v < 0) v = 0;
    	 maxHSV[2] = v;
     }
     
     
     
     public int[] getRect(int x0, int y0, int x1, int y1) {
    	 int lowerX;
    	 int lowerY;
    	 
    	 if (x0 < x1) lowerX = x0;
    	 else lowerX = x1;
    	 if (y0 < y1) lowerY = y0;
    	 else lowerY = y1;
    	 
    	 int width  = Math.abs(x0 - x1);
    	 int height = Math.abs(y0 - y1);
    	 
    	 int[] result = {lowerX, lowerY, width, height};
    	 
    	 return result;
     }
     
     public void updateRect() {
    	 int[] updatedRect = getRect(startX, startY, endX, endY);
    	 rect = updatedRect;
     }
     
     public String getRectAsString() {
    	 String string = "x: " + rect[0] + "; y: " + rect[1] +
		 		 "; width: " + rect[2] + "; height: " + rect[3];
    	 
    	 return string;
     }
     
     public void printRect() {
    	 System.out.println(getRectAsString());
     }
     
     public void updateRectData() {
    	 if (debug) System.out.println("update rect data");
    	 updateRect();
    	 
    	 updateMinHSV();
    	 updateMaxHSV();
    	 
    	 updateAverageHSV();
     }
     
     public String getRectDataAsString() {
    	 String string = getRectAsString();
    	 
    	 string += "\naverageHSV:" + Arrays.toString(averageHSV);
    	 string += "\nminHSV:" + Arrays.toString(minHSV);
    	 string += "\nmaxHSV:" + Arrays.toString(maxHSV);
    	 
    	 return string;
     }
     
     public void printRectData() {
    	 System.out.println(getRectDataAsString());
     }
     
     public boolean updateImage(Mat matrix) {
    	 if (matToBufferedImage(matrix)) {
    		 this.repaint();
    		 return true;
    	 }
    	 else return false;
     }
     
     public void paintComponent(Graphics g){
          super.paintComponent(g);
          if (this.image==null) return;
        
          g.drawImage(this.image, 0, 0, GraphicsFrame.WIDTH, GraphicsFrame.HEIGHT, null);
          g.setColor(Color.WHITE);
          g.setFont(new Font("", 0, 20));
          g.drawString("Frame: " + GraphicsFrame.FRAME, 50, 50);
          
          if (objects.size() > 0) {
	          for (Obj object : objects) {
	        	  object.draw(g);
	          }
          }
          
          if (drawRect) {
        	  g.setColor(rectColor);
        	  g.fillRect(rect[0], rect[1], rect[2], rect[3]);
          }
          
     }
     
     
     
     
     public void addActionListener(ActionListener al) {
    	 listeners.add(al);
     }
     
     public void removeActionListener(ActionListener al) {
    	 listeners.remove(al);
     }
     
     public void fireActionPerformed(ActionEvent ae) {
    	 for (ActionListener al : listeners) {
    		 al.actionPerformed(ae);
    	 }
     }
     
     public void fireActionPerformed() {
    	 fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
    			 			 getAverageHSVAsString()));
     }
     
	@Override
	public void mouseClicked(MouseEvent e) {
		
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mousePressed(MouseEvent e) {
		if (debug) System.out.println("mouse pressed");
		drawRect = true;
		startX = e.getX();
		startY = e.getY();
		
		endX = startX;
		endY = startY;
		
		updateRect();
		
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		if (debug) System.out.println("mouse released");
		drawRect = false;
		endX = e.getX();
		endY = e.getY();
		
		if (startX != endX || startY != endY) {
			if (endX < 0) endX = 0;
			if (endY < 0) endY = 0;
			
			updateRectData();
			setMinMaxHSV(40);
			if (debug) printRectData();
			fireActionPerformed();
		}
	}
	@Override
	public void mouseDragged(MouseEvent e) {
		endX = e.getX();
		endY = e.getY();
		
		updateRect();
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
      
}  