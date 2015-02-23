package colorTracking;


import java.awt.Color;
import javax.swing.JFrame;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class ColorTrackerRunner {
    public static final int WIDTH = 720, HEIGHT = 480;
    public static final double CAPTURE_SCALE = 0.4;
    public static int FRAME = 0;

    public static void main(String arg[]) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        //make the JFrame
        JFrame frame = new JFrame("WebCam Capture - Object Tracking");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ColorTracker tracker = new ColorTracker();
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
        Mat thresholdImage;

        frame1 = new Mat();
        frame2 = new Mat();
        grayImage1 = new Mat();
        grayImage2 = new Mat();
        differenceImage = new Mat();
        thresholdImage = new Mat();

        VideoCapture capture = new VideoCapture();

        while (true) {
            capture.open("bouncingBall.avi"); // *** will need to update this

            if (!capture.isOpened()) {
                System.out.println("VideoCapture error");

            }
            else {
                try {
                    Thread.sleep(500);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                for (int i = 0; i < 38; i++) {
                    System.out.println("" + i + ": " + capture.get(i));
                }

                while (capture.get(1) < capture.get(7) - 2) { // there may be problems here ***
                    capture.read(frame1);
                    Imgproc.cvtColor(frame1, grayImage1, Imgproc.COLOR_BGR2GRAY);

                    capture.read(frame2);
                    Imgproc.cvtColor(frame2, grayImage2, Imgproc.COLOR_BGR2GRAY);

                    Core.absdiff(frame1, frame2, differenceImage);
                    Imgproc.threshold(differenceImage, thresholdImage, ColorTracker.SENSITIVITY_VALUE,
                                      255, Imgproc.THRESH_BINARY);

                    if (debugMode) {
                        //can add things here later
                    }
                    else {
                        //may need to destroy the other windows
                    }

                    Size mySize = new Size(ColorTracker.BLUR_SIZE, ColorTracker.BLUR_SIZE);
                    Imgproc.blur(thresholdImage, thresholdImage, mySize);
                    Imgproc.threshold(thresholdImage, thresholdImage, ColorTracker.SENSITIVITY_VALUE,
                              255, Imgproc.THRESH_BINARY);

                    if (debugMode) {
                        //more stuff here that I'll add later
                    }
                    else {
                        //yep, more stuff
                    }

                    if (trackingEnabled) {
                        tracker.searchForMovement(thresholdImage, frame1);
                    }

                    panel.matToBufferedImage(frame1);
                    panel.repaint();

                    FRAME++;
                }

            capture.release();
            FRAME = 0;
            }
        }// end of continuous while loop

    } //end of main

} //end of runner
