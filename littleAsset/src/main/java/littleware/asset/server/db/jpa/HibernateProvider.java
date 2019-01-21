package littleware.asset.server.db.jpa;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;


@Singleton
public class HibernateProvider implements Provider<EntityManagerFactory> {
    private static final Logger log = Logger.getLogger(HibernateProvider.class.getName());
    private final DataSource dataSource;
    private final String dataSourceUrl;
    private EntityManagerFactory emFactory = null;


    @Inject
    public HibernateProvider(@Named("datasource.littleware") DataSource dsource,
            @Named("datasource.littleware") String dataSourceUrl) {
        this.dataSource = dsource;
        this.dataSourceUrl = dataSourceUrl;
    }

    
    /**
     * Extract a list of
     * SQL DDL commands (CREATE TABLE, CREATE INDEX, ...)
     * from the given reader
     * to initialize the db
     */
    public static List<String> loadDDLCommands(BufferedReader reader) throws IOException {
        final List<String> result = new ArrayList<>();
        final StringBuilder sb = new StringBuilder();
        int state = 0;

        for (String line = reader.readLine();
                    line != null;
                    line = reader.readLine()
        ) {
            final String trim = line.trim();
            if (0 == state && trim.startsWith("CREATE ")) {
                sb.setLength(0);
                state = 1;
            }
            if (1 == state) {
                sb.append("\n").append(trim);

                if (trim.endsWith(";")) {
                    // drop trailing ;
                    sb.setLength(sb.length() - 1);
                    result.add(sb.toString().trim());
                    state = 0;                   
                }
            }
        }
        return result;
    }

    /**
     * Return a list of
     * SQL DDL commands (CREATE TABLE, CREATE INDEX, ...)
     * to initialize the db
     */
    public static List<String> loadDDLCommands() {
        try (
            InputStream istream = HibernateProvider.class.getClassLoader().getResourceAsStream("littleware/asset/server/db/ddl/derby/asset.sql");
        ) {
            if (istream != null) {
                return loadDDLCommands(
                    new BufferedReader(
                        new InputStreamReader(istream, "UTF-8")
                    )
                );
            }                    
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load ddl resource", ex);
        }
        throw new IllegalStateException("Failed to load ddl resource");
    }

    private static boolean tablesCreated = false;

    /**
     * Only necessary to invoke once - issues DDL commands
     * against schema to create tables, indexes, etc
     */
    private void createTables() {
        if (tablesCreated) {
            log.warning("Multiple calls to createTables");
            return;
        }
        // only get to do this once - cannot handle partial success well
        tablesCreated = true;
        final List<String> commandList = loadDDLCommands();
        if (!commandList.isEmpty()) {
            try (
                final Connection cn = this.dataSource.getConnection();
                final var stmt = cn.createStatement();
            ) {
                commandList.stream().forEach(
                    (String command) -> {
                        try {
                            stmt.execute(command);
                        } catch (SQLException ex) {
                            throw new IllegalStateException("Failed to execute setup command: " + command, ex);
                        }
                    }
                );
            } catch (SQLException ex) {
                throw new IllegalStateException("Unable to connect to database", ex);
            }
        }
    }

    @Override
    public EntityManagerFactory get() {
        if (null == emFactory) {
            synchronized(this) {
                if (null == emFactory) {
                    this.createTables();
                    LittleDriver.setDataSource( dataSource );
                    LittleContext.setDataSource( dataSource );
                    //if ( null == System.getProperty(  "java.naming.factory.initial" ) ) {
                        System.setProperty( "java.naming.factory.initial", LittleContext.Factory.class.getName() );
                    //}
                    
                    emFactory = Persistence.createEntityManagerFactory( "littlewarePU" );
                }
            }
        }
        return emFactory;
    }
}
