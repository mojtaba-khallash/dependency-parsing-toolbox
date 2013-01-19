package clear.engine;

import clear.morph.MorphEnAnalyzer;
import clear.treebank.TBNode;
import clear.treebank.TBReader;
import clear.treebank.TBTree;
import clear.util.IOUtil;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class ExtractVerbs {

    HashSet<String> m_raising;
    HashSet<String> m_sbjControl;
    HashSet<String> m_objControl;

    public ExtractVerbs(String parseFile, String outputFile, String dictFile) {
        TBReader reader = new TBReader(parseFile);
        MorphEnAnalyzer morph = new MorphEnAnalyzer(dictFile);
        TBTree tree;

        m_raising = new HashSet<>();
        m_sbjControl = new HashSet<>();
        m_objControl = new HashSet<>();

        int n;
        for (n = 0; (tree = reader.nextTree()) != null; n++) {
            extract(tree.getRootNode(), morph);
            if (n % 1000 == 0) {
                System.out.print("\r" + n + "K");
            }
        }
        System.out.println("\r" + n);

        print(outputFile);
    }

    final void extract(TBNode curr, MorphEnAnalyzer morph) {
        if (curr.isPhrase()) {
            if (curr.isPos("VP")) {
                String lemma = null;

                for (TBNode child : curr.getChildren()) {
                    if (child.isPos("VB.*")) {
                        lemma = morph.getLemma(child.form, child.pos);
                    } else if (child.isPos("PRP|RP") && lemma != null) {
                        lemma += "_" + child.pos.toLowerCase();
                    } else if (child.isPos("S") && lemma != null) {
                    }
                }
            }
        }
    }

    final void print(String outputFile) {
        PrintStream fout = IOUtil.createPrintFileStream(outputFile + ".raising");
        ArrayList<String> list = new ArrayList<>(m_raising);
        Collections.sort(list);
        for (String item : list) {
            fout.println(item);
        }

        fout = IOUtil.createPrintFileStream(outputFile + ".sbjControl");
        list = new ArrayList<>(m_sbjControl);
        Collections.sort(list);
        for (String item : list) {
            fout.println(item);
        }

        fout = IOUtil.createPrintFileStream(outputFile + ".objControl");
        list = new ArrayList<>(m_objControl);
        Collections.sort(list);
        for (String item : list) {
            fout.println(item);
        }
    }

    static public void main(String[] args) {
        ExtractVerbs extractVerbs = new ExtractVerbs(args[0], args[1], args[2]);
    }
}