package extractors;

import is2.data.*;

/**
 * @author Dr. Bernd Bohnet, 29.04.2011
 *
 *
 */
public interface Extractor {

    /**
     * Initializes the Extractor general parts
     */
    public void initStat();

    /**
     * Initializes the Extractor specific parts
     */
    public void init();

    public int basic(short[] pos, int[] forms, int w1, int w2, Cluster cluster, IFV f);

    public void firstm(Instances is, int i, int w1, int w2, int j, Cluster cluster, long[] svs);

    public void siblingm(Instances is, int i, short[] pos, int[] forms,
            int[] lemmas, short[][] feats, int w1, int w2, int g, int j,
            Cluster cluster, long[] svs, int n);

    public void gcm(Instances is, int i, int w1, int w2, int g, int j, Cluster cluster, long[] svs);

    public int getType();

    public FV encodeCat(Instances is, int n, short[] pos, int[] is2,
            int[] is3, short[] heads, short[] labels, short[][] s, Cluster cl,
            FV pred);

    public void setMaxForm(int integer);

    /**
     * @return
     */
    public int getMaxForm();

    public float encode3(short[] pos, short[] heads, short[] labs, DataF x);
}