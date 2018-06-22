[![Build Status](https://travis-ci.org/STAMP-project/testrunner.svg?branch=master)](https://travis-ci.org/STAMP-project/testrunner)[![Coverage Status](https://coveralls.io/repos/github/STAMP-project/testrunner/badge.svg?branch=master)](https://coveralls.io/github/STAMP-project/testrunner?branch=master)[![Maven Central](https://maven-badges.herokuapp.com/maven-central/eu.stamp-project/test-runner/badge.svg)](https://mavenbadges.herokuapp.com/maven-central/eu.stamp-project/test-runner)


# Test Runner

This project provides a framework to run JUnit test in a new JVM. It allows to retrieve results using serialization / deserialization.

# Supported Features:


* test: run JUnit test, the whole test class or specific test cases methods.
* coverage: run JaCoCo to compute the instruction coverage of the given test suite or by test methods.
* JVMArgs: can specify Java Virtual Machine Arguments
* workingDirectory: can specify where to launch the java process
* outputStream and errStream: can customize the output stream and the error stream of the java process.
* timeout: can specify a custom time out in milli second.
* blacklist: can discard test methods by their name among test classes.

# Development:

1. clone:
```
git clone https://github.com/STAMP-project/testrunner.git
```

2. build resources:
```
cd testrunner/src/test/resources/test-projects/
mvn install
```

3. build `testrunner`:
```
cd ../../../..
mvn install
```

Please, open an issue if you have any question / suggestion. Pull request are welcome! 😃

### Licence

TestRunner is published under LGPL-3.0 (see [Licence.md](https://github.com/STAMP-project/testrunner/blob/master/LICENSE) for
further details).

### Funding

TestRunner is partially funded by research project STAMP (European Commission - H2020)
![STAMP - European Commission - H2020](docs/logo_readme_md.png)
