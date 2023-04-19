package com.example.cubesolver;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.os.Handler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Solution extends AppCompatActivity {

    private TextView solution;
    private Intent receivedIntent;
    private Button finishButton;
    private TextView timerTextView;
    private Handler handler;
    private long startTime;
    private Runnable timer = new Runnable() {
        @Override
        public void run() {
            long elapsedMillis = SystemClock.elapsedRealtime() - startTime;
            int minutes = (int) (elapsedMillis / 60000);
            int seconds = (int) (elapsedMillis % 60000 / 1000);
            int millis = (int) (elapsedMillis % 1000);
            timerTextView.setText(String.format("%02d:%02d:%03d", minutes, seconds, millis));
            // Update every 1 millisecond
            handler.postDelayed(this, 1);
        }
    };
    private TextToSpeech textToSpeech;
    private boolean isAutoPlaying = false;
    Button btnAutoPlay;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solution);

        timerTextView = findViewById(R.id.tv_timer);
        finishButton = findViewById(R.id.btn_finish);
        btnAutoPlay = findViewById(R.id.btn_auto_play);

        // Start the timer
        handler = new Handler();
        startTime = SystemClock.elapsedRealtime();
        handler.post(timer);

        // Initialize the TextToSpeech object
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                    // Pitch and rate
                    SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
                    float pitch = sharedPreferences.getFloat("pitch", 0.85f);
                    float rate = sharedPreferences.getFloat("rate", 1.45f);
                    textToSpeech.setPitch(pitch);
                    textToSpeech.setSpeechRate(rate);
                }
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Stop the timer
                handler.removeCallbacks(timer);
                long elapsedMillis = SystemClock.elapsedRealtime() - startTime;
                // Go to the Result activity
                Intent intent = new Intent(Solution.this, Result.class);
                intent.putExtra("elapsedMillis", elapsedMillis);
                startActivity(intent);
            }
        });


        // Get the solution from the previous activity
        receivedIntent = getIntent();
        String moves = receivedIntent.getStringExtra("solution");

        // For testing
