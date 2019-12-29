package gitlet;

import java.io.Serializable;

/** Stage object.
 * @author theresachen **/
public class Stage implements Serializable {

    /** Stage object constructor.
     * @param blobRef idk
     * @param fileName idk */
    public Stage(String fileName, String blobRef) {
        _fileName = fileName;
        _blobRef = blobRef;
    }

    /** Variable. */
    protected String _fileName;

    /** Variable. */
    protected String _blobRef;
}
