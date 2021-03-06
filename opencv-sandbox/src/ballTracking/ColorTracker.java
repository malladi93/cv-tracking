package ballTracking;

import java.util.List;
import java.util.ArrayList;

import org.opencv.imgproc.Imgproc;
import org.opencv.core.*;

public class ColorTracker {
	//hsv color values for tracking the specific object
	private float H_MIN = 0;
	private float H_MAX = 360;
	private float S_MIN = 0;
	private float S_MAX = 255;
	private float V_MIN = 0;
	private float V_MAX = 255;
	
	private Mat threshold;
	
	private int numObjects = 0;
	
	private Scalar color;
	
	private List<Rect> myROIs = new ArrayList<Rect>();
	
	//max number of objects to be tracked via this method
	private final int MAX_NUM_OBJECTS = 7;
	//min and max object area to be tracked
	private final double MIN_OBJECT_AREA = 20;
	private final double MAX_OBJECT_AREA = 300000;
	
	
	private boolean objectFound = false;
	private boolean track = true;
	
	private boolean useMorphOps = true;
	private int erodeSize = 3, dilateSize = 8;
	
	private boolean addGraphics = false;
	private boolean debug = false;
	
	public ColorTracker () {
		setColor(new Scalar(0,255,0));
		threshold = new Mat();
	}
	
	public Scalar getMinHSV() {
		return new Scalar(H_MIN, S_MIN, V_MIN);
	}
	
	public Scalar getMaxHSV() {
		return new Scalar(H_MAX, S_MAX, V_MAX);
	}
	
	public void setMinHSV(float f, float g, float h) {
		H_MIN = f;
		S_MIN = g;
		V_MIN = h;
	}
	
	public void setMaxHSV(float f, float g, float h) {
		H_MAX = f;
		S_MAX = g;
		V_MAX = h;
	}
	
	public void track(boolean b) {
		track = b;
	}
	
	public boolean track() {
		return track;
	}
	
	public boolean useMorphOps() {
		return useMorphOps;
	}
	
	public void useMorphOps(boolean b) {
		useMorphOps = b;
	}
	
	public void morphOps(Mat thresh) {
		Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(erodeSize, erodeSize));
		Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(dilateSize, dilateSize));
		
		Imgproc.erode(thresh, thresh, erodeElement);
		Imgproc.erode(thresh, thresh, erodeElement);
		
		Imgproc.dilate(thresh, thresh, dilateElement);
		Imgproc.dilate(thresh, thresh, dilateElement);
	}
	
	public void resetMyROIs () { //may want to change method name...
		myROIs.clear();
	}
	
	public void setColor(int r, int g, int b) {
		color = new Scalar(r, g, b);
	}
	
	public void addGraphics(boolean b) {
		addGraphics = b;
	}
	
	public Mat getThreshold() {
		return threshold;
	}
	
	public void trackColor(Mat videoFrame, Mat graphicsFrame, List<Obj> objects) {
		Mat hsvFrame = new Mat();
		Imgproc.cvtColor(videoFrame, hsvFrame, Imgproc.COLOR_BGR2HSV);
		threshold = new Mat();
		Core.inRange(hsvFrame, getMinHSV(), getMaxHSV(), threshold);
		
		if (useMorphOps) {
			morphOps(threshold);
		}
		
		trackColor1(threshold, graphicsFrame, objects);
	}
	
	public void trackColor1(Mat threshold, Mat graphicsFrame, List<Obj> objects) {
		Mat temp = new Mat();
		threshold.copyTo(temp);
		
		resetMyROIs();
		
		
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(temp, contours, hierarchy, Imgproc.RETR_EXTERNAL,
							 Imgproc.CHAIN_APPROX_SIMPLE);
		
		
		if (contours.size() > 0) objectFound = true;
		else objectFound = false;
		
		if (objectFound) {
			//List<MatOfPoint> largestContourVec = new ArrayList<MatOfPoint>();
			//largestContourVec.add(contours.get(0)); //add a for loops here?
			
			double largestArea = 0;
			double smallestArea = 100000000;
			
			numObjects = 0;
			
			for (MatOfPoint mop : contours) {
				Rect nextRect = Imgproc.boundingRect(mop);
				if (nextRect.area() > MIN_OBJECT_AREA && nextRect.area() < MAX_OBJECT_AREA
					&& numObjects < MAX_NUM_OBJECTS) {
					if (nextRect.area() > largestArea) largestArea = nextRect.area();
					if (nextRect.area() < smallestArea) smallestArea = nextRect.area();
					myROIs.add(nextRect);
					Obj nextObj = new Obj(nextRect);
					objects.add(nextObj);
					numObjects++;
				}
			}
			if (debug) {
				System.out.println("===large-area: " + largestArea);
				System.out.println("===small-area: " + smallestArea);
			}
			
			//draws text and bounding rectangle to image
			
			int rectCounter = 0;
			
			if (addGraphics) {
				for (Rect boundingRect : myROIs) {
					
					double x = boundingRect.x + boundingRect.width / 2;
					double y = boundingRect.y + boundingRect.height / 2;
					
					Core.putText(graphicsFrame, "tracking object: " + rectCounter, new Point(x,y), 2,
							 .67, new Scalar(0,255,0),
							 (int)(2));
					
					Core.rectangle(graphicsFrame, new Point(boundingRect.x, boundingRect.y),
							   	   new Point(boundingRect.x + boundingRect.width, boundingRect.y + boundingRect.height),
							   	   new Scalar(0,255,0));
					rectCounter++;
				}
			}
			if (debug) System.out.println("tracking " + myROIs.size() + " object(s)\n");
		}
	}

	public Scalar getColor() {
		return color;
	}

	public void setColor(Scalar color) {
		this.color = color;
	}
}
