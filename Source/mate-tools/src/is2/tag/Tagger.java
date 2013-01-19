package is2.tag;

import is2.data.*;
import is2.io.CONLLReader09;
import is2.io.CONLLWriter09;
import is2.parser.Parser;
import is2.tools.IPipe;
import is2.tools.Tool;
import is2.tools.Train;
import is2.util.DB;
import is2.util.Evaluator;
import is2.util.OptionsSuper;
import java.io.*;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Tagger implements Tool, Train {

    public ExtractorT2 pipe;
    public ParametersFloat params;
    public Long2IntInterface li;
    public MFO mf;
    private OptionsSuper _options;

    /**
     * Initialize
     *
     * @param options
     */
    public Tagger(Options options) {


        // load the model
        try {
            readModel(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Tagger() {
    }

    /**
     * @param modelFileName the file name of the model
     */
    public Tagger(String modelFileName) {
        this(new Options(new String[]{"-model", modelFileName}));
    }

    public static void main(String[] args) throws FileNotFoundException, Exception {

        long start = System.currentTimeMillis();
        Options options = new Options(args);

        Tagger tagger = new Tagger();

        if (options.train) {

            //		depReader.normalizeOn=false;

            tagger.li = new Long2Int(options.hsize);
            tagger.pipe = new ExtractorT2(options, tagger.mf = new MFO());

            //tagger.pipe.li =tagger.li;

            InstancesTagger is = (InstancesTagger) tagger.pipe.createInstances(options.trainfile);

            tagger.params = new ParametersFloat(tagger.li.size());

            tagger.train(options, tagger.pipe, tagger.params, is);
            tagger.writeModel(options, tagger.pipe, tagger.params);
        }

        if (options.test) {

            tagger.readModel(options);

            tagger.out(options, tagger.pipe, tagger.params);
        }

        Parser.out.println();

        if (options.eval) {
            Parser.out.println("\nEVALUATION PERFORMANCE:");
            Evaluator.evaluateTagger(options.goldfile, options.outfile, options.format);
        }
        long end = System.currentTimeMillis();
        Parser.out.println("used time " + ((float) ((end - start) / 100) / 10));
    }

    @Override
    public void readModel(OptionsSuper options) {

        try {
            pipe = new ExtractorT2(options, mf = new MFO());
            _options = options;
            // load the model
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(options.modelName)));
            zis.getNextEntry();
            DataInputStream dis = new DataInputStream(new BufferedInputStream(zis));

            pipe.mf.read(dis);
            pipe.initValues();
            pipe.initFeatures();

            params = new ParametersFloat(0);
            params.read(dis);
            li = new Long2Int(params.parameters.length);
            pipe.read(dis);

            dis.close();

            pipe.types = new String[pipe.mf.getFeatureCounter().get(ExtractorT2.POS)];
            for (Entry<String, Integer> e : pipe.mf.getFeatureSet().get(ExtractorT2.POS).entrySet()) {
                pipe.types[e.getValue()] = e.getKey();
            }

            DB.println("Loading data finished. ");
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
     * @throws IOException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    @Override
    public void train(OptionsSuper options, IPipe pipe, ParametersFloat params, Instances is2) {

        InstancesTagger is = (InstancesTagger) is2;
        String wds[] = MFO.reverse(this.pipe.mf.getFeatureSet().get(ExtractorT2.WORD));

        int pd[] = new int[this.pipe.types.length];
        for (int k = 0; k < pd.length; k++) {
            pd[k] = k;
        }

        int del = 0;
        F2SF f = new F2SF(params.parameters);
        long vs[] = new long[ExtractorT2._MAX];

        int types = this.pipe.types.length;

        double upd = options.numIters * is.size() + 1;

        for (int i = 0; i < options.numIters; i++) {

            long start = System.currentTimeMillis();

            int numInstances = is.size();

            long last = System.currentTimeMillis();
            FV pred = new FV(), gold = new FV();

            int correct = 0, count = 0;
            Parser.out.print("Iteration " + i + ": ");

            for (int n = 0; n < numInstances; n++) {

                if ((n + 1) % 500 == 0) {
                    del = PipeGen.outValueErr(n + 1, (count - correct), (float) correct / (float) count, del, last, upd);
                }

                int length = is.length(n);

                upd--;

                for (int w = 1; w < length; w++) {

                    double best = -1000;
                    short bestType = -1;

                    int[] lemmas; //= is.lemmas[n];
                    if (options.noLemmas) {
                        lemmas = new int[is.length(n)];
                    } else {
                        lemmas = is.plemmas[n];
                    }

                    this.pipe.addFeatures(is, n, wds[is.forms[n][w]], w, is.gpos[n], is.forms[n], lemmas, vs);

                    for (short t = 0; t < types; t++) {

                        // the hypotheses of a part of speech tag
                        long p = t << ExtractorT2.s_type;
                        f.clear();

                        // add the features to the vector
                        for (int k1 = 0; vs[k1] != Integer.MIN_VALUE; k1++) {
                            if (vs[k1] > 0) {
                                f.add(this.li.l2i(vs[k1] | p));
                            }
                        }

                        if (f.score > best) {
                            bestType = t;
                            best = f.score;
                        }
                    }

                    count++;
                    if (bestType == is.gpos[n][w]) {
                        correct++;
                        continue;
                    }

                    pred.clear();
                    for (int k1 = 0; vs[k1] != Integer.MIN_VALUE; k1++) {
                        if (vs[k1] > 0) {
                            pred.add(this.li.l2i(vs[k1] | bestType << ExtractorT2.s_type));
                        }
                    }

                    gold.clear();
                    for (int k1 = 0; vs[k1] != Integer.MIN_VALUE; k1++) {
                        if (vs[k1] > 0) {
                            gold.add(this.li.l2i(vs[k1] | is.gpos[n][w] << ExtractorT2.s_type));
                        }
                    }

                    params.update(pred, gold, (float) upd, 1.0F);
                }
            }

            long end = System.currentTimeMillis();
            String info = "time " + (end - start);
            PipeGen.outValueErr(numInstances, (count - correct), (float) correct / (float) count, last, upd, info);
            Parser.out.println();
            del = 0;
        }

        params.average(options.numIters * is.size());
    }

    /**
     * Tag a sentence
     *
     * @param options
     * @param pipe
     * @param params
     * @throws IOException
     */
    @Override
    public void out(OptionsSuper options, IPipe pipe, ParametersFloat params) {

        try {

            long start = System.currentTimeMillis();
// change this backe!!!
//		CONLLReader09 depReader = new CONLLReader09(options.testfile, CONLLReader09.NO_NORMALIZE);
            CONLLReader09 depReader = new CONLLReader09(options.testfile);

            CONLLWriter09 depWriter = new CONLLWriter09(options.outfile);

            Parser.out.print("Processing Sentence: ");
            pipe.initValues();

            int cnt = 0;
            int del = 0;
            while (true) {

                InstancesTagger is = new InstancesTagger();
                is.init(1, mf);
                SentenceData09 instance = depReader.getNext(is);
                if (instance == null || instance.forms == null) {
                    break;
                }


                is.fillChars(instance, 0, ExtractorT2._CEND);

                cnt++;


                tag(is, instance);

                SentenceData09 i09 = new SentenceData09(instance);
                i09.createSemantic(instance);
                depWriter.write(i09);

                if (cnt % 100 == 0) {
                    del = PipeGen.outValue(cnt, del);
                }

            }
            del = PipeGen.outValue(cnt, del);
            depWriter.finishWriting();

            float min = 1000, max = -1000;

            //	int r[] = new int[14];
		/*
             * for(Entry<Float, Integer> e : map.entrySet()) {
             * if(e.getKey()<min)min=e.getKey();
             * if(e.getKey()>max)max=e.getKey();
             *
             * if(e.getKey()<0.2) r[0]++; else if(e.getKey()<0.5)
             * r[1]+=e.getValue(); else if(e.getKey()<0.7) r[2]+=e.getValue();
             * else if(e.getKey()<0.8) r[3]+=e.getValue(); else
             * if(e.getKey()<0.9) r[4]+=e.getValue(); else if(e.getKey()<1.0)
             * r[5]+=e.getValue(); else if(e.getKey()<1.2) r[6]+=e.getValue();
             * else if(e.getKey()<1.3) r[7]+=e.getValue(); else
             * if(e.getKey()<1.4) r[8]+=e.getValue(); else if(e.getKey()<1.5)
             * r[9]+=e.getValue(); else if(e.getKey()<1.9) r[10]+=e.getValue();
             * else if(e.getKey()<2.2) r[11]+=e.getValue(); else
             * if(e.getKey()<2.5) r[12]+=e.getValue(); else if(e.getKey()>=2.5)
             * r[13]+=e.getValue(); }
             */
            //	for(int k=0;k<r.length;k++) Parser.out.println(k+" "+r[k][0]+" "+((float)r[k][1]/(float)r[k][0])+" good "+r[k][1]);
            //	Parser.out.println("min "+min+" "+max);

            long end = System.currentTimeMillis();
            Parser.out.println(PipeGen.getSecondsPerInstnace(cnt, (end - start)));
            Parser.out.println(PipeGen.getUsedTime(end - start));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SentenceData09 tag(SentenceData09 instance) throws IOException {
        InstancesTagger is = new InstancesTagger();
        is.init(1, pipe.mf);
        new CONLLReader09().insert(is, instance);
        is.fillChars(instance, 0, ExtractorT2._CEND);
        tag(is, instance);

        return instance;
    }

    private void tag(InstancesTagger is, SentenceData09 instance) {

        int length = instance.ppos.length;

        short[] pos = new short[instance.gpos.length];

        float sc[] = new float[instance.ppos.length];

        instance.ppos[0] = is2.io.CONLLReader09.ROOT_POS;
        pos[0] = (short) pipe.mf.getValue(ExtractorT2.POS, is2.io.CONLLReader09.ROOT_POS);

        for (int j = 1; j < length; j++) {

            short bestType = (short) pipe.fillFeatureVectorsOne(instance.forms[j], params, j, is, 0, pos, this.li, sc);
            pos[j] = bestType;
            instance.ppos[j] = pipe.types[bestType];
        }

        for (int j = 1; j < length; j++) {

            short bestType = (short) pipe.fillFeatureVectorsOne(instance.forms[j], params, j, is, 0, pos, this.li, sc);
            instance.ppos[j] = pipe.types[bestType];
            pos[j] = bestType;
        }
    }

    /**
     * Tag a single word and return a n-best list of Part-of-Speech tags.
     *
     * @param is set of sentences
     * @param instanceIndex index to the sentence in question
     * @param word word to be tagged
     * @return n-best list of Part-of-Speech tags
     */
    public ArrayList<POS> tag(InstancesTagger is, int instanceIndex, int word, String wordForm) {

        return pipe.classify(wordForm, params, word, is, instanceIndex, is.pposs[instanceIndex], li);

    }

    public ArrayList<String> tagStrings(InstancesTagger is, int instanceIndex, int word, String wordForm) {

        ArrayList<POS> plist = pipe.classify(wordForm, params, word, is, instanceIndex, is.pposs[instanceIndex], li);
        String pos[] = is2.tag.MFO.reverse(this.pipe.mf.getFeatureSet().get(ExtractorT2.POS));

        ArrayList<String> postags = null;
        for (POS p : plist) {
            try {
                postags.add(pos[p.p]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return postags;
    }

    /**
     * Tag a sentence
     *
     * @param options
     * @param pipe
     * @param parametersReranker
     * @throws IOException
     */
    public String[] tag(String[] words, String[] lemmas) {

        String[] pposs = new String[words.length];

        try {
            pipe.initValues();

            int length = words.length + 1;


            InstancesTagger is = new InstancesTagger();
            is.init(1, pipe.mf);
            is.createInstance09(length);

            SentenceData09 instance = new SentenceData09();
            instance.forms = new String[length];
            instance.forms[0] = is2.io.CONLLReader09.ROOT;

            instance.plemmas = new String[length];
            instance.plemmas[0] = is2.io.CONLLReader09.ROOT_LEMMA;

            for (int j = 0; j < words.length; j++) {
                instance.forms[j + 1] = words[j];
                instance.plemmas[j + 1] = lemmas[j];
            }

            for (int j = 0; j < length; j++) {
                is.setForm(0, j, instance.forms[j]);
                is.setLemma(0, j, instance.plemmas[j]);
            }

            instance.ppos = new String[length];

            is.fillChars(instance, 0, ExtractorT2._CEND);

            this.tag(is, instance);

            for (int j = 0; j < words.length; j++) {
                pposs[j] = instance.ppos[j + 1];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pposs;
    }

    /*
     * (non-Javadoc) @see is2.tools.Tool#apply(is2.data.SentenceData09)
     */
    @Override
    public SentenceData09 apply(SentenceData09 snt09) {
        try {
            tag(snt09);
        } catch (Exception e) {
        }
        return snt09;
    }

    /*
     * (non-Javadoc) @see is2.tools.Train#writeModel(is2.util.OptionsSuper,
     * is2.mtag2.Pipe, is2.data.ParametersFloat)
     */
    @Override
    public void writeModel(OptionsSuper options, IPipe pipe, is2.data.ParametersFloat params) {
        try {
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(options.modelName)));
            zos.putNextEntry(new ZipEntry("data"));
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(zos));

            this.pipe.mf.writeData(dos);

            DB.println("number of parameters " + params.parameters.length);
            dos.flush();

            params.write(dos);
            pipe.write(dos);
            dos.flush();
            dos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}