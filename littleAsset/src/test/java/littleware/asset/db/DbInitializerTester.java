package littleware.asset.db;

import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

import org.junit.Test;

import littleware.asset.db.jpa.HibernateProvider;


public class DbInitializerTester {
    @Test
    public void testLoadDDLCommands() {
        try {
            final String testString = String.join(
                "\n",
                "CREATE TABLE bla(",
                ");",
                "CREATE INDEX frickjack;",
                "bla bla",
                "bla CREATE ; whatever"
            );
            final List<String> ddlList = HibernateProvider.loadDDLCommands(
                new BufferedReader(new StringReader(testString))
            );
            Assert.assertEquals("loaded 2 commands", 2, ddlList.size());
            for (String command: ddlList) {
                Assert.assertTrue("command stripped of ;: " + command, !command.endsWith(";"));
            }
        } catch (Exception ex) {
            fail("Caught unexpected: " + ex);
        }
    }

    @Test
    public void testLoadDDLResource() {
        try {
            final List<String> ddlList = HibernateProvider.loadDDLCommands();
            Assert.assertTrue("loaded ddl from jar resources: " + ddlList.size(), 1 < ddlList.size());
        } catch (Exception ex) {
            fail("Caught unexpected: " + ex);
        }
    }
}
