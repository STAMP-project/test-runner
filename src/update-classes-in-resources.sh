#!/usr/bin/env bash

mvn clean compile

rm -rf src/main/resources/runner-classes/
mkdir --parent src/main/resources/runner-classes/eu/stamp_project/testrunner/runner/test/
mkdir --parent src/main/resources/runner-classes/eu/stamp_project/testrunner/runner/coverage/
mkdir --parent src/main/resources/runner-classes/eu/stamp_project/testrunner/runner/junit5/
cp -r target/classes/eu/stamp_project/testrunner/runner/test/*.class src/main/resources/runner-classes/eu/stamp_project/testrunner/runner/test/
cp -r target/classes/eu/stamp_project/testrunner/runner/coverage/*.class src/main/resources/runner-classes/eu/stamp_project/testrunner/runner/coverage/
cp -r target/classes/eu/stamp_project/testrunner/runner/junit5/*.class src/main/resources/runner-classes/eu/stamp_project/testrunner/runner/junit5/
cp -r target/classes/eu/stamp_project/testrunner/TestListener.class src/main/resources/runner-classes/eu/stamp_project/testrunner/
cp -r target/classes/eu/stamp_project/testrunner/TestListenerImpl.class src/main/resources/runner-classes/eu/stamp_project/testrunner/