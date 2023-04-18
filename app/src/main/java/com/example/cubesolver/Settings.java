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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        seekBarPitch = findViewById(R.id.seekBar_pitch);
        seekBarRate = findViewById(R.id.seekBar_rate);
        seekBarPitchValue = findViewById(R.id.seekBar_pitch_value);
        seekBarRateValue = findViewById(R.id.seekBar_rate_value);


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

        // Set up app_language_spinner
        Spinner appLanguageSpinner = findViewById(R.id.app_language_spinner);
        ArrayAdapter<CharSequence> appLanguageAdapter = ArrayAdapter.createFromResource(this,
                R.array.app_language_options, android.R.layout.simple_spinner_item);
        appLanguageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        appLanguageSpinner.setAdapter(appLanguageAdapter);
        int defaultAppLanguagePosition = appLanguageAdapter.getPosition("English (UK)");
        appLanguageSpinner.setSelection(defaultAppLanguagePosition);

    }
}
