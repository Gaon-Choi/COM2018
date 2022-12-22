package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;


/**
 *  Represents a gitlet repository.
 *  @author Gaon Choi
 */
public class Repository {
    /**
     *  Paths to each file required to implement Persistence functionality
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /**
     * Directory - Branches are saved in this folder.
     */
    public static final File GITLET_REFS = join(GITLET_DIR, "refs");
    public static final File GITLET_HEADS = join(GITLET_REFS, "heads");

    /**
     * Directory - Objects are saved in this folder.
     */
    public static final File GITLET_OBJECTS = join(GITLET_DIR, "objects");

    /**
     * File - Stores current branch(HEAD)
     */
    public static final File GITLET_HEADER = join(GITLET_DIR, "HEAD");

    /**
     * File - Staging Area
     */
    public static final File GITLET_STAGE = join(GITLET_DIR, "index");

    public static final File GITLET_LOG = join(GITLET_DIR, "logs");

    /**
     * Current HEADER
     */
    public static Branch currbch;
    public static final String DEFAULT_BRANCH = "master";

    /**
     * initialize Git in the given repository
     * (*) git init
     */
    public static void init() {
        // Case1: this repository already has its Gitlet
        if (GITLET_DIR.exists()) {
            System.out.println(
                    "A Gitlet version-control system already exists in the current directory."
            );
            return;
        }

        // Case2: this repository doesn't have any Gitlet yet
        GITLET_DIR.mkdir();

        GITLET_REFS.mkdir();
        GITLET_HEADS.mkdir();
        GITLET_OBJECTS.mkdir();
        try {
            GITLET_HEADER.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            GITLET_STAGE.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            GITLET_LOG.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Commit comm = new Commit();
        comm.saveCommit();
        currbch = new Branch(DEFAULT_BRANCH, comm);
        currbch.saveBranch();
        String curr = "ref: refs/heads/" + currbch.getName();
        Utils.writeObject(GITLET_HEADER, curr);

        StageArea stagearea = new StageArea();
        Utils.writeObject(GITLET_STAGE, stagearea);

        Logs logs = new Logs();
        logs.saveLogs();

        logs = Logs.loadLogs();
        logs.appendItem(currbch.getName(), comm);
    }

    /**
     * Returns the relative path of given path
     * @param path
     * @return
     */
    public static String getRelativePath(String path) {
        return path.substring((int)System.getProperty("user.dir").length() + 1);
    }

    /**
     * Create a new branch with given branch name
     * (*) git branch
     * @param bname
     */
    public static void branch(String bname) {
        Branch br = Repository.getCurrBranch();
        File file = Utils.join(GITLET_HEADS, bname);
        if (file.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Branch branch = new Branch(bname, br.getPointer());
        Utils.writeObject(file, branch);
    }

    /**
     * Returns the current Branch
     * @return
     */
    public static Branch getCurrBranch() {
        String header_ = Utils.readContentsAsString(GITLET_HEADER);
        int idx = header_.lastIndexOf("/");
        return Branch.loadBranch(header_.substring(idx + 1));
    }

    /**
     * Set the current Branch with given branch name
     * @param bname
     */
    public static void setCurrBranch(String bname) {
        File file = Utils.join(GITLET_HEADS, bname);
        if (file.exists()) {
            String header = "ref: refs/heads/" + bname;
            Utils.writeObject(GITLET_HEADER, header);
        }
    }

    /**
     * change the commit that the current branch points to,
     * for performing the "git commit" command
     * @param comm
     */
    public static void setCurrBranchCommit(Commit comm) {
        if (comm != null) {
            Branch curr = getCurrBranch();
            curr.setPointer(comm);
            curr.saveBranch();
        }
    }

    /**
     * Removes a branch with given name
     * (*) git rm-branch
     * @param bname
     */
    public static void rm_branch(String bname) {
        File file = Utils.join(GITLET_HEADS, bname);
        if (!file.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        Branch br = Repository.getCurrBranch();
        if (br.getName().equals(bname)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        // Utils.restrictedDelete(file);
        file.delete();
    }

    /**
     * Prints all logs for current branch
     */
    public static void log() {
        Branch curr = getCurrBranch();
        Commit comm = curr.getPointer();
        while (comm != null) {
            System.out.println(Commit.printLog(comm));
            String parr = Commit.getParent(comm.getId());

            comm = (parr != null) ? Commit.loadCommit(parr) : null;
        }
    }

    /**
     * Prints current status of StageArea
     */
    public static void status() {
        showBranches();
        showStagedFiles();
        showRemovedFiles();
        showModifiedNotStagedFiles();
        showUntrackedFiles();
    }

    /**
     * Prints all branches with a star-marking on the current branch
     */
    private static void showBranches() {
        String curr = getCurrBranch().getName();
        System.out.println("=== Branches ===");
        System.out.println("*" + curr);
        String[] branchNames = GITLET_HEADS.list();
        Arrays.sort(branchNames);
        for (String branch : branchNames) {
            if(!branch.equals(curr)) {
                System.out.println(branch);
            }
        }
        System.out.println("  ");
    }

    /**
     * Print a list of staged files
     */
    private static void showStagedFiles() {
        System.out.println("=== Staged Files ===");
        List<String> sortedKeys=new ArrayList<String>(StageArea.stageArea().getAdded().keySet());
        Collections.sort(sortedKeys);
        for (String path : sortedKeys) {
            System.out.println(getRelativePath(path));
        }
        System.out.println("  ");
    }

    /**
     * Print a list of to-be-removed files
     */
    private static void showRemovedFiles() {
        System.out.println("=== Removed Files ===");
        List<String> sortedKeys = new ArrayList<String>(StageArea.stageArea().getRemoved());
        Collections.sort(sortedKeys);
        for (String path : sortedKeys) {
            System.out.println(getRelativePath(path));
        }
        System.out.println("  ");
    }

    /**
     * Print a list of modified-but-not-staged files
     */
    public static void showModifiedNotStagedFiles() {
        System.out.println("=== Modification Not Staged For Commit ===");

        HashMap<String, String> modified = getModifiedList();

        Object[] mapkey = modified.keySet().toArray();
        Arrays.sort(mapkey);
        for (Object str : mapkey) {
            System.out.println(str + " " + "(" + modified.get(str) + ")");
        }

        System.out.println("  ");
    }

    /**
     * Print a list of modified files with their status
     * @return
     */
    private static HashMap<String, String> getModifiedList() {
        List<String> flist = Utils.plainFilenamesIn(CWD);
        HashMap<String, String> fmap = getCurrBranch().getPointer().getfmap();
        HashMap<String, String> modified = new HashMap<>();
        ArrayList<String> removed = StageArea.stageArea().getRemoved();

        for (String file : flist) {
            File thisFile = Utils.join(CWD, file);
            String path = thisFile.getPath();

            if (fmap.get(path) != null && !fmap.get(path).equals(Blob.generateId(thisFile))) {
                // file have been modified
                modified.put(file, "modified");
            }
        }

        for (String file : fmap.keySet()) {
            File f = Utils.join(file);
            // exception!!: the files which is staged for removal
            if (!f.exists() && !removed.contains(file)) {
                modified.put(getRelativePath(file), "deleted");
            }
        }
        return modified;
    }

    /**
     * Print a list of untracked files
     */
    public static void showUntrackedFiles() {
        System.out.println("=== Untracked Files ===");
        List<String> untracked = getUntrackedList();

        for (String file : untracked) {
            System.out.println(file);
        }
    }

    /**
     * Checks all files in working directory whether they are untracked or not
     * @return
     */
    private static List<String> getUntrackedList() {
        StageArea sa = StageArea.stageArea();
        HashMap<String, String> fmap = getCurrBranch().getPointer().getfmap();

        List<String> flist = Utils.plainFilenamesIn(CWD);
        List<String> untracked = new ArrayList<String>();
        for (String file : flist) {
            String path = Utils.join(CWD, file).getPath();
            // if this file can't be seen in HEAD commit and staging area
            if (fmap.get(path) == null && sa.getTracked().get(path) == null && !sa.getRemoved().contains(path)) {
                untracked.add(file);
            }
        }
        Collections.sort(untracked);
        return untracked;
    }

    /**
     * Add a file with given name
     * (*) git add
     * @param filename
     */
    public static void add(String filename) {
        File file = Utils.join(CWD, filename);

        String filepath = file.getPath();
        StageArea sa = StageArea.stageArea();

        if (!file.exists() && sa.getTracked().get(filepath) == null) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        // exception: if the tracked file have been deleted -> should be capable of staging the file
        if (!file.exists() && sa.getTracked().get(filepath) != null) {
            sa.getRemoved().add(filepath);
            sa.saveStage();
            return;
        }

        // If the current working version of the file is identical to the version in the current commit,
        // do not stage it to be added, and remove it from the staging area if it is already there.
        Commit comm = getCurrBranch().getPointer();
        String prevBlobId = comm.getfmap().get(filepath);
        String newBlobId = Blob.generateId(file);

        // compare the id --> if they are same, then the file has not been changed.
        if (prevBlobId != null && prevBlobId.equals(newBlobId)) {
            // remove it from the staging area if it is already there.
            if (sa.getAdded().get(filepath) != null) {
                sa.getAdded().remove(filepath);
                sa.saveStage();
            }
            return;
        }

        // add to the stageArea
        sa.add(file);
        sa.saveStage();
    }

    /**
     * Remove a file with given file name
     * (*) git rm
     * @param filename
     */
    public static void rm(String filename) {
        File file = Utils.join(CWD, filename);

        StageArea sa = StageArea.stageArea();
        sa.rm(file);
        sa.saveStage();
    }

    /**
     * Finds commits with given message and prints them
     * @param message
     */
    public static void find(String message) {
        int num = 0;    // the number of Commits with given message

        Logs logs = Logs.loadLogs();

        ArrayList<Logs.Commits> comms = logs.getLog();
        for (Logs.Commits comm : comms) {
            if (comm.getCommit().getMessage().equals(message)) {
                System.out.println(comm.getCommit().getId());
                num += 1;
            }
        }
        if (num == 0) {
            // No commit found
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    /**
     * Prints all commits log (for all branches)
     */
    public static void global_log() {
        Logs logs = Logs.loadLogs();
        ArrayList<Logs.Commits> comms = logs.getLog();
        for (Logs.Commits comm : comms) {
            System.out.println(Commit.printLog(comm.getCommit()));
        }
    }

    /**
     * Create a commit from current StageArea
     * (*) git commit
     * @param message
     */
    public static void commit(String message) {
        StageArea sa = StageArea.stageArea();
        // if no files have been staged
        if (sa.notStaged()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        // Every commit must have a non-blank message.
        if (message.isBlank()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }

        Branch brr = getCurrBranch();
        String parentId = brr.getPointer().getId();
        ArrayList<String> parentIds = new ArrayList<String>();
        parentIds.add(parentId);
        Date now = new Date();
        HashMap<String, String> fmap = StageArea.stageArea().getTrackedCommit();

        Commit comm = new Commit(parentIds, fmap, now, message);
        comm.saveCommit();
        Logs logs = Logs.loadLogs();
        logs.appendItem(getCurrBranch().getName(), comm);

        // change the HEAD
        setCurrBranchCommit(comm);

        StageArea.resetStageArea();
    }

    /**
     * Check out branch with given branch name
     * (*) git checkout [bname]
     * @param bname
     */
    public static void checkoutBranch(String bname) {
        Branch brr = Branch.loadBranch(bname);
        StageArea sa = StageArea.stageArea();
        String currName = getCurrBranch().getName();

        /**
         * Perform this check before doing anything else. Do not change the CWD.
         */

        // If no branch with that name exists
        if (brr == null) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }

        // If that branch is the current branch
        if (bname.equals(getCurrBranch().getName())) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        // If a working file is untracked in the current branch and would be overwritten by the checkout
        if (!Repository.getUntrackedList().isEmpty()) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }

        /**
         * Main Part
         */
        Commit comm = brr.getPointer();
        HashMap<String, String> fmap = comm.getfmap();

        // delete all files before checking out each files
        List<String> files = Utils.plainFilenamesIn(CWD);
        for (String file : files) {
            File f = Utils.join(CWD, file);
            f.delete();
        }

        // load all files in the target commit
        for (String file : fmap.keySet()) {
            String blobId = fmap.get(file);
            Blob blob = Blob.loadBlob(blobId);
            Blob.writeContents(blob);
        }

        // the given branch will now be considered the current branch (HEAD)
        setCurrBranch(bname);

        // The staging area is cleared, unless the checked-out branch is the current branch
        if (!currName.equals(bname)) {
            StageArea.resetStageArea();
        }

    }

    /**
     * Check out a specific file with given name
     * (*) git checkout -- [filename]
     * filename should be in a form of relative paths
     * @param fname
     */
    public static void checkoutFile(String fname) {
        File file = Utils.join(CWD, fname);
        String filepath = file.getPath();

        Branch curr = getCurrBranch();
        Commit comm = curr.getPointer();
        if (comm.getfmap().get(filepath) == null) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        String blobId = curr.getPointer().getfmap().get(filepath);
        if (blobId != null) {
            Blob blob = Blob.loadBlob(blobId);
            Blob.writeContents(blob);
        }
    }

    /**
     * Checkout specific files in specific commit with given file name and commit id respectively
     * (*) git checkout [commit id] -- [file name]
     * @param commitId
     * @param filename
     */
    public static void checkoutFileCommit(String commitId, String filename) {
        // bug fixed: the key is "absolute path", not "relative path"!
        File file = Utils.join(CWD, filename);
        String filepath = file.getPath();

        Logs logs = Logs.loadLogs();

        ArrayList<Logs.Commits> commitLogs = logs.getLog();

        Commit targetCommit = null;

        for (Logs.Commits comm : commitLogs) {
            Commit commit = comm.getCommit();
            if (commit.getId().equals(commitId)) {
                targetCommit = commit;
            }
        }

        if (targetCommit == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        String blobId = targetCommit.getfmap().get(filepath);
        if (blobId == null) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        Blob blob = Blob.loadBlob(blobId);
        Blob.writeContents(blob);
    }

    /**
     * Reset working directory with the status of specific commit with given commit id
     * (*) git reset
     * @param commId
     */
    public static void reset(String commId) {
        Logs logs = Logs.loadLogs();
        Commit targetCommit = null;

        ArrayList<Logs.Commits> comms = logs.getLog();
        for (Logs.Commits comm : comms) {
            if (comm.getCommit().getId().equals(commId)) {
                targetCommit = comm.getCommit();
            }
        }

        // If no commit with the given id exists
        if (targetCommit == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        // If a working file is untracked in the current branch and would be overwritten by the reset
        if (!getUntrackedList().isEmpty()) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }

        // delete all files before checking out each files
        List<String> files = Utils.plainFilenamesIn(CWD);
        for (String file : files) {
            File f = Utils.join(CWD, file);
            f.delete();
        }

        HashMap<String, String> fmap = targetCommit.getfmap();
        for (String file : fmap.keySet()) {
            String blobId = fmap.get(file);
            Blob blob = Blob.loadBlob(blobId);
            Blob.writeContents(blob);
        }

        // moves the current branch's head to that commit node
        setCurrBranchCommit(targetCommit);

        // The staging area is cleared
        StageArea.resetStageArea();
    }

    /**
     * Merge current branch with given branch, which is corresponding given branch name
     * @param bname
     */
    public static void merge(String bname) {
        Branch brr = Branch.loadBranch(bname);
        Branch curr = getCurrBranch();
        StageArea sa = StageArea.stageArea();

        /**
         * Complicated Failure Cases
         */

        // If there are staged additions or removals present
        if (!sa.notStaged()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }

        // If a branch with the given name does not exist
        if (brr == null) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        // If attempting to merge a branch with itself
        if (bname.equals(curr.getName())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        // If merge would generate an error because the commit that it does has no changes in it,
        // just let the normal commit error message for this go through.

        // Two Nodes for LCA
        Commit comm1 = getCurrBranch().getPointer();
        Commit comm2 = brr.getPointer();

        // If an untracked file in the current commit would be overwritten or deleted by the merge
        if (!Repository.getUntrackedList().isEmpty()) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }

        /**
         * Last check before Main Part of Merge
         */

        // LCA; latest common ancestor
        Commit lca = LCAwithId(comm1.getId(), comm2.getId());

        // If the split point is the same commit as the given branch,
        // then we do nothing; the merge is complete
        if (comm1.equals(lca)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }

        // If the split point is the current branch,
        // then the effect is to check out the given branch
        if (comm2.equals(lca)) {
            Repository.checkoutBranch(bname);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }

        /**
         * Main Part of Merge
         */

        // <1>
        // Any files that have been modified in the given branch since the split point,
        // but not modified in the current branch since the split point should be changed to their versions in the given branch
        HashMap<String, String> lcaFmap = lca.getfmap();
        HashMap<String, String> currFmap = curr.getPointer().getfmap();
        HashMap<String, String> givenFmap = brr.getPointer().getfmap();

        if (currFmap.equals(givenFmap)) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        // file list of which the files have been modified in the given branch but same in current branch
        // modified -> should be automatically checked out and staged
        HashMap<String, String> modified = new HashMap<>();
        for (String file : givenFmap.keySet()) {
            boolean fileExistsAll = lcaFmap.get(file) != null && currFmap.get(file) != null && givenFmap.get(file) != null;
            boolean modifiedInGivenBranch = !currFmap.get(file).equals(givenFmap.get(file));
            boolean sameInCurrentBranch = lcaFmap.get(file).equals(currFmap.get(file));
            if (fileExistsAll && modifiedInGivenBranch && sameInCurrentBranch) {
                modified.put(file, givenFmap.get(file));
            }
        }

        // checked out and staged
        for (String file : modified.keySet()) {
            Repository.checkoutFileCommit(comm2.getId(), Repository.getRelativePath(file));
            File f = new File(file);
            sa.add(f);
            sa.saveStage();
        }

        // <2>
        // Any files that have been modified in the current branch
        // but not in the given branch since the split point should stay as they are.
        HashMap<String, String> preserved = new HashMap<>();
        for (String file : currFmap.keySet()) {
            boolean fileExistsAll = lcaFmap.get(file) != null && currFmap.get(file) != null && givenFmap.get(file) != null;
            boolean modifiedInCurrentBranch = !currFmap.get(file).equals(lcaFmap.get(file));
            boolean sameInGivenBranch = lcaFmap.get(file).equals(givenFmap.get(file));
            if (fileExistsAll && modifiedInCurrentBranch && sameInGivenBranch) {
                preserved.put(file, currFmap.get(file));
            }
        }

        // <3>
        for (String file : currFmap.keySet()) {
            boolean fileExistsAll = lcaFmap.get(file) != null && currFmap.get(file) != null && givenFmap.get(file) != null;
            boolean currbrrIsSameWithGivenbrr = currFmap.get(file).equals(givenFmap.get(file));
            if (fileExistsAll && currbrrIsSameWithGivenbrr) {
                preserved.put(file, currFmap.get(file));
            }
        }

        // <4>
        // Any files that were not present at the split point and are present only
        // in the current branch should remain as they are.
        for (String file : currFmap.keySet()) {
            boolean notPresentInLCA = lcaFmap.get(file) == null;
            boolean presentInCurrBrr = givenFmap.get(file) == null;
            if (notPresentInLCA && presentInCurrBrr) {
                preserved.put(file, currFmap.get(file));
            }
        }


        // <5>
        // Any files that were not present at the split point and are present only in the given branch should be checked out and staged.
        // modified2 -> should be automatically checked out and staged
        HashMap<String, String> modified2 = new HashMap<>();
        for (String file : givenFmap.keySet()) {
            boolean notPresentInLCA = lcaFmap.get(file) == null;
            boolean notPresentInCurrBrr = currFmap.get(file) == null;
            if (notPresentInLCA && notPresentInCurrBrr) {
                modified2.put(file, givenFmap.get(file));
            }
        }

        // checked out and staged
        for (String file : modified2.keySet()) {
            Repository.checkoutFileCommit(comm2.getId(), Repository.getRelativePath(file));
            File f = new File(file);
            sa.add(f);
            sa.saveStage();
        }

        // <6>
        // Any files present at the split point, unmodified in the current branch,
        // and absent in the given branch should be removed (and untracked)
        ArrayList<String> removed = new ArrayList<>();
        for (String file : lcaFmap.keySet()) {
            boolean presentInLCA = lcaFmap.get(file) != null;
            boolean lcaSameWithCurrBrr = lcaFmap.get(file).equals(currFmap.get(file));
            boolean notPresentInGivenBrr = givenFmap.get(file) == null;
            if (presentInLCA && lcaSameWithCurrBrr && notPresentInGivenBrr) {
                removed.add(file);
            }
        }

        // <7>
        // Any files present at the split point, unmodified in the given branch,
        // and absent in the current branch should remain absent.
        for (String file : lcaFmap.keySet()) {
            boolean givenSameWithLCA = givenFmap.get(file).equals(lcaFmap.get(file));
            boolean notPresentInCurrBrr = currFmap.get(file) == null;
            if (givenSameWithLCA && notPresentInCurrBrr) {
                removed.add(file);
            }
        }

        // <8>
        // Any files modified in different ways in the current and given branches are in conflict.
        // 1) the contents of both are changed and different from other
        // 2) the contents of one are changed and the other file is deleted
        // 3) file was absent at the split point and has different contents in the given and current branches

        HashMap<String, String> conflicted = new HashMap<>();
        boolean isConflict = false;

        /**
         * This following codes for three cases will be optimized soon. The inner code is the same..
         */

        // <8> - case1
        for (String file : currFmap.keySet()) {
            boolean fileExistsAll = lcaFmap.get(file) != null && currFmap.get(file) != null && givenFmap.get(file) != null;
            boolean currIsNotSameWithGivenBrr = !currFmap.get(file).equals(givenFmap.get(file));
            if (fileExistsAll && currIsNotSameWithGivenBrr) {
                addConflicted(file, currFmap, givenFmap, conflicted);
                isConflict = true;
            }
        }

        // <8> - case2
        for (String file : currFmap.keySet()) {
            boolean existsInLCA = lcaFmap.get(file) != null;
            boolean notExistsInGiven = givenFmap.get(file) == null;
            boolean sameInCurrent = !currFmap.get(file).equals(lcaFmap.get(file));
            boolean changedInGiven = !givenFmap.get(file).equals(lcaFmap.get(file));

            boolean existsInCurr = currFmap.get(file) == null;
            if ((existsInLCA && sameInCurrent && notExistsInGiven) &&
                    (existsInLCA && existsInCurr && changedInGiven)) {
                addConflicted(file, currFmap, givenFmap, conflicted);
                isConflict = true;
            }
        }

        // <8> - case3
        for (String file : currFmap.keySet()) {
            boolean existsInLCA = lcaFmap.get(file) == null;
            boolean notInCurrAndGiven = currFmap.get(file) != null && givenFmap.get(file) != null;
            boolean notSameInCurrAndGiven = !currFmap.get(file).equals(givenFmap.get(file));
            if (existsInLCA && notInCurrAndGiven && notSameInCurrAndGiven) {
                addConflicted(file, currFmap, givenFmap, conflicted);
                isConflict = true;
            }
        }

        // tracked file list
        HashMap<String, String> tracked = new HashMap<>();
        tracked.putAll(modified);
        tracked.putAll(modified2);
        tracked.putAll(conflicted);
        tracked.putAll(preserved);
        for (String file : removed) {
            tracked.remove(file);  // removal
        }

        ArrayList<String> parentIds = new ArrayList<>();
        parentIds.add(curr.getPointer().getId());
        parentIds.add(brr.getPointer().getId());

        Date now = new Date();

        // commit message for merge commit
        String commitMsg = String.format("Merged %s into %s.", bname, curr.getName());

        Commit mergeCommit = new Commit(parentIds, tracked, now, commitMsg);
        mergeCommit.saveCommit();

        Logs logs = Logs.loadLogs();
        logs.appendItem(curr.getName(), mergeCommit);

        // if the merge encountered a conflict, print the message on the console
        if (isConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /**
     * Returns the least common ancestor of given two commits
     * LINK: https://www.crocus.co.kr/660
     * @param c1    first node for LCA target
     * @param c2    second node for LCA target
     * @return      LCA node of given two nodes
     */
    private static Commit LCA(Commit c1, Commit c2) {
        // calculate tree label for two given Commits
        int l1 = getLevel(c1);
        int l2 = getLevel(c2);

        int balance = l1 - l2;
        balanceNode(c1, c2, balance);
        while (true) {
            boolean c1IsRootNode = Commit.getParent(c1.getId()) == null;
            boolean c2IsRootNode = Commit.getParent(c2.getId()) == null;
            boolean c1IsSameWithc2 = c1.equals(c2);
            if (c1IsRootNode || c2IsRootNode || c1IsSameWithc2) {
                break;
            }
            c1 = Commit.loadCommit(Commit.getParent(c1.getId()));
            c2 = Commit.loadCommit(Commit.getParent(c2.getId()));
        }
        return c1;
    }

    /**
     * Balance two given commit nodes depending on their balance difference
     * @param c1
     * @param c2
     * @param balance   level(c1) - level(c2)
     */
    private static void balanceNode(Commit c1, Commit c2, int balance) {
        if (balance == 0)
            return;
        if (balance > 0) {
            // c1 is deeper than c2 -> c1 should be go upstairs
            for (int i = 0; i < balance; i++) {
                c1 = Commit.loadCommit(Commit.getParent(c1.getId()));
            }
        }
        else {
            // c2 is deeper than c1 -> c2 should be go upstairs
            int b = -balance;
            for (int i = 0; i < b; i++) {
                c2 = Commit.loadCommit(Commit.getParent(c2.getId()));
            }
        }
    }

    /**
     * Find least common anscestor of given commits
     * with their commit id
     * @param c1
     * @param c2
     * @return
     */
    private static Commit LCAwithId(String c1, String c2) {
        Commit cc1 = Commit.loadCommit(c1);
        Commit cc2 = Commit.loadCommit(c2);
        return Repository.LCA(cc1, cc2);
    }

    /**
     * Returns the level of given commit node
     * @param comm  given commit node
     * @return      level of given commit node (level of root node = 0)
     */
    private static int getLevel(Commit comm) {
        if (Commit.getParent(comm.getId()) == null)
            return 0;
        else {
            return 1 + getLevel(Commit.loadCommit(
                    Commit.getParent(comm.getId())
            ));
        }
    }

    /**
     * Mark conflicted file and write them in working directory
     * See also: Repository.conflicted method
     * @param file
     * @param currFmap
     * @param givenFmap
     * @param conflicted
     */
    private static void addConflicted(String file, HashMap<String, String> currFmap, HashMap<String, String> givenFmap, HashMap<String, String> conflicted) {
        String currFile = new String(Blob.loadBlob(currFmap.get(file)).getContentRaw());
        String givenFile = new String(Blob.loadBlob(givenFmap.get(file)).getContentRaw());
        String conflict = Repository.conflicted(currFile, givenFile);

        // write file ...
        File f = new File(file);
        Utils.writeContents(f, conflict);
        Blob blob = new Blob(f);
        blob.saveBlob();

        // add to the conflicted file list
        conflicted.put(file, blob.getId());
    }

    /**
     * Return the content of conflicted file with given form
     *
     * <<<<<<< HEAD
     * [current branch file content]
     * =======
     * [given branch file content]
     * >>>>>>>
     *
     * @param currFile
     * @param givenFile
     * @return
     */
    private static String conflicted(String currFile, String givenFile) {
        String result = "<<<<<<< HEAD" + "\n";
        result += currFile;
        result += "=======" + "\n";
        result += givenFile;
        result += ">>>>>>>";

        return result;
    }
}
