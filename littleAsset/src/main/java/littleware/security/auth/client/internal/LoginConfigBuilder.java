/*
 * Copyright 2011 http://code.google.com/p/littleware
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.auth.client.internal;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import littleware.security.auth.client.ClientLoginModule;
import littleware.security.auth.client.SessionManager;

public class LoginConfigBuilder implements ClientLoginModule.ConfigurationBuilder {

    private final Map<String, Object> optionMap = new HashMap<String, Object>();
    
    public LoginConfigBuilder() {}
    
    @Inject
    public LoginConfigBuilder( SessionManager sessionManager ) {
        optionMap.put( ClientLoginModule.MANAGER_OPTION, sessionManager );
    }

    @Override
    public ClientLoginModule.ConfigurationBuilder host(String value) {
        optionMap.put(ClientLoginModule.HOST_OPTION, value);
        return this;
    }

    @Override
    public ClientLoginModule.ConfigurationBuilder port(int value) {
        optionMap.put(ClientLoginModule.PORT_OPTION, Integer.toString(value));
        return this;
    }

    @Override
    public ClientLoginModule.ConfigurationBuilder useCache(boolean value) {
        optionMap.put(ClientLoginModule.CACHE_OPTION, value ? "true" : "false");
        return this;
    }

    @Override
    public Configuration build() {
        final AppConfigurationEntry[] entry = {new AppConfigurationEntry(ClientLoginModule.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, ImmutableMap.copyOf(optionMap))};
        return new Configuration() {

            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                return entry;
            }
        };
    }
}
