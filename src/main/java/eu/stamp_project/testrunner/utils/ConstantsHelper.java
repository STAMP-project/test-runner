package eu.stamp_project.testrunner.utils;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 28/11/18
 */
public class ConstantsHelper {

    public static void exit() {
        if (!"true".equals(System.getProperty("noExitForTesting"))) {
            System.exit(0);
        }
    }

    public enum MutationEngine {
        GREGOR(Collections.singletonList("ALL")),
        DESCARTES(Arrays.asList(
                "void",
                "null",
                "true",
                "false",
                "empty",
                "0",
                "1",
                "(byte)0",
                "(byte)1",
                "(short)0",
                "(short)1",
                "0L",
                "1L",
                "0.0",
                "1.0",
                "0.0f",
                "1.0f",
                "'\40'",
                "'A'",
                "\"\"",
                "\"A\""
        ));
        public final List<String> mutators;
        MutationEngine(List<String> mutators) {
            this.mutators = mutators;
        }
    }

    public static final String WHITE_SPACE = " ";

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

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
