package gitlet;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** A serializable commit object.
 * @author theresachen **/
public class Commit implements Serializable {

    /** A serializable commit object constructor.
     * @param message idk
     * @param blobs idk
     * @param parentSHA idk
     * @param timeStamp idk */
    public Commit(String message, String[] parentSHA, String timeStamp,
                   HashMap<String, String> blobs) {
        this._message = message;
        this._parentSHA = parentSHA;
        this._timeStamp = timeStamp;
        this._blobs = blobs;
        List<Object> toSHA = new ArrayList<>();
        for (String ref : blobs.values()) {
            toSHA.add(ref);
        }
        toSHA.add(parentSHA[0]);
        toSHA.add(message);
        toSHA.add(timeStamp);
        this._sha = Utils.sha1(toSHA);
    }

    /** Commit message that accompanies commit command. **/
    protected String _message;

    /** This commit's SHA-1 id. **/
    protected String _sha;

    /** This commit's parent's SHA-1 value. **/
    protected String[] _parentSHA;

    /** Timestamp of commit. **/
    protected String _timeStamp;

    /** Copies of all the files tracked in this commit. **/
    protected HashMap<String, String> _blobs;

}
