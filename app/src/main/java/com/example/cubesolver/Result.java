package com.example.cubesolver;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;
import android.view.ViewTreeObserver;


public class Result extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Find the KonfettiView for the particle effect
        final nl.dionsegijn.konfetti.KonfettiView viewKonfetti = findViewById(R.id.view_konfetti);

        // Set an OnGlobalLayoutListener to get the width of the view
        viewKonfetti.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Remove the listener to avoid multiple calls
                viewKonfetti.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Create a new particle system with the desired parameters
                viewKonfetti.build()
                        .addColors(Color.parseColor("#ff9933"), Color.parseColor("#99cc00"),
                                   Color.parseColor("#3399ff"), Color.parseColor("#ff6699"))
                        .setDirection(250.0, 290.0)
                        .setSpeed(1f, 5f)
                        .setFadeOutEnabled(true)
                        .setTimeToLive(2100L)
                        .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                        .addSizes(new Size(12, 5f))
                        // Set the position of the emitter to be a line across the top of the screen
                        .setPosition(0f, (float) viewKonfetti.getWidth(), -40f, -40f)
                        .streamFor(250, 4000L);
            }
        });
    }
}