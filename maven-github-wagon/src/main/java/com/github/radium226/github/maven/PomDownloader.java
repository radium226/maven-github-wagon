/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.radium226.github.maven;

import com.github.radium226.common.Ok;
import com.github.radium226.common.Pair;
import com.github.radium226.github.maven.GitHubService;
import com.github.radium226.maven.Coordinates;
import com.google.common.base.Optional;
import com.squareup.okhttp.OkHttpClient;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;

/**
 *
 * @author adrien
 */
public class PomDownloader extends AbstractDownloader {

    public static final String RESOURCE_NAME_REGEX = "^com/github/[a-zA-Z0-9_-]+?/[a-zA-Z0-9_-]+?/.+?/.+" + Pattern.quote(".pom") + "$";
    
    public PomDownloader() {
        super(RESOURCE_NAME_REGEX);
    }

    @Override
    public InputStream download(GHRepository repository, Optional<GHTag> tag, String fileName) throws ResourceDoesNotExistException, TransferFailedException {
        InputStream inputStream = getGitHubService().downloadFileContent("pom.xml", tag.get());
        return inputStream;
    }

}
