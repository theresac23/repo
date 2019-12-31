package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author
 */
public class PermutationTest {

    /**
     * Testing time limit.
     */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /**
     * Check that perm has an alphabet whose size is that of
     * FROMALPHA and TOALPHA and that maps each character of
     * FROMALPHA to the corresponding character of FROMALPHA, and
     * vice-versa. TESTID is used in error messages.
     */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                    e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                    c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                    ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                    ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        perm = new Permutation("", UPPER);
        checkPerm("identity", UPPER_STRING, UPPER_STRING);
    }

    @Test
    public void checkpermute() {
        Permutation perm1 = new Permutation(
                "(ABC) (DEF)", new Alphabet());
        assertEquals(perm1.permute('C'), 'A');
    }

    @Test
    public void checkpermuteint() {
        Permutation perm2 = new Permutation(
                "(ABCZ) (DEF)", new Alphabet());
        assertEquals(perm2.permute(2), 25);
    }

    @Test
    public void checkpermuteint2() {
        Permutation perm3 = new Permutation(
                "(ABCZ) (DEF)", new Alphabet());
        assertEquals(perm3.permute(25), 0);
    }

    @Test
    public void invert() {
        Permutation perm4 = new Permutation("(ABCZ) (DEF)", new Alphabet());
        assertEquals(perm4.invert('Z'), 'C');
    }

    @Test
    public void invert2() {
        Permutation perm5 = new Permutation("(ABCZ) (DEF)", new Alphabet());
        assertEquals(perm5.invert('A'), 'Z');
    }
    @Test
    public void invertint() {
        Permutation perm6 = new Permutation("(ABCZ) (DEF)", new Alphabet());
        assertEquals(perm6.invert(1), 0);
    }
    @Test
    public void invertint2() {
        Permutation perm6 = new Permutation("(ABCZ) (DEF)", new Alphabet());
        assertEquals(perm6.invert(0), 25);
    }
    @Test
    public void testderangement() {
        Permutation perm6 = new Permutation("(ABCZ) (DEF)", new Alphabet());
        assertEquals(perm6.derangement(), false);
    }
    @Test
    public void testderangement2() {
        Permutation perm6 = new Permutation(
                "(QWERTYUIOPLKJHGFDSAZXCVBNM)", new Alphabet());
        assertEquals(perm6.derangement(), true);
    }
}
