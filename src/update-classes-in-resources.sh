#!/usr/bin/env bash

rm -rf src/main/resources/stamp/
mkdir --parent src/main/resources/runner-classes/eu/stamp/runner/test/
mkdir --parent src/main/resources/runner-classes/eu/stamp/runner/coverage/
cp -r target/classes/eu/stamp/runner/test/*.class src/main/resources/runner-classes/eu/stamp/runner/test/
cp -r target/classes/eu/stamp/runner/coverage/*.class src/main/resources/runner-classes/eu/stamp/runner/coverage/