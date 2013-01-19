package clear.experiment;

import clear.dep.DepFeat;
import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.dep.srl.SRLHead;
import clear.dep.srl.SRLInfo;
import clear.morph.MorphEnAnalyzer;
import clear.reader.SRLReader;
import clear.util.cluster.Prob1dMap;
import clear.util.cluster.SRLClusterBuilder;
import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class SRLVerbCluster {

    static public final String FLAG_LOCAL = "l";
    static public final String FLAG_GLOBAL = "g";
    MorphEnAnalyzer m_morph;
    Prob1dMap m_keyword;
    int i_verbId;
    HashMap<String, ObjectDoubleOpenHashMap<String>> m_args;

    public SRLVerbCluster(String dicFile) {
        m_morph = new MorphEnAnalyzer(dicFile);
        m_keyword = new Prob1dMap();
        m_args = new HashMap<>();
    }

    public void lemmatize(DepTree tree) {
        DepNode node;

        for (int i = 1; i < tree.size(); i++) {
            node = tree.get(i);
            if (node.isPredicate()) {
                node.lemma = m_morph.getLemma(node.form, "VB");
            } else if (node.isPosx("NN.*")) {
                node.lemma = m_morph.getLemma(node.form, "NN");
            }
        }
    }

    private String getArgLemma(DepTree tree, DepNode node, SRLHead head) {
        DepNode tmp;

        if (head.equals("A0") && node.isLemma("by") && (tmp = tree.getRightNearestDependent(node.id)) != null) {
            return tmp.lemma;
        } else {
            return node.lemma;
        }
    }

    public void retrieveKeywords(DepTree tree) {
        DepNode node;
        SRLInfo info;
        String key;

        for (int i = 1; i < tree.size(); i++) {
            node = tree.get(i);
            info = node.srlInfo;

            if (!node.isPosx("NN.*")) {
                continue;
            }

            for (SRLHead head : info.heads) {
                key = getArgLemma(tree, node, head) + ":" + head.label;
                m_keyword.increment(key);
            }
        }
    }

    public void retrieveArgs(DepTree tree, String flag) {
        IntObjectOpenHashMap<ObjectDoubleOpenHashMap<String>> map = getArgSet(tree);
        ObjectDoubleOpenHashMap<String> set;
        DepNode node;
        String key;

        for (int i = 1; i < tree.size(); i++) {
            if ((node = tree.get(i)).isPredicate()) {
                //	key = flag+":"+i_verbId;
                key = flag + ":" + i_verbId + ":" + node.lemma;
                set = map.get(node.id);

                if (set != null) {
                    m_args.put(key, set);
                }
                i_verbId++;
            }
        }
    }

    private IntObjectOpenHashMap<ObjectDoubleOpenHashMap<String>> getArgSet(DepTree tree) {
        IntObjectOpenHashMap<ObjectDoubleOpenHashMap<String>> map = new IntObjectOpenHashMap<>();
        ObjectDoubleOpenHashMap<String> set;
        DepNode node;
        SRLInfo info;
        String key;
        double msc;

        for (int i = 1; i < tree.size(); i++) {
            node = tree.get(i);
            info = node.srlInfo;

            for (SRLHead head : info.heads) {
                if (head.label.matches("AM-MOD|AM-NEG")) {
                    continue;
                }

                if (map.containsKey(head.headId)) {
                    set = map.get(head.headId);
                } else {
                    set = new ObjectDoubleOpenHashMap<>();
                    map.put(head.headId, set);
                }

                key = getArgLemma(tree, node, head) + ":" + head.label;
                msc = m_keyword.containsKey(key) ? Math.exp(m_keyword.getProb(key)) : 1;

                set.put(head.label, head.score);
                set.put(key, msc);
            }
        }

        return map;
    }

    public void retrieveHmCluster(SRLClusterBuilder build) {
        build.hmCluster(m_args);
    }

    public void retrieveKmCluster(SRLClusterBuilder build) {
        build.kmCluster(m_args);
    }

    public void initDS() {
        m_args.clear();
        i_verbId = 0;
    }

    public void assignCluster(DepTree tree, IntIntOpenHashMap map) {
        DepNode node;
        int clusterId;

        for (int i = 1; i < tree.size(); i++) {
            if ((node = tree.get(i)).isPredicate()) {
                if ((clusterId = map.get(i_verbId)) > 0) {
                    if (node.feats == null) {
                        node.feats = new DepFeat("");
                    }
                    node.feats.put("ct", Integer.toString(clusterId));
                }

                i_verbId++;
            }
        }
    }

    static public void main(String[] args) {
        String dicFile = args[0];
        String localFile = args[1];
        //	String globalFile = args[2];

        SRLVerbCluster cluster = new SRLVerbCluster(dicFile);
        SRLReader reader;
        DepTree tree;

        //	SRLClusterBuilder build = new SRLClusterBuilder();
        //	System.out.println("== Retrieve local keywords ==");
        reader = new SRLReader(localFile, true);

        HashSet<String> set = new HashSet<>();

        while ((tree = reader.nextTree()) != null) {
            cluster.lemmatize(tree);
            for (int i = 1; i < tree.size(); i++) {
                DepNode node = tree.get(i);

                if (node.isPredicate()) {
                    set.add(node.lemma);
                }
            }
        }

        ArrayList<String> list = new ArrayList<>(set);
        Collections.sort(list);
        for (String lemma : list) {
            System.out.println(lemma);
        }



        /*
         * while ((tree = reader.nextTree()) != null) { cluster.lemmatize(tree);
         * cluster.retrieveKeywords(tree); }
         *
         * reader.close();
         *
         * System.out.println("== Retrieve local arguments ==");
         * reader.open(localFile); cluster.initDS();
         *
         * while ((tree = reader.nextTree()) != null) { cluster.lemmatize(tree);
         * cluster.retrieveArgs(tree, FLAG_LOCAL); }
         *
         * reader.close();
         *
         * System.out.println("== Retrieve local clusters ==");
         * cluster.retrieveHmCluster(build);
         *
         * System.out.println("== Retrieve global arguments ==");
         * reader.open(globalFile); cluster.initDS();
         *
         * while ((tree = reader.nextTree()) != null) { cluster.lemmatize(tree);
         * cluster.retrieveArgs(tree, FLAG_GLOBAL); }
         *
         * reader.close();
         *
         * System.out.println("== Retrieve global clusters ==");
         * cluster.retrieveKmCluster(build);
         *
         * JObjectObjectTuple<IntIntOpenHashMap, IntIntOpenHashMap> maps =
         * build.getClusterMaps(); IntIntOpenHashMap lMap = maps.o1;
         * IntIntOpenHashMap gMap = maps.o2;
         *
         * System.out.println("== Print local clusters =="); PrintStream fout =
         * IOUtil.createPrintFileStream(localFile+".ct"); cluster.initDS();
         * reader.open(localFile);
         *
         * while ((tree = reader.nextTree()) != null) {
         * cluster.assignCluster(tree, lMap); fout.println(tree+"\n"); }
         *
         * reader.close(); fout.close();
         *
         * System.out.println("== Print global clusters =="); fout =
         * IOUtil.createPrintFileStream(globalFile+".ct"); cluster.initDS();
         * reader.open(globalFile);
         *
         * while ((tree = reader.nextTree()) != null) {
         * cluster.assignCluster(tree, gMap); fout.println(tree+"\n"); }
         *
         * reader.close();
		fout.close();
         */
    }
}