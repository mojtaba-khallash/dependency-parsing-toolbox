package is2.mtag;

import is2.data.*;
import is2.io.CONLLReader09;
import is2.parser.Parser;
import is2.tools.IPipe;
import is2.util.OptionsSuper;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

final public class Pipe extends PipeGen implements IPipe {

    public static int _CEND;
    private static final String STWRD = "STWRD", STPOS = "STPOS", END = "END", STR = "STR";
    public String[] types;
    Cluster cl;
    final public MFO mf = new MFO();
    public Long2IntInterface li;
    final MFO.Data4 d1 = new MFO.Data4(), d2 = new MFO.Data4(), d3 = new MFO.Data4(), dw = new MFO.Data4();
    final MFO.Data4 dwp = new MFO.Data4(), dp = new MFO.Data4();
    private OptionsSuper options;
    private int _ewrd;
    static private int _mid, _strp, _endp;

    public Pipe(Options options, Long2Int long2Int) throws IOException {
        this.options = options;

        li = long2Int;
    }

    public Pipe(OptionsSuper options) {
        this.options = options;
    }
    public HashMap<Integer, Integer> form2morph = new HashMap<>();

    @Override
    public Instances createInstances(String file) throws Exception {

        CONLLReader09 depReader = new CONLLReader09(CONLLReader09.NO_NORMALIZE);

        depReader.startReading(file);
        mf.register(POS, "<root-POS>");

        mf.register(FEAT, CONLLReader09.NO_TYPE);
        mf.register(FEAT, "");

        InstancesTagger is = new InstancesTagger();

        Parser.out.println("Registering feature parts ");

        HashMap<String, HashSet<String>> op2form = new HashMap<>();
        HashMap<String, Integer> freq = new HashMap<>();


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
                mf.register(WORD, w[i1].toLowerCase());
                Integer f = freq.get(w[i1].toLowerCase());
                if (f == null) {
                    freq.put(w[i1].toLowerCase(), 1);
                } else {
                    freq.put(w[i1].toLowerCase(), f + 1);
                }

                HashSet<String> forms = op2form.get(w[i1].toLowerCase());
                if (forms == null) {
                    forms = new HashSet<>();
                    op2form.put(w[i1].toLowerCase(), forms);
                }
                forms.add(instance1.ofeats[i1] == null ? "_" : instance1.ofeats[i1]);
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

            w = instance1.ppos;
            for (int i1 = 0; i1 < w.length; i1++) {
                mf.register(POS, w[i1]);
            }

            w = instance1.gpos;
            for (int i1 = 0; i1 < w.length; i1++) {
                mf.register(POS, w[i1]);
            }

            w = instance1.ofeats;
            for (int i1 = 0; i1 < w.length; i1++) {
                if (w[i1] != null) {
                    mf.register(FEAT, w[i1]);
                }
            }

            //	w = instance1.pfeats;
            //for(int i1 = 0; i1 < w.length; i1++) if (w[i1]!=null) mf.register(FEAT,  w[i1]);
        }


        for (Entry<String, HashSet<String>> e : op2form.entrySet()) {
            if (e.getValue().size() == 1 && freq.get(e.getKey()) > 10) {
                //	Parser.out.println("found map "+e.getKey()+" "+e.getValue()+" "+freq.get(e.getKey()));
                form2morph.put(mf.getValue(Pipe.WORD, e.getKey()), mf.getValue(FEAT, (String) e.getValue().toArray()[0]));
            }
        }

        initFeatures();

        mf.calculateBits();
        initValues();

        Parser.out.println("" + mf.toString());

        depReader.startReading(file);

        int num1 = 0;
        long start1 = System.currentTimeMillis();

        Parser.out.print("Creating Features: ");
        is.init(ic, mf);
        int del = 0;

        while (true) {
            if (num1 % 100 == 0) {
                del = outValue(num1, del);
            }
            SentenceData09 instance1 = depReader.getNext(is);
            if (instance1 == null) {
                break;
            }

            if (num1 > options.count) {
                break;
            }

            num1++;
        }
        long end1 = System.currentTimeMillis();
        System.gc();
        long mem2 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        Parser.out.print("  time " + (end1 - start1) + " mem " + (mem2 / 1024) + " kb");

        types = new String[mf.getFeatureCounter().get(FEAT)];

        for (Entry<String, Integer> e : is2.mtag.MFO.getFeatureSet().get(FEAT).entrySet()) {
            types[e.getValue()] = e.getKey();
        }


        if (options.clusterFile == null) {
            cl = new Cluster();
        } else {
            cl = new Cluster(options.clusterFile, mf, 6);
        }


        Parser.out.println("Num Features: " + types.length);


        depReader.startReading(file);



        int num11 = 0;

        while (true) {

            SentenceData09 instance = depReader.getNext();
            if (instance == null) {
                break;
            }

            is.fillChars(instance, num11, _CEND);


            if (num11 > options.count) {
                break;
            }

            num11++;
        }

