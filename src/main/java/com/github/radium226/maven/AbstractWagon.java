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
package com.github.radium226.maven;

import com.github.radium226.io.ListenableFileOutputStream;
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
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.wagon.events.TransferEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractWagon implements Wagon {

    private SessionListenerList sessionListeners;
    private TransferListenerList transferListeners;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWagon.class);

    private boolean interactive;
    private Optional<Integer> readTimeout;
    private Optional<Integer> timeout;
    private Repository repository;

    /*@Requirement
     private MavenSession session;*/
    @Requirement
    private LegacySupport legacySupport;

    protected AbstractWagon() {
        super();

        this.sessionListeners = SessionListenerList.create(this);
        this.transferListeners = TransferListenerList.create(this);
    }

    protected TransferListenerList getTransferListeners() {
        return transferListeners;
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
        connect(repository, Optional.<AuthenticationInfo>absent());
    }

    @Override
    public void connect(Repository repository, ProxyInfo proxyInfo) throws ConnectionException, AuthenticationException {
        connect(repository, Optional.<AuthenticationInfo>absent());
    }

    @Override
    public void connect(Repository repository, ProxyInfoProvider proxyInfoProvider) throws ConnectionException, AuthenticationException {
        connect(repository, Optional.<AuthenticationInfo>absent());
    }

    @Override
    public void connect(Repository repository, AuthenticationInfo authenticationInfo) throws ConnectionException, AuthenticationException {
        connect(repository, Optional.fromNullable(authenticationInfo));
    }

    @Override
    public void connect(Repository repository, AuthenticationInfo authenticationInfo, ProxyInfo proxyInfo) throws ConnectionException, AuthenticationException {
        connect(repository, Optional.fromNullable(authenticationInfo));
    }

    public void connect(Repository repository, Optional<AuthenticationInfo> authenticationInfo) throws ConnectionException, AuthenticationException {
        this.repository = repository;
        connect(authenticationInfo);
    }

    @Override
    public void connect(Repository repository, AuthenticationInfo authenticationInfo, ProxyInfoProvider proxyInfoProvider) throws ConnectionException, AuthenticationException {
        Optional<ProxyInfo> proxyInfo = Optional.fromNullable(proxyInfoProvider == null ? null : proxyInfoProvider.getProxyInfo("github"));
        connect(repository, Optional.fromNullable(authenticationInfo));
    }

    public void connect(Optional<AuthenticationInfo> authenticationInfo) throws ConnectionException, AuthenticationException {
        sessionListeners.fireSessionOpening();
        try {
            doConnect(authenticationInfo);
            sessionListeners.fireSessionLoggedIn();
            sessionListeners.fireSessionOpened();
        } catch (AuthenticationException | ConnectionException e) {
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
    public void get(String resourceName, File resourceFile) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        transferListeners.fireTransferInitiated(resourceName, TransferEvent.REQUEST_GET);
        try (
                ListenableFileOutputStream fileOutputStream = new ListenableFileOutputStream(resourceFile);
                InputStream resouceInputStream = doGet(resourceName);) {
            resourceFile.getParentFile().mkdirs();
            transferListeners.fireTransferStarted(resourceName, TransferEvent.REQUEST_GET);
            fileOutputStream.addListener((File file, byte[] buffer, int length) -> {
                transferListeners.fireTransferProgress(resourceName, TransferEvent.REQUEST_GET, buffer, length);
            });
            ByteStreams.copy(resouceInputStream, fileOutputStream);
            transferListeners.fireTransferCompleted(resourceName, TransferEvent.REQUEST_GET);
        } catch (IOException e) {
            transferListeners.fireTransferError(resourceName, TransferEvent.REQUEST_GET);
            LOGGER.debug("Unable to transfer resource {}", resourceName, e);
        }
    }

    protected abstract InputStream doGet(String resourceName) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException;

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
        return getSession().getSettings().getProxies();
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
        return legacySupport.getSession();
    }

    @Override
    public boolean resourceExists(String resourceName) throws TransferFailedException, AuthorizationException {
        boolean exists;
        try {
            doGet(resourceName);
            exists = true;
        } catch (ResourceDoesNotExistException e) {
            exists = false;
        }
        return exists;
    }

}
