package littleware.base;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Session;
import javax.naming.InitialContext;

/**
 * Configure the GUICE binding String constants according
 * to the constructor injected Properties.
 * If a mail.smtp.host property is set, then bind javax.mail.Session
 * to a provider that sends mail via SMTP to the host.
 */
public class PropertiesGuice implements Module {

    private static final Logger log = Logger.getLogger(PropertiesGuice.class.getName());
    private final Properties properties;

    protected PropertiesGuice(Properties prop) {
        properties = prop;
    }


    /** 
     * Factory method
     * 
     * @param props to back the new Module with
     */
    public static PropertiesGuice build( Properties props ) {
        return new PropertiesGuice( props );
    }
    /**
     * Shortcut injects this( PropertiesLoader.loadProps( propPath )
     */
    public static PropertiesGuice build(String propPath) throws IOException {
        return build(PropertiesLoader.get().loadProperties(propPath));
    }

    /** Shortcut to PropertiesLoader.loadProperties() */
    public static PropertiesGuice build() throws IOException {
        return build(PropertiesLoader.get().loadProperties());
    }
    
    /** Shortcut to PropertiesLoader.loadProperties() */
    public static PropertiesGuice build( Class<?> propClass ) throws IOException {
        return build(PropertiesLoader.get().loadProperties( propClass ));
    }

    /**
     * Get an editable reference to the properties underlying
     * this module.  The properties may be edited/overriden
     * before Guice configuration.
     */
    public Properties getProperties() {
        return properties;
    }


    /**
     * Guice bind a @Named key constant to the given value.
     * If sValue starts with "int." prefix, then also bind an integer value.
     *
     * @param binder for guice
     * @param sKey
     * @param sValue
     */
    public void bindKeyValue(Binder binder, String sKey, String sValue) {
        binder.bindConstant().annotatedWith(Names.named(sKey)).to(sValue);
        final String clean = sValue.trim();
        if (clean.startsWith("http://") || clean.startsWith( "https://" ) ) {
            try {
                final URL url = new URL(clean);
                binder.bind(URL.class).annotatedWith(Names.named(sKey)).toInstance(url);
            } catch (MalformedURLException ex) {
                log.log(Level.WARNING, "Failed to parse URL-like value: " + clean);
            }
        }
    }

    @Override
    public void configure(Binder binder) {
        for (String sKey : properties.stringPropertyNames()) {
            final String sValue = properties.getProperty(sKey);
            bindKeyValue(binder, sKey, sValue);
        }
        if (properties.containsKey("mail.smtp.host")) {
            try {
                final String host = properties.getProperty("mail.smtp.host");
                /**
                 * Setup lazy Session binding.
                 * The mail.smtp.host property may be set on a global properties file
                 * accessed by many apps that don't actually need to setup a mail session,
                 * so be lazy - only setup session if needed.
                 */
                final Provider<Session> provider = new Provider<Session>() {

                    private Session session = null;

                    @Override
                    public Session get() {
                        if (null != session) {
                            return session;
                        }
                        synchronized (this) {
                            if (null == session) {
                                if (host.startsWith("jndi:")) {
                                    try {
                                        session = (Session) (new InitialContext()).lookup(host.substring("jndi:".length()));
                                    } catch (Exception ex) {
                                        throw new AssertionFailedException("Failed jndi lookup for smtp host: " + host);
                                    }
                                } else {
                                    session = Session.getInstance(properties, null);
                                }
                            }
                        }

                        return session;
                    }
                };

                // then bind mail session
                binder.bind(Session.class).toProvider(provider).in(Scopes.SINGLETON);
            } catch (NoClassDefFoundError ex) {
                // Do not require the app to link with mail.jar if it doesn't actually need a Session
                log.log(Level.INFO, "Not binding javax.mail.Session - mail.jar not in classpath" );
                log.log(Level.FINE, "Not binding javax.mail.Session - mail.jar not in classpath", ex);
            }
        }
    }
}
