package is2.data;

import is2.io.CONLLWriter09;
import java.io.*;
import java.util.ArrayList;

public class SentenceData09 {

    public String[] id;
    public String[] forms;
    public String[] lemmas;
    public String[] plemmas;
    public int[] heads;
    public int[] pheads;
    public String[] labels;
    public String[] plabels;
    public String[] gpos;   // gold pos
    public String[] ppos;
    public String feats[][];
//	public String[] split_lemma;
    public String[] sem;
    public int[] semposition;
    // predicate number, argument number -> argument string
    public String[][] arg;
    public int[][] argposition;
    public String[] fillp;
    public String[] ofeats;
    public String[] pfeats;

    public SentenceData09() {
    }

    public SentenceData09(String[] forms, String[] postags, String[] labs, int[] heads) {
        this.forms = forms;
        gpos = postags;
        labels = labs;
        this.heads = heads;
    }

    public SentenceData09(String[] forms, String[] lemmas, String[] postags, String[] labs, int[] heads) {
        this.forms = forms;
        gpos = postags;
        //ppos = postags;

        labels = labs;
        this.heads = heads;
        this.plemmas = lemmas;
    }

    public SentenceData09(String[] forms, String[] lemmas, String[] gpos, String[] ppos, String[] labs, int[] heads) {
        this.forms = forms;
        this.gpos = gpos;
        this.ppos = ppos;

        labels = labs;
        this.heads = heads;
        this.plemmas = lemmas;
    }

    public SentenceData09(String[] forms, String[] lemmas, String[] gpos, String[] ppos, String[] labs, int[] heads, String[] fillpred) {
        this.forms = forms;
        this.gpos = gpos;
        this.ppos = ppos;

        labels = labs;
        this.heads = heads;
        this.plemmas = lemmas;

        fillp = fillpred;
    }

    public SentenceData09(String[] forms, String[] lemmas, String[] olemmas, String[] gpos, String[] ppos, String[] labs, int[] heads, String[] fillpred) {
        this.forms = forms;
        this.gpos = gpos;
        this.ppos = ppos;

        labels = labs;
        this.heads = heads;
        this.plemmas = lemmas;
        this.lemmas = olemmas;
        fillp = fillpred;
    }

    public SentenceData09(String[] forms, String[] olemmas, String[] lemmas, String[] gpos,
            String[] ppos, String[] labs, int[] heads, String[] fillpred, String[] of, String[] pf) {
        this.forms = forms;
        this.gpos = gpos;
        this.ppos = ppos;

        labels = labs;
        this.heads = heads;
        this.pheads = heads;
        this.plabels = labs;
        this.plemmas = lemmas;
        this.lemmas = olemmas;

        this.ofeats = of;
        this.pfeats = pf;
        fillp = fillpred;
    }

    /**
     * Create an instance without root of the input instance
     *
     * @param instance
     */
    public SentenceData09(SentenceData09 i) {

        int length = i.length() - 1;

        forms = new String[length];
        gpos = new String[length];
        ppos = new String[length];
        plemmas = new String[length];
        plabels = new String[length];
        lemmas = new String[length];
        heads = new int[length];
        pheads = new int[length];
        ofeats = new String[length];
        pfeats = new String[length];
        labels = new String[length];
        fillp = new String[length];
        id = new String[length];


        for (int j = 0; j < length; j++) {
            forms[j] = i.forms[j + 1];
            ppos[j] = i.ppos[j + 1];
            gpos[j] = i.gpos[j + 1];

            labels[j] = i.labels[j + 1];
            heads[j] = i.heads[j + 1];



            if (i.pheads != null) {
                pheads[j] = i.pheads[j + 1];
            }
            if (i.plabels != null) {
                plabels[j] = i.plabels[j + 1];
            }


            if (i.lemmas != null) {
                lemmas[j] = i.lemmas[j + 1];
            }

            plemmas[j] = i.plemmas[j + 1];


            if (i.ofeats != null) {
                ofeats[j] = i.ofeats[j + 1];
            }
            if (i.pfeats != null) {
                pfeats[j] = i.pfeats[j + 1];
            }

            if (i.fillp != null) {
                fillp[j] = i.fillp[j + 1];
            }
            if (i.id != null) {
                id[j] = i.id[j + 1];
            }
        }
    }

    public void setPPos(String[] pos) {
        ppos = pos;
    }

    public void setLemmas(String[] lemmas) {
        this.plemmas = lemmas;
    }

    public void setFeats(String[] fts) {
        feats = new String[fts.length][];
        for (int i = 0; i < fts.length; i++) {
            feats[i] = fts[i].split("\\|");
        }
        pfeats = fts;
    }

    public int length() {
        return forms.length;
    }

    @Override
    public String toString() {
        // prepare the output
        StringWriter sw = new StringWriter();
        CONLLWriter09 snt2str = new is2.io.CONLLWriter09(sw);
        try {
            snt2str.write(this, CONLLWriter09.NO_ROOT);
            snt2str.finishWriting();
            return sw.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // backup
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < forms.length; k++) {
            sb.append(k + 1).append('\t').append(forms[k]).append('\t').append(heads[k]).append('\t').append(labels[k]).append('\n');
        }
        return sw.toString();
    }

    final public void write(DataOutputStream out) throws IOException {

        out.writeInt(forms.length);
        for (int k = 0; k < forms.length; k++) {
            out.writeUTF(forms[k]);
            out.writeUTF(ppos[k]);
            out.writeUTF(gpos[k]);
            out.writeInt(heads[k]);
            out.writeUTF(labels[k]);
            out.writeUTF(lemmas[k]);
            out.writeUTF(plemmas[k]);
            out.writeUTF(ofeats[k]);  // needed for mtag
            out.writeUTF(fillp[k]);
        }

        //	out.writeUTF(actParseTree);
    }

