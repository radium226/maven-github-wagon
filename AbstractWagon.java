package com.github.radium.maven;

import java.io.File;
import java.util.List;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.events.SessionListener;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.proxy.ProxyInfoProvider;
import org.apache.maven.wagon.repository.Repository;

import com.google.common.base.Optional;

public abstract class AbstractWagon implements Wagon {
	
	private SessionListenerList sessionListeners;
	private TransferListenerList transferListeners;
	
	private boolean interactive;
	private Optional<Integer> readTimeout;
	private Optional<Integer> timeout;
	private Repository repository;
	
	protected AbstractWagon() {
		super();
		
		this.sessionListeners = SessionListenerList.create(this);
		this.transferListeners = TransferListenerList.create(this);
	}
	
	@Override
	public void addSessionListener(SessionListener sessionListener) {
		sessionListeners.add(sessionListener);
	}

	@Override
	public void addTransferListener(TransferListener transferListener) {
		transferListeners.add(transferListener);
	}

	@Override
	public void connect(Repository repository) throws ConnectionException, AuthenticationException {
		connect(repository, Optional.<AuthenticationInfo>absent(), Optional.<ProxyInfo>absent());
	}

	@Override
	public void connect(Repository repository, ProxyInfo proxyInfo) throws ConnectionException, AuthenticationException {
		connect(repository, Optional.<AuthenticationInfo>absent(), Optional.of(proxyInfo));
	}

	@Override
	public void connect(Repository repository, ProxyInfoProvider proxyInfoProvider) throws ConnectionException, AuthenticationException {
		connect(repository, Optional.<AuthenticationInfo>absent(), Optional.<ProxyInfo>fromNullable(proxyInfoProvider.getProxyInfo("http")));
	}

	@Override
	public void connect(Repository repository, AuthenticationInfo authenticationInfo) throws ConnectionException, AuthenticationException {
		connect(repository, Optional.of(authenticationInfo), Optional.<ProxyInfo>absent());
	}

	@Override
	public void connect(Repository repository, AuthenticationInfo authenticationInfo, ProxyInfo proxyInfo) throws ConnectionException, AuthenticationException {
		connect(repository, Optional.of(authenticationInfo), Optional.of(proxyInfo));
	}
	
	public void connect(Repository repository, Optional<AuthenticationInfo> authenticationInfo, Optional<ProxyInfo> proxyInfo) throws ConnectionException, AuthenticationException {
		this.repository = repository;
		connect(authenticationInfo, proxyInfo);
	}

	@Override
	public void connect(Repository repository, AuthenticationInfo authenticationInfo, ProxyInfoProvider proxyInfoProvider) throws ConnectionException, AuthenticationException {
		connect(repository, Optional.of(authenticationInfo), Optional.fromNullable(proxyInfoProvider.getProxyInfo("http")));
	}
	
	public abstract void connect(Optional<AuthenticationInfo> authenticationInfo, Optional<ProxyInfo> proxyInfo);

	@Override
	public abstract void disconnect() throws ConnectionException;

	@Override
	public abstract void get(String arg0, File arg1) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException;

	@Override
	public abstract List<String> getFileList(String arg0) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException;

	@Override
	public abstract boolean getIfNewer(String arg0, File arg1, long arg2) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException;

	@Override
	public int getReadTimeout() {
		return readTimeout.or(DEFAULT_READ_TIMEOUT);
	}

	@Override
	public Repository getRepository() {
		return repository;
	}

	@Override
	public int getTimeout() {
		return timeout.or(DEFAULT_CONNECTION_TIMEOUT);
	}

	@Override
	public boolean hasSessionListener(SessionListener sessionListener) {
		return sessionListeners.contains(sessionListener);
	}

	@Override
	public boolean hasTransferListener(TransferListener transferListener) {
		return transferListeners.contains(transferListener);
	}

	@Override
	public boolean isInteractive() {
		return interactive;
	}

	@Override
	public void openConnection() throws ConnectionException, AuthenticationException {
		connect(Optional.<AuthenticationInfo>absent(), Optional.<ProxyInfo>absent());
	}

	@Override
	public abstract void put(File file, String resourceName) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException;

	@Override
	public abstract void putDirectory(File arg0, String arg1) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException;

	@Override
	public void removeSessionListener(SessionListener sessionListener) {
		sessionListeners.remove(sessionListener);
	}

	@Override
	public void removeTransferListener(TransferListener transferListener) {
		transferListeners.remove(transferListener);
	}

	@Override
	public abstract boolean resourceExists(String arg0) throws TransferFailedException, AuthorizationException;

	@Override
	public void setInteractive(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public void setReadTimeout(int readTimeout) {
		this.readTimeout = Optional.of(readTimeout);
	}

	@Override
	public void setTimeout(int timeout) {
		this.timeout = Optional.of(timeout);
	}

	@Override
	public abstract boolean supportsDirectoryCopy();

}
