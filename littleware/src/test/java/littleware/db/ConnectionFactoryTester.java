package littleware.db;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.sql.*;
import javax.sql.DataSource;
import littleware.test.LittleTest;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * Little test case for connection factory
 */
public class ConnectionFactoryTester {

    private static final Logger log = Logger.getLogger(ConnectionFactoryTester.class.getName());
    private static final String TEST_QUERY = "SELECT 'hello' FROM asset";

    private final DataSource dsource;
    private final DataSourceHandler proxyHandler;

    /**
     * Constructor takes a connection factory and a test query to run against a
     * checked out connection. The supplied query should just return 'Hello'.
     *
     * @param dsource to test against
     * @param proxyHandler to test against
     */
    @Inject
    public ConnectionFactoryTester(
            @Named("datasource.littleware") DataSource dsource,
            @Named("datasource.littleware") DataSourceHandler proxyHandler
    ) {
        this.dsource = dsource;
        this.proxyHandler = proxyHandler;
    }

    /**
     * Run a test query - note: only works against some databases ...
     */
    @Test
    public void testQuery() {
        try (Connection conn = dsource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rset = stmt.executeQuery(TEST_QUERY)) {

                    assertTrue("Resultset not empty from test query: " + TEST_QUERY,
                            rset.next());
                    assertTrue("Resultset.getString(1) == Hello",
                            "Hello".equalsIgnoreCase(rset.getString(1))
                    );
                    log.log(Level.INFO, "Test query worked, got: {0}", rset.getString(1));
                }
            }
        } catch (Exception ex) {
            LittleTest.handle(ex);
        }
    }

    /**
     * Test that our injected DataSource is actually a dynamic-proxy around our
     * injected DataSourceHandler
     */
    @Test
    public void testProxy() {
        // Ok - test the proxy stuff
        assertTrue("DataSource != proxyHandler.getDataSource b/c it's a proxy!",
                proxyHandler.getDataSource() != dsource
        );
        final DataSource remember = proxyHandler.getDataSource();
        final String rememberUrl = proxyHandler.getJdbcUrl();
        try {
            proxyHandler.setDataSource(null, "bla");
            // should get null-pointer exception via proxy DataSource now
            try {
                dsource.getConnection();
                fail("Injected data source should be a proxy with our proxyHandler");
            } catch (Exception ex) {
            }
        } finally {
            proxyHandler.setDataSource(remember, rememberUrl);
        }
    }
}
