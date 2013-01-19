package is2.tools;

import is2.data.SentenceData09;

/**
 * @author Bernd Bohnet, 27.10.2010
 *
 * Interface to all tools
 */
public interface Tool {

    /**
     * Uses the tool and applies it on the input sentence. The input is altered
     * and has to include a root (token).
     *
     * @param i the input sentence
     * @return The result of the performance without the root.
     */
    SentenceData09 apply(SentenceData09 snt09);
}