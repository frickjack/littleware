/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.bootstrap.test;

import com.google.inject.Binder;
import com.google.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.base.Option;
import littleware.base.Options;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.LittleBootstrap;
import littleware.bootstrap.SessionBootstrap;
import littleware.bootstrap.SessionModule;
import littleware.bootstrap.SessionModuleFactory;
import littleware.test.LittleTest;

public class BootstrapTester extends LittleTest {
    private final LittleBootstrap boot;

    @Inject
    public BootstrapTester(LittleBootstrap boot) {
        setName("testModuleLoad");
        this.boot = boot;
    }

    /**
     * Just simple test to verify that ClientBootstrap
     * and ServerBootstrap load some modules
     */
    public void testModuleLoad() {
        try {
            assertTrue("Found an app module",
                    !boot.getModuleSet().isEmpty());
            assertTrue( "Boootstrap factory initialized",
                    LittleBootstrap.factory.getActiveRuntime().get() == boot
                    );
        } catch (Exception ex) { handle(ex); }
    }

    private SessionBootstrap buildSession(final String label) {
        final SessionBootstrap.SessionBuilder builder = boot.newSessionBuilder();
        for (SessionModuleFactory factory : builder.getSessionModuleSet()) {
            builder.removeModuleFactory(factory);
        }
        builder.addModuleFactory(new SessionModuleFactory() {

            @Override
            public SessionModule buildSessionModule(AppProfile profile) {
                return new SessionModule() {

                    @Override
                    public Option<? extends Class<Runnable>> getSessionStarter() {
                        return Options.NONE;
                    }

                    @Override
                    public void configure(Binder binder) {
                        binder.bind(String.class).toInstance(label);
                    }
                };
            }
        });
        return builder.build();
    }

    /**
     * Verify semantics of child session builders ...
     */
    public void testSessionSemantics() {
        try {
            final String label1 = buildSession("1").startSession(String.class);
            assertTrue("Got expected session 1 label: " + label1, "1".equals(label1));
            final String label2 = buildSession("2").startSession(String.class);
            assertTrue("Got expected session 2 label: " + label2, "2".equals(label2));
        } catch (Exception ex) { handle(ex); }
    }
}