        return is;//.toNativeArray();
    }

    private void registerChars(String type, String word) {
        for (int i = 0; i < word.length(); i++) {
            mf.register(type, Character.toString(word.charAt(i)));
        }
    }

    @Override
    public void initValues() {
        s_feat = is2.mtag.MFO.getFeatureBits(FEAT);
        s_word = is2.mtag.MFO.getFeatureBits(WORD);
        s_type = is2.mtag.MFO.getFeatureBits(TYPE);
        s_char = is2.mtag.MFO.getFeatureBits(CHAR);
        s_pos = is2.mtag.MFO.getFeatureBits(POS);
        //	dl1.a[0] = s_type; dl1.a[1] = s_pos;
        //	for (int k = 2; k < 7; k++) dl1.a[k] = s_pos;

        d1.a0 = s_type;
        d1.a1 = s_feat;
        d1.a2 = s_word;
        d2.a0 = s_type;
        d2.a1 = s_feat;
        d2.a2 = s_feat;
        d2.a3 = s_feat;
        d2.a4 = s_feat;
        d2.a5 = s_feat;
        d2.a6 = s_feat;
        d3.a0 = s_type;
        d3.a1 = s_feat;
        d3.a2 = s_char;
        d3.a3 = s_char;
        d3.a4 = s_char;
        d3.a5 = s_char;
        d3.a6 = s_char;
        d3.a7 = s_char;
        dp.a0 = s_type;
        dp.a1 = s_feat;
        dp.a2 = s_pos;
        dp.a3 = s_pos;
        dp.a4 = s_feat;// dp.a5=  s_char; dp.a6=  s_char; dp.a7= s_char;
        dw.a0 = s_type;
        dw.a1 = s_feat;
        dw.a2 = s_word;
        dw.a3 = s_word;
        dw.a4 = s_word;
        dw.a5 = s_word;
        dw.a6 = s_word;
        dw.a7 = s_word;
        dwp.a0 = s_type;
        dwp.a1 = s_feat;
        dwp.a2 = s_word;
        dwp.a3 = s_feat;
        dwp.a4 = s_word;

    }
    public static short s_feat, s_word, s_type, s_dir, s_dist, s_char, s_pos;

    /**
     * Initialize the features types.
     */
    @Override
    public void initFeatures() {

        for (int t = 0; t < 62; t++) {
            mf.register(TYPE, "F" + t);
        }


//		_mid = mf.register(POS, MID);
        _strp = mf.register(POS, STR);
        _endp = mf.register(POS, END);

        mf.register(WORD, STR);
        _ewrd = mf.register(WORD, END);


        _CEND = mf.register(CHAR, END);




        // optional features
        mf.register(WORD, STWRD);
        mf.register(POS, STPOS);
    }

    final public void addCF(InstancesTagger is, int ic, String fs, int i, int pfeat[], short ppos[], int[] forms, int[] lemmas, long[] vs) {

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

            if (Character.isDigit(c) && k1 == 0) {
                number = 2;
            } else if (Character.isDigit(c) && number == 1) {
                number = 3;
            }
        }

        int form = forms[i];

        int len = forms.length;
        long l;
        d1.v0 = f++;
        d1.v2 = form;
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
        f += 3;

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
        }
        f += 3;


        dw.v0 = f++;
        dw.v2 = i + 1 < len ? forms[i + 1] : _ewrd;
        dw.v3 = forms[i];
        vs[n++] = mf.calc4(dw);

        if (len > i + 1) {

            dw.v0 = f;
            dw.v2 = forms[i + 1];
            vs[n++] = mf.calc3(dw);
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
                dw.v3 = forms[i];
                vs[n++] = mf.calc4(dw);
            }

            if (len > i + 2) {
                dw.v0 = f + 12;
                dw.v2 = forms[i + 2];
                dw.v3 = forms[i + 1];
                vs[n++] = mf.calc4(dw);
                vs[n++] = mf.calc3(dw);
//				d2.v0=f+13; d2.v2=pfeat[i+1]; d2.v3=  pfeat[i+2];	vs[n++]=mf.calc4(d2);
                //	dp.v0= f+14; dp.v2=ppos[i+1]; dp.v3=ppos[i+2]; vs[n++]=mf.calc4(dp);

            }

            if (len > i + 3) {
                dw.v0 = f + 14;
                dw.v2 = forms[i + 3];
                dw.v3 = forms[i + 2];
                vs[n++] = mf.calc4(dw);
                vs[n++] = mf.calc3(dw);

            }
        }
        f += 16;

        // length
        d2.v0 = f++;
        d2.v2 = is.chars[ic][i][11];
        vs[n++] = mf.calc3(d2);


        // contains a number
        d2.v0 = f++;
        d2.v2 = number;
        vs[n++] = mf.calc3(d2);
        d1.v0 = f++;
        d1.v2 = lemmas[i];
        vs[n++] = mf.calc3(d1);

        if (i != 0 && len > i + 1) {
            dw.v0 = f;
            dw.v2 = lemmas[i - 1];
            dw.v3 = lemmas[i + 1];
            vs[n++] = mf.calc4(dw);
            d2.v0 = f + 1;
            d2.v2 = pfeat[i - 1];
            d2.v3 = pfeat[i + 1];
            vs[n++] = mf.calc4(d2);
        }
        f += 2;

        d2.v0 = f++;
        d2.v2 = i >= 1 ? pfeat[i - 1] : _strp;
        vs[n++] = mf.calc3(d2);
        dp.v0 = f++;
        dp.v2 = ppos[i];
        vs[n++] = mf.calc3(dp);

        if (i > 0) {
            dw.v0 = f++;
            dw.v2 = i >= 1 ? forms[i - 1] : _strp;
            vs[n++] = mf.calc3(dw);
            dw.v0 = f++;
            dw.v2 = i >= 1 ? lemmas[i - 1] : _strp;
            vs[n++] = mf.calc3(dw);

            if (len > i + 1) {
//				d2.v0=f;	d2.v2= pfeat[i-1];d2.v3= pfeat[i+1]; vs[n++]=mf.calc4(d2);
                //		dp.v0= f+1; dp.v2=ppos[i-1]; dp.v3=ppos[i+1]; vs[n++]=mf.calc4(dp);
            }
            f++;
            dp.v0 = f++;
            dp.v2 = ppos[i];
            dp.v3 = ppos[i - 1];
            vs[n++] = mf.calc4(dp);

            if (i > 1) {
                d2.v0 = f++;
                d2.v2 = i < 2 ? _strp : pfeat[i - 2];
                vs[n++] = mf.calc3(d2);
                d2.v0 = f++;
                d2.v2 = pfeat[i - 1];
                d2.v3 = pfeat[i - 2];
                vs[n++] = mf.calc4(d2);

                dw.v0 = f++;
                dw.v2 = forms[i - 2];
                vs[n++] = mf.calc3(dw);
                dwp.v0 = f++;
                dwp.v2 = forms[i - 1];
                dwp.v3 = pfeat[i - 2];
                vs[n++] = mf.calc4(dwp);
                dwp.v0 = f++;
                dwp.v2 = forms[i - 2];
                dwp.v3 = pfeat[i - 1];
                vs[n++] = mf.calc4(dwp);

                if (i > 2) {
                    d2.v0 = f++;
                    d2.v2 = pfeat[i - 3];
                    vs[n++] = mf.calc3(d2);
                    d2.v0 = f++;
                    d2.v2 = pfeat[i - 2];
                    d2.v3 = pfeat[i - 3];
                    vs[n++] = mf.calc4(d2);
                    dw.v0 = f++;
                    dw.v2 = forms[i - 3];
                    dw.v3 = forms[i - 2];
                    vs[n++] = mf.calc4(dw);
                    //			dp.v0= f++; dp.v2=ppos[i-3]; dp.v3=ppos[i-2]; vs[n++]=mf.calc4(dp);
                }
            }
        }
        vs[n] = Integer.MIN_VALUE;
    }

    public int fillFeatureVectorsOne(ParametersFloat params, int w1, String form, Instances is, int n, int[] features, long[] vs) {
        double best = -1;
        int bestType = -1;

        F2SF f = new F2SF(params.parameters);
        //is.gfeats[n]
        addCF((InstancesTagger) is, n, form, w1, features, is.pposs[n], is.forms[n], is.plemmas[n], vs);

        for (int t = 0; t < types.length; t++) {

            f.clear();
            int p = t << Pipe.s_type;
            for (int k = vs.length - 1; k >= 0; k--) {
                if (vs[k] >= 0) {
                    f.add(li.l2i(vs[k] + p));
                }
            }
            if (f.score > best) {
                bestType = t;
                best = f.score;
            }
        }
        return bestType;
    }
    //static ArrayList<T> todo = new ArrayList<T>();
    static SentenceData09 instance;
    public static int _FC = 200;

    /**
     * Write the lemma that are not mapped by operations
     *
     * @param dos
     */
    public void writeMap(DataOutputStream dos) {

        try {
            dos.writeInt(this.form2morph.size());
            for (Entry<Integer, Integer> e : form2morph.entrySet()) {
                dos.writeInt(e.getKey());
                dos.writeInt(e.getValue());
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Read the form-lemma mapping not read by operations
     *
     * @param dis
     */
    public void readMap(DataInputStream dis) {
        try {
            int size = dis.readInt();
            for (int i = 0; i < size; i++) {
                form2morph.put(dis.readInt(), dis.readInt());
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /*
     * (non-Javadoc) @see is2.tools.IPipe#write(java.io.DataOutputStream)
     */
    @Override
    public void write(DataOutputStream dos) {
        try {
            cl.write(dos);
            writeMap(dos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}