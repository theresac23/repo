package gitlet;

import ucb.junit.textui;
import org.junit.Test;


/** The suite of all JUnit tests for the gitlet package.
 *  @author
 */
public class UnitTest {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {

        textui.runClasses(UnitTest.class);
    }

    /** A dummy test to avoid complaint. */
    @Test
    public void placeholderTest() {
    }

    @Test
    public void init() {
        Main.main("init");
    }

    @Test
    public void add() {
        String[] toAdd = {"add", "wug.txt"};
        Main.main(toAdd);
    }

    @Test
    public void add1() {
        String[] toAdd = {"add", "notwug.txt"};
        Main.main(toAdd);
    }

    @Test
    public void commit() {
        String[] toCommit = {"commit", "help"};
        Main.main(toCommit);
    }

    @Test
    public void testFind() {
        String[] toFind = {"find", "help"};
        Main.main(toFind);
    }


    @Test
    public void testRemove() {

    }
}


