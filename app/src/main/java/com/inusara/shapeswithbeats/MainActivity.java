package com.inusara.shapeswithbeats;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.*;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.*;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "ShapesOfBeats::Activity";
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat rgbaImg, grayImg, hierarchy;
    private List<MatOfPoint> contours, result;
    private MatOfPoint2f approxCurve;
    private List<Moments> imgMoments;
    private List<Point> massCenter;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity() {
        //Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.main_activity);
        mOpenCvCameraView.setMaxFrameSize(800, 480);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        rgbaImg = inputFrame.rgba();
        grayImg = new Mat();

        contours = new ArrayList<MatOfPoint>();
        result = new ArrayList<MatOfPoint>();
        approxCurve = new MatOfPoint2f();
        hierarchy = new Mat();
        imgMoments = new ArrayList<Moments>();
        massCenter = new ArrayList<Point>();

        Imgproc.cvtColor(rgbaImg, grayImg, Imgproc.COLOR_BGR2GRAY);
        //Imgproc.GaussianBlur(grayImg, grayImg, new Size(5, 5), 5);
        Imgproc.blur(grayImg, grayImg, new Size(3, 3));
        //Imgproc.threshold(grayImg, grayImg, 100, 255, Imgproc.THRESH_BINARY_INV);

        Imgproc.Canny(grayImg, grayImg, 100, 100*2);
        Imgproc.findContours(grayImg, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);


        //drawing = Mat::zeros(grayImg.size(), )
        for(int i = 0; i < contours.size(); i++) {
//            double contourArea = Imgproc.contourArea(contours.get(i));
//            if(contourArea > -1) {
//                Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), approxCurve, ((int)contours.get(i).total()*0.05), true);
////                if(approxCurve.total() == 8) {
////                    Imgproc.drawContours(rgbaImg, contours, i, new Scalar(255,0,0), 2, 8, hierarchy, 0, new Point());
////                }
//                //Imgproc.minEnclosingCircle(new MatOfPoint2f(contours.get(i).toArray()), );
//
//            }

            double contourArea = Imgproc.contourArea(contours.get(i));
            imgMoments.add(Imgproc.moments(contours.get(i)));

            //if(contourArea > -1) {
            Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), approxCurve, Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true) *0.02, true);
            massCenter.add(new Point(imgMoments.get(i).get_m10() / imgMoments.get(i).get_m00(), imgMoments.get(i).get_m01() / imgMoments.get(i).get_m00()));


            // MatOfPoint2f en = new MatOfPoint2f();
            // float ss[] = new float[2];

            //Imgproc.minEnclosingCircle(new MatOfPoint2f(contours.get(i).toArray()), new Point(massCenter.get(i).x, massCenter.get(i).y), ss);

            //Skip small or non-convex objects
            //if(contourArea > 100 || !Imgproc.isContourConvex(new MatOfPoint(contours.get(i).toArray()))) {
            if(contourArea < 100 || !Imgproc.isContourConvex(new MatOfPoint(approxCurve.toArray()))) {
                continue;
            }

            if (approxCurve.total() >= 8 && approxCurve.isContinuous()) {
                Core.circle(rgbaImg, new Point(massCenter.get(i).x, massCenter.get(i).y), 2, new Scalar(0, 255, 0), 2);
                Imgproc.drawContours(rgbaImg, contours, i, new Scalar(255, 0, 0), 2);
            }
        }

        return rgbaImg;
    }
}