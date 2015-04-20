/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.radium.maven;

import com.github.radium.common.Ok;
import com.github.radium.common.Pair;
import com.github.radium.github.wagon.GitHubHelper;
import com.squareup.okhttp.OkHttpClient;
import java.io.IOException;
import java.io.InputStream;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;

/**
 *
 * @author adrien
 */
public class PomDownloader implements Downloader {
    
    private GitHubHelper helper;

    public PomDownloader(GitHubHelper helper) {
        super();

        this.helper = helper;
    }

    @Override
    public InputStream download(String resourceName) throws IOException {
        Pair<GHRepository, String> repositoryAndTagName = helper.findRepositoryAndTagNameContainingResource(resourceName);
        return download(repositoryAndTagName.getFirst(), repositoryAndTagName.getSecond());
    }
    
    public InputStream download(GHRelease release) throws IOException {
        return download(release.getOwner(), release.getTagName());
    }
    
    public InputStream download(GHRepository repository, String tagName) throws IOException {
        GHContent pomContent = repository.getFileContent("pom.xml", tagName);
        String url = pomContent.getDownloadUrl();
        InputStream inputStream = helper.download(url);
        return inputStream;
    }

}
