package littleware.base;

/** 
 * Combination Factory and confiuration manager.
 */
public abstract class ScriptRunnerFactory implements Factory<ScriptRunner>, java.io.Serializable {

    /** Do nothing constructor */
    public ScriptRunnerFactory() {
    }

    /**
     * Static handle to get the Factory that creates
     * the preferred ScriptRunner implementation.
     * Hard-coded to BsfScriptRunner.Factory for now -
     * may make configurable to javax.script when we
     * migrate to java 6.
     */
    public static ScriptRunnerFactory getFactory() {
        return BsfScriptRunner.getFactory();
    }

    public abstract ScriptRunner create();
}
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

