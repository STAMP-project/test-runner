package stamp.fr.inria.launcher;

import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;
import stamp.fr.inria.listener.TestListener;
import stamp.fr.inria.runner.DefaultTestRunner;
import stamp.fr.inria.runner.MockitoTestRunner;
import stamp.fr.inria.runner.TestRunner;

import java.util.Collection;


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

	public TestListener run(CtType<?> testClass, Collection<String> testMethodNames) {
		if (testClass.getModifiers().contains(ModifierKind.ABSTRACT)) {
			final CtTypeReference<?> referenceToAbstractClass = testClass.getReference();
			return testClass.getFactory().Class().getAll().stream()
					.filter(ctType -> ctType.getSuperclass() != null)
					.filter(ctType ->
						ctType.getSuperclass().equals(referenceToAbstractClass)
					)
					.map(ctType -> this.run(ctType, testMethodNames))
					.reduce(new TestListener(), TestListener::aggregate);
		}
		final TypeTestEnum typeTest = TypeTestEnum.getTypeTest(testClass);
		if (typeTest == TypeTestEnum.DEFAULT) {
			return defaultRunner.run(testClass.getQualifiedName(), testMethodNames);
		} else {
			return mockitoRunner.run(testClass.getQualifiedName(), testMethodNames);
		}
	}

	public TestListener run(CtType<?> testClass) {
		if (testClass.getModifiers().contains(ModifierKind.ABSTRACT)) {
			final CtTypeReference<?> referenceToAbstractClass = testClass.getReference();
			return testClass.getFactory().Class().getAll().stream()
					.filter(ctType -> ctType.getSuperclass() != null)
					.filter(ctType ->
							ctType.getSuperclass().equals(referenceToAbstractClass)
					)
					.map(this::run)
					.reduce(new TestListener(), TestListener::aggregate);
		}
		final TypeTestEnum typeTest = TypeTestEnum.getTypeTest(testClass);
		if (typeTest == TypeTestEnum.DEFAULT) {
			return defaultRunner.run(testClass.getQualifiedName());
		} else {
			return mockitoRunner.run(testClass.getQualifiedName());
		}
	}


}
