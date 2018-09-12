package com.example.varun.firedetectionju;

import android.util.Log;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnalyseFire {

    public int fireCheck(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
        Mat imgHSV = inputFrame.rgba();
        Imgproc.cvtColor(imgHSV, imgHSV, Imgproc.COLOR_RGBA2RGB);
        int x1 = imgHSV.rows();
        int y1 = imgHSV.cols();
        Imgproc.cvtColor(imgHSV, imgHSV, Imgproc.COLOR_RGB2HSV);
        List<Mat> lRgb = new ArrayList<Mat>(3);
        Core.split(imgHSV, lRgb);
        Mat hChannel = lRgb.get(0);
        Mat sChannel = lRgb.get(1);
        Mat vChannel = lRgb.get(2);
        Core.multiply(sChannel, new Scalar(2), sChannel);
        Mat newS = sChannel.clone();
        Scalar meanV = Core.mean(vChannel);
        Core.subtract(vChannel, meanV, vChannel);
        Core.multiply(vChannel, new Scalar(1.5), vChannel);
        Core.add(vChannel, meanV, vChannel);
        Mat newV = vChannel.clone();
        List<Mat> listMat = Arrays.asList(hChannel, sChannel, vChannel);
        Core.merge(listMat, imgHSV);
        Imgproc.cvtColor(imgHSV, imgHSV, Imgproc.COLOR_HSV2RGB);

        //Here complex conjugate transpose was used in MATLAB.
        Mat h_col_sum = sum(hChannel);
        h_col_sum = sum(h_col_sum.t());
        Mat s_col_sum = sum(newS);
        s_col_sum = sum(s_col_sum.t());
        Mat v_col_sum = sum(newV);
        v_col_sum = sum(v_col_sum.t());

        Scalar Y = new Scalar(h_col_sum.get(0,0)[0]/(x1*y1));
        Scalar C = new Scalar(s_col_sum.get(0,0)[0]/(x1*y1));
        Scalar B = new Scalar(v_col_sum.get(0,0)[0]/(x1*y1));

        //Double precision not done
        List<Mat> klm = new ArrayList<Mat>(3);
        Core.split(imgHSV, klm);
        Mat k = klm.get(0);
        Mat l = klm.get(1);
        Mat m = klm.get(2);
        Log.d("MyTAG", "test");

        //Here complex conjugate transpose was used in MATLAB.
        Mat L1 = sum(k);
        L1 = sum(L1.t());
        Mat a1 = sum(l);
        a1 = sum(a1.t());
        Mat b1 = sum(m);
        b1 = sum(b1.t());

        Scalar RED = new Scalar(L1.get(0,0)[0]/(x1*y1));
        Scalar GREEN = new Scalar(a1.get(0,0)[0]/(x1*y1));
        Scalar BLUE = new Scalar(b1.get(0,0)[0]/(x1*y1));

        Mat allBlack = Mat.zeros(imgHSV.rows(), imgHSV.cols(), CvType.CV_8UC1);
        List<Mat> temp_r = Arrays.asList(k, allBlack, allBlack);
        List<Mat> temp_g = Arrays.asList(allBlack, l, allBlack);
        List<Mat> temp_b = Arrays.asList(allBlack, allBlack, m);
        Mat red = new Mat();
        Mat green = new Mat();
        Mat blue = new Mat();
        Core.merge(temp_r, red);
        Core.merge(temp_g, green);
        Core.merge(temp_b, blue);

        Log.d("MyTAG", "test2");

        Mat d = Mat.zeros(x1, y1, CvType.CV_8UC1);
        a1 = Mat.zeros(x1, y1, CvType.CV_8UC1);
        for(int row=1; row<x1; row++){
            for(int col=1; col<y1; col++) {
                if(k.get(row, col)[0] >= l.get(row, col)[0])
                    a1.put(row, col, 1);
                else
                    a1.put(row, col, 0);
            }
        }
        Log.d("MyTAG", "test3");

        b1 = Mat.zeros(x1, y1, CvType.CV_8UC1);
        for(int row=1; row<x1; row++){
            for(int col=1; col<y1; col++) {
                if((k.get(row, col)[0] >= RED.val[0]) && (l.get(row, col)[0] >= GREEN.val[0]) && (m.get(row, col)[0] >= BLUE.val[0]))
                    b1.put(row, col, 1);
                else
                    b1.put(row, col, 0);
            }
        }
        Log.d("MyTAG", "test4");

        Mat g = Mat.zeros(x1, y1, CvType.CV_8UC1);
        for(int row=1; row<x1; row++){
            for(int col=1; col<y1; col++) {
                if((newS.get(row, col)[0] <= 0.38) && (newV.get(row, col)[0] >= 1))
                    g.put(row, col, 1);
                else
                    g.put(row, col, 0);
            }
        }
        Log.d("MyTAG", "test5");

        Mat a2 = Mat.zeros(x1, y1, CvType.CV_8UC1);
        Mat b2 = Mat.zeros(x1, y1, CvType.CV_8UC1);
        Mat h = Mat.zeros(x1, y1, CvType.CV_8UC1);
        for(int row=1; row<x1; row++){
            for(int col=1; col<y1; col++) {
                if((g.get(row, col)[0]!=0) && (a1.get(row, col)[0]!=0) && (b1.get(row, col)[0]==1)) {
                    newV.put(row, col, 1);
                    h.put(row, col, 1);
                    a2.put(row, col, row);
                    b2.put(row, col, col);
                }
                else {
                    newV.put(row, col, 0);
                    h.put(row, col, 0);
                }
            }
        }
        Log.d("MyTAG", "test6");

        Mat im_th = new Mat();
        Imgproc.threshold(newV, im_th, 220 , 255 , Imgproc.THRESH_BINARY_INV);
        Mat im_floodfill = im_th.clone();
        Point flood=new Point(0,0);
        Mat mask = Mat.zeros(new Size(imgHSV.width() + 2, imgHSV.height() + 2), CvType.CV_8UC1);
        Imgproc.floodFill(im_floodfill, mask, flood, Scalar.all(255));
        Mat im_floodfill_inv = new Mat();
        Core.bitwise_not(im_floodfill, im_floodfill_inv);
        Mat im_out = new Mat();
        List<Mat> imfill = Arrays.asList(im_th, im_floodfill_inv);
        Core.merge(imfill, im_out);

        Log.d("MyTAG", "test7");
        return 0;
        //return imgHSV;
    }

    private Mat sum (Mat matArray) {
        double sum=0.0;
        Mat ret_mat = new Mat(1, matArray.cols(), CvType.CV_8UC1);
        for(int col=0; col<matArray.cols(); col++){
            for(int row=0; row<matArray.rows(); row++) {
                sum += matArray.get(row, col)[0];
            }
            ret_mat.put(0, col, sum);
            sum=0;
        }
        return ret_mat;
    }
}
