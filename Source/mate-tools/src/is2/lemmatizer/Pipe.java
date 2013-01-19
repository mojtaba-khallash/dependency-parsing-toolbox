package is2.lemmatizer;

import is2.data.*;
import is2.io.CONLLReader09;
import is2.parser.Parser;
import is2.tools.IPipe;
import is2.util.DB;
import is2.util.OptionsSuper;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

final public class Pipe extends PipeGen implements IPipe {

    private static final int _MIN_WORDS_MAPPED_BY_SCRIPT = 1;
    private static final int _MIN_OCCURENT_FOR_SCRIPT_USE = 4;
    private static final String _F0 = "F0";
    private static final String _F1 = "F1", _F2 = "F2", _F3 = "F3", _F4 = "F4", _F5 = "F5", _F6 = "F6", _F7 = "F7", _F8 = "F8", _F9 = "F9", _F10 = "F10";
    private static final String _F11 = "F11", _F12 = "F12", _F13 = "F13", _F14 = "F14", _F15 = "F15", _F16 = "F16", _F17 = "F17", _F18 = "F18", _F19 = "F19", _F20 = "F20";
    private static final String _F21 = "F21", _F22 = "F22", _F23 = "F23", _F24 = "F24", _F25 = "F25", _F26 = "F26", _F27 = "F27", _F28 = "F28", _F29 = "F29", _F30 = "F30";
    private static final String _F31 = "F31", _F32 = "F32", _F33 = "F33", _F34 = "F34", _F35 = "F35", _F36 = "F36", _F37 = "F37", _F38 = "F38", _F39 = "F39", _F40 = "F40";
    private static final String _F41 = "F41";
    private static int _f0, _f1, _f2, _f3, _f4, _f5, _f6, _f7, _f8, _f9, _f10, _f11, _f12, _f13, _f14, _f15, _f16, _f17, _f18, _f19, _f20;
    private static int _f21, _f22, _f23, _f24, _f25, _f26, _f27, _f28, _f29, _f30, _f31, _f32, _f33, _f34, _f35, _f36, _f37, _f38, _f39, _f41;
    public static int _CEND, _swrd, _ewrd;
    public static final String MID = "MID", END = "END", STR = "STR", OPERATION = "OP";
    private CONLLReader09 depReader;
    public HashMap<String, String> opse = new HashMap<>();
    public String[] types;
    public MFO mf = new MFO();
    private D4 z, x;
    Cluster cl;
    OptionsSuper options;
    Long2Int li;

    public Pipe(OptionsSuper options2, Long2Int l) {

        options = options2;
        li = l;
    }

