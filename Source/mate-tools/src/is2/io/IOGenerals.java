package is2.io;

/**
 * Base class for "CONLLReader" and "CONLLWriter" Class
 * 
 * @author Dr. Bernd Bohnet, 18.08.2011
 *
 */
public abstract class IOGenerals {

    // some constants
    public static final String US = "_";
    public static final String REGEX = "\t";
    public static final String STRING = "*";
    public static final String PIPE = "\\|";
    public static final String NO_TYPE = "<no-type>";
    public static final String ROOT_POS = "<root-POS>";
    public static final String ROOT_LEMMA = "<root-LEMMA>";
    public static final String ROOT = "<root>";
    public static final String EMPTY_FEAT = "<ef>";
    // the different readers 
    public static final int F_CONLL09 = 0;
    public static final int F_ONE_LINE = 1;
    // normalization of the input
    public static final String NUMBER = "[0-9]+|[0-9]+\\.[0-9]+|[0-9]+[0-9,]+";
    public static final String NUM = "<num>";
}