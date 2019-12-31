package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author TREESA
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine newEnigma = readConfig();
        String configLine = _input.nextLine();

        if (configLine.charAt(0) != '*') {
            throw error("wrong input format, missing *");
        }

        while (_input.hasNextLine()) {
            setUp(newEnigma, configLine);
            String inputLine =  _input.nextLine();
            while (!inputLine.contains("*")) {
                if (inputLine.isEmpty()) {
                    _output.println();
                }
                if (_input.hasNextLine()) {
                    String inputMessage = inputLine.replaceAll("\\s+", "")
                            .toUpperCase();
                    printMessageLine(newEnigma.convert(inputMessage));
                    inputLine = _input.nextLine();
                } else {
                    String inputMessage = inputLine.replaceAll("\\s+", "")
                            .toUpperCase();
                    printMessageLine(newEnigma.convert(inputMessage));
                    break;
                }
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            String firstLine = _config.next();
            if (firstLine.contains("*") || firstLine.contains("(")
                || firstLine.contains(")") || firstLine.contains(" ")) {
                throw error("incompatible characters in alphabet");
            }

            _alphabet = new Alphabet(firstLine);
            if (!_config.hasNextInt()) {
                throw new EnigmaException("Should be number of rotors");
            }

            int numRotors = _config.nextInt();
            if (!_config.hasNextInt()) {
                throw new EnigmaException("Should be number of pawls");
            }

            int numPawls = _config.nextInt();

            _name = _config.next().toUpperCase();
            while (_config.hasNext()) {
                notches = _config.next();
                mRotors.add(readRotor());
            }
            return new Machine(_alphabet, numRotors, numPawls, mRotors);

        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String currCycle = _config.next();
            String permutation = "";
            while (currCycle.contains("(")) {
                if (!currCycle.contains(")") || currCycle.contains("*")) {
                    throw error("Wrong cycle format");
                }
                if (_config.hasNext()) {
                    permutation = permutation + currCycle;
                    currCycle = _config.next();
                } else if (!_config.hasNext()) {
                    permutation = permutation + currCycle;
                    break;
                }
            }
            String tempname = _name.toUpperCase();
            if (_config.hasNext()) {
                _name = currCycle;
            }
            if (notches.charAt(0) == 'M') {
                return new MovingRotor(tempname,
                        new Permutation(permutation, _alphabet),
                        notches.substring(1));
            } else if (notches.charAt(0) == 'N') {
                return new FixedRotor(tempname,
                        new Permutation(permutation, _alphabet));
            } else {
                return new Reflector(tempname,
                        new Permutation(permutation, _alphabet));
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        Scanner scanSettings = new Scanner(settings);
        ArrayList<String> settingList = new ArrayList<>();
        int i = 0;
        while (scanSettings.hasNext()) {
            settingList.add(scanSettings.next());
            i++;
        }
        if (settingList.size() < M.numRotors() + 1) {
            throw error("too few items for machine configuration");
        }

        String[] rotorSlots = new String[M.numRotors()];
        for (int r = 1; r < M.numRotors() + 1; r++) {
            rotorSlots[r - 1] = settingList.get(r).toUpperCase();
        }
        M.insertRotors(rotorSlots);

        M.setRotors(settingList.get(M.numRotors() + 1));

        String plugboardString = "";
        if (settingList.size() > rotorSlots.length + 2) {
            for (int p = rotorSlots.length + 2; p < settingList.size(); p++) {
                plugboardString = plugboardString + settingList.get(p);
            }
        }
        M.setPlugboard(new Permutation(plugboardString, _alphabet));
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        String wholeMessage = "";
        for (int i = 0; i < msg.length(); i += 5) {
            if (msg.length() - i > 5) {
                wholeMessage = msg.substring(i, i + 5) + " ";
                _output.print(wholeMessage);
            } else {
                wholeMessage = msg.substring(i, msg.length());
                _output.println(wholeMessage);
            }
        }
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** Collection of all potential rotors. */
    private Collection<Rotor> mRotors = new ArrayList<>();

    /** Notches of a rotor. */
    private String notches;

    /** Name of input and config files. */
    private String _name;
}