    final public void read(DataInputStream dis) throws IOException {

        int l = dis.readInt();

        forms = new String[l];
        lemmas = new String[l];
        plemmas = new String[l];
        ppos = new String[l];
        gpos = new String[l];
        labels = new String[l];
        heads = new int[l];
        fillp = new String[l];
        ofeats = new String[l];

        for (int k = 0; k < l; k++) {
            forms[k] = dis.readUTF();
            ppos[k] = dis.readUTF();
            gpos[k] = dis.readUTF();
            heads[k] = dis.readInt();
            labels[k] = dis.readUTF();
            lemmas[k] = dis.readUTF();
            plemmas[k] = dis.readUTF();
            ofeats[k] = dis.readUTF();
            fillp[k] = dis.readUTF();

        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        forms = (String[]) in.readObject();
        plemmas = (String[]) in.readObject();
        ppos = (String[]) in.readObject();
        heads = (int[]) in.readObject();
        labels = (String[]) in.readObject();
    }

    public void addPredicate(int i, String s) {

        int predId;
        if (sem == null) {
            predId = 0;
            sem = new String[1];
            semposition = new int[1];
        } else {
            predId = sem.length;
            String p[] = new String[sem.length + 1];
            System.arraycopy(sem, 0, p, 0, sem.length);
            int id[] = new int[sem.length + 1];
            System.arraycopy(semposition, 0, id, 0, semposition.length);
            sem = p;
            semposition = id;
        }
        sem[predId] = s;
        semposition[predId] = i;
    }

    /**
     * Add an argument
     *
     * @param i the instance (the child)
     * @param predId the id of the predicate (the head)
     * @param a the label of the argument
     */
    public void addArgument(int i, int predId, String a) {

        if (a == null || a.equals("_")) {
            return;
        }

        // ensure the space for the argument in the data structure
        if (arg == null) {
            arg = new String[predId + 1][];
            argposition = new int[predId + 1][];
        } else if (arg.length <= predId) {
            String p[][] = new String[predId + 1][];
            System.arraycopy(arg, 0, p, 0, arg.length);
            arg = p;

            int id[][] = new int[predId + 1][];
            System.arraycopy(argposition, 0, id, 0, argposition.length);
            argposition = id;
        }

        int aId;
        if (arg[predId] == null) {
            aId = 0;
            arg[predId] = new String[1];
            argposition[predId] = new int[1];
        } else {
            aId = arg[predId].length;
            String args[] = new String[arg[predId].length + 1];
            System.arraycopy(arg[predId], 0, args, 0, arg[predId].length);
            arg[predId] = args;

            int argsId[] = new int[argposition[predId].length + 1];
            System.arraycopy(argposition[predId], 0, argsId, 0, argposition[predId].length);
            argposition[predId] = argsId;
        }

        arg[predId][aId] = a;
        argposition[predId][aId] = i;
    }

    public int[] getParents() {
        return heads;
    }

    public String[] getLabels() {
        return labels;
    }

    public String printSem() {

        if (sem == null) {
            return "";
        }
        StringBuilder s = new StringBuilder();

        for (int k = 0; k < sem.length; k++) {
            s.append(sem[k]).append("\n");

            if (arg == null) {
                s.append("arg == null");
            } else if (arg.length <= k) {
                s.append("args.length <=k arg.length:").append(arg.length).append(" k:").append(k);
            } else if (arg[k] != null) {
                for (int a = 0; a < arg[k].length; a++) {
                    s.append("  ").append(arg[k][a]);
                }
            } else {
                s.append("args == null ");
            }
            s.append('\n');
        }
        return s.toString();
    }

    /**
     * Initialize a instance so that a tagger, parser, etc. could be applied
     *
     * @param forms
     */
    public void init(String[] forms) {
        this.forms = forms;
        heads = new int[forms.length];
        gpos = new String[forms.length];
        ppos = new String[forms.length];
        plemmas = new String[forms.length];
        feats = new String[forms.length][0];
        labels = new String[forms.length];
    }

    /**
     * @param instance
     * @param fillp2
     * @param i09
     */
    public void createSemantic(SentenceData09 instance) {

        this.sem = instance.sem;
        this.semposition = instance.semposition;

        if (instance.semposition != null) {
            for (int k = 0; k < instance.semposition.length; k++) {
                this.semposition[k] = instance.semposition[k] - 1;
            }
        }

        this.arg = instance.arg;


        this.argposition = instance.argposition;

        if (this.argposition != null) {
            for (int p = 0; p < instance.argposition.length; p++) {
                if (this.argposition[p] != null) {
                    for (int a = 0; a < instance.argposition[p].length; a++) {
                        this.argposition[p][a] = instance.argposition[p][a] - 1;
                    }
                }
            }
        }
    }

    /**
     *
     */
    public String oneLine() {

        StringBuilder o = new StringBuilder();
        for (int i = 1; i < this.length(); i++) {

            if (i != 1) {
                o.append(" ");
            }
            o.append(this.forms[i]);
        }
        return o.toString();
    }

    /**
     * Get the children of this instance
     *
     * @param head
     * @return children of the head
     */
    public ArrayList<Integer> getChildren(int head) {

        ArrayList<Integer> children = new ArrayList<>();
        for (int i = 0; i < length(); i++) {
            if (heads[i] == head) {
                children.add(i);
            }
        }
        return children;
    }
}