package littleware.base;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import littleware.test.LittleTest;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Just run UUIDFactory implementations through a simple test
 */
public class DynamicEnumTester {

    private static final Logger log = Logger.getLogger( DynamicEnumTester.class.getName() );

    private static class TestEnum1 extends DynamicEnum<TestEnum1> {

        protected TestEnum1() {
        }

        protected TestEnum1(UUID u_id, String s_name) {
            super(u_id, s_name, TestEnum1.class);
        }

        public static Set<TestEnum1> getMembers() {
            return DynamicEnum.getMembers(TestEnum1.class);
        }

        public static TestEnum1 getMember(String s_name) {
            return DynamicEnum.getMember(s_name, TestEnum1.class);
        }
        public static TestEnum1 MEMBER1 = new TestEnum1(UUIDFactory.parseUUID("00000000000000000000000000000000"),
                "MEMBER1");
        public static TestEnum1 MEMBER2 = new TestEnum1(UUIDFactory.parseUUID("00000000000000000000000000000001"),
                "MEMBER2");
    }

    private static class TestEnum2 extends DynamicEnum<TestEnum2> {

        protected TestEnum2() {
        }

        protected TestEnum2(UUID u_id, String s_name) {
            super(u_id, s_name, TestEnum2.class);
        }

        public static TestEnum2 getMember(String s_name) {
            return DynamicEnum.getMember(s_name, TestEnum2.class);
        }

        public static Set<TestEnum2> getMembers() {
            return DynamicEnum.getMembers(TestEnum2.class);
        }
        public static TestEnum2 MEMBER1 = new TestEnum2(UUIDFactory.parseUUID("00000000000000000000000000000004"),
                "MEMBER1");
    }

    

    /**
     * Just create a couple UUID's, then go back and
     * forth to the string representation
     */
    @Test
    public void testEnum() {
        try {
            assertTrue("Got 2 members of TestEnum1: " + TestEnum1.getMembers().size(),
                    TestEnum1.getMembers().size() == 2);
            assertTrue("Got 1 member of TestEnum2: " + TestEnum2.getMembers().size(),
                    TestEnum2.getMembers().size() == 1);
            final TestEnum1 n_member1 = TestEnum1.getMember("MEMBER1");
            assertTrue("testEnum1.getMember got expected member: " + n_member1,
                    n_member1.equals(TestEnum1.MEMBER1));
            final TestEnum2 n_member2 = TestEnum2.getMember("MEMBER1");
            assertTrue("testEnum2.getMember got expected member: " + n_member2,
                    n_member2.equals(TestEnum2.MEMBER1));

        } catch (Exception ex) {
            LittleTest.handle(ex);
        }
    }
}

