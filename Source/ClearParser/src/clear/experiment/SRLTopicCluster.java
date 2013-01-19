package clear.experiment;

import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.dep.srl.SRLHead;
import clear.dep.srl.SRLInfo;
import clear.reader.SRLReader;
import clear.util.cluster.Prob2dMap;
import clear.util.tuple.JObjectDoubleTuple;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SRLTopicCluster {

    HashMap<String, Prob2dMap> m_ta, m_at;
    HashMap<String, HashSet<String>> s_verbs;

    public SRLTopicCluster() {
        m_ta = new HashMap<>();
        m_at = new HashMap<>();

        s_verbs = new HashMap<>();
    }

    public void retrieveTopics(DepTree tree) {
        DepNode node, pred;
        SRLInfo info;
        String feat;
        Prob2dMap pTA, pAT;

        for (int i = 1; i < tree.size(); i++) {
            node = tree.get(i);
            info = node.srlInfo;
            if (!node.isPosx("NN.*")) {
                continue;
            }

            for (SRLHead head : info.heads) {
                pred = tree.get(head.headId);
                if ((feat = pred.getFeat("ct")) == null) {
                    continue;
                }

                pTA = getSubMap(m_ta, feat);
                pTA.increment(head.label, node.lemma);

                pAT = getSubMap(m_at, feat);
                pAT.increment(node.lemma, head.label);

                getSubSet(s_verbs, feat).add(pred.lemma);
            }
        }
    }

    public void getTopics(ArrayList<HashSet<String>> aTopics, String argKey, double threshold, int num) {
        ArrayList<JObjectDoubleTuple<String>> aTA;
        Prob2dMap pTA, pAT;
        HashSet<String> topics, clone;

        outer:
        for (String id : m_ta.keySet()) {
            pTA = m_ta.get(id);
            pAT = m_at.get(id);
            if ((aTA = pTA.getProb1dList(argKey)) == null) {
                continue;
            }
            topics = new HashSet<>();

            for (JObjectDoubleTuple<String> tup : aTA) {
                tup.value *= pAT.get1dProb(tup.object, argKey);
                if (tup.value >= threshold) {
                    topics.add(tup.object);
                }
            }

            if (topics.size() >= num) {
                for (HashSet<String> pSet : aTopics) {
                    clone = new HashSet<>(topics);
                    clone.removeAll(pSet);

                    if (clone.size() < num) {
                        continue outer;
                    }
                }

                aTopics.add(topics);
            }
        }
    }

    private Prob2dMap getSubMap(HashMap<String, Prob2dMap> mTa, String key) {
        Prob2dMap submap;

        if (mTa.containsKey(key)) {
            submap = mTa.get(key);
        } else {
            submap = new Prob2dMap();
            mTa.put(key, submap);
        }

        return submap;
    }

    private HashSet<String> getSubSet(HashMap<String, HashSet<String>> mTa, String key) {
        HashSet<String> subset;

        if (mTa.containsKey(key)) {
            subset = mTa.get(key);
        } else {
            subset = new HashSet<>();
            mTa.put(key, subset);
        }

        return subset;
    }

    static public void main(String[] args) {
        String inputFile = args[0];
        String outputFile = args[1];

        ArrayList<HashSet<String>> aTopics = new ArrayList<>();
        SRLTopicCluster tbuild = new SRLTopicCluster();
        SRLReader reader = new SRLReader(inputFile, true);
        DepTree tree;

        while ((tree = reader.nextTree()) != null) {
            tbuild.retrieveTopics(tree);
        }


        //	tbuild.getTopics(aTopics, "A0", 0.005, 10);
        tbuild.getTopics(aTopics, "A1", 0.005, 10);

        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(outputFile));
            outputStream.writeObject(aTopics);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}