package com.example.nekketsu.boundaryidcard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

//http://answers.opencv.org/question/16993/display-image/
//http://stackoverflow.com/questions/5991319/capture-image-from-camera-and-display-in-activity
//http://stackoverflow.com/questions/28889044/how-to-pause-and-resume-listening-to-frames-from-cvcameraviewlistener2


class VerifyListener implements DialogInterface.OnClickListener
{

    ImageView iv;
    public VerifyListener(ImageView iv) {
        this.iv = iv;
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {

    }
};

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static String TAG = "MainActivity";
    private CameraLine mOpenCvCameraView;
    private Mat rgba;
    private Mat roi;
    private Mat mIntermediateMat;
    private Mat mGray;
    private Bitmap region_of_interest;
    boolean choiceChosen = false;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "Opencv Load successfully");
                    mOpenCvCameraView.enableView();
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                }
            }
        }
    };

    public final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.arg1 == 1)
                Toast.makeText(getApplicationContext(), "ID Card Saved!", Toast.LENGTH_LONG).show();
        }
    };

    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraLine) findViewById(R.id.MainActivityCameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

    }


    @Override
    public void onCameraViewStarted(int width, int height) {
        rgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {

    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        rgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        Imgproc.Canny(inputFrame.gray(), mIntermediateMat, 80, 100);
//        Imgproc.erode(mIntermediateMat,mIntermediateMat,Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2)));
        Imgproc.dilate(mIntermediateMat, mIntermediateMat, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(4, 4)));
//        Imgproc.cvtColor(mIntermediateMat, rgba, Imgproc.COLOR_GRAY2RGBA, 4);

        //---------------------------------------- region of interest ----------------
        Rect rect = new Rect(288, 10, 295, 455);
        if (rgba.width() > rect.width && rgba.height() > rect.height) {
            notifyObserver();
            roi = rgba.submat(rect);
            Mat testCrop = new Mat(rgba.rows(), rgba.cols(), CvType.CV_8UC4, new Scalar(0, 0, 0, 0));
            roi.copyTo(testCrop.submat(rect));

//            return testCrop;
        }

        return mIntermediateMat;
        //-------------------------------------------------------------------------------

    }

    public void notifyObserver() {
//        http://stackoverflow.com/questions/17338488/detecting-edges-of-a-card-with-rounded-corners
//        http://stackoverflow.com/questions/40192541/how-to-detect-the-bounds-of-a-passport-page-with-opencv
        boolean overTL = false, overTR = false, overBL = false, overBR = false;

        if (mIntermediateMat.get(410, 280)[0] > 0.0) {
            overTL = true;
        } else {
            overTL = false;
        }

        if (mIntermediateMat.get(10, 280)[0] > 0.0) {
            overTR = true;
        } else {
            overTR = false;
        }

        if (mIntermediateMat.get(410, 510)[0] > 0.0) {
            overBL = true;
        } else {
            overBL = false;
        }

        if (mIntermediateMat.get(10, 510)[0] > 0.0) {
            overBR = true;
        } else {
            overBR = false;
        }
        mOpenCvCameraView.update(overTL, overTR, overBL, overBR);

        if (overTL && overBL && overBR && overTR) {
            acceptImage();
        }
    }

    public void acceptImage() {

        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bmp = null;
                        try {
                            bmp = Bitmap.createBitmap(roi.cols(), roi.rows(), Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(roi, bmp);
                        } catch (CvException e) {

                        }
                        ImageView iv = (ImageView) findViewById(R.id.imageView);
                        region_of_interest = bmp;
                        iv.setImageBitmap(region_of_interest);

                        verifyImage(iv);
                    }
                });
    }


    public void verifyImage(ImageView iv) {

        VerifyListener dialogClickListener = new VerifyListener(iv) {

            //@Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        saveImage(region_of_interest);
                        iv.setImageResource(android.R.color.transparent);
                        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        iv.setImageResource(android.R.color.transparent);
                        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
                        break;
                }

            }
        };
        mOpenCvCameraView.setVisibility(SurfaceView.GONE);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.requestWindowFeature(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        builder.setMessage("Do you want to accept this image?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void saveImage(Bitmap bmp) {
        FileOutputStream out = null;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateandTime = sdf.format(new Date());
        String filename = currentDateandTime + "_ROI.png";

        File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        boolean success = true;
        if (!sd.exists()) {
            success = sd.mkdir();
        }
        if (success) {
            File dest = new File(sd, filename);

            try {
                out = new FileOutputStream(dest);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (Exception e) {
                e.printStackTrace();

            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        }
        Message msg = handler.obtainMessage();
        msg.arg1 = 1;
        handler.sendMessage(msg);
    }
}



