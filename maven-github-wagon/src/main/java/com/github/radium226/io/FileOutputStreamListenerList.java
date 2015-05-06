/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.radium226.io;

import com.github.radium226.common.ListenerList;
import java.io.File;

/**
 *
 * @author adrien
 */
public class FileOutputStreamListenerList {

    private final File file;
    private final ListenerList<FileOutputStreamListener> listeners;

    public FileOutputStreamListenerList(File file) {
        super();

        this.file = file;
        listeners = ListenerList.of(FileOutputStreamListener.class);
    }

    public static FileOutputStreamListenerList create(File file) {
        return new FileOutputStreamListenerList(file);
    }

    public void fireBytesWritten(byte[] buffer, int length) {
        listeners.fire("bytesWritten", file, buffer, length);
    }

    public boolean add(FileOutputStreamListener listener) {
        return listeners.add(listener);
    }

    public boolean remove(FileOutputStreamListener listener) {
        return listeners.remove(listener);
    }

}
