package com.example.cubesolver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class Settings extends AppCompatActivity {

    private SeekBar seekBarPitch;
    private SeekBar seekBarRate;
    private TextView seekBarPitchValue;
    private TextView seekBarRateValue;
    SeekBar seekBarSpeed;
    TextView seekBarSpeedValue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        seekBarPitch = findViewById(R.id.seekBar_pitch);
        seekBarRate = findViewById(R.id.seekBar_rate);
        seekBarPitchValue = findViewById(R.id.seekBar_pitch_value);
        seekBarRateValue = findViewById(R.id.seekBar_rate_value);
        seekBarSpeed = findViewById(R.id.seekBar_speed);
        seekBarSpeedValue = findViewById(R.id.seekBar_speed_value);

        /* General */
        //Set up theme spinner
        Spinner themeSpinner = findViewById(R.id.theme_spinner);
        ArrayAdapter<CharSequence> themeAdapter = ArrayAdapter.createFromResource(this,
                R.array.theme_options, android.R.layout.simple_spinner_item);
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        themeSpinner.setAdapter(themeAdapter);
        int defaultThemePosition = themeAdapter.getPosition("Light");
        themeSpinner.setSelection(defaultThemePosition);

        // Set up app_language_spinner
        Spinner appLanguageSpinner = findViewById(R.id.app_language_spinner);
        ArrayAdapter<CharSequence> appLanguageAdapter = ArrayAdapter.createFromResource(this,
                R.array.app_language_options, android.R.layout.simple_spinner_item);
        appLanguageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        appLanguageSpinner.setAdapter(appLanguageAdapter);
        int defaultAppLanguagePosition = appLanguageAdapter.getPosition("English (UK)");
        appLanguageSpinner.setSelection(defaultAppLanguagePosition);

        /* Text to Speech */
        // Get and set current pitch and rate values
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        float currentPitch = sharedPreferences.getFloat("pitch", 0.85f);
        float currentRate = sharedPreferences.getFloat("rate", 1.45f);

        seekBarPitch.setProgress((int) (currentPitch * 50));
        seekBarPitchValue.setText(String.valueOf( (int) (currentPitch * 50)));
        seekBarRate.setProgress((int) (currentRate * 50));
        seekBarRateValue.setText(String.valueOf( (int) (currentRate * 50)));

        // Listen to pitch SeekBar changes
        seekBarPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float pitch = progress / 50.0f;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putFloat("pitch", pitch);
                editor.apply();
                seekBarPitchValue.setText(String.valueOf(progress));
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
                seekBarRateValue.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Set up speech_language_spinner
        Spinner speechLanguageSpinner = findViewById(R.id.speech_language_spinner);
        ArrayAdapter<CharSequence> speechLanguageAdapter = ArrayAdapter.createFromResource(this,
                R.array.speech_language_options, android.R.layout.simple_spinner_item);
        speechLanguageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speechLanguageSpinner.setAdapter(speechLanguageAdapter);
        int defaultSpeechLanguagePosition = speechLanguageAdapter.getPosition("English (UK)");
        speechLanguageSpinner.setSelection(defaultSpeechLanguagePosition);


        /* Auto play */
        SharedPreferences sharedSpeedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        int speed = sharedSpeedPreferences.getInt("speed", -1);
        if (speed == -1) {
            speed = 75;
        }
        seekBarSpeed.setMax(100);
        seekBarSpeed.setProgress(speed);
        seekBarSpeedValue.setText(String.valueOf(speed));

        seekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarSpeedValue.setText(String.valueOf(progress));

                // Speed to delay
                int delay = (int) (9000 - 8000 * (progress / 100.0));

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("delay", delay);
                editor.putInt("speed", progress); // Store the updated speed value
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

    }
}
