package is2.io;

import is2.data.Instances;
import is2.data.SentenceData09;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Dr. Bernd Bohnet, 18.08.2011
 *
 *
 */
public abstract class CONLLReader extends IOGenerals {
    // protected fiels

    public boolean normalizeOn = true;
    protected BufferedReader inputReader;
    protected int lineNumber = 0;

    /**
     * Read a instance an store it in a compressed format
     *
     * @param is
     * @return
     * @throws IOException
     */
    final public SentenceData09 getNext(Instances is) throws Exception {

        SentenceData09 it = getNext();

        if (is != null) {
            insert(is, it);
        }

        return it;
    }

    public abstract SentenceData09 getNext() throws Exception;

    public String normalize(String s) {
        if (!normalizeOn) {
            return s;
        }
        if (s.matches(NUMBER)) {
            return NUM;
        }
        return s;
    }

    public void startReading(String file) {
        lineNumber = 0;
        try {
            inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"), 32768);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public abstract boolean insert(Instances is, SentenceData09 it) throws IOException;
}