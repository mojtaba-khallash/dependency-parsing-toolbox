package is2.tag;

import is2.data.*;
import is2.io.CONLLReader09;
import is2.parser.Parser;
import is2.tools.IPipe;
import is2.util.OptionsSuper;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

final public class ExtractorT2 extends PipeGen implements IPipe {

    final static int _MAX = 71;
    private static final String STWRD = "STWRD", STPOS = "STPOS";
    private static short s_pos, s_word, s_char;
    protected static short s_type;
    private static int _strp, _ewrd;
    static int _CEND;
    public String[] types;
    final public MFO mf;
    final MFO.Data4 d1 = new MFO.Data4(), d2 = new MFO.Data4(), d3 = new MFO.Data4(),
            dw = new MFO.Data4(), dwp = new MFO.Data4();
    Cluster cl;
    private OptionsSuper options;

    public ExtractorT2(OptionsSuper options, MFO mf) throws IOException {
        this.mf = mf;
        this.options = options;
    }
    public HashMap<Integer, int[]> _pps = new HashMap<>();
    private Lexicon lx;
    public int corpusWrds = 0;

    /*
     * (non-Javadoc) @see is2.tag5.IPipe#createInstances(java.lang.String,
     * java.io.File, is2.data.InstancesTagger)
     */
    @Override
    public Instances createInstances(String file) throws Exception {
        return createInstances(file, -1, -1);
    }

    public Instances createInstances(String file, int skipStart, int skipEnd) throws Exception {

        InstancesTagger is = new InstancesTagger();

        CONLLReader09 depReader = new CONLLReader09(CONLLReader09.NO_NORMALIZE);

        depReader.startReading(file);
        mf.register(POS, "<root-POS>");
        mf.register(WORD, "<root>");

        Parser.out.println("Registering feature parts ");

        HashMap<Integer, HashSet<Integer>> pps = new HashMap<>();

        int ic = 0;
        while (true) {

            SentenceData09 instance1 = depReader.getNext();

            if (instance1 == null) {
                break;
            }
            ic++;

            String[] w = instance1.forms;
            for (int i1 = 0; i1 < w.length; i1++) {
                mf.register(WORD, w[i1]);
            }
            for (int i1 = 0; i1 < w.length; i1++) {
                registerChars(CHAR, w[i1]);
            }
            for (int i1 = 0; i1 < w.length; i1++) {
                registerChars(CHAR, w[i1].toLowerCase());
            }


            w = instance1.plemmas;
            for (int i1 = 0; i1 < w.length; i1++) {
                mf.register(WORD, w[i1]);
            }
            for (int i1 = 0; i1 < w.length; i1++) {
                registerChars(CHAR, w[i1]);
            }

            w = instance1.gpos;
            for (int i1 = 0; i1 < w.length; i1++) {
                mf.register(POS, w[i1]);
            }
            for (int i1 = 0; i1 < w.length; i1++) {
                HashSet<Integer> ps = pps.get(mf.getValue(POS, w[i1]));
                if (ps == null) {
                    ps = new HashSet<>();
                    pps.put(mf.getValue(POS, w[i1]), ps);
                }
                if (i1 + 1 < w.length) {
                    ps.add(mf.getValue(POS, w[i1 + 1]));
                }
            }

        }

        for (Entry<Integer, HashSet<Integer>> e : pps.entrySet()) {
            int[] ps = new int[e.getValue().size()];
            int j = 0;
            for (int k : e.getValue().toArray(new Integer[0])) {
                ps[j++] = k;
            }
            _pps.put(e.getKey(), ps);
            //	Parser.out.println("put "+e.getKey()+" "+ps.length+" pps size "+_pps.size());
        }

        Parser.out.println("words in corpus " + (corpusWrds = mf.getFeatureCounter().get(ExtractorT2.WORD)));
        if (options.clusterFile == null) {
            cl = new Cluster();
        } else {
            cl = new Cluster(options.clusterFile, mf, 6);
        }

        if (options.lexicon == null) {
            lx = new Lexicon(new byte[0][0]);
        } else {
            lx = new Lexicon(options.lexicon, mf);
        }

        initFeatures();

        mf.calculateBits();
        initValues();

        Parser.out.println("" + mf.toString());

        depReader.startReading(file);

        int num1 = 0;

        int instanceCount = 0;

        Parser.out.print("Creating Instances: ");

        is.init(ic, mf);
        int del = 0;

        while (true) {
            if (num1 % 100 == 0) {
                del = outValue(num1, del);
            }

            if (num1 >= skipStart && num1 < skipEnd && skipStart >= 0) {
                SentenceData09 instance1 = depReader.getNext();
                if (instance1 == null) {
                    break;
                }
                num1++;
                continue;
            }


            SentenceData09 instance1 = depReader.getNext(is);
            if (instance1 == null) {
                break;
            }

            is.fillChars(instance1, instanceCount, _CEND);
            for (int k = 0; k < instance1.length(); k++) {
                if (instance1.ppos[k].contains("\\|")) {
                    is.pposs[num1][k] = (short) mf.getValue(FM, instance1.ppos[k].split("\\|")[1]);
                }
            }


            if (num1 > options.count) {
                break;
            }

            num1++;
            instanceCount++;
        }
        outValue(num1, del);
        Parser.out.println();

        types = is2.tag.MFO.reverse(mf.getFeatureSet().get(POS));
        return is;
    }

