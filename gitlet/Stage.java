package gitlet;

import java.io.Serializable;

/** Stage object.
 * @author theresachen **/
public class Stage implements Serializable {

    /** Stage object constructor. */
    public Stage(String fileName, String blobRef) {
        _fileName = fileName;
        _blobRef = blobRef;
    }

    protected String _fileName;

    /** In SHA form. */
    protected String _blobRef;
}
