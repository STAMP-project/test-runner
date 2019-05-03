#!/usr/bin/env bash

mvn clean compile

rm -rf src/main/resources/runner-classes/

mkdir --parent src/main/resources/runner-classes/eu/stamp_project/testrunner/listener/
mkdir --parent src/main/resources/runner-classes/eu/stamp_project/testrunner/utils/
mkdir --parent src/main/resources/runner-classes/eu/stamp_project/testrunner/listener/impl
mkdir --parent src/main/resources/runner-classes/eu/stamp_project/testrunner/listener/junit4
mkdir --parent src/main/resources/runner-classes/eu/stamp_project/testrunner/listener/junit5
mkdir --parent src/main/resources/runner-classes/eu/stamp_project/testrunner/listener/pit
mkdir --parent src/main/resources/runner-classes/eu/stamp_project/testrunner/runner/
mkdir --parent src/main/resources/runner-classes/eu/stamp_project/testrunner/runner/coverage/

cp -r target/classes/eu/stamp_project/testrunner/utils/*.class src/main/resources/runner-classes/eu/stamp_project/testrunner/utils/
cp -r target/classes/eu/stamp_project/testrunner/listener/*.class src/main/resources/runner-classes/eu/stamp_project/testrunner/listener/
cp -r target/classes/eu/stamp_project/testrunner/listener/impl/*.class src/main/resources/runner-classes/eu/stamp_project/testrunner/listener/impl/
cp -r target/classes/eu/stamp_project/testrunner/listener/junit4/*.class src/main/resources/runner-classes/eu/stamp_project/testrunner/listener/junit4/
cp -r target/classes/eu/stamp_project/testrunner/listener/junit5/*.class src/main/resources/runner-classes/eu/stamp_project/testrunner/listener/junit5/
cp -r target/classes/eu/stamp_project/testrunner/listener/pit/*.class src/main/resources/runner-classes/eu/stamp_project/testrunner/listener/pit/
cp -r target/classes/eu/stamp_project/testrunner/runner/*.class src/main/resources/runner-classes/eu/stamp_project/testrunner/runner/
cp -r target/classes/eu/stamp_project/testrunner/runner/coverage/*.class src/main/resources/runner-classes/eu/stamp_project/testrunner/runner/coverage/

mvn clean compile