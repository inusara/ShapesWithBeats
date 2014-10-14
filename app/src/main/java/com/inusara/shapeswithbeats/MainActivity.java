package com.inusara.shapeswithbeats;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.*;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.*;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
    MediaPlayer bg_beat, beats1, beats2, beats3, beats4;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();

                    AudioManager audioManager = (AudioManager) getSystemService(getApplicationContext().AUDIO_SERVICE);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 20, 0);

                    bg_beat = MediaPlayer.create(getApplicationContext(), R.raw.bgloop);

                    bg_beat.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            bg_beat.setLooping(true);
                            bg_beat.start();
                        }
                    });

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
        bg_beat.pause();
        if(beats1 != null) {
            beats1.pause();
        }
        if(beats2 != null) {
            beats2.pause();
        }
        if(beats3 != null) {
            beats3.pause();
        }
        if(beats4 != null) {
            beats4.pause();
        }
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
        if(beats1 != null) {
            beats1.stop();
            beats1.release();
        }
        if(beats2 != null) {
            beats2.stop();
            beats2.release();
        }
        if(beats3 != null) {
            beats3.stop();
            beats3.release();
        }
        if(beats4 != null) {
            beats4.stop();
            beats4.release();
        }
        bg_beat.stop();
        bg_beat.release();
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

                int shapeX = (int)massCenter.get(i).x;
                int shapeY = (int)massCenter.get(i).y;

                double[] shapeRGB = rgbaImg.get(shapeX, shapeY);

                if(shapeRGB != null) {
                    double[] colorConversion = { 255 - shapeRGB[0], 255 - shapeRGB[1], 255 - shapeRGB[2] };

                    if(colorConversion[0] >= 0 && colorConversion[1] >= 0 && colorConversion[2] >= 0) {
                        shapeColor((int)colorConversion[0], (int)colorConversion[1], (int)colorConversion[2]);
                        //Log.i("Color", "Red - " + shapeRGB[0] + " Green - " + shapeRGB[1] + " Blue - " + shapeRGB[2]);
                    }
                }

            }

        }

        return rgbaImg;
    }

    public void shapeColor(int red, int green, int blue) {
       beats1 = MediaPlayer.create(getApplicationContext(), R.raw.beats1);
       beats2 = MediaPlayer.create(getApplicationContext(), R.raw.beats2);
       beats3 = MediaPlayer.create(getApplicationContext(), R.raw.beats3);
       beats4 = MediaPlayer.create(getApplicationContext(), R.raw.beats4);

        if(red > 100 && green < 100 && blue < 100) { //if red
            if(!beats1.isPlaying()) {
                beats1.start();
            }
        } else if(red < 100 && green > 100 && blue < 100) { //if green
            if(!beats2.isPlaying()) {
                beats2.start();
            }
        } else if(red < 100 && green < 100 && blue > 100) { //if blue
            if(!beats3.isPlaying()) {
                beats3.start();
            }
        } else { //color not covered
            if(!beats4.isPlaying()) {
                beats4.start();
            }
        }
    }
}