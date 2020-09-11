package eu.stamp_project.testrunner.runner.coverage;

import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.testrunner.runner.Failure;
import eu.stamp_project.testrunner.utils.ConstantsHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.ResourceBundle.clearCache;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 18/12/17
 */
public abstract class JacocoRunner {

    protected MemoryClassLoader instrumentedClassLoader;

    protected Instrumenter instrumenter;

    protected IRuntime runtime;

    protected List<String> blackList;

    /**
     * @param classesDirectory     the path to the directory that contains the .class file of sources
     * @param testClassesDirectory the path to the directory that contains the .class file of test sources
     */
    public JacocoRunner(String classesDirectory, String testClassesDirectory) {
        this(classesDirectory, testClassesDirectory, Collections.emptyList());
    }

    /**
     * @param classesDirectory     the path to the directory that contains the .class file of sources
     * @param testClassesDirectory the path to the directory that contains the .class file of test sources
     * @param blackList            the names of the test methods to NOT be run.
     */
    public JacocoRunner(String classesDirectory, String testClassesDirectory, List<String> blackList) {
        try {
            this.instrumentedClassLoader = new MemoryClassLoader(
                    new URL[]{
                            new File(classesDirectory).toURI().toURL(),
                            new File(testClassesDirectory).toURI().toURL()
                    }
            );
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        this.blackList = blackList;
        this.runtime = new LoggerRuntime();
        this.instrumenter = new Instrumenter(this.runtime);
        // instrument source code
        instrumentAll(classesDirectory);
    }

    
	public void recreateInstrumentedClassloaded(String classpath, String classesDirectory, String testClassesDirectory,
			Map<String, byte[]> definitions) {
		try {

			URLClassLoader urlloader = getUrlClassloaderFromClassPath(classpath);

			this.instrumentedClassLoader = new MemoryClassLoader(new URL[] { new File(classesDirectory).toURI().toURL(),
					new File(testClassesDirectory).toURI().toURL() }, urlloader);
			this.instrumentedClassLoader.setDefinitions(definitions);

		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

	}
    
    /**
     * Compute the instruction coverage of the given tests
     * Using directly this method is discouraged, since it won't avoid class loading conflict. Use {@link EntryPoint#runCoverage(String, String, String[], String[])} instead.
     *
     * @param classesDirectory             the path to the directory that contains the .class file of sources
     * @param testClassesDirectory         the path to the directory that contains the .class file of test sources
     * @param fullQualifiedNameOfTestClass the full qualified name of the test class to execute
     * @param testMethodNames              the simple names of the test methods to exeecute
     * @return a {@link Coverage} instance that contains the instruction coverage of the given tests.
     */
    public Coverage run(String classesDirectory,
                        String testClassesDirectory,
                        String fullQualifiedNameOfTestClass,
                        String... testMethodNames) {
        final RuntimeData data = new RuntimeData();
        final ExecutionDataStore executionData = new ExecutionDataStore();
        final SessionInfoStore sessionInfos = new SessionInfoStore();
        URLClassLoader classLoader;
        try {
            classLoader = new URLClassLoader(new URL[]
                    {new File(testClassesDirectory).toURI().toURL()}, this.instrumentedClassLoader);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        final String resource = ConstantsHelper.fullQualifiedNameToPath.apply(fullQualifiedNameOfTestClass) + ".class";
        try {
            this.instrumentedClassLoader.addDefinition(
                    fullQualifiedNameOfTestClass,
                    IOUtils.toByteArray(classLoader.getResourceAsStream(resource))
            );
            runtime.startup(data);
            final Coverage listener = this.executeTest(new String[]{fullQualifiedNameOfTestClass}, testMethodNames, Collections.emptyList());
            if (!((TestResult) listener).getFailingTests().isEmpty()) {
                System.err.println("Some test(s) failed during computation of coverage:\n" +
                        ((TestResult) listener).getFailingTests()
                                .stream()
                                .map(Failure::toString)
                                .collect(Collectors.joining("\n"))
                );
            }
            data.collect(executionData, sessionInfos, false);
            runtime.shutdown();
            clearCache(this.instrumentedClassLoader);
            listener.collectData(executionData, classesDirectory);
            return listener;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract Coverage executeTest(String[] testClassNames,
                                            String[] testMethodNames,
                                            List<String> blackList);
    
    
    
    
    protected Coverage executeTest(String[] testClassNames,
                               String[] testMethodNames,
                               List<String> blackList, Coverage internal) {
    	return executeTest(testClassNames, testMethodNames, blackList);
    }
    

    /**
     * Compute the instruction coverage of the given tests
     * Using directly this method is discouraged, since it won't avoid class loading conflict. Use {@link EntryPoint#runCoverage(String, String, String[], String[])} instead.
     *
     * @param classesDirectory               the path to the directory that contains the .class file of sources
     * @param testClassesDirectory           the path to the directory that contains the .class file of test sources
     * @param fullQualifiedNameOfTestClasses the full qualified names of the test classes to execute
     * @return a {@link Coverage} instance that contains the instruction coverage of the given tests.
     */
    public Coverage run(String classesDirectory,
                        String testClassesDirectory,
                        String... fullQualifiedNameOfTestClasses) {
        final RuntimeData data = new RuntimeData();
        final ExecutionDataStore executionData = new ExecutionDataStore();
        final SessionInfoStore sessionInfos = new SessionInfoStore();
        URLClassLoader classLoader;
        try {
            classLoader = new URLClassLoader(new URL[]
                    {new File(testClassesDirectory).toURI().toURL()}, this.instrumentedClassLoader);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        Arrays.stream(fullQualifiedNameOfTestClasses).forEach(fullQualifiedNameOfTestClass -> {
            final String resource = ConstantsHelper.fullQualifiedNameToPath.apply(fullQualifiedNameOfTestClass) + ".class";
            try {
                this.instrumentedClassLoader.addDefinition(
                        fullQualifiedNameOfTestClass,
                        IOUtils.toByteArray(classLoader.getResourceAsStream(resource)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            runtime.startup(data);
            final Coverage listener = this.executeTest(fullQualifiedNameOfTestClasses, new String[0], this.blackList);
            if (!((TestResult) listener).getFailingTests().isEmpty()) {
                System.err.println("Some test(s) failed during computation of coverage:\n" +
                        ((TestResult) listener).getFailingTests()
                                .stream()
                                .map(Failure::toString)
                                .collect(Collectors.joining("\n"))
                );
            }
            data.collect(executionData, sessionInfos, false);
            runtime.shutdown();
            clearCache(this.instrumentedClassLoader);
            listener.collectData(executionData, classesDirectory);
            return listener;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * We indicate the method to compute the coverage
     * @return
     */
    public Coverage runAlternative(Coverage coverage, String classesDirectory, String testClassesDirectory,
			String fullQualifiedNameOfTestClass, String... testMethodNames) {

		final RuntimeData data = new RuntimeData();
		final ExecutionDataStore executionData = new ExecutionDataStore();
		final SessionInfoStore sessionInfos = new SessionInfoStore();
		
		try {
		
			runtime.startup(data);

			final Coverage listener = executeTest(new String[] { fullQualifiedNameOfTestClass }, testMethodNames,
					Collections.emptyList(), coverage);

			if (!((TestResult) listener).getFailingTests().isEmpty()) {
				System.err.println("Some test(s) failed during computation of coverage:\n" + ((TestResult) listener)
						.getFailingTests().stream().map(Failure::toString).collect(Collectors.joining("\n")));
			}

		
			data.collect(executionData, sessionInfos, false);

			runtime.shutdown();

			clearCache(this.instrumentedClassLoader);

			listener.collectData(executionData, classesDirectory);
			listener.collectData(executionData, testClassesDirectory);

			return listener;

		} catch (

		Exception e) {
			System.out.println("Error: could not collect test data");
			e.printStackTrace();
			return null;
		}

	}
    
	
    private void instrumentAll(String classesDirectory) {
        final Iterator<File> iterator = FileUtils.iterateFiles(new File(classesDirectory), new String[]{"class"}, true);
        while (iterator.hasNext()) {
            final File next = iterator.next();
            final String fileName = next.getPath().substring(classesDirectory.length() + (classesDirectory.endsWith(ConstantsHelper.FILE_SEPARATOR) ? 0 : 1));
            final String fullQualifiedName = ConstantsHelper.pathToFullQualifiedName.apply(fileName).substring(0, fileName.length() - ".class".length());
            try {
                instrumentedClassLoader.addDefinition(fullQualifiedName,
                        instrumenter.instrument(instrumentedClassLoader.getResourceAsStream(fileName), fullQualifiedName));
            } catch (IOException e) {
                throw new RuntimeException(fileName + "," + new File(fileName).getAbsolutePath() +
                        "," + fullQualifiedName, e);
            }
        }
        clearCache(instrumentedClassLoader);
    }
    
    public MemoryClassLoader getInstrumentedClassLoader() {
		return instrumentedClassLoader;
	}

  	public URLClassLoader getUrlClassloader(String[] classpath, String classesDirectory, String testClassesDirectory) {
  		URLClassLoader classLoader;
  		URL[] urls = new URL[classpath.length + 2];

  		try {

  			for (int i = 0; i < classpath.length; i++) {
  				// System.out.println(cps[i]);
  				urls[i] = new File(classpath[i]).toURI().toURL();
  			}

  			urls[classpath.length] = new File(classesDirectory).toURI().toURL();

  			urls[classpath.length + 1] = new File(testClassesDirectory).toURI().toURL();

  			classLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader()// this.instrumentedClassLoader
  			);


  		} catch (MalformedURLException e) {
  			throw new RuntimeException(e);
  		}
  		return classLoader;
  	}
  	
  	/**
  	 * Return a classpath with all the dependencies.
  	 * 
  	 * @param classpath
  	 * @param classesDirectory
  	 * @param testClassesDirectory
  	 * @return
  	 */
  	public URLClassLoader getUrlClassloaderFromClassPath(String classpath) {
  		URLClassLoader classLoader;
  		String[] cps = classpath.split(File.pathSeparator);
  		URL[] urls = new URL[cps.length];

  		try {

  			for (int i = 0; i < cps.length; i++) {
  	
  				urls[i] = new File(cps[i]).toURI().toURL();
  			}

  			classLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());

  		} catch (MalformedURLException e) {
  			throw new RuntimeException(e);
  		}
  		return classLoader;
  	}
    

}
