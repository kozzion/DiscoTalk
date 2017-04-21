package nl.everlutions.directionhearing;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ShortBuffer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final int SAMPLE_RATE = 44100;
    @BindView(R.id.button_play)
    Button mButtonPlay;
    @BindView(R.id.button_record)
    Button mButtonRecord;

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String mFileName = null;
    boolean mIsRecording = false;
    boolean mIsPlaying = true;

    private MediaRecorder mRecorder = null;


    private MediaPlayer mPlayer = null;


    short[] mOurSamples = new short[SAMPLE_RATE * 120];
    int mSampleWriteIndex;
    int mSampleReadIndex;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private boolean mShouldContinuePlaying;

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

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
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


        mFileName = getExternalCacheDir().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    @OnClick(R.id.button_record)
    public void onRecordButtonClick() {
        Log.e(LOG_TAG, String.format("Click: " + mIsRecording));
        ///*  onRecord(mIsRecording);
        if (mIsRecording) {
            Log.e(LOG_TAG, String.format("Click: stop"));
            recordAudioStop();
            mIsRecording = false;
            mButtonRecord.setText("Start recording");
        } else {
            Log.e(LOG_TAG, String.format("Click: start"));
            recordAudioStart();
            mIsRecording = true;
            mButtonRecord.setText("Stop recording");
        }
    }

    @OnClick(R.id.button_play)
    public void onPlayButtonClick() {
        Log.e(LOG_TAG, String.format("Click: " + mIsPlaying));
//        onPlay(mIsPlaying);

        if (mIsPlaying) {
            Log.e(LOG_TAG, String.format("Click: stop playback"));
            playAudioStop();
            mButtonPlay.setText("Stop playing");
            mIsPlaying= false;
        } else {

            Log.e(LOG_TAG, String.format("Click: play start"));
            playAudioStart();
            mIsPlaying= true;
            mButtonPlay.setText("Start playing");
        }

    }

    private void playAudioStop() {
        mShouldContinuePlaying = false;
    }

    boolean mShouldContinue; // Indicates if recording / playback should stop

    void recordAudioStop() {
        mShouldContinue = false;
    }


    void recordAudioStart() {
        if (!mShouldContinue) {

            mShouldContinue = true;
            mSampleWriteIndex = 0;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

                    // buffer size in bytes
                    int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                            AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT);

                    if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                        bufferSize = SAMPLE_RATE * 2;
                    }

                    short[] audioBuffer = new short[bufferSize / 2];

                    AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                            SAMPLE_RATE,
                            AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            bufferSize);

                    if (record.getState() != AudioRecord.STATE_INITIALIZED) {
                        Log.e(LOG_TAG, "Audio Record can't initialize!");
                        return;
                    }
                    record.startRecording();

                    Log.e(LOG_TAG, "Start recording");

                    while (mShouldContinue) {
                        int numberOfShort = record.read(audioBuffer, 0, audioBuffer.length);

                        System.arraycopy(audioBuffer, 0, mOurSamples, mSampleWriteIndex, numberOfShort);
                        mSampleWriteIndex += numberOfShort;
                        // Do something with the audioBuffer
                    }

                    record.stop();
                    record.release();

                    Log.e(LOG_TAG, String.format("Recording stopped. Samples read: %d", mSampleWriteIndex));

                }
            }).start();
        }
    }

    ShortBuffer mSamples; // the samples to play
    int mNumSamples; // number of samples to play

    void playAudioStart() {
        if(!mShouldContinuePlaying) {
            mShouldContinuePlaying = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mSamples = ShortBuffer.wrap(mOurSamples);
                    mNumSamples = mSampleWriteIndex;
                    int bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT);
                    if (bufferSize == AudioTrack.ERROR || bufferSize == AudioTrack.ERROR_BAD_VALUE) {
                        bufferSize = SAMPLE_RATE * 2;
                    }

                    AudioTrack audioTrack = new AudioTrack(
                            AudioManager.STREAM_MUSIC,
                            SAMPLE_RATE,
                            AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            bufferSize,
                            AudioTrack.MODE_STREAM);

                    audioTrack.play();

                    Log.e(LOG_TAG, "Audio streaming started");

                    short[] buffer = new short[bufferSize];
                    mSamples.rewind();
                    int limit = mNumSamples;
                    int totalWritten = 0;
                    while (mSamples.position() < limit && mShouldContinuePlaying) {
                        int numSamplesLeft = limit - mSamples.position();
                        int samplesToWrite;
                        if (numSamplesLeft >= buffer.length) {
                            mSamples.get(buffer);
                            samplesToWrite = buffer.length;
                        } else {
                            for (int i = numSamplesLeft; i < buffer.length; i++) {
                                buffer[i] = 0;
                            }
                            mSamples.get(buffer, 0, numSamplesLeft);
                            samplesToWrite = numSamplesLeft;
                        }
                        totalWritten += samplesToWrite;
                        audioTrack.write(buffer, 0, samplesToWrite);
                    }

                    if (!mShouldContinuePlaying) {
                        audioTrack.release();
                    }

                    Log.v(LOG_TAG, "Audio streaming finished. Samples written: " + totalWritten);
                }

            }).start();
        }
    }

}
