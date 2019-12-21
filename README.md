# Test Runner

[![Build Status](https://travis-ci.org/STAMP-project/testrunner.svg?branch=master)](https://travis-ci.org/STAMP-project/testrunner) [![Coverage Status](https://coveralls.io/repos/github/STAMP-project/testrunner/badge.svg?branch=master)](https://coveralls.io/github/STAMP-project/testrunner?branch=master) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/eu.stamp-project/test-runner/badge.svg)](https://mavenbadges.herokuapp.com/maven-central/eu.stamp-project/test-runner)

This project provides a framework to run JUnit tests in a new JVM. It allows to retrieve results using serialization / deserialization.

## Supported Features:


* test: run JUnit test, the whole test class or specific test cases methods.
* coverage: run JaCoCo to compute the instruction coverage of the given test suite or by test methods.
* mutation score: run PIT to compute the mutation score of the test suite. Supporting descartes and gregor mutation engine.
* JVMArgs: can specify Java Virtual Machine Arguments
* workingDirectory: can specify where to launch the java process
* outputStream and errStream: can customize the output stream and the error stream of the java process.
* timeout: can specify a custom time out in milli second.
* blacklist: can discard test methods by their name among test classes.
* maven execution: can now executes the test using **Maven**. This allows users to have a complex build configured in their `pom.xml`.
* the test runner supports JUnit3, JUnit4, and JUnit5. By default it runs JUnit3 or JUnit4. If you need to execute JUnit5 test methods, use the boolean in [EntryPoint](https://github.com/STAMP-project/testrunner/blob/master/src/main/java/eu/stamp_project/testrunner/EntryPoint.java#L69).
* Parametrized JUnit 4 test methods.

/!\ WARNING the test runner is not able to run parametrized JUnit5 test methods.

## API

The provided API is [`eu.stamp_project.testrunner.EntryPoint`](https://github.com/STAMP-project/testrunner/blob/master/src/main/java/eu/stamp_project/testrunner/EntryPoint.java#L63). This class provides several static methods to execute all the test methods of given test classes, specific test methods of a given test class, compute the code coverage with JaCoCo, etc.

#### Tests Execution

1. Executing all the test methods of a test class.

```java
// class TestResult explained below 
TestResult result = EntryPoint.runTests(String classpath, String fullQualifiedNameOfTestClass);
```  

The `classpath` must contain all the dependencies required to execute the test. Elements must be separated by the system path separator. The `fullQualifiedNameOfTestClass` must be a correct full qualified name, _e.g._ `my.package.TestClass`. The folder that contains the compiled file of your project must be included in the `classpath`. The compiled (`.class`) of the test class to be executed must be included in the given `classpath`. 

2. Executing specific test methods of a given test class.

```java
TestResult result = EntryPoint.runTests(String classpath, String fullQualifiedNameOfTestClass, String[] methodNames);
```

The two first parameters are the same above. The String array `methodsNames`contains the name of test methods to be executed.  Each of these test methods must be in the test class designated by the `fullQualifiedNameOfTestClass`.

Complete list:

```java
// Execute all the test methods of a given test class
TestResult result = EntryPoint.runTests(String classpath, String fullQualifiedNameOfTestClass);

// Execute all the test methods of given test classes
TestResult result = EntryPoint.runTests(String classpath, String[] fullQualifiedNameOfTestClasses);

// Execute a specific test method of a given test class
TestResult result = EntryPoint.runTests(String classpath, String fullQualifiedNameOfTestClass, String methodName);

// Execute specific test methods of a given test class
TestResult result = EntryPoint.runTests(String classpath, String fullQualifiedNameOfTestClass, String[] methodNames);

// Execute specific test methods of given test classes
TestResult result = EntryPoint.runTests(String classpath, String[] fullQualifiedNameOfTestClasses, String[] methodNames); 
```

##### Output

The output of all `runTests()` API is a [`eu.stamp_project.testrunner.listener.TestResult`](https://github.com/STAMP-project/testrunner/blob/master/src/main/java/eu/stamp_project/testrunner/listener/TestResult.java#L14).

This object provides all the information needed about the execution of test methods:

   * `getRunningTests()`: returns the list of test methods that have been executed.
   * `getPassingTests()`: returns the list of test methods that succeed.
   * `getFailingTests()`: returns the list of test methods that failed.
   * `getAssumptionFailingTests()`: returns the list of test methods that have a failing assumption. For example, in JUnit4 one can make assumptions using `org.junit.Assume` API, _e.g._ `Assume.assumeTrue(myBoolean)`. If the assumption does not hold, it is not necessary because the program is broken but rather than the test is irrelevant in the current state, _e.g._ one can make dedicated test to a platform. 
   * `getIgnoredTests()`: returns the list of test methods that are ignored.
   
The method `TestResult#aggregate(TestResult that)` allows to aggregate the results. It returns a new `TestResult` that contains both test results, _i.e._ test result of the caller and the parameter. Example:

```java
TestResult result1 = EntryPoint.runTests(classpath, eu.stamp_project.MyFirstTest);
TestResult result2 = EntryPoint.runTests(classpath, eu.stamp_project.MySecondTest);
TestResult allResult = result1.aggregate(result2); // contains both result1 and result2
``` 

#### Global Coverage with JaCoCo. 

API to compute the coverage:

```java
// Compute the instruction coverage of all the test methods of a given test class
Coverage coverage = EntryPoint.runCoverage(String classpath, String targetProjectClasses, String fullQualifiedNameOfTestClass);

// Compute the instruction coverage of all the test methods of given test classes
Coverage coverage = EntryPoint.runCoverage(String classpath, String targetProjectClasses, String[] fullQualifiedNameOfTestClasses);

// Compute the instruction coverage of a specific test method of a given test class
Coverage coverage = EntryPoint.runCoverage(String classpath, String targetProjectClasses, String fullQualifiedNameOfTestClass, String methodName);

// Compute the instruction coverage of specific test methods of a given test class
Coverage coverage = EntryPoint.runCoverage(String classpath, String targetProjectClasses, String fullQualifiedNameOfTestClass, String[] methodNames);

// Compute the instruction coverage of specific test methods of given test classes
Coverage coverage = EntryPoint.runCoverage(String classpath, String targetProjectClasses, String[] fullQualifiedNameOfTestClasses, String[] methodNames); 
```

`String targetProjectClasses` must contain both the absolute paths to the binaries of the program and of the binaries of the test suite. For a typical maven project, this is would be: `<path_to_project>/target/classes:<path_to_project>/target/test-classes` where `<path_to_project>` is the path to the project under test. Note that the separator, here `:` is used on Linux. You must use the system separator. 

##### Output

The output of all `runCoverage()` API is a [`eu.stamp_project.testrunner.listener.Coverage`](https://github.com/STAMP-project/testrunner/blob/master/src/main/java/eu/stamp_project/testrunner/listener/Coverage.java#L11).

The object provides the two following method:

   * `getInstructionsCovered()`: returns the number of instruction covered by the tests.
   * `getInstructionsTotal()`: returns the total number of instruction.

#### Coverage per test methods

In the same way, you can have the coverage per test methods using `runCoveragePerTestMethods()` API of `EntryPoint` class.

##### Output

The output of all `runCoveragePerTestMethods()` API is a [`eu.stamp_project.testrunner.listener.CoveragePerTestMethod`](https://github.com/STAMP-project/testrunner/blob/master/src/main/java/eu/stamp_project/testrunner/listener/CoveragePerTestMethod.java#L13).

   * `Map<String, Coverage> getCoverageResultsMap()`: returns a map that associate the simple of a test method to its instruction coverage.
   * `Coverage getCoverageOf(String testMethodName)`: returns the instruction coverage of a test method, specified by its simple name. 
   
#### Mutation Score

The test runner can now compute the mutation using [PIT](http://pitest.org).

API:

```java
List<? extends AbstractPitResult> EntryPoint.runPit(final String classpath, final String pathToRootProject, final String filterTargetClasses, final String targetTests)
```

`classpath` is the classpath of the application. It must contains all the dependencies, the source code and the test code.
`pathToRootProject` is the path to the root folder of the project.
`filterTargetClasses` is a regex that matches the application source code to be mutated.
`targetTests` is a regex that matches test classes that will be executed to compute the mutation score.

#### Output

The output is a list of pit result. Pit results are encapsuled in two objects, depending on the output format used.

In any case, the pit result gives the following information:

* the full qualified name of the mutated class (application).
* the full qualified name of the mutant operator used to mutate the class designed above.
* the simple name of the method mutated.
* the line number of the mutant.
* the state of the mutant at the end of the analysis. It can be: `SURVIVED`, `KILLED`, `NO_COVERAGE`, `TIMED_OUT`, `NON_VIABLE`, `MEMORY_ERROR`.
* the full qualified name of the test killer.
  
### Configuration

In `EntryPoint` class, you have access to several fields that allow to configure the execution:

   * `boolean jUnit5Mode`: set JUnit5 mode. If your test are JUnit5, you must set this boolean to true.
   * `boolean verbose`: enable the verbose mode.
   * `int timeoutInMs`: the number of milliseconds to wait before considering that the execution is in timeout. In case of time out, this will end with a failure and `EntryPoint` will throw a `java.util.concurrentTimeoutException`.
   * `File workingDirectory`: the file descriptor to specify from where you want to execute the tests. You can use it when your test use relative path for instance. By default, it is set to null to inherit from this java process. 
   * `String JVMArgs`: `EntryPoint` uses the command "java". This field allows users to specify Java Virtual Machine(JVM) arguments, _e.g._ `-Xms4G`. If this value is `null`, `EntryPoint` won't pass any JVMArgs. The value of this field should be properly formatted for command line usage, _e.g._ `-Xms4G -Xmx8G -XX:-UseGCOverheadLimit`. The args should be separated with white spaces.
   * `PrintStream outPrintStream`: allows to pass a customized `PrintStream` on which the java process called will printout. If this field is equal to `null`, `EntryPoint` with use the stdout.
   * `PrintStream errPrintStream`: allows to pass a customized `PrintStream` on which the java process called will printerr. If this field is equal to `null`, `EntryPoint` with use the stderr.
   * `boolean persistence`: enable this boolean in order to keep the state between runs. By default, the persistence is set to true. If you set it to false, the following values will be reset (_i.e._ set the default value) after each run: `JVMArgs`,  `outPrintStream`, `errPrintStream`, `workingDirectory`, `timeoutInMs`.
   * `List<String> blackList`: add to this list the simple name of test methods that you want to avoid to execute. 
   * `MutationEngine mutationEngine`: configure the mutation engine to be used. Possible values are `MutationEngine.DESCARTES` or `MutationEngine.GREGOR`. Default is `MutationEngine.DESCARTES`. You must use the accessor to set this value, see [EntryPoint#setMutationEngine(ConstantsHelper.MutationEngine mutationEngine)](https://github.com/STAMP-project/testrunner/blob/master/src/main/java/eu/stamp_project/testrunner/EntryPoint.java#L156).
   * `List<String> pitMutators`: List of mutators to be used. These mutators are designed by a string. They must match with the used mutation engine. By default, it uses the default mutators for descartes and the mutator `ALL` for gregor. This value is modified when you change the mutation engine with [EntryPoint#setMutationEngine(ConstantsHelper.MutationEngine mutationEngine)](https://github.com/STAMP-project/testrunner/blob/master/src/main/java/eu/stamp_project/testrunner/EntryPoint.java#L156).
   * `AbstractParser.OutputFormat pitOutputFormat`: specify the output format to be used for the mutation analyzed. Possible values are `AbstractParser.OutputFormat.XML` or `AbstractParser.OutputFormat.CSV`. Default is `AbstractParser.OutputFormat.XML`. The `AbstractParser.OutputFormat.XML` contains more information than the `AbstractParser.OutputFormat.CSV`.

## Dependency:

You can add to your `pom.xml`:

```xml
<dependency>
    <groupId>eu.stamp-project</groupId>
    <artifactId>test-runner</artifactId>
    <version>2.0.8</version>
</dependency>
```

## Development:

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

## Licence

TestRunner is published under LGPL-3.0 (see [Licence.md](https://github.com/STAMP-project/testrunner/blob/master/LICENSE) for
further details).

## Funding

TestRunner is partially funded by research project STAMP (European Commission - H2020)
![STAMP - European Commission - H2020](docs/logo_readme_md.png)

