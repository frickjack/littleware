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

import java.util.logging.Logger;
import java.io.*;
import java.util.*;

import org.apache.bsf.*;

/**
 * BSF based implementation of ScriptRunner.
 */
public class BsfScriptRunner implements ScriptRunner {

    private static Logger olog_generic = Logger.getLogger("littleware.base.BSFScriptRunner");

    private enum Language {

        PYTHON {

            public String toString() {
                return "jython";
            }
        },
        JAVASCRIPT {

            public String toString() {
                return "javascript";
            }
        };
    }
    private static SortedSet<String> ov_lang = new TreeSet<String>();

    static {
        for (Language n_lang : Language.values()) {
            ov_lang.add(n_lang.toString());
        }
        ov_lang = Collections.unmodifiableSortedSet(ov_lang);
    }
    private static final String OS_STDERR = "stderr";
    private static final String OS_STDOUT = "stdout";
    private BSFManager om_script = new BSFManager();
    private PrintWriter owrite_err = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.err)));
    private PrintWriter owrite_out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
    private String os_lang = Language.JAVASCRIPT.toString();

    /** 
     * Constructor just registers System.err and System.out writers
     * as stderr and stdout beans.
     */
    public BsfScriptRunner() {
        setWriter(owrite_out);
        setErrorWriter(owrite_err);
    }

    public PrintWriter getErrorWriter() {
        return owrite_err;
    }

    public void setErrorWriter(PrintWriter write_err) {
        owrite_err = write_err;
        om_script.registerBean(OS_STDERR, write_err);
    }

    public PrintWriter getWriter() {
        return owrite_out;
    }

    public void setWriter(PrintWriter write_out) {
        owrite_out = write_out;
        om_script.registerBean(OS_STDOUT, write_out);
    }

    public String getLanguage() {
        return os_lang;
    }

    public void setLanguage(String s_lang) throws ScriptException {
        if (ov_lang.contains(s_lang)) {
            os_lang = s_lang;
        } else {
            throw new ScriptException("Unsupported languages: " + s_lang);
        }
    }

    /**
     * Hard coded to jython and javascript for now,
     * can pull from littleware.properties later.
     */
    public SortedSet<String> getSupportedLanguages() {
        return ov_lang;
    }

    public void exec(String s_script) throws ScriptException {
        try {
            om_script.exec(os_lang, "unknown", 0, 0, s_script);
        } catch (BSFException e) {
            throw new ScriptException("Script exec failed, caught: " + e, e);
        } finally {
            owrite_out.flush();
            owrite_err.flush();
        }
    }

    public Object registerBean(String s_name, Object x_bean) {
        if ((null == s_name) || s_name.equals(OS_STDOUT) || s_name.equals(OS_STDERR)) {
            return null;
        }
        Object x_result = om_script.lookupBean(s_name);
        om_script.registerBean(s_name, x_bean);
        return x_result;
    }

    public Object clearBean(String s_name) {
        if ((null == s_name) || s_name.equals(OS_STDOUT) || s_name.equals(OS_STDERR)) {
            return null;
        }
        Object x_result = om_script.lookupBean(s_name);
        om_script.unregisterBean(s_name);
        return x_result;
    }

    public Object getBean(String s_bean) {
        return om_script.lookupBean(s_bean);
    }
    private static ScriptRunnerFactory ofactory_cache = new ScriptRunnerFactory() {

        public ScriptRunner create() {
            return new BsfScriptRunner();
        }

        public void recycle(ScriptRunner m_done) {
        }
    };

    /**
     * Get a Factory that pushes these guys out.
     * Clients should use ScriptRunnerFactory.getFactory ().
     */
    public static ScriptRunnerFactory getFactory() {
        return ofactory_cache;
    }
}

