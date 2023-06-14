package com.example.whatthe;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

public class CameraActivity extends AppCompatActivity
        implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;

    public native void smaller(long matAddrInput, long matAddrResult);

    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;

    // These variables are used (at the moment) to fix camera orientation from 270degree to 0degree
    Mat mRgba;
    Mat mRgbaF;
    Mat mRgbaT;

    private Mat matInput;
    private Mat matResult;
    Byte[] b;
    Button btnCapture;
    Button btnStop;
    TextView resultTV;

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


    private final Semaphore writeLock = new Semaphore(1);

    public void getWriteLock() throws InterruptedException { writeLock.acquire(); }
    public void releaseWriteLock() { writeLock.release(); }

    //소켓
    private Socket socket;
    private String ip = "192.168.43.136"; // IP
    //private String ip = "192.168.113.14"; // IP
    private int port = 8000;

    OutputStream outputStream;
    InputStream inputStream;

    private Timer cTimer;
    boolean timerFlag = false;
    boolean sendID = false;

    private Handler mHandler;
    String result;
    String getID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_camera);

        getID = getIntent().getStringExtra("ID");

        mHandler = new Handler();

        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.show_camera_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        mOpenCvCameraView.setCameraIndex(1);//1전면


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
        } else {
            Log.d(TAG, "Permissions granted");
            mOpenCvCameraView.setCameraPermissionGranted();
        }

        resultTV = (TextView) findViewById(R.id.resultView);
        Button btnEnd = (Button) findViewById(R.id.btnEnd);
        final int value = 0;
        btnEnd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                try {
                    //outputStream.close();
                    //inputStream.close();
                    if(socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finish();
            }
        });

        btnCapture = (Button) findViewById(R.id.btnCapture);
        btnStop = (Button) findViewById(R.id.btnStop);
        btnCapture.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                btnCapture.setVisibility(View.GONE);
                btnStop.setVisibility(View.VISIBLE);
                checkUpdate c = new checkUpdate();
                timerFlag = true;
                sendID = true;
                c.start();
                send_period();
            }
        });


        btnStop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                btnStop.setVisibility(View.GONE);
                timerFlag = false;
                cTimer.cancel();
                mOpenCvCameraView.setVisibility(SurfaceView.GONE);
                resultTV.setVisibility(View.VISIBLE);
            }
        });

    }

    class result_ex implements Runnable {
        private String msg;
        private String[] emotion = {"Angry","Disgusting","Fearful","Happy","Sad","Surprising","Neutral","NoPerson"};
        public result_ex(String str){
            this.msg = str;
        }
        @Override
        public void run() {
            String[] array = msg.split("/");
            //Log.d("???",msg);
            resultTV.setText("공부 시간 : "+array[0]+"\n감정 상태 : "+emotion[Integer.parseInt(array[1])]+"\n집중 점수 : "+array[2]);
        }
    }


    int w =0,h=0,f=0, dum=0;
    String lth;

    class checkUpdate extends Thread {
        @Override
        public void run() {
            try {
                socket = new Socket(ip, port);
                //w = matInput.cols();
                //h = matInput.rows();
                w = 640;
                h = 360;
                f = w * h;
                lth = Integer.toString(f);

                dum = lth.length();
                if (dum < 12) {
                    for (int i = 1; i+dum<=12; i++)
                        lth = "0" + lth;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void send_period(){
        cTimer = new Timer();

        TimerTask sp = new TimerTask(){
            @Override
            public void run() {
                try {

                    getWriteLock();

                    outputStream = socket.getOutputStream();
                    inputStream = socket.getInputStream();

                    byte[] imageInByte = new byte [f];
                    byte[] inst = lth.getBytes();

                    if ( matResult == null )
                        matResult = new Mat(matInput.rows(), matInput.cols(), matInput.type());

                    smaller(matInput.getNativeObjAddr(),matResult.getNativeObjAddr());

                    if(sendID == true){

                        int idLength = getID.length();
                        if (idLength < 12) {
                            for (int i = 1; i+idLength<=12; i++)
                                getID = "0" + getID;
                        }

                        byte[] student_id = getID.getBytes();
                        outputStream.write(student_id);
                        sendID = false;
                    }

                    matInput.get(0,0,imageInByte);
                    outputStream.write(inst);
                    for(int i = 0;i<f;i++){
                        outputStream.write((imageInByte[i] & 0xff));
                    }

                    String end = "000000000009";
                    byte[] endb = end.getBytes();

                    if(timerFlag == false){
                        outputStream.write(endb);

                        //Log.d("???","0009 보냄");
                        /*
                        byte[] d = new byte[50];
                        inputStream.read(d, 0, 32);
                        ByteBuffer b = ByteBuffer.wrap(d);
                        b.order(ByteOrder.LITTLE_ENDIAN);
                        int length = b.getInt();
                        byte[] dd = new byte[length];
                        inputStream.read(dd, 0, length);
                        result = new String(dd, "UTF-8");*/
                        byte[] buf = new byte[50];
                        int read_Byte = inputStream.read(buf);
                        result = new String(buf, 0, read_Byte);
                        mHandler.post(new result_ex(result));
                        Log.d("ClientThread", "받은 데이터 : " + result);
                    }

                }
                catch (Exception e) {
                }
                releaseWriteLock();
            }
        };

        cTimer.schedule(sp, 500, 1000);
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
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {

        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        matInput = inputFrame.rgba();

        return matInput;
    }

    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }


}
