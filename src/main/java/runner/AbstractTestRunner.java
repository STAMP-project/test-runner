package runner;

import listener.TestListener;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

import java.io.File;
import java.net.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 30/06/17
 */
public abstract class AbstractTestRunner implements TestRunner {

    private URLClassLoader classLoader;

    public AbstractTestRunner(String classpath) {
        this(classpath.split(System.getProperty("path.separator")));
    }

    public AbstractTestRunner(String[] classpath) {
        this.classLoader = new URLClassLoader(Arrays.stream(classpath)
                .map(File::new)
                .map(File::toURI)
                .map(uri -> {
                    try {
                        return uri.toURL();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }).toArray(URL[]::new), ClassLoader.getSystemClassLoader());
    }

    protected Class<?> loadClass(String fullQualifiedName) {
        try {
            return this.classLoader.loadClass(fullQualifiedName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TestListener run(Map<String, Collection<String>> testMethodNamesForClasses) {
        return testMethodNamesForClasses.keySet().stream()
                .map(fullQualifiedName -> this.run(fullQualifiedName, testMethodNamesForClasses.get(fullQualifiedName)))
                .reduce(new TestListener(), TestListener::aggregate);
    }

    @Override
    public TestListener run(Collection<String> fullQualifiedNames) {
        return fullQualifiedNames.stream()
                .map(this::run)
                .reduce(new TestListener(), TestListener::aggregate);
    }

    @Deprecated
    protected Class<?>[] loadClasses(List<String> fullQualifiedNames) {
        return (Class<?>[]) (fullQualifiedNames.stream()
                .map(this::loadClass)
                .toArray()
        );
    }

}
