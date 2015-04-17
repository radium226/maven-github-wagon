#!/bin/bash

cd "maven-github-wagon"
mvn clean install
cd -

cd "dummy"
mvn -X -s settings.xml -U clean package
cd -
