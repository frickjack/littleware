/*
 * Copyright 2013 http://frickjack.com
 * Code freely available subject to the terms of the LGPL 2.1
 */
package littleware.apps.fishRunner;

import com.google.inject.*;
import com.google.inject.name.Named;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.embeddable.*;

/**
 * Singleton factory for glassfish server with standard configuration listening
 * on port 8080 with a 'littleware' postgres datasource referencing the Heroku
 * database environment if any. The server should be ready to deploy a given
 * .war
 */
public class FishFactory implements Provider<GlassFish> {

    private static final Logger log = Logger.getLogger(FishFactory.class.getName());
    private GlassFish glassfish;
    private final URI dbURL;
    private final GlassFishProperties glassfishProperties;


    private void setupDbConnection() throws GlassFishException {
        // setup database connection pool
        final CommandRunner commandRunner = glassfish.getCommandRunner();

        {
            final String username = dbURL.getUserInfo().split(":")[0];
            final String password = dbURL.getUserInfo().split(":")[1];

            log.log(Level.INFO, "Registering postgres db connection pool with glassfish: {0}", dbURL);
            final CommandResult commandResult = commandRunner.run(
                    "create-jdbc-connection-pool",
                    "--datasourceclassname", "org.postgresql.ds.PGConnectionPoolDataSource",
                    "--restype", "javax.sql.ConnectionPoolDataSource",
                    "--property",
                    "User=" + username
                    + ":Password=" + password
                    + ":PortNumber=" + dbURL.getPort()
                    + ":DatabaseName=" + dbURL.getPath().replaceAll("^/+", "")
                    + ":ServerName=" + dbURL.getHost(),
                    "littlePool");

            if (commandResult.getExitStatus().equals(CommandResult.ExitStatus.FAILURE)) {
                throw new RuntimeException("Failed to setup jdbc/littleDB connection pool", commandResult.getFailureCause());
            }
        }

        {
            final CommandResult commandResult = commandRunner.run(
                    "create-jdbc-resource",
                    "--connectionpoolid=littlePool",
                    "jdbc/littleDB");
            if (commandResult.getExitStatus().equals(CommandResult.ExitStatus.FAILURE)) {
                throw new RuntimeException("Failed to setup jdbc/littleDB jdbc resource", commandResult.getFailureCause());
            }
        }

        { // enable gzip compression while we're at it
            final CommandResult commandResult = commandRunner.run(
                    "set",
                    "configs.config.server-config.network-config.protocols.protocol.http-listener.http.compression=on"
                    );
            if (commandResult.getExitStatus().equals(CommandResult.ExitStatus.FAILURE)) {
                throw new RuntimeException("Failed to configure gzip compression", commandResult.getFailureCause());
            }
        }

    }

    @Inject
    public FishFactory( @Named("DATABASE_URL") URI dbURL, GlassFishProperties glassfishProperties ) {
        this.dbURL = dbURL;
        this.glassfishProperties = glassfishProperties;
        try {
            glassfish = GlassFishRuntime.bootstrap().newGlassFish(glassfishProperties);
        } catch (GlassFishException ex) {
            throw new RuntimeException("glassfish startup failed", ex);
        }
    }

    @Override
    public GlassFish get() {
        try {
            if (!(glassfish.getStatus().equals(GlassFish.Status.STARTING)
                    || glassfish.getStatus().equals(GlassFish.Status.STARTED))) {
                glassfish.start();
                setupDbConnection();
            }

        } catch (GlassFishException ex) {
            throw new RuntimeException("Failure starting glassfish", ex);
        }
        return glassfish;
    }
}
