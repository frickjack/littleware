package littleware.db;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import littleware.base.validate.ValidationException;
import littleware.db.DataSourceHandler.DSHBuilder;
// add this back in when we have good public source for the driver :import oracle.jdbc.pool.OracleDataSource;

/**
 * General DataSourceBuilder implementation just resolves
 * jdbc URL to an appropriate db-specific DataSource implementation.
 */
public class GeneralDSHBuilder implements DataSourceHandler.DSHBuilder {

    private String url = null;
    private String name = "";

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public final void setUrl(String value) {
        url(value);
    }

    @Override
    public DataSourceHandler.DSHBuilder url(String value) {
        this.url = value;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public final void setName(String value) {
        name(value);
    }

    @Override
    public DSHBuilder name(String value) {
        name = value;
        return this;
    }

    private static class Handler implements DataSourceHandler {

        private DataSource dsource;
        private String jdbcUrl;

        public Handler(DataSource dsource, String jdbcUrl ) {
            this.dsource = dsource;
            this.jdbcUrl = jdbcUrl;
        }

        @Override
        public DataSource getDataSource() {
            return dsource;
        }

        @Override
        public void setDataSource(DataSource value, String jdbcUrl ) {
            this.dsource = value;
            this.jdbcUrl = jdbcUrl.trim();
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // TODO - inject JavaSimon monitoring
            return method.invoke(dsource, args);
        }

        @Override
        public String getJdbcUrl() {
            return jdbcUrl;
        }

        @Override
        public Optional<DataSource> resetIfNecessary(String newJdbcUrl, DSHBuilder builder) {
            final String cleanUrl = newJdbcUrl.trim();
            if( cleanUrl.equalsIgnoreCase( getJdbcUrl() ) ) {
                return Optional.empty();
            }
            final Optional<DataSource> result = Optional.ofNullable( dsource );
            setDataSource( builder.url( cleanUrl ).build().getDataSource(), cleanUrl );
            return result;
        }
    }

    @Override
    public DataSourceHandler build() {
        if (null == url) {
            throw new ValidationException("Null URL");
        }

        if (url.startsWith("jdbc:derby:")) {
            // figure out if its an embedded or network datasource
            final Pattern pattern = Pattern.compile("jdbc:derby://(\\w+):(\\d+)/(.+)$");
            final Matcher match = pattern.matcher(url);
            if (match.find()) {
                final org.apache.derby.jdbc.ClientDataSource data = new org.apache.derby.jdbc.ClientDataSource();
                data.setServerName ( match.group(1) );
                data.setPortNumber( Integer.parseInt( match.group(2) ) );
                data.setDatabaseName ( match.group(3) );
                return new Handler(data,url);
            } else {
                // TODO: define derby.system.home: https://db.apache.org/derby/docs/10.0/manuals/develop/develop12.html#HDRSII-DEVELOP-13018
                final org.apache.derby.jdbc.EmbeddedDataSource data = new org.apache.derby.jdbc.EmbeddedDataSource();
                data.setDatabaseName(url.substring("jdbc:derby:".length()));
                return new Handler(data, url);
            }
        } else if (url.startsWith("jdbc:oracle:")) {
            try {
              /*
                final OracleDataSource data = new OracleDataSource();
                data.setURL(url);
                return new Handler(data, url);
                */
              throw new UnsupportedOperationException( "Orale support removed until Oracle publishes driver for maven access");
            } catch (Exception ex) {
                throw new IllegalArgumentException("Failed ORACLE setup " + url, ex);
            }
        } else if (url.startsWith("jdbc:postgresql:")) {
            final org.postgresql.ds.PGSimpleDataSource data = new org.postgresql.ds.PGSimpleDataSource();

            String s_user = "littleware_user";
            String s_password = "secret";

            // set the user/password if possible
            for (String s_param : url.split("\\?\\&")) {
                if (s_param.startsWith("user=")) {
                    s_user = s_param.substring("user=".length());
                } else if (s_param.startsWith("password=")) {
                    s_password = s_param.substring("password=".length());
                }
            }
            final java.net.URI uri;
            try {
              uri = new java.net.URI( url.substring( "jdbc:".length() ));
            } catch( java.net.URISyntaxException ex ) {
              throw new IllegalArgumentException( "Failed parsing jdbc url: " + url, ex );
            }
            data.setUser(s_user);
            data.setPassword(s_password);
            data.setServerName( uri.getHost() );
            data.setPortNumber( uri.getPort() );
            data.setDatabaseName( uri.getPath().replaceAll( "^/+", "" ));
            //data.setMaximumConnectionCount(10);
            //data.setMaximumActiveTime(60000);
            return new Handler(data,url);
        } else if (url.startsWith("jdbc:mysql:")) {
            final com.mysql.jdbc.jdbc2.optional.MysqlDataSource data = new com.mysql.jdbc.jdbc2.optional.MysqlDataSource();
            data.setURL(url);
            return new Handler(data,url);
        } else if (url.startsWith("jndi:")) {
            try {
                final DataSource data = (DataSource) new InitialContext().lookup(url.substring("jndi:".length()));
                return new Handler(data,url);
            } catch (NamingException ex) {
                throw new IllegalArgumentException("Failed JNDI lookup for " + url, ex);
            }
        } else {
            throw new IllegalArgumentException("Not autobinding datasource of unknown type: " + name + " - " + url);
        }

    }
}
