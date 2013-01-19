package clear.helper;

import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.morph.MorphEnLib;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class Tokenizer {

    private TokenizerME me_tokenizer;

    public Tokenizer(String modelFile) {
        try {
            me_tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream(modelFile)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DepTree tokenize(String line) {
        String[] tokens = me_tokenizer.tokenize(line);
        DepTree tree = new DepTree();
        DepNode node;

        for (int i = 0; i < tokens.length; i++) {
            node = new DepNode();
            node.id = i + 1;
            node.form = tokens[i];

            tree.add(node);
        }

        return tree;
    }

    static public String[] hyphenate(String[] tokens) {
        ArrayList<String> l0 = new ArrayList<>();
        StringTokenizer tok;

        for (String token : tokens) {
            tok = new StringTokenizer(token, "-/", true);

            while (tok.hasMoreTokens()) {
                l0.add(tok.nextToken());
            }
        }

        ArrayList<String> l1 = new ArrayList<>();
        String prev, curr, next;
        int i, idx, size = l0.size();

        for (i = 0; i < size; i++) {
            curr = l0.get(i);

            if (i > 0 && (curr.equals("-") || curr.equals("/"))) {
                if (MorphEnLib.isPunctuation(prev = l0.get(i - 1))) {
                    idx = l1.size() - 1;
                    l1.set(idx, l1.get(idx) + curr);
                    continue;
                } else if (i + 1 < size && prev.matches("\\d+") && (next = l0.get(i + 1)).matches("\\d+")) {
                    idx = l1.size() - 1;
                    l1.set(idx, l1.get(idx) + curr + next);
                    i++;
                    continue;
                }
            }

            l1.add(curr);
        }

        String[] tmp = new String[l1.size()];
        l1.toArray(tmp);

        return tmp;
    }
}