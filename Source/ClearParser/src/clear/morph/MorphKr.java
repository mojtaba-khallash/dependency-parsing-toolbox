package clear.morph;

import clear.dep.DepLib;
import clear.ftr.xml.DepFtrXml;
import clear.util.tuple.JMorphTuple;
import java.util.ArrayDeque;
import java.util.ArrayList;

public class MorphKr {

    static public int SEL_FST_ST = 0;
    static public int SEL_LST_ST = 1;
    static public int SEL_LST_CP = 2;
    static public int SEL_LST_EM = 3;
    static public int SEL_LST_DS = 4;
    static public int SEL_LST_PS = 5;
    private JMorphTuple[] t_morphems;
    public boolean isX;

    public MorphKr() {
        t_morphems = new JMorphTuple[6];

        for (int i = 0; i < 5; i++) {
            t_morphems[i] = new JMorphTuple(DepLib.ROOT_TAG, DepLib.ROOT_TAG);
        }
    }

    public MorphKr(String lemma) {
        ArrayList<JMorphTuple> list = getAllMorphems(lemma);
        list = removeParens(list);

        t_morphems = new JMorphTuple[6];
        isX = MorphKrAnalyzer.isX(list);

        initPunctuation(list);
        initMorphems(list);

        /*
         * System.out.println("-- "+lemma+" --");
         *
         * for (JMorphTuple tup : t_morphems) { if (tup != null)
         * System.out.println(tup.toString()); else System.out.println("Null");
		}
         */
    }

    static public ArrayList<JMorphTuple> getAllMorphems(String lemma) {
        String[] tmp = lemma.replaceAll("\\+/", "PLUS/").split("\\+");
        ArrayList<JMorphTuple> list = new ArrayList<>(tmp.length);
        String stem, pos;
        int idx;
        //	System.out.println(Arrays.toString(tmp));
        for (String str : tmp) {
            idx = str.lastIndexOf("/");
            stem = str.substring(0, idx).trim();
            pos = str.substring(idx + 1).trim();
            //	pos  = sejongToMach(pos);

            if (stem.equals("PLUS")) {
                stem = "+";
            } else if (stem.matches("\\d+")) {
                stem = "0";
            }

            list.add(new JMorphTuple(stem, pos));
        }

        return list;
    }

    static public String sejongToMach(String pos) {
        if (pos.matches("NNG|NNP|SL|SH")) {
            return "NN";
        }
        if (pos.matches("NNB")) {
            return "NX";
        }
        if (pos.matches("NP")) {
            return "NP";
        }
        if (pos.matches("NR|SN")) {
            return "NU";
        }
        if (pos.matches("VV")) {
            return "VV";
        }
        if (pos.matches("VA|VCN")) {
            return "AJ";
        }
        if (pos.matches("VCP")) {
            return "CP";
        }
        if (pos.matches("VX")) {
            return "VX";
        }
        if (pos.matches("MM")) {
            return "DT";
        }
        if (pos.matches("MA.*")) {
            return "AD";
        }
        if (pos.matches("J.*")) {
            return "JO";
        }
        if (pos.matches("EP")) {
            return "EP";
        }
        if (pos.matches("E.*")) {
            return "EM";
        }
        if (pos.matches("XPN")) {
            return "PF";
        }
        if (pos.matches("XSN")) {
            return "SN";
        }
        if (pos.matches("XSV")) {
            return "SV";
        }
        if (pos.matches("XSA")) {
            return "SJ";
        }
        if (pos.matches("IC")) {
            return "IJ";
        }
        if (pos.matches("NF")) {
            return "NR";
        }
        if (pos.matches("NA|NV|XR")) {
            return "UK";
        }
        if (pos.matches("S.*")) {
            return "SY";
        }

        System.out.println(pos);
        return pos;
    }

    private ArrayList<JMorphTuple> removeParens(ArrayList<JMorphTuple> list) {
        ArrayDeque<JMorphTuple> deque = new ArrayDeque<>();
        JMorphTuple sub;
        int count = 0;

        for (JMorphTuple tup : list) {
            if (tup.lemma.equals(")") && count > 0) {
                do {
                    sub = deque.removeLast();
                } while (!sub.lemma.equals("("));

                deque.add(new JMorphTuple("(*)", "LR"));
                count--;
            } else {
                if (tup.lemma.equals("(")) {
                    count++;
                }
                deque.add(tup);
            }
        }

        return new ArrayList<>(deque);
    }

    private void initPunctuation(ArrayList<JMorphTuple> list) {
        int i, size = list.size();
        JMorphTuple tup = list.get(size - 1);

        if (MorphKrAnalyzer.isPunctuation(tup.pos)) {
            t_morphems[SEL_LST_PS] = tup;
        }

        ArrayList<JMorphTuple> remove = new ArrayList<>();

        for (i = 0; i < size; i++) {
            tup = list.get(i);
            if (MorphKrAnalyzer.isPunctuation(tup.pos)) {
                remove.add(tup);
            }
        }

        list.removeAll(remove);
        if (list.isEmpty()) {
            isX = true;
        }
    }

    private void initMorphems(ArrayList<JMorphTuple> list) {
        int i, size = list.size(), idx = size - 1;
        JMorphTuple tup;

        for (i = size - 1; i >= 0; i--) {
            tup = list.get(i);

            if (MorphKrAnalyzer.isCaseParticle(tup.pos)) {
                if (t_morphems[SEL_LST_CP] == null) {
                    t_morphems[SEL_LST_CP] = tup;
                }

                idx = i - 1;
            } else if (MorphKrAnalyzer.isEndingMarker(tup.pos)) {
                if (t_morphems[SEL_LST_EM] == null) {
                    t_morphems[SEL_LST_EM] = tup;
                }

                idx = i - 1;
            } else if (t_morphems[SEL_LST_DS] == null && MorphKrAnalyzer.isDerivationalSuffix(tup.pos)) {
                t_morphems[SEL_LST_DS] = tup;
            }
        }

        if (idx >= 0) {
            t_morphems[SEL_LST_ST] = list.get(idx);
        }

        if (size > 0) {
            t_morphems[SEL_FST_ST] = list.get(0);
        }
    }

    public String getMorphem(int loc, String type) {
        if (t_morphems[loc] == null) {
            return null;
        }

        if (type.equals(DepFtrXml.F_LEMMA)) {
            return t_morphems[loc].lemma;
        } else {
            return t_morphems[loc].pos;
        }
    }

    @Override
    public String toString() {
        StringBuilder build = new StringBuilder();

        for (JMorphTuple tup : t_morphems) {
            if (tup == null) {
                build.append("null");
            } else {
                build.append(tup.toString());
            }
            build.append(" | ");
        }

        return build.toString().trim();
    }
}