package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import static gitlet.Repository.GITLET_LOG;

/**
 * Represents a series of branch-commit pairs
 *
 * @author Gaon Choi
 */
public class Logs implements Serializable {
    private ArrayList<Commits> log;

    /**
     * Default Constructor for Logs class
     */
    public Logs() {
        this.log = new ArrayList<Commits>();
    }

    /**
     * Getter for this.log
     * log is an arrayList of Commits (in reversed chronological order)
     * @return
     */
    public ArrayList<Commits> getLog() {
        return this.log;
    }

    /**
     * Adds branch-commit pair to the log arrayList
     * @param branch
     * @param comm
     */
    public void appendItem(String branch, Commit comm) {
        Commits commits = new Commits(branch, comm);
        this.log.add(0, commits);
        this.saveLogs();
    }

    /**
     * Loads the saved log from the .gitlet folder
     * @return
     */
    public static Logs loadLogs() {
        return Utils.readObject(GITLET_LOG, Logs.class);
    }

    /**
     * Saves this log inside the .gitlet folder
     * -- "Persistence"
     */
    public void saveLogs() {
        Utils.writeObject(GITLET_LOG, this);
    }

    /**
     * Represents a branch-commit pair
     */
    public class Commits implements Serializable {
        // Branch name of the commit
        private String branch;

        // The target Commit
        private Commit comm;

        /**
         * Constructor for Commits class
         * @param brch
         * @param comm
         */
        public Commits(String brch, Commit comm) {
            this.branch = brch;
            this.comm = comm;
        }

        /**
         * Getter for this.comm
         * @return
         */
        public Commit getCommit() {
            return this.comm;
        }

        public String getBranchName() {
            return this.branch;
        }
    }
}
