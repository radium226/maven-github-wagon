# Maven Wagon for GitHub

## Purpose
This wagon allow the use of project release on GitHub as depdendency. 

## Installation
```
wget "https://github.com/radium226/maven-github-wagon/releases/download/0.1/maven-github-wagon-0.1.jar" && mvn install:install-file -Dfile="maven-github-wagon-0.1.jar"
```

## Usage
You should add in the `pom.xml` file:
```
...
<repositories>
    <repository>
        <id>github</id>
        <url>github://.m2/repository</url> <!-- Only needed to trigger the wagon: we need to put something because an empty text causes a NullPointerException -->
        <releases>
            <enabled>true</enabled>
            <checksumPolicy>ignore</checksumPolicy>
        </releases>
        <snapshots>
            <enabled>true</enabled>
            <checksumPolicy>ignore</checksumPolicy>
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
The GitHub repository must be closely related to the `groupId`
