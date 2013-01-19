package is2.io;

import is2.data.PSTree;

/**
 * Phrase Structure PSReader
 * 
 * @author Dr. Bernd Bohnet, 07.02.2011
 *
 *
 */
public interface PSReader {

    public PSTree getNext();

    /**
     * @param ps
     * @param filter
     */
    public void startReading(String ps, String[] filter);
}