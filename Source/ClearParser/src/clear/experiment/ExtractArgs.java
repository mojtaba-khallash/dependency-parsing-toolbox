package clear.experiment;

import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.reader.DepReader;
import clear.util.IOUtil;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class ExtractArgs {

    HashMap<String, HashSet<String>> m_obj;
    PrintStream f_out;

    public ExtractArgs(String inputFile, String outputFile) {
        m_obj = new HashMap<>();
        f_out = IOUtil.createPrintFileStream(outputFile);

        DepReader reader = new DepReader(inputFile, true);
        DepTree tree;

        while ((tree = reader.nextTree()) != null) {
            retrieve(tree);
        }

        print();
    }

    void retrieve(DepTree tree) {
        HashSet<String> set;

        for (int i = 1; i < tree.size(); i++) {
            DepNode node = tree.get(i);
            DepNode head = tree.get(node.headId);

            if (node.isDeprel("OBJ") && node.isPosx("NN.*") && head.isPosx("VB.*")) {
                if (m_obj.containsKey(head.lemma)) {
                    set = m_obj.get(head.lemma);
                } else {
                    set = new HashSet<>();
                    m_obj.put(head.lemma, set);
                }

                set.add(node.lemma);
            }
        }
    }

    void print() {
        ArrayList<String> keys = new ArrayList<>(m_obj.keySet());
        ArrayList<String> values;
        Collections.sort(keys);

        for (String key : keys) {
            values = new ArrayList<>(m_obj.get(key));
            Collections.sort(values);

            StringBuilder build = new StringBuilder();

            build.append(key);

            for (String item : values) {
                build.append(" ");
                build.append(item);
            }

            f_out.println(build.toString());
        }

        f_out.close();
    }

    static public void main(String[] args) {
        new ExtractArgs(args[0], args[1]);
    }
}