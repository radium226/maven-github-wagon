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

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.codec.Charsets;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

public class MetaDataDownloader extends AbstractDownloader {

    public static final String RESOURCE_NAME_REGEX = "^com/github/[a-zA-Z0-9_-]+?/[a-zA-Z0-9_-]+?/" + Pattern.quote("maven-metadata.xml") + "$";
    private static final Logger LOGGER = LoggerFactory.getLogger(MetaDataDownloader.class);

    public MetaDataDownloader() {
        super(RESOURCE_NAME_REGEX);
    }

    @Override
    public InputStream download(GHRepository repository, Optional<GHTag> tag, String fileName) throws TransferFailedException, ResourceDoesNotExistException {
        List<GHRelease> releases = null;
        try {
            releases = repository.listReleases().asList();
            releases.sort((GHRelease oneRelease, GHRelease otherRelease) -> {
                return -oneRelease.getPublished_at().compareTo(otherRelease.getPublished_at());
            });
        } catch (IOException e) {
            throw new TransferFailedException("Glup", e);
        }

        StringBuilder metaData = new StringBuilder("<metadata>").append("\n");
        GHRelease lastRelease = releases.get(0);
        metaData.append("   <groupId>").append(extractGroupID(lastRelease)).append("</groupId>").append("\n");
        metaData.append("   <artifactId>").append(extractArtifactID(lastRelease)).append("</artifactId>").append("\n");
        metaData.append("   <version>").append(extractVersion(lastRelease)).append("</version>").append("\n");
        metaData.append("   <versioning>").append("\n");
        metaData.append("       <versions>").append("\n");
        for (GHRelease release : releases) {
            metaData.append("           <version>").append(extractVersion(release)).append("</version>").append("\n");
        }
        metaData.append("       </versions>").append("\n");
        metaData.append("   </versioning>").append("\n");
        metaData.append("</metadata>").append("\n");

        LOGGER.debug("metaData = {}", metaData.toString());

        InputStream inputStream = new ByteArrayInputStream(metaData.toString().getBytes(Charsets.UTF_8));
        return inputStream;
    }

    public static String evaluateXPath(InputStream pomInputStream, String expression) throws XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        xPath.setNamespaceContext(new NamespaceContext() {

            @Override
            public String getNamespaceURI(String prefix) {
                if (prefix.equals("ns")) {
                    return "http://maven.apache.org/POM/4.0.0";
                }

                return null;
            }

            @Override
            public String getPrefix(String namespaceURI) {
                return null;
            }

            @Override
            public Iterator getPrefixes(String namespaceURI) {
                return null;
            }
        });
        XPathExpression xPathExpression = xPath.compile(expression);
        String version = (String) xPathExpression.evaluate(new InputSource(pomInputStream), XPathConstants.STRING);
        return version;
    }

    public String extractVersion(GHRelease release) throws TransferFailedException {
        try (InputStream pomInputStream = getGitHubService().downloadFileContent("pom.xml", release.getOwner(), release.getTagName())) {
            return evaluateXPath(pomInputStream, "/ns:project/ns:version/text()");
        } catch (IOException | XPathExpressionException e) {
            throw new TransferFailedException("Youplaboum", e);
        }
    }

    public String extractGroupID(GHRelease release) throws TransferFailedException {
        try (InputStream pomInputStream = getGitHubService().downloadFileContent("pom.xml", release.getOwner(), release.getTagName())) {
            return evaluateXPath(pomInputStream, "/ns:project/ns:groupId/text()");
        } catch (IOException | XPathExpressionException e) {
            throw new TransferFailedException("Youplaboum", e);
        }
    }

    public String extractArtifactID(GHRelease release) throws TransferFailedException {
        try (InputStream pomInputStream = getGitHubService().downloadFileContent("pom.xml", release.getOwner(), release.getTagName())) {
            return evaluateXPath(pomInputStream, "/ns:project/ns:artifactId/text()");
        } catch (IOException | XPathExpressionException e) {
            throw new TransferFailedException("Youplaboum", e);
        }
    }

}
