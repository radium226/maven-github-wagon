package com.github.radium226.github.maven;

import java.io.InputStream;
import java.io.OutputStream;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;

public interface Downloader {

    public Downloader with(GitHubService gitHubHelper);

    public InputStream download(String resourceName) throws TransferFailedException, ResourceDoesNotExistException;

    public boolean accept(String resourceName);

}
