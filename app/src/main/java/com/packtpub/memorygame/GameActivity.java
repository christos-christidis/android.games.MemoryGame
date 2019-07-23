package com.packtpub.memorygame;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Locale;
import java.util.Random;

public class GameActivity extends Activity implements View.OnClickListener {

    // initialize sound variables
    private final SoundPool soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
    private final int[] samples = new int[4];

    private final Button[] sampleButtons = new Button[4];

    private final int INITIAL_SCORE = 0;
    private int score;

    private final int INITIAL_DIFFICULTY = 3;
    private int difficultyLevel;

    // An array to hold the randomly generated sequence
    private final int[] sequence = new int[100];

    private boolean timeToPlaySequence;     // Are we playing a sequence at the moment?
    private boolean userAllowedToClick;
    private boolean userCanClickOnSamples = true;

    private int elementToPlay = 0;          // which element of the sequence are we on?
    private int numResponses;

    private SharedPreferences prefs;

    private Animation wobble;

    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        VisibilityManager.hideSystemUI(this);

        try {
            AssetManager assetManager = getAssets();
            for (int i = 0; i < samples.length; i++) {
                AssetFileDescriptor descriptor = assetManager.openFd(String.format(Locale.getDefault(), "sample%d.ogg", i + 1));
                samples[i] = soundPool.load(descriptor, 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        prefs = getSharedPreferences(MainActivity.DATA_NAME, MODE_PRIVATE);

        setScore(INITIAL_SCORE);
        setDifficulty(INITIAL_DIFFICULTY);

        sampleButtons[0] = findViewById(R.id.button1);
        sampleButtons[1] = findViewById(R.id.button2);
        sampleButtons[2] = findViewById(R.id.button3);
        sampleButtons[3] = findViewById(R.id.button4);
        Button buttonReplay = findViewById(R.id.buttonReplay);

        for (Button button : sampleButtons) {
            button.setOnClickListener(this);
        }
        buttonReplay.setOnClickListener(this);

        wobble = AnimationUtils.loadAnimation(this, R.anim.wobble);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (timeToPlaySequence) {
                    int sampleIndex = sequence[elementToPlay];
                    sampleButtons[sampleIndex].startAnimation(wobble);
                    playSample(samples[sampleIndex]);

                    elementToPlay++;
                    if (elementToPlay == difficultyLevel) {
                        sequenceFinished();
                    }
                }

                // sends empty message to self in the future
                handler.postDelayed(this, 900);
            }
        }, 1000);

        playSequence();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            VisibilityManager.hideSystemUI(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onClick(View v) {
        // First handle buttonReplay alone.
        if (userAllowedToClick && v.getId() == R.id.buttonReplay) {
            restartGame();
        }

        // For the buttons to be clicked, both conditions must hold true
        if (userAllowedToClick && userCanClickOnSamples) {
            switch (v.getId()) {
                case R.id.button1:
                    playSample(samples[0]);
                    checkChoice(0);
                    break;
                case R.id.button2:
                    playSample(samples[1]);
                    checkChoice(1);
                    break;
                case R.id.button3:
                    playSample(samples[2]);
                    checkChoice(2);
                    break;
                case R.id.button4:
                    playSample(samples[3]);
                    checkChoice(3);
                    break;
            }
        }
    }

    private void restartGame() {
        setScore(INITIAL_SCORE);
        setDifficulty(INITIAL_DIFFICULTY);
        userCanClickOnSamples = true;
        playSequence();
    }

    private void createRandomSequence() {
        Random random = new Random();
        for (int i = 0; i < difficultyLevel; i++) {
            sequence[i] = random.nextInt(4);
        }
    }

    private void playSequence() {
        userAllowedToClick = false;
        setPrompt(R.string.text_prompt_watch);

        createRandomSequence();

        elementToPlay = 0;
        numResponses = 0;
        timeToPlaySequence = true;
    }

    private void sequenceFinished() {
        timeToPlaySequence = false;
        setPrompt(R.string.text_prompt_go);
        userAllowedToClick = true;
    }

    private void checkChoice(int userChoice) {
        numResponses++;

        if (userChoice == sequence[numResponses - 1]) {
            int newScore = score + (userChoice + 1) * 2;
            setScore(newScore);

            // user got the whole sequence
            if (numResponses == difficultyLevel) {
                setDifficulty(difficultyLevel + 1);
                playSequence();
            }
        } else {
            setPrompt(R.string.text_prompt_failed);
            userCanClickOnSamples = false;

            if (score > MainActivity.hiScore) {
                MainActivity.hiScore = score;

                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(MainActivity.INT_NAME, MainActivity.hiScore);
                editor.apply();
                Toast.makeText(this, "New Hi-score!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void playSample(int sample) {
        soundPool.play(sample, 1, 1, 0, 0, 1);
    }

    private void setScore(int newScore) {
        score = newScore;
        TextView textScore = findViewById(R.id.textScore);
        textScore.setText(getString(R.string.text_score, newScore));
    }

    private void setDifficulty(int newDifficulty) {
        difficultyLevel = newDifficulty;
        TextView textDifficulty = findViewById(R.id.textDifficulty);
        textDifficulty.setText(getString(R.string.text_difficulty, newDifficulty));
    }

    private void setPrompt(int stringId) {
        TextView textPrompt = findViewById(R.id.textPrompt);
        textPrompt.setText(getString(stringId));
    }
}
