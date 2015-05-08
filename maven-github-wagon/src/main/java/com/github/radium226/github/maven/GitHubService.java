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

import com.github.radium226.common.Ok;
import com.github.radium226.common.Pair;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.squareup.okhttp.OkHttpClient;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitHubService {

    final private static Logger LOGGER = LoggerFactory.getLogger(GitHubService.class);

    private GitHub gitHub;
    private OkHttpClient httpClient;

    private GitHubService(GitHub gitHub) {
        super();

        this.gitHub = gitHub;
    }

    public static GitHubService forGitHub(GitHub gitHub) {
        return new GitHubService(gitHub);
    }

    public GitHubService withHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    public GHRepository findRepository(String groupID, String artifactID) throws TransferFailedException {
        try {
            String repositoryName = groupID.substring("com.github".length()) + "/" + artifactID;
            return gitHub.getRepository(repositoryName);
        } catch (IOException e) {
            throw new TransferFailedException("Lol", e);
        }
    }

    public Pair<GHRepository, Optional<GHTag>> findRepositoryAndTag(String groupID, String artifactID, Optional<String> version) throws TransferFailedException {
        Pair<GHRepository, Optional<GHTag>> repositoryAndTag = null;
        if (version.isPresent()) {
            repositoryAndTag = findRepositoryAndTag(groupID, artifactID, version.get()).mapSecond((GHTag tag) -> {
                return Optional.of(tag);
            });
        } else {
            repositoryAndTag = Pair.of(findRepository(groupID, artifactID), Optional.absent());
        }
        return repositoryAndTag;
    }

    public Pair<GHRepository, GHTag> findRepositoryAndTag(String groupID, String artifactID, String version) throws TransferFailedException {
        try {
            GHRepository repository = findRepository(groupID, artifactID);
            GHTag tag = Iterables.getFirst(Iterables.filter(repository.listTags(), new Predicate<GHTag>() {

                @Override
                public boolean apply(GHTag tag) {
                    return tag.getName().equals(version);
                }

            }), null);

            return Pair.of(repository, tag);
        } catch (IOException e) {
            throw new TransferFailedException("Something happened with GitHub", e);
        }
    }

//    public String findResourceURL(String resourceName) throws IOException, TransferFailedException {
//        Coordinates coordinates = Coordinates.of(resourceName);
//        Pair<GHRepository, GHTag> repositoryAndTag = findRepositoryAndTag(coordinates.get);
//        GHRepository repository = repositoryAndTag.getFirst();
//        PagedIterable<GHRelease> releases = repository.listReleases();
//        for (GHRelease release : releases) {
//            String tagName = release.getTagName();
//            if (tagName.equals(repositoryAndTag.getSecond().getName())) {
//                if (resourceName.endsWith(".pom")) {
//                    String url = repository.getFileContent("pom.xml", tagName).getDownloadUrl();
//                    return url;
//                } else if (resourceName.endsWith(".jar")) {
//                    List<GHAsset> assets = release.getAssets();
//                    for (GHAsset asset : assets) {
//                        if (resourceName.endsWith(asset.getName())) {
//                            String url = asset.getBrowserDownloadUrl();
//                            return url;
//                        }
//                    }
//                }
//            }
//        }
//        return null;
//    }
    public InputStream downloadFileContent(String filePath, GHTag tag) throws TransferFailedException {
        return downloadFileContent(filePath, tag.getOwner(), tag);
    }

    public InputStream downloadFileContent(String filePath, GHRepository repository, String tagName) throws TransferFailedException {
        try {
            GHTag tag = Iterables.getFirst(Iterables.filter(repository.listTags(), new Predicate<GHTag>() {

                @Override
                public boolean apply(GHTag tag) {
                    return tagName.equals(tag.getName());
                }

            }), null);
            return downloadFileContent(filePath, repository, tag);
        } catch (IOException e) {
            throw new TransferFailedException("I don't know why it's so complicated... ", e);
        }
    }

    public InputStream downloadFileContent(String filePath, GHRepository repository, GHTag tag) throws TransferFailedException {
        try {
            String url = repository.getFileContent(filePath, tag.getName()).getDownloadUrl();
            return download(url);
        } catch (IOException e) {
            throw new TransferFailedException("Something happened with GitHub", e);
        }
    }

    public InputStream download(String url) throws IOException {
        return Ok.download(httpClient, url);
    }

//    public InputStream downloadAsset(String fileName, Coordinates coordinates) throws TransferFailedException, ResourceDoesNotExistException {
//        return downloadAsset(fileName, findRepositoryAndTag(coordinates).getSecond());
//    }
//    public InputStream downloadFileContent(String filePath, Coordinates coordinates) throws TransferFailedException {
//        return downloadFileContent(filePath, findRepositoryAndTag(coordinates).getSecond());
//    }
    public InputStream downloadAsset(String fileName, GHTag tag) throws TransferFailedException, ResourceDoesNotExistException {
        return downloadAsset(fileName, tag.getOwner(), tag);
    }

    public InputStream downloadAsset(String filePath, GHRepository repository, GHTag tag) throws TransferFailedException, ResourceDoesNotExistException {
        try {
            PagedIterable<GHRelease> releases = repository.listReleases();
            for (GHRelease release : releases) {
                String tagName = release.getTagName();
                if (tagName.equals(tag.getName())) {
                    List<GHAsset> assets = release.getAssets();
                    for (GHAsset asset : assets) {
                        if (filePath.equals(asset.getName())) {
                            String url = asset.getBrowserDownloadUrl();
                            return download(url);
                        }
                    }
                }
            }
            throw new ResourceDoesNotExistException("Unable to locate the right asset");
        } catch (IOException e) {
            throw new TransferFailedException("Something happened with GitHub", e);
        }
    }

}
