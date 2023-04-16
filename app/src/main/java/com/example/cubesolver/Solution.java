package com.example.cubesolver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Solution extends AppCompatActivity {

    private TextView solution;
    private Intent receivedIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solution);

//        // Get the solution from the previous activity
//        receivedIntent = getIntent();
//        String moves = receivedIntent.getStringExtra("solution");
        String moves = "R L U D F B  R' L' U'  D' F'  B' R2 L2  U2  D2 F2 B2(14f)";

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

    }
}