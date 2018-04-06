#!/usr/bin/env bash

mvn clean compile

rm -rf src/main/resources/runner-classes/
mkdir --parent src/main/resources/runner-classes/eu/stamp/project/testrunner/runner/test/
mkdir --parent src/main/resources/runner-classes/eu/stamp/project/testrunner/runner/coverage/
cp -r target/classes/eu/stamp/project/testrunner/runner/test/*.class src/main/resources/runner-classes/eu/stamp/project/testrunner/runner/test/
cp -r target/classes/eu/stamp/project/testrunner/runner/coverage/*.class src/main/resources/runner-classes/eu/stamp/project/testrunner/runner/coverage/