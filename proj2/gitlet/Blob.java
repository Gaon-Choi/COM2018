package gitlet;


import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import static gitlet.Repository.GITLET_OBJECTS;

/**
 * Blob is a dump file for specific object.
 * The target file contents and its metadata(such as file path) can create sha-1 id, which can represent the file with its version info.
 * The contents of the target file is deserialized to a sequence of bytes.
 *
 * The first two digit represents the folder name for the blob file.
 * The remains represent the file name for the blob file.
 *
 * e.g. Blob ID: 563d51a65ce82240ffaed6c4f396c9ddcfa608f7
 *      - folder name: 56
 *      - file   name: 3d51a65ce82240ffaed6c4f396c9ddcfa608f7
 *
 * Blob is a target concept for "persistence", which should be stored locally as a separate file.
 * --> "implements Serializable"
 *
 *
 * @author Gaon Choi
 *
 * Reference
 *  1) "what is blob" -> https://heropy.blog/2019/02/28/blob/
 *  2) Git Blob -> https://git-scm.com/book/en/v2/Git-Internals-Git-Objects
 *  3) Blob & Hashing -> https://kotlinworld.com/300
 *  4) Github Blob -> https://github.com/smirnovice/GitHubAccess/blob/979a10be03ee2c93595760489ba54a4722f39fef/GitHubbAccess/src/org/eclipse/egit/github/core/Blob.java
 *  5) https://github.com/sxdcfvazsx/20thingsilearned/blob/eedb139a30cf5906aeb6d566aeda09c4add45879/war/WEB-INF/lib/com/googlecode/objectify/impl/emul/com/google/appengine/api/datastore/Blob.java
 *
 * Personal Document
 *  https://beaded-dewberry-37d.notion.site/GitLet-Technical-Document-f1681cac21de4ec286cedadecb3e74d8
 */
public class Blob implements Serializable {
    /**
     * The target file to convert to blob object
     */
    private File srcFile;

    /**
     * The contents of the file with byte sequence
     */
    private byte[] content;

    /**
     * The SHA-1 id which represents the Blob object
     */
    private String id;

    /**
     * The actual file location of the Blob object
     */
    private File blobFile;

    // The directory name for blob file
    String dir;

    // the file name for blob file
    String file;

    /**
     * Constructor for Blob class
     * creates a blob instance for the given File.
     *
     * @param srcFile
     */
    public Blob(File srcFile) {
        this.srcFile = srcFile;
        if (!srcFile.exists()) {
            // System.out.println("ERROR Blob: " + srcFile.getPath());
            System.exit(0);
        }
        this.content = Utils.readContents(srcFile);
        this.id = Utils.sha1(this.srcFile.getPath(), this.content);

        this.dir = id.substring(0, 2);
        this.file = id.substring(2);
        this.blobFile = Utils.join(GITLET_OBJECTS, dir, file);
    }

    /**
     * Saves this blob instance inside the .gitlet folder
     * -- "Persistence"
     */
    public void saveBlob() {
        File folder = Utils.join(GITLET_OBJECTS, this.dir);
        folder.mkdir();
        Utils.writeObject(this.blobFile, this);
    }

    /**
     * Loads blob instance with given blobId
     *
     * @param id
     * @return
     */
    public static Blob loadBlob(String id) {
        String dir = id.substring(0, 2);
        String file = id.substring(2);
        File blobFile = Utils.join(GITLET_OBJECTS, dir, file);
        return Utils.readObject(blobFile, Blob.class);
    }

    /**
     * Returns the contents of the blob with "utf-8" encoded format
     *
     * @return
     * @throws UnsupportedEncodingException
     */
    public String getContent() throws UnsupportedEncodingException {
        // blob bytes -> String (UTF-8)
        // https://stackoverflow.com/questions/3890792/how-to-view-blob-content
        String str = new String(this.content, "UTF-8");
        return str;
    }

    /**
     * Returns the contents of the blob with a format of series of bytes
     *
     * @return
     */
    public byte[] getContentRaw() {
        return this.content;
    }

    /**
     * Getter of blobId
     * @return
     */
    public String getId() {
        return this.id; // Utils.sha1(this.srcFile.getPath(), this.content)
    }

    /**
     * Sha-1 id generator for given File
     *
     * @param file
     * @return
     */
    public static String generateId(File file) {
        String path = file.getPath();
        byte[] content = Utils.readContents(file);
        return Utils.sha1(path, content);
    }

    /**
     * Getter of blob file
     * @return
     */
    public File getBlobFile() {
        return this.blobFile;
    }

    /**
     * Loads the saved blob into the working directory
     *
     * @param blob
     */
    public static void writeContents(Blob blob) {
        Utils.writeContents(Utils.join(blob.srcFile.getPath()), blob.getContentRaw());
    }
}
