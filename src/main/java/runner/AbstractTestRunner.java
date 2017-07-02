package runner;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

import java.io.File;
import java.net.*;
import java.util.Arrays;
import java.util.List;

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

    @Deprecated
    protected Class<?>[] loadClasses(List<String> fullQualifiedNames) {
        return (Class<?>[]) (fullQualifiedNames.stream()
                .map(this::loadClass)
                .toArray()
        );
    }

}