    private void registerChars(String type, String word) {
        for (int i = 0; i < word.length(); i++) {
            mf.register(type, Character.toString(word.charAt(i)));
        }
    }


    /*
     * (non-Javadoc) @see is2.tag5.IPipe#initValues()
     */
    @Override
    public void initValues() {
        s_pos = mf.getFeatureBits(POS);
        s_word = mf.getFeatureBits(WORD);
        s_type = mf.getFeatureBits(TYPE);
        s_char = mf.getFeatureBits(CHAR);

        d1.a0 = s_type;
        d1.a1 = s_pos;
        d1.a2 = s_word;
        d1.a3 = s_word;
        d2.a0 = s_type;
        d2.a1 = s_pos;
        d2.a2 = s_pos;
        d2.a3 = s_pos;
        d2.a4 = s_pos;
        d2.a5 = s_pos;
        d2.a6 = s_pos;
        d3.a0 = s_type;
        d3.a1 = s_pos;
        d3.a2 = s_char;
        d3.a3 = s_char;
        d3.a4 = s_char;
        d3.a5 = s_char;
        d3.a6 = s_char;
        d3.a7 = s_char;
        dw.a0 = s_type;
        dw.a1 = s_pos;
        dw.a2 = s_word;
        dw.a3 = s_word;
        dw.a4 = s_word;
        dw.a5 = s_word;
        dw.a6 = s_word;
        dw.a7 = s_word;
        dwp.a0 = s_type;
        dwp.a1 = s_pos;
        dwp.a2 = s_word;
        dwp.a3 = s_pos;
        dwp.a4 = s_word;
    }

    /*
     * (non-Javadoc) @see is2.tag5.IPipe#initFeatures()
     */
    @Override
    public void initFeatures() {
        // 62
        for (int t = 0; t < 67; t++) {
            mf.register(TYPE, "F" + t);
        }

        mf.register(POS, MID);
        _strp = mf.register(POS, STR);
        mf.register(POS, END);

        mf.register(WORD, STR);
        _ewrd = mf.register(WORD, END);

        _CEND = mf.register(CHAR, END);

        mf.register(WORD, STWRD);
        mf.register(POS, STPOS);


    }

