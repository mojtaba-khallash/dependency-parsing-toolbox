package is2.tools;

import is2.data.SentenceData09;

/**
 * Provides Methods for the retraining
 *
 * @author bohnetbd
 *
 */
public interface Retrainable {

    /**
     * Retrains with a update factor (upd). The retraining stops when the model
     * was successful adapted or it gave up after the maximal iterations.
     *
     * @param sentence the data container of the new example.
     * @param upd the update factor, e.g. 0.01
     * @param iterations maximal number of iterations that are tried to adapt
     * the system.
     * @return success = true -- else false
     */
    public boolean retrain(SentenceData09 sentence, float upd, int iterations);

    boolean retrain(SentenceData09 sentence, float upd, int iterations, boolean print);
}