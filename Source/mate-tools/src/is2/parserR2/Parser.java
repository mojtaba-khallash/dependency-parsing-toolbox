package is2.parserR2;

import extractors.Extractor;
import extractors.ExtractorFactory;
import is2.data.*;
import is2.io.CONLLReader09;
import is2.io.CONLLWriter09;
import is2.tools.Tool;
import is2.util.DB;
import is2.util.OptionsSuper;
import is2.util.ParserEvaluator;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Parser implements Tool {

    // output evaluation info 
    private static final boolean MAX_INFO = true;
    public static int THREADS = 4;
    Long2IntInterface l2i;
    ParametersFloat params;
    Pipe pipe;
    OptionsSuper options;
    HashMap<Integer, Integer> rank = new HashMap<>();
    int amongxbest = 0, amongxbest_ula = 0, nbest = 0, bestProj = 0, smallestErrorSum = 0, countAllNodes = 0;
    static int NBest = 1000;
    ExtractorFactory extractorFactory = new ExtractorFactory(ExtractorFactory.StackedClusteredR2);

    /**
     * Initialize the parser
     *
     * @param options
     */
    public Parser(OptionsSuper options) {

        this.options = options;
        pipe = new Pipe(options);

        params = new ParametersFloat(0);

        // load the model
        try {
            readModel(options, pipe, params);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @param modelFileName The file name of the parsing model
     */
    public Parser(String modelFileName) {
        this(new Options(new String[]{"-model", modelFileName}));
    }

    /**
     *
     */
    public Parser() {
    }

    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();
        OptionsSuper options = new Options(args);

        NBest = options.best;

        DB.println("n-best" + NBest);

        Runtime runtime = Runtime.getRuntime();
        THREADS = runtime.availableProcessors();
        if (options.cores < THREADS && options.cores > 0) {
            THREADS = options.cores;
        }

        DB.println("Found " + runtime.availableProcessors() + " cores use " + THREADS);

        if (options.train) {

            Parser p = new Parser();
            p.options = options;

            p.l2i = new Long2Int(options.hsize);

            p.pipe = new Pipe(options);
            Instances is = new Instances();

            p.pipe.extractor = new Extractor[THREADS];

            for (int t = 0; t < THREADS; t++) {
                p.pipe.extractor[t] = p.extractorFactory.getExtractor(p.l2i);
            }

            p.params = new ParametersFloat(p.l2i.size());

            if (options.useMapping != null) {
                String model = options.modelName;

                options.modelName = options.useMapping;
                DB.println("Using mapping of model " + options.modelName);
                ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(options.modelName)));
                zis.getNextEntry();
                try (DataInputStream dis = new DataInputStream(new BufferedInputStream(zis))) {
                    p.pipe.mf.read(dis);

                    DB.println("read\n" + p.pipe.mf.toString());

                    ParametersFloat params = new ParametersFloat(0);
                    params.read(dis);

                    Edges.read(dis);
                }
                DB.println("end read model");
                options.modelName = model;
            }


            p.pipe.createInstances(options.trainfile, is);


            p.train(options, p.pipe, p.params, is, p.pipe.cl);

            p.writeModell(options, p.params, null, p.pipe.cl);

        }

        if (options.test) {

            Parser p = new Parser();
            p.options = options;

            p.pipe = new Pipe(options);
            p.params = new ParametersFloat(0);  // total should be zero and the parameters are later read

            // load the model

            p.readModel(options, p.pipe, p.params);

            DB.println("test on " + options.testfile);

            is2.parser.Parser.out.println("" + p.pipe.mf.toString());


            p.outputParses(options, p.pipe, p.params, !MAX_INFO);

        }

        is2.parser.Parser.out.println();

        if (options.eval) {
            is2.parser.Parser.out.println("\nEVALUATION PERFORMANCE:");
            ParserEvaluator.evaluate(options.goldfile, options.outfile);
        }

        long end = System.currentTimeMillis();
        is2.parser.Parser.out.println("used time " + ((float) ((end - start) / 100) / 10));

        Decoder.executerService.shutdown();
        Pipe.executerService.shutdown();
        is2.parser.Parser.out.println("end.");


    }

    /**
     * Read the models and mapping
     *
     * @param options
     * @param pipe
     * @param params
     * @throws IOException
     */
    public void readModel(OptionsSuper options, Pipe pipe, Parameters params) throws IOException {


        DB.println("Reading data started");

        // prepare zipped reader
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(options.modelName)));
        zis.getNextEntry();
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(zis))) {
            pipe.mf.read(dis);

            pipe.cl = new Cluster(dis);

            params.read(dis);
            this.l2i = new Long2Int(params.size());
            DB.println("parsing -- li size " + l2i.size());

            pipe.extractor = new Extractor[THREADS];

            for (int t = 0; t < THREADS; t++) {
                pipe.extractor[t] = this.extractorFactory.getExtractor(l2i);
            }

            Edges.read(dis);

            options.decodeProjective = dis.readBoolean();

            int maxForm = dis.readInt();

            for (int t = 0; t < THREADS; t++) {
                pipe.extractor[t].setMaxForm(maxForm);
                pipe.extractor[t].initStat();
                pipe.extractor[t].init();
            }

            boolean foundInfo = false;
            try {
                String info;
                int icnt = dis.readInt();
                for (int i = 0; i < icnt; i++) {
                    info = dis.readUTF();
                    is2.parser.Parser.out.println(info);
                }
            } catch (Exception e) {
                if (!foundInfo) {
                    is2.parser.Parser.out.println("no info about training");
                }
            }
        }

        DB.println("Reading data finnished");

        Decoder.NON_PROJECTIVITY_THRESHOLD = (float) options.decodeTH;
        for (int t = 0; t < THREADS; t++) {
            pipe.extractor[t].initStat();
            pipe.extractor[t].init();
        }

    }

    /**
     * Do the training
     *
     * @param instanceLengths
     * @param options
     * @param pipe
     * @param params
     * @param is
     * @param cluster
     * @throws IOException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    public void train(OptionsSuper options, Pipe pipe, ParametersFloat params, Instances is, Cluster cluster)
            throws IOException, InterruptedException, ClassNotFoundException {


        DB.println("\nTraining Information ");
        DB.println("-------------------- ");


        Decoder.NON_PROJECTIVITY_THRESHOLD = (float) options.decodeTH;

        if (options.decodeProjective) {
            is2.parser.Parser.out.println("Decoding: " + (options.decodeProjective ? "projective" : "non-projective"));
        } else {
            is2.parser.Parser.out.println("" + Decoder.getInfo());
        }
        int numInstances = is.size();

        int maxLenInstances = 0;
        for (int i = 0; i < numInstances; i++) {
            if (maxLenInstances < is.length(i)) {
                maxLenInstances = is.length(i);
            }
        }

        DataF data = new DataF(maxLenInstances, pipe.mf.getFeatureCounter().get(PipeGen.REL).shortValue());

        int iter = 0;
        int del = 0;
        float error;
        float f1;

        FV pred = new FV();
        FV act = new FV();

        double upd = (double) (numInstances * options.numIters) + 1;

        for (; iter < options.numIters; iter++) {

            is2.parser.Parser.out.print("Iteration " + iter + ": ");

            long start = System.currentTimeMillis();

            long last = System.currentTimeMillis();
            error = 0;
            f1 = 0;
            for (int n = 0; n < numInstances; n++) {

                upd--;

                if (is.labels[n].length > options.maxLen) {
                    continue;
                }

                String info = " td " + ((Decoder.timeDecotder) / 1000000F) + " tr " + ((Decoder.timeRearrange) / 1000000F)
                        + " te " + ((Pipe.timeExtract) / 1000000F);

                if ((n + 1) % 500 == 0) {
                    PipeGen.outValueErr(n + 1, Math.round(error * 1000) / 1000, f1 / n, last, upd, info);
                }

                short pos[] = is.pposs[n];

                data = pipe.fillVector((F2SF) params.getFV(), is, n, data, cluster, THREADS, l2i);

                List<ParseNBest> parses = Decoder.decode(pos, data, options.decodeProjective, pipe.extractor[0]);
                Parse d = parses.get(0);
                double e = pipe.errors(is, n, d);

                if (d.f1 > 0) {
                    f1 += (d.labels.length - 1 - e) / (d.labels.length - 1);
                }

                if (e <= 0) {
                    continue;
                }

                // get predicted feature vector
                pred.clear();
                pipe.extractor[0].encodeCat(is, n, pos, is.forms[n], is.plemmas[n], d.heads, d.labels, is.feats[n], pipe.cl, pred);

                error += e;

                act.clear();
                pipe.extractor[0].encodeCat(is, n, pos, is.forms[n], is.plemmas[n], is.heads[n], is.labels[n], is.feats[n], pipe.cl, act);

                params.update(act, pred, is, n, d, upd, e);
            }

            String info = " td " + ((Decoder.timeDecotder) / 1000000F) + " tr " + ((Decoder.timeRearrange) / 1000000F)
                    + " te " + ((Pipe.timeExtract) / 1000000F) + " nz " + params.countNZ();
            PipeGen.outValueErr(numInstances, Math.round(error * 1000) / 1000, f1 / numInstances, last, upd, info);
            del = 0;
            long end = System.currentTimeMillis();
            is2.parser.Parser.out.println(" time:" + (end - start));


            ParametersFloat pf = params.average2((iter + 1) * is.size());
            try {

                if (options.testfile != null) {
                    outputParses(options, pipe, pf, !MAX_INFO);
                    ParserEvaluator.evaluate(options.goldfile, options.outfile);
                    //		writeModell(options, pf, ""+(iter+1),pipe.cl); 
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

            Decoder.timeDecotder = 0;
            Decoder.timeRearrange = 0;
            Pipe.timeExtract = 0;
        }
        params.average(iter * is.size());
    }

    /**
     * Do the parsing
     *
     * @param options
     * @param pipe
     * @param params
     * @throws IOException
     */
    private void outputParses(OptionsSuper options, Pipe pipe, ParametersFloat params, boolean maxInfo) throws Exception {

        long start = System.currentTimeMillis();

        CONLLReader09 depReader = new CONLLReader09(options.testfile, options.formatTask);
        CONLLWriter09 depWriter = new CONLLWriter09(options.outfile, options.formatTask);

//		ExtractorClusterStacked.initFeatures();

        int cnt = 0;
        int del = 0;
        long last = System.currentTimeMillis();

        if (maxInfo) {
            is2.parser.Parser.out.println("\nParsing Information ");
        }
        if (maxInfo) {
            is2.parser.Parser.out.println("------------------- ");
        }

        if (maxInfo && !options.decodeProjective) {
            is2.parser.Parser.out.println("" + Decoder.getInfo());
        }

        //	if (!maxInfo) Parser.out.println();

        String[] types = new String[pipe.mf.getFeatureCounter().get(PipeGen.REL)];
        for (Entry<String, Integer> e : MFB.getFeatureSet().get(PipeGen.REL).entrySet()) {
            types[e.getValue()] = e.getKey();
        }


        is2.parser.Parser.out.print("Processing Sentence: ");

        while (true) {

            Instances is = new Instances();
            is.init(1, new MFB(), options.formatTask);

            SentenceData09 instance = pipe.nextInstance(is, depReader);
            if (instance == null) {
                break;
            }
            cnt++;

            SentenceData09 i09 = this.parse(instance, params);

            //		}
            depWriter.write(i09);
            del = PipeGen.outValue(cnt, del, last);
            //		DB.println("xbest "+amongxbest+" cnt "+cnt+" "+((float)((float)amongxbest/cnt))+" nbest "+((float)nbest/cnt)+
            //				" 1best "+((float)(rank.get(0)==null?0:rank.get(0))/cnt)+" best-proj "+((float)bestProj/cnt));

        }

        //pipe.close();

        depWriter.finishWriting();
        long end = System.currentTimeMillis();
        DB.println("rank\n" + rank + "\n");
        DB.println("x-best-las " + amongxbest + " x-best-ula " + amongxbest_ula + " cnt " + cnt + " x-best-las "
                + ((float) ((float) amongxbest / cnt))
                + " x-best-ula " + ((float) ((float) amongxbest_ula / cnt))
                + " nbest " + ((float) nbest / cnt)
                + " 1best " + ((float) (rank.get(0) == null ? 0 : rank.get(0)) / cnt)
                + " best-proj " + ((float) bestProj / cnt)
                + " Sum LAS " + ((float) this.smallestErrorSum / countAllNodes));

        //		DB.println("errors "+error);

        rank.clear();
        amongxbest = 0;
        amongxbest_ula = 0;
        nbest = 0;
        bestProj = 0;
        if (maxInfo) {
            is2.parser.Parser.out.println("Used time " + (end - start));
        }
        if (maxInfo) {
            is2.parser.Parser.out.println("forms count " + Instances.m_count + " unkown " + Instances.m_unkown);
        }
    }

    /**
     * Do the parsing
     *
     * @param options
     * @param pipe
     * @param params
     * @throws IOException
     */
    private void getNBest(OptionsSuper options, Pipe pipe, ParametersFloat params, boolean maxInfo) throws Exception {


        CONLLReader09 depReader = new CONLLReader09(options.testfile, options.formatTask);

        //	ExtractorClusterStacked.initFeatures();

        int cnt = 0;

        String[] types;
        types = new String[pipe.mf.getFeatureCounter().get(PipeGen.REL)];
        for (Entry<String, Integer> e : MFB.getFeatureSet().get(PipeGen.REL).entrySet()) {
            types[e.getValue()] = e.getKey();
        }

//			Parser.out.print("Processing Sentence: ");

        while (true) {

            Instances is = new Instances();
            is.init(1, new MFB(), options.formatTask);

            SentenceData09 instance = pipe.nextInstance(is, depReader);
            if (instance == null) {
                break;
            }
            cnt++;

            this.parseNBest(instance);
        }

        //pipe.close();
//			depWriter.finishWriting();
//			long end = System.currentTimeMillis();
//			DB.println("rank\n"+rank+"\n");
//			DB.println("x-best-las "+amongxbest+" x-best-ula "+amongxbest_ula+" cnt "+cnt+" x-best-las "
//					+((float)((float)amongxbest/cnt))+
//					" x-best-ula "+((float)((float)amongxbest_ula/cnt))+
//					" nbest "+((float)nbest/cnt)+
//					" 1best "+((float)(rank.get(0)==null?0:rank.get(0))/cnt)+
//					" best-proj "+((float)bestProj/cnt));
        //		DB.println("errors "+error);


    }

    public SentenceData09 parse(SentenceData09 instance, ParametersFloat params) throws IOException {

        String[] types = new String[pipe.mf.getFeatureCounter().get(PipeGen.REL)];
        for (Entry<String, Integer> e : MFB.getFeatureSet().get(PipeGen.REL).entrySet()) {
            types[e.getValue()] = e.getKey();
        }

        Instances is = new Instances();
        is.init(1, new MFB(), options.formatTask);
        new CONLLReader09().insert(is, instance);

        String[] forms = instance.forms;

        // use for the training ppos
        DataF d2;
        try {
            d2 = pipe.fillVector(params.getFV(), is, 0, null, pipe.cl, THREADS, l2i);//cnt-1
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        short[] pos = is.pposs[0];

        List<ParseNBest> parses = null;
        Parse d = null;
        try {
            parses = Decoder.decode(pos, d2, options.decodeProjective, pipe.extractor[0]); //cnt-1
            d = parses.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (parses.size() > NBest) {
            parses = parses.subList(0, NBest);
        }

        int g_las = Decoder.getGoldRank(parses, is, 0, Decoder.LAS);
        int g_ula = Decoder.getGoldRank(parses, is, 0, !Decoder.LAS);

        int smallest = Decoder.getSmallestError(parses, is, 0, !Decoder.LAS);
        smallestErrorSum += is.length(0) - smallest;
        countAllNodes += is.length(0);

        if (g_las >= 0) {
            amongxbest++;
        }
        if (g_ula >= 0) {
            amongxbest_ula++;
        }

        nbest += parses.size();

        Integer r = rank.get(g_las);
        if (r == null) {
            rank.put(g_las, 1);
        } else {
            rank.put(g_las, r + 1);
        }

        float err = (float) this.pipe.errors(is, 0, d);

        float errBestProj = (float) this.pipe.errors(is, 0, Decoder.bestProj);

        if (errBestProj == 0) {
            bestProj++;
        }

        SentenceData09 i09 = new SentenceData09(instance);

        i09.createSemantic(instance);

        for (int j = 0; j < forms.length - 1; j++) {
            i09.plabels[j] = types[d.labels[j + 1]];
            i09.pheads[j] = d.heads[j + 1];
        }
        return i09;

    }

    public List<ParseNBest> parseNBest(SentenceData09 instance) throws IOException {

        Instances is = new Instances();
        is.init(1, new MFB(), options.formatTask);
        new CONLLReader09().insert(is, instance);

        // use for the training ppos
        DataF d2;
        try {
            d2 = pipe.fillVector(params.getFV(), is, 0, null, pipe.cl, THREADS, l2i);//cnt-1
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        short[] pos = is.pposs[0];

        List<ParseNBest> parses = null;
        try {
            parses = Decoder.decode(pos, d2, options.decodeProjective, pipe.extractor[0]); //cnt-1
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (parses.size() > NBest) {
            parses = parses.subList(0, NBest);
        }

        return parses;

    }

    /*
     * (non-Javadoc) @see is2.tools.Tool#apply(is2.data.SentenceData09)
     */
    @Override
    public SentenceData09 apply(SentenceData09 snt09) {

        try {
            parse(snt09, this.params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Decoder.executerService.shutdown();
        Pipe.executerService.shutdown();

        return snt09;
    }

    /**
     * Write the parsing model
     *
     * @param options
     * @param params
     * @param extension
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void writeModell(OptionsSuper options, ParametersFloat params, String extension, Cluster cs) throws FileNotFoundException, IOException {

        String name = extension == null ? options.modelName : options.modelName + extension;
//		Parser.out.println("Writting model: "+name);
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(name)));
        zos.putNextEntry(new ZipEntry("data"));
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(zos))) {
            MFB.writeData(dos);
            cs.write(dos);

            params.write(dos);

            Edges.write(dos);

            dos.writeBoolean(options.decodeProjective);

            dos.writeInt(pipe.extractor[0].getMaxForm());

            dos.writeInt(5);  // Info count
            dos.writeUTF("Used parser   " + Parser.class.toString());
            dos.writeUTF("Creation date " + (new SimpleDateFormat("yyyy.MM.dd HH:mm:ss")).format(new Date()));
            dos.writeUTF("Training data " + options.trainfile);
            dos.writeUTF("Iterations    " + options.numIters + " Used sentences " + options.count);
            dos.writeUTF("Cluster       " + options.clusterFile);

            dos.flush();
        }
    }
}