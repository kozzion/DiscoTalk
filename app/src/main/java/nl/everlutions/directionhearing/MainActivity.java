package nl.everlutions.directionhearing;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.button_play)
    Button mButtonPlay;
    @BindView(R.id.button_record)
    Button mButtonRecord;

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;


    private AudioSampleManager mAudioSampleManager;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        String output = audioManager.getProperty("PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED");
        Toast.makeText(this, "output: " + output, Toast.LENGTH_LONG).show();

        //New code
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        mAudioSampleManager = new AudioSampleManager();

    }

    @Override
    public void onStop() {
        super.onStop();
        mAudioSampleManager.onStop();

    }

    @OnClick(R.id.button_record)
    public void onRecordButtonClick() {
        Log.e(LOG_TAG, String.format("Click: " + mAudioSampleManager.mIsRecording));
        ///*  onRecord(mIsRecording);
        if (mAudioSampleManager.mIsRecording) {
            Log.e(LOG_TAG, String.format("Click: stop"));
            mAudioSampleManager.recordAudioStop();
            mButtonRecord.setText("Start recording");
        } else {
            Log.e(LOG_TAG, String.format("Click: start"));
            mAudioSampleManager.recordAudioStart();
            mButtonRecord.setText("Stop recording");
        }
    }

    @OnClick(R.id.button_play)
    public void onPlayButtonClick() {
        Log.e(LOG_TAG, String.format("Click: " + mAudioSampleManager.mIsPlaying));
//        onPlay(mIsPlaying);

        if (mAudioSampleManager.mIsPlaying) {
            Log.e(LOG_TAG, String.format("Click: queuePlaySamples back"));
            mAudioSampleManager.playAudioStop();
            mButtonPlay.setText("Stop playing");
        } else {
            Log.e(LOG_TAG, String.format("Click: stop  start"));
            mAudioSampleManager.playAudioStart();
            mButtonPlay.setText("Start playing");
        }
    }

}
