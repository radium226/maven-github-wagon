package com.github.radium.github.wagon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.codehaus.plexus.component.annotations.Component;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.PagedIterable;
import org.kohsuke.github.extras.OkHttpConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.radium.maven.AbstractWagon;
import com.github.radium.maven.Coordinates;
import com.github.radium.maven.MetaDataDownloader;
import com.github.radium.maven.ProxyInfos;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.net.UrlEscapers;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.FileInputStream;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.yaml.snakeyaml.Yaml;

@Component(role = Wagon.class, hint = "github", instantiationStrategy = "per-lookup")
public class AssetWagon extends AbstractWagon {

    final private static Logger LOGGER = LoggerFactory.getLogger(AssetWagon.class);

    private GitHubHelper gitHubHelper;
    private GitHub gitHub;
    private OkHttpClient okHttpClient;
    
    private String coucou;

    public AssetWagon() {
        super();
    }
    
    public String getCoucou() {
        return coucou;
    }
    
    public void setCoucou(String coucou) {
        this.coucou = coucou;   
    }
    
    public File getConfigFile() {
        File folder = getSession().getProjects().get(0).getBasedir();
        File configFile = new File(folder, "maven-github-wagon.yml");
        return configFile.exists() ? configFile : null; 
    }

    @Override
    public void doConnect(Optional<AuthenticationInfo> authenticationInfo) throws ConnectionException {
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$ configFile = " + getConfigFile());
        try {
            // GitHub Authentication
            GitHubBuilder gitHubBuilder = new GitHubBuilder();
            if (authenticationInfo.isPresent()) {
                String login = authenticationInfo.get().getUserName();
                String password = authenticationInfo.get().getPassword();
                if (password.startsWith("oauth2_token:")) {
                    gitHubBuilder = gitHubBuilder.withOAuthToken(password.substring("oauth2_otken:".length()));
                } else {
                    gitHubBuilder = gitHubBuilder.withPassword(login, password);
                }
            }

            // Proxy Authentication
            okHttpClient = new OkHttpClient();
            ProxyInfo proxyInfo = getProxyInfoByType("http");
            if (proxyInfo != null) {
                okHttpClient.setProxy(ProxyInfos.asProxy(proxyInfo));
                final String userName = proxyInfo.getUserName();
                final String password = proxyInfo.getPassword();
                if (userName != null && password != null) {
                    okHttpClient.setAuthenticator(new Authenticator() {

                        @Override
                        public Request authenticateProxy(Proxy proxy, Response response) {
                            return response.request();
                        }

                        @Override
                        public Request authenticate(Proxy proxy, Response response) throws IOException {
                            String credentials = Credentials.basic(userName, password);
                            return response.request().newBuilder()
                                    .header("Proxy-Authorization", credentials)
                                .build();
                        }

                    });
                }
            }
            gitHub = gitHubBuilder
                    .withConnector(new OkHttpConnector(new OkUrlFactory(okHttpClient)))
                .build();
            
            gitHubHelper = GitHubHelper.forGitHub(gitHub)
                    .withUrl(getRepository().getUrl())
                    .withConfigFile(getConfigFile())
                    .withHttpClient(okHttpClient);

            GHMyself myself = gitHub.getMyself();
            LOGGER.info("Connected as {}", myself.getName());
        } catch (IOException e) {
            throw new ConnectionException("Unable to connect to GitHub", e);
        }

    }

    @Override
    public void doDisconnect() throws ConnectionException {

    }

    @Override
    public void get(String resourceName, File resourceFile) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        resourceFile.getParentFile().mkdirs();
        if (resourceName.contains("guava")) {
            throw new ResourceDoesNotExistException("Fuckit");
        }
        if (resourceName.endsWith("maven-metadata.xml")) {
            try (FileOutputStream resourceFileOutputStream = new FileOutputStream(resourceFile)) {
                MetaDataDownloader metaDataDownloader = new MetaDataDownloader(gitHubHelper);
                InputStream metaDataInputStream = metaDataDownloader.download(resourceName);
                ByteStreams.copy(metaDataInputStream, resourceFileOutputStream);
            } catch (IOException e) {
                throw new TransferFailedException("Hey hey hye! ", e);
            }
        } else {
            try {
                String url = gitHubHelper.findResourceURL(resourceName);
                if (url == null) {
                    throw new ResourceDoesNotExistException("Sorry... ");
                }

                Request request = new Request.Builder()
                        .url(url)
                    .build();
                Response response = okHttpClient.newCall(request).execute();
                InputStream bodyInputStream = response.body().byteStream();
                try (FileOutputStream resourceFileOutputStream = new FileOutputStream(resourceFile)) {
                    ByteStreams.copy(bodyInputStream, resourceFileOutputStream);
                }
            } catch (IOException e) {
                throw new TransferFailedException("Sorry... ", e);
            }
        }
    }

    @Override
    public List<String> getFileList(String arg0) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        System.out.println(" -----> getFileList(" + arg0 + ")");
        throw new ResourceDoesNotExistException("Not implemented");
    }

    @Override
    public boolean getIfNewer(String resourceName, File resourceFile, long timestamp) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        System.out.println(" -----> getIfNewer(" + resourceName + ", " + resourceFile + ")");
        return false;
    }

    @Override
    public void put(File file, String resourceName) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        System.out.println(" -----> put(" + file + ", " + resourceName + ")");
        throw new ResourceDoesNotExistException("Not implemented");
    }

    @Override
    public void putDirectory(File folder, String resourceFolderName) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        System.out.println(" -----> putDirectory(" + folder + ", " + resourceFolderName + ")");
        throw new ResourceDoesNotExistException("Not implemented");
    }

    @Override
    public boolean resourceExists(String resourceName) throws TransferFailedException, AuthorizationException {
        System.out.println(" ---------> resourceExists(" + resourceName + ")");
        try {
            String url = gitHubHelper.findResourceURL(resourceName);
            return url != null;
        } catch (IOException e) {
            throw new TransferFailedException("Sorry... ", e);
        }
    }

    @Override
    public boolean supportsDirectoryCopy() {
        return false;
    }

}
