package clear.experiment;

import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.reader.DepReader;
import clear.util.IOUtil;
import java.io.File;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DepKrClean {

    final Pattern P_Q = Pattern.compile("[QU]=\\d+");
    final Pattern P_Qd = Pattern.compile("^[QU]\\d+(.*)");

    public DepKrClean(String inputDir, String outputDir) {
        File dir = new File(inputDir);
        DepReader reader;
        DepTree tree;
        PrintStream fout;

        for (String filename : dir.list()) {
            if (!filename.endsWith(".dep")) {
                continue;
            }
            System.out.println(filename);

            reader = new DepReader(inputDir + File.separator + filename, true);
            fout = IOUtil.createPrintFileStream(outputDir + File.separator + filename);

            while ((tree = reader.nextTree()) != null) {
                if (check(tree)) {
                    fout.println(tree + "\n");
                }
            }

            reader.close();
            fout.close();
        }
    }

    public boolean check(DepTree tree) {
        if (tree.size() == 2) {
            return false;
        }
        DepNode node;
        Matcher m;
        String fst, snd, form;
        int idx;

        for (int i = 1; i < tree.size(); i++) {
            node = tree.get(i);

            if (node.lemma.startsWith("+")) {
                node.lemma = node.lemma.substring(1);
            }

            if (node.lemma.endsWith("+")) {
                node.lemma = node.lemma.substring(0, node.lemma.length() - 1);
            }

            if (!node.form.contains("*")) {
                node.lemma = node.lemma.replaceAll("\\*\\/", "/");
            }

            if (P_Q.matcher(node.form).find()) {
                return false;
            }

            if (i == 1 && (m = P_Qd.matcher(node.form)).find()) {
                idx = node.lemma.indexOf("/");
                fst = node.lemma.substring(0, idx);
                snd = node.lemma.substring(idx + 1);

                form = m.group(1);
                if (snd.startsWith("SN")) {
                    form = fst + form;
                }
                if (!form.isEmpty()) {
                    node.form = form;
                }
            }
        }

        return true;
    }

    static public void main(String[] args) {
        new DepKrClean(args[0], args[1]);
    }
}