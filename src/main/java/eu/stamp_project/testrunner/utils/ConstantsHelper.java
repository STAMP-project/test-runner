package eu.stamp_project.testrunner.utils;


import java.util.function.Function;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 28/11/18
 */
public class ConstantsHelper {

    public static final String WHITE_SPACE = " ";

    public static final String PATH_SEPARATOR = System.getProperty("path.separator");

    public static final String FILE_SEPARATOR = System.getProperty("file.separator");

    public static final transient Function<String, String> pathToFullQualifiedName = string ->
    {
        if (FILE_SEPARATOR.equals("\\")) {
            return string.replace("\\", ".");
        } else {
            return string.replace(FILE_SEPARATOR, ".");
        }
    };

    public static final transient Function<String, String> fullQualifiedNameToPath = (String string) ->
    {
        if (FILE_SEPARATOR.equals("\\")) {
            return string.replace(".", "\\\\");
        } else {
            return string.replace(".", FILE_SEPARATOR);
        }
    };

}
