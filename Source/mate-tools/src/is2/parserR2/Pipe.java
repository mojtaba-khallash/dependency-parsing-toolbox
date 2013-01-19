package is2.parserR2;

import extractors.Extractor;
import extractors.ParallelExtract;
import is2.data.*;
import is2.io.CONLLReader09;
import is2.util.OptionsSuper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

final public class Pipe extends PipeGen {

    public Extractor[] extractor;
    final public MFB mf = new MFB();
    Cluster cl;
    private OptionsSuper options;
    public static long timeExtract;

    public Pipe(OptionsSuper o) {
        options = o;
    }

    public void createInstances(String file, Instances is) throws Exception {


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


        for (Extractor e : extractor) {
            e.setMaxForm(mf.getFeatureCounter().get(WORD));
        }

        if (options.clusterFile == null) {
            cl = new Cluster();
        } else {
            cl = new Cluster(options.clusterFile, mf, 6);
        }

        mf.calculateBits();

        is2.parser.Parser.out.println("" + mf.toString());

        for (Extractor e : extractor) {
            e.initStat();
            e.init();
        }

        depReader.startReading(file);

        int num1 = 0;


        Edges.init(mf.getFeatureCounter().get(POS));


        is2.parser.Parser.out.print("Creating edge filters and read corpus: ");
        del = 0;

        is.init(ic, new MFB());

        while (true) {
            if (num1 % 100 == 0) {
                del = outValue(num1, del);
            }

            SentenceData09 instance1 = depReader.getNext(is);

            if (instance1 == null) {
                break;
            }

            int last = is.size() - 1;
            short[] pos = is.pposs[last];

            for (int k = 0; k < is.length(last); k++) {
                if (is.heads[last][k] < 0) {
                    continue;
                }
                Edges.put(pos[is.heads[last][k]], pos[k], k < is.heads[last][k], is.labels[last][k]);
            }

            if (!options.allFeatures && num1 > options.count) {
                break;
            }

            num1++;

        }
        del = outValue(num1, del);
        is2.parser.Parser.out.println();
        Edges.findDefault();
    }

    public void getInstances(String file, Instances is) throws Exception {
        CONLLReader09 depReader = new CONLLReader09(file);

        int ic = options.count + 2;

        is.init(ic, new MFB());

        int num1 = 0, del = 0;
        while (true) {
            if (num1 % 100 == 0) {
                del = outValue(num1, del);
            }

            SentenceData09 instance1 = depReader.getNext(is);

            if (instance1 == null) {
                break;
            }

            if (!options.allFeatures && num1 > options.count) {
                break;
            }

            num1++;

        }
        del = outValue(num1, del);
        is2.parser.Parser.out.println();

    }

    /**
     * Creates an instance for outputParses
     *
     * @param is
     * @return
     * @throws IOException
     */
    protected final SentenceData09 nextInstance(Instances is, CONLLReader09 depReader) throws Exception {

        SentenceData09 instance = depReader.getNext(is);
        if (instance == null || instance.forms == null) {
            return null;
        }

        return instance;
    }
    public static ExecutorService executerService = java.util.concurrent.Executors.newFixedThreadPool(Parser.THREADS);

    public DataF fillVector(F2SF params, Instances is, int inst, DataF d, Cluster cluster, int threads, Long2IntInterface li) throws InterruptedException {

        long ts = System.nanoTime();

        if (executerService.isShutdown()) {
            executerService = java.util.concurrent.Executors.newCachedThreadPool();
        }


        final int length = is.length(inst);
        if (d == null || d.len < length) {
            d = new DataF(length, mf.getFeatureCounter().get(PipeGen.REL).shortValue());
        }

        ArrayList<ParallelExtract> pe = new ArrayList<>();


        for (int i = 0; i < threads; i++) {

//			DB.println(""+((ExtractorClusterStackedR2)extractor[i]).s_dist);
            pe.add(new ParallelExtract(extractor[i], is, inst, d, (F2SF) params.clone(), cluster, li));
        }

        for (int w1 = 0; w1 < length; w1++) {
            for (int w2 = 0; w2 < length; w2++) {
                if (w1 == w2) {
                    continue;
                }
                ParallelExtract.add(w1, w2);
            }
        }
        executerService.invokeAll(pe);

        timeExtract += (System.nanoTime() - ts);


        return d;
    }

    /**
     * the loss function
     */
    public double errors(Instances is, int ic, Parse p) {

        if (p.heads == null) {
            p.signature2parse(p.signature());
        }
        short[] act = is.heads[ic];
        double correct = 0;

        // do not count root
        for (int i = 1; i < act.length; i++) {
            if (p.heads[i] == act[i]) {
                correct += 0.5;
                if (p.labels[i] == is.labels[ic][i]) {
                    correct += 0.5;
                }
            }
        }

        double x = ((double) act.length - 1 - correct);

        //p.f1 = (double)correct / (double)(act.length-1);

        return x;
    }
}