package gitlet;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Repository.GITLET_OBJECTS;

/**
 * Represents a gitlet Commit object.
 *
 *
 *  @author Gaon Choi
 */
public class Commit implements Serializable, Cloneable {
    /** The SHA id of this Commit. */
    /**
     * GIT SHA-1
     * SHA1-Hash(file size + a zero + the file contents)
     */
    private String id;

    /** The SHA ids of the parent Commit. */
    private ArrayList<String> parentIds;

    /** The date when this Commit is created. */
    private Date date;

    /** The message of this Commit. */
    private String message;

    /**
     * Tracked File List
     * This hashmap represents the whole file list when this commit was created ("git commit").
     * (+): insert  /  (-): delete
     */
    private HashMap<String, String> fmap;

    // link: https://www.java67.com/2013/01/how-to-format-date-in-java-simpledateformat-example.html
    // (sample) Thu Jan 1 09:00:00 1970 +0900
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
    /* TODO: fill in the rest of this class. */

    /**
     * Constructor for default Commit
     */
    public Commit() {
        this.parentIds = new ArrayList<String>();

        // https://needneo.tistory.com/25

        /**
         * Hash Map: filename : blob id(sha-1)
         */
        this.fmap = new HashMap<String, String>();
        this.date = new Date(0);    // 00:00:00 UTC, Thursday, 1 January 1970
                                    // represented internally by the time 0
        this.message = "initial commit";
        this.id = generateUID(this.date, this.message, this.parentIds, this.fmap);
    }

    /**
     * Constructor for Commit (general)
     * @param parentIds
     * @param date
     * @param message
     */
    public Commit(ArrayList<String> parentIds, HashMap<String, String> fmap, Date date, String message) {
        this.parentIds = (ArrayList<String>) parentIds;
        this.fmap = fmap;
        this.date = (Date) date;
        this.message = message;
        this.id = generateUID(this.date, this.message, this.parentIds, this.fmap);
    }

    /**
     * Getter for this.message
     * @return
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Getter for sha-1 id of Commit
     * @return
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sha-1 ID generator for Commit instance
     * @param date
     * @param message
     * @param parentIds
     * @param fmap
     * @return
     */
    private static String generateUID(Date date, String message, ArrayList<String> parentIds, Map<String, String> fmap) {
        String UID = Utils.sha1(date.toString(), message, parentIds.toString(), fmap.toString());
        return UID;
    }

    /**
     * This methods loads an Commit instance from a local file
     */
    public static Commit loadCommit(String id) {
        File file = Utils.join(GITLET_OBJECTS, getDir(id), getFile(id));
        Commit comm = Utils.readObject(file, Commit.class);
        return comm;
    }

    /**
     * This method saves Commit instance into local file
     */
    public void saveCommit() {
        File file_ = Utils.join(GITLET_OBJECTS, this.getDir(this.id));
        File file = Utils.join(file_, this.getFile(this.id));
        if (!file_.exists()) file_.mkdir();
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Utils.writeObject(file, this);
    }

    // source: https://coding-factory.tistory.com/126
    /**
     * Returns the directory name for given sha-1 id
     *
     * @param id
     * @return
     */
    private static String getDir(String id) {
        return id.substring(0, 2);
    }

    /**
     * Returns the dump file name for given sha-1 id
     *
     * @param id
     * @return
     */
    private static String getFile(String id) {
        return id.substring(2);
    }

    /**
     * Returns the string represents this.date
     *
     * @return
     */
    private String getDate() {
        return this.date.toString();
    }

    /**
     * Returns this.date
     *
     * @return
     */
    private Date getDateRaw() {
        return this.date;
    }

    /**
     * Prints log for given Commit instance
     *
     * @param comm
     * @return
     */
    public static String printLog(Commit comm) {
        String log = "";
        log = log + "===" + "\n";
        log += "commit " + comm.getId() + "\n";
        // merge -> prints two parent commitId
        if (comm.parentIds.size() > 1) {
            String parentId = String.format("Merge: %s %s", comm.parentIds.get(0), comm.parentIds.get(1));
            log += parentId + "\n";
        }
        log += "Date: " + DATE_FORMAT.format(comm.getDateRaw()) + "\n";
        log += comm.getMessage();
        if (Commit.getParent(comm.getId()) != null) {
            log += '\n';
        }
        return log;
    }

    /**
     * Gets the first parentId for Commit with given sha-1 commitId
     *
     * @param id
     * @return
     */
    public static String getParent(String id) {
        Commit comm = loadCommit(id);
        if (comm.parentIds.size() == 0)
            return null;
        return comm.parentIds.get(0);
    }

    /**
     * Returns the tracked file list for this Commit
     *
     * @return
     */
    public HashMap<String, String> getfmap() {
        return this.fmap;
    }

    /**
     * Returns a string which represents Commit instance
     * -- only for debugging
     *
     * @return
     */
    public String toString() {
        String tmp = "[COMMIT]\n";
        tmp += "id: " + this.getId() + "\n";
        tmp += "date: " + DATE_FORMAT.format(this.getDateRaw()) + "\n";
        tmp += "message: " + this.getMessage() + "\n";
        tmp += "parent id: " + this.parentIds.toString() + "\n";
        tmp += "file map: " + "\n";
        tmp += StageArea.hashMapToString(this.fmap);
        return tmp;
    }

    // clone method for Commit
    // LINK: https://velog.io/@tomato2532/Object.clone-%EC%96%95%EC%9D%80-%EB%B3%B5%EC%82%AC-%EA%B9%8A%EC%9D%80-%EB%B3%B5%EC%82%AC-%EB%B3%B5%EC%82%AC-%EC%83%9D%EC%84%B1%EC%9E%90
    @Override
    public Commit clone() throws CloneNotSupportedException{
        Commit clone = (Commit)super.clone();
        clone.parentIds = new ArrayList<>();
        clone.fmap = new HashMap<>();
        clone.id = this.getId();
        clone.date = (Date)this.getDateRaw().clone();
        clone.message = this.getMessage();

        clone.parentIds.addAll(this.parentIds);
        clone.fmap.putAll(this.fmap);
        return clone;
    }
}
