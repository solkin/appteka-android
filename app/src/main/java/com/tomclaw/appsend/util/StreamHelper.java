package com.tomclaw.appsend.util;

import java.io.Closeable;
import java.io.IOException;

public class StreamHelper {

    public static void safeClose(Closeable... streams) {
        for (Closeable stream : streams) {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
