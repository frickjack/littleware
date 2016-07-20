package littleware.base;

import java.util.Optional;
import static org.junit.Assert.assertTrue;
import org.junit.Test;


/**
 * Test the Whatever utility methods
 */
public class WhateverTester {

    public enum TestEnum { Uga, BooGa, GooGoo, Ga };

    @Test
    public void testWhatever() {
        final Optional<TestEnum> maybe = Whatever.get().findEnumIgnoreCase("booga", TestEnum.values() );
        assertTrue( "findEnumIgnoreCase ok", maybe.isPresent() && maybe.get().equals( TestEnum.BooGa ) );
        assertTrue( "findEnumIgnoreCase found empty", ! Whatever.get().findEnumIgnoreCase("frick", TestEnum.values() ).isPresent() );
        assertTrue( "equalsSafe ok", Whatever.get().equalsSafe(null, null) && (! Whatever.get().equalsSafe( "bla", null ) ) );
        assertTrue( "empty test ok", Whatever.get().empty(null) && Whatever.get().empty( "" ) && (! Whatever.get().empty("foo")));

        for( Whatever.Folder folder : Whatever.Folder.values() ) {
            assertTrue( "Folder exists " + folder + ": " + folder.getFolder(),
                    folder.getFolder().exists()
                    );
        }
    }
}
