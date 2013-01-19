package clear.experiment;

import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.dep.srl.SRLHead;
import clear.dep.srl.SRLInfo;
import clear.morph.MorphEnAnalyzer;
import clear.reader.SRLReader;
import clear.util.cluster.Prob1dMap;
import clear.util.cluster.Prob2dMap;
import clear.util.cluster.SRLClusterBuilder;
import clear.util.tuple.JObjectDoubleTuple;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.regex.Pattern;

public class SRLProbE {

    Pattern P_NOUN = Pattern.compile("NN.*");
    Pattern P_STOP = Pattern.compile("\\$#ORD#\\$|\\$#CRD#\\$");
    MorphEnAnalyzer m_morph;
    String[] a_topics;
    Prob1dMap m_verbs;
    Prob2dMap m_prob;
    Prob1dMap m_topicsT_VA;
    Prob1dMap m_topicsT_V;
    Prob2dMap m_topicsA_VT;

    public SRLProbE(String dicFile, String[] topics) {
        m_morph = new MorphEnAnalyzer(dicFile);
        a_topics = topics;
        m_verbs = new Prob1dMap();
        m_prob = new Prob2dMap();

        m_topicsT_VA = new Prob1dMap();
        m_topicsT_V = new Prob1dMap();
        m_topicsA_VT = new Prob2dMap();

        for (String topic : topics) {
            m_topicsT_VA.put(topic, 0);
        }
    }

    public void lemmatize(DepTree tree) {
        DepNode node;

        for (int i = 1; i < tree.size(); i++) {
            node = tree.get(i);

            if (node.isPredicate()) {
                node.lemma = m_morph.getLemma(node.form, "VB");
            } else if (P_NOUN.matcher(node.pos).matches()) {
                node.lemma = m_morph.getLemma(node.form, "NN");
            }
        }
    }

    private String getArgKey(DepNode node, SRLHead head) {
        String dir = (node.id < head.headId) ? "-" : "+";
        return dir + head.label;
    }

    private String getArgLemma(DepTree tree, DepNode pred, DepNode node, SRLHead head) {
        DepNode tmp;

        if (head.equals("A0") && node.isLemma("by") && (tmp = tree.getRightNearestDependent(node.id)) != null) {
            return tmp.lemma;
        } else {
            return node.lemma;
        }
    }

//	========================= 1st-iteration =========================
    public void retrieveVerbs(DepTree tree, String argKey) {
        DepNode node, pred;
        String topic, verb;
        SRLInfo sInfo;

        for (int i = 1; i < tree.size(); i++) {
            node = tree.get(i);
            sInfo = node.srlInfo;

            for (SRLHead head : sInfo.heads) {
                if (!argKey.equals(getArgKey(node, head))) {
                    continue;
                }

                pred = tree.get(head.headId);
                topic = getArgLemma(tree, pred, node, head);
                verb = pred.lemma;

                if (m_topicsT_VA.containsKey(topic)) {
                    m_verbs.increment(verb);
                }
            }
        }
    }

    public void printVerbs() {
        System.out.println(m_verbs.toStringProb());
        System.out.println();
    }

    public void clearVerbs() {
        m_verbs.clear();
    }

//	========================= 2nd-iteration =========================
    public void retrieveTopics(DepTree tree, String argKey) {
        DepNode node, pred;
        String topic;
        SRLInfo sInfo;

        for (int i = 1; i < tree.size(); i++) {
            node = tree.get(i);
            sInfo = node.srlInfo;
            if (!P_NOUN.matcher(node.pos).matches()) {
                continue;
            }
            if (P_STOP.matcher(node.lemma).matches()) {
                continue;
            }

            for (SRLHead head : sInfo.heads) {
                pred = tree.get(head.headId);
                topic = getArgLemma(tree, pred, node, head);

                if (m_verbs.containsKey(pred.lemma)) {
                    if (argKey.equals(getArgKey(node, head))) {
                        m_topicsT_VA.increment(topic);
                        m_topicsA_VT.increment(topic, argKey);
                    } else {
                        for (int j = 0; j < 5; j++) {
                            m_topicsA_VT.increment(topic, "REST");
                        }
                    }

                    m_topicsT_V.increment(topic);
                }
            }
        }
    }

    public ArrayList<JObjectDoubleTuple<String>> trimTopics(double threshold, String argKey) {
        ArrayList<JObjectDoubleTuple<String>> topics = new ArrayList<>();
        String topic;
        double prob, total = 0;

        for (ObjectCursor<String> cur : m_topicsT_VA.keys()) {
            topic = cur.value;
            prob = m_topicsT_VA.getProb(topic) * m_topicsA_VT.get1dProb(topic, argKey) * m_topicsT_V.getProb(topic);
            total += prob;
            topics.add(new JObjectDoubleTuple<>(topic, prob));
        }

        ArrayList<JObjectDoubleTuple<String>> remove = new ArrayList<>();
        HashSet<String> sTopics = new HashSet<>(Arrays.asList(a_topics));
        Collections.sort(topics);

        for (JObjectDoubleTuple<String> tup : topics) {
            topic = tup.object;
            tup.value /= total;

            if (!sTopics.contains(topic) && tup.value < threshold) {
                remove.add(tup);
                m_topicsT_VA.remove(topic);
            }
        }

        topics.removeAll(remove);
        return topics;
    }

