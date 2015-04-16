package com.github.radium.github;

import java.io.File;
import java.util.List;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.codehaus.plexus.component.annotations.Component;

import com.github.radium.maven.AbstractWagon;
import com.google.common.base.Optional;

@Component(role = Wagon.class, hint = "github")
public class AssetWagon extends AbstractWagon {

	private boolean connected = false;
	
	public AssetWagon() {
		super();
		System.out.println(" =====> Construct");
	}
	
	@Override
	public void doConnect(Optional<AuthenticationInfo> authenticationInfo, Optional<ProxyInfo> proxyInfo) {
		System.out.println(" -----> connect! ");
		this.connected = true;
	}

	@Override
	public void doDisconnect() throws ConnectionException {
		System.out.println(" -----> disconnect! (connected=" + connected + ")");
		this.connected = false;
	}

	@Override
	public void get(String resourceName, File file) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
		System.out.println(" -----> get(" + resourceName + ", " + file + ")");
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

	

}
