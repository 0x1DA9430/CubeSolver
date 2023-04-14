package com.example.cubesolver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Solution extends AppCompatActivity {

    private TextView solution;
    private Intent receivedIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solution);

        // Get the solution from the previous activity
        receivedIntent = getIntent();
        String moves = receivedIntent.getStringExtra("solution");
        moves = moves.substring(0, moves.indexOf('(') - 1); // Remove the unnecessary part

        // Split the solution into separate steps
        String[] movesArray = moves.split("\\s+");
        // Number of steps
        int steps = movesArray.length;

        // Display the solution
        solution = findViewById(R.id.tv_solution);
        solution.setText(moves);
    }
}