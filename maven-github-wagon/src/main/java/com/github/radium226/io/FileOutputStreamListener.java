package com.github.radium226.io;

import java.io.File;

public interface FileOutputStreamListener {

    void bytesWritten(File file, byte[] buffer, int length);

}
