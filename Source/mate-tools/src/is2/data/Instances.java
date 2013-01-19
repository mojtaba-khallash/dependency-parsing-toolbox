package is2.data;

import is2.parser.Parser;
import java.util.BitSet;

public class Instances {

    public IEncoder m_encoder;
    protected int size = 0;
    protected int capacity;
    public int[][] forms;
    public int[][] plemmas;
    public int[][] glemmas;
    public short[][] heads;
    public short[][] pheads;
    public short[][] labels;
    public short[][] plabels;
    public short[][] gpos;
    public short[][] pposs;
    public short[][][] feats;
    public int[][] predicat;
    public short[][] predicateId;
    public short[][] semposition;
    public short[][][] arg;
    public short[][][] argposition;
    public BitSet[] pfill;
    public short[][] gfeats;
    public short[][] pfeats;

    public Instances() {
    }
    public static int m_unkown = 0;
    public static int m_count = 0;
    public static boolean m_report;
    public static boolean m_found = false;

    final public void setForm(int i, int p, String x) {


        forms[i][p] = m_encoder.getValue(PipeGen.WORD, x);
        if (forms[i][p] == -1) {
            if (m_report) {
                Parser.out.println("unkwrd " + x);
            }
            m_unkown++;
            m_found = true;
        }
        m_count++;
    }

    final public void setRel(int i, int p, String x) {
        labels[i][p] = (short) m_encoder.getValue(PipeGen.REL, x);

    }

    final public void setHead(int i, int c, int p) {
        heads[i][c] = (short) p;
    }

    final public int size() {
        return size;
    }

    public void setSize(int n) {
        size = n;
    }

    public void init(int ic, IEncoder mf) {
        init(ic, mf, -1);
    }

    public void init(int ic, IEncoder mf, int version) {
        capacity = ic;
        m_encoder = mf;

        forms = new int[capacity][];
        plemmas = new int[capacity][];
        glemmas = new int[capacity][];
        pposs = new short[capacity][];

        gpos = new short[capacity][];
        labels = new short[capacity][];
        heads = new short[capacity][];
        plabels = new short[capacity][];
        pheads = new short[capacity][];
        feats = new short[capacity][][];
        gfeats = new short[capacity][];
        pfeats = new short[capacity][];

        predicat = new int[ic][];
        predicateId = new short[ic][];
        semposition = new short[ic][];
        arg = new short[ic][][];
        argposition = new short[ic][][];

        pfill = new BitSet[ic];
    }

    public int length(int i) {
        return forms[i].length;
    }

    public int createInstance09(int length) {

        forms[size] = new int[length];
        plemmas[size] = new int[length];
        glemmas[size] = new int[length];

        pposs[size] = new short[length];

        gpos[size] = new short[length];

        labels[size] = new short[length];
        heads[size] = new short[length];

        this.pfill[size] = new BitSet(length);

        feats[size] = new short[length][];
        gfeats[size] = new short[length];
        pfeats[size] = new short[length];
        plabels[size] = new short[length];
        pheads[size] = new short[length];

        size++;

        return size - 1;

    }

    /*
     * public final void setPPos(int i, int p, String x) { ppos[i][p] =
     * (short)m_encoder.getValue(PipeGen.POS,x);
     *
     * }
     */
    public final void setPPoss(int i, int p, String x) {
        pposs[i][p] = (short) m_encoder.getValue(PipeGen.POS, x);

    }

    public final void setGPos(int i, int p, String x) {
        gpos[i][p] = (short) m_encoder.getValue(PipeGen.POS, x);
    }

    public void setLemma(int i, int p, String x) {
        plemmas[i][p] = m_encoder.getValue(PipeGen.WORD, x);
    }

    public void setGLemma(int i, int p, String x) {
        glemmas[i][p] = m_encoder.getValue(PipeGen.WORD, x);
    }

    public void setFeats(int i, int p, String[] fts) {
        if (fts == null) {
            feats[i][p] = null;
            return;
        }
        feats[i][p] = new short[fts.length];

        for (int k = 0; k < fts.length; k++) {
            feats[i][p][k] = (short) m_encoder.getValue(PipeGen.FEAT, fts[k]);
        }

    }

    public void setFeature(int i, int p, String feature) {
        if (feature == null) {
            return;
        }
        this.gfeats[i][p] = (short) m_encoder.getValue(PipeGen.FEAT, feature);
        /*
         * if (gfeats[i][p]==-1) { Parser.out.println("+"+feature); new
         * Exception().printStackTrace(); System.exit(0); }
         */
    }

    public void setPFeature(int i, int p, String feature) {
        if (feature == null) {
            return;
        }
        this.pfeats[i][p] = (short) m_encoder.getValue(PipeGen.FFEATS, feature);
    }

    public int getWValue(String v) {
        return m_encoder.getValue(PipeGen.WORD, v);
    }

    public final void setPRel(int i, int p, String x) {
        plabels[i][p] = (short) m_encoder.getValue(PipeGen.REL, x);
    }

