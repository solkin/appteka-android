package com.tomclaw.appsend.util;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

/**
 * Created by solkin on 13.01.15.
 */
public class MultipartStream extends OutputStream {

    private static final String TAG = "multipart";
    private static final int BUFFER_SIZE = 128 * 1024;

    private final String boundary;
    private final OutputStream outputStream;

    private boolean isSetLast = false;
    private boolean isSetFirst = false;

    public MultipartStream(OutputStream outputStream, String boundary) {
        this.outputStream = outputStream;
        this.boundary = boundary;
    }

    public void writeFirstBoundaryIfNeeds() {
        if (!isSetFirst) {
            try {
                outputStream.write(("--" + boundary + "\r\n").getBytes());
            } catch (final IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        isSetFirst = true;
    }

    public void writeLastBoundaryIfNeeds() {
        if (isSetLast) {
            return;
        }
        try {
            outputStream.write(("\r\n--" + boundary + "--\r\n").getBytes());
        } catch (final IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        isSetLast = true;
    }

    public void writePart(final String key, final String value) {
        writeFirstBoundaryIfNeeds();
        try {
            outputStream.write(("Content-Disposition: form-data; name=\"" + key + "\"\r\n").getBytes());
            outputStream.write("Content-Type: text/plain; charset=UTF-8\r\n".getBytes());
            outputStream.write("Content-Transfer-Encoding: 8bit\r\n\r\n".getBytes());
            outputStream.write(value.getBytes());
            outputStream.write(("\r\n--" + boundary + "\r\n").getBytes());
        } catch (final IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void writePart(final String key, final String fileName, final InputStream inputStream,
                          String type, ProgressHandler callback) {
        writeFirstBoundaryIfNeeds();
        try {
            type = "Content-Type: " + type + "\r\n";
            outputStream.write(("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + fileName + "\"\r\n").getBytes());
            outputStream.write(type.getBytes());
            outputStream.write("Content-Transfer-Encoding: binary\r\n\r\n".getBytes());

            int cache;
            long sent = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((cache = inputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
                outputStream.write(buffer, 0, cache);
                outputStream.flush();
                sent += cache;
                callback.onProgress(sent);
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
            }
            outputStream.write(("\r\n--" + boundary + "\r\n").getBytes());
        } catch (InterruptedIOException ex) {
            Log.e(TAG, "[upload] IO interruption while application downloading", ex);
            callback.onCancelled(ex);
        } catch (InterruptedException ex) {
            Log.e(TAG, "[upload] Interruption while application downloading", ex);
            callback.onCancelled(ex);
        } catch (final Throwable e) {
            Log.e(TAG, e.getMessage(), e);
            callback.onError(e);
        } finally {
            try {
                inputStream.close();
            } catch (final IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    @Override
    public void write(int oneByte) throws IOException {
        outputStream.write(oneByte);
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    public interface ProgressHandler {
        void onProgress(long sent);

        void onError(Throwable e);

        void onCancelled(Throwable e);
    }
}
