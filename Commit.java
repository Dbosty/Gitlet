package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.LinkedHashMap;

/** Class that creates Commits.
 * @author Daniel Bostwick*/

public class Commit implements Serializable {

    /** Commit Constructor.
     * @param blobs the blobs.
     * @param commitParent the parent commit.
     * @param msg the commit message. */
    public Commit(String msg, String commitParent,
                  LinkedHashMap<String, String> blobs) {
        this.message = msg;
        this.parent = commitParent;
        this.addedBlobs = blobs;
        this.timestamp = getTimestamp();
        this.sha1Commit = Utils.sha1(Utils.serialize(this));
    }

    /** This gets a commit message.
     * @return message.*/
    public String getMessage() {
        return this.message;
    }

    /** Gets the timestamp of the Commit.
     * @return timestamp. */
    public String getTimestamp() {
        if (message.equals("initial commit")) {
            this.timestamp = "Wed Dec 31 16:00:00 1969 -0800";
        } else {
            this.timestamp = time.format(new Date());
        }
        return this.timestamp;
    }

    /** Gets the parent of the Commit.
     * @return Parent. */
    public String getParent() {
        return this.parent;
    }

    /** String fileName, String sha1.
     * @return addedBlobs. */
    public LinkedHashMap<String, String> getBlobs() {
        return addedBlobs;
    }

    /** Gets the SHA1 ID of the Commit.
     * @return SHA of commit. */
    public String getCommitSHA() {
        return sha1Commit;
    }

    /** A Commit message. */
    private String message;

    /** A Commit timestamp for the initial Commit. */
    private String timestamp;

    /** A Commit parent. */
    private String parent;

    /** A Commit timestamp for every Commit besides the initial Commit. */
    private SimpleDateFormat time =
            new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");

    /** Commits' SHA1 ID. */
    private String sha1Commit;

    /** Creates a HashMap to store blobs. */
    private LinkedHashMap<String, String> addedBlobs;
}

