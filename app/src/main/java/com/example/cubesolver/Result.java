package com.example.cubesolver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class Result extends AppCompatActivity implements Comparable<Result>{

    private Button homeButton;
    private TextView timeTextView;
    String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Time Used
        timeTextView = findViewById(R.id.tv_result_time);
        // Get the elapsed time
        long elapsedMillis = getIntent().getLongExtra("elapsedMillis", 0);
        // Format: mm:ss:ms
        int minutes = (int) (elapsedMillis / 60000);
        int seconds = (int) (elapsedMillis % 60000 / 1000);
        int millis = (int) (elapsedMillis % 1000);
        String result = String.format("%02d:%02d:%03d", minutes, seconds, millis);
        timeTextView.setText(result);
        // Save the result to the history
        saveResultToHistory(result);


        // Home button
        homeButton = findViewById(R.id.btn_home);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Back to the MainActivity (home screen)
                Intent intent = new Intent(Result.this, MainActivity.class);
                startActivity(intent);
            }
        });


        // Confetti effect
        final nl.dionsegijn.konfetti.KonfettiView viewKonfetti = findViewById(R.id.view_konfetti);
        viewKonfetti.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Remove the listener to avoid multiple calls
                viewKonfetti.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                // Create a new particle system with the desired parameters
                viewKonfetti.build()
                        .addColors(Color.parseColor("#FF8F1F"), Color.parseColor("#A1D600"),
                                   Color.parseColor("#73BBF3"), Color.parseColor("#FFF314"))
                        .setDirection(250.0, 290.0)
                        .setSpeed(1f, 6f)
                        .setFadeOutEnabled(true)
                        .setTimeToLive(2100L)
                        .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                        .addSizes(new Size(12, 5f))
                        // Set the position at the top of screen
                        .setPosition(0f, (float) viewKonfetti.getWidth(), -40f, -40f)
                        .streamFor(200, 3900L);
            }
        });
    }


    // Save the result to the history
    private void saveResultToHistory(String result) {
        SharedPreferences sharedPreferences = getSharedPreferences("results_history", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(currentDateTime, result);
        editor.apply();
    }

    // Sort the results in descending order
    @Override
    public int compareTo(Result anotherResult) {
        return -this.currentDateTime.compareTo(anotherResult.currentDateTime);
    }

    @Override
    public void onBackPressed() {
        // Do nothing to disable the back button
    }

}