package nl.everlutions.directionhearing;

import android.util.Log;

/**
 * Created by jaapo on 30-5-2017.
 */

public class ArrayTranscoderShortShort
{
    private short [] mCurrentArray;
    private IMessageHandler< short []> mHandler;
    private int mOutputSize;
    private int mWriteIndex;

    public ArrayTranscoderShortShort(int outputSize, IMessageHandler< short []> handler)
    {
        mOutputSize = outputSize;
        mHandler = handler;
        mCurrentArray = new short[outputSize];
        mWriteIndex = 0;
    }

    public void transCode(short [] input, int toWriteCount)
    {
        //int toWriteCount = record.read(audioRecordBuffer, 0, audioRecordBuffer.length);
        Log.e("transCode", "transCode");
        int readIndex = 0;
        while (0 < toWriteCount)
        {
            int spaceRemaining = mOutputSize - mWriteIndex;
            if( mWriteIndex == mOutputSize)
            {
                Log.e("transCode", "queue all");
                mHandler.handle(mCurrentArray);
                mCurrentArray = new short[mOutputSize];
                mWriteIndex = 0;
            }
            else if(toWriteCount <= spaceRemaining)
            {
                System.arraycopy(input, readIndex, mCurrentArray, mWriteIndex, toWriteCount);
                mWriteIndex += toWriteCount;
                Log.e("transCode", "write all");
                Log.e("transCode", "mReadIndex: " + readIndex);
                Log.e("transCode", "mWriteIndex: " + mWriteIndex);

                if( mWriteIndex == mOutputSize)
                {
                    Log.e("transCode", "queue all");
                    mHandler.handle(mCurrentArray);
                    mCurrentArray = new short[mOutputSize];
                    mWriteIndex = 0;
                }
                return;
            }
            else
            {
                System.arraycopy(input, readIndex, mCurrentArray, mWriteIndex, spaceRemaining);
                toWriteCount -=  spaceRemaining;
                readIndex +=  spaceRemaining;
                mWriteIndex +=  spaceRemaining;
                Log.e("transCode", "write some");
                Log.e("transCode", "readIndex: " + readIndex);
                Log.e("transCode", "mWriteIndex: " + mWriteIndex);
            }
        }
    }


}
