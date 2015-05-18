#!/bin/bash
#
#    Copyright 2015 Radium226
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
#    limitations under the License.
#


export MAVEN="$( which 'mvn' )"
export MAVEN_OPTIONS='--batch-mode --settings "settings.xml" --update-snapshots'
export GITHUB_OAUTH2_TOKEN="62348150912e4f48c4216e141fa2a39dea046636"
set -e
declare version="${1}"
declare skip_test="${2}"
if [[ "${skip_test}" = "false" ]]; then
    cd "src/test/dummy"
    sed "s,{{VERSION}},${version},g" "pom.xml-TEMPLATE" >"pom.xml"
    ${MAVEN} ${MAVEN_OPTIONS} clean package
    java -jar "target/$( ls -1 "target" | grep -E 'dependencies\.jar$' )"
    ${MAVEN} ${MAVEN_OPTIONS} versions:display-dependency-updates
    cd "-"
fi
exit 0