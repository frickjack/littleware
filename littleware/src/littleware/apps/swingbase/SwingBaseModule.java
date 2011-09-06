/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.swingbase;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.apps.swingbase.controller.SwingBaseTool;
import littleware.apps.swingbase.model.BaseData;
import littleware.base.validate.ValidationException;
import littleware.base.feedback.Feedback;
import littleware.base.feedback.FeedbackBundle;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.AppModule;
import littleware.bootstrap.AppModuleFactory;
import littleware.bootstrap.helper.AbstractAppModule;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Setup basic bindings for Feedback and whatever for swing.base framework.
 */
public class SwingBaseModule extends AbstractAppModule {

    private static final Logger log = Logger.getLogger(SwingBaseModule.class.getName());

    public static class Factory implements AppModuleFactory {

        private String appName = null;
        private URL helpUrl = null;
        private String version = null;
        private Properties props = null;

        public Factory appName(String value) {
            this.appName = value;
            return this;
        }

        public String getAppName() {
            return appName;
        }

        public Factory helpUrl(URL value) {
            this.helpUrl = value;
            return this;
        }

        public URL getHelpUrl() {
            return helpUrl;
        }

        public Factory version(String value) {
            this.version = value;
            return this;
        }

        public String getVersion() {
            return version;
        }

        public Factory properties(Properties value) {
            this.props = value;
            return this;
        }

        @Override
        public AppModule build(AppProfile profile) {
            if (null == helpUrl) {
                throw new ValidationException("null helpUrl");
            }
            if (null == version) {
                throw new ValidationException("null version");
            }
            if (null == props) {
                throw new ValidationException("null properties");
            }
            if (null == appName) {
                throw new ValidationException("null appName");
            }

            return new SwingBaseModule(appName, version, helpUrl, props, profile);
        }
    }

    /**
     * Performs swingbase setup and teardown.
     * At start-time - just load the BaseData properties
     * from storage.
     */
    public static class Activator implements BundleActivator {

        private final BaseData data;
        private final SwingBaseTool tool;

        @Inject
        public Activator(BaseData data, SwingBaseTool tool) {
            this.data = data;
            this.tool = tool;
        }

        @Override
        public void start(BundleContext bc) {
            try {
                tool.loadAndApplySavedProps(data);
            } catch (Exception ex) {
                log.log(Level.WARNING, "Failed to load saved session properties", ex);
            }
        }

        @Override
        public void stop(BundleContext bc) throws Exception {
        }
    }
    private final String appName;
    private final String version;
    private final URL helpUrl;
    private final Properties defaultProperties;

    /**
     * Inject swingbase.BaseData properties - binds BaseData.* @Named
     * constants at configure time.
     */
    private SwingBaseModule(String appName, String version,
            URL helpUrl, Properties defaultProperties,
            AppBootstrap.AppProfile profile) {
        super(profile);
        this.appName = appName;
        this.version = version;
        this.helpUrl = helpUrl;
        this.defaultProperties = defaultProperties;
    }

    @Provides
    @Singleton
    public Feedback provideFeedback(FeedbackBundle fbBundle) {
        return fbBundle.getFeedback();
    }

    @Provides
    @Singleton
    public BaseData provideBaseData(BaseData.BDBuilder dataBuilder) {
        return dataBuilder.appName(appName).version(version).
                helpUrl(helpUrl).
                putAllProps(defaultProperties).build();
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(FeedbackBundle.class).in(Scopes.SINGLETON);
    }

    @Override
    public Class<Activator> getActivator() {
        return Activator.class;
    }
}