    public void printTopics(ArrayList<JObjectDoubleTuple<String>> topics) {
        StringBuilder build1 = new StringBuilder();
        StringBuilder build2 = new StringBuilder();
        DecimalFormat format = new DecimalFormat("#0.0000");

        for (JObjectDoubleTuple<String> tup : topics) {
            build1.append(tup.object);
            build1.append("|");

            build2.append(tup.object);
            build2.append("\t");
            build2.append(format.format(tup.value));
            build2.append("\n");
        }

        build1.append("\n");
        System.out.println(build1.toString());
        System.out.println(build2.toString());
    }

//	========================= 4th-iteration =========================
    public void retrieveArgs(DepTree tree) {
        DepNode node, pred;
        String arg;
        SRLInfo sInfo;

        for (int i = 1; i < tree.size(); i++) {
            node = tree.get(i);
            sInfo = node.srlInfo;

            for (SRLHead head : sInfo.heads) {
                pred = tree.get(head.headId);
                if (!m_verbs.containsKey(pred.lemma)) {
                    continue;
                }
                if (head.label.matches("AM-MOD|AM-NEG|R-.*")) {
                    continue;
                }

                arg = getArgLemma(tree, pred, node, head) + ":" + head.label;
                m_prob.increment(pred.lemma, arg);
            }
        }
    }

    public ObjectDoubleOpenHashMap<String> getTopicweights(ArrayList<JObjectDoubleTuple<String>> topics, String argLabel) {
        ObjectDoubleOpenHashMap<String> map = new ObjectDoubleOpenHashMap<>();

        for (JObjectDoubleTuple<String> tup : topics) {
            map.put(tup.object + argLabel, Math.exp(tup.value));
        }

        return map;
    }

    public void weightArgMs(double weight) {
        Prob1dMap map;
        String label;

        for (String verb : m_prob.keySet()) {
            map = m_prob.get(verb);

            for (ObjectCursor<String> arg : map.keys()) {
                label = arg.value;

                if (label.contains("AM")) {
                    map.put(label, (int) Math.ceil(weight * map.get(label)));
                }
            }
        }
    }

//	========================= 5th-iteration =========================
    public void retrieveCluster(double threshold, ObjectDoubleOpenHashMap<String> mWeights) {
        SRLClusterBuilder build = new SRLClusterBuilder(threshold);

        build.cluster(m_prob, mWeights);
    }

    static public void main(String[] args) {
        String inputFile = args[0];
        String dicFile = args[1];
        String argKey = args[2];

        //	String[] topic = "scotty,doctor".split(",");
        //	String[] topic = "scotty,rachel,doctor,warren,father,prevot,president,boy,boxell".split(",");
        //	String[] topic = "time".split(",");
        //	String[] topic = "time,grip,appetite,reason,chance,appearance,remainder,spot,desire,need,ammo,exercise,fact,number,concussion,attitude,moment,glow".split(",");
        //	String[] topic = "figure".split(",");
        String[] topic = "parent".split(",");

        SRLReader reader = new SRLReader(inputFile, true);
        SRLProbE prob = new SRLProbE(dicFile, topic);
        DepTree tree;

        // brown
        double topicThreshold = 0.02;
        double clusterThreshold = 0.15;
        // wsj-brown
        //	double topicThreshold   = 0.003;
        //	double clusterThreshold = 0.28;

        System.out.println(Arrays.toString(topic) + ": " + argKey + "\n");
        System.out.println("== Related verbs ==\n");

        while ((tree = reader.nextTree()) != null) {
            prob.lemmatize(tree);
            prob.retrieveVerbs(tree, argKey);
        }

        prob.printVerbs();
        reader.close();

        System.out.println("== Related topics ==\n");
        reader.open(inputFile);

        while ((tree = reader.nextTree()) != null) {
            prob.lemmatize(tree);
            prob.retrieveTopics(tree, argKey);
        }

        ArrayList<JObjectDoubleTuple<String>> topics = prob.trimTopics(topicThreshold, argKey);
        prob.printTopics(topics);
        reader.close();

        System.out.println("== More related verbs ==\n");
        reader.open(inputFile);
        prob.clearVerbs();

        while ((tree = reader.nextTree()) != null) {
            prob.lemmatize(tree);
            prob.retrieveVerbs(tree, argKey);
        }

        prob.printVerbs();
        reader.close();

        System.out.println("== Verb clusters ==\n");
        reader.open(inputFile);

        while ((tree = reader.nextTree()) != null) {
            prob.lemmatize(tree);
            prob.retrieveArgs(tree);
        }

        System.out.println("Clustering");
        ObjectDoubleOpenHashMap<String> mWeights = prob.getTopicweights(topics, argKey.substring(1));
        prob.retrieveCluster(clusterThreshold, mWeights);
        reader.close();
    }
}