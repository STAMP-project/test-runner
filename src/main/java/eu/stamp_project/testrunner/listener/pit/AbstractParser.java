package eu.stamp_project.testrunner.listener.pit;

import java.io.File;
import java.util.List;

abstract public class AbstractParser<T extends AbstractPitResult> {

    public enum OutputFormat {XML, CSV}

    private final String PATH_TO_MUTATIONS_RESULT;

    AbstractParser(String PATH_TO_MUTATIONS_RESULT) {
        this.PATH_TO_MUTATIONS_RESULT = PATH_TO_MUTATIONS_RESULT;
    }

    protected File getPathOfMutationsFile(String pathToDirectoryResults) {
        if (!new File(pathToDirectoryResults).exists()) {
            return null;
        }
        if (new File(pathToDirectoryResults + PATH_TO_MUTATIONS_RESULT).exists()) {
            return new File(pathToDirectoryResults + PATH_TO_MUTATIONS_RESULT);
        }
        final File[] files = new File(pathToDirectoryResults).listFiles();
        if (files == null) {
            return null;
        }
        File directoryReportPit = files[0];
        if (!directoryReportPit.exists()) {
            return null;
        }
        return new File(directoryReportPit.getPath() + PATH_TO_MUTATIONS_RESULT);
    }

    public List<T> parseAndDelete(String pathToDirectoryResults) {
        final File fileResults = getPathOfMutationsFile(pathToDirectoryResults);
        final List<T> results = parse(fileResults);
        // TODO re-implement it without FileUtils
        /*try {
            FileUtils.deleteDirectory(new File(pathToDirectoryResults));
        } catch (IOException e) {
            // ignored
        }*/
        return results;
    }

    abstract public List<T> parse(File fileResults);

    public static AbstractParser<?> build(OutputFormat outputFormat) {
        switch (outputFormat) {
            case CSV:
                return new PitCSVResultParser();
            case XML:
                return new PitXMLResultParser();
            default:
                throw new RuntimeException("Unrecognized parameter:" + outputFormat.toString());
        }
    }
}
