package eu.stamp_project.testrunner.listener.pit;


public class AbstractPitResult {

    public enum State {SURVIVED, KILLED, NO_COVERAGE, TIMED_OUT, NON_VIABLE, MEMORY_ERROR}

    protected final String fullQualifiedNameOfMutatedClass;

    protected final String fullQualifiedNameMutantOperator;

    protected final String nameOfMutatedMethod;

    protected final int lineNumber;

    protected final AbstractPitResult.State stateOfMutant;

    protected final String fullQualifiedNameOfKiller;

    protected final String simpleNameMethod;

//    protected CtMethod testCase = null;

    public AbstractPitResult(String fullQualifiedNameOfMutatedClass, AbstractPitResult.State stateOfMutant,
                             String fullQualifiedNameMutantOperator,
                             String fullQualifiedNameMethod, String fullQualifiedNameOfKiller,
                             int lineNumber,
                             String nameOfLocalisation) {
        this.fullQualifiedNameOfMutatedClass = fullQualifiedNameOfMutatedClass;
        this.stateOfMutant = stateOfMutant;
        this.fullQualifiedNameMutantOperator = fullQualifiedNameMutantOperator;
        this.fullQualifiedNameOfKiller = fullQualifiedNameOfKiller;
        String[] split = fullQualifiedNameMethod.split("\\.");
        this.simpleNameMethod = split[split.length - 1];
        this.lineNumber = lineNumber;
        this.nameOfMutatedMethod = nameOfLocalisation;
    }

    public AbstractPitResult.State getStateOfMutant() {
        return stateOfMutant;
    }

    public String getFullQualifiedNameMutantOperator() {
        return fullQualifiedNameMutantOperator;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getNameOfMutatedMethod() {
        return nameOfMutatedMethod;
    }

    public String getFullQualifiedNameOfKiller() {
        return fullQualifiedNameOfKiller;
    }

    // TODO move this somewhere in DSpot...
    /*
    public CtMethod getMethod(CtType<?> ctClass) {
        if ("none".equals(this.simpleNameMethod)) {
            return null;
        } else {
            if (this.testCase == null) {
                List<CtMethod<?>> methodsByName = ctClass.getMethodsByName(this.simpleNameMethod);
                if (methodsByName.isEmpty()) {
                    if (ctClass.getSuperclass() != null) {
                        return getMethod(ctClass.getSuperclass().getDeclaration());
                    } else {
                        return null;
                    }
                }
                this.testCase = methodsByName.get(0);
            }
            return this.testCase;
        }
    }
    */
}
