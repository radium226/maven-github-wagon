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

import com.github.radium226.common.ListenerList;
import java.io.File;

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
