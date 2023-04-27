package com.example.cubesolver;

import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_8U;


import android.graphics.ImageFormat;
import android.graphics.PixelFormat;

import androidx.annotation.Nullable;
import androidx.camera.core.ImageProxy;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
* 转换魔方注释，替换颜色标签。Convert cube annotation, replace color label.
* 计算图像中方框的平均颜色。 Calculate the average color of the box in the image.
* 计算两个颜色矩阵的移动平均值。 Calculate the moving average value of two color matrices.
* 使用 OpenCV 将 ImageProxy 对象转换为 Mat 对象。 Use OpenCV to convert ImageProxy object to Mat object.
* 生成一个可视化魔方解决方案的 URI。 Generate a URI for a visualized cube solution.
*/

public class ImageProcess {

    // Color definition
    static final double[][] colorData = {
            {255, 215, 0, 0},    // Yellow
            {254, 80, 0, 0},     // Orange
            {0, 154, 68, 0},     // Green
            {255, 255, 255, 0},  // White
            {186, 23, 47, 0},    // Red
            {0, 61, 165, 0},     // Blue
    };
    static final String[] colorLabel = {"Y", "O", "G", "W", "R", "B"};
    static final String[] colorName = {"Yellow", "Orange", "Green", "White", "Red", "Blue"};
    static final List<Integer> colorResponse = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5));
    // top -> left -> down -> right
    static final String[] arrSideColors = {
            "BRGO",  // Yellow
            "YGWB",  // Orange
            "YRWO",  // Green
            "GRBO",  // White
            "YBWG",  // Red
            "YOWR",  // Blue
    };

    // Error Message
    static final String[] verifyMsg = {
            "There is not exactly one facelet of each color.",               // -1
            "Not all 12 edges exist exactly once.",                          // -2
            "Flip error: One edge has to be flipped.",                       // -3
            "Not all 8 corners exist exactly once.",                         // -4
            "Twist error: One corner has to be twisted.",                    // -5
            "Parity error: Two corners or two edges have to be exchanged.",  // -6
    };

    // 转换魔方注释，替换扫描到的魔方的颜色标签。 Convert cube annotation, replace color label of scanned cube.
    static String convertCubeAnnotation(String scannedCube) {
        return scannedCube
                .replace("Y", "U")
                .replace("R", "L")
                .replace("G", "F")
                .replace("O", "R")
                .replace("W", "D");
    }

    // Calculate the average color of the box in the image.
    static Mat calcBoxAvgColor(Mat mat, int boxX, int boxY, int boxLen) {
        // extract box as sub matrix
        Mat boxMat = mat.submat(new Rect(boxX, boxY, boxLen, boxLen));

        // create mask
        Mat mask = new Mat(boxMat.cols(), boxMat.rows(), CV_8U);
        mask.setTo(new Scalar(0.0));
        int innerBoxLen = (int) (boxLen * 0.6);
        int innerX = (int) ((boxLen - innerBoxLen) / 2);
        int innerY = (int) ((boxLen - innerBoxLen) / 2);
        Imgproc.rectangle(mask, new Rect(innerX, innerY, innerBoxLen, innerBoxLen), new Scalar(255, 255, 255), -1);

        Scalar mean = Core.mean(boxMat, mask);
        Mat ret = new Mat(1, mean.val.length, CV_32F);
        for (int i = 0; i < mean.val.length; i++) {
            ret.put(0, i, mean.val[i]);
        }
        return ret;
    }

    // Calculate the moving average value of two color matrices.
    static Mat calcMovingAvgColor(@Nullable Mat matPrev, Mat matCurrent, float alpha) {
        if (matPrev == null) {
            return matCurrent;
        }
        assert matPrev.rows() == matCurrent.rows();
        assert matPrev.cols() == matCurrent.cols();
        Mat ret = new Mat(matPrev.rows(), matPrev.cols(), CV_32F);
        for (int i = 0; i < matPrev.cols(); i++) {
            ret.put(0, i, matPrev.get(0, i)[0] * alpha + matCurrent.get(0, i)[0] * (1 - alpha));
        }
        return ret;
    }

    // 使用 OpenCV 将 ImageProxy 对象转换为 Mat 对象。 Use OpenCV to convert ImageProxy object to Mat object.
    static public Mat imageToMat(ImageProxy image) {
        // Create cv::mat(RGB888) from image(NV21)
        if (image.getFormat() == ImageFormat.YUV_420_888) {
            ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
            ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
            ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();
            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();
            byte[] nv21 = new byte[ySize + uSize + vSize];
            yBuffer.get(nv21, 0, ySize);
            vBuffer.get(nv21, ySize, vSize);
            uBuffer.get(nv21, ySize + vSize, uSize);
            Mat yuv = new Mat(image.getHeight() + image.getHeight() / 2, image.getWidth(), CvType.CV_8UC1);
            yuv.put(0, 0, nv21);
            Mat mat = new Mat();
            Imgproc.cvtColor(yuv, mat, Imgproc.COLOR_YUV2RGB_NV21, 3);
            return mat;
        } else if (image.getFormat() == PixelFormat.RGBA_8888) {
            ByteBuffer argbBuffer = image.getPlanes()[0].getBuffer(); // ARGBARGB...
            int argbSize = argbBuffer.remaining();
            byte[] argb_buf = new byte[argbSize];
            argbBuffer.get(argb_buf, 0, argbSize);
            Mat bgra = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4);
            bgra.put(0, 0, argb_buf);
            Mat rgb = new Mat();
            Imgproc.cvtColor(bgra, rgb, Imgproc.COLOR_BGRA2BGR);
            return rgb;
        }
        assert false;
        return null;
    }
}

