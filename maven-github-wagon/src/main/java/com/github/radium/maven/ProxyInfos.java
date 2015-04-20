package com.github.radium.maven;

import java.net.InetSocketAddress;
import java.net.Proxy;
import org.apache.maven.wagon.proxy.ProxyInfo;

final public class ProxyInfos {

    private ProxyInfos() {
        super();
    }

    public static Proxy asProxy(ProxyInfo proxyInfo) {
        String proxyHost = proxyInfo.getHost();
        int proxyPort = proxyInfo.getPort();
        Proxy proxy = new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(proxyHost, proxyPort));
        return proxy;
    }

}
