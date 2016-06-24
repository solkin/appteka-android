package com.tomclaw.appsend;

/**
 * Created by Solkin on 08.11.2014.
 */
public class VariableBuffer {

    private static final int DEFAULT_BUFFER_SIZE = 128 * 1024;
    private static final int MAXIMUM_BUFFER_SIZE = 1 * 1024 * 1024;

    private byte[] buffer;

    private long executeStart;
    private long executeTime;
    private float averageSpeed;
    private int bufferSize;

    public VariableBuffer() {
        reset();
    }

    public void onExecuteStart() {
        executeStart = System.currentTimeMillis();
        executeTime = 0;
    }

    public void onExecuteCompleted(int read) {
        executeTime = System.currentTimeMillis() - executeStart;
        executeStart = 0;
        if (executeTime > 0) {
            float bytesPerSecond = 1000 * read / executeTime;
            if (averageSpeed > 0) {
                averageSpeed = (averageSpeed + bytesPerSecond) / 2;
            } else {
                averageSpeed = bytesPerSecond;
            }
            int size = (int) averageSpeed;
            if (size < DEFAULT_BUFFER_SIZE) {
                size = DEFAULT_BUFFER_SIZE;
            } else if (size > MAXIMUM_BUFFER_SIZE) {
                size = MAXIMUM_BUFFER_SIZE;
            }
            bufferSize = size;
            // Log.d(Settings.LOG_TAG, "speed: " + StringUtil.formatSpeed(averageSpeed) + ", bufferSize: " + bufferSize);
        }
    }

    public void reset() {
        applyBufferSize(DEFAULT_BUFFER_SIZE);
    }

    private void applyBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        this.buffer = new byte[bufferSize];
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public float getAverageSpeed() {
        return averageSpeed;
    }

    public byte[] calculateBuffer() {
        buffer = new byte[bufferSize];
        return buffer;
    }
}