//        String moves = "R L U D F B  R' L' U'  D' F'  B' R2 L2  U2  D2 F2 B2(14f)";
//        String moves = "R L U D F B (14f)";


        moves = moves.substring(0, moves.indexOf('(') - 1); // Remove the unnecessary part
        String[] movesArray = moves.split("\\s+");
        moves = String.join("  ", movesArray); // Remove the unnecessary spaces


        // Display the solution
        solution = findViewById(R.id.tv_solution);
        solution.setText(moves);

        // Next step button
        String finalMoves = moves;
        findViewById(R.id.btn_next_step).setOnClickListener(new View.OnClickListener() {
            int steps = movesArray.length;
            int currentStep = 0; // Track of the current step
            int searchStartPosition = 0; // Store the search start position
            TextView solutionStep = findViewById(R.id.tv_solution_step);
            @Override
            public void onClick(View view) {

                if (steps == 0) {
                    // Finished. Ban the user from clicking the button
                    view.setEnabled(false);
                    // If auto playing, stop auto play
                    if (isAutoPlaying) {
                        isAutoPlaying = false;
                    }
                } else {
                    // Get the next step
                    int nextStepIndex = movesArray.length - steps;
                    String nextStep = movesArray[nextStepIndex];

                    // Highlight the next step in TextView `solution` of String `moves`
                    SpannableString spannableString = new SpannableString(finalMoves);
                    int start = finalMoves.indexOf(nextStep, searchStartPosition);
                    int end = start + nextStep.length();
                    spannableString.setSpan(new ForegroundColorSpan(Color.rgb(115, 187, 243)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    solution.setText(spannableString);
                    // Update the searchStartPosition for the next search
                    searchStartPosition = end;

                    // Translate the next step
                    switch (nextStep) {
                        case "R":
                            nextStep = "Turn the Right face clockwise.";
                            highlight(nextStep);
                            break;
                        case "R'":
                            nextStep = "Turn the Right face counter-clockwise.";
                            highlight(nextStep);
                            break;
                        case "R2":
                            nextStep = "Turn the Right face twice.";
                            highlight(nextStep);
                            break;
                        case "L":
                            nextStep = "Turn the Left face clockwise.";
                            highlight(nextStep);
                            break;
                        case "L'":
                            nextStep = "Turn the Left face counter-clockwise.";
                            highlight(nextStep);
                            break;
                        case "L2":
                            nextStep = "Turn the Left face twice.";
                            highlight(nextStep);
                            break;
                        case "U":
                            nextStep = "Turn the Upper face clockwise.";
                            highlight(nextStep);
                            break;
                        case "U'":
                            nextStep = "Turn the Upper face counter-clockwise.";
                            highlight(nextStep);
                            break;
                        case "U2":
                            nextStep = "Turn the Upper face twice.";
                            highlight(nextStep);
                            break;
                        case "D":
                            nextStep = "Turn the Down face clockwise.";
                            highlight(nextStep);
                            break;
                        case "D'":
                            nextStep = "Turn the Down face counter-clockwise.";
                            highlight(nextStep);
                            break;
                        case "D2":
                            nextStep = "Turn the Down face twice.";
                            highlight(nextStep);
                            break;
                        case "F":
                            nextStep = "Turn the Front face clockwise.";
                            highlight(nextStep);
                            break;
                        case "F'":
                            nextStep = "Turn the Front face counter-clockwise.";
                            highlight(nextStep);
                            break;
                        case "F2":
                            nextStep = "Turn the Front face twice.";
                            highlight(nextStep);
                            break;
                        case "B":
                            nextStep = "Turn the Back face clockwise.";
                            highlight(nextStep);
                            break;
                        case "B'":
                            nextStep = "Turn the Back face counter-clockwise.";
                            highlight(nextStep);
                            break;
                        case "B2":
                            nextStep = "Turn the Back face twice.";
                            highlight(nextStep);
                            break;
                    }
                    // Update the solution
                    steps--;
                    currentStep++;
                }
            }

            // Highlight and bold words
            public void highlight (String nextStep) {
                Pattern pattern = Pattern.compile("\\b(?!Turn|the|face|\\.)\\w+\\b");
                Matcher matcher = pattern.matcher(nextStep);
                SpannableString spannableString = new SpannableString(nextStep);
                while (matcher.find()) {
                    spannableString.setSpan(new ForegroundColorSpan(Color.rgb(115, 187, 243)), matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new StyleSpan(Typeface.BOLD), matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                solutionStep.setText(spannableString);
            }
        });


        // Speaker
        ImageButton btnSpeak = findViewById(R.id.btn_speak);
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView solutionStep = findViewById(R.id.tv_solution_step);
                String textToRead = solutionStep.getText().toString();
                textToSpeech.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        // Auto play
        // Get the delay from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        int delay = sharedPreferences.getInt("delay", 3000); // default value 3000 ms
        btnAutoPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAutoPlaying = !isAutoPlaying;
                if (isAutoPlaying) {
                    // Start auto play
                    autoPlayNextStep(delay);
                    btnAutoPlay.setBackgroundColor(Color.parseColor("#dd0d18"));
                } else {
                    // Stop auto play
                    btnAutoPlay.setBackgroundColor(Color.parseColor("#73BBF3"));
                }
            }
        });


    }

    // Release the TextToSpeech object resources
    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }


    // Auto play
    private void autoPlayNextStep(int delay) {
        if (!isAutoPlaying) {
            return;
        }

        Button btnNextStep = findViewById(R.id.btn_next_step);
        ImageButton btnSpeak = findViewById(R.id.btn_speak);
        Button finish = findViewById(R.id.btn_finish);

        // Click the next step button
        btnNextStep.performClick();
        // Check if the next step is available
        if (!btnNextStep.isEnabled()) {
            // Stop auto play and finish
            isAutoPlaying = false;
            finish.performClick();
            return;
        }
        // Delay 200ms and then click the speaker button
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                btnSpeak.performClick();
            }
        }, 200);

        // Continue auto play with a delay
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                autoPlayNextStep(delay);
            }
        }, delay);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isAutoPlaying = false;
        btnAutoPlay.setBackgroundColor(Color.parseColor("#73BBF3"));
    }

    @Override
    public void onBackPressed() {
        // Sure to go back to MainActivity (Home Page)?
        new MaterialAlertDialogBuilder(this)
                .setTitle("Sure to go back to Home Page?")
                .setMessage("You will lose the current progress.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Stop the timer
                        handler.removeCallbacks(timer);
                        // kill solution activity
                        finish();
                        // Go to the MainActivity
                        Intent intent = new Intent(Solution.this, MainActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

}


//    // Web Cube Link
//    static public String generateCubeLink(String solution) {
//        String url = "https://ruwix.com/widget/3d/?";
//        url += String.format("&alg=%s", Uri.encode(solution));
//        url += String.format("&hover=%s" , "1");
//        url += String.format("&speed=%s" , "1000");
//        url += String.format("&flags=%s" , "showalg");
//        url += String.format("&colors=%s" , Uri.encode("U:y L:r F:g R:o B:b D:w", ":"));
//        url += String.format("&algdisplay=%s" , "rotations");
//
//        url = url.replace("'", "%27");
//
//        return url;
//    }