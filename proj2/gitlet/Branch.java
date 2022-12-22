package gitlet;

import gitlet.Repository;
import java.io.File;
import java.io.Serializable;


/**
 * Branch
 *  This class represents a branch in Git.
 *  A branch can point one commits including the initial commit.
 *  Branch allows the developers work independently starting some initial point.
 *  Multiple Branches can be merged to create a newly combined Branch.
 *
 *  The initial Branch for the user will be "master"!
 *
 * @author Gaon Choi
 *
 */
public class Branch implements Serializable {
    // branch name
    private String name;

    // the commit to which the branch points
    private Commit pointer;

    /**
     * Constructor for Branch class
     * @param name
     * @param p
     */
    public Branch(String name, Commit p) {
        this.name = name;
        this.pointer = p;
    }

    /**
     * Constructor for Branch class
     *
     * @param p
     */
    public Branch(Commit p) {
        this.name = "master";
        this.pointer = p;
    }

    /**
     * Getter for the branch name
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * Getter for the commit, to which this branch points
     * @return
     */
    public Commit getPointer() {
        return this.pointer;
    }

    /**
     * Loads a branch with given name
     * -- if no branch found -> return null
     *
     * @param name
     * @return
     */
    public static Branch loadBranch(String name) {
        File file = Utils.join(Repository.GITLET_HEADS, name);
        if (file.exists()) {
            return Utils.readObject(file, Branch.class);
        }
        return null;
    }

    /**
     * Saves this branch instance inside the .gitlet folder
     * -- "Persistence"
     */
    public void saveBranch() {
        File file = Utils.join(Repository.GITLET_HEADS, name);
        Utils.writeObject(file, this);
    }

    /**
     * Change the commit to which the branch points
     * @param p
     */
    public void setPointer(Commit p) {
        this.pointer = p;
    }
}
