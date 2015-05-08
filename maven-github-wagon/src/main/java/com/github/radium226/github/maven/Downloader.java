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
package com.github.radium226.github.maven;

import com.google.common.collect.ImmutableList;
import java.io.InputStream;
import java.util.List;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Downloader {

    public static List<Downloader> INSTANCES = ImmutableList.of(new JarDownloader(), new PomDownloader(), new MetaDataDownloader());
    public static final Logger LOGGER = LoggerFactory.getLogger(Downloader.class);
    
    public Downloader with(GitHubService gitHubHelper);

    public InputStream download(String resourceName) throws TransferFailedException, ResourceDoesNotExistException;

    public boolean accept(String resourceName);
    
    public static Downloader forResource(String resourceName) throws ResourceDoesNotExistException {
        Downloader downloader = null;
        for (Downloader instance : INSTANCES) {
            if (instance.accept(resourceName)) {
                downloader = instance;
                break;
            }
        }

        if (downloader == null) {
            throw new ResourceDoesNotExistException("Nothing was found to download " + resourceName);
        }

        LOGGER.debug("Using {} for {}", downloader.getClass().getName(), resourceName);
        return downloader;
    }

}
