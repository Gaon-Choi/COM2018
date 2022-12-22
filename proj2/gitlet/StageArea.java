package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static gitlet.Repository.GITLET_STAGE;
import static gitlet.Repository.getCurrBranch;

/**
 * Staging Area
 *  This area is for storing changing status, which is valid until one commit is created.
 *
 *  "git add" can make changes with new folders or files within this StageArea.
 *  "git rm" can unstage the added files or remove file from the working directory.
 *  "git commit" can convert this StageArea into a commit.
 *
 * @author Gaon Choi
 */
public class StageArea implements Serializable {
    /**
     * Structure of HashMap
     * file path <--> blob id
     */

    // staged files
    private HashMap<String, String> added = new HashMap<>();

    // removed files
    private ArrayList<String> removed = new ArrayList<>();

    // tracked files
    private HashMap<String, String> tracked = new HashMap<>();

    // non-argument constructor for StageArea
    public StageArea() {
        this.added = new HashMap<>();
        this.removed = new ArrayList<>();
        this.tracked = new HashMap<>();
    }

    /**
     * constructor for StageArea, which is called for every "git commit"
     * @param tracked_
     */
    public StageArea(HashMap<String, String> tracked_) {
        this.added = new HashMap<>();
        this.removed = new ArrayList<>();
        this.tracked = new HashMap<>();
        this.tracked.putAll(tracked_);
    }

    /**
     * getter for this.added
     * @return
     */
    public HashMap<String, String> getAdded() {
        return this.added;
    }

    /**
     * getter for this.removed
     * @return
     */
    public ArrayList<String> getRemoved() {
        return this.removed;
    }

    /**
     * getter for this.tracked
     * @return
     */
    public HashMap<String, String> getTracked() {
        return this.tracked;
    }

    /**
     * "git add" command
     *      1) if the file with given file name doesn't exist in StageArea
     *          -> add it to the added list and tracked list
     *          -> save the newly-created blob for the file
     *
     *      2) if the file with given file name exists but the contents have been changed
     *          -> we can check it with sha-1 id
     *          -> substitute the file info in added list, tracked list with new info(blobId)
     *          -> save the newly-created blob for the changed file
     *
     *      3) if the file with given file name exists and the contents remain the same
     *          -> do nothing
     *
     * @param file
     */
    public void add(File file) {
        // HashMap: https://codechacha.com/ko/java-map-hashmap/
        String newpath = file.getPath();

        Blob newblob = new Blob(file);
        String newblobId = newblob.getId();
        String prevblobId = tracked.get(newpath);

        // case1: file doesn't exist (--> new file)
        if (prevblobId == null) {
            added.put(newpath, newblobId);
            tracked.put(newpath, newblobId);
            newblob.saveBlob();
        }

        // case2: file exists but the contents changed
        else if (!prevblobId.equals(newblobId)) {
            // delete previous information
            added.remove(newpath);
            removed.remove(newpath);
            tracked.remove(newpath);

            // update new information
            added.put(newpath, newblobId);
            tracked.put(newpath, newblobId);
            newblob.saveBlob();
        }

        // case3: file exists and the contents unchanged -> do nothing
        else {
            removed.remove(newpath);
        }

    }


    /**
     * "git rm" command
     * @param file
     */
    public void rm(File file) {
        String newpath = file.getPath();
        String addedId = this.added.get(newpath);       // check whether it is staged
        String trackedId = this.tracked.get(newpath);    // check whether it is tracked

        // if the file is neither staged nor tracked by the head commit
        if (addedId == null && trackedId == null) {
            System.out.println("No reason to remove the file");
            System.exit(0);
        }

        // unstage the file if it is currently staged for addition
        if (addedId != null) {
            added.remove(newpath);
            tracked.remove(newpath);
            return;
        }

        // If the file is tracked in the current commit,
        // stage it for removal and remove the file from the working directory
        // if the user has not already done so
        if (trackedId != null) {
            removed.add(newpath);
            // remove file from the working directory
            if (file.exists()) {
                // how to remove file in Java? --> https://javacpro.tistory.com/27
                file.delete();
            }
        }
    }

    /**
     * Saves current StageArea inside the .gitlet folder
     * -- "Persistence"
     */
    public void saveStage() {
        Utils.writeObject(GITLET_STAGE, this);
    }

    /**
     * Loads current StageArea inside the .gitlet folder
     * --> can be used anywhere, since this is defined as "static"
     * @return
     */
    public static StageArea stageArea() {
        StageArea stageArea = Utils.readObject(GITLET_STAGE, StageArea.class);
        return stageArea;
    }

    /**
     * Checks whether the current StageArea has nothing to do for the staged / removed files
     * @return
     */
    public boolean notStaged() {
        boolean a = this.added.isEmpty();
        boolean b = this.removed.isEmpty();
        return a && b;
    }

    /**
     * Returns the final tracked file list
     * (+) this.added
     * (-) this.removed
     * -- This method is called only when new commit is created.
     *
     * @return
     */
    public HashMap<String, String> getTrackedCommit() {
        HashMap<String, String> fmap = new HashMap<String, String>();
        fmap.putAll(this.tracked);  // addition
        for (String file : this.removed) {
            fmap.remove(file);  // removal
        }
        return fmap;
    }

    /**
     * Resets current StageArea
     * - vacate added list and removed list
     * - tracked file list must be preserved even when a new commit is created.
     */
    public static void resetStageArea() {
        HashMap<String, String> fmap_ = getCurrBranch().getPointer().getfmap();
        StageArea newSA = new StageArea(fmap_);
        newSA.saveStage();
    }

    /**
     * Returns a string which represents current StageArea
     * -- only for debugging
     *
     * @return
     */
    public String toString() {
        String temp = "";
        temp += "[STAGING AREA]\n";
        temp += "<added>" + "\n" + hashMapToString(this.added) + "\n";
        temp += "<removed>" + "\n" + arrayListToString(this.removed) + "\n";
        temp += "<tracked>" + "\n" + hashMapToString(this.tracked);
        return temp;
    }

    /**
     * Returns a string which represents given HashMap<String, String>
     * -- only for debugging
     *
     * @param hmap
     * @return
     */
    public static String hashMapToString(HashMap<String, String> hmap) {
        String tmp = "";
        for (Map.Entry<String, String> entry : hmap.entrySet()) {
            tmp += entry.getKey() + " <-> " + entry.getValue() + "\n";
        }
        return tmp;
    }

    /**
     * Returns a string which represents given ArrayList<String>
     * -- only for debugging
     *
     * @param arr
     * @return
     */
    public static String arrayListToString(ArrayList<String> arr) {
        String tmp = "";
        for (String str : arr) {
            tmp += str + "\n";
        }
        return tmp;
    }
}
