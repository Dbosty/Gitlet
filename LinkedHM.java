package gitlet;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Set;

/** Creates a LinkedHashMap class.
 * @author Daniel Bostwick */
public class LinkedHM implements Serializable {

    /** LinkedHashMap Constructor for Commits. */
    public LinkedHM() {
        this.linkedCommit = new LinkedHashMap<>();
    }

    /** LinkedHashMap head.
     * @return */
    public Commit head() {
        Set<String> keySet = linkedCommit.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        return linkedCommit.get(keyArray[keyArray.length - 1]);
    }

    /** Adds to a LinkedHashMap Object.
     * @param sha The shaID of the added commit.
     * @param commits The commit object to be added. */
    public void add(String sha, Commit commits) {
        linkedCommit.put(sha, commits);
    }

    /** Clears a LinkedHashMap. */
    public void clear() {
        linkedCommit = new LinkedHashMap<>();
    }

    /** Gets a LinkedHashMap SHA.
     * @return */
    public String getSHA1() {
        return Utils.sha1(commit);
    }

    /** Gets the commits from a LinkedHashMap Object.
     * @return  */
    public LinkedHashMap<String, Commit> getCommits() {
        return linkedCommit;
    }

    /** Creates a LinkedHashMap of String and Commit.
     * <SHA1, Commit>. */
    private LinkedHashMap<String, Commit> linkedCommit;

    /** The key for the LinkedHashMap. */
    private String fileName;

    /** The value for the LinkedHashMap.  */
    private String sha1;

    /** The Commit. */
    private Commit commit;
}
