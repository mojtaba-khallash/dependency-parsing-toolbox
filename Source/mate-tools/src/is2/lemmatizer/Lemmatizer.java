package is2.lemmatizer;

import is2.data.*;
import is2.io.CONLLReader09;
import is2.io.CONLLWriter09;
import is2.parser.Parser;
import is2.tools.IPipe;
import is2.tools.Tool;
import is2.tools.Train;
import is2.util.DB;
import is2.util.OptionsSuper;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Lemmatizer implements Tool, Train {

    public Pipe pipe;
    public ParametersFloat params;
    private Long2Int li;
    private long[] vs = new long[40];

    public Lemmatizer(Options options) {
        this.readModel(options);
    }

    /**
     * Creates a lemmatizer due to the model stored in modelFileName
     *
     * @param modelFileName the path and file name to a lemmatizer model
     */
    public Lemmatizer(String modelFileName) {

        // tell the lemmatizer the location of the model
        try {
            Options m_options = new Options(new String[]{"-model", modelFileName});
            li = new Long2Int(m_options.hsize);

            // initialize the lemmatizer
            readModel(m_options);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Lemmatizer() {
    }

    public static void main(String[] args) throws FileNotFoundException, Exception {

        Lemmatizer lemmatizer = new Lemmatizer();

        long start = System.currentTimeMillis();
        Options options = new Options(args);

        if (options.train) {


            lemmatizer.li = new Long2Int(options.hsize);
            lemmatizer.pipe = new Pipe(options, lemmatizer.li);

            InstancesTagger is = lemmatizer.pipe.createInstances(options.trainfile);

            DB.println("Features: " + lemmatizer.pipe.mf.size() + " Operations " + lemmatizer.pipe.mf.getFeatureCounter().get(Pipe.OPERATION));

            ParametersFloat params = new ParametersFloat(lemmatizer.li.size());

            lemmatizer.train(options, lemmatizer.pipe, params, is);

            lemmatizer.writeModel(options, lemmatizer.pipe, params);
        }

        if (options.test) {

            lemmatizer.readModel(options);

            lemmatizer.out(options, lemmatizer.pipe, lemmatizer.params);
        }

        Parser.out.println();

        if (options.eval) {
            Parser.out.println("\nEVALUATION PERFORMANCE:");
            Evaluator.evaluate(options.goldfile, options.outfile, options.formatTask);
        }
        long end = System.currentTimeMillis();
        Parser.out.println("used time " + ((float) ((end - start) / 100) / 10));
    }

    /*
     * (non-Javadoc) @see is2.tools.Train#writeModel(is2.util.OptionsSuper,
     * is2.tools.IPipe, is2.data.ParametersFloat)
     */
    @Override
    public void writeModel(OptionsSuper options, IPipe pipe,
            ParametersFloat params) {
        try {
            // store the model
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(options.modelName)));
            zos.putNextEntry(new ZipEntry("data"));
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(zos));

            MFO.writeData(dos);

            dos.flush();
            params.write(dos);

            pipe.write(dos);

            dos.flush();
            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void readModel(OptionsSuper options) {

        try {

            // load the model
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(options.modelName)));
            zis.getNextEntry();
            DataInputStream dis = new DataInputStream(new BufferedInputStream(zis));

            MFO mf = new MFO();
            mf.read(dis);
            params = new ParametersFloat(0);
            params.read(dis);
            li = new Long2Int(params.size());
            pipe = new Pipe(options, li);
            pipe.mf = mf;

            pipe.initFeatures();
            pipe.initValues();

            pipe.readMap(dis);

            for (Entry<String, Integer> e : MFO.getFeatureSet().get(Pipe.OPERATION).entrySet()) {
                this.pipe.types[e.getValue()] = e.getKey();
                //	Parser.out.println("set pos "+e.getKey());
            }


            pipe.cl = new Cluster(dis);

            dis.close();
            DB.println("Loading data finished. ");

            DB.println("number of params  " + params.parameters.length);
            DB.println("number of classes " + pipe.types.length);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Do the training
     *
     * @param instanceLengths
     * @param options
     * @param pipe
     * @param params
     * @param li
     * @throws IOException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    @Override
    public void train(OptionsSuper options, IPipe p, ParametersFloat params, Instances ist) {

        InstancesTagger is = (InstancesTagger) ist;

        int i, del = 0;
        FV g = new FV(), f = new FV();

        int LC = this.pipe.types.length + 1, UC = LC + 1;

        String wds[] = MFO.reverse(MFO.getFeatureSet().get(Pipe.WORD));

        F2SF fs = params.getFV();
        double upd = 0;

        for (i = 0; i < options.numIters; i++) {

            Parser.out.print("Iteration " + i + ": ");

            long start = System.currentTimeMillis();
            int numInstances = is.size();
            int correct = 0, count = 0;

            long last = System.currentTimeMillis();
            int wrongOp = 0, correctOp = 0, correctUC = 0, wrongUC = 0;

            HashMap<String, Integer> map = new HashMap<>();

            for (int n = 0; n < numInstances; n++) {

                if ((n + 1) % 500 == 0) {
                    del = Pipe.outValueErr(n + 1, (float) (count - correct), (float) correct / (float) count, del, last, upd);
                }

                upd = (double) (options.numIters * numInstances - (numInstances * i + (n + 1)) + 1);

                for (int k = 0; k < is.length(n); k++) {

                    double best = -1000;
                    String bestOp = "";



                    count++;
                    pipe.addCoreFeatures(is, n, k, 0, wds[is.forms[n][k]], vs);

                    String lemma = pipe.opse.get(wds[is.forms[n][k]].toLowerCase());


                    // predict
                    if (lemma == null) {
                        for (int t = 0; t < pipe.types.length; t++) {

                            fs.clear();
                            for (int l = vs.length - 1; l >= 0; l--) {
                                if (vs[l] > 0) {
                                    fs.add(li.l2i(vs[l] + (t * Pipe.s_type)));
                                }
                            }

                            float score = (float) fs.getScore();
                            if (score > best) {
                                bestOp = pipe.types[t];
                                best = score;
                            }
                        }
                    }

                    if (options.upper) {
                        fs.clear();
                        for (int l = vs.length - 1; l >= 0; l--) {
                            if (vs[l] > 0) {
                                fs.add(li.l2i(vs[l] + (LC * Pipe.s_type)));
                            }
                        }

                        int correctOP = -1, selectedOP = -1;
                        if (wds[is.glemmas[n][k]].length() > 0
                                && Character.isUpperCase(wds[is.glemmas[n][k]].charAt(0))
                                && fs.score > 0) {

                            correctOP = UC;
                            selectedOP = LC;
                        } else if (wds[is.glemmas[n][k]].length() > 0
                                && Character.isLowerCase(wds[is.glemmas[n][k]].charAt(0))
                                && fs.score <= 0) {


                            correctOP = LC;
                            selectedOP = UC;
                        }

                        if (correctOP != -1 && wds[is.glemmas[n][k]].length() > 0) {

                            wrongUC++;
                            f.clear();
                            for (int l = vs.length - 1; l >= 0; l--) {
                                if (vs[l] > 0) {
                                    f.add(li.l2i(vs[l] + (selectedOP * Pipe.s_type)));
                                }
                            }

                            g.clear();
                            for (int l = vs.length - 1; l >= 0; l--) {
                                if (vs[l] > 0) {
                                    g.add(li.l2i(vs[l] + (correctOP * Pipe.s_type)));
                                }
                            }

                            double lam_dist = params.getScore(g) - params.getScore(f);//f
                            double loss = 1 - lam_dist;

                            FV dist = g.getDistVector(f);
                            dist.update(params.parameters, params.total, params.update(dist, loss), upd, false);

                        } else {
                            correctUC++;
                        }
                    }
                    if (lemma != null) {
                        correct++;
                        correctOp++;
                        continue;
                    }


                    String op = Pipe.getOperation(is, n, k, wds);
                    if (op.equals(bestOp)) {
                        correct++;
                        correctOp++;
                        continue;
                    }
                    wrongOp++;

                    f.clear();
                    int bop = pipe.mf.getValue(Pipe.OPERATION, bestOp);
                    for (int r = vs.length - 1; r >= 0; r--) {
                        if (vs[r] > 0) {
                            f.add(li.l2i(vs[r] + (bop * Pipe.s_type)));
                        }
                    }

                    g.clear();
                    int gop = pipe.mf.getValue(Pipe.OPERATION, op);
                    for (int r = vs.length - 1; r >= 0; r--) {
                        if (vs[r] > 0) {
                            g.add(li.l2i(vs[r] + (gop * Pipe.s_type)));
                        }
                    }
                    double lam_dist = params.getScore(g) - params.getScore(f);//f

                    double loss = 1 - lam_dist;

                    FV dist = g.getDistVector(f);

                    dist.update(params.parameters, params.total, params.update(dist, loss), upd, false); //0.05

                }

            }
            ArrayList<Entry<String, Integer>> opsl = new ArrayList<>();
            for (Entry<String, Integer> e : map.entrySet()) {
                if (e.getValue() > 1) {
                    opsl.add(e);
                }
            }

            Collections.sort(opsl, new Comparator<Entry<String, Integer>>() {

                @Override
                public int compare(Entry<String, Integer> o1,
                        Entry<String, Integer> o2) {

                    return o1.getValue() == o2.getValue() ? 0 : o1.getValue() > o2.getValue() ? 1 : -1;
                }
            });

            if (opsl.size() > 0) {
                Parser.out.println();
            }
            for (Entry<String, Integer> e : opsl) {
                Parser.out.println(e.getKey() + "  " + e.getValue());
            }
            map.clear();

            Pipe.outValueErr(numInstances, (float) (count - correct), (float) correct / (float) count, last, upd,
                    "time " + (System.currentTimeMillis() - start)
                    + " corr/wrong " + correctOp + " " + wrongOp + " uppercase corr/wrong  " + correctUC + " " + wrongUC);
            del = 0;
            Parser.out.println();
        }

        params.average(i * is.size());

    }

    /**
     * Do the work
     *
     * @param options
     * @param pipe
     * @param params
     * @throws IOException
     */
    @Override
    public void out(OptionsSuper options, IPipe pipe, ParametersFloat params) {

        long start = System.currentTimeMillis();

        CONLLReader09 depReader = new CONLLReader09(options.testfile, CONLLReader09.NO_NORMALIZE);
        depReader.setInputFormat(options.formatTask);
        CONLLWriter09 depWriter = new CONLLWriter09(options.outfile);
        depWriter.setOutputFormat(options.formatTask);

        Parser.out.print("Processing Sentence: ");

        int cnt = 0;
        int del = 0;

        try {

            while (true) {

                InstancesTagger is = new InstancesTagger();

                is.init(1, new MFO());
                SentenceData09 instance = depReader.getNext(is);//pipe.nextInstance(null, depReader);

                if (instance == null) {
                    break;
                }
                is.fillChars(instance, 0, Pipe._CEND);
                cnt++;
                SentenceData09 i09 = lemmatize(is, instance, this.li);

                if (options.normalize) {
                    for (int k = 0; k < i09.length(); k++) {
                        boolean save = depReader.normalizeOn;
                        depReader.normalizeOn = true;
                        i09.plemmas[k] = depReader.normalize(i09.plemmas[k]);
                        depReader.normalizeOn = save;
                    }
                }

                if (options.overwritegold) {
                    i09.lemmas = i09.plemmas;
                }



                depWriter.write(i09);

                if (cnt % 100 == 0) {
                    del = Pipe.outValue(cnt, del);
                }
            }
            depWriter.finishWriting();
            del = Pipe.outValue(cnt, del);
            long end = System.currentTimeMillis();

            Parser.out.println(PipeGen.getSecondsPerInstnace(cnt, (end - start)));
            Parser.out.println(PipeGen.getUsedTime(end - start));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SentenceData09 lemmatize(InstancesTagger is, SentenceData09 instance, Long2Int li) {

        int LC = pipe.types.length + 1;

        is.feats[0] = new short[instance.length()][11];

        is.fillChars(instance, 0, Pipe._CEND);

        int length = instance.length();

        F2SF fs = new F2SF(params.parameters);


        for (int w1 = 0; w1 < length; w1++) {
            instance.plemmas[w1] = "_";
            pipe.addCoreFeatures(is, 0, w1, 0, instance.forms[w1], vs);

            String f = null;
            if (is.forms[0][w1] != -1) {
                f = pipe.opse.get(instance.forms[w1].toLowerCase());
                if (f != null) {
                    instance.plemmas[w1] = f;
                }
            }
            double best = -1000.0;
            int bestOp = 0;

            for (int t = 0; t < pipe.types.length; t++) {

                fs.clear();
                for (int l = vs.length - 1; l >= 0; l--) {
                    if (vs[l] > 0) {
                        fs.add(li.l2i(vs[l] + (t * Pipe.s_type)));
                    }
                }

                if (fs.score >= best) {
                    best = fs.score;
                    bestOp = t;
                }
            }
            //instance.ppos[w1]=""+bestOp;
            if (f == null) {
                instance.plemmas[w1] = StringEdit.change(instance.forms[w1], pipe.types[bestOp]);
            }

            // check for empty string
            if (instance.plemmas[w1].length() == 0) {
                instance.plemmas[w1] = "_";
            }

            fs.clear();
            for (int l = vs.length - 1; l >= 0; l--) {
                if (vs[l] > 0) {
                    fs.add(li.l2i(vs[l] + (LC * Pipe.s_type)));
                }
            }

            try {

                if (fs.score <= 0 && instance.plemmas[w1].length() > 1) {
                    instance.plemmas[w1] = Character.toUpperCase(instance.plemmas[w1].charAt(0)) + instance.plemmas[w1].substring(1);
                } else if (fs.score <= 0 && instance.plemmas[w1].length() > 0) {
                    instance.plemmas[w1] = String.valueOf(Character.toUpperCase(instance.plemmas[w1].charAt(0)));
                } else if (fs.score > 0) {
                    instance.plemmas[w1] = instance.plemmas[w1].toLowerCase();
                }

            } catch (Exception e) {
                e.printStackTrace();
                //	Parser.out.println("error "+pipe.types[bestOp]+" "+instance.forms[w1]);
            }
        }


        SentenceData09 i09 = new SentenceData09(instance);
        i09.createSemantic(instance);
        return i09;
    }

    /*
     * (non-Javadoc) @see is2.tools.Tool#apply(is2.data.SentenceData09)
     */
    @Override
    public SentenceData09 apply(SentenceData09 snt09) {
        InstancesTagger is = new InstancesTagger();

        is.init(1, new MFO());
        is.createInstance09(snt09.length());
        is.fillChars(snt09, 0, Pipe._CEND);

        for (int j = 0; j < snt09.length(); j++) {
            is.setForm(0, j, snt09.forms[j]);
        }

        return lemmatize(is, snt09, li);
    }
}