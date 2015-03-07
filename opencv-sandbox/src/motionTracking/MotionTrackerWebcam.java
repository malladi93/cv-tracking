package motionTracking;


import java.awt.Color;
import javax.swing.JFrame;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class MotionTrackerWebcam {
	public static final int WIDTH = 720, HEIGHT = 480;
	public static final double CAPTURE_SCALE = 1;
	public static int FRAME = 0;
	
	public static void main(String arg[]) {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		//make the JFrame
		JFrame frame = new JFrame("WebCam Capture - Object Tracking");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		MotionTracker tracker = new MotionTracker();
		GraphicsPanel panel = new GraphicsPanel();
		
		frame.setSize(WIDTH, HEIGHT);
		frame.setBackground(Color.BLACK);
		frame.add(panel);
		frame.setVisible(true);
		
		boolean objectDetected = false;
		boolean debugMode = false;
		boolean trackingEnabled = true;
		boolean pause = false;
		
		Mat frame1, frame2;
		Mat grayImage1, grayImage2;
		Mat differenceImage;
		Mat threshold;
		
		frame1 = new Mat();
		frame2 = new Mat();
		grayImage1 = new Mat();
		grayImage2 = new Mat();
		differenceImage = new Mat();
		threshold = new Mat();
		
		VideoCapture capture = new VideoCapture(0);
		
		capture.set(3, WIDTH * CAPTURE_SCALE);
		capture.set(4, HEIGHT * CAPTURE_SCALE);
		
		if (capture.isOpened()) {
			try {
				Thread.sleep(500);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			while (true) {
				
				
				/* for (int i = 0; i < 38; i++) {
					System.out.println("" + i + ": " + capture.get(i));
				} */
				
				capture.read(frame1);
				Imgproc.cvtColor(frame1, grayImage1, Imgproc.COLOR_BGR2GRAY);
				
				//may need to add a Thread.sleep(20) here
				
				capture.read(frame2);
				Imgproc.cvtColor(frame2, grayImage2, Imgproc.COLOR_BGR2GRAY);
				
				Core.absdiff(frame1, frame2, differenceImage);
				Imgproc.threshold(differenceImage, threshold, MotionTracker.SENSITIVITY_VALUE,
							      255, Imgproc.THRESH_BINARY);
				
				if (debugMode) {
					//can add things here later
				}
				else {
					//may need to destroy the other windows
				}
				
				Size mySize = new Size(MotionTracker.BLUR_SIZE, MotionTracker.BLUR_SIZE);
				Imgproc.blur(threshold, threshold, mySize);
				Imgproc.threshold(threshold, threshold, MotionTracker.SENSITIVITY_VALUE,
					      255, Imgproc.THRESH_BINARY); //is this reduntant from above?
				
				if (debugMode) {
					//more stuff here that I'll add later
				}
				else {
					//yep, more stuff
				}
				
				if (trackingEnabled) {
					tracker.searchForMovement(threshold, frame1);
				}
				
				panel.matToBufferedImage(threshold);
				panel.repaint();
				
				FRAME++;
				
			}
		}
		
		else {
			System.out.println("Problem initializing videocapture");
		}

	} //end of main
	
} //end of runner