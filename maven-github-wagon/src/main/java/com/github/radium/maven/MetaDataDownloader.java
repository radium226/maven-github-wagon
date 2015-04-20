package com.github.radium.maven;

import com.github.radium.common.Ok;
import com.github.radium.common.Pair;
import com.github.radium.github.wagon.GitHubHelper;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.squareup.okhttp.OkHttpClient;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.codec.Charsets;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.xml.sax.InputSource;

public class MetaDataDownloader {

    private GitHubHelper helper;

    public MetaDataDownloader(GitHubHelper helper) {
        super();
        
        this.helper = helper;
    }

    public InputStream download(String resourceName) throws IOException {
        System.out.println(" ___________________ " + resourceName);
        
        
        Pair<GHRepository, String> repositoryAndTagName = helper.findRepositoryAndTagNameContainingResource(resourceName);
        GHRepository repository = repositoryAndTagName.getFirst();
        String artifactID = null;
        String groupID = null;
        StringBuilder metaData = new StringBuilder("<metadata>");
        metaData.append("   <groupId>").append(groupID).append("</groupId>");
        metaData.append("   <artifactId>").append(artifactID).append("</artifactId>");
        metaData.append("   <versioning>");
        metaData.append("       <versions>");
        for (GHRelease release : repository.listReleases()) {
            PomDownloader pomDownloader = new PomDownloader(helper);
            try (InputStream pomInputStream = pomDownloader.download(release)) {
                try {
                    metaData.append("           <version>").append(readVersion(pomInputStream)).append("</version>");
                } catch (XPathExpressionException e) {
                    throw new IOException(e);
                }
            }
        }
        metaData.append("       </versions>");
        metaData.append("   </versioning>");
        metaData.append("</metadata>");

        InputStream inputStream = new ByteArrayInputStream(metaData.toString().getBytes(Charsets.UTF_8));
        return inputStream;
    }

    public static String readVersion(InputStream pomInputStream) throws XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        XPathExpression xPathExpression = xPath.compile("/version");
        String version = xPathExpression.evaluate(new InputSource(pomInputStream));
        return version;
    }

}