    public final void setPHead(int i, int c, int p) {
        pheads[i][c] = (short) p;
    }

    /*
     * public String toString(int c) { StringBuffer s = new StringBuffer();
     * for(int i=0;i<length(c);i++) {
     * s.append(i).append('\t').append(forms[c][i]).append("\t_\t").append(ppos[c][i]).append('\t').
     * append('\t').append(heads[c][i]).append('\n'); }
     *
     * return s.toString(); }
     */

    /*
     * public void setPos(int i, int p, String x) { ppos[i][p] =
     * (short)m_encoder.getValue(PipeGen.POS,x);
     *
     * }
     */
    /**
     * Create the semantic representation
     *
     * @param inst
     * @param it
     * @return
     */
    public boolean createSem(int inst, SentenceData09 it) {

        boolean error = false;

        if (it.sem == null) {
            return error;
        }

        predicat[inst] = new int[it.sem.length];
        semposition[inst] = new short[it.sem.length];
        predicateId[inst] = new short[it.sem.length];

        if (it.sem != null) {
            arg[inst] = new short[it.sem.length][];
            argposition[inst] = new short[it.sem.length][];
        }
        if (it.sem == null) {
            return error;
        }

        // init sems
        for (int i = 0; i < it.sem.length; i++) {

            String pred;
            short predSense;
            if (it.sem[i].indexOf('.') > 0) {
                pred = it.sem[i].substring(0, it.sem[i].indexOf('.'));
                predSense = (short) m_encoder.getValue(PipeGen.SENSE, it.sem[i].substring(it.sem[i].indexOf('.') + 1, it.sem[i].length()));
                //Short.parseShort(it.sem[i].substring(it.sem[i].indexOf('.')+1, it.sem[i].length()));
            } else {
                pred = it.sem[i];
                predSense = (short) m_encoder.getValue(PipeGen.SENSE, "");
            }

            predicat[inst][i] = m_encoder.getValue(PipeGen.PRED, pred);
            predicateId[inst][i] = predSense;

            semposition[inst][i] = (short) it.semposition[i];

            // this can happen too when no arguments have values
            if (it.arg == null) {
                //	DB.println("error  arg == null "+i+" sem"+it.sem[i]+" inst number "+inst);
                //	error =true;
                continue;
            }

            // last pred(s) might have no argument 
            if (it.arg.length <= i) {
                //	DB.println("error in instance "+inst+" argument list and number of predicates different arg lists: "+it.arg.length+" preds "+sem.length);
                //	error =true;
                continue;
            }


            // this happens from time to time, if the predicate has no arguments
            if (it.arg[i] == null) {
                //	DB.println("error no args for pred "+i+" "+it.sem[i]+" length "+it.ppos.length);
                //		error =true;
                continue;
            }

            int argCount = it.arg[i].length;
            arg[inst][i] = new short[it.arg[i].length];
            argposition[inst][i] = new short[it.arg[i].length];

            // add the content of the argument
            for (int a = 0; a < argCount; a++) {
                arg[inst][i][a] = (short) m_encoder.getValue(PipeGen.ARG, it.arg[i][a]);
                argposition[inst][i][a] = (short) it.argposition[i][a];

                //Parser.out.print(" #"+a+" pos: "+argposition[inst][i][a]+"  "+it.arg[i][a]+" ");
            }
            //Parser.out.println("");

        }
        return error;
    }

    public int predCount(int n) {
        return pfill[n].cardinality();
    }

    /**
     * @param pscnt
     * @return
     */
    public String print(int pscnt) {
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < this.length(pscnt); i++) {
            s.append(i).append("\t").append(forms[pscnt][i]).append("\t").append(this.glemmas[pscnt][i]).append("\t").append(this.plemmas[pscnt][i]).append("\t").append(this.gpos[pscnt][i]).append("\t").append(this.pposs[pscnt][i]).append("\t").append(this.gfeats[pscnt][i]).append("\t").append((this.feats[pscnt][i] != null && this.feats[pscnt][i].length > 0 ? this.feats[pscnt][i][0] : null)).append("\t l ").append((labels[pscnt] != null && labels[pscnt].length > i ? labels[pscnt][i] : null)).append("\t").append("\t").append(heads[pscnt][i]).append("\t").append((plabels[pscnt] != null && plabels[pscnt].length > i ? plabels[pscnt][i] : null)).append("\t").append(this.predicat[pscnt][i]).append("\n");
        }
        return s.toString();
    }

    public String print1(int pscnt) {
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < this.length(pscnt); i++) {
            s.append(i).append("\t").append(forms[pscnt][i]).append("\t").append("\t").append(this.plemmas[pscnt][i]).append("\t").append(this.pposs[pscnt][i]).append("\t l ").append((labels[pscnt] != null && labels[pscnt].length > i ? labels[pscnt][i] : null)).append("\t").append("\t").append(heads[pscnt][i]).append("\t").append((plabels[pscnt] != null && plabels[pscnt].length > i ? plabels[pscnt][i] : null)).append("\n");
        }
        return s.toString();
    }
}