    @Override
    public InstancesTagger createInstances(String file) {

        InstancesTagger is = new InstancesTagger();

        depReader = new CONLLReader09(CONLLReader09.NO_NORMALIZE);

        depReader.startReading(file);
        mf.register(REL, "<root-type>");
        mf.register(POS, "<root-POS>");


        Parser.out.print("Registering feature parts ");
        HashMap<String, Integer> ops = new HashMap<>();
        HashMap<String, HashSet<String>> op2form = new HashMap<>();
        int ic = 0;
        int del = 0;
        HashSet<String> rm = new HashSet<>();

        while (true) {
            SentenceData09 instance1 = depReader.getNext();
            if (instance1 == null) {
                break;
            }
            ic++;
            if (ic % 100 == 0) {
                del = outValue(ic, del);
            }


            String[] labs1 = instance1.labels;
            for (int i1 = 0; i1 < labs1.length; i1++) {
                //typeAlphabet.lookupIndex(labs1[i1]);
                mf.register(REL, labs1[i1]);
            }

            String[] w = instance1.forms;
            for (int i1 = 0; i1 < w.length; i1++) {
                // saw the first time?
                if (mf.getValue(WORD, w[i1].toLowerCase()) == -1) {
                    opse.put(instance1.forms[i1].toLowerCase(), instance1.lemmas[i1]);
                }

                mf.register(WORD, w[i1].toLowerCase());
            }
            for (int i1 = 0; i1 < w.length; i1++) {
                mf.register(WORD, w[i1]);
            }

            w = instance1.lemmas;
            for (int i1 = 0; i1 < w.length; i1++) {
                mf.register(WORD, w[i1]);
            }
            for (int i1 = 0; i1 < w.length; i1++) {
                mf.register(WORD, w[i1].toLowerCase());
            }

            w = instance1.plemmas;
            for (int i1 = 0; i1 < w.length; i1++) {
                mf.register(WORD, w[i1]);
            }
            for (int i1 = 0; i1 < w.length; i1++) {
                mf.register(WORD, w[i1].toLowerCase());
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


            for (int i1 = 1; i1 < w.length; i1++) {
                String op = getOperation(instance1, i1);
                if (ops.get(op) == null) {
                    ops.put(op, 1);
                } else {
                    ops.put(op, (ops.get(op) + 1));
                    if (ops.get(op) > 4) {
                        rm.add(instance1.forms[i1].toLowerCase());
                    }
                }


                HashSet<String> forms = op2form.get(op);
                if (forms == null) {
                    forms = new HashSet<>();
                    op2form.put(op, forms);
                }
                forms.add(instance1.forms[i1].toLowerCase());
            }
        }

        int countFreqSingleMappings = 0;

        int sc = 0;
        ArrayList<Entry<String, Integer>> opsl = new ArrayList<>();
        for (Entry<String, Integer> e : ops.entrySet()) {

            // do not use scripts for infrequent cases or frequent single mappings (der -> die)
            if (e.getValue() > _MIN_OCCURENT_FOR_SCRIPT_USE && op2form.get(e.getKey()).size() > _MIN_WORDS_MAPPED_BY_SCRIPT) {
                mf.register(OPERATION, e.getKey());
                sc++;
                opsl.add(e);
            } else {
                // do not remove the infrequent cases
                rm.removeAll(op2form.get(e.getKey()));

                if (op2form.get(e.getKey()).size() <= 1) {
                    countFreqSingleMappings += op2form.get(e.getKey()).size();
                }
            }
        }
        for (String k : rm) {
            opse.remove(k);
        }

        Collections.sort(opsl, new Comparator<Entry<String, Integer>>() {

            @Override
            public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {

                return o1.getValue() == o2.getValue() ? 0 : o1.getValue() > o2.getValue() ? 1 : -1;
            }
        });



        for (Entry<String, Integer> e : opsl) {
            //	Parser.out.println(e.getKey()+"  "+e.getValue());
        }


        if (options.clusterFile == null) {
            cl = new Cluster();
        } else {
            cl = new Cluster(options.clusterFile, mf, 6);
        }


        Parser.out.println("\nfound scripts " + ops.size() + " used scripts " + sc);
        Parser.out.println("found mappings of single words " + countFreqSingleMappings);
        Parser.out.println("use word maps instead of scripts " + this.opse.size());
        //		Parser.out.println(" "+opse);
        Parser.out.println("" + mf.toString());

        initFeatures();

        mf.calculateBits();
        initValues();

        depReader.startReading(options.trainfile);

        int i = 0;
        long start1 = System.currentTimeMillis();

        Parser.out.print("Creating Features: ");
        is.init(ic, mf);
        del = 0;
        while (true) {
            try {
                if (i % 100 == 0) {
                    del = outValue(i, del);
                }
                SentenceData09 instance1 = depReader.getNext(is);
                if (instance1 == null) {
                    break;
                }

                is.fillChars(instance1, i, _CEND);

                if (i > options.count) {
                    break;
                }

                i++;
            } catch (Exception e) {
                DB.println("error in sentnence " + i);
                e.printStackTrace();
            }
        }
        long end1 = System.currentTimeMillis();
        System.gc();
        long mem2 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        Parser.out.print("  time " + (end1 - start1) + " mem " + (mem2 / 1024) + " kb");

        types = new String[mf.getFeatureCounter().get(OPERATION)];

        for (Entry<String, Integer> e : is2.lemmatizer.MFO.getFeatureSet().get(OPERATION).entrySet()) {
            types[e.getValue()] = e.getKey();
            //	Parser.out.println("set pos "+e.getKey());
        }

        Parser.out.println("Num Features: " + mf.size());

        return is;
    }

    /**
     * @param is
     * @param n
     * @param k
     * @param wds
     * @return
     */
    public static String getOperation(Instances is, int n, int k, String[] wds) {
        String form = wds[is.forms[n][k]];
        String olemma = wds[is.glemmas[n][k]];

        String s = new StringBuffer(form.toLowerCase()).reverse().toString();
        String t = new StringBuffer(olemma.toLowerCase()).reverse().toString();

        return getOperation2(s, t);
    }

    public static String getOperation(SentenceData09 instance1, int i1) {
        String s = new StringBuffer(instance1.forms[i1].toLowerCase()).reverse().toString();
        String t = new StringBuffer(instance1.lemmas[i1].toLowerCase()).reverse().toString();

        return getOperation2(s, t);
    }

    public static String getOperation(String si, String ti) {
        String s = new StringBuffer(si.toLowerCase()).reverse().toString();
        String t = new StringBuffer(ti.toLowerCase()).reverse().toString();

        return getOperation2(s, t);
    }

    private static String getOperation2(String s, String t) {
        StringBuffer po = new StringBuffer();
        String op;
        if (!s.equals(t)) {


            int[][] d = StringEdit.LD(s, t);
            StringEdit.searchPath(s, t, d, po, false);
            op = po.toString();

        } else {
            op = "0"; // do nothing
        }
        return op;
    }

    private void registerChars(String type, String word) {
        for (int i = 0; i < word.length(); i++) {
            mf.register(type, Character.toString(word.charAt(i)));
        }
    }

    @Override
    public void initValues() {

        z = new D4(li);

        x = new D4(li);
        x.a0 = s_type;

        s_pos = mf.getFeatureCounter().get(POS).intValue();//mf.getFeatureBits(POS);
        s_word = mf.getFeatureCounter().get(WORD);
        s_type = mf.getFeatureCounter().get(TYPE).intValue();//mf.getFeatureBits(TYPE);
        s_char = mf.getFeatureCounter().get(CHAR).intValue();//mf.getFeatureBits(CHAR);
        s_oper = mf.getFeatureCounter().get(OPERATION).intValue();//mf.getFeatureBits(OPERATION);

        types = new String[mf.getFeatureCounter().get(Pipe.OPERATION)];
        for (Entry<String, Integer> e : is2.lemmatizer.MFO.getFeatureSet().get(Pipe.OPERATION).entrySet()) {
            types[e.getValue()] = e.getKey();
        }

        //wds  = new String[mf.getFeatureCounter().get(Pipe.WORD)];
        //for(Entry<String,Integer> e : mf.getFeatureSet().get(Pipe.WORD).entrySet()) wds[e.getValue()] = e.getKey();


        z.a0 = s_type;
        z.a1 = s_oper;
        z.a2 = s_char;
        z.a3 = s_char;
        z.a4 = s_char;
        z.a5 = s_char;
        z.a6 = s_char;
        z.a7 = s_char;
        x.a0 = s_type;
        x.a1 = s_oper;
        x.a2 = s_word;
        x.a3 = s_word;
        x.a4 = s_word;
        x.a5 = s_char;
        x.a6 = s_char;
        x.a7 = s_char;
    }
    public static int s_pos, s_word, s_type, s_dir, s_dist, s_char, s_oper;

    /**
     * Initialize the features.
     *
     * @param maxFeatures
     */
    @Override
    public void initFeatures() {
        for (int k = 0; k < 50; k++) {
            mf.register(TYPE, "F" + k);
        }

        _f0 = mf.register(TYPE, _F0);
        _f1 = mf.register(TYPE, _F1);
        _f2 = mf.register(TYPE, _F2);
        _f3 = mf.register(TYPE, _F3);
        _f4 = mf.register(TYPE, _F4);
        _f5 = mf.register(TYPE, _F5);
        _f6 = mf.register(TYPE, _F6);
        _f7 = mf.register(TYPE, _F7);
        _f8 = mf.register(TYPE, _F8);
        _f9 = mf.register(TYPE, _F9);
        _f10 = mf.register(TYPE, _F10);
        _f11 = mf.register(TYPE, _F11);
        _f12 = mf.register(TYPE, _F12);
        _f13 = mf.register(TYPE, _F13);
        _f14 = mf.register(TYPE, _F14);
        _f15 = mf.register(TYPE, _F15);
        _f16 = mf.register(TYPE, _F16);
        _f17 = mf.register(TYPE, _F17);
        _f18 = mf.register(TYPE, _F18);
        _f19 = mf.register(TYPE, _F19);
        _f20 = mf.register(TYPE, _F20);
        _f21 = mf.register(TYPE, _F21);
        _f22 = mf.register(TYPE, _F22);
        _f23 = mf.register(TYPE, _F23);
        _f24 = mf.register(TYPE, _F24);
        _f25 = mf.register(TYPE, _F25);
        _f26 = mf.register(TYPE, _F26);
        _f27 = mf.register(TYPE, _F27);
        _f28 = mf.register(TYPE, _F28);
        _f29 = mf.register(TYPE, _F29);
        _f30 = mf.register(TYPE, _F30);

        _f31 = mf.register(TYPE, _F31);
        _f32 = mf.register(TYPE, _F32);
        _f33 = mf.register(TYPE, _F33);
        _f34 = mf.register(TYPE, _F34);

        _f35 = mf.register(TYPE, _F35);
        _f36 = mf.register(TYPE, _F36);
        _f37 = mf.register(TYPE, _F37);
        _f38 = mf.register(TYPE, _F38);


        mf.register(POS, MID);
        mf.register(POS, STR);
        mf.register(POS, END);
        mf.register(TYPE, CHAR);

        _swrd = mf.register(WORD, STR);
        _ewrd = mf.register(WORD, END);


        _CEND = mf.register(CHAR, END);
    }

    final public void addCoreFeatures(InstancesTagger is, int ic, int i, int oper, String form, long[] f) {

        for (int l = f.length - 1; l >= 0; l--) {
            f[l] = 0;
        }

        int formi = is.forms[ic][i];
        int wl = is.chars[ic][i][11];//.forms[i].length();

        int position = 1 + (i < 3 ? i : 3);

        int c0 = is.chars[ic][i][0], c1 = is.chars[ic][i][1], c2 = is.chars[ic][i][2], c3 = is.chars[ic][i][3], c4 = is.chars[ic][i][4], c5 = is.chars[ic][i][5];
        int e0 = is.chars[ic][i][6], e1 = is.chars[ic][i][7], e2 = is.chars[ic][i][8], e3 = is.chars[ic][i][9], e4 = is.chars[ic][i][10];

        int len = is.length(ic);



        x.v1 = oper;
        x.v0 = _f0;
        x.v2 = formi;
        x.cz3();
        f[0] = x.getVal();
        f[1] = x.csa(3, position);
        x.v0 = _f1;
        x.v2 = formi;
        x.v3 = i + 1 >= len ? x.v3 = _ewrd : is.forms[ic][i + 1];
        x.cz4();
        f[2] = x.getVal();

        // contains upper case include again!!!

        short upper = 0;
        short number = 1;
        for (int k1 = 0; k1 < wl; k1++) {
            char c = form.charAt(k1);
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

        // contains a number
        z.v0 = _f21;
        z.v2 = number;
        z.cz3();
        f[3] = z.getVal();

        z.v0 = _f4;
        z.v1 = oper;
        z.v2 = c0;
        z.cz3();
        f[4] = z.getVal();
        z.v0 = _f5;
        z.v2 = e0;
        z.cz3();
        f[5] = z.getVal();

        z.v2 = c0;
        z.v3 = c1;
        z.v4 = c2;
        z.v5 = c3;
        z.v6 = c4;
        z.v0 = _f6;
        z.cz4();
        f[6] = z.getVal();
        z.v0 = _f7;
        z.cz5();
        f[7] = z.getVal();
        z.v0 = _f8;
        z.cz6();
        f[8] = z.getVal();
        z.v0 = _f9;
        z.cz7();
        f[9] = z.getVal();

        int c = 10;
        z.v2 = e0;
        z.v3 = e1;
        z.v4 = e2;
        z.v5 = e3;
        z.v6 = e4;
        z.v0 = _f10;
        z.cz4();
        f[c++] = z.getVal();
        f[c++] = z.csa(3, upper);
        z.v0 = _f11;
        z.cz5();
        f[c++] = z.getVal();
        f[c++] = z.csa(3, upper);
        z.v0 = _f12;
        z.cz6();
        f[c++] = z.getVal();
        f[c++] = z.csa(3, upper);
        z.v0 = _f13;
        z.cz7();
        f[c++] = z.getVal();
        f[c++] = z.csa(3, upper);

        if (len > i + 1) {

            z.v0 = _f14;
            z.v2 = is.chars[ic][i + 1][0];
            z.cz3();
            f[c++] = z.getVal();

            z.v0 = _f15;
            z.v2 = is.chars[ic][i + 1][5];
            z.cz3();
            f[c++] = z.getVal();

            if (is.chars[ic][i + 1][11] > 1) {
                z.v0 = _f16;
                z.v2 = is.chars[ic][i + 1][0];
                z.v3 = is.chars[ic][i + 1][2];
                z.cz4();
                f[c++] = z.getVal();

                z.v0 = _f17;
                z.v2 = is.chars[ic][i + 1][1];
                z.v3 = is.chars[ic][i + 1][6];
                z.cz4();
                f[c++] = z.getVal();//fv.add(li.l2i(mf.calc4(b)));
            }


            x.v0 = _f18;
            x.v2 = is.forms[ic][i + 1];
            x.cz3();
            f[c++] = x.getVal();

            if (len > i + 2) {
                x.v0 = _f32;
                x.v2 = is.forms[ic][i + 2];
                x.v3 = is.forms[ic][i + 1];
                x.cz4();
                f[c++] = x.getVal();
                x.cz3();
                f[c++] = x.getVal();//fv.add(li.l2i(mf.calc3(b)));

            }

            if (len > i + 3) {
                x.v0 = _f33;
                x.v2 = is.forms[ic][i + 3];
                x.v3 = is.forms[ic][i + 2];
                x.cz4();
                f[c++] = x.getVal();//fv.add(li.l2i(mf.calc4(b)));
                x.cz3();
                f[27] = x.getVal();//fv.add(li.l2i(mf.calc3(b)));
            }
        }

        // length

        z.v0 = _f19;
        z.v1 = oper;
        z.v2 = wl;
        z.cz3();
        f[c++] = z.getVal();//fv.add(li.l2i(mf.calc3(dl1)));

        if (i < 1) {
            return;
        }

        x.v0 = _f27;
        x.v1 = oper;
        x.v2 = is.forms[ic][i - 1];
        x.cz3();
        f[c++] = x.getVal();//fv.add(li.l2i(mf.calc3(b)));


        if (i < 2) {
            return;
        }

        //added this before it was 99.46
        x.v0 = _f28;
        x.v2 = is.forms[ic][i - 2];
        x.cz3();
        f[c++] = x.getVal();//fv.add(li.l2i(mf.calc3(b)));

        // result 99.484
        if (i < 3) {
            return;
        }

        x.v0 = _f31;
        x.v1 = oper;
        x.v2 = is.forms[ic][i - 3];
        x.v3 = is.forms[ic][i - 2];
        x.cz4();
        f[c++] = x.getVal();//fv.add(li.l2i(mf.calc4(b)));
    }

//	public String[] wds;
    /**
     * Write the lemma that are not mapped by operations
     *
     * @param dos
     */
    private void writeMap(DataOutputStream dos) {

        try {
            dos.writeInt(opse.size());
            for (Entry<String, String> e : opse.entrySet()) {
                dos.writeUTF(e.getKey());
                dos.writeUTF(e.getValue());
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
                opse.put(dis.readUTF(), dis.readUTF());
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
        this.writeMap(dos);
        try {
            cl.write(dos);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}