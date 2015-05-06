package com.github.radium226.github.maven;

import com.google.common.base.Optional;
import java.io.InputStream;
import java.util.regex.Pattern;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;

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
