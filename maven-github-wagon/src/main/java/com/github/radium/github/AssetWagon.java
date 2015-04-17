package com.github.radium.github;

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

@Component(role = Wagon.class, hint = "github", instantiationStrategy = "per-lookup")
public class AssetWagon extends AbstractWagon {

	final private static Logger LOGGER = LoggerFactory.getLogger(AssetWagon.class);
	
	private GitHub gitHub;
	private OkHttpClient okHttpClient;
	
	public AssetWagon() {
		super();
	}
	
	@Override
	public void doConnect(Optional<AuthenticationInfo> authenticationInfo) throws ConnectionException {
		try {
			// GitHub Authentication
			GitHubBuilder gitHubBuilder = new GitHubBuilder();
			if (authenticationInfo.isPresent()) {
				String login = authenticationInfo.get().getUserName();
				String password = authenticationInfo.get().getPassword();
				gitHubBuilder = gitHubBuilder.withPassword(login, password);
			}
			
			// Proxy Authentication
			okHttpClient = new OkHttpClient();
			final org.apache.maven.settings.Proxy settingsProxy = getProxyByProtocol("http");
			if (settingsProxy != null) {
				String proxyHost = settingsProxy.getHost();
				int proxyPort = settingsProxy.getPort();
				LOGGER.info("Using {} as HTTP proxy", proxyHost);
				Proxy proxy = new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(proxyHost, proxyPort));
				okHttpClient.setProxy(proxy);
				if (settingsProxy.getUsername() != null) {
					okHttpClient.setAuthenticator(new Authenticator() {
						
						@Override
						public Request authenticateProxy(Proxy proxy, Response response) {
							return response.request();
						}
						
						@Override
						public Request authenticate(Proxy proxy, Response response) throws IOException {
							String credentials = Credentials.basic(settingsProxy.getUsername(), settingsProxy.getPassword());
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
		try {
			Coordinates coordinates = Coordinates.of(resourceName);
			GHRepository repository = getRepositoryByCoordinates(coordinates);
			System.out.println("{}{}{}{}{}{}{}{}{}{}{}{}{}{}{}{}{}{}{}{}{}{} repository = " + repository);
			
			PagedIterable<GHRelease> releases = repository.listReleases();
			for (GHRelease release : releases) {
				System.out.println("TAGNAME = " + release.getTagName() + " / " + coordinates.getArtifactID() + "-" + coordinates.getVersion());
				if (release.getTagName().equals(coordinates.getArtifactID() + "-" + coordinates.getVersion()) || release.getTagName().equals(coordinates.getVersion())) {
					System.out.println("++++++++ resourceName = " + resourceName);
					if (resourceName.endsWith(".pom")) {
						System.out.println("YYYYYYYYYYYYYYYYYYYYAAAAAAAAAAAAAAAAAAAAAAAAAYYYYYYYYYYYYYYYYYYYYYYYYYYY");
					} else if (resourceName.endsWith(".jar")) {
						List<GHAsset> assets = release.getAssets();
						for (GHAsset asset : assets) {
							if (resourceName.endsWith(asset.getName())) {
								Request request = new Request.Builder()
										.url(asset.getBrowserDownloadUrl())
									.build();
								Response response = okHttpClient.newCall(request).execute();
								InputStream bodyInputStream = response.body().byteStream();
								FileOutputStream resourceFileOutputStream = new FileOutputStream(resourceFile);
								ByteStreams.copy(bodyInputStream, resourceFileOutputStream);
								resourceFileOutputStream.close();
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
		System.out.println(" -----> get(" + resourceName + ", " + resourceFile + ")");
		throw new ResourceDoesNotExistException("Not implemented");
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
		System.out.println(" -----> resourceExists(" + resourceName + ")");
		return false;
	}

	@Override
	public boolean supportsDirectoryCopy() {
		System.out.println(" -----> supportsDirectoryCopy()");
		return false;
	}
	
	public Map<Coordinates, GHRepository> getMappings() {
		Map<Coordinates, GHRepository> mappings = Maps.newHashMap();
		try {
			List<String> mappingsAsText = Splitter.on(",").splitToList(URLDecoder.decode(getRepository().getUrl(), "UTF-8").replace("github://?map=", "")); // TODO MY EYES BUUUUURN
			System.out.println("mappingsAsText=" + mappingsAsText);
			for (String mappingAsText : mappingsAsText) {
				try {
					List<String> parts = Splitter.on("|").splitToList(mappingAsText);
					System.out.println("parts = " + parts);
					Coordinates coordinates = Coordinates.fromText(parts.get(0));
					System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ parts.get(1) = " + parts.get(1));
					GHRepository repository = gitHub.getRepository(parts.get(1));
					mappings.put(coordinates, repository);
				} catch (IOException e) {
					LOGGER.warn("Unable to parse repository URL", e);
				}
			}
			
		} catch (IllegalArgumentException e) {
			LOGGER.warn("Unable to parse repository URL", e);
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn("Unable to parse repository URL", e);
		}
		return mappings;
	}
	
	public GHRepository getRepositoryByCoordinates(Coordinates coordinates) throws IOException {
		Map<Coordinates, GHRepository> mappings = getMappings();
		GHRepository repository = null; 
		if (mappings.containsKey(coordinates)) {
			repository = mappings.get(coordinates);
		} else {
			String groupID = coordinates.getGroupID();
			if (groupID.startsWith("com.github")) {
				groupID = groupID.substring("com.github".length());
			}
			
			String artifactID = coordinates.getArtifactID();
			
			String repositoryName = groupID + "/" + artifactID;
			System.out.println(" =================> Trying " + repositoryName);
			repository = gitHub.getRepository(repositoryName);
		}
		return repository; 
	}

}
