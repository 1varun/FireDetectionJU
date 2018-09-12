package com.example.varun.firedetectionju;

import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.lang.Math;
import java.util.Scanner;

public class AnalyseFire {

    String pathname = "/storage/emulated/0/Android/data/com.example.varun.firedetectionju/files/FIRE_SAMPLE.jpg";

    public int fireCheck(){
        BufferedImage img = null;
        File f;

        try {
            f = new File(pathname);
            img = ImageIO.read(f);
        }
        catch(IOException e) {
            System.out.println(e);
            return 0;
        }
        int width = img.getWidth();
        int height = img.getHeight();
        double[][] rValue = new double[width][height];
        double[][] gValue = new double[width][height];
        double[][] bValue = new double[width][height];
        double[][] yValue = new double[width][height];
        double[][] cbValue = new double[width][height];
        double[][] crValue = new double[width][height];
        double rMean,gMean,bMean;
        double rSum = 0.0,gSum = 0.0,bSum = 0.0;

        int pixel,R,G,B;
        double Y,Cb,Cr;
        double yMean,cbMean,crMean;
        double ySum = 0.0,cbSum = 0.0,crSum = 0.0;

        for(int y = 0;y<height;y++) {
            for(int x = 0;x<width;x++) {
                pixel = img.getRGB(x, y);
                R = (pixel >> 16) & 0xff;
                G = (pixel >> 8) & 0xff;
                B = pixel & 0xff;
                rSum += (double)R;
                gSum += (double)G;
                bSum += (double)B;
                rValue[x][y] = (double)R;
                gValue[x][y] = (double)G;
                bValue[x][y] = (double)B;
                Y = 16 + (0.2568*R) + (0.5041*G) + (0.0979*B);
                Cb = 128 - (0.1482*R) - (0.2910*G) + (0.4392*B);
                Cr = 128 + (0.4392*R) - (0.3678*G) - (0.0714*B);
                ySum += Y;
                cbSum += Cb;
                crSum += Cr;
                yValue[x][y] = Y;
                cbValue[x][y] = Cb;
                crValue[x][y] = Cr;
            }
        }

        int totalPixels = width*height;

        rMean = (double)(rSum)/totalPixels;
        gMean = (double)(gSum)/totalPixels;
        bMean = (double)(bSum)/totalPixels;

        yMean = (double)(ySum)/totalPixels;
        cbMean = (double)(cbSum)/totalPixels;
        crMean = (double)(crSum)/totalPixels;

        boolean rgbFlag = false;
        boolean yCbCrFlag = false;

        for(int y = 0;y<height;y++) {
            for(int x = 0;x<width;x++) {
                if((rValue[x][y] > 225.0 && gValue[x][y] > 100.0 && bValue[x][y] < 140.0) &&
                        (rValue[x][y] > gValue[x][y] && gValue[x][y] > bValue[x][y])
                        && (rValue[x][y] > rMean && gValue[x][y] > gMean && bValue[x][y] < bMean)) {
                    rgbFlag = true;
                }
                if((yValue[x][y] >= cbValue[x][y]) && (crValue[x][y] >= cbValue[x][y])
                        && (yValue[x][y] >= yMean && cbValue[x][y] <= cbMean && crValue[x][y] >= crMean)
                        && (crValue[x][y] - cbValue[x][y] >= 30.0)
                        && (cbValue[x][y] <= 120.0 && crValue[x][y] >= 150.0)) {
                    yCbCrFlag = true;
                }
            }
        }
        if(rgbFlag && yCbCrFlag) {
            return 1;
        }
        else {
            return 0;
        }
    }
}
