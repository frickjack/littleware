/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.base;

import java.util.SortedSet;
import java.io.PrintWriter;

/**
 * Simplified interface wrapping java scripting bindings.
 * We are currently using jdk1.5 and the Apache BSF scripting engine,
 * but will hopefully eventually move to the jdk1.6 based javax.scripting
 * based engine.
 */
public interface ScriptRunner {

    /**
     * Get the preferred stderr for this Script Runner
     */
    public PrintWriter getErrorWriter();

    /**
     * Set the preferred stderr for this ScriptRunner
     */
    public void setErrorWriter(PrintWriter write_error);

    /**
     * Get the prefered stdout for this runner
     */
    public PrintWriter getWriter();

    /** Set the preferred stdout */
    public void setWriter(PrintWriter write_out);

    /** Get the active scripting language */
    public String getLanguage();

    /**
     * Set the active scripting language
     *
     * @param s_lang valid registered scripting language
     * @exception NoSuchThingException if language not available
     */
    public void setLanguage(String s_lang) throws ScriptException;

    /**
     * Get the set of scripting languages supported by this guy
     */
    public SortedSet<String> getSupportedLanguages();

    /**
     * Execute the given script
     */
    public void exec(String s_script) throws ScriptException;

    /**
     * Register a bean to be made accessible to subsequent scripts
     * via callbacks to bsf.lookupBean() - up to us to register a bsf
     * object that manages that when we move to jdk6 scripting api.
     *
     * @param s_name to register bean under - NOOP if "stderr" or "stdout"
     * @param x_bean to expose to the scripting engine
     * @return previous bean value or null
     */
    public Object registerBean(String s_name, Object x_bean);

    /**
     * Clear a previously registered bean
     *
     * @param s_name to clear - NOOP if "stderr" or "stdout"
     * @return previous bean value or null
     */
    public Object clearBean(String s_name);

    /**
     * Get the bean associated with the given name if any
     *
     * @param s_bean name
     * @return bean or null
     */
    public Object getBean(String s_bean);
}

