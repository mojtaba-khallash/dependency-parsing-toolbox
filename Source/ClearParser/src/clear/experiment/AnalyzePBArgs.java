package clear.experiment;

import clear.parse.VoiceDetector;
import clear.propbank.PBArg;
import clear.propbank.PBInstance;
import clear.propbank.PBLoc;
import clear.propbank.PBReader;
import clear.treebank.TBNode;
import clear.treebank.TBReader;
import clear.treebank.TBTree;
import clear.util.IOUtil;
import clear.util.tuple.JObjectDoubleTuple;
import clear.util.tuple.JObjectIntTuple;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class AnalyzePBArgs {

    final String TOTAL = "TOTAL";
    ArrayList<TBTree> ls_trees;
    TBTree tb_tree;
    ArrayList<ObjectIntOpenHashMap<String>> ls_numberedAdjuncts;
    ObjectIntOpenHashMap<String> m_verbs;
    ObjectIntOpenHashMap<String> m_verbArgs;
    ObjectIntOpenHashMap<String> m_verbArgNs;
    ObjectIntOpenHashMap<String> m_verbArgMs;
    ObjectIntOpenHashMap<String> m_preps;
    ObjectIntOpenHashMap<String> m_verbPreps;
    ObjectIntOpenHashMap<String> m_verbPrepNs;
    ObjectIntOpenHashMap<String> m_verbPrepMs;
    int n_count = 0;

    public AnalyzePBArgs(String rootPath, String outputFile) {
        init();
        read(rootPath);
        //	trimVerbPrep();
        print(outputFile);
    }

    void init() {
        //	initNumberedAdjunct();
        initVerbPrepPMI();
        initRequiredArgument();
    }

    void print(String outputFile) {
        //	printNumberedAdjunct(outputFile);
        //	printVerbPrepPMI(outputFile);
        printRequiredArgument(outputFile);
    }

    void read(String rootPath) {
        File root = new File(rootPath);			// v4.0
        File prop;
        String path;
        int count;

        for (String corpusDir : root.list()) // ebc, wsj
        {
            path = rootPath + File.separator + corpusDir + File.separator + "prop";
            prop = new File(path);
            if (!prop.isDirectory()) {
                continue;
            }
            System.out.println(path);

            count = 0;
            for (String propFile : prop.list()) {
                propFile = path + File.separator + propFile;
                if (++count % 100 == 0) {
                    System.out.print(".");
                }
                readParses(propFile.replaceAll("/prop/", "/parse/").replaceAll("\\.prop$", ".parse"));
                readProps(propFile);
            }

            System.out.println();
        }
    }

    void readParses(String parseFile) {
        TBReader reader = new TBReader(parseFile);
        TBTree tree;

        ls_trees = new ArrayList<>();

        while ((tree = reader.nextTree()) != null) {
            ls_trees.add(tree);
        }

        ls_trees.trimToSize();
    }

    void readProps(String propFile) {
        PBReader reader = new PBReader(propFile);
        PBInstance instance;

        while ((instance = reader.nextInstance()) != null) {
            if (!instance.type.endsWith("-v")) {
                continue;
            }
            if (instance.getArgs().size() <= 1) {
                continue;
            }
            instance.type = instance.type.substring(0, instance.type.length() - 2);
            tb_tree = ls_trees.get(instance.treeIndex);

            //	processNumberedAdjunct(instance);
            //	processVerbPrepPMI(instance);
            processRequiredArgument(instance);
        }
    }

    // ----------------------------- NumberedAdjunct -----------------------------
    void initNumberedAdjunct() {
        ls_numberedAdjuncts = new ArrayList<>();

        for (int i = 0; i <= 5; i++) {
            ls_numberedAdjuncts.add(new ObjectIntOpenHashMap<String>());
        }
    }

    void processNumberedAdjunct(PBInstance instance) {
        ObjectIntOpenHashMap<String> map;
        TBNode node;

        for (PBArg arg : instance.getArgs()) {
            if (!arg.label.matches("ARG\\d")) {
                continue;
            }
            map = ls_numberedAdjuncts.get(Integer.parseInt(arg.label.substring(3, 4)));

            for (PBLoc loc : arg.getLocs()) {
                node = tb_tree.getNode(loc.terminalId, loc.height);
                if (node.isEmptyCategoryRec()) {
                    continue;
                }
                if (!node.isPos("PP")) {
                    continue;
                }

                for (TBNode child : node.getChildren()) {
                    if (child.isPos("IN")) {
                        increment(map, child.form.toLowerCase());
                        break;
                    }
                }
            }

            increment(map, TOTAL);
        }
    }

    void printNumberedAdjunct(String outputFile) {
        try (PrintStream fout = IOUtil.createPrintFileStream(outputFile)) {
            ObjectIntOpenHashMap<String> map;
            ArrayList<JObjectIntTuple<String>> list;
            int total;
            String key;

            for (int i = 0; i < ls_numberedAdjuncts.size(); i++) {
                map = ls_numberedAdjuncts.get(i);
                list = new ArrayList<>();

                for (ObjectCursor<String> cur : map.keys()) {
                    key = cur.value;
                    if (key.equals(TOTAL)) {
                        continue;
                    }
                    list.add(new JObjectIntTuple<>(key, map.get(key)));
                }

                Collections.sort(list);

                total = map.get(TOTAL);
                fout.println("ARG" + i + "\t" + total);

                for (JObjectIntTuple<String> tup : list) {
                    fout.println(tup.object + "\t" + tup.integer + "\t" + (double) tup.integer * 100 / total);
                }
            }

            fout.flush();
        }
    }

    // ----------------------------- VerbPrepPMI -----------------------------
    void initVerbPrepPMI() {
        m_verbs = new ObjectIntOpenHashMap<>();
        m_preps = new ObjectIntOpenHashMap<>();
        m_verbPreps = new ObjectIntOpenHashMap<>();
        m_verbPrepNs = new ObjectIntOpenHashMap<>();
        m_verbPrepMs = new ObjectIntOpenHashMap<>();
        m_verbArgs = new ObjectIntOpenHashMap<>();
        m_verbArgNs = new ObjectIntOpenHashMap<>();
        m_verbArgMs = new ObjectIntOpenHashMap<>();
    }

    void processVerbPrepPMI(PBInstance instance) {
        boolean isPassive = VoiceDetector.getPassive(tb_tree.getNode(instance.predicateId, 0)) > 0;
        String vLemma = instance.type, pLemma;
        TBNode node;

        increment(m_verbs, vLemma);
        increment(m_verbs, TOTAL);

        for (PBArg arg : instance.getArgs()) {
            if (!arg.label.startsWith("ARG")) {
                continue;
            }
            if (arg.label.matches("ARGM-MOD|ARGM-NEG")) {
                continue;
            }

            if (arg.label.matches("ARG\\d")) {
                increment(m_verbArgNs, vLemma);
            } else {
                increment(m_verbArgMs, vLemma);
            }

            increment(m_verbArgs, vLemma);
            increment(m_verbArgs, TOTAL);

            for (PBLoc loc : arg.getLocs()) {
                node = tb_tree.getNode(loc.terminalId, loc.height);
                if (node.isEmptyCategoryRec()) {
                    continue;
                }
                if (!node.isPos("PP")) {
                    continue;
                }

                for (TBNode child : node.getChildren()) {
                    if (child.isPos("IN")) {
                        pLemma = child.form.toLowerCase();

                        if (!(isPassive && arg.label.equals("ARG0") && pLemma.equals("by"))) {
                            String key = vLemma + "_" + pLemma;
                            increment(m_verbPreps, key);

                            if (arg.label.matches("ARG\\d")) {
                                increment(m_verbPrepNs, key);
                            } else {
                                increment(m_verbPrepMs, key);
                            }

                            increment(m_preps, pLemma);
                            increment(m_preps, TOTAL);
                            //	if (vLemma.equals("buy") && pLemma.equals("at"))
                            //		System.out.println(instance.rolesetId+" "+arg.label+"\n"+tb_tree.getRootNode().toWords());
                        }

                        break;
                    }
                }
            }
        }
    }

    void trimVerbPrep() {
        String key;
        int value;
        String[] tmp;

        for (ObjectCursor<String> cur : m_verbPreps.keys()) {
            key = cur.value;
            tmp = key.split("_");
            value = m_verbPreps.get(key);

            if (value <= 1) {
                decrement(m_verbPreps, key, value);
                decrement(m_verbPrepNs, key, value);
                decrement(m_verbPrepMs, key, value);
                decrement(m_verbArgs, key, value);
                decrement(m_verbArgs, TOTAL, value);
                decrement(m_verbArgNs, key, value);
                decrement(m_verbArgMs, key, value);
                decrement(m_verbs, tmp[0], value);
                decrement(m_verbs, TOTAL, value);
                decrement(m_preps, tmp[1], value);
                decrement(m_preps, TOTAL, value);
            }
        }
    }

    void printVerbPrepPMI(String outputFile) {
        try (PrintStream fout = IOUtil.createPrintFileStream(outputFile)) {
            ArrayList<JObjectDoubleTuple<String>> list = new ArrayList<>();
            int nVerb, nVerbTotal, nPrep, nVerbArg, nVerbArgTotal, nVerbPrep;
            String key;
            String[] tmp;
            double smooth = 0.000001;
            @SuppressWarnings("unused")
            double pmi, pv, p, v, pnv, pmv;

            nVerbTotal = m_verbs.get(TOTAL);
            nVerbArgTotal = m_verbArgs.get(TOTAL);

            for (ObjectCursor<String> cur : m_verbPreps.keys()) {
                key = cur.value;
                tmp = key.split("_");

                nVerbPrep = m_verbPreps.get(key);
                if (nVerbPrep == 0) {
                    continue;
                }
                nVerb = m_verbs.get(tmp[0]);
                nVerbArg = m_verbArgs.get(tmp[0]);
                nPrep = m_preps.get(tmp[1]);

                pv = (double) nVerbPrep / nVerbArg;
                p = (double) nPrep / nVerbArgTotal;
                v = (double) nVerb / nVerbTotal;

                pnv = smooth + (double) m_verbPrepNs.get(key) / m_verbArgNs.get(tmp[0]);
                pmv = smooth + (double) m_verbPrepMs.get(key) / m_verbArgMs.get(tmp[0]);
                if (m_verbArgMs.get(tmp[0]) == 0) {
                    pmv = smooth;
                }
                pmi = Math.log(pnv / pmv);

                //	pmi = getPMI(pv, p);
                //	pmi /= -(Math.log(pv) + Math.log(v));

                list.add(new JObjectDoubleTuple<>(key, pmi));
            }

            Collections.sort(list);

            for (JObjectDoubleTuple<String> tup : list) {
                key = tup.object;
                tmp = key.split("_");
                pnv = smooth + (double) m_verbPrepNs.get(key) / m_verbArgNs.get(tmp[0]);
                pmv = smooth + (double) m_verbPrepMs.get(key) / m_verbArgMs.get(tmp[0]);
                if (m_verbArgMs.get(tmp[0]) == 0) {
                    pmv = smooth;
                }

                fout.println(key + "\t" + pnv + "\t" + pmv + "\t" + tup.value);
                //	fout.println(key+"\t"+m_verbPreps.get(key)+"\t"+m_preps.get(tmp[1])+"\t"+m_verbs.get(tmp[0])+"\t"+tup.value);
            }

            fout.flush();
        }
    }

// ----------------------------- RequiredArgument -----------------------------
    void initRequiredArgument() {
        m_verbs = new ObjectIntOpenHashMap<>();
        m_preps = new ObjectIntOpenHashMap<>();
        m_verbArgNs = new ObjectIntOpenHashMap<>();
        m_verbArgMs = new ObjectIntOpenHashMap<>();
    }

    void processRequiredArgument(PBInstance instance) {
        boolean isPassive = VoiceDetector.getPassive(tb_tree.getNode(instance.predicateId, 0)) > 0;
        TBNode predicate = tb_tree.getNode(instance.predicateId, 0);
        String sentence = predicate.getSentenceGroup();
        if (!(!isPassive && sentence != null && sentence.equals("SQ"))) {
            return;
        }
        n_count++;

        String vLemma = instance.type;
        String key;

        increment(m_verbs, vLemma);
        increment(m_verbs, TOTAL);

        for (PBArg arg : instance.getArgs()) {
            if (!arg.label.startsWith("ARG")) {
                continue;
            }
            if (arg.label.matches("ARGM-MOD|ARGM-NEG")) {
                continue;
            }

            if (vLemma.equals("buy") && arg.label.equals("ARGM-LOC")) {
                System.out.println(instance.predicateId + " " + arg.getLocs() + " " + tb_tree.getRootNode().toWords());
            }

            key = vLemma + "_" + arg.label;

            if (arg.label.matches("ARG\\d")) {
                increment(m_verbArgNs, key);
            } else {
                increment(m_verbArgMs, key);
                increment(m_preps, arg.label);
                increment(m_preps, TOTAL);
            }
        }
    }

    void printRequiredArgument(String outputFile) {
        try (PrintStream fout = IOUtil.createPrintFileStream(outputFile)) {
            System.out.println(n_count);

            int nVerb, nVerbTotal, nPrep, nPrepTotal, nVerbArg;
            String key;
            String[] tmp;
            double pmi, pv, p, v;
            double thresh = 0;

            HashMap<String, ArrayList<JObjectDoubleTuple<String>>> map = new HashMap<>();

            for (ObjectCursor<String> cur : m_verbs.keys()) {
                if (cur.value.equals(TOTAL)) {
                    continue;
                }
                map.put(cur.value, new ArrayList<JObjectDoubleTuple<String>>());
            }

            ArrayList<JObjectDoubleTuple<String>> list;

            for (ObjectCursor<String> cur : m_verbArgNs.keys()) {
                key = cur.value;
                tmp = key.split("_");
                list = map.get(tmp[0]);

                nVerbArg = m_verbArgNs.get(key);
                nVerb = m_verbs.get(tmp[0]);
                pmi = (double) nVerbArg * 100 / nVerb;

                if (pmi > thresh) {
                    list.add(new JObjectDoubleTuple<>(tmp[1], pmi));
                }
            }

            nVerbTotal = m_verbs.get(TOTAL);
            nPrepTotal = m_preps.get(TOTAL);

            for (ObjectCursor<String> cur : m_verbArgMs.keys()) {
                key = cur.value;
                tmp = key.split("_");
                list = map.get(tmp[0]);

                nVerbArg = m_verbArgMs.get(key);
                nVerb = m_verbs.get(tmp[0]);
                nPrep = m_preps.get(tmp[1]);
                pv = (double) nVerbArg / nVerb;
                p = (double) nPrep / nPrepTotal;
                v = (double) nVerb / nVerbTotal;
                pmi = getPMI(pv, p);
                pmi /= -(Math.log(pv) + Math.log(v));

                if (pmi > 0) {
                    list.add(new JObjectDoubleTuple<>(tmp[1], pmi));
                }
            }

            for (String verb : map.keySet()) {
                list = map.get(verb);
                Collections.sort(list);

                StringBuilder build = new StringBuilder();
                build.append(verb);
                build.append("\t");
                build.append(m_verbs.get(verb));

                for (JObjectDoubleTuple<String> tup : list) {
                    build.append("\t");
                    build.append(tup.toString());
                }

                fout.println(build.toString());
            }

            fout.flush();
        }
    }

    double getPMI(double pxy, double px) {
        return Math.log(pxy / px);
    }

    void increment(ObjectIntOpenHashMap<String> map, String key) {
        map.put(key, map.get(key) + 1);
    }

    void decrement(ObjectIntOpenHashMap<String> map, String key, int dec) {
        map.put(key, map.get(key) - dec);
    }

    double log2(double d) {
        return Math.log(d) / Math.log(2);
    }

    boolean isArgM(String label) {
        return label.startsWith("ARGM") && !label.equals("ARGM-NEG") && !label.equals("ARGM-MOD");
    }

    public static void main(String[] args) {
        AnalyzePBArgs analyzePBArgs = new AnalyzePBArgs(args[0], args[1]);
    }
}