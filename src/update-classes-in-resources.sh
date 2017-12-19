#!/usr/bin/env bash

rm -rf src/main/resources/stamp/
mkdir --parent src/main/resources/stamp/fr/inria/test/
mkdir --parent src/main/resources/stamp/fr/inria/coverage/
cp -r target/classes/stamp/fr/inria/test/*.class src/main/resources/stamp/fr/inria/test/
cp -r target/classes/stamp/fr/inria/coverage/*.class src/main/resources/stamp/fr/inria/coverage/