package stamp.fr.inria;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/12/17
 */
public class AbstractTest {

    protected final String MAVEN_HOME = Arrays.stream(((URLClassLoader) URLClassLoader.getSystemClassLoader()).getURLs())
            .map(URL::getPath)
            .filter(path -> path.contains("/.m2/repository/"))
            .findFirst()
            .map(s -> s.substring(0, s.indexOf("/.m2/repository/") + "/.m2/repository/".length()))
            .get();

    protected final String nl = System.getProperty("line.separator");

}