    final public void addFeatures(InstancesTagger is, int ic, String fs, int i, short pposs[], int[] forms, int[] lemmas, long[] vs) {

        int c0 = is.chars[ic][i][0], c1 = is.chars[ic][i][1], c2 = is.chars[ic][i][2], c3 = is.chars[ic][i][3], c4 = is.chars[ic][i][4], c5 = is.chars[ic][i][5];
        int e0 = is.chars[ic][i][6], e1 = is.chars[ic][i][7], e2 = is.chars[ic][i][8], e3 = is.chars[ic][i][9], e4 = is.chars[ic][i][10];

        int f = 1, n = 0;
        short upper = 0, number = 1;
        for (int k1 = 0; k1 < fs.length(); k1++) {
            char c = fs.charAt(k1);
            if (Character.isUpperCase(c)) {
                if (k1 == 0) {
                    upper = 1;
                } else {
                    // first char + another
                    if (upper == 1) {
                        upper = 3;
                    } // another uppercase in the word
                    else if (upper == 0) {
                        upper = 2;
                    }
                }
            }

            // first 
            if (Character.isDigit(c) && k1 == 0) {
                number = 2;
            } else if (Character.isDigit(c) && number == 1) {
                number = 3;
            }
            //	if(number==2 &&Character.isDigit(c)) number=4;
            //	if(number==4 && !Character.isDigit(c)) number=5;
        }

        //	if (i==0 && upper>0) upper+=4; 
        int form = forms[i], form2 = forms[i] < corpusWrds ? forms[i] : -1;

        int len = forms.length;
        long l;
        d1.v0 = f++;
        d1.v2 = form2;
        l = mf.calc3(d1);
        vs[n++] = mf.calc3(d1);

        d1.v0 = f++;
        d1.v2 = is.formlc[ic][i];
        vs[n++] = mf.calc3(d1);


        d3.v2 = c0;
        d3.v3 = c1;
        d3.v4 = c2;
        d3.v5 = c3;
        d3.v6 = c4;
        d3.v0 = f++;
        vs[n++] = mf.calc3(d3);
        d3.v0 = f++;
        vs[n++] = mf.calc4(d3);
        d3.v0 = f++;
        vs[n++] = mf.calc5(d3);
        d3.v0 = f++;
        vs[n++] = mf.calc6(d3);
        d3.v0 = f++;
        vs[n++] = mf.calc7(d3);

        if (form != -1) {
            d3.v2 = c2;
            d3.v3 = c3;
            d3.v4 = c4;
            d3.v5 = c5;
            d3.v6 = cl.getLP(form);
            d3.v0 = f;
            vs[n++] = mf.calc6(d3);
            d3.v0 = f + 1;
            vs[n++] = mf.calc7(d3);
        }
        f += 2;

        if (form > 0) {
            d3.v0 = f;
            d3.v5 = cl.getLP(form);
            vs[n++] = mf.calc6(d3);
            d3.v0 = f + 1;
            d3.v4 = cl.getLP(form);
            vs[n++] = mf.calc5(d3);
            d3.v0 = f + 2;
            d3.v3 = cl.getLP(form);
            vs[n++] = mf.calc4(d3);
        }
        f += 5;

        d3.v2 = e0;
        d3.v3 = e1;
        d3.v4 = e2;
        d3.v5 = e3;
        d3.v6 = e4;
        d3.v0 = f++;
        vs[n++] = mf.calc3(d3);
        d3.v0 = f++;
        vs[n++] = l = mf.calc4(d3);
        vs[n++] = d3.calcs(3, upper, l);
        d3.v0 = f++;
        vs[n++] = l = mf.calc5(d3);
        vs[n++] = d3.calcs(3, upper, l);
        d3.v0 = f++;
        vs[n++] = l = mf.calc6(d3);
        vs[n++] = d3.calcs(3, upper, l);
        d3.v0 = f++;
        vs[n++] = l = mf.calc7(d3);
        vs[n++] = d3.calcs(3, upper, l);

        if (form > 0) {
            d3.v0 = f;
            d3.v5 = cl.getLP(form);
            vs[n++] = mf.calc6(d3);
            d3.v0 = f + 1;
            d3.v4 = cl.getLP(form);
            vs[n++] = mf.calc5(d3);
            d3.v0 = f + 2;
            d3.v3 = cl.getLP(form);
            vs[n++] = mf.calc4(d3);

            d3.v2 = e0;
            d3.v3 = e1;
            d3.v4 = e2;

            d3.v0 = f + 3;
            d3.v2 = lx.getTag(form);
            vs[n++] = mf.calc3(d3);
            d3.v0 = f + 4;
            d3.v4 = cl.getLP(form);
            vs[n++] = mf.calc5(d3);
            d3.v0 = f + 5;
            d3.v3 = cl.getLP(form);
            vs[n++] = mf.calc4(d3);
        }
        f += 6;

        // sign three-grams
        d3.v0 = f++;
        d3.v2 = c1;
        d3.v3 = c2;
        d3.v4 = c3;
        vs[n++] = mf.calc5(d3);
        d3.v0 = f++;
        d3.v2 = c2;
        d3.v3 = c3;
        d3.v4 = c4;
        vs[n++] = mf.calc5(d3);
        d3.v0 = f++;
        d3.v2 = c3;
        d3.v3 = c4;
        d3.v4 = c5;
        vs[n++] = mf.calc5(d3);

        // sign quad-grams
        d3.v0 = f++;
        d3.v2 = c1;
        d3.v3 = c2;
        d3.v4 = c3;
        d3.v5 = c4;
        vs[n++] = mf.calc6(d3);
        d3.v0 = f++;
        d3.v2 = c2;
        d3.v3 = c3;
        d3.v4 = c4;
        d3.v5 = c5;
        vs[n++] = mf.calc6(d3); // changed to 6

        if (i + 1 < len && forms[i + 1] < this.corpusWrds) {
            dw.v0 = f;
            dw.v2 = forms[i + 1];
            dw.v3 = form2;
            vs[n++] = mf.calc4(dw);
        }
        f++;

        if (len > i + 1) {

            if (forms[i + 1] < corpusWrds) {
                dw.v0 = f;
                dw.v2 = forms[i + 1];
                vs[n++] = mf.calc3(dw);
            }

            d3.v0 = f + 1;
            d3.v2 = is.chars[ic][i + 1][0];
            vs[n++] = mf.calc3(d3);
            d3.v0 = f + 2;
            d3.v2 = is.chars[ic][i + 1][6];
            vs[n++] = mf.calc3(d3);

            d3.v2 = e0;
            d3.v3 = e1;

            d3.v0 = f + 3;
            d3.v4 = is.chars[ic][i + 1][0];
            vs[n++] = mf.calc5(d3);
            d3.v0 = f + 4;
            d3.v4 = is.chars[ic][i + 1][6];
            vs[n++] = mf.calc5(d3);

            if (is.chars[ic][i + 1][11] > 1) { //  instance.forms[i+1].length()

                d3.v0 = f + 5;
                d3.v2 = is.chars[ic][i + 1][0];
                d3.v3 = is.chars[ic][i + 1][1];
                vs[n++] = mf.calc4(d3);
                d3.v0 = f + 6;
                d3.v2 = is.chars[ic][i + 1][6];
                d3.v3 = is.chars[ic][i + 1][7];
                vs[n++] = mf.calc4(d3);

                d3.v2 = e0;
                d3.v3 = e1;

                d3.v0 = f + 7;
                d3.v4 = is.chars[ic][i + 1][0];
                d3.v5 = is.chars[ic][i + 1][1];
                vs[n++] = mf.calc6(d3);
                d3.v0 = f + 8;
                d3.v4 = is.chars[ic][i + 1][6];
                d3.v5 = is.chars[ic][i + 1][7];
                vs[n++] = mf.calc6(d3);

                if (forms[i + 1] > 0) {
                    d3.v0 = f + 9;
                    d3.v2 = is.chars[ic][i + 1][0];
                    d3.v3 = is.chars[ic][i + 1][1];
                    d3.v4 = cl.getLP(forms[i + 1]);
                    vs[n++] = mf.calc5(d3);
                    d3.v0 = f + 10;
                    d3.v2 = is.chars[ic][i + 1][6];
                    d3.v3 = is.chars[ic][i + 1][7];
                    d3.v4 = cl.getLP(forms[i + 1]);
                    vs[n++] = mf.calc5(d3);
                }
            }

            if (forms[i + 1] > 0) {


                dw.v0 = f + 11;
                dw.v2 = cl.getLP(forms[i + 1]);
                dw.v3 = form2;
                vs[n++] = mf.calc4(dw);

                //		if (forms[i]>0){
                //			dw.v0=f+12; dw.v2= cl.getLP(forms[i+1]); dw.v3=lx.getTag(form);vs[n++]=mf.calc4(dw);
                //			dw.v0=f+13; dw.v2= cl.getLP(forms[i]);   dw.v3=lx.getTag(forms[i+1]);vs[n++]=mf.calc4(dw);
                //		}
            }


            if (len > i + 2) {
                if (forms[i + 2] < corpusWrds && forms[i + 1] < corpusWrds) {
                    dw.v0 = f + 12;
                    dw.v2 = forms[i + 2];
                    dw.v3 = forms[i + 1];
                    vs[n++] = mf.calc4(dw);
                    vs[n++] = mf.calc3(dw);
                }
                d2.v0 = f + 13;
                d2.v2 = pposs[i + 1];
                d2.v3 = pposs[i + 2];
                vs[n++] = mf.calc4(d2);
            }

            if (len > i + 3) {
                if (forms[i + 3] < this.corpusWrds && forms[i + 2] < this.corpusWrds) {
                    dw.v0 = f + 14;
                    dw.v2 = forms[i + 3];
                    dw.v3 = forms[i + 2];
                    vs[n++] = mf.calc4(dw);
                    vs[n++] = mf.calc3(dw);
                }
            }
        }
        f += 15;

        // length
        d2.v0 = f++;
        d2.v2 = is.chars[ic][i][11];
        vs[n++] = mf.calc3(d2);


        // contains a number
        d2.v0 = f++;
        d2.v2 = number;
        vs[n++] = mf.calc3(d2);
        if (lemmas[i] < corpusWrds) {
            d1.v0 = f;
            d1.v2 = lemmas[i];
            vs[n++] = mf.calc3(d1);
        }
        f++;

        if (i != 0 && len > i + 1) {

            if (lemmas[i - 1] < corpusWrds && lemmas[i + 1] < corpusWrds) {
                dw.v0 = f;
                dw.v2 = lemmas[i - 1];
                dw.v3 = lemmas[i + 1];
                vs[n++] = mf.calc4(dw);
            }

            d2.v0 = f + 1;
            d2.v2 = pposs[i - 1];
            d2.v3 = pposs[i + 1];
            vs[n++] = mf.calc4(d2);
        }
        f += 2;

        d2.v0 = f++;
        d2.v2 = i >= 1 ? pposs[i - 1] : _strp;
        vs[n++] = mf.calc3(d2);

        if (i > 0) {

            dw.v0 = f;
            dw.v2 = i >= 1 ? forms[i - 1] < corpusWrds ? forms[i - 1] : -1 : _strp;
            vs[n++] = mf.calc3(dw);
            f++;

            if (lemmas[i - 1] < corpusWrds) {
                dw.v0 = f;
                dw.v2 = i >= 1 ? lemmas[i - 1] : _strp;
                vs[n++] = mf.calc3(dw);
            }
            f++;

            //if (len>i+1) {d2.v0=f;	d2.v2= pposs[i-1];d2.v3= pposs[i+1]; vs[n++]=mf.calc4(d2);}
            //f++;

            if (i > 1) {

                d2.v0 = f++;
                d2.v2 = i < 2 ? _strp : pposs[i - 2];
                vs[n++] = mf.calc3(d2);
                d2.v0 = f++;
                d2.v2 = pposs[i - 1];
                d2.v3 = pposs[i - 2];
                vs[n++] = mf.calc4(d2);

                if (forms[i - 2] < corpusWrds) {
                    dw.v0 = f;
                    dw.v2 = forms[i - 2];
                    vs[n++] = mf.calc3(dw);
                }
                f++;
                if (forms[i - 1] < corpusWrds) {
                    dwp.v0 = f;
                    dwp.v2 = forms[i - 1];
                    dwp.v3 = pposs[i - 2];
                    vs[n++] = mf.calc4(dwp);
                }
                f++;
                if (forms[i - 2] < corpusWrds) {
                    dwp.v0 = f;
                    dwp.v2 = forms[i - 2];
                    dwp.v3 = pposs[i - 1];
                    vs[n++] = mf.calc4(dwp);
                }
                f++;

                if (i > 2) {
                    d2.v0 = f++;
                    d2.v2 = pposs[i - 3];
                    vs[n++] = mf.calc3(d2);
                    d2.v0 = f++;
                    d2.v2 = pposs[i - 2];
                    d2.v3 = pposs[i - 3];
                    vs[n++] = mf.calc4(d2);
                    if (forms[i - 3] < this.corpusWrds && forms[i - 2] < this.corpusWrds) {
                        dw.v0 = f;
                        dw.v2 = forms[i - 3];
                        dw.v3 = forms[i - 2];
                        vs[n++] = mf.calc4(dw);
                    }
                    f++;
                }
            }
        }
        vs[n] = Integer.MIN_VALUE;
    }

