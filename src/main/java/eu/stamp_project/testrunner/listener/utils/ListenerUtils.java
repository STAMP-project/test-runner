package eu.stamp_project.testrunner.listener.utils;

import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.listener.CoveredTestResultPerTestMethod;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.junit.runner.Description;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.function.Function;

public class ListenerUtils {

    public static final Function<Description, String> getMethodName = description -> {
        String methodName = "";
        try {
            methodName = description.getMethodName();
        } catch (NoSuchMethodError e) {
            methodName = description.getDisplayName().split("\\(")[0];
        }
        return methodName;
    };

    public static final Function<Description, String> getClassName = description -> {
        String className = "";
        try {
            className = description.getClassName();
        } catch (NoSuchMethodError e) {
            if (description.isSuite()) {
                className = description.getDisplayName();
            } else {
                className = description.getDisplayName().split("\\(")[1].split("\\)")[0];
            }
        }
        return className;
    };

	/**
	 * Clones each result so it doesn't get changed by the runtime afterwards
	 * @param original the original store
	 * @return a clone of the original store
	 */
	public static ExecutionDataStore cloneExecutionDataStore(ExecutionDataStore original) {
		ExecutionDataStore cloned = new ExecutionDataStore();
		original.getContents().stream().forEach(x -> {
			ExecutionData executionData = new ExecutionData(x.getId(), x.getName(), x.getProbes().clone());
			synchronized (cloned) {
				cloned.put(executionData);
			}
		});
		return cloned;
	}

    public static void saveToMemoryMappedFile(File file, Object object) {
        try {
            // Serialize the object
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
            byte[] bytes = bos.toByteArray();

            // Create output dir if it does not exist
            File outputDir = new File(CoveredTestResultPerTestMethod.OUTPUT_DIR);
            if (!outputDir.exists()) {
                if (!outputDir.mkdirs()) {
                    System.err.println("Error while creating output dir");
                }
            }

            // Write to shared memory file
            final FileChannel channel = FileChannel.open(file.toPath(),
                    StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, bytes.length);
            buffer.put(bytes);

            bos.close();
            out.close();
            channel.close();
            System.out.println("File saved to the following path: " + file.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Error while writing memory-mapped serialized file.");
            throw new RuntimeException(e);
        }
    }

    public static <T> T loadFromMemoryMappedFile(File file) {
        try {
            // Access the shared memory file
            FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.READ);
            MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());

            // Get content
            byte[] buffer = new byte[(int) channel.size()];
            mappedByteBuffer.get(buffer);

            // Read and deserialize the file
            ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(buffer));
            final T load = (T) is.readObject();

            is.close();
            channel.close();
            file.delete();
            return load;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static File computeTargetFilePath(String outputDir, String outputFile) {
        return new File(
                new File(EntryPoint.workingDirectory != null && EntryPoint.workingDirectory.exists() ? EntryPoint.workingDirectory.getAbsolutePath() : "./",
                        outputDir).getAbsolutePath(),
                outputFile);
    }
}