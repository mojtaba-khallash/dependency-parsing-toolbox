package is2.parserR2;

import extractors.Extractor;
import extractors.ExtractorClusterStacked;
import extractors.ExtractorReranker;
import is2.data.*;
import is2.io.CONLLReader09;
import is2.io.CONLLWriter09;
import is2.tools.Tool;
import is2.util.DB;
import is2.util.OptionsSuper;
import is2.util.ParserEvaluator;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Reranker implements Tool {

    public static int THREADS = 4;
    Long2IntInterface l2i;
    // the parser models
    ParametersFloat paramsParsers[];
    // the re-ranker model
    ParametersFloat parametersReranker;
    PipeReranker pipeReranker;
    Pipe pipe;
    Options options;
    HashMap<Integer, Integer> rank = new HashMap<>();
    int amongxbest = 0, amongxbest_ula = 0, nbest = 0, bestProj = 0, smallestErrorSum = 0, countAllNodes = 0;
    static int NBest = 1000;

    /**
     * Initialize the parser
     *
     * @param options
     */
    public Reranker(Options options) {

        this.options = options;
    }

    /**
     * @param modelFileName The file name of the parsing model
     */
    public Reranker(String modelFileName) {
        this(new Options(new String[]{"-model", modelFileName}));
    }

    public Reranker() {
    }

    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();
        Options options = new Options(args);

        NBest = options.best;

        DB.println("n-best " + NBest);

        Runtime runtime = Runtime.getRuntime();
        THREADS = runtime.availableProcessors();

        if (options.cores < THREADS && options.cores > 0) {
            THREADS = options.cores;
        }

        DB.println("Found " + runtime.availableProcessors() + " cores use " + THREADS);


        if (options.train) {

            Reranker p = new Reranker();
            p.options = options;


            p.l2i = new Long2Int(options.hsize);
            p.pipeReranker = new PipeReranker(options);
            p.pipeReranker.extractor = new ExtractorReranker(p.l2i);


            // initialize the parser
            p.pipe = new Pipe(options);

            // read parsing models
            p.paramsParsers = new ParametersFloat[options.end + 1];
            for (int m = 0; m <= options.end; m++) {
                String name = options.prefix_model + m;
                p.paramsParsers[m] = new ParametersFloat(0);
                p.readModel(name, p.pipe, p.paramsParsers[m]);
            }

            // set up the reranker
            p.parametersReranker = new ParametersFloat(p.l2i.size());

            Instances[] iss = new Instances[options.end + 1];

            for (int m = 0; m <= options.end; m++) {
                String name = options.prefix_test + m;
                iss[m] = new Instances();
                DB.println("create instances of part " + name);
                p.pipe.getInstances(name, iss[m]);
            }


            ExtractorReranker.initFeatures();
            p.pipeReranker.extractor.init();

            ExtractorReranker.initStat();

            p.train(options, iss);

            p.writeModell(options, p.parametersReranker, null, p.pipe.cl);
        }

        if (options.test) {

            Reranker p = new Reranker();
            p.options = options;

            // set up the reranker
            p.l2i = new Long2Int(options.hsize);
            p.pipeReranker = new PipeReranker(options);
            p.pipeReranker.extractor = new ExtractorReranker(p.l2i);
            p.parametersReranker = new ParametersFloat(p.l2i.size());


            // initialize the parser
            p.pipe = new Pipe(options);

            // read parsing models
            p.paramsParsers = new ParametersFloat[options.end + 1];

            String nbestName = "n-best+" + options.testfile.substring(options.testfile.length() - 12, options.testfile.length() - 1);
            File fnbest = new File(nbestName);
            int read = fnbest.exists() ? 2 : 1;

            if (read != 2) {
                for (int m = 0; m <= options.end; m++) {
                    String name = options.prefix_model + m;
                    p.paramsParsers[m] = new ParametersFloat(0);
                    p.readModel(name, p.pipe, p.paramsParsers[m]);
                }
            }

            p.readModel(options.modelName, p.pipeReranker, p.parametersReranker);


            ExtractorReranker.initFeatures();
            ExtractorReranker.initStat();
            p.pipeReranker.extractor.init();

            p.rerankedParses(options, p.pipe, p.parametersReranker, false, nbestName);
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
     * @param prm
     * @throws IOException
     */
    public void readModel(String modelName, Pipe pipe, Parameters prm) throws IOException {


        DB.println("Reading data started: " + modelName);

        // prepare zipped reader
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(modelName)));
        zis.getNextEntry();
        DataInputStream dis = new DataInputStream(new BufferedInputStream(zis));

        pipe.mf.read(dis);

        pipe.cl = new Cluster(dis);

        prm.read(dis);

        Long2Int l2i = new Long2Int(prm.size());
        DB.println("li size " + l2i.size());

        pipe.extractor = new ExtractorClusterStacked[THREADS];

        for (int t = 0; t < THREADS; t++) {
            pipe.extractor[t] = new ExtractorClusterStacked(l2i);
        }

        ExtractorClusterStacked.initFeatures();


        for (int t = 0; t < THREADS; t++) {
            pipe.extractor[t].initStat();
            pipe.extractor[t].init();
        }

        Edges.read(dis);

        options.decodeProjective = dis.readBoolean();

        ExtractorClusterStacked.maxForm = dis.readInt();

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


        dis.close();

        DB.println("Reading data finnished");

        Decoder.NON_PROJECTIVITY_THRESHOLD = (float) options.decodeTH;

        //	ExtractorClusterStacked.initStat();
    }

    /**
     * Read the models and mapping
     *
     * @param options
     * @param pipe
     * @param params
     * @throws IOException
     */
    public void readModel(String modelName, PipeReranker pipe, Parameters params) throws IOException {

        DB.println("Reading data started: " + modelName);

        // prepare zipped reader
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(modelName)));
        zis.getNextEntry();
        DataInputStream dis = new DataInputStream(new BufferedInputStream(zis));

        pipe.mf.read(dis);

        //	DB.println("reranker model "+pipe.mf.toString());

        pipe.cl = new Cluster(dis);

        params.read(dis);
        this.l2i = new Long2Int(params.size());
        DB.println("li size " + l2i.size());

        pipe.extractor = new ExtractorReranker(l2i);

        ExtractorReranker.initFeatures();
        ExtractorReranker.initStat();

        pipe.extractor.init();

        Edges.read(dis);

        options.decodeProjective = dis.readBoolean();

        ExtractorClusterStacked.maxForm = dis.readInt();

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


        dis.close();

        DB.println("Reading data finnished");

        Decoder.NON_PROJECTIVITY_THRESHOLD = (float) options.decodeTH;

        //ExtractorClusterStacked.initStat();
    }

    /**
     * Do the training
     *
     * @param instanceLengths
     * @param options
     * @param pipe
     * @param parametersReranker
     * @param is
     * @param cluster
     * @throws IOException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    public void train(OptionsSuper options, Instances[] iss)
            throws IOException, InterruptedException, ClassNotFoundException {



        int read = 0; // 0 compute -- 1 compute and write -- 2 read parses


        DB.println("Training Information ");
        DB.println("-------------------- ");

        ExtractorReranker.initStat();
        pipeReranker.extractor.init();

        for (Extractor e : this.pipe.extractor) {
            e.init();
        }

        int numInstances = 0;
        int maxLenInstances = 0;
        //	int maxLenSentence=1;
        for (Instances is : iss) {
            numInstances += is.size();
            for (int i = 0; i < is.size(); i++) {
                if (maxLenInstances < is.length(i)) {
                    maxLenInstances = is.length(i);
                }
            }
        }


        DataF data = new DataF(maxLenInstances, pipe.mf.getFeatureCounter().get(PipeGen.REL).shortValue());

        int iter = 0;
        float error;
        float f1;




        double upd = (double) (options.count * options.numIters) + options.numIters * 10;

        //float[][]  = new float[this.NBest][3];
        FVR act = new FVR();

        FVR pred = new FVR();

        FVR f = new FVR();
        long[] vs = new long[ExtractorReranker._FC * maxLenInstances];


        for (; iter < options.numIters; iter++) {

            is2.parser.Parser.out.print("Iteration " + iter + ": ");
            error = 0;
            f1 = 0;

            float las = 0, cnt = 0, averageScore = 0;


            float firstBestTotalError = 0, totalError = 0;

            long start = System.currentTimeMillis();

            long last = System.currentTimeMillis();

            long rerankTime = 0;


            String nbest = "n-best";
            File fnbest = new File(nbest);
            read = fnbest.exists() ? 2 : 1;

            DataInputStream dis = null;
            DataOutputStream dos = null;

            if (read == 1) {

                DB.println("computing and writting nbest list to file: " + nbest);

                ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(nbest)));
                zos.putNextEntry(new ZipEntry("data"));
                dos = new DataOutputStream(new BufferedOutputStream(zos));
            }


            // start reading again
            if (read == 2) {

                //		DB.println("reading nbest list from file: "+nbest);

                ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(nbest)));
                zis.getNextEntry();
                dis = new DataInputStream(new BufferedInputStream(zis));
            }

            HashMap<Integer, Integer> remapped = new HashMap<>();

            int i = 0, round = 0, instance = 0, length = 0, count = 0, changes = 0;
            for (Instances is : iss) {

                F2SF fparser = this.paramsParsers[instance].getFV();
                round++;


                // go over the sentences in the instance
                for (int n = 0; n < is.size(); n++) {
                    count += 1;
                    length += is.length(n);
                    upd--;

                    if (is.labels[n].length > options.maxLen) {
                        continue;
                    }

                    List<ParseNBest> parses = null;

                    short pos[] = is.pposs[n];

                    // read or write nbest list
                    if (read == 0 || read == 1 && iter == 0) {
                        data = pipe.fillVector(fparser, is, n, data, pipe.cl, THREADS, l2i);
                        parses = Decoder.decode(pos, data, options.decodeProjective, pipe.extractor[0]);

                        if (parses.size() > NBest) {
                            parses = parses.subList(0, NBest);
                        }


                        if (read == 1) {
                            // write the forest
                            dos.writeInt(parses.size());
                            for (int k = 0; k < parses.size(); k++) {
                                dos.writeUTF(parses.get(k).signature());
                                dos.writeFloat((float) parses.get(k).f1);
                            }
                        }
                    } else if (read == 2) {
                        parses = new ArrayList<>();
                        int parseCount = dis.readInt();
                        for (int k = 0; k < parseCount; k++) {
                            ParseNBest p = new ParseNBest(dis.readUTF(), dis.readFloat());
                            if (parses.size() < NBest) {
                                parses.add(p);
                            }
                        }
                    }


                    int best = 0;
                    float bestScore = -100;
                    int goldBest = 0;
                    double goldError = Float.MAX_VALUE;

                    long startReranking = System.currentTimeMillis();

                    // score the n-best parses
                    for (int k = 0; k < parses.size(); k++) {

                        ParseNBest p = parses.get(k);

                        pipeReranker.extractor.extractFeatures(is, n, p, parses.indexOf(p), vs, pipe.cl);

                        int rank = 1 * ExtractorReranker.s_type;

                        f.clear();

                        for (int j = 0; j < vs.length; j++) {
                            if (vs[j] == Integer.MIN_VALUE) {
                                break;
                            }
                            if (vs[j] > 0) {
                                f.add(pipeReranker.extractor.li.l2i(vs[j] + rank));
                            }
                        }

                        f.add(pipeReranker.extractor.li.l2i(1 + rank), (float) p.f1);
                        float score = (float) (parametersReranker.getScore(f));
                        if (score > bestScore) { //rankScore[k][2]>
                            bestScore = score;
                            best = k;

                        }
                    }

                    // get the best parse in the n-best list
                    for (int k = 0; k < parses.size(); k++) {

                        if (parses.get(k).heads.length != is.length(n)) {
                            DB.println("error " + n + " " + parses.get(k).heads.length + " " + is.length(n));
                            continue;
                        }
                        double errg = pipe.errors(is, n, parses.get(k));
                        if (goldError > errg) {
                            goldError = errg;
                            goldBest = k;
                        }
                    }

                    ParseNBest firstBest = parses.get(0);
                    ParseNBest predParse = parses.get(best);
                    ParseNBest goldBestParse = parses.get(goldBest);

                    double e = pipe.errors(is, n, predParse);

                    Integer ctb = remapped.get(best);
                    if (ctb == null) {
                        remapped.put(best, 1);
                    } else {
                        remapped.put(best, ctb + 1);
                    }

                    String info = " 1best-error " + ((length - firstBestTotalError) / length)
                            + " reranked " + ((length - totalError) / length)
                            + " chd  " + changes + "  " + " ps las " + (las / cnt) + " avs " + ((float) averageScore / (float) count) + "   ";



                    if ((n + 1) % 500 == 0) {
                        PipeGen.outValueErr(count, Math.round(error * 1000) / 1000, f1 / count, last, upd, info);
                    }

                    firstBestTotalError += Decoder.getError(firstBest, is, n, Decoder.LAS);

                    totalError += Decoder.getError(predParse, is, n, Decoder.LAS);


                    rerankTime += System.currentTimeMillis() - startReranking;

                    if (best != 0) {
                        changes++;
                    }

                    las += is.length(n) - Decoder.getError(goldBestParse, is, n, Decoder.LAS);
                    cnt += is.length(n);

                    averageScore += predParse.f1;


                    if (options.count < count) {
                        break;
                    }


                    if (Decoder.getError(goldBestParse, is, n, Decoder.LAS)
                            >= Decoder.getError(predParse, is, n, Decoder.LAS)) {
                        continue;
                    }


                    // get predicted feature vector
                    pipeReranker.extractor.extractFeatures(is, n, predParse, parses.indexOf(predParse), vs, pipe.cl);

                    pred.clear();
                    int rank = 1 * ExtractorReranker.s_type;

                    for (int j = 0; j < vs.length; j++) {
                        if (vs[j] == Integer.MIN_VALUE) {
                            break;
                        }
                        if (vs[j] > 0) {
                            pred.add(pipeReranker.extractor.li.l2i(vs[j] + rank));
                        }
                    }
                    pred.add(pipeReranker.extractor.li.l2i(1 + rank), (float) predParse.f1);
                    error += 1;

                    pipeReranker.extractor.extractFeatures(is, n, goldBestParse, parses.indexOf(goldBestParse), vs, pipe.cl);


                    act.clear();
                    rank = 1 * ExtractorReranker.s_type;
                    for (int j = 0; j < vs.length; j++) {
                        if (vs[j] == Integer.MIN_VALUE) {
                            break;
                        }
                        if (vs[j] > 0) {
                            act.add(pipeReranker.extractor.li.l2i(vs[j] + rank));
                        }
                    }

                    act.add(pipeReranker.extractor.li.l2i(1 + rank), (float) goldBestParse.f1);
                    float lam_dist = (float) (parametersReranker.getScore(act)
                            - (parametersReranker.getScore(pred)));



                    parametersReranker.update(act, pred, is, n, null, upd, e, lam_dist);

                }
                instance++;

            }

            String info = " td " + ((Decoder.timeDecotder) / 1000000F) + " tr " + ((Decoder.timeRearrange) / 1000000F)
                    + " te " + ((Pipe.timeExtract) / 1000000F) + " nz " + parametersReranker.countNZ()
                    + " 1best-error " + ((length - firstBestTotalError) / length)
                    + " reranked-best " + ((length - totalError) / length)
                    + " rds " + round + "   "
                    + " rerank-t " + (rerankTime / count)
                    + " chd " + changes + "   " + " ps las " + (las / cnt) + " avs " + ((float) averageScore / (float) count) + "   ";


            //	DB.println("remapped "+remapped);

            PipeGen.outValueErr(count, Math.round(error * 1000) / 1000, f1 / count, last, upd, info);
            long end = System.currentTimeMillis();
            is2.parser.Parser.out.println(" time:" + (end - start));
            i++;
            //	ParametersFloat pf = params.average2((iter+1)*is.size());



            Decoder.timeDecotder = 0;
            Decoder.timeRearrange = 0;
            Pipe.timeExtract = 0;

            if (dos != null) {
                dos.close();
            }
            if (dis != null) {
                dis.close();
            }

        }
        DB.println("sb " + parametersReranker.parameters[this.pipeReranker.extractor.li.l2i(4090378920L + 1 * ExtractorReranker.s_type)]);//4090378266
        parametersReranker.average(iter * numInstances);
    }

    /**
     * Do the parsing
     *
     * @param options
     * @param pipe
     * @param params
     * @throws IOException
     */
    private void rerankedParses(OptionsSuper options, Pipe pipe, ParametersFloat params, boolean maxInfo, String nbestName) throws Exception {

        long start = System.currentTimeMillis();

        ExtractorClusterStacked.initFeatures();

        DataInputStream dis = null;
        DataOutputStream dos = null;

        float olas = 0, olcnt = 0;

        File fnbest = new File(nbestName);
        int read = fnbest.exists() ? 2 : 1;
        if (read == 1) {

            DB.println("computing and writting nbest list to file: " + nbestName);

            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(nbestName)));
            zos.putNextEntry(new ZipEntry("data"));
            dos = new DataOutputStream(new BufferedOutputStream(zos));
        }



        if (read == 2) {

            //		DB.println("reading nbest list from file: "+nbestName);

            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(nbestName)));
            zis.getNextEntry();
            dis = new DataInputStream(new BufferedInputStream(zis));
        }

        for (int m = 0; m < this.paramsParsers.length; m++) {


            CONLLReader09 depReader = new CONLLReader09(options.testfile, options.formatTask);
            CONLLWriter09 depWriter = new CONLLWriter09(options.outfile, options.formatTask);

            float las = 0, lcnt = 0, averageScore = 0;
            int cnt = 0;
            int del = 0;


            long last = System.currentTimeMillis();


            String[] types = new String[pipe.mf.getFeatureCounter().get(PipeGen.REL)];
            for (Entry<String, Integer> e : MFB.getFeatureSet().get(PipeGen.REL).entrySet()) {
                types[e.getValue()] = e.getKey();
            }

            is2.parser.Parser.out.print("Processing Sentence: ");


            FVR f = new FVR();

            while (true) {

                Instances is = new Instances();
                is.init(1, new MFB(), options.formatTask);

                SentenceData09 instance = pipe.nextInstance(is, depReader);
                if (instance == null) {
                    break;
                }
                cnt++;

                String[] forms = instance.forms;

                List<ParseNBest> parses = null;

                // read or write nbest list
                if (read == 0 || read == 1) {
                    parses = this.parseNBest(instance, this.paramsParsers[m]);
                    //			data = pipe.fillVector(fparser, is, n, data, pipe.cl,THREADS);
                    //			parses = Decoder.decode(pos,  data, options.decodeProjective);

                    if (parses.size() > NBest) {
                        parses = parses.subList(0, NBest);
                    }


                    if (read == 1) {
                        // write the forest
                        dos.writeInt(parses.size());
                        for (int k = 0; k < parses.size(); k++) {
                            dos.writeUTF(parses.get(k).signature());
                            dos.writeFloat((float) parses.get(k).f1);
                        }
                    }
                } else if (read == 2) {
                    parses = new ArrayList<>();
                    int parseCount = dis.readInt();
                    for (int k = 0; k < parseCount; k++) {
                        ParseNBest p = new ParseNBest(dis.readUTF(), dis.readFloat());
                        if (parses.size() < NBest) {
                            parses.add(p);
                        }
                    }
                }

                nbest += parses.size();


                //List<ParseNBest> parses = this.parseNBest(instance, this.paramsParsers[m]);

                long vs[] = new long[ExtractorReranker._FC * is.length(0)];

                float bestScore = 0;
                int best = 0;


                for (int k = 0; k < parses.size(); k++) {

                    ParseNBest p = parses.get(k);

                    pipeReranker.extractor.extractFeatures(is, 0, p, k, vs, pipeReranker.cl);

                    int rank = 1 * ExtractorReranker.s_type;
                    f.clear();
                    for (int j = 0; j < vs.length; j++) {
                        if (vs[j] == Integer.MIN_VALUE) {
                            break;
                        }
                        if (vs[j] > 0) {
                            f.add(pipeReranker.extractor.li.l2i(vs[j] + rank));
                        }
                    }
                    f.add(pipeReranker.extractor.li.l2i(1 + rank), (float) p.f1);

                    float score = (float) (parametersReranker.getScore(f));
                    if (score > bestScore) { //rankScore[k][2]>
                        bestScore = score;
                        best = k;

                    }
                }
                // change to best
                ParseNBest d = parses.get(best);

                las += (is.length(0) - 1) - Decoder.getError(d, is, 0, Decoder.LAS);
                lcnt += is.length(0) - 1;

                averageScore += d.f1;

                SentenceData09 i09 = new SentenceData09(instance);

                i09.createSemantic(instance);

                for (int j = 0; j < forms.length - 1; j++) {
                    i09.plabels[j] = types[d.labels[j + 1]];
                    i09.pheads[j] = d.heads[j + 1];
                }


                depWriter.write(i09);
                String info = "" + ((float) (averageScore / (float) cnt)) + "   ";

                if (cnt % 10 == 0) {
                    PipeGen.outValueErr(cnt, lcnt - las, las / lcnt, last, 0, info);//outValue(cnt, del,last, info);
                }
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
                    + " Sum LAS " + ((float) this.smallestErrorSum / countAllNodes) + " "
                    + "" + (las / lcnt));

            //		DB.println("errors "+error);
            olas += las;
            olcnt += lcnt;
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

        if (dos != null) {
            dos.flush();
            dos.close();
        }
        if (dis != null) {
            dis.close();
        }

        DB.println("\n overall las " + (olas / olcnt));
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

        ExtractorClusterStacked.initFeatures();

        int cnt = 0;

        String[] types = new String[pipe.mf.getFeatureCounter().get(PipeGen.REL)];
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

            this.parseNBest(instance, this.paramsParsers[0]);
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

    public List<ParseNBest> parseNBest(SentenceData09 instance, ParametersFloat params) throws IOException {

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
            parse(snt09, this.parametersReranker);
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
        DB.println("Writting model: " + name);
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(name)));
        zos.putNextEntry(new ZipEntry("data"));
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(zos));

        MFB.writeData(dos);
        cs.write(dos);

        params.write(dos);

        Edges.write(dos);

        dos.writeBoolean(options.decodeProjective);

        dos.writeInt(ExtractorClusterStacked.maxForm);

        dos.writeInt(5);  // Info count
        dos.writeUTF("Used parser   " + Reranker.class.toString());
        dos.writeUTF("Creation date " + (new SimpleDateFormat("yyyy.MM.dd HH:mm:ss")).format(new Date()));
        dos.writeUTF("Training data " + options.trainfile);
        dos.writeUTF("Iterations    " + options.numIters + " Used sentences " + options.count);
        dos.writeUTF("Cluster       " + options.clusterFile);

        dos.flush();
        dos.close();
    }
}