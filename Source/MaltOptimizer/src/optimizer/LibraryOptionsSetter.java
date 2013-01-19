package optimizer;

import java.util.StringTokenizer;

/**
 * @author miguel
 *
 */
public class LibraryOptionsSetter {

    private String libraryOptions;
    static private LibraryOptionsSetter singleton = null;

    static public LibraryOptionsSetter getSingleton() {

        if (singleton == null) {
            singleton = new LibraryOptionsSetter("-s_4_-c_0.1");
        }
        return singleton;
    }

    public LibraryOptionsSetter(String loptions) {
        this.libraryOptions = loptions;
    }

    public void incrementC(double factor) {
        //-s_0_-t_1_-d_2_-g_0.2_-c_1.0_-r_0.4_-e_0.1"
        StringTokenizer st = new StringTokenizer(libraryOptions, "_");
        String tok;
        boolean nextTok = false;
        String oldCValue = "";
        int contTokens = 0;
        int whereC = 0;
        while (st.hasMoreTokens()) {
            tok = st.nextToken();
            //System.out.println(tok);
            if (nextTok) {
                oldCValue = tok;
                nextTok = false;
            }
            if (tok.equals("-c")) {
                nextTok = true;
                whereC = contTokens;
                //	System.out.println(tok);
            }
            contTokens += tok.length() + 1;
        }
        //System.out.println(whereC);
        double oldC = Double.parseDouble(oldCValue);
        double newC = oldC + factor;

        newC = redondear(newC);
        //System.out.println(newC);

        String newLibrary = libraryOptions.substring(0, whereC + 3);
        newLibrary += newC;
        newLibrary += libraryOptions.substring(whereC + 6, libraryOptions.length());

        libraryOptions = newLibrary;
        //System.out.println(newLibrary);
    }

    public String getC() {
        StringTokenizer st = new StringTokenizer(libraryOptions, "_");
        String tok;
        boolean nextTok = false;
        String oldCValue = "";
        int contTokens = 0;
        int whereC = 0;
        while (st.hasMoreTokens()) {
            tok = st.nextToken();
            //System.out.println(tok);
            if (nextTok) {
                oldCValue = tok;
                nextTok = false;
            }
            if (tok.equals("-c")) {
                nextTok = true;
                whereC = contTokens;
                //	System.out.println(tok);
            }
            contTokens += tok.length() + 1;
        }
        //System.out.println(whereC);
        return oldCValue;
    }

    public String getLibraryOptions() {
        return libraryOptions;
    }

    public void setLibraryOptions(String libraryOptions) {
        this.libraryOptions = libraryOptions;
    }

    public static void main(String[] args) {
        LibraryOptionsSetter lo = new LibraryOptionsSetter("-s_4_-c_0.1");
        lo.incrementC(-0.8);
    }

    private double redondear(double numero) {
        return Math.rint(numero * 100) / 100.0;
    }
}