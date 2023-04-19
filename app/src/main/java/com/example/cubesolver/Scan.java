package com.example.cubesolver;

import static androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST;
import static org.opencv.core.CvType.CV_32F;
import static org.opencv.ml.Ml.ROW_SAMPLE;
import static java.lang.Math.min;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.common.util.concurrent.ListenableFuture;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.KNearest;
import org.opencv.utils.Converters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Scan extends AppCompatActivity {
    private static final String TAG = "Scan";
    final private int REQUEST_CODE_FOR_PERMISSIONS = 10;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};

    protected LinearProgressIndicator scanIndicator;
    protected ImageView imageView;
    private CubeView cubeView;
    private Button resetButton;
    private Button scanButton;

    private Camera camera;
    private ImageAnalysis imageAnalysis = null;
    final private ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();
    /*** For Rubik's Cube Solver ***/
    final private Mat trainData = new Mat(6, 4, CV_32F);
    final private KNearest knn = KNearest.create();

    // For Color Detection
    /***
     * Scan Order : Upper(0, Yellow) -> Right(1, Orange) -> Front(2, Green) -> Down(3, White) -> Left(4, Red) -> Back(5, Blue)
     */
    final protected int[][] detectedColor = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
    final protected Mat[][] aveColor = {{null, null, null}, {null, null, null}, {null, null, null}};
    private static final float alpha = 0.75f;
    protected String scannedCube = "";
    protected int currentFaceIdx = 0;

    static {
        System.loadLibrary("opencv_java4");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        scanIndicator = findViewById(R.id.scan_indicator);
        imageView = findViewById(R.id.image_view);
        cubeView = findViewById(R.id.cube_view);
        resetButton = findViewById(R.id.reset_button);
        scanButton = findViewById(R.id.scan_button);

        // Indicator
        scanIndicator.show();
        updateIndicator();

        // Scan button
        scanButton.setOnClickListener(view -> {
            // Read detectedColor
            synchronized (detectedColor) {
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (i == 1 && j == 1) {
                            scannedCube += ImageProcess.colorLabel[currentFaceIdx];
                        } else {
                            scannedCube += ImageProcess.colorLabel[detectedColor[j][i]];
                        }
                    }
                }
                if (detectedColor[1][1] != currentFaceIdx && currentFaceIdx < 5) {
                    new MaterialAlertDialogBuilder(Scan.this)
                            .setTitle(R.string.right_face_dialog_title)
                            .setMessage("Center color should be " + ImageProcess.colorName[currentFaceIdx] + ".")
                            .setNegativeButton(R.string.right_face_dialog_negative, (dialogInterface, i) -> scanRollback())
                            .setPositiveButton(R.string.right_face_dialog_positive, null)
                            .setCancelable(false)
                            .show();
                }
            }
            if (currentFaceIdx < 5) {
                currentFaceIdx++;
                display();
            } else {
                // solve
                currentFaceIdx++;
                updateIndicator();
                new SolveTask().execute(scannedCube);
            }
        });

        // Reset button
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanReset();
            }
        });

        // Permission check and start camera
        if (checkPermission()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_FOR_PERMISSIONS);
        }

        // Prepare K-nearest neighbor
        for (int i = 0; i < 6; i++) {
            trainData.put(i, 0, ImageProcess.colorData[i]); // Training data
        }
        knn.train(trainData, ROW_SAMPLE, Converters.vector_int_to_Mat(ImageProcess.colorResponse));

        // fully initialize solver
        Solver.init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        display();
    }

    private void scanReset() {
        currentFaceIdx = 0;
        scannedCube = "";
        display();
    }

    private void scanRollback() {
        assert currentFaceIdx > 0;
        assert scannedCube.length() == currentFaceIdx * 9;
        currentFaceIdx--;
        scannedCube = scannedCube.substring(0, scannedCube.length() - 9);
        display();
    }

    private void display() {
        resetButton.setEnabled(currentFaceIdx > 0);
        cubeView.setSideColors(ImageProcess.arrSideColors[currentFaceIdx]);
        cubeView.setFrontColors(detectedColor);
        cubeView.setCenterColor(ImageProcess.colorLabel[currentFaceIdx]);
        updateIndicator();
    }

    private class SolveTask extends android.os.AsyncTask<String, Void, String> {

        private int lastErrorCode;

        @Override
        protected void onPreExecute() {
            disableButtons();
            scanIndicator.setIndeterminate(true);
        }

        @Override
        protected String doInBackground(String... strings) {
            String scannedCube = strings[0];
            String scrambledCube = ImageProcess.convertCubeAnnotation(scannedCube);
            lastErrorCode = Util.verify(scrambledCube);
            if (lastErrorCode == 0) {
                return new Solver().solution(scrambledCube, 21, 100000000, 10000, Solver.APPEND_LENGTH);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String moves) {
            if (lastErrorCode == 0) {
                // Jump to `solution` activity
                Intent intent = new Intent(Scan.this, Solution.class);
                intent.putExtra("solution", moves);
                startActivity(intent);
                scanReset();
            } else {
                // Jump to 'invalid' activity
                Intent intent = new Intent(Scan.this, Invalid.class);
                intent.putExtra("errorCode", lastErrorCode);
                startActivity(intent);
                scanReset();
            }
            enableButtons();
        }
    }

    private void updateIndicator() {
        scanIndicator.setIndeterminate(false);
        scanIndicator.setProgress((int) (100 / 6) * currentFaceIdx, true);
    }

    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        Context context = this;
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    imageAnalysis = new ImageAnalysis.Builder()
                            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                            .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                            .build();
                    imageAnalysis.setAnalyzer(cameraExecutor, new MyImageAnalyzer());
                    CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

                    cameraProvider.unbindAll();
                    camera = cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, imageAnalysis);
                } catch (Exception e) {
                    Log.e(TAG, "[startCamera] Use case binding failed", e);
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void enableButtons() {
        resetButton.setEnabled(true);
        scanButton.setEnabled(true);
    }

    private void disableButtons() {
        resetButton.setEnabled(false);
        scanButton.setEnabled(false);
    }

    private class MyImageAnalyzer implements ImageAnalysis.Analyzer {

        @Override
        public void analyze(@NonNull ImageProxy image) {
            // Create cv::mat(RGB888) from image(NV21)
            Mat mat = ImageProcess.imageToMat(image);

            // Fix image rotation (it looks image in PreviewView is automatically fixed by CameraX???)
            mat = fixMatRotation(mat);
            int h = mat.rows(), w = mat.cols();

            // Do some image processing
            Mat matOutput = new Mat(mat.rows(), mat.cols(), mat.type());
            mat.copyTo(matOutput);

            // calculate box point
            double cubeLen = min(h, w) * 0.8;
            int boxLen = (int) (cubeLen / 3);
            int startX = (int) (w - cubeLen) / 2;
            int startY = (int) (h - cubeLen) / 2;

            // detect color of each box using KNN
            synchronized (detectedColor) {
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        Mat color = ImageProcess.calcBoxAvgColor(mat, startX + boxLen * i, startY + boxLen * j, boxLen);
                        color = ImageProcess.calcMovingAvgColor(aveColor[i][j], color, alpha);
                        aveColor[i][j] = color;
                        Mat res = new Mat();
                        knn.findNearest(color, 1, res);
                        detectedColor[i][j] = (int) res.get(0, 0)[0];
                    }
                }
            }

            // Update cubeView
            cubeView.setFrontColors(detectedColor);

            // Draw frame and detected color
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    int centerX = startX + boxLen * i + boxLen / 2;
                    int centerY = startY + boxLen * j + boxLen / 2;
                    int halfWidth = (boxLen - 60) / 2;
                    int halfHeight = (boxLen - 60) / 2;
                    Imgproc.rectangle(matOutput, new Rect(centerX - halfWidth, centerY - halfHeight, boxLen - 55, boxLen - 55), new Scalar(ImageProcess.colorData[detectedColor[i][j]]), 6);
                    Imgproc.rectangle(matOutput, new Rect(startX + boxLen * i, startY + boxLen * j, boxLen, boxLen), new Scalar(90, 176, 243), 5/2);
                }
            }


            if (h > w) {
                int bitmapStartX = 0;
                int bitmapStartY = startY - startX;
                matOutput = matOutput.submat(new Rect(bitmapStartX, bitmapStartY, w, w));
            } else {
                int bitmapStartY = 0;
                int bitmapStartX = startX - startY;
                matOutput = matOutput.submat(new Rect(bitmapStartX, bitmapStartY, h, h));
            }

            // Convert cv::mat to bitmap for drawing
            Bitmap bitmap = Bitmap.createBitmap(matOutput.cols(), matOutput.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(matOutput, bitmap);

            // Display the result onto ImageView
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(bitmap);
                }
            });

            // Close the image otherwise, this function is not called next time
            image.close();
        }

        private Mat fixMatRotation(Mat matOrg) {
            Mat mat;
            switch (imageView.getDisplay().getRotation()) {
                default:
                case Surface.ROTATION_0:
                    mat = new Mat(matOrg.cols(), matOrg.rows(), matOrg.type());
                    Core.transpose(matOrg, mat);
                    Core.flip(mat, mat, 1);
                    break;
                case Surface.ROTATION_90:
                    mat = matOrg;
                    break;
                case Surface.ROTATION_270:
                    mat = matOrg;
                    Core.flip(mat, mat, -1);
                    break;
            }
            return mat;
        }
    }

    private boolean checkPermission() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_FOR_PERMISSIONS) {
            if (checkPermission()) {
                startCamera();
            } else {
                this.finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
