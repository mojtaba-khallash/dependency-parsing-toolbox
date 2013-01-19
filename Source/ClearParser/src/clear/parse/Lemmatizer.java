package clear.parse;

import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.morph.MorphEnAnalyzer;

public class Lemmatizer {

    private MorphEnAnalyzer morph_analyzer;

    public Lemmatizer(String morphDict) {
        morph_analyzer = new MorphEnAnalyzer(morphDict);
    }

    public void lemmatize(DepTree tree) {
        DepNode node;

        for (int i = 1; i < tree.size(); i++) {
            node = tree.get(i);
            node.lemma = morph_analyzer.getLemma(node.form, node.pos);
        }
    }

    static public void defaultLemmatize(DepTree tree) {
        DepNode node;

        for (int i = 1; i < tree.size(); i++) {
            node = tree.get(i);
            node.lemma = node.form.toLowerCase();
        }
    }
}