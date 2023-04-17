package com.example.cubesolver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;


public class MainActivity extends AppCompatActivity {

    private ImageButton scanCubeButton;
    private ImageButton popupMenuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanCubeButton = findViewById(R.id.img_btn_scan);
        popupMenuButton = findViewById(R.id.img_btn_popup_menu);

        // Show PopupMenu
        popupMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                final ImageButton popupMenuButton = findViewById(R.id.popup_menu_button);
                popupMenuButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 创建并显示 PopupMenu
                        PopupMenu popupMenu = new PopupMenu(MainActivity.this, popupMenuButton);
                        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.action_history:
                                        // Open History
                                        Intent historyIntent = new Intent(MainActivity.this, History.class);
                                        startActivity(historyIntent);
                                        return true;
                                    case R.id.action_settings:
                                        // Open Settings
                                        Intent intent = new Intent(MainActivity.this, Settings.class);
                                        startActivity(intent);
                                        return true;
                                    default:
                                        return false;
                                }
                            }
                        });
                        popupMenu.show();
                    }
                });
            }
        });


        // Jump to ScanCubeActivity
        scanCubeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Scan.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        // Exit the app
        moveTaskToBack(true);
    }

}