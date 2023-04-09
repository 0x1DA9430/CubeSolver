package com.example.cubesolver;

import static androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST;
import static org.opencv.core.CvType.CV_32F;
import static org.opencv.ml.Ml.ROW_SAMPLE;
import static java.lang.Math.min;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
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

public class ScanCube extends AppCompatActivity {
    /*** Fixed values ***/
    private static final String TAG = "RubikCubeSolver";
    final private int REQUEST_CODE_FOR_PERMISSIONS = 10;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};

    /*** Views ***/
    protected LinearProgressIndicator scanIndicator;
    protected ImageView imageView;
    private CubeView cubeView;
    private Button resetButton;
    private Button scanButton;
    /*** For CameraX ***/
    private Camera camera;
    private ImageAnalysis imageAnalysis = null;
    final private ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();
    /*** For Rubik's Cube Solver ***/
    final private Mat trainData = new Mat(6, 4, CV_32F);
    final private KNearest knn = KNearest.create();
    /* For Color Detection ***/
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
        setContentView(R.layout.activity_scan_cube);

        scanIndicator = findViewById(R.id.scan_indicator);
        imageView = findViewById(R.id.image_view);
        cubeView = findViewById(R.id.cube_view);
        resetButton = findViewById(R.id.reset_button);
        scanButton = findViewById(R.id.scan_button);




        scanIndicator.show();
        updateIndicator();

        scanButton.setOnClickListener(view -> {
            // read detectedColor
            synchronized (detectedColor) {
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (i == 1 && j == 1) {
                            scannedCube += ImageUtil.colorLabel[currentFaceIdx];
                        } else {
                            scannedCube += ImageUtil.colorLabel[detectedColor[j][i]];  // hacky idx
                        }
                    }
                }
                if (detectedColor[1][1] != currentFaceIdx && currentFaceIdx < 5) {
                    new MaterialAlertDialogBuilder(ScanCube.this)
                            .setTitle(R.string.right_face_dialog_title)
                            .setMessage("Center color should be " + ImageUtil.colorName[currentFaceIdx] + ", instead of " + ImageUtil.colorName[detectedColor[1][1]] + ".")
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
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentFaceIdx > 0) {
                    scanRollback();
                } else {
                    new MaterialAlertDialogBuilder(ScanCube.this)
                            .setTitle("Confirm")
                            .setMessage("Is it OK to finish this app?")
                            .setPositiveButton("Finish", (dialog, i) -> finish())
                            .setNegativeButton("Stay", (dialog, i) -> dialog.dismiss())
                            .setCancelable(false)
                            .show();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
        resetButton.setOnClickListener(view -> scanReset());

        if (checkPermissions()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_FOR_PERMISSIONS);
        }

        // prepare K-nearest neighbor
        for (int i = 0; i < 6; i++) {
            trainData.put(i, 0, ImageUtil.colorData[i]);
        }
        knn.train(trainData, ROW_SAMPLE, Converters.vector_int_to_Mat(ImageUtil.colorResponse));

        // fully initialize solver
        Search.init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        display();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
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
        cubeView.setSideColors(ImageUtil.arrSideColors[currentFaceIdx]);
        cubeView.setFrontColors(detectedColor);
        cubeView.setCenterColor(ImageUtil.colorLabel[currentFaceIdx]);
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
            Log.i(TAG, "Scanned : " + scannedCube);
            String scrambledCube = ImageUtil.convertCubeAnnotation(scannedCube);
            Log.i(TAG, "Scrambled : " + scrambledCube);
            lastErrorCode = Tools.verify(scrambledCube);
            if (lastErrorCode == 0) {
                return new Search().solution(scrambledCube, 21, 100000000, 10000, Search.APPEND_LENGTH);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String moves) {
            if (lastErrorCode == 0) {
                new MaterialAlertDialogBuilder(ScanCube.this)
                        .setTitle(R.string.solved_dialog_title)
                        .setMessage(getString(R.string.solved_dialog_msg_prefix) + "\n" + moves)
                        .setPositiveButton(R.string.solved_dialog_positive, null)
                        .setNeutralButton(R.string.solved_dialog_animation, (dialog, i) -> {
                            String solution = moves.substring(0, moves.indexOf('(') - 1);
                            Uri webpage = ImageUtil.generateAnimationLink(solution);
                            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                            Log.i(TAG, "Start Animation");
                            startActivity(intent);
                        })
                        .setCancelable(false)
                        .show();
                scanReset();
            } else {
                int msgIdx = (lastErrorCode * -1) - 1;
                new MaterialAlertDialogBuilder(ScanCube.this)
                        .setTitle(R.string.invalid_dialog_title)
                        .setMessage("errorCode : " + lastErrorCode + "\n" + ImageUtil.verifyMsg[msgIdx])
                        .setPositiveButton(R.string.invalid_dialog_positive, (dialog, i) -> scanReset())
                        .setNeutralButton(R.string.invalid_dialog_neutral, null)
                        .setCancelable(false)
                        .show();
                scanRollback();
                Log.e(TAG, "[solver] Invalid cube (errorCode : " + lastErrorCode + ")");
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
                            // .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
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
            /* Create cv::mat(RGB888) from image(NV21) */
            Mat mat = ImageUtil.getMatFromImage(image);

            /* Fix image rotation (it looks image in PreviewView is automatically fixed by CameraX???) */
            mat = fixMatRotation(mat);
            int h = mat.rows(), w = mat.cols();

            /* Do some image processing */
            Mat matOutput = new Mat(mat.rows(), mat.cols(), mat.type());
            mat.copyTo(matOutput);

            // calculate box point
            double cubeLen = min(h, w) * 0.8;
            int boxLen = (int) (cubeLen / 3);
            int startX = (int) (w - cubeLen) / 2;
            int startY = (int) (h - cubeLen) / 2;

            // detect color of each box
            synchronized (detectedColor) {
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        Mat color = ImageUtil.calcBoxColorAve(mat, startX + boxLen * i, startY + boxLen * j, boxLen);
                        color = ImageUtil.calcMovingAveColor(aveColor[i][j], color, alpha);
                        aveColor[i][j] = color;
                        Mat res = new Mat();
                        knn.findNearest(color, 1, res);
                        detectedColor[i][j] = (int) res.get(0, 0)[0];
                    }
                }
            }

            // update cubeView
            cubeView.setFrontColors(detectedColor);

            // draw frame and detected color
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    Imgproc.putText(matOutput, ImageUtil.colorLabel[detectedColor[i][j]], new Point(startX + boxLen * i, startY + boxLen * (j + 1)), 2, 3, new Scalar(ImageUtil.colorData[detectedColor[i][j]]));
                    Imgproc.rectangle(matOutput, new Rect(startX + boxLen * i, startY + boxLen * j, boxLen, boxLen), new Scalar(255, 0, 0), 2);
                    Log.v(TAG, "[analyze] (" + i + ", " + j + ") = " + detectedColor[i][j]);
                }
            }

            // make output preview square
            Log.v(TAG, "height : " + h + ", width : " + w);
            if (h > w) {
                int bitmapStartX = 0;
                int bitmapStartY = startY - startX;
                matOutput = matOutput.submat(new Rect(bitmapStartX, bitmapStartY, w, w));
            } else {
                int bitmapStartY = 0;
                int bitmapStartX = startX - startY;
                matOutput = matOutput.submat(new Rect(bitmapStartX, bitmapStartY, h, h));
            }

            /* Convert cv::mat to bitmap for drawing */
            Bitmap bitmap = Bitmap.createBitmap(matOutput.cols(), matOutput.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(matOutput, bitmap);

            /* Display the result onto ImageView */
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(bitmap);
                }
            });

            /* Close the image otherwise, this function is not called next time */
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

    private boolean checkPermissions() {
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
            if (checkPermissions()) {
                startCamera();
            } else {
                Log.i(TAG, "[onRequestPermissionsResult] Failed to get permissions");
                this.finish();
            }
        }
    }
}
