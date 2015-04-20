#!/bin/bash

cd "maven-github-wagon"
mvn clean install
cd -

trash "${HOME}/.m2/repository/radium/executable"

cd "dummy"
mvn $1 -s settings.xml -U versions:display-dependency-updates #clean package
cd -
