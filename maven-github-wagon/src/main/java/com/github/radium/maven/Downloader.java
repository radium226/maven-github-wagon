package com.github.radium.maven;

import java.io.IOException;
import java.io.InputStream;

public interface Downloader {

    public InputStream download(String resourceName) throws IOException;
    
}