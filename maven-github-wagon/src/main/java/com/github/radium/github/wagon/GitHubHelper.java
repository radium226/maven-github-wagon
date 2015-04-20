package com.github.radium.github.wagon;

import com.github.radium.common.Ok;
import com.github.radium.common.Pair;
import com.github.radium.maven.Coordinates;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class GitHubHelper {
    
    final private static Logger LOGGER = LoggerFactory.getLogger(GitHubHelper.class);
    
    private GitHub gitHub;
    private String url;
    private File configFile;
    private OkHttpClient httpClient;
    
    private GitHubHelper(GitHub gitHub) {
        super();
        
        this.gitHub = gitHub;
    }
    
    public static GitHubHelper forGitHub(GitHub gitHub) {
        return new GitHubHelper(gitHub);
    }
    
    public GitHubHelper withUrl(String url) {
        this.url = url;
        return this;
    }
    
    public GitHubHelper withConfigFile(File configFile) {
        this.configFile = configFile;
        return this;
    }
    
    public GitHubHelper withHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }
    
    public Pair<GHRepository, String> findRepositoryAndTagNameContainingResource(String resourceName) throws IOException {
        Coordinates coordinates = Coordinates.of(resourceName);
        System.out.println("coordiantes = " + coordinates);
        return findRepositoryAndTagNameContainingCoordinates(coordinates);
    }
    
    public Pair<GHRepository, String> findRepositoryAndTagNameContainingCoordinates(Coordinates coordinates) throws IOException {
        coordinates = coordinates.withPackaging("jar");
        Map<Coordinates, Pair<GHRepository, String>> mappings = getMappings();
        GHRepository repository = null; 
        String tagName = null; 
        if (mappings.containsKey(coordinates)) {
            Pair<GHRepository, String> repositoryAndTagNamePattern = mappings.get(coordinates);
            repository = repositoryAndTagNamePattern.getFirst();
            String version = coordinates.getVersion();
            tagName = repositoryAndTagNamePattern.getSecond().replace("%{version}", version);
        } else {
            String groupID = coordinates.getGroupID();
            if (groupID.startsWith("com.github")) {
                groupID = groupID.substring("com.github".length());
            }
            String version = coordinates.getVersion();
            String artifactID = coordinates.getArtifactID();
            tagName = artifactID + "-" + version;
            String repositoryName = groupID + "/" + artifactID;
            System.out.println(" =================> Trying " + repositoryName);
            repository = gitHub.getRepository(repositoryName);
        }
        return Pair.of(repository, tagName);
    }
    
    public Map<Coordinates, Pair<GHRepository, String>> getMappings() {
        Map<Coordinates,  Pair<GHRepository, String>> mappings = Maps.newHashMap();
        List<Map<String, String>> yamlList = Lists.newArrayList();
        try (FileInputStream fileInputStream = new FileInputStream(configFile)) {
            yamlList = (List<Map<String, String>>) new Yaml().load(fileInputStream);
            System.out.println("++++++++++++) object = " + yamlList);
        } catch (IOException e) {
            LOGGER.warn("Unable to read configuration file", e);
        }
        
        for (Map<String, String> yamlMap : yamlList) {
            try {
                Coordinates coordinates = Coordinates.fromText(yamlMap.get("maven_dependency"));
                GHRepository repository = gitHub.getRepository(yamlMap.get("github_repository"));
                String tagNamePattern = yamlMap.get("tag_name_pattern");
                mappings.put(coordinates, Pair.of(repository, tagNamePattern));
            } catch (IOException e) {
                LOGGER.warn("Something went wrong", e);
            }
        }        
        return mappings;
    }
    
    public String findResourceURL(String resourceName) throws IOException {
        Coordinates coordinates = Coordinates.of(resourceName);
        Pair<GHRepository, String> repositoryAndTagName = findRepositoryAndTagNameContainingCoordinates(coordinates);
        GHRepository repository = repositoryAndTagName.getFirst();
        System.out.println("{}{}{}{}{}{}{}{}{}{}{}{}{}{}{}{}{}{}{}{}{}{} repository = " + repository);

        PagedIterable<GHRelease> releases = repository.listReleases();
        for (GHRelease release : releases) {
            System.out.println("TAGNAME = " + release.getTagName() + " / " + coordinates.getArtifactID() + "-" + coordinates.getVersion());
            String tagName = release.getTagName();
            if (tagName.equals(repositoryAndTagName.getSecond())) {
                System.out.println("++++++++ resourceName = " + resourceName);
                if (resourceName.endsWith(".pom")) {
                    String url = repository.getFileContent("pom.xml", tagName).getDownloadUrl();
                    System.out.println(" +++++++++++} pomUrl = " + url);
                    return url;
                } else if (resourceName.endsWith(".jar")) {
                    List<GHAsset> assets = release.getAssets();
                    for (GHAsset asset : assets) {
                        if (resourceName.endsWith(asset.getName())) {
                            String url = asset.getBrowserDownloadUrl();
                            return url;
                        }
                    }
                }
            }
        }
        return null; 
    }
    
    public InputStream download(String url) throws IOException {
        return Ok.download(httpClient, url);
    }
    
    
}
