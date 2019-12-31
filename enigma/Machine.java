package enigma;

import java.util.Collection;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author TREESA
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        if (numRotors < 1) {
            throw error("Not enough rotors");
        }
        if (pawls >= numRotors) {
            throw error("Too many pawls");
        }
        _numRotors = numRotors;
        _numPawls = pawls;
        _allRotors = allRotors;
        rotorSlots = new Rotor[_numRotors];
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _numPawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        if (rotors.length != _numRotors) {
            throw error("Wrong number of rotor names");
        }
        for (Rotor r : _allRotors) {
            if (r.name().equals(rotors[0])) {
                if (r instanceof Reflector) {
                    rotorSlots[0] = r;
                } else {
                    throw error("First rotor is not a reflector");
                }
            }
        }
        for (int i = 1; i < _numRotors - _numPawls; i++) {
            for (Rotor r : _allRotors) {
                if (r.name().equals(rotors[i])) {
                    if (r instanceof FixedRotor) {
                        rotorSlots[i] = r;
                    } else {
                        throw error("This slot should have a fixed rotor");
                    }
                }
            }
        }
        for (int i = _numRotors - _numPawls; i < rotorSlots.length; i++) {
            for (Rotor r : _allRotors) {
                if (r.name().equals(rotors[i])) {
                    if (r instanceof MovingRotor) {
                        rotorSlots[i] = r;
                    } else {
                        throw error("This slot should have a moving rotor");
                    }
                }
            }
        }
        for (int i = 0; i < rotorSlots.length; i++) {
            if (rotorSlots[i] == null) {
                throw error("Name in rotors string not within allRotors");
            }
        }
    }



    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != rotorSlots.length - 1) {
            throw error("Wrong number of settings");
        }
        int si = 0;
        for (int i = 1; i < rotorSlots.length; i++) {
            rotorSlots[i].set(setting.charAt(si));
            si++;
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        if (c > _alphabet.size() - 1) {
            throw error("No letter corresponding to input c in alphabet");
        }
        rotorSlots[rotorSlots.length - 1].advance();
        int crossRotors = _plugboard.permute(c);
        for (int i = rotorSlots.length - 1; i >= 0; i--) {
            crossRotors = rotorSlots[i].convertForward(crossRotors);
        }
        for (int i = 1; i < rotorSlots.length; i++) {
            crossRotors = rotorSlots[i].convertBackward(crossRotors);
        }
        return _plugboard.invert(crossRotors);
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String encoded = "";
        msg = msg.replaceAll("\\s+", "");
        for (int i = 0; i < msg.length(); i++) {
            for (int r = rotorSlots.length - numPawls();
                 r < rotorSlots.length - 1; r++) {
                if (rotorSlots[r + 1].atNotch()) {
                    rotorSlots[r].advance();
                    if (rotorSlots[r + 1]
                            != rotorSlots[rotorSlots.length - 1]) {
                        rotorSlots[r + 1].advance();
                    }
                }
            }
            encoded = encoded + _alphabet.toChar(convert
                    (_alphabet.toInt(msg.charAt(i))));
        }
        return encoded;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;
    /** Number of pawls. */
    private int _numPawls;
    /** Number of rotors. */
    private int _numRotors;
    /** Array of my rotors. */
    protected Rotor[] rotorSlots;
    /** Collection of potential rotors. */
    private Collection<Rotor> _allRotors;
    /** My plugboard. */
    private Permutation _plugboard;
}
