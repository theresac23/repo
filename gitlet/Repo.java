package gitlet;

import java.text.SimpleDateFormat;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.TimeZone;

/** Interaction with .gitlet.
 * @author theresachen **/

public class Repo {

    static final File GITLET = Utils.join(Main.WD, ".gitlet");

    static final File BRANCHES = Utils.join(GITLET, "branches");

    static final File STAGE = Utils.join(GITLET, "stage");

    static final File BLOBS = Utils.join(GITLET, "blobs");

    static final File TRACKER = Utils.join(GITLET, "tracker");

    static final File CURBRANCH = Utils.join(TRACKER, "curBranch");

    static final File HEADS = Utils.join(TRACKER, "heads");

    static final File SHALIST = Utils.join(TRACKER, "shaList");

    private static String getDate() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE MMM d HH:mm:ss YYYY Z");
        dateFormat.setTimeZone(TimeZone.getTimeZone("PST"));
        return dateFormat.format(date);
    }

    /** Initializing .gitlet directory. **/
    public static void initialize() {
        GITLET.mkdirs();
        BRANCHES.mkdir();
        STAGE.mkdir();
        BLOBS.mkdir();
        TRACKER.mkdir();

        String initTime = getDate();

        HashMap<String, String> empty = new HashMap<>();
        String[] parentSha = {"", null};
        Commit firstCommit = new Commit("initial commit",
                parentSha, initTime, empty);
        String head = firstCommit._sha;

        File commit = new File(System.getProperty("user.dir")
                + "/.gitlet/branches/" + head);
        Utils.writeObject(commit, firstCommit);

        HashSet<String> toRemove = new HashSet<String>();
        File toRemoveF = Utils.join(TRACKER, "removes");
        Utils.writeObject(toRemoveF, toRemove);

        HashMap<String, String> heads = new HashMap<String, String>();
        heads.put("master", firstCommit._sha);
        Utils.writeObject(HEADS, heads);

        String curBranchS = "master";
        Utils.writeObject(CURBRANCH, curBranchS);

        HashSet<String> shaList = new HashSet<String>();
        shaList.add(firstCommit._sha);
        Utils.writeObject(SHALIST, shaList);
    }

    /** Staging a file. **/
    public static void add(File toAdd) {
        File removesF = new File(System.getProperty("user.dir")
                + "/.gitlet/tracker/removes");
        @SuppressWarnings("unchecked")
        HashSet<String> removes = Utils.readObject(removesF, HashSet.class);
        if (removes.contains(toAdd.getName())) {
            removes.remove(toAdd.getName());
            Utils.writeObject(removesF, removes);
        } else {

            String curBranch = Utils.readObject(CURBRANCH, String.class);
            @SuppressWarnings("unchecked")
            HashMap<String, String> headHM = Utils.readObject(HEADS,
                    HashMap.class);
            String headCS = headHM.get(curBranch);
            File tmpFile = new File(System.getProperty("user.dir")
                    + "/.gitlet/branches/" + headCS);
            Commit headC = Utils.readObject(tmpFile, Commit.class);

            String fileName = toAdd.getName();
            String fileContent = Utils.sha1(Utils.readContentsAsString(toAdd));
            if (headC._blobs.containsKey(fileName) && headC._blobs.
                    get(fileName).equals(fileContent)) {
                File checkIfStaged = new File(System.getProperty("user.dir")
                        + "/.gitlet/stage/" + fileName);
                if (checkIfStaged.exists()) {
                    Utils.restrictedDelete(checkIfStaged);
                }
            } else {
                Stage toStage = new Stage(fileName, fileContent);
                File checkIfBlob = new File(System.getProperty("user.dir")
                        + "/.gitlet/blobs/" + fileContent);
                if (!checkIfBlob.exists()) {
                    Blob toBlob = new Blob(Utils.readContentsAsString(toAdd));
                    Utils.writeObject(checkIfBlob, toBlob);
                }
                File stage = new File(System.getProperty("user.dir")
                        + "/.gitlet/stage/" + fileName);
                Utils.writeObject(stage, toStage);

            }
        }
    }

    /** Commits files in staging area. **/
    public static void commit(String message, String secondParent) {
        @SuppressWarnings("unchecked")
        HashSet<String> toRemove = Utils.readObject(new File(
                System.getProperty("user.dir")
                + "/.gitlet/tracker/removes"), HashSet.class);
        File[] stage = STAGE.listFiles();
        if (stage.length == 0 && toRemove.isEmpty()) {
            System.out.print("No changes added to the commit.");
        } else {
            String curBranch = Utils.readObject(CURBRANCH, String.class);
            @SuppressWarnings("unchecked")
            HashMap<String, String> headHM = Utils.readObject(HEADS,
                    HashMap.class);
            String headCS = headHM.get(curBranch);
            File currHead = new File(System.getProperty("user.dir")
                    + "/.gitlet/branches/" + headCS);
            Commit headNode = Utils.readObject(currHead, Commit.class);

            HashMap<String, String> newBlobs = new HashMap<>(headNode._blobs);
            for (File stageFile : stage) {
                Stage toStage = Utils.readObject(stageFile, Stage.class);
                newBlobs.put(toStage._fileName, toStage._blobRef);
                stageFile.delete();
            }

            if (!toRemove.isEmpty()) {
                for (String file : toRemove) {
                    newBlobs.remove(file);
                }
                toRemove.clear();
                Utils.writeObject(new File(System.getProperty("user.dir")
                        + "/.gitlet/tracker/removes"), toRemove);
            }

            Commit newHead = new Commit(message,
                    new String[]{headNode._sha, secondParent},
                    getDate(), newBlobs);
            String newHeadString = newHead._sha;
            File newHeadFile = new File(System.getProperty("user.dir")
                    + "/.gitlet/branches/" + newHeadString);
            Utils.writeObject(newHeadFile, newHead);

            headHM.put(curBranch, newHeadString);
            Utils.writeObject(HEADS, headHM);
            @SuppressWarnings("unchecked")
            HashSet<String> shaList = Utils.readObject(SHALIST, HashSet.class);
            shaList.add(newHeadString);
            Utils.writeObject(SHALIST, shaList);

        }
    }

    /** Prints log of commits starting from the current branch. **/
    public static void log() {
        String curBranch = Utils.readObject(CURBRANCH, String.class);
        @SuppressWarnings("unchecked")
        HashMap<String, String> headHM = Utils.readObject(HEADS, HashMap.class);
        String headCS = headHM.get(curBranch);
        File currHead = new File(System.getProperty("user.dir")
                + "/.gitlet/branches/" + headCS);
        Commit toLog = Utils.readObject(currHead, Commit.class);


        while (!toLog._parentSHA[0].equals("")) {
            System.out.println("===");
            System.out.println("commit " + toLog._sha);
            if (toLog._parentSHA[1] != null) {
                String parent1 = toLog._parentSHA[0].substring(0, 7);
                String parent2 = toLog._parentSHA[1].substring(0, 7);
                System.out.println("Merge: " + parent1 + " " + parent2);
            }
            System.out.println("Date: " + toLog._timeStamp);
            System.out.println(toLog._message);
            System.out.println();
            currHead = new File(System.getProperty("user.dir")
                    + "/.gitlet/branches/" + toLog._parentSHA[0]);
            toLog = Utils.readObject(currHead, Commit.class);
        }
        System.out.println("===");
        System.out.println("commit " + toLog._sha);
        System.out.println("Date: " + toLog._timeStamp);
        System.out.println(toLog._message);
    }

    /** Prints log of commits across all branches. **/
    public static void globalLog() {
        File[] allCommitsF = BRANCHES.listFiles();
        if (allCommitsF != null) {
            for (int i = 0; i < allCommitsF.length; i++) {
                File commit = allCommitsF[i];
                Commit c = Utils.readObject(commit, Commit.class);
                System.out.println("===");
                System.out.println("commit " + c._sha);
                System.out.println("Date: " + c._timeStamp);
                System.out.println(c._message);
                if (i != allCommitsF.length - 1) {
                    System.out.println();
                }
            }
        }
    }

    /** Reverting given file in the WD to the file in the current head commit. **/
    public static void checkoutHead(String file) {
        Commit toLog = getHeadCom();
        checkout(file, toLog);
    }

    /** Reverting all files in WD to the files of the given commit. **/
    public static void checkoutCommit(String id, String file) {
        if (id.length() < REGLENGTH) {
            @SuppressWarnings("unchecked")
            HashSet<String> shaList = Utils.readObject(SHALIST, HashSet.class);
            for (String fullSha : shaList) {
                if (fullSha.startsWith(id)) {
                    id = fullSha;
                }
            }
        }
        File commit = new File(System.getProperty("user.dir")
                + "/.gitlet/branches/" + id);
        if (!commit.exists()) {
            System.out.println("No commit with that id exists.");
        } else {
            Commit toLog = Utils.readObject(commit, Commit.class);
            checkout(file, toLog);
        }
    }

    /** Reverting all files in WD to files in the head of the given branch. **/
    public static void checkoutBranch(String branchName) {
        String curBranch = Utils.readObject(CURBRANCH, String.class);
        if (curBranch.equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
        } else {
            @SuppressWarnings("unchecked")
            HashMap<String, String> heads = Utils.readObject(HEADS,
                    HashMap.class);
            if (!heads.containsKey(branchName)) {
                System.out.println("No such branch exists.");
            } else {
                Commit fromC = Utils.readObject(new File(
                        System.getProperty("user.dir")
                        + "/.gitlet/branches/" + heads.get(curBranch)),
                        Commit.class);
                HashMap<String, String> fromHM = fromC._blobs;
                HashMap<String, String> fromCopy = new HashMap<>(fromHM);
                Commit toC = Utils.readObject(new File(
                        System.getProperty("user.dir")
                        + "/.gitlet/branches/" + heads.get(branchName)),
                        Commit.class);
                HashMap<String, String> toHM = toC._blobs;

                transfer(toHM, fromCopy);

                curBranch = branchName;
                Utils.writeObject(CURBRANCH, curBranch);
            }
        }
    }

    /** A helper method to transfer contents from one commit's files to another. **/
    private static void transfer(HashMap<String, String> toHM,
                                 HashMap<String, String> fromCopy) {
        for (String fileName : toHM.keySet()) {
            if (fromCopy.containsKey(fileName)) {
                Utils.restrictedDelete(fileName);
                File replacement = Utils.join(Main.WD, fileName);
                File contents = Utils.join(BLOBS, toHM.get(fileName));
                Blob trueContents = Utils.readObject(contents, Blob.class);
                Utils.writeContents(replacement, trueContents._contents);
                fromCopy.remove(fileName);
            } else {
                File notTracked = Utils.join(Main.WD, fileName);
                if (notTracked.exists()) {
                    System.out.println("There is an untracked file "
                            + "in the way; delete it or add it first.");
                    return;
                } else {
                    File contents = Utils.join(BLOBS, toHM.get(fileName));
                    Blob trueContents = Utils.readObject(contents, Blob.class);
                    Utils.writeContents(notTracked, trueContents._contents);
                }
            }
        }
        if (!fromCopy.isEmpty()) {
            for (String remaining : fromCopy.keySet()) {
                Utils.restrictedDelete(remaining);
            }
        }
        File[] stage = STAGE.listFiles();
        if (stage != null) {
            for (File delete : stage) {
                delete.delete();
            }
        }
    }

    /** Length of normal sha. */
    private static final int REGLENGTH = 40;

    /** Resetting WD files to files in the given commit, moving head pointer to a given commit. **/
    public static void reset(String id) {
        if (id.length() < REGLENGTH) {
            @SuppressWarnings("unchecked")
            HashSet<String> shaList = Utils.readObject(SHALIST,
                    HashSet.class);
            for (String fullSha : shaList) {
                if (fullSha.startsWith(id)) {
                    id = fullSha;
                }
            }
        }
        File toCommitF = new File(System.getProperty("user.dir")
                + "/.gitlet/branches/" + id);
        if (!toCommitF.exists()) {
            System.out.println("No commit with that id exists.");
        } else {
            String curBranch = Utils.readObject(CURBRANCH, String.class);
            @SuppressWarnings("unchecked")
            HashMap<String, String> heads = Utils.readObject(HEADS,
                    HashMap.class);
            Commit fromC = Utils.readObject(new File(
                    System.getProperty("user.dir")
                    + "/.gitlet/branches/" + heads.get(curBranch)),
                    Commit.class);
            HashMap<String, String> fromHM = fromC._blobs;
            HashMap<String, String> fromCopy = new HashMap<>(fromHM);
            Commit toC = Utils.readObject(new File(
                    System.getProperty("user.dir")
                    + "/.gitlet/branches/" + id), Commit.class);
            HashMap<String, String> toHM = toC._blobs;

            transfer(toHM, fromCopy);

            heads.put(curBranch, id);
            Utils.writeObject(HEADS, heads);
        }
    }

    /** Helper method for the three different checkout commands. **/
    public static void checkout(String file, Commit check) {
        if (!check._blobs.containsKey(file)) {
            System.out.println("File does not exist in that commit.");
        } else {
            File toDel = Utils.join(Main.WD, file);
            Utils.restrictedDelete(toDel);
            String content = check._blobs.get(file);

            File blob = new File(System.getProperty("user.dir")
                    + "/.gitlet/blobs/" + content);
            Blob readBlob = Utils.readObject(blob, Blob.class);
            String trueContents = readBlob._contents;

            File toAdd = Utils.join(Main.WD, file);
            Utils.writeContents(toAdd, trueContents);
        }
    }

    /** Removes a file from the staging area and untracks it from the next commit. **/
    public static void remove(String file) {
        boolean deleted;

        File checkStage = new File(System.getProperty("user.dir")
                + "/.gitlet/stage/" + file);
        deleted = checkStage.delete();

        Commit headCom = getHeadCom();
        if (headCom._blobs.containsKey(file)) {
            File toRemoveF = new File(System.getProperty("user.dir")
                    + "/.gitlet/tracker/removes");
            @SuppressWarnings("unchecked")
            HashSet<String> toRemove = Utils.readObject(toRemoveF,
                    HashSet.class);
            toRemove.add(file);
            Utils.writeObject(toRemoveF, toRemove);
            deleted = true;
            Utils.restrictedDelete(file);
        }
        if (!deleted) {
            System.out.println("No reason to remove the file.");
        }
    }

    /** Gives status of gitlet. **/
    public static void status() {
        System.out.println("=== Branches ===");
        @SuppressWarnings("unchecked")
        HashMap<String, String> headHM = Utils.readObject(HEADS, HashMap.class);
        String curBranch = Utils.readObject(CURBRANCH, String.class);
        List<String> sortLex = new ArrayList<String>(headHM.keySet());
        Collections.sort(sortLex);
        for (String branchName : sortLex) {
            if (branchName.equals(curBranch)) {
                System.out.println("*" + branchName);
            } else {
                System.out.println(branchName);
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        File[] stage = STAGE.listFiles();
        if (stage != null) {
            List<String> sortLex1 = new ArrayList<String>();
            for (File file : stage) {
                sortLex1.add(file.getName());
            }
            Collections.sort(sortLex1);
            for (String staged : sortLex1) {
                System.out.println(staged);
            }
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        @SuppressWarnings("unchecked")
        HashSet<String> removes = Utils.readObject(new File(
                System.getProperty("user.dir")
                + "/.gitlet/tracker/removes"), HashSet.class);
        List<String> sortLex2 = new ArrayList<String>(removes);
        Collections.sort(sortLex2);
        for (String remove : sortLex2) {
            System.out.println(remove);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
    }

    /** Prints all commit id's with the given message. **/
    public static void find(String message) {
        File[] allCommits = BRANCHES.listFiles();
        HashSet<String> toPrint = new HashSet<>();

        for (File commit : allCommits) {
            Commit check = Utils.readObject(commit, Commit.class);
            if (check._message.equals(message)) {
                toPrint.add(check._sha);
            }
        }
        if (toPrint.isEmpty()) {
            System.out.println("Found no commit with that message.");
        } else {
            for (String print : toPrint) {
                System.out.println(print);
            }
        }
    }

    /** Creates a new branch. **/
    public static void branch(String name) {
        @SuppressWarnings("unchecked")
        HashMap<String, String> headHM = Utils.readObject(HEADS, HashMap.class);
        if (headHM.containsKey(name)) {
            System.out.println("A branch with that name already exists.");
        } else {
            String curBranch = Utils.readObject(CURBRANCH, String.class);
            String curHead = headHM.get(curBranch);
            headHM.put(name, curHead);
            Utils.writeObject(HEADS, headHM);
        }
    }

    /** Removes a branch. **/
    public static void removeB(String branch) {
        HashMap headHM = Utils.readObject(HEADS, HashMap.class);
        if (!headHM.containsKey(branch)) {
            System.out.println("A branch with that name does not exist.");
        } else {
            String curBranch = Utils.readObject(CURBRANCH, String.class);
            if (curBranch.equals(branch)) {
                System.out.println("Cannot remove the current branch.");
            } else {
                headHM.remove(branch);
                Utils.writeObject(HEADS, headHM);
            }
        }
    }

    /** Merges the current branch head with the given branch head. **/
    public static void merge(String branch) {
        checkStage();
        checkCurBranch(branch);
        checkHeadHM(branch);

        String curBranch = Utils.readObject(CURBRANCH, String.class);
        String splitPoint = findSplit(curBranch, branch);
        @SuppressWarnings("unchecked")
        HashMap<String, String> headsHM = Utils.readObject(HEADS,
                HashMap.class);
        if (splitPoint.equals(headsHM.get(branch))) {
            System.out.println("Given branch is an ancestor of "
                    + "the current branch.");
            System.exit(0);
        }

        if (splitPoint.equals(headsHM.get(curBranch))) {
            reset(headsHM.get(branch));
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        Commit curCommit = Utils.readObject(Utils.join(BRANCHES,
                headsHM.get(curBranch)), Commit.class);
        HashMap<String, String> cBlobs = curCommit._blobs;
        HashMap<String, String> cCopy = new HashMap<>(cBlobs);

        Commit mergeCommit = Utils.readObject(Utils.join(BRANCHES,
                headsHM.get(branch)), Commit.class);
        HashMap<String, String> mBlobs = mergeCommit._blobs;

        Commit splitCommit = Utils.readObject(Utils.join(BRANCHES,
                splitPoint), Commit.class);
        HashMap<String, String> sBlobs = splitCommit._blobs;
        HashMap<String, String> sCopy = new HashMap<>(sBlobs);

        @SuppressWarnings("unchecked")
        HashMap<String, String>[] copies = mergeHelper(cCopy,
                mBlobs, sCopy, mergeCommit);
        cCopy = copies[0];
        sCopy = copies[1];

        mergeHelper2(cCopy, sCopy);

        commit("Merged " + branch + " into " + curBranch + ".",
                headsHM.get(branch));

        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
        conflict = false;
    }

    /** Checking if there was a merge conflict. **/
    private static boolean conflict = false;

    /** Helper method for all the different cases of merging. **/
    private static HashMap[] mergeHelper(HashMap<String, String> cCopy,
                                         HashMap<String,
            String> mBlobs, HashMap<String, String> sCopy, Commit mergeCommit) {

        for (String blob : mBlobs.keySet()) {
            if (cCopy.containsKey(blob) && sCopy.containsKey(blob)) {
                if (!mBlobs.get(blob).equals(sCopy.get(blob))
                        && sCopy.get(blob).equals(cCopy.get(blob))) {
                    File checkout = Utils.join(Main.WD, blob);
                    checkout(blob, mergeCommit);
                    add(checkout);
                } else if (mBlobs.get(blob).equals(sCopy.get(blob))
                        && !sCopy.get(blob).equals(cCopy.get(blob))) {
                    conflict = false;
                } else if (!mBlobs.get(blob).equals(sCopy.get(blob))
                        && !sCopy.get(blob).equals(cCopy.get(blob))) {
                    mergeConflict(cCopy, mBlobs, blob);
                    conflict = true;
                }
                sCopy.remove(blob);
                cCopy.remove(blob);
            } else if (!cCopy.containsKey(blob) && sCopy.containsKey(blob)) {
                if (!mBlobs.get(blob).equals(sCopy.get(blob))) {
                    mergeConflict(new HashMap<>(), mBlobs, blob);
                    conflict = true;
                    sCopy.remove(blob);
                } else {
                    sCopy.remove(blob);
                }
            } else if (!cCopy.containsKey(blob) && !sCopy.containsKey(blob)) {

                File checkout = Utils.join(Main.WD, blob);
                if (checkout.exists()) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it or add it first.");
                    System.exit(0);
                }
                checkout(blob, mergeCommit);
                add(checkout);
            } else if (cCopy.containsKey(blob) && !sCopy.containsKey(blob)) {
                if (!cCopy.get(blob).equals(mBlobs.get(blob))) {
                    mergeConflict(cCopy, mBlobs, blob);
                    conflict = true;
                    cCopy.remove(blob);
                }
            }
        }
        return new HashMap[]{cCopy, sCopy};
    }

    /** Helper method that merges files with merge conflicts. **/
    private static void mergeConflict(HashMap<String, String> cCopy,
                          HashMap<String, String> mBlobs, String blob) {
        String curCont = "";
        if (!cCopy.isEmpty()) {
            File curBlob = Utils.join(BLOBS, cCopy.get(blob));
            curCont = Utils.readObject(curBlob, Blob.class)._contents;
        }
        String merCont = "";
        if (!mBlobs.isEmpty()) {
            File merBlob = Utils.join(BLOBS, mBlobs.get(blob));
            merCont = Utils.readObject(merBlob, Blob.class)._contents;
        }

        File confictFile = Utils.join(Main.WD, blob);
        String content = "<<<<<<< HEAD\n";
        content += curCont + "\n";
        content += "=======\n";
        content += merCont;
        content += ">>>>>>>\n";

        Utils.writeContents(confictFile, content);
    }

    /** A helper method for merge. **/
    private static void mergeHelper2(HashMap<String, String> cCopy,
                                     HashMap<String, String> sCopy) {
        for (String blob : cCopy.keySet()) {
            if (sCopy.containsKey(blob)) {
                if (cCopy.get(blob).equals(sCopy.get(blob))) {
                    Utils.restrictedDelete(blob);
                    remove(blob);
                }
                if (!cCopy.get(blob).equals(sCopy.get(blob))) {
                    mergeConflict(cCopy, new HashMap<>(), blob);
                    conflict = true;
                }
            }
        }
    }

    /** Finds the split point between current and given branches. **/
    private static String findSplit(String checkBranch, String findBranch) {
        @SuppressWarnings("unchecked")
        HashMap<String, String> headsHM = Utils.readObject(HEADS,
                HashMap.class);
        String checkCommitS = headsHM.get(checkBranch);
        String findCommitS = headsHM.get(findBranch);

        List<String> checkPath = new ArrayList<String>();
        List<String> findPath = new ArrayList<String>();
        Commit checkCommit = Utils.readObject(Utils.join(BRANCHES,
                checkCommitS), Commit.class);
        Commit findCommit = Utils.readObject(Utils.join(BRANCHES,
                findCommitS), Commit.class);

        findPath.add(findCommit._sha);
        checkPath.add(checkCommit._sha);

        while (!findCommit._parentSHA[0].equals("")) {
            findPath.add(findCommit._parentSHA[0]);
            if (findCommit._parentSHA[1] != null) {
                findPath.add(findCommit._parentSHA[1]);
            }
            findCommit = Utils.readObject(Utils.join(BRANCHES,
                    findCommit._parentSHA[0]), Commit.class);
        }
        while (!checkCommit._parentSHA[0].equals("")) {
            checkPath.add(checkCommit._parentSHA[0]);
            if (checkCommit._parentSHA[1] != null) {
                checkPath.add(checkCommit._parentSHA[1]);
            }
            checkCommit = Utils.readObject(Utils.join(BRANCHES,
                    checkCommit._parentSHA[0]), Commit.class);
        }
        for (String commit : findPath) {
            if (checkPath.contains(commit)) {
                return commit;
            }
        }
        return checkCommit._sha;
    }

    private static void checkStage() {
        File[] checkStage = STAGE.listFiles();
        if (checkStage.length > 0) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
    }

    private static void checkCurBranch(String branch) {
        String curBranch = Utils.readObject(CURBRANCH, String.class);
        if (branch.equals(curBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }

    /** Helper method. **/
    private static void checkHeadHM(String branch) {
        @SuppressWarnings("unchecked")
        HashMap<String, String> headsHM = Utils.readObject(HEADS,
                HashMap.class);
        if (!headsHM.containsKey(branch)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
    }

    /** Helper Method. **/
    public static Commit getHeadCom() {
        String curBranch = Utils.readObject(CURBRANCH, String.class);
        @SuppressWarnings("unchecked")
        HashMap<String, String> headHM = Utils.readObject(HEADS, HashMap.class);
        String headCS = headHM.get(curBranch);
        File currHead = new File(System.getProperty("user.dir")
                + "/.gitlet/branches/" + headCS);
        return Utils.readObject(currHead, Commit.class);
    }

}







