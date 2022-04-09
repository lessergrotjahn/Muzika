package com.example.muzika;

import static com.example.muzika.MainActivity.cleanSongName;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class PlayerActivity extends AppCompatActivity {
    Button playButton, skipNextButton, skipPrevButton, nextButton, prevButton;
    TextView playingSongTxtName, barLeftTxt, barRightTxt;
    SeekBar seekBar;
    Thread updateSeekBar;

    String songName;
    MediaPlayer mediaPlayer;

    int position;
    ArrayList<File> songs;

    final int FFtime = 10000;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mediaPlayer.isPlaying()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getSupportActionBar().setTitle("Muzika - Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        playButton = findViewById(R.id.playButton);
        skipNextButton = findViewById(R.id.skipNextButton);
        skipPrevButton = findViewById(R.id.skipPrevButton);
        nextButton = findViewById(R.id.nextButton);
        prevButton = findViewById(R.id.prevButton);
        playingSongTxtName = findViewById(R.id.playingSongTxtName);
        barLeftTxt = findViewById(R.id.barLeftTxt);
        barRightTxt = findViewById(R.id.barRightTxt);
        seekBar = findViewById(R.id.seekBar);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        songs = (ArrayList) bundle.getParcelableArrayList("songs");
        position = bundle.getInt("pos", 0);
        playingSongTxtName.setSelected(true);

        File firstSong = songs.get(position);
        Uri uri = Uri.parse(firstSong.toString());
        songName = firstSong.getName();
        songName = cleanSongName(songName);
        playingSongTxtName.setText(songName);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(mediaPlayer -> skipNextButton.performClick());

        initSeekBar();

        playButton.setBackgroundResource(R.drawable.ic_pause);

        songs = (ArrayList) bundle.getParcelableArrayList("selectedSongs");
        if (songs.size() == 0) {
            songs = (ArrayList) bundle.getParcelableArrayList("songs");
        }

        Collections.shuffle(songs);

        int firstSongIndex = songs.indexOf(firstSong);
        if (firstSongIndex != -1) {
            position = firstSongIndex;
        }
        else {
            position = 0;
        }

        playButton.setOnClickListener(view -> {
            if (mediaPlayer.isPlaying()) {
                playButton.setBackgroundResource(R.drawable.ic_play);
                mediaPlayer.pause();
            }
            else {
                playButton.setBackgroundResource(R.drawable.ic_pause);
                mediaPlayer.start();
            }
        });

        skipNextButton.setOnClickListener(view -> {
            position = ((position+1) % songs.size());
            playSongAtPosition();
        });

        skipPrevButton.setOnClickListener(view -> {
                position -= 1;
                if (position == -1) {
                    position = songs.size() - 1;
                }
                playSongAtPosition();
        });

        nextButton.setOnClickListener(view -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + FFtime);
            }
        });

        prevButton.setOnClickListener(view -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - FFtime);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        super.onDestroy();
    }

    private void playSongAtPosition() {

        mediaPlayer.stop();
        mediaPlayer.release();

        Uri uri = Uri.parse(songs.get(position).toString());
        songName = songs.get(position).getName();
        songName = cleanSongName(songName);
        playingSongTxtName.setText(songName);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(mediaPlayer -> skipNextButton.performClick());

        initSeekBar();

        playButton.setBackgroundResource(R.drawable.ic_pause);
    }

    private void initSeekBar() {
        updateSeekBar = new Thread() {
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;
                while (currentPosition < totalDuration) {
                    try {
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                    }
                    catch (InterruptedException | IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        seekBar.setMax(mediaPlayer.getDuration());
        updateSeekBar.start();
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endTime = createTime(mediaPlayer.getDuration());
        barRightTxt.setText(endTime);

        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                barLeftTxt.setText(currentTime);
                handler.postDelayed(this, delay);
            }
        }, delay);

    }

    public void startService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Muzika");
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }

    public String createTime(int time_ms) {
        String time = "";
        int min = time_ms/60000;
        int sec = (time_ms/1000)%60;
        time += min + ":";
        if (sec < 10) {
            time += "0";
        }
        time += sec;
        return time;
    }
    
}