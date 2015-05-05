package com.github.radium226.github.maven;

import com.google.common.collect.ImmutableList;
import com.google.inject.internal.util.Classes;
import java.util.List;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Downloaders {

    private static List<Downloader> INSTANCES = ImmutableList.of(new JarDownloader(), new PomDownloader(), new MetaDataDownloader());
    private static final Logger LOGGER = LoggerFactory.getLogger(Downloaders.class);
    
    private Downloaders() {
        super();
    }
    
    public static Downloader of(String resourceName) throws ResourceDoesNotExistException {
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
