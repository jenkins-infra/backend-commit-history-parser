#!/bin/bash

# TODO consider deleting in favor of binaries automatically deployed to https://repo.jenkins-ci.org/incrementals/org/jenkins-ci/main/jenkins-war/

set -ex

: ${PACKAGING_DIR="../packaging"}

if ! git log '@{u}..' --exit-code; then
  echo "There are changes not pushed" >&2
  git log '@{u}..' >&2
  exit 1
fi

exit 1

# Build
MAVEN_OPTS="-XX:MaxPermSize=512M" mvn -e -Dbuild.type=rc -Prelease -Plts-release clean deploy -Dconcurrency=2 -DskipTests=true
WAR_FILE=$(pwd)/war/target/jenkins.war

# Push the bits
pushd $PACKAGING_DIR
make WAR=${WAR_FILE} BRAND=./branding/jenkins-stable-rc.mk BUILDENV=./env/release.mk war.publish
popd
