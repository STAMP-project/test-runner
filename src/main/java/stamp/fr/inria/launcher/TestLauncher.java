package stamp.fr.inria.launcher;

import spoon.reflect.declaration.CtType;
import stamp.fr.inria.listener.TestListener;
import stamp.fr.inria.runner.DefaultTestRunner;
import stamp.fr.inria.runner.MockitoTestRunner;
import stamp.fr.inria.runner.TestRunner;


/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/07/17
 */
public class TestLauncher {

	private TestRunner mockitoRunner;
	private TestRunner defaultRunner;

	public TestLauncher(String classpath) {
		this.mockitoRunner = new MockitoTestRunner(classpath);
		this.defaultRunner = new DefaultTestRunner(classpath);
	}

	public TestListener run(CtType<?> testClass) {
		final TypeTestEnum typeTest = TypeTestEnum.getTypeTest(testClass);
		System.out.println(typeTest);
		return null;
	}


}
