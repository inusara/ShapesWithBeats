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
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;


public class MainActivity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "ShapesOfBeats::Activity";
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat rgbaImg, grayImg, frameCircles;

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
        Log.i(TAG, "Instantiated new " + this.getClass());
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
        grayImg = inputFrame.gray();
        frameCircles = new Mat();

        //detect circles in frame
        Imgproc.GaussianBlur( grayImg, grayImg, new Size(9, 9), 2, 2 );
        Imgproc.HoughCircles(grayImg, frameCircles, Imgproc.CV_HOUGH_GRADIENT, 1.2, grayImg.rows() / 4, 200, 80, 30, 400);

        if(frameCircles.cols() > 0) {
            for(int i = 0; i < frameCircles.cols(); i++) {
                double vCircle[] = frameCircles.get(0,i);

                if (vCircle == null)
                    break;

                Point center = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
                int rad = (int)Math.round(vCircle[2]);

                Core.circle( rgbaImg, center, rad, new Scalar(255,0,0), 3, 8, 0 ); //draws circle outline
            }
        }

        return rgbaImg;
    }
}
