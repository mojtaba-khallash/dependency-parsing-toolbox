package is2.parser;

import is2.data.*;
import is2.io.*;
import is2.tools.Retrainable;
import is2.tools.Tool;
import is2.util.DB;
import is2.util.OptionsSuper;
import is2.util.ParserEvaluator;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Parser implements Tool, Retrainable {

    // output evaluation info 
    private static final boolean MAX_INFO = true;
    public static int THREADS = 4;
    public Long2IntInterface l2i;
    public ParametersFloat params;
    public Pipe pipe;
    public OptionsSuper options;
    // keep some of the parsing information for later evaluation
    public Instances is;
    DataFES d2;
    public Parse d = null;
    private long startTime;
    
    public static PrintStream out = System.out;

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

        out.print("Started: " + new Date(System.currentTimeMillis()) + "\n");

        OptionsSuper options = new Options(args);

        Runtime runtime = Runtime.getRuntime();
        THREADS = runtime.availableProcessors();
        if (options.cores < THREADS && options.cores > 0) {
            THREADS = options.cores;
        }
        DB.println("Found " + runtime.availableProcessors() + " cores use " + THREADS);

        if (options.train) {
            Parser p = new Parser();
            p.startTime = System.currentTimeMillis();
            p.options = options;

            p.l2i = new Long2Int(options.hsize);

            p.pipe = new Pipe(options);
            Instances is = new Instances();

            Extractor.initFeatures();
            p.pipe.extractor = new Extractor[THREADS];
            DB.println("hsize " + options.hsize);

            DB.println("Use " + (options.featureCreation == OptionsSuper.MULTIPLICATIVE ? "multiplication" : "shift") + "-based feature creation function");
            for (int t = 0; t < THREADS; t++) {
                p.pipe.extractor[t] = new Extractor(p.l2i, options.stack, options.featureCreation);
            }

            DB.println("Stacking " + options.stack);

            p.pipe.createInstances(options.trainfile, is);

            p.params = new ParametersFloat(p.l2i.size());

            p.train(options, p.pipe, p.params, is, p.pipe.cl);

            out.print("Saving model...");
            p.writeModell(options, p.params, null, p.pipe.cl);
            out.println("done.");

            out.println("Training Time: " + CalculateTime(p.startTime, System.currentTimeMillis()));
        }

        if (options.test) {
            Parser p = new Parser();
            p.startTime = System.currentTimeMillis();
            p.options = options;


            p.pipe = new Pipe(options);
            p.params = new ParametersFloat(0);  // total should be zero and the parameters are later read

            // load the model

            p.readModel(options, p.pipe, p.params);

            DB.println("label only? " + options.label);
            p.out(options, p.pipe, p.params, !MAX_INFO, options.label);
            
            out.println("\nParsing Time: " + CalculateTime(p.startTime, System.currentTimeMillis()));
        }

        if (options.eval) {
            out.println("EVALUATION PERFORMANCE:");
            ParserEvaluator.evaluate(options.goldfile, options.outfile);
        }
        
        out.println("Finished: " + new Date(System.currentTimeMillis()));

        Decoder.executerService.shutdown();
        Pipe.executerService.shutdown();
        out.println("end.");
    }

    /**
     * Read the models and mapping
     *
     * @param options
     * @param pipe
     * @param params
     * @throws IOException
     */
    public final void readModel(OptionsSuper options, Pipe pipe, Parameters params) throws IOException {

        DB.println("Reading data started");

        // prepare zipped reader
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(options.modelName)));
        zis.getNextEntry();
        DataInputStream dis = new DataInputStream(new BufferedInputStream(zis));

        try {
            pipe.mf.read(dis);
        }
        catch(Exception ex) {
            throw new java.io.StreamCorruptedException();
        }

        pipe.cl = new Cluster(dis);

        params.read(dis);
        this.l2i = new Long2Int(params.size());
        DB.println("parsing -- li size " + l2i.size());


        pipe.extractor = new Extractor[THREADS];

        boolean stack = dis.readBoolean();

        options.featureCreation = dis.readInt();

        for (int t = 0; t < THREADS; t++) {
            pipe.extractor[t] = new Extractor(l2i, stack, options.featureCreation);
        }
        DB.println("Stacking " + stack);

        Extractor.initFeatures();
        Extractor.initStat(options.featureCreation);


        for (int t = 0; t < THREADS; t++) {
            pipe.extractor[t].init();
        }

        Edges.read(dis);

        options.decodeProjective = dis.readBoolean();

        Extractor.maxForm = dis.readInt();

        boolean foundInfo = false;
        try {
            String info;
            int icnt = dis.readInt();
            for (int i = 0; i < icnt; i++) {
                info = dis.readUTF();
                out.println(info);
            }
        } catch (Exception e) {
            if (!foundInfo) {
                out.println("no info about training");
            }
        }


        dis.close();

        DB.println("Reading data finnished");

        Decoder.NON_PROJECTIVITY_THRESHOLD = (float) options.decodeTH;

        Extractor.initStat(options.featureCreation);
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
            out.println("Decoding: " + (options.decodeProjective ? "projective" : "non-projective"));
        } else {
            out.println(Decoder.getInfo());
        }
        int numInstances = is.size();

        int maxLenInstances = 0;
        for (int i = 0; i < numInstances; i++) {
            if (maxLenInstances < is.length(i)) {
                maxLenInstances = is.length(i);
            }
        }

        DataFES data = new DataFES(maxLenInstances, pipe.mf.getFeatureCounter().get(PipeGen.REL).shortValue());

        int iter = 0;
        float error;
        float f1;

        FV pred = new FV();
        FV act = new FV();

        double upd = (double) (numInstances * options.numIters) + 1;

        for (; iter < options.numIters; iter++) {

            out.print("Iteration " + iter + ": ");

            long start = System.currentTimeMillis();

            long last = System.currentTimeMillis();
            error = 0;
            f1 = 0;
            for (int n = 0; n < numInstances; n++) {

                upd--;

                if (is.labels[n].length > options.maxLen) {
                    continue;
                }

                String info = " - Decotder Time: " + ((Decoder.timeDecotder) / 1000000F) + 
                              " - Rearrange Time: " + ((Decoder.timeRearrange) / 1000000F) + 
                              " - Extract Time: " + ((Pipe.timeExtract) / 1000000F);

                if ((n + 1) % 500 == 0) {
                    PipeGen.outValueErr(n + 1, error, f1 / n, last, upd, info);
                }

                short pos[] = is.pposs[n];

                data = pipe.fillVector((F2SF) params.getFV(), is, n, data, cluster);

                Parse d = Decoder.decode(pos, data, options.decodeProjective, Decoder.TRAINING);

                double e = pipe.errors(is, n, d);

                if (d.f1 > 0) {
                    f1 += d.f1;
                }

                if (e <= 0) {
                    continue;
                }

                pred.clear();
                pipe.extractor[0].encodeCat(is, n, pos, is.forms[n], is.plemmas[n], d.heads, d.labels, is.feats[n], pipe.cl, pred);

                error += e;

                params.getFV();


                act.clear();
                pipe.extractor[0].encodeCat(is, n, pos, is.forms[n], is.plemmas[n], is.heads[n], is.labels[n], is.feats[n], pipe.cl, act);

                params.update(act, pred, is, n, d, upd, e);
            }

            String info = " - Decotder Time: " + ((Decoder.timeDecotder) / 1000000F) + 
                          " - Rearrange Time: " + ((Decoder.timeRearrange) / 1000000F) + 
                          " - Extract Time: " + ((Pipe.timeExtract) / 1000000F);
            PipeGen.outValueErr(numInstances, error, f1 / numInstances, last, upd, info);
            long end = System.currentTimeMillis();
            out.println("\n\t\tNon-Zero Count: " + params.countNZ() + " - Time: " + CalculateTime(start, end) + " (" + (end - start) + " ms)");

            ParametersFloat pf = params.average2((iter + 1) * is.size());
            try {

                if (options.testfile != null && options.goldfile != null) {
                    out(options, pipe, pf, !MAX_INFO, false);
                    ParserEvaluator.evaluate(options.goldfile, options.outfile);
                    //		writeModell(options, pf, ""+(iter+1),pipe.cl); 
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (error == 0) {
                DB.println("stopped because learned all lessons");
                break;
            }

            Decoder.timeDecotder = 0;
            Decoder.timeRearrange = 0;
            Pipe.timeExtract = 0;
        }
        if (options.average) {
            params.average(iter * is.size());
        }
    }

    /**
     * Do the parsing job
     *
     * @param options
     * @param pipe
     * @param params
     * @throws IOException
     */
    private void out(OptionsSuper options, Pipe pipe, ParametersFloat params, boolean maxInfo, boolean labelOnly)
            throws Exception {

        long start = System.currentTimeMillis();

        CONLLReader depReader = null;
        CONLLWriter depWriter = null;
        if (options.formatTask == 4) {
            depReader = new CONLLReader04(options.testfile);
            depWriter = new CONLLWriter06(options.outfile);
        }
        else if (options.formatTask == 6) {
            depReader = new CONLLReader06(options.testfile);
            depWriter = new CONLLWriter06(options.outfile);
        }
        else if (options.formatTask == 8) {
            depReader = new CONLLReader08(options.testfile);
            depWriter = new CONLLWriter09(options.outfile);
        }
        else if (options.formatTask == 9) {
            depReader = new CONLLReader09(options.testfile);
            depWriter = new CONLLWriter09(options.outfile);
        }

        Extractor.initFeatures();

        int cnt = 0;
        int del = 0;
        long last = System.currentTimeMillis();

        if (maxInfo) {
            out.println("\nParsing Information ");
        }
        if (maxInfo) {
            out.println("------------------- ");
        }

        if (maxInfo && !options.decodeProjective) {
            out.println("" + Decoder.getInfo());
        }

        //	if (!maxInfo) out.println();

        String[] types = new String[pipe.mf.getFeatureCounter().get(PipeGen.REL)];
        for (Entry<String, Integer> e : pipe.mf.getFeatureSet().get(PipeGen.REL).entrySet()) {
            types[e.getValue()] = e.getKey();
        }


        out.print("Processing Sentence: ");

        while (true) {

            Instances ins = new Instances();
            ins.init(1, new MFO(), options.formatTask);

            SentenceData09 instance = pipe.nextInstance(ins, depReader);
            if (instance == null) {
                break;
            }
            cnt++;

            SentenceData09 i09 = this.parse(instance, params, labelOnly, options);

            depWriter.write(i09);
            del = PipeGen.outValue(cnt, del, last);

        }
        //pipe.close();
        depWriter.finishWriting();
        long end = System.currentTimeMillis();
        //		DB.println("errors "+error);
        if (maxInfo) {
            out.println("Used time " + (end - start));
        }
        if (maxInfo) {
            out.println("forms count " + Instances.m_count + " unkown " + Instances.m_unkown);
        }
    }

    /**
     * Parse a single sentence
     *
     * @param instance
     * @param params
     * @param labelOnly
     * @param options
     * @return
     */
    public SentenceData09 parse(SentenceData09 instance, ParametersFloat params, boolean labelOnly, OptionsSuper options) throws IOException {

        String[] types = new String[pipe.mf.getFeatureCounter().get(PipeGen.REL)];
        for (Entry<String, Integer> e : MFO.getFeatureSet().get(PipeGen.REL).entrySet()) {
            types[e.getValue()] = e.getKey();
        }

        is = new Instances();
        is.init(1, new MFO(), options.formatTask);
        new CONLLReader09().insert(is, instance);

        // use for the training ppos

        SentenceData09 i09 = new SentenceData09(instance);
        i09.createSemantic(instance);

        if (labelOnly) {
            F2SF f2s = params.getFV();

            // repair pheads

            is.pheads[0] = is.heads[0];

            for (int l = 0; l < is.pheads[0].length; l++) {
                if (is.pheads[0][l] < 0) {
                    is.pheads[0][l] = 0;
                }
            }


            short[] labels = pipe.extractor[0].searchLabel(is, 0, is.pposs[0], is.forms[0], is.plemmas[0], is.pheads[0], is.plabels[0], is.feats[0], pipe.cl, f2s);

            for (int j = 0; j < instance.forms.length - 1; j++) {
                i09.plabels[j] = types[labels[j + 1]];
                i09.pheads[j] = is.pheads[0][j + 1];
            }
            return i09;
        }

        if (options.maxLength > instance.length() && options.minLength <= instance.length()) {
            try {
                d2 = pipe.fillVector(params.getFV(), is, 0, null, pipe.cl);//cnt-1
                d = Decoder.decode(is.pposs[0], d2, options.decodeProjective, !Decoder.TRAINING); //cnt-1

            } catch (Exception e) {
                e.printStackTrace();
            }

            for (int j = 0; j < instance.forms.length - 1; j++) {
                i09.plabels[j] = types[d.labels[j + 1]];
                i09.pheads[j] = d.heads[j + 1];
            }
        }
        return i09;
    }


    /*
     * (non-Javadoc) @see is2.tools.Tool#apply(is2.data.SentenceData09)
     */
    @Override
    public SentenceData09 apply(SentenceData09 snt09) {

        SentenceData09 outData = null;
        try {
            outData = parse(snt09, this.params, false, options);


        } catch (Exception e) {
            e.printStackTrace();
        }

        Decoder.executerService.shutdown();
        Pipe.executerService.shutdown();

        return outData;
    }

    /**
     * Get the edge scores of the last parse.
     *
     * @return the scores
     */
    public float[] getInfo() {

        float[] scores = new float[is.length(0)];
        Extractor.encode3(is.pposs[0], d.heads, d.labels, d2, scores);

        return scores;
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
        //		out.println("Writting model: "+name);
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(name)));
        zos.putNextEntry(new ZipEntry("data"));
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(zos));

        MFO.writeData(dos);
        cs.write(dos);

        params.write(dos);

        dos.writeBoolean(options.stack);
        dos.writeInt(options.featureCreation);

        Edges.write(dos);

        dos.writeBoolean(options.decodeProjective);

        dos.writeInt(Extractor.maxForm);

        dos.writeInt(5);  // Info count
        dos.writeUTF("Used parser   " + Parser.class.toString());
        dos.writeUTF("Creation date " + (new SimpleDateFormat("yyyy.MM.dd HH:mm:ss")).format(new Date()));
        dos.writeUTF("Training data " + options.trainfile);
        dos.writeUTF("Iterations    " + options.numIters + " Used sentences " + options.count);
        dos.writeUTF("Cluster       " + options.clusterFile);

        dos.flush();
        dos.close();
    }

    @Override
    public boolean retrain(SentenceData09 sentence, float upd, int iterations) {

        params.total = params.parameters;

        boolean done = false;

        for (int k = 0; k < iterations; k++) {
            try {
                // create the data structure
                DataFES data = new DataFES(sentence.length(), pipe.mf.getFeatureCounter().get(PipeGen.REL).shortValue());


                Instances ins = new Instances();
                ins.m_encoder = pipe.mf;



                ins.init(1, pipe.mf, options.formatTask);
                new CONLLReader09().insert(ins, sentence);

//				String list[] = ((MFO)is.m_encoder).reverse(((MFO)is.m_encoder).getFeatureSet().get(Pipe.POS));
//				for(String s :list) {
//					out.println(s+" ");
//				}

//				for(int i=0;i<is.length(0);i++) {

//					out.printf("%d\t %d\t %d \n",i,is.forms[0][i],is.pposs[0][i] );
//					out.printf("%s\t form:%s pos:%s\n",i,sentence.forms[i],sentence.ppos[i]);

//				}

                SentenceData09 i09 = new SentenceData09(sentence);
                i09.createSemantic(sentence);

                // create the weights 
                data = pipe.fillVector((F2SF) params.getFV(), ins, 0, data, pipe.cl);

                short[] pos = ins.pposs[0];

                // parse the sentence
                Parse d = Decoder.decode(pos, data, options.decodeProjective, Decoder.TRAINING);

                // training successful?
                double e = pipe.errors(ins, 0, d);
                //			out.println("errors "+e);
                if (e == 0) {
                    done = true;
                    break;
                }

                // update the weight vector
                FV pred = new FV();
                pipe.extractor[0].encodeCat(ins, 0, pos, ins.forms[0], ins.plemmas[0], d.heads, d.labels, ins.feats[0], pipe.cl, pred);

                params.getFV();

                FV act = new FV();
                pipe.extractor[0].encodeCat(ins, 0, pos, ins.forms[0], ins.plemmas[0], ins.heads[0], ins.labels[0], ins.feats[0], pipe.cl, act);

                params.update(act, pred, ins, 0, d, upd, e);

                if (upd > 0) {
                    upd--;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Decoder.executerService.shutdown();
        Pipe.executerService.shutdown();

        return done;
    }

    @Override
    public boolean retrain(SentenceData09 sentence, float upd, int iterations,
            boolean print) {
        return retrain(sentence, upd, iterations);
    }
    
    public static String CalculateTime(long startTime, long endTime) {
        int a = 1000000;
        long time = (endTime-startTime)/1000;
        int hour = (int)(time / 3600);
        String hh = String.valueOf(hour);
        if (hh.length() == 1)
            hh = "0" + hh;
        
        time = time % 3600;
        int min = (int)(time / 60);
        String mm = String.valueOf(min);
        if (mm.length() == 1)
            mm = "0" + mm;
        
        int second = (int)(time % 60);
        String ss = String.valueOf(second);
        if (ss.length() == 1)
            ss = "0" + ss;
        
        return String.format("%s:%s:%s", hh, mm, ss);
    }
}