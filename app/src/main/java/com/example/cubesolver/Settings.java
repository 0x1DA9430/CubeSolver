package com.example.cubesolver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SeekBar;

public class Settings extends AppCompatActivity {

    private SeekBar seekBarPitch;
    private SeekBar seekBarRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        seekBarPitch = findViewById(R.id.seekBar_pitch);
        seekBarRate = findViewById(R.id.seekBar_rate);

        // Get and set current pitch and rate values
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        float currentPitch = sharedPreferences.getFloat("pitch", 0.85f);
        float currentRate = sharedPreferences.getFloat("rate", 1.45f);


        seekBarPitch.setProgress((int) (currentPitch * 50));
        seekBarRate.setProgress((int) (currentRate * 50));

        // Listen to pitch SeekBar changes
        seekBarPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float pitch = progress / 50.0f;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putFloat("pitch", pitch);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Listen to rate SeekBar changes
        seekBarRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float rate = progress / 50.0f;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putFloat("rate", rate);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
}
