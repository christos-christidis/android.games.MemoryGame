package com.packtpub.memorygame;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

    // stuff needed for the SharedPreferences
    final static String DATA_NAME = "MEMORY_GAME_PREFS";
    final static String INT_NAME = "HI_SCORE";
    final private static int DEFAULT_SCORE = 0;

    // MainActivity will be stopped & may be destroyed when GameActivity starts. Making this static
    // means that GameActivity will always have access to this variable
    static int hiScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        VisibilityManager.hideSystemUI(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            VisibilityManager.hideSystemUI(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences(DATA_NAME, MODE_PRIVATE);
        hiScore = prefs.getInt(INT_NAME, DEFAULT_SCORE);

        TextView textHiScore = findViewById(R.id.textHiScore);
        textHiScore.setText(getString(R.string.text_hi_score, hiScore));
    }

    public void onClickPlay(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }
}
