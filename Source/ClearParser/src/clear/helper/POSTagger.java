package clear.helper;

import clear.dep.DepNode;
import clear.dep.DepTree;
import java.io.FileInputStream;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

public class POSTagger {

    private POSTaggerME me_tagger;

    public POSTagger(String modelFile) {
        try {
            me_tagger = new POSTaggerME(new POSModel(new FileInputStream(modelFile)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void postag(DepTree tree) {
        String[] tags = me_tagger.tag(getTokens(tree));
        DepNode node;

        for (int i = 1; i < tree.size(); i++) {
            node = tree.get(i);
            node.pos = tags[i - 1];
        }
    }

    protected String[] getTokens(DepTree tree) {
        int i, size = tree.size();
        String[] tokens = new String[size - 1];

        for (i = 1; i < size; i++) {
            tokens[i - 1] = tree.get(i).form;
        }

        return tokens;
    }
}