# Maven Wagon for GitHub

## Purpose
This project can be used in order to use the GitHub Release functionnality as a Maven repository. It's also compatible with the `mvn versions:display-dependency-updates`. 

## Installation
```
cd "$( mktemp --directory )"
wget "https://github.com/radium226/maven-github-wagon/archive/0.1.tar.gz"
tar -xf "0.1.tar.gz"
mvn --file="maven-github-wagon-0.1/pom.xml" install
cd -
```

## Usage
You should add in the `pom.xml` file:
```
...
<repositories>
    <repository>
        <id>github</id>
        <url>github://.m2/repository</url> <!-- Only needed to trigger the wagon: we need to put something because an empty text after github:// causes a NullPointerException -->
        <releases>
            <enabled>true</enabled>
            <checksumPolicy>ignore</checksumPolicy> <!-- We can extract it from the Release API but only for the JAR (not for the pom.xml) so let's ignore it for the moment -->
        </releases>
        <snapshots>
            <enabled>true</enabled>
            <checksumPolicy>ignore</checksumPolicy> <!-- Ditto -->
        </snapshots>
    </repository>
</repositories>
...
<build>
    <extensions>
        <extension>
            <groupId>com.github.radium226</groupId>
            <artifactId>maven-github-wagon</artifactId>
            <version>0.1</version>
        </extension>
    </extensions>
</build>
...
```

And in your `settings.xml` file:
```
<servers>
  <server>
    <id>github</id>
    <username>abcd</username>
    <password>1234</password>
    <!-- <password>oauth2_token:0123456789abcdef</password> You can set an OAuth2 token -->
  </server>
</servers>
```

# Limitations
The `groupId` of the artifact should be `com.github.<GitHub User>` and the `ærtifactId` should be the same as the GitHub repository name. The `version` should be the same as the GitHub release tag.
