package enigma;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author TREESA
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        if (cycles.length() == 0) {
            _permutations = cycles;
        }
        cycles = cycles.replaceAll("\\s+", "");

        boolean withinParentheses = false;
        for (int i = 0; i < cycles.length(); i++) {
            if (cycles.charAt(i) == '(') {
                if (withinParentheses) {
                    throw new EnigmaException("invalid cycle");
                }
                withinParentheses = true;
            } else if (cycles.charAt(i) == ')') {
                if (!withinParentheses) {
                    throw new EnigmaException("invalid cycle");
                }
                withinParentheses = false;
            } else if (!_alphabet.contains(cycles.charAt(i))) {
                throw new EnigmaException("letter not contained "
                       + "within alphabet");
            }
        }
        _permutations = cycles;
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        _permutations = _permutations.concat(cycle);
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        int permuteIndex = wrap(p);
        char permuteChar = _alphabet.toChar(permuteIndex);
        for (int i = 0; i < _permutations.length(); i++) {
            if (_permutations.charAt(i) == permuteChar) {
                if (_permutations.charAt(i + 1) == ')') {
                    for (int backwards = i; backwards >= 0; backwards--) {
                        if (_permutations.charAt(backwards) == '(') {
                            return _alphabet.toInt(
                                    _permutations.charAt(backwards + 1));
                        }
                    }
                }
                return _alphabet.toInt(_permutations.charAt(i + 1));
            }
        }
        return permuteIndex;
    }


    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        int invertIndex = wrap(c);
        char invertChar = _alphabet.toChar(invertIndex);
        for (int i = 0; i < _permutations.length(); i++) {
            if (_permutations.charAt(i) == invertChar) {
                if (_permutations.charAt(i - 1) == '(') {
                    for (int frontwards = i;
                         frontwards < _permutations.length(); frontwards++) {
                        if (_permutations.charAt(frontwards) == ')') {
                            return _alphabet.toInt(
                                    _permutations.charAt(frontwards - 1));
                        }
                    }
                }
                return _alphabet.toInt(_permutations.charAt(i - 1));
            }
        }
        return invertIndex;
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        for (int i = 0; i < _permutations.length(); i++) {
            if (_permutations.charAt(i) == p) {
                if (_permutations.charAt(i + 1) == ')') {
                    for (int backwards = i; backwards >= 0; backwards--) {
                        if (_permutations.charAt(backwards) == '(') {
                            return _permutations.charAt(backwards + 1);
                        }
                    }
                }
                return _permutations.charAt(i + 1);
            }
        }
        return p;
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        for (int i = 0; i < _permutations.length(); i++) {
            if (_permutations.charAt(i) == c) {
                if (_permutations.charAt(i - 1) == '(') {
                    for (int frontwards = i;
                         frontwards < _permutations.length(); frontwards++) {
                        if (_permutations.charAt(frontwards) == ')') {
                            return _permutations.charAt(frontwards - 1);
                        }
                    }
                }
                return _permutations.charAt(i - 1);
            }
        }
        return c;
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (int i = 0; i < _alphabet.size(); i++) {
            if (!_permutations.contains(
                    Character.toString(_alphabet.toChar(i)))) {
                return false;
            }
        }
        return true;
    }


    /** Alphabet of this permutation. */
    protected Alphabet _alphabet;

/** String containing the permutation. */
    private String _permutations;
}
