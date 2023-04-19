package com.example.cubesolver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class History extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Get and Sort the results history
        Map<String, String> resultsHistory = getResultsFromHistory();
        Map<String, String> sortedResultsHistory = sortResultsByDateTime(resultsHistory);
        LinearLayout historyContainer = findViewById(R.id.ll_history_container);

        // Find the minimum result
        if (!sortedResultsHistory.isEmpty()) {
            String minResult = Collections.min(sortedResultsHistory.values());

            for (Map.Entry<String, String> entry : sortedResultsHistory.entrySet()) {
                String dateTime = entry.getKey();
                String result = entry.getValue();
                boolean isBest = result.equals(minResult);

                TextView dateTimeTextView = new TextView(this);
                dateTimeTextView.setText(dateTime);
                dateTimeTextView.setTextSize(16);
                dateTimeTextView.setTextColor(Color.BLACK);

                TextView resultTextView = new TextView(this);
                resultTextView.setText(result);
                resultTextView.setTextSize(16);
                resultTextView.setTextColor(Color.BLACK);
                resultTextView.setGravity(Gravity.END);

                // LinearLayout as a container
                LinearLayout rowLayout = new LinearLayout(this);
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, 20, 0, 20);
                rowLayout.setLayoutParams(layoutParams);

                // Set weight to show them in one line
                LinearLayout.LayoutParams dateTimeLayoutParams = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                LinearLayout.LayoutParams resultLayoutParams = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                dateTimeTextView.setLayoutParams(dateTimeLayoutParams);
                resultTextView.setLayoutParams(resultLayoutParams);


                // Add Date/Time TextView
                rowLayout.addView(dateTimeTextView);
                // Add blue border for the best result
                if (isBest) {
                    rowLayout.setBackgroundColor(Color.rgb(232, 245, 255));
                    rowLayout.setPadding(0, 0, 0, 10);
                    TextView bestTextView = new TextView(this);
                    bestTextView.setText("Best!");
                    bestTextView.setTextSize(14);
                    bestTextView.setTextColor(Color.rgb(115, 187, 243));
                    bestTextView.setTypeface(null, Typeface.BOLD);
                    bestTextView.setGravity(Gravity.END | Gravity.TOP);
                    rowLayout.addView(bestTextView);
                }
                // Add Result TextView
                rowLayout.addView(resultTextView);


                // Add a row and a divider
                historyContainer.addView(rowLayout);
                historyContainer.addView(createDivider());
            }
        }


    }

    private Map<String, String> getResultsFromHistory() {
        SharedPreferences sharedPreferences = getSharedPreferences("results_history", MODE_PRIVATE);
        return (Map<String, String>) sharedPreferences.getAll();
    }

    private Map<String, String> sortResultsByDateTime(Map<String, String> results) {
        TreeMap<String, String> sortedResults = new TreeMap<>(Collections.reverseOrder());
        sortedResults.putAll(results);
        return sortedResults;
    }

    private View createDivider() {
        View divider = new View(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1);
        layoutParams.setMargins(0, 16, 0, 16);
        divider.setLayoutParams(layoutParams);
        divider.setBackgroundColor(Color.parseColor("#BDBDBD"));
        return divider;
    }

}