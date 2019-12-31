package enigma;
import java.util.HashMap;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author TREESA
 */
class Alphabet {
    /**
     * A new alphabet containing CHARS.  Character number #k has index
     * K (numbering from 0). No character may be duplicated.
     */

    Alphabet(String chars) {
        HashMap<Integer, Character> alphabetMap = new HashMap<>();
        for (int i = 0; i < chars.length(); i++) {
            if (!alphabetMap.containsValue(chars.charAt(i))) {
                alphabetMap.put(i, chars.charAt(i));
            }
        }
        _alphabetMap = alphabetMap;
    }

    /**
     * A default alphabet of all upper-case characters.
     */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /**
     * Returns the size of the alphabet.
     */
    int size() {
        return _alphabetMap.size();
    }

    /**
     * Returns true if preprocess(CH) is in this alphabet.
     */
    boolean contains(char ch) {
        for (Integer i : _alphabetMap.keySet()) {
            if (_alphabetMap.get(i) == ch) {
                return true;
            }
        }
        return false;
    }
    /**
     * Returns character number INDEX in the alphabet, where
     * 0 <= INDEX < size().
     */
    char toChar(int index) {
        return _alphabetMap.get(index);
    }

    /**
     * Returns the index of character preprocess(CH), which must be in
     * the alphabet. This is the inverse of toChar().
     */
    int toInt(char ch) {
        /**  Will return the index of the ch value if it exists in the hashmap.
             If not, will return -1.
         */
        for (Integer i : _alphabetMap.keySet()) {
            if (_alphabetMap.get(i) == ch) {
                return i;
            }
        }
        return -1;
    }

    /** Hashmap of all the alphabet, with integers as keys and characters
     * as values.
     */
    private HashMap<Integer, Character> _alphabetMap;
}
