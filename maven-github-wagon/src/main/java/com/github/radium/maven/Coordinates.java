package com.github.radium.maven;

import java.io.File;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class Coordinates {

    final public static String SEPARATOR = "/";

    private String groupID;
    private String artifactID;
    private String packaging;
    private String classifier;
    private String version;
    
    public Coordinates(String groupID, String artifactID, String packaging, String classifier, String version) {
        super();

        this.groupID = groupID;
        this.artifactID = artifactID;
        this.packaging = packaging;
        this.classifier = classifier;
        this.version = version;
    }

    public String getGroupID() {
        return this.groupID;
    }

    public String getArtifactID() {
        return this.artifactID;
    }

    public String getPackaging() {
        return this.packaging;
    }

    public String getClassifier() {
        return this.classifier;
    }

    public String getVersion() {
        return this.version;
    }

    @Override
    public boolean equals(Object object) {
        boolean equal = false;
        if (object instanceof Coordinates) {
            Coordinates that = (Coordinates) object;
            equal = Objects.equal(this.groupID, that.groupID) && Objects.equal(this.artifactID, that.artifactID) && Objects.equal(this.packaging, that.packaging) && Objects.equal(this.classifier, that.classifier) && Objects.equal(this.version, that.version);
        }
        return equal;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Coordinates.class)
                .add("groupID", this.groupID)
                .add("artifactID", this.artifactID)
                .add("packaging", this.packaging)
                .add("classifier", this.classifier)
                .add("version", this.version)
            .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.groupID, this.artifactID, this.packaging, this.classifier, this.version);
    }

    public static Coordinates fromText(String coordinatesAsText) {
        List<String> parts = Splitter.on(":").splitToList(coordinatesAsText);
        int partCount = parts.size();
        
        Coordinates coordinates = null; 
        switch(partCount) {
        case 3:
            coordinates = new Coordinates(parts.get(0), parts.get(1), "jar", null, parts.get(2));
            break;
        default:
            throw new UnsupportedOperationException("Sorry :(");   
        }
        return coordinates;
    }
    
    public static Coordinates of(String resourceName) {
        resourceName = resourceName.replace(File.separator, SEPARATOR); // Just to be sure

        String fileName = resourceName.substring(resourceName.lastIndexOf(SEPARATOR) + 1); //, filePath.lastIndexOf("."));
        String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf("."));
        
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
        String directoryPath = resourceName.substring(0, resourceName.length() - fileName.length() - 1);
        List<String> directoryNames = Splitter.on(SEPARATOR).splitToList(directoryPath);
        int directoryCount = directoryNames.size();

        String version = directoryNames.get(directoryCount - 1);
        String artifactID = directoryNames.get(directoryCount - 2);
        String groupID = Joiner.on(".").join(directoryNames.subList(0, directoryCount - 2));
        String packaging = fileExtension;
        String classifier = fileNameWithoutExtension.substring(artifactID.length() + 1 + version.length());
        if (!Strings.isNullOrEmpty(classifier)) {
            classifier = classifier.substring(1);
        } else {
            classifier = null;
        }

        return new Coordinates(groupID, artifactID, packaging, classifier, version);
    }

    public String toText() {
        StringBuilder text = new StringBuilder();
        text
                .append(this.groupID)
                .append(":")
                .append(this.artifactID)
                .append(":")
                .append(this.packaging)
                .append(":");
        if (this.classifier != null) {
            text
                    .append(this.classifier)
                    .append(":");
        }
        text.append(this.version);
        return text.toString();
    }

    public List<Node> toXML(Document document, boolean asDependency) {
        List<Node> nodes = Lists.newArrayList();
        if (!asDependency) {
            Element groupIDElement = document.createElement("groupId");
            groupIDElement.appendChild(document.createTextNode(this.groupID));
            nodes.add(groupIDElement);

            Element artifactIDElement = document.createElement("artifactId");
            artifactIDElement.appendChild(document.createTextNode(this.artifactID));
            nodes.add(artifactIDElement);

            if (this.packaging != null) {
                Element packagingElement = document.createElement("packaging");
                packagingElement.appendChild(document.createTextNode(this.packaging));
                nodes.add(packagingElement);
            }

            if (this.classifier != null) {
                Element classifierElement = document.createElement("classifier");
                classifierElement.appendChild(document.createTextNode(this.classifier));
                nodes.add(classifierElement);
            }

            Element versionElement = document.createElement("version");
            versionElement.appendChild(document.createTextNode(this.version));
            nodes.add(versionElement);
        } else {
            Element dependencyElement = document.createElement("dependency");
            for (Node node : toXML(document, false)) {
                dependencyElement.appendChild(node);
            }
            nodes.add(dependencyElement);
        }
        return nodes;
    }
    
    public Coordinates withPackaging(String packaging) {
        return new Coordinates(groupID, artifactID, packaging, classifier, version);
    }

}
