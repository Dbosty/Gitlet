package gitlet;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.ArrayList;

/** Creates a Staging Area Class.
 * @author Daniel Bostwick */
public class Staged implements Serializable {

    /** Creates LinkedHashMap to store all Files that are to be added.
     * LinkedHashMap{fileName, SHA1 of Contents}.
     */
    private LinkedHashMap<String, String> addedFiles;

    /** Creates an ArrayList of Files to be removed.
     *  ArrayList{fileName}.
     */
    private ArrayList<String> removedFiles;

    /** Staging Area Constructor. */
    public Staged() {
        addedFiles = new LinkedHashMap<>();
        removedFiles = new ArrayList<>();
    }

    /** Adds fileName and SHA1 ID into LinkedHashMap.
     * @param fileName Name of file.
     * @param sha1 File SHA ID. */
    public void add(String fileName, String sha1) {
        addedFiles.put(fileName, sha1);
    }

    /** Clears all contents from Staging Area. */
    public void clear() {
        addedFiles = new LinkedHashMap<>();
        removedFiles = new ArrayList<>();
    }

    /** Adds files to be removed.
     * @param fileName Name of file. */
    public void toBeRemoved(String fileName) {
        removedFiles.add(fileName);
    }

    /** Grabs a LinkedHashMap of added files.
     * @return */
    public LinkedHashMap<String, String> getAdded() {
        return addedFiles;
    }

    /** Grabs an ArrayList of removed files.
     * @return */
    public ArrayList<String> getRemoved() {
        return removedFiles;
    }
}
