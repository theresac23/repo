package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import java.util.ArrayList;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author
 */
public class MachineTest {
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    private Machine _machine;
    private ArrayList<Rotor> _allRotors = new ArrayList<>();

    private void setMachine(Alphabet alpha, int numRotors, int numPawls) {

        Rotor rotorI = new MovingRotor("I",
                new Permutation(
                        "(AELTPHQXRU) (BKNW) (CMOY) (DFG) (IV) (JZ) (S)",
                        new Alphabet()), "Q");
        Rotor rotorII = new MovingRotor("II",
                new Permutation(
                        "(FIXVYOMW) (CDKLHUP) (ESZ) (BJ) (GR) (NT) (A) (Q)",
                        new Alphabet()), "E");
        Rotor rotorIII = new MovingRotor("III",
                new Permutation(
                        "(ABDHPEJT) (CFLVMZOYQIRWUKXSG) (N)",
                        new Alphabet()), "V");
        Rotor rotorIV = new MovingRotor("IV",
                new Permutation(
                        "(AEPLIYWCOXMRFZBSTGJQNH) (DV) (KU)",
                        new Alphabet()), "J");
        Rotor rotorVI = new MovingRotor("VI",
                new Permutation(
                        "(AJQDVLEOZWIYTS) (CGMNHFUX) (BPRK)",
                        new Alphabet()), "ZM");
        Rotor rotorBeta = new FixedRotor("Beta",
                new Permutation(
                        "(ALBEVFCYODJWUGNMQTZSKPR) (HIX)",
                        new Alphabet()));
        Rotor reflectorB = new Reflector("ReflectorB",
                new Permutation(
                        "(AE) (BN) (CK) (DQ) (FU) (GY) "
                                + "(HW) (IJ) (LO) (MP) (RX) (SZ) (TV)",
                        new Alphabet()));
        Rotor reflectorC = new Reflector("ReflectorC",
                new Permutation(
                        "(AR) (BD) (CO) (EJ) (FN) (GT) "
                                + "(HK) (IV) (LM) (PW) (QZ) (SX) (UY)",
                        new Alphabet()));

        _allRotors.add(rotorI);
        _allRotors.add(rotorII);
        _allRotors.add(rotorIII);
        _allRotors.add(rotorIV);
        _allRotors.add(rotorVI);
        _allRotors.add(rotorBeta);
        _allRotors.add(reflectorB);
        _allRotors.add(reflectorC);

        _machine = new Machine(alpha, numRotors, numPawls, _allRotors);
    }

    private void setSimpleMachine(Alphabet alpha, int numRotors, int numPawls) {
        Rotor simple1 = new MovingRotor("simple1",
                new Permutation("", new Alphabet("ABC")), "C");
        Rotor simple2 = new MovingRotor("simple2",
                new Permutation("", new Alphabet("ABC")), "C");
        Rotor simple3 = new MovingRotor("simple3",
                new Permutation("", new Alphabet("ABC")), "C");
        Rotor simpleReflector = new Reflector("simpleReflector",
                new Permutation("", new Alphabet("ABC")));

        _allRotors.add(simple1);
        _allRotors.add(simple2);
        _allRotors.add(simple3);
        _allRotors.add(simpleReflector);

        _machine = new Machine(alpha, numRotors, numPawls, _allRotors);
    }

    @Test
    public void testInsertRotors() {
        setMachine(UPPER, 5, 3);
        String[] rotorStrings = {"ReflectorC", "Beta", "VI", "II", "I"};
        _machine.insertRotors(rotorStrings);
        Rotor[] actual = new Rotor[5];
        int i = 0;
        for (Rotor r : _allRotors) {
            if (r.name().equals(rotorStrings[i])) {
                actual[i] = r;
                i++;
            }
        }
    }

    @Test
    public void testsetRotors1() {
        setMachine(UPPER, 4, 2);
        String[] rotorString1 = {"ReflectorC", "Beta", "II", "I"};
        _machine.insertRotors(rotorString1);
        _machine.setRotors("ABA");
        assertEquals(_machine.rotorSlots[0].setting(), 0);
        assertEquals(_machine.rotorSlots[1].setting(), 0);
        assertEquals(_machine.rotorSlots[2].setting(), 1);
        assertEquals(_machine.rotorSlots[3].setting(), 0);
    }


    @Test
    public void testConvert1() {
        setMachine(UPPER, 5, 3);
        String[] rotorString2 = {"ReflectorB", "Beta", "III", "IV", "I"};
        _machine.insertRotors(rotorString2);
        _machine.setRotors("AXLE");
        _machine.setPlugboard(new Permutation("(RP)(HQ)", UPPER));
        assertEquals(_machine.rotorSlots[4].setting(), 4);
        assertEquals(_machine.convert("F"), "Q");
    }

