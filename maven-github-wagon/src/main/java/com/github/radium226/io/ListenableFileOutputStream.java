/**
 *    Copyright 2015 Radium226
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.github.radium226.io;

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
