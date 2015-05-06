package com.github.radium226.github.maven;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.util.List;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.codehaus.plexus.component.annotations.Component;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.extras.OkHttpConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.radium226.maven.AbstractWagon;
import com.github.radium226.maven.ProxyInfos;
import com.google.common.base.Optional;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.apache.maven.wagon.proxy.ProxyInfo;

@Component(role = Wagon.class, hint = "github", instantiationStrategy = "per-lookup")
public class AssetWagon extends AbstractWagon {

    final private static Logger LOGGER = LoggerFactory.getLogger(AssetWagon.class);

    private GitHubService gitHubService;
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

    @Override
    public void doConnect(Optional<AuthenticationInfo> authenticationInfo) throws ConnectionException {
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

            this.gitHubService = GitHubService.forGitHub(gitHub)
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
    public InputStream doGet(String resourceName) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {

        if (!resourceName.startsWith("com/github/")) {
            throw new ResourceDoesNotExistException("There is no " + resourceName + " on GitHub");
        }

        return Downloaders.of(resourceName).with(this.gitHubService).download(resourceName);
    }

    @Override
    public List<String> getFileList(String arg0) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getIfNewer(String resourceName, File resourceFile, long timestamp) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(File file, String resourceName) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putDirectory(File folder, String resourceFolderName) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsDirectoryCopy() {
        return false;
    }

}