    @Test
    public void testConvert2() {
        setMachine(UPPER, 5, 3);
        String[] rotorString2 = {"ReflectorB", "Beta", "III", "IV", "I"};
        _machine.insertRotors(rotorString2);
        _machine.setRotors("AXLE");
        _machine.setPlugboard(new Permutation("(RP) (HQ)", UPPER));
        assertEquals(_machine.convert("Q"), "F");
    }

    @Test
    public void testConvert3() {
        setMachine(UPPER, 5, 3);
        String[] rotorString2 = {"ReflectorB", "Beta", "III", "IV", "I"};
        _machine.insertRotors(rotorString2);
        _machine.setRotors("AAAA");
        _machine.setPlugboard(new Permutation("(AB)(CD)", UPPER));
        assertEquals(_machine.convert("A"), "U");
    }

    @Test
    public void testConvert4() {
        setMachine(UPPER, 5, 3);
        String[] rotorString2 = {"ReflectorB", "Beta", "III", "IV", "I"};
        _machine.insertRotors(rotorString2);
        _machine.setRotors("AXLE");
        _machine.setPlugboard(new Permutation("(AB)(CD)", UPPER));
        assertEquals(_machine.convert("CIPHER"), "XXTMUU");
    }

    @Test
    public void testAdvancement() {
        setSimpleMachine(new Alphabet("ABC"), 4, 3);
        String[] rotorString = {"simpleReflector",
                                "simple1", "simple2", "simple3"};
        _machine.insertRotors(rotorString);
        _machine.setRotors("AAA");
        _machine.setPlugboard(new Permutation("", new Alphabet("ABC")));
        _machine.convert("A");
        assertEquals(_machine.rotorSlots[3].setting(), 1);
        assertEquals(_machine.rotorSlots[2].setting(), 0);
        assertEquals(_machine.rotorSlots[1].setting(), 0);
        assertEquals(_machine.rotorSlots[0].setting(), 0);
        _machine.convert("B");
        assertEquals(_machine.rotorSlots[3].setting(), 2);
        assertEquals(_machine.rotorSlots[2].setting(), 0);
        assertEquals(_machine.rotorSlots[1].setting(), 0);
        assertEquals(_machine.rotorSlots[0].setting(), 0);
        _machine.convert("C");
        assertEquals(_machine.rotorSlots[3].setting(), 0);
        assertEquals(_machine.rotorSlots[2].setting(), 1);
        assertEquals(_machine.rotorSlots[1].setting(), 0);
        assertEquals(_machine.rotorSlots[0].setting(), 0);
        _machine.convert("D");
        assertEquals(_machine.rotorSlots[3].setting(), 1);
        assertEquals(_machine.rotorSlots[2].setting(), 1);
        assertEquals(_machine.rotorSlots[1].setting(), 0);
        assertEquals(_machine.rotorSlots[0].setting(), 0);
        _machine.convert("E");
        assertEquals(_machine.rotorSlots[3].setting(), 2);
        assertEquals(_machine.rotorSlots[2].setting(), 1);
        assertEquals(_machine.rotorSlots[1].setting(), 0);
        assertEquals(_machine.rotorSlots[0].setting(), 0);
        _machine.convert("F");
        assertEquals(_machine.rotorSlots[3].setting(), 0);
        assertEquals(_machine.rotorSlots[2].setting(), 2);
        assertEquals(_machine.rotorSlots[1].setting(), 0);
        assertEquals(_machine.rotorSlots[0].setting(), 0);
        _machine.convert("G");
        assertEquals(_machine.rotorSlots[3].setting(), 1);
        assertEquals(_machine.rotorSlots[2].setting(), 0);
        assertEquals(_machine.rotorSlots[1].setting(), 1);
        assertEquals(_machine.rotorSlots[0].setting(), 0);
        _machine.convert("H");
        assertEquals(_machine.rotorSlots[3].setting(), 2);
        assertEquals(_machine.rotorSlots[2].setting(), 0);
        assertEquals(_machine.rotorSlots[1].setting(), 1);
        assertEquals(_machine.rotorSlots[0].setting(), 0);
        _machine.convert("I");
        assertEquals(_machine.rotorSlots[3].setting(), 0);
        assertEquals(_machine.rotorSlots[2].setting(), 1);
        assertEquals(_machine.rotorSlots[1].setting(), 1);
        assertEquals(_machine.rotorSlots[0].setting(), 0);
        _machine.convert("J");
        assertEquals(_machine.rotorSlots[3].setting(), 1);
        assertEquals(_machine.rotorSlots[2].setting(), 1);
        assertEquals(_machine.rotorSlots[1].setting(), 1);
        assertEquals(_machine.rotorSlots[0].setting(), 0);
        _machine.convert("K");
        assertEquals(_machine.rotorSlots[3].setting(), 2);
    }
}


