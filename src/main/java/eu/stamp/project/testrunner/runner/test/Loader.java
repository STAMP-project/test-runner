package eu.stamp.project.testrunner.runner.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 21/12/17
 */
public class Loader<T> {

    public T load(String name) {
        T object;
        try (FileInputStream fin = new FileInputStream("target/dspot/" + name + ".ser");) {
            try (ObjectInputStream ois = new ObjectInputStream(fin)) {
                object = (T) ois.readObject();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        new File("target/dspot/" + name + ".ser").delete();
        return object;
    }

}
