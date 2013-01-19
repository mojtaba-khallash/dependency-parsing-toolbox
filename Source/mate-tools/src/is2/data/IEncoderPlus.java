package is2.data;

import java.util.HashMap;

/**
 * @author Bernd Bohnet, 20.09.2009
 *
 *
 */
public interface IEncoderPlus extends IEncoder {

    final public static String NONE = "<None>";

    /**
     * @param spath
     * @param substring
     */
    public int register(String spath, String substring);

    /**
     * @return
     */
    public HashMap<String, Integer> getFeatureCounter();
}