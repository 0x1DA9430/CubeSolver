package com.example.cubesolver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Invalid extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invalid);

        Button retry = findViewById(R.id.btn_retry);
        // listen for clicks
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Go back to Scan activity
                finish();
            }
        });
    }
}