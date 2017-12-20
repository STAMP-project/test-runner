#!/usr/bin/env bash

rm -rf src/main/resources/stamp/
mkdir --parent src/main/resources/fr/inria/stamp/test/
mkdir --parent src/main/resources/fr/inria/stamp/coverage/
cp -r target/classes/fr/inria/stamp/test/*.class src/main/resources/fr/inria/stamp/test/
cp -r target/classes/fr/inria/stamp/coverage/*.class src/main/resources/fr/inria/stamp/coverage/