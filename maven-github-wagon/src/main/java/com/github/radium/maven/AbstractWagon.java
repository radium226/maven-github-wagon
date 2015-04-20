package com.github.radium.maven;

import com.google.common.base.Function;
import java.io.File;
import java.util.List;

import org.apache.maven.settings.Proxy;
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
import org.codehaus.plexus.component.annotations.Requirement;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.maven.execution.MavenSession;

public abstract class AbstractWagon implements Wagon {

    private SessionListenerList sessionListeners;
    private TransferListenerList transferListeners;

    private boolean interactive;
    private Optional<Integer> readTimeout;
    private Optional<Integer> timeout;
    private Repository repository;

    @Requirement
    private MavenSession session;

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
        System.out.println("connect(Repository repository)");
        connect(repository, Optional.<AuthenticationInfo>absent());
    }

    @Override
    public void connect(Repository repository, ProxyInfo proxyInfo) throws ConnectionException, AuthenticationException {
        System.out.println("connect(Repository repository, ProxyInfo proxyInfo)");
        connect(repository, Optional.<AuthenticationInfo>absent());
    }

    @Override
    public void connect(Repository repository, ProxyInfoProvider proxyInfoProvider) throws ConnectionException, AuthenticationException {
        System.out.println("connect(Repository repository, ProxyInfoProvider proxyInfoProvider)");
        connect(repository, Optional.<AuthenticationInfo>absent());
    }

    @Override
    public void connect(Repository repository, AuthenticationInfo authenticationInfo) throws ConnectionException, AuthenticationException {
        System.out.println("connect(Repository repository, AuthenticationInfo authenticationInfo)");
        connect(repository, Optional.fromNullable(authenticationInfo));
    }

    @Override
    public void connect(Repository repository, AuthenticationInfo authenticationInfo, ProxyInfo proxyInfo) throws ConnectionException, AuthenticationException {
        System.out.println("connect(Repository repository, AuthenticationInfo authenticationInfo, ProxyInfo proxyInfo)");
        connect(repository, Optional.fromNullable(authenticationInfo));
    }

    public void connect(Repository repository, Optional<AuthenticationInfo> authenticationInfo) throws ConnectionException, AuthenticationException {
        System.out.println("connect(Repository repository, Optional<AuthenticationInfo> authenticationInfo, Optional<ProxyInfo> proxyInfo)");
        this.repository = repository;
        connect(authenticationInfo);
    }

    @Override
    public void connect(Repository repository, AuthenticationInfo authenticationInfo, ProxyInfoProvider proxyInfoProvider) throws ConnectionException, AuthenticationException {
        System.out.println("connect(Repository repository, AuthenticationInfo authenticationInfo, ProxyInfoProvider proxyInfoProvider)");
        System.out.println(" =====> proxyInfoProvider = " + proxyInfoProvider);
        Optional<ProxyInfo> proxyInfo = Optional.fromNullable(proxyInfoProvider == null ? null : proxyInfoProvider.getProxyInfo("github"));
        connect(repository, Optional.fromNullable(authenticationInfo));
    }

    public void connect(Optional<AuthenticationInfo> authenticationInfo) throws ConnectionException, AuthenticationException {
        sessionListeners.fireSessionOpening();
        try {
            doConnect(authenticationInfo);
            sessionListeners.fireSessionLoggedIn();
            sessionListeners.fireSessionOpened();
        } catch (AuthenticationException e) {
            sessionListeners.fireSessionConnectionRefused();
            throw e;
        } catch (ConnectionException e) {
            sessionListeners.fireSessionConnectionRefused();
            throw e;
        }
    }

    protected abstract void doConnect(Optional<AuthenticationInfo> authenticationInfo) throws ConnectionException, AuthenticationException;

    @Override
    public void disconnect() throws ConnectionException {
        sessionListeners.fireSessionDisconnecting();
        try {
            doDisconnect();
            sessionListeners.fireSessionLoggedOff();
            sessionListeners.fireSessionDisconnected();
        } catch (ConnectionException e) {
            sessionListeners.fireSessionConnectionRefused();
            throw e;
        }

    }

    public abstract void doDisconnect() throws ConnectionException;

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

    @Deprecated
    @Override
    public void openConnection() throws ConnectionException, AuthenticationException {

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
        this.readTimeout = Optional.fromNullable(readTimeout);
    }

    @Override
    public void setTimeout(int timeout) {
        this.timeout = Optional.fromNullable(timeout);
    }

    @Override
    public abstract boolean supportsDirectoryCopy();

    public List<Proxy> getProxies() {
        return session.getSettings().getProxies();
    }

    public List<ProxyInfo> getProxyInfos() {
        return Lists.transform(getProxies(), new Function<Proxy, ProxyInfo>() {

            @Override
            public ProxyInfo apply(Proxy proxy) {
                return toProxyInfo(proxy);
            }

        });
    }

    public ProxyInfo getProxyInfoByType(final String type) {
        return Iterables.getFirst(Iterables.filter(getProxyInfos(), new Predicate<ProxyInfo>() {

            @Override
            public boolean apply(ProxyInfo proxyInfo) {
                return proxyInfo.getType().equals(type);
            }
            
        }), null);
    }

    public static ProxyInfo toProxyInfo(Proxy proxy) {
        ProxyInfo proxyInfo = new ProxyInfo();
        proxyInfo.setHost(proxy.getHost());
        proxyInfo.setNonProxyHosts(proxy.getNonProxyHosts());
        proxyInfo.setPassword(proxy.getPassword());
        proxyInfo.setPort(proxy.getPort());
        proxyInfo.setType(proxy.getProtocol());
        proxyInfo.setUserName(proxy.getUsername());
        return proxyInfo;
    }

    public Proxy getProxyByProtocol(final String protocol) {
        return Iterables.getFirst(Iterables.filter(getProxies(), new Predicate<Proxy>() {

            @Override
            public boolean apply(Proxy proxy) {
                return proxy.getProtocol().equals(protocol);
            }

        }), null);
    }
    
    public MavenSession getSession() {
        return session;
    }

}
