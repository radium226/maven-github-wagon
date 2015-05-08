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

import java.net.InetSocketAddress;
import java.net.Proxy;
import org.apache.maven.wagon.proxy.ProxyInfo;

public final class ProxyInfos {

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
