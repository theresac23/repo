package gitlet;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author tonkesa
 */
public class Main {
    /** Our working directory that will track files
     * and hold .gitlet directory. **/
    static final File WD = new File(System.getProperty("user.dir"));

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.print("Please enter a command.");
            System.exit(0);
        } else if (args[0].equals("init")) {
            initGit();
            System.exit(0);
        }
        if (Repo.GITLET.exists()) {
            switch (args[0]) {
            case "add":
                add(args);
                return;
            case "commit":
                commit(args);
                return;
            case "rm":
                remove(args);
                return;
            case "log":
                log(args);
                return;
            case "global-log":
                globalLog(args);
                return;
            case "find":
                find(args);
                return;
            case "status":
                status(args);
                return;
            case "checkout":
                checkout(args);
                return;
            case "branch":
                branch(args);
                return;
            case "rm-branch":
                removeB(args);
                return;
            case "reset":
                reset(args);
                return;
            case "merge":
                merge(args);
                return;
            case "printHeads":
                printHeads();
                return;
            case "printRemoves":
                printRemoves();
                return;
            default:
                System.out.print("No command with that name exists.");
                System.exit(0);
            }
        }
        System.out.print("Not in an initialized Gitlet directory.");
        System.exit(0);
    }

    /** Method. **/
    public static void printHeads() {
        @SuppressWarnings("unchecked")
        HashMap<String, String> headHM = Utils.readObject(Repo.HEADS,
                HashMap.class);
        System.out.print(headHM.keySet());
        System.exit(0);
    }
    /** Method. **/
    public static void printRemoves() {
        @SuppressWarnings("unchecked")
        HashSet<String> removes = Utils.readObject(new File(
                System.getProperty("user.dir")
                        + "/.gitlet/tracker/removes"), HashSet.class);
        System.out.print(removes);
        System.exit(0);
    }
    /** Method. **/
    public static void initGit() {
        if (Repo.GITLET.exists()) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
        } else {
            Repo.initialize();
        }
    }
    /** Method.
     * @param args **/
    private static void add(String[] args) {
        File toAdd = Utils.join(WD, args[1]);
        if (toAdd.exists()) {
            Repo.add(toAdd);
        } else {
            System.out.print("File does not exist.");
        }
        System.exit(0);
    }

    /** Method.
     * @param args **/
    private static void commit(String[] args) {
        if (args.length < 2 || args[1].equals("")) {
            System.out.print("Please enter a commit message.");
        } else {
            Repo.commit(args[1], null);
        }
        System.exit(0);
    }

    /** Method.
     * @param args **/
    private static void remove(String[] args) {
        Repo.remove(args[1]);
        System.exit(0);
    }

    /** Method.
     * @param args **/
    private static void log(String[] args) {
        Repo.log();
        System.exit(0);
    }

    /** Method.
     * @param args **/
    private static void globalLog(String[] args) {
        Repo.globalLog();
        System.exit(0);
    }

    /** Method.
     * @param args **/
    private static void find(String[] args) {
        Repo.find(args[1]);
        System.exit(0);
    }

    /** Method.
     * @param args **/
    private static void status(String[] args) {
        Repo.status();
        System.exit(0);
    }

    /** Method.
     * @param args **/
    private static void checkout(String[] args) {
        if (args.length == 3) {
            if (!args[1].equals("--")) {
                System.out.println("Incorrect operands.");
            } else {
                Repo.checkoutHead(args[2]);
            }
        } else if (args.length == 4) {
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
            } else {
                Repo.checkoutCommit(args[1], args[3]);
            }
        } else if (args.length == 2) {
            Repo.checkoutBranch(args[1]);
        }
        System.exit(0);
    }

    /** Method.
     * @param args **/
    private static void branch(String[] args) {
        Repo.branch(args[1]);
        System.exit(0);
    }

    /** Method.
     * @param args **/
    private static void removeB(String[] args) {
        Repo.removeB(args[1]);
        System.exit(0);
    }

    /** Method.
     * @param args **/
    private static void reset(String[] args) {
        Repo.reset(args[1]);
        System.exit(0);
    }

    /** Method.
     * @param args **/
    private static void merge(String[] args) {
        Repo.merge(args[1]);
        System.exit(0);
    }
}
