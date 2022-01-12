package gitlet;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Formatter;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Collection;


/** Class that holds all the major methods in Gitlet.
 * @author Daniel Bostwick */
public class Objects implements Serializable {


    /** Creates a new Gitlet version-control system in the current directory. */
    public void init() throws IOException {
        GITLET.mkdir();
        COMMIT.mkdir();
        STAGING_DIR.mkdir();
        BLOBS.mkdir();
        STAGING_AREA.mkdir();
        BRANCHES.mkdir();
        try {
            HEAD.createNewFile();
            STAGEDFILE.createNewFile();
        } catch (IOException excp) {
            throw new GitletException(excp.getMessage());
        }
        Commit initial = new Commit("initial commit",
                null, new LinkedHashMap<>());
        String initialCommitSHA = initial.getCommitSHA();
        if (COMMITSMAP.exists()) {
            System.out.println("A Gitlet version-control "
                    + "system already exists in the current directory.");
        } else {
            committedFiles = new LinkedHM();
            committedFiles.add(initialCommitSHA, initial);
            Utils.writeObject(COMMITSMAP, committedFiles);
            Utils.writeContents(HEAD, "master");
            Utils.writeContents(MASTER, initialCommitSHA);
            stage = new Staged();
            Utils.writeObject(STAGEDFILE, stage);
        }
    }

    /** Takes in a file and creates a unique SHA-1 ID from its contents.
     * @param file The file passed in.
     * @return */
    public String fileSHA(File file) {
        return Utils.sha1(file.getName(), Utils.readContents(file));
    }


    /** Gets the head Commit.
     * @return */
    public Commit getHead2() {
        LinkedHM commits = Utils.readObject(COMMITSMAP, LinkedHM.class);
        File head = new File(BRANCHES + "/"
                + Utils.readContentsAsString(HEAD));
        String headSHA = Utils.readContentsAsString(head);
        return commits.getCommits().get(headSHA);
    }

    /** Adds a copy of the file as it currently exists to the staging area.
     * @param added The filename to be added. */
    public void add(String added) throws IOException, FileNotFoundException {
        File addedFile = new File(added);
        File stagingFile = Utils.join(STAGING_DIR, added);
        Staged stagingArea = Utils.readObject(STAGEDFILE, Staged.class);
        Commit head = getHead2();
        if (!addedFile.exists()) {
            System.out.println("File does not exist.");
        } else {
            if (head.getBlobs().get(added) == null
                    || !fileSHA(addedFile).equals(head.getBlobs().get(added))) {
                if (stagingArea.getRemoved().contains(fileSHA(addedFile))) {
                    stagingArea.getRemoved().remove(added);
                }
                Utils.writeContents(stagingFile, fileSHA(addedFile));
                stagingArea.add(added, fileSHA(addedFile));
                Utils.writeObject(STAGEDFILE, stagingArea);
                Utils.writeContents(Utils.join(BLOBS + "/"
                        + fileSHA(addedFile)), Utils.readContents(addedFile));
            }
            if (fileSHA(addedFile).equals(head.getBlobs().get(added))) {
                if (stagingArea.getRemoved().contains(added)) {
                    stagingArea.getRemoved().remove(added);
                    Utils.writeObject(STAGEDFILE, stagingArea);
                }
            }
        }
    }

    /** Takes in a File and clears the current directory of all its files.
     * @param directory The directory to be cleared. */
    public void clearDir(File directory) {
        if (directory.isDirectory() && directory.length() != 0) {
            for (File dirFiles : directory.listFiles()) {
                dirFiles.delete();
            }
        }
    }