    public int fillFeatureVectorsOne(String fs, ParametersFloat params, int w1, InstancesTagger is, int n, short[] pos, Long2IntInterface li, float[] score) {

        float best = -1000;
        int bestType = -1;

        F2SF f = new F2SF(params.parameters);

        long vs[] = new long[_MAX];
        int lemmas[];
        if (options.noLemmas) {
            lemmas = new int[is.length(n)];
        } else {
            lemmas = is.plemmas[n];
        }
        addFeatures(is, n, fs, w1, pos, is.forms[n], lemmas, vs);

        //for(int t = 0; t < types.length; t++) {

        for (int t = 0; t < types.length; t++) {

            int p = t << s_type;

            f.clear();
            for (int k = 0; vs[k] != Integer.MIN_VALUE; k++) {
                if (vs[k] > 0) {
                    f.add(li.l2i(vs[k] + p));
                }
            }
            if (f.score > best) {
                bestType = t;
                score[w1] = best = f.score;
            }
        }
        return bestType;
    }

    public ArrayList<POS> classify(String fs, ParametersFloat params, int w1, InstancesTagger is, int n, short[] pos, Long2IntInterface li) {


        F2SF f = new F2SF(params.parameters);

        long vs[] = new long[_MAX];
        int lemmas[];
        if (options.noLemmas) {
            lemmas = new int[is.length(n)];
        } else {
            lemmas = is.plemmas[n];
        }
        addFeatures(is, n, fs, w1, pos, is.forms[n], lemmas, vs);

        ArrayList<POS> best = new ArrayList<>(types.length);

        for (int t = 0; t < types.length; t++) {

            int p = t << s_type;

            f.clear();
            f.add(vs, li, p);
            POS px = new POS(t, f.score);
            best.add(px);
        }
        Collections.sort(best);
        return best;
    }

    /*
     * (non-Javadoc) @see is2.tag5.IPipe#write(java.io.DataOutputStream)
     */
    @Override
    public void write(DataOutputStream dos) {
        try {
            this.cl.write(dos);
            this.lx.write(dos);
            dos.writeInt(this.corpusWrds);
            dos.writeInt(_pps.size());

            for (Entry<Integer, int[]> e : _pps.entrySet()) {
                dos.writeInt(e.getValue().length);
                for (int k : e.getValue()) {
                    dos.writeInt(k);
                }
                dos.writeInt(e.getKey());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void read(DataInputStream dis) {
        try {
            this.cl = new Cluster(dis);
            this.lx = new Lexicon(dis);
            this.corpusWrds = dis.readInt();

            int pc = dis.readInt();
            for (int j = 0; j < pc; j++) {
                int ps[] = new int[dis.readInt()];
                for (int k = 0; k < ps.length; k++) {
                    ps[k] = dis.readInt();
                }
                _pps.put(dis.readInt(), ps);
            }
            //	Parser.out.println("_pps "+ps.length);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}