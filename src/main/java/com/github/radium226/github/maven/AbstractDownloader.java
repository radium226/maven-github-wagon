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
package com.github.radium226.github.maven;

import com.github.radium226.common.Pair;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDownloader implements Downloader {

    private GitHubService gitHubService;
    private final String resourceNameRegex;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDownloader.class);

    public AbstractDownloader(String resourceNameRegex) {
        super();

        this.resourceNameRegex = resourceNameRegex;
    }

    @Override
    public Downloader with(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
        return this;
    }

    protected GitHubService getGitHubService() {
        if (gitHubService == null) {
            throw new IllegalStateException("GitHubService should be provided");
        }
        return this.gitHubService;
    }

    @Override
    public boolean accept(String resourceName) {
        boolean match = Pattern.matches(this.resourceNameRegex, resourceName);
        LOGGER.debug("Pattern.matches({}, {}) = {}", this.resourceNameRegex, resourceName, match);
        return match;
    }

    protected abstract InputStream download(GHRepository repository, Optional<GHTag> tag, String fileName) throws TransferFailedException, ResourceDoesNotExistException;

    @Override
    public InputStream download(String resourceName) throws TransferFailedException, ResourceDoesNotExistException {
        List<String> parts = Lists.newArrayList(Splitter.on("/").splitToList(resourceName));
        if (!Joiner.on(".").join(parts.subList(0, 2)).equals("com.github")) {
            throw new ResourceDoesNotExistException("The resource " + resourceName + " is not on GitHub");
        }

        parts.remove(0);
        parts.remove(0);

        String groupID = "com.github" + parts.get(0);
        String artifactID = parts.get(1);
        parts.remove(0);
        parts.remove(0);

        Optional<String> version = Optional.absent();
        if (parts.size() == 2) {
            version = Optional.of(parts.get(0));
            parts.remove(0);
        }

        String fileName = parts.get(0);

        Pair<GHRepository, Optional<GHTag>> repositoryAndTag = gitHubService.findRepositoryAndTag(groupID, artifactID, version);
        return download(repositoryAndTag.getFirst(), repositoryAndTag.getSecond(), fileName);
    }

}