    /** Saves a snapshot of tracked files in the current commit and
     * staging area, so they can be restored at a later time,
     * creating a new commit.
     * @param message The commit message. */
    public void commit(String message) throws FileNotFoundException {
        Staged stagingArea = Utils.readObject(STAGEDFILE, Staged.class);
        LinkedHM tempCommitLHM = Utils.readObject(COMMITSMAP, LinkedHM.class);
        if (stagingArea.getAdded().isEmpty()
                && stagingArea.getRemoved().isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Commit current = getHead2();
        LinkedHashMap<String, String> tempBlobMap = new LinkedHashMap<>();
        if (!getHead2().getBlobs().isEmpty()) {
            tempBlobMap.putAll(current.getBlobs());
        }
        for (String removed : stagingArea.getRemoved()) {
            tempBlobMap.remove(removed);
        }
        for (String added : stagingArea.getAdded().keySet()) {
            tempBlobMap.put(added, stagingArea.getAdded().get(added));

        }
        Commit nextCommit = new Commit(message,
                current.getCommitSHA(), tempBlobMap);
        String nextCommitSHA = nextCommit.getCommitSHA();
        tempCommitLHM.add(nextCommitSHA, nextCommit);
        Utils.writeObject(COMMITSMAP, tempCommitLHM);
        String s = Utils.readContentsAsString(HEAD);
        File head = Utils.join(BRANCHES, s);
        Utils.writeContents(head, nextCommitSHA);
        stagingArea.clear();
        Utils.writeObject(STAGEDFILE, stagingArea);
        clearDir(STAGING_DIR);
    }

    /** Removes a file from Gitlet.
     * @param fileName The name of the file to remove.  */
    public void rm(String fileName) {
        Staged stagingArea = Utils.readObject(STAGEDFILE, Staged.class);
        Commit head = getHead2();
        if (!stagingArea.getAdded().containsKey(fileName)
                && !head.getBlobs().containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
        }
        if (stagingArea.getAdded().containsKey(fileName)) {
            stagingArea.getAdded().remove(fileName);
            Utils.writeObject(STAGEDFILE, stagingArea);
        }
        if (head.getBlobs().containsKey(fileName)) {
            File isFileInCWD = new File(CWD, fileName);
            if (isFileInCWD.exists()) {
                Utils.restrictedDelete(isFileInCWD);
            }
            stagingArea.getRemoved().add(fileName);
            Utils.writeObject(STAGEDFILE, stagingArea);
        }
    }

    /** Displays information about each commit. */
    public void log() {
        Formatter format = new Formatter();
        Commit commit = getHead2();
        while (commit != null) {
            format.format("===\n");
            format.format("commit ");
            format.format(commit.getCommitSHA());
            format.format("\nDate: ");
            format.format(commit.getTimestamp());
            format.format("\n");
            format.format(commit.getMessage());
            if (commit.getParent() == null) {
                break;
            } else {
                format.format("\n\n");
                commit = specificCom(commit.getParent());
            }
        }
        System.out.println(format);
        System.out.println();
    }

    /** Gets a specific Commit.
     * @param id The ID.
     * @return commits. */
    private Commit specificCom(String id) {
        LinkedHM commits = Utils.readObject(COMMITSMAP, LinkedHM.class);
        return commits.getCommits().get(id);
    }

    /** Displays info on all commits ever made. */
    public void globalLog() {
        log();
    }

    /** Method to check out branches. Called in checkout().
     * @param branchName Takes in a branch. */
    public void checkoutBranch(String branchName) {
        Commit head = getHead2();
        Commit formerHead = head;
        LinkedHM tempCommitLHM = Utils.readObject(COMMITSMAP, LinkedHM.class);
        File branch = Utils.join(BRANCHES, branchName);
        Staged stagingArea = Utils.readObject(STAGEDFILE, Staged.class);
        if (!branch.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (Utils.readContentsAsString(HEAD).equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        for (File file: CWD.listFiles()) {
            if (file.isFile()) {
                if (!Utils.join(BLOBS, fileSHA(file)).exists()) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
        stagingArea.clear();
        Utils.writeObject(STAGEDFILE, stagingArea);
        Utils.writeContents(HEAD, branchName);
        File headBranch = new File(BRANCHES
                + "/" + Utils.readContentsAsString(HEAD));
        String branchContents = Utils.readContentsAsString(headBranch);
        head = tempCommitLHM.getCommits().get(branchContents);
        File newHead = Utils.join(BRANCHES, headBranch.getName());
        Utils.writeContents(newHead, branchContents);
        for (String blobName : head.getBlobs().keySet()) {
            String checkedFileSHA = head.getBlobs().get(blobName);
            File outCheckedFile = new File(BLOBS
                    + "/" + checkedFileSHA);
            String readCheckedString
                    = Utils.readContentsAsString(outCheckedFile);
            File cwd = new File(".");
            Utils.writeContents(Utils.join(cwd, blobName), readCheckedString);
        }
        for (String fileName : formerHead.getBlobs().keySet()) {
            File cwdFile = Utils.join(CWD, fileName);
            String cwdName = fileSHA(cwdFile);
            if (formerHead.getBlobs().containsValue(cwdName)) {
                Utils.restrictedDelete(cwdFile);
            }
        }
    }

    /** Method to check out files. Called in checkout().
     * @param fileName Takes in a file. */
    public void checkoutFile(String fileName) {
        Commit head = getHead2();
        if (head.getBlobs().get(fileName) == null) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        } else {
            if (head.getBlobs().containsKey(fileName)) {
                String checkedFileSHA = head.getBlobs().get(fileName);
                File outCheckedFile = new File(BLOBS
                        + "/" + checkedFileSHA);
                String readCheckedString =
                        Utils.readContentsAsString(outCheckedFile);
                File cwd = new File(".");
                Utils.writeContents(Utils.join(cwd,
                        fileName), readCheckedString);
                System.exit(0);
            }
        }
    }

    /** Method to checks out commits. Called in checkout().
     * @param commitName Takes in a commit.
     * @param fileName  Takes in a file. */
    public void checkoutCommit(String commitName, String fileName) {
        LinkedHM commits = Utils.readObject(COMMITSMAP, LinkedHM.class);
        for (String commitSHA : commits.getCommits().keySet()) {
            if (commitSHA.contains(commitName)) {
                commitName = commitSHA;
            }
        }
        LinkedHM tempCommitLHM = Utils.readObject(COMMITSMAP, LinkedHM.class);
        if (!tempCommitLHM.getCommits().containsKey(commitName)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        if (!tempCommitLHM.getCommits().get(commitName).getBlobs().
                containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        if (tempCommitLHM.getCommits().get(commitName).getBlobs().
                containsKey(fileName)) {
            String checkedFileSHA = tempCommitLHM.getCommits().
                    get(commitName).getBlobs().get(fileName);
            File outCheckedFile = new File(BLOBS + "/" + checkedFileSHA);
            String readCheckedString
                    = Utils.readContentsAsString(outCheckedFile);
            File cwd = new File(".");
            Utils.writeContents(Utils.join(cwd, fileName), readCheckedString);
            System.exit(0);
        }
    }

    /** Checks out either a branch, a commit, or a file.
     * @param args The arguments. */
    public void checkout(String[] args) throws IOException {
        if (args.length == 2) {
            checkoutBranch(args[1]);
        }
        if (args.length == 3) {
            checkoutFile(args[2]);
        }
        if (args.length == 4) {
            checkoutCommit(args[1], args[3]);
        }
    }

    /** Finds all commits with the given message.
     * @param message The commit message to be found. */
    public void find(String message) {
        LinkedHM tempCommitLHM = Utils.readObject(COMMITSMAP, LinkedHM.class);
        Collection<Commit> commitCollection
                = tempCommitLHM.getCommits().values();
        ArrayList<String> commitSHA = new ArrayList<>();
        for (Commit commitMessage : commitCollection) {
            if (commitMessage.getMessage().equals(message)) {
                commitSHA.add(commitMessage.getCommitSHA());
            }
        }
        if (commitSHA.size() == 0) {
            System.out.println("Found no commit with that message.");
        }
        Collections.sort(commitSHA);
        for (String commitMessage : commitSHA) {
            System.out.println(commitMessage);
        }
    }

    /** Runs the tests for the edge cases for Modifications in Status. */
    public void statusModificationsCheck() {
        Staged stagingArea = Utils.readObject(STAGEDFILE, Staged.class);
        for (File file : CWD.listFiles()) {
            if (file.isFile()) {
                if (getHead2().getBlobs().containsKey(file.getName())
                        && !getHead2().getBlobs().
                        containsValue(fileSHA(file))) {
                    System.out.println(file.getName() + " (modified)");
                }
                if (stagingArea.getAdded().containsKey(file.getName())
                        && !stagingArea.getAdded().containsValue(fileSHA(file))) {
                    System.out.println(file.getName() + " (modified)");
                }
                if (!file.exists()
                        && stagingArea.getAdded().containsKey(file.getName())) {      //!file.exists() &&
                     System.out.println(file.getName() + " (deleted)");
                }
                if (!file.exists()
                        && !stagingArea.getRemoved().contains(file.getName())
                        && !getHead2().getBlobs().containsKey(fileSHA(file))) {
                    System.out.println(file.getName() + " (deleted)");
                }
//                if (!Utils.join(BLOBS, fileSHA(file)).exists()) {
//                    System.out.println(file.getName());
//                }
            }
        }
    }

    /** Displays what branches currently exist. */
    public void status() {
        Commit head = getHead2();
        System.out.println("=== Branches ===");
        for (String branches : Utils.plainFilenamesIn(BRANCHES)) {
            if (branches.equals(Utils.readContentsAsString(HEAD))) {
                System.out.print("*");
            }
            System.out.println(branches);
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        Staged stagingArea = Utils.readObject(STAGEDFILE, Staged.class);
        for (String added : stagingArea.getAdded().keySet()) {
            System.out.println(added);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String deleted : stagingArea.getRemoved()) {
            System.out.println(deleted);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        statusModificationsCheck();
        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (File file: CWD.listFiles()) {
            if (file.isFile()) {
//                if (file.exists()
                   if(!getHead2().getBlobs().containsKey(file.getName())
                           && !Utils.join(BLOBS, fileSHA(file)).exists()
                           && !stagingArea.getAdded().
                           containsKey(file.getName())) {
                       System.out.println(file.getName());
                }
            }
        }
        System.out.println();
    }

    /** Creates a new branch with the given name.
     *  @param branchName The name of the branch to be added. */
    public void branch(String branchName) {
        Commit head = getHead2();
        String newBranchCommit = head.getCommitSHA();
        File branch = Utils.join(BRANCHES, branchName);
        if (branch.exists()) {
            System.out.println("A branch with that name already exists.");
        } else {
            Utils.writeContents(branch, newBranchCommit);
        }
    }

    /** Deletes the branch with the given name.
     *  @param branchName The name of the branch to be removed. */
    public void rmBranch(String branchName) {
        File branch = Utils.join(BRANCHES, branchName);
        if (Utils.readContentsAsString(HEAD).equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        for (File fileName : BRANCHES.listFiles()) {
            if (fileName.equals(branch)) {
                branch.delete();
                System.exit(0);
            }
        }
        System.out.println("A branch with that name does not exist. ");
    }

    /** Checks out all the files tracked by the given commit.
     * @param commitID The name of the commit. */
    public void reset(String commitID) {
        LinkedHM commits = Utils.readObject(COMMITSMAP, LinkedHM.class);
        Commit commitToReset = commits.getCommits().get(commitID);
        Staged stagingArea = Utils.readObject(STAGEDFILE, Staged.class);
        if (!commits.getCommits().containsKey(commitID)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        for (File file: CWD.listFiles()) {
            if (file.isFile()) {
                if (!Utils.join(BLOBS, fileSHA(file)).exists()) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
        for (String blob : commitToReset.getBlobs().keySet()) {
            Utils.writeContents(Utils.join(CWD, blob),
                    Utils.readContentsAsString(Utils.join(BLOBS,
                            commitToReset.getBlobs().get(blob))));
        }
        for (String filename: getHead2().getBlobs().keySet()) {
            if (!commitToReset.getBlobs().containsKey(filename)) {
                Utils.restrictedDelete(Utils.join(CWD, filename));
            }
        }
        stagingArea.clear();
        Utils.writeObject(STAGEDFILE, stagingArea);
        File headBranch = new File(BRANCHES
                + "/" + Utils.readContentsAsString(HEAD));
        Utils.writeContents(headBranch, commitID);
    }

//    /** Merges files from the given branch into the current branch. */
//    public void merge(String branchName) {
//
//    }


    /** Creates a current working directory. */
    static final File CWD = new File(System.getProperty("user.dir"));

    /** Creates the hidden .gitlet folder that stores all metadata. */
    static final File GITLET = Utils.join(CWD, ".gitlet");

    /** Creates the BLOBS folder. */
    static final File BLOBS = Utils.join(GITLET, "blobs");

    /** Creates the COMMIT folder to store commits. */
    static final File COMMIT = Utils.join(GITLET, "commits");

    /** Creates the BRANCHES folder. */
    static final File BRANCHES = Utils.join(GITLET, "branches");

    /** Creates the STAGING_DIR folder. */
    static final File STAGING_DIR = Utils.join(GITLET, "staging-dir");

    /** Creates a Staging Area called STAGING_AREA folder. */
    static final File STAGING_AREA = Utils.join(GITLET, "staging-area");




    /** Creates the HEAD folder to store commits. */
    static final File HEAD = Utils.join(GITLET, "headCommit");

    /** Creates the MASTER folder. */
    static final File MASTER = Utils.join(BRANCHES, "master");

    /** Creates a Staging Area File. */
    static final File STAGEDFILE = Utils.join(STAGING_AREA, "stagingFiles");

    /** Creates a folder to store the HashMap of Commits. */
    static final File COMMITSMAP = Utils.join(COMMIT, "commitsMap.txt");




    /** LinkedHashMap to hold all files that are committed. */
    private LinkedHM committedFiles;

    /** Staging area to reference all staged blobs. */
    private Staged stage;

    /** LinkedHashMap to store marked files. */
    private static LinkedHashMap<String, String> markedFiles
            = new LinkedHashMap<>();

    /** LinkedHashMap to store staged files. */
    private static LinkedHashMap<String, File> stagedFiles
            = new LinkedHashMap<>();

    /** LinkedHashMap to store commit LinkedHashmap. */
    private static LinkedHashMap<String, Commit> commitLHM
            = new LinkedHashMap<>();

    /** LinkedHashMap to store all blobs. */
    private static LinkedHashMap<String, String> blobsHashMap
            = new LinkedHashMap<>();

    /** ReadObject supplement because there is no class to read object class.
     * @param file The file to be written.
     * @param linkedHashMap The linked HashMap. */
    public void writeContentsLHM(File file, LinkedHashMap linkedHashMap) {
        String linkedSHA = Utils.sha1(Utils.serialize(linkedHashMap));
        Utils.writeContents(file, linkedSHA);
    }

    /** Gets the sha1 of the master branch.
     * @return */
    public String getMasterSHA() throws IOException {
        return Utils.readFile(MASTER.getPath());
    }
}
