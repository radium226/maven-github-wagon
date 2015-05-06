package com.github.radium226.io;

import com.github.radium226.common.ListenerList;
import com.github.radium226.maven.TransferListenerList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ListenableFileOutputStream extends FileOutputStream {

    private final FileOutputStreamListenerList listeners;

    public ListenableFileOutputStream(File file) throws FileNotFoundException {
        super(file);

        this.listeners = FileOutputStreamListenerList.create(file);
    }

    @Override
    public void write(int i) throws IOException {
        super.write(i);
        this.listeners.fireBytesWritten(new byte[]{(byte) i}, 1);
    }

    @Override
    public void write(byte[] buffer) throws IOException {
        super.write(buffer);
        this.listeners.fireBytesWritten(buffer, buffer.length);
    }

    @Override
    public void write(byte buffer[], int offset, int length) throws IOException {
        super.write(buffer, offset, length);
        if (offset != 0) {
            byte[] smallerBuffer = new byte[length];
            System.arraycopy(buffer, offset, smallerBuffer, 0, length);
            buffer = smallerBuffer;
        }
        this.listeners.fireBytesWritten(buffer, buffer.length);
    }

    public void addListener(FileOutputStreamListener listener) {
        listeners.add(listener);
    }

}
