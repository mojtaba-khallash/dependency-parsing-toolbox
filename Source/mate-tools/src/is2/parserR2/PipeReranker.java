package is2.parserR2;

import extractors.ExtractorReranker;
import is2.data.*;
import is2.io.CONLLReader09;
import is2.util.OptionsSuper;
import java.util.concurrent.ExecutorService;

final public class PipeReranker extends PipeGen {

    public ExtractorReranker extractor;
    final public MFB mf = new MFB();
    Cluster cl;
    private OptionsSuper options;
    public static long timeExtract;

    public PipeReranker(OptionsSuper o) {
        options = o;
    }

    public void createInstances(String file, Instances is) //	throws Exception 
    {


        CONLLReader09 depReader = new CONLLReader09(file);

        mf.register(REL, "<root-type>");

        // register at least one predicate since the parsing data might not contain predicates as in 
        // the Japaness corpus but the development sets contains some

        long sl = 0;

        is2.parser.Parser.out.print("Registering feature parts of sentence: ");
        int ic = 0;
        int del = 0;
        while (true) {
            SentenceData09 instance = depReader.getNext();
            if (instance == null) {
                break;
            }
            ic++;

            sl += instance.labels.length;

            if (ic % 1000 == 0) {
                del = outValue(ic, del);
            }

            String[] labs1 = instance.labels;
            for (int i1 = 0; i1 < labs1.length; i1++) {
                mf.register(REL, labs1[i1]);
            }

            String[] w = instance.forms;
            for (int i1 = 0; i1 < w.length; i1++) {
                mf.register(WORD, depReader.normalize(w[i1]));
            }

            w = instance.plemmas;
            for (int i1 = 0; i1 < w.length; i1++) {
                mf.register(WORD, depReader.normalize(w[i1]));
            }


            w = instance.ppos;
            for (int i1 = 0; i1 < w.length; i1++) {
                mf.register(POS, w[i1]);
            }

            w = instance.gpos;
            for (int i1 = 0; i1 < w.length; i1++) {
                mf.register(POS, w[i1]);
            }

            if (instance.feats != null) {
                String fs[][] = instance.feats;
                for (int i1 = 0; i1 < fs.length; i1++) {
                    w = fs[i1];
                    if (w == null) {
                        continue;
                    }
                    for (int i2 = 0; i2 < w.length; i2++) {
                        mf.register(FEAT, w[i2]);
                    }
                }
            }

            if ((ic - 1) > options.count) {
                break;
            }
        }
        del = outValue(ic, del);

        is2.parser.Parser.out.println();
        ExtractorReranker.initFeatures();

        ExtractorReranker.maxForm = mf.getFeatureCounter().get(WORD);

        if (options.clusterFile == null) {
            cl = new Cluster();
        } else {
            cl = new Cluster(options.clusterFile, mf, 6);
        }

        mf.calculateBits();
        ExtractorReranker.initStat();

        is2.parser.Parser.out.println("" + mf.toString());

        extractor.init();
        depReader.startReading(file);

        int num1 = 0;

        is.init(ic, new MFB());

        Edges.init(mf.getFeatureCounter().get(POS));

        del = 0;


        del = outValue(num1, del);
        is2.parser.Parser.out.println();
    }
    public static ExecutorService executerService = java.util.concurrent.Executors.newFixedThreadPool(Parser.THREADS);
}