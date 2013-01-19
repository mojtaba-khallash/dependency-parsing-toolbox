package extractors;

import is2.data.Long2IntInterface;

/**
 * @author Dr. Bernd Bohnet, 29.04.2011
 *
 *
 */
public class ExtractorFactory {

    public static final int StackedClustered = 4;
    public static final int StackedClusteredR2 = 5;
    private int type = -1;

    /**
     * @param stackedClusteredR22
     */
    public ExtractorFactory(int t) {
        type = t;
    }

    /**
     * @param stackedClusteredR22
     * @param l2i
     * @return
     */
    public Extractor getExtractor(Long2IntInterface l2i) {
        switch (type) {
            case StackedClustered:
                return new ExtractorClusterStacked(l2i);
            case StackedClusteredR2:
                return new ExtractorClusterStackedR2(l2i);
        }
        return null;
    }
}