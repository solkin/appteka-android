package com.tomclaw.appsend.util;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by solkin on 13.01.15.
 */
public class MultipartStream extends OutputStream {

    private static final String TAG = "multipart";

    private String boundary;
    private OutputStream outputStream;

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

            VariableBuffer buffer = new VariableBuffer();
            int cache;
            long sent = 0;
            while ((cache = inputStream.read(buffer.calculateBuffer())) != -1) {
                buffer.onExecuteStart();
                outputStream.write(buffer.getBuffer(), 0, cache);
                outputStream.flush();
                buffer.onExecuteCompleted(cache);
                sent += cache;
                callback.onProgress(sent);
            }
        } catch (final IOException e) {
            Log.e(TAG, e.getMessage(), e);
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
        public void onProgress(long sent);
    }
}
