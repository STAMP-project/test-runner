package eu.stamp.project.testrunner.runner.coverage;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

class MemoryClassLoader extends URLClassLoader {

    private final Map<String, byte[]> definitions = new HashMap<>();

    public MemoryClassLoader(URL[] urls) {
        super(urls, ClassLoader.getSystemClassLoader());
    }

    /**
     * Add a in-memory representation of a class.
     *
     * @param name  name of the class
     * @param bytes class definition
     */
    public void addDefinition(final String name, final byte[] bytes) {
        definitions.put(name, bytes);
    }

    @Override
    public Class<?> loadClass(final String name)
            throws ClassNotFoundException {
        final byte[] bytes = definitions.get(name);
        try {
            if (bytes != null) {
                return defineClass(name, bytes, 0, bytes.length);
            }
        } catch (java.lang.LinkageError error) {
            return super.loadClass(name, false);
        }
        return super.loadClass(name, false);
    }

}