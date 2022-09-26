#!/bin/bash
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
REPO_ROOT=$SCRIPT_DIR/../../

cd $REPO_ROOT/eclipse
docker run --rm -v "$(pwd)":/usr/src -w /usr/src maven:3.6-jdk-8 mvn clean verify
