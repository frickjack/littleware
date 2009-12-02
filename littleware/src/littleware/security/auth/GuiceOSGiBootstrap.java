/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.auth;

import com.google.inject.Module;
import java.util.List;
import org.osgi.framework.BundleActivator;

/**
 * Specialization of LittleBootstrap for OSGiGuiceBootstrap.
 * Implementation uses registered Guice modules to allocate
 * OSGi BundleActivators, then bootstraps OSGi.
 */
public interface GuiceOSGiBootstrap extends LittleBootstrap {
    /**
     * Guice module list with which bootstrapServer
     * builds its injector.  The module list is dervied
     * from the lw.guice_module property in littleware.properties if
     * set - otherwise a default set of modules is used.
     *
     * @return list of allocated modules that the caller may modify
     *        to change bootstrap behavior
     */
    public List<Module>  getGuiceModule();

    /**
     * OSGi activators to be allocated via the Guice injector,
     * and then passed on to OSGi for bootstrap.
     *
     * @return list of OSGi activator classes that the caller may
     *              modify to change bootstrap behavior
     */
    public List<Class<? extends BundleActivator>> getOSGiActivator();

    /**
     * Boot the littleware runtime and return a guice-injected
     * instance of the given class.
     * 
     * @param bootClass to instantiate
     * @return injected object upon system startup 
     */
    public <T> T bootstrap( Class<T> bootClass );
}
