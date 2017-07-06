package stamp.fr.inria.launcher;

import org.junit.Test;
import spoon.Launcher;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 06/07/17
 */
public class TestLauncherTest {

	@Test
	public void test() throws Exception {

		Launcher launcher = new Launcher();
		launcher.getEnvironment().setSourceClasspath(
				System.getProperty("java.class.path").split(System.getProperty("path.separator"))
		);
		launcher.addInputResource("src/test/resources/src/");
		launcher.addInputResource("src/test/resources/test/");
		launcher.buildModel();

		TestLauncher testLauncher = new TestLauncher(
				"src/test/resources/MockitoDemo-1.0-SNAPSHOT.jar"
						+ System.getProperty("path.separator") +
						"src/test/resources/MockitoDemo-1.0-SNAPSHOT-tests.jar"
		);

		testLauncher.run(launcher.getFactory().Class().get("info.sanaulla.dal.BookDALTest"));
	}
}
