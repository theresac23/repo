package gitlet;

import java.io.Serializable;

/** Blob objects that will be stored in blob files.
 * @author theresachen **/
public class Blob implements Serializable {
    /** Blob constructor.**/
    public Blob(String contents) {
        _contents = contents;
        sha = Utils.sha1(_contents);
    }
    /** Contents of blobs. **/
    protected String _contents;

    /** This blob's sha value. **/
    protected String sha;
}
