package com.example.varun.firedetectionju;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String pathname = "/storage/emulated/0/Android/data/com.example.varun.firedetectionju/files/FIRE_SAMPLE_1.jpg";

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static{
        ORIENTATIONS.append(Surface.ROTATION_0,90);
        ORIENTATIONS.append(Surface.ROTATION_90,0);
        ORIENTATIONS.append(Surface.ROTATION_180,270);
        ORIENTATIONS.append(Surface.ROTATION_270,180);
    }

    private TextureView textureView;
    private TextView output;
    TextView secondProg;
    Button mainButton;
    ImageButton cancelButton;
    ProgressBar progressBar;

    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    int numberOfPicture = 2;
    int mPictureCounter = 1;
    int nameCounter = 1;

    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private Handler handler;

    final MyAsyncTask2 task2 = new MyAsyncTask2();

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("TAG", "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) { }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) { }
    };

    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            cameraDevice=null;
        }
    };

    ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            file = new File(getExternalFilesDir(null), "FIRE_SAMPLE_"+Integer.toString(nameCounter++)+".jpg");
            if (nameCounter == (numberOfPicture+1))
                nameCounter = 1;
            mBackgroundHandler.post(new ImageSaver(imageReader.acquireLatestImage(), file));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.textureView);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);

        mainButton = findViewById(R.id.mainButton);
        cancelButton = findViewById(R.id.cancelButton);
        progressBar = findViewById(R.id.progressBar);
        output = findViewById(R.id.output);
        secondProg = findViewById(R.id.textView);
        secondProg.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        cancelButton.setVisibility(View.INVISIBLE);

        handler = new Handler(getApplicationContext().getMainLooper());
        mainButton.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if(textureView.isAvailable())
            openCamera();
        else
            textureView.setSurfaceTextureListener(textureListener);

        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "OpenCV Load Failed!", Toast.LENGTH_SHORT).show();
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, baseLoaderCallback);
        } else {
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }

    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        mainButton.setEnabled(false);
        if(task2.getStatus() == AsyncTask.Status.RUNNING){
            Log.d("MyTag", "task2 running");
            task2.cancel(true);
        }
        while (task2.getStatus() == AsyncTask.Status.RUNNING){
            Log.d("MyTag", " waiting for task2 to stop");
        }
        takePicture();
    }

    private void openCamera() {
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            //Check realtime permission if run higher API 23
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId,stateCallback,null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreview() {
        try{
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert  texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(),imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if(cameraDevice == null)
                        return;
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "Changed", Toast.LENGTH_SHORT).show();
                }
            },null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if(cameraDevice == null)
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_MODE_AUTO);
        try{
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(),null,mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CAMERA_PERMISSION)
        {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "You can't use camera without permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void takePicture() {
        if(cameraDevice == null)
            return;
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if(characteristics != null)
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        .getOutputSizes(ImageFormat.JPEG);

            //Capture image with custom size
            int width = 640;
            int height = 480;
            //if(jpegSizes != null && jpegSizes.length > 0)
            //{
            //    width = jpegSizes[0].getWidth();
            //    height = jpegSizes[0].getHeight();
            //}
            final ImageReader reader = ImageReader.newInstance(width,height,ImageFormat.JPEG,1);
            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(reader.getSurface());
            outputSurface.add(new Surface(textureView.getSurfaceTexture()));

            final List<CaptureRequest> captureList = new ArrayList<CaptureRequest>();
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            //Check orientation base on device
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,ORIENTATIONS.get(rotation));

            for (int i=0;i<numberOfPicture;i++) {
                captureList.add(captureBuilder.build());
            }

            reader.setOnImageAvailableListener(readerListener,mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    mPictureCounter++;
                    if (mPictureCounter >= (numberOfPicture+1)) {
                        Toast.makeText(MainActivity.this, "Image Saved", Toast.LENGTH_SHORT).show();
                        mPictureCounter = 1;
                        createCameraPreview();
                        try {
                            generateOutput();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try{
                        cameraCaptureSession.stopRepeating();
                        cameraCaptureSession.captureBurst(captureList, captureListener ,mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                }
            },mBackgroundHandler);


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try{
            mBackgroundThread.join();
            mBackgroundThread= null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private static class ImageSaver implements Runnable {

        private final Image mImage;
        private final File mFile;

        ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void generateOutput() throws InterruptedException {
        handler.post(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                mainButton.setText(R.string.analysis_button_msg);
            }
        });

        final MyAsyncTask task = new MyAsyncTask();
        task.execute();
        task2.execute();
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task.cancel(true);
                task2.cancel(true);
            }
        });
    }

    private class MyAsyncTask extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            AnalyseFire fire = new AnalyseFire();
            AnalyseFire2 fire2 = new AnalyseFire2();
            return fire.fireCheck();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            output.setText(R.string.analysis_msg);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            int fireCheck = integer;
            if (fireCheck==0) {
                output.setText("No FIRE Detected!");
                task2.cancel(true);
            }
            else {
                output.setText("FIRE Detected by 1st Algo!");
                secondProg.setVisibility(View.VISIBLE);
            }


            progressBar.setVisibility(View.INVISIBLE);
            cancelButton.setVisibility(View.INVISIBLE);
            mainButton.setText(R.string.main_button);
            mainButton.setEnabled(true);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            progressBar.setVisibility(View.INVISIBLE);
            cancelButton.setVisibility(View.INVISIBLE);
            mainButton.setText(R.string.main_button);
            mainButton.setEnabled(true);
            output.setText(R.string.analysis_cancel_msg);
        }
    }

    private class MyAsyncTask2 extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {

            Mat imgHSV = Imgcodecs.imread(pathname);
            int x1 = imgHSV.rows();
            int y1 = imgHSV.cols();
            imgHSV.convertTo(imgHSV, CvType.CV_32FC3, 1.0/255.0);
            Imgproc.cvtColor(imgHSV, imgHSV, Imgproc.COLOR_BGR2HSV);

            List<Mat> lHsv = new ArrayList<Mat>(3);
            Core.split(imgHSV, lHsv);
            Mat hChannel = lHsv.get(0);
            Mat sChannel = lHsv.get(1);
            Mat vChannel = lHsv.get(2);

            Core.multiply(sChannel, new Scalar(2), sChannel);
            Mat newS = sChannel.clone();

            Scalar meanV = Core.mean(vChannel);
            Mat newV = vChannel_correction(vChannel, meanV.val[0]);

            List<Mat> listMat = Arrays.asList(hChannel, newS, newV);
            Core.merge(listMat, imgHSV);
            Imgproc.cvtColor(imgHSV, imgHSV, Imgproc.COLOR_HSV2RGB);

            List<Mat> klm = new ArrayList<Mat>(3);
            Core.split(imgHSV, klm);
            Mat k = klm.get(0);
            Mat l = klm.get(1);
            Mat m = klm.get(2);

            Scalar L01 = Core.sumElems(k);
            Scalar a01 = Core.sumElems(l);
            Scalar b01 = Core.sumElems(m);
            double RED = L01.val[0]/(x1*y1);
            double GREEN = a01.val[0]/(x1*y1);
            double BLUE = b01.val[0]/(x1*y1);

            Log.d("MyTAG", "L01 : "+L01);
            Log.d("MyTAG", "a01 : "+a01);
            Log.d("MyTAG", "b01 : "+b01);
            Log.d("MyTAG", "RED : "+RED);
            Log.d("MyTAG", "GREEN : "+GREEN);
            Log.d("MyTAG", "BLUE : "+BLUE);

            Mat allBlack = Mat.zeros(x1, y1, CvType.CV_32FC1);
            List<Mat> temp_r = Arrays.asList(k, allBlack, allBlack);
            List<Mat> temp_g = Arrays.asList(allBlack, l, allBlack);
            List<Mat> temp_b = Arrays.asList(allBlack, allBlack, m);
            Mat red = new Mat();
            Mat green = new Mat();
            Mat blue = new Mat();
            Core.merge(temp_r, red);
            Core.merge(temp_g, green);
            Core.merge(temp_b, blue);

            Mat a1 = Mat.zeros(x1, y1, CvType.CV_8UC1);
            Mat b1 = Mat.zeros(x1, y1, CvType.CV_8UC1);
            Mat g = Mat.zeros(x1, y1, CvType.CV_8UC1);
            Mat a2 = Mat.zeros(x1, y1, CvType.CV_8UC1);
            //Mat b2 = Mat.zeros(x1, y1, CvType.CV_8UC1);
            Mat h = Mat.zeros(x1, y1, CvType.CV_8UC1);

            for(int row=0; row<x1; row++){
                for(int col=0; col<y1; col++) {
                    if (isCancelled())
                        break;
                    if(k.get(row, col)[0] >= l.get(row, col)[0])
                        a1.put(row, col, 1);
                    else
                        a1.put(row, col, 0);

                    if((k.get(row, col)[0] >= RED) && (l.get(row, col)[0] >= GREEN) && (m.get(row, col)[0] >= BLUE))
                        b1.put(row, col, 1);
                    else
                        b1.put(row, col, 0);

                    if((newS.get(row, col)[0] <= 0.38) && (newV.get(row, col)[0] >= 1))
                        g.put(row, col, 1);
                    else
                        g.put(row, col, 0);

                    if((g.get(row, col)[0]!=0.0) && (a1.get(row, col)[0]!=0.0) && (b1.get(row, col)[0]==1)) {
                        h.put(row, col, 1);
                        //newV.put(row, col, 1);
                        a2.put(row, col, row);
                        //b2.put(row, col, col);
                    }
                    else {
                        h.put(row, col, 0);
                        //newV.put(row, col, 0);
                    }
                }
                if (isCancelled())
                    break;
            }
            Log.d("MyTAG", "test3");

            Scalar h_value = Core.sumElems(h);
            Scalar a2_value = Core.sumElems(a2);
            Log.d("MyTAG", "h_value :"+h_value);
            Log.d("MyTAG", "a2_value :"+a2_value);

            if (h_value.val[0] > 300) {
                //count = count + 1;
                Log.d("MyTAG", "FIRE");

                //if(a2_value.val[0] < h_value.val[0]) {
                //Log.d("MyTAG", "FIRE GROWING");
                //count1=count1+1;
                //return 10;
                //}
                return 1;
            }
            else {
                Log.d("MyTAG", "FIRE NOT DETECTED");
                return 0;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            int fireCheck = integer;
            if (fireCheck==0)
                output.setText("FIRE Detected only by 1st Algo!");
            else
                output.setText("FIRE Detected by both Algo!");

            secondProg.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            secondProg.setVisibility(View.INVISIBLE);
        }
    }

    private static Mat vChannel_correction (Mat matArray, double mean) {
        Mat return_mat = matArray.clone();
        for(int col=0; col<matArray.cols(); col++){
            for(int row=0; row<matArray.rows(); row++) {
                double[] data = matArray.get(row, col);
                data[0] = 1.5*data[0] - 0.5*mean;
                return_mat.put(row, col, data);
            }
        }
        return return_mat;
    }
}
