package ir.ac.iust.nlp.dependencyparser.converter;

/**
 *
 * @author Mojtaba Khallash
 */
public class ConverterGenerator {
    
    public static String generateConverter(String type) {
        int ind = type.lastIndexOf(".py");
        if (ind != -1) {
            type = type.substring(0, ind);
        }
        
        switch (type) {
            case "conll2mst":
                return generateCoNLL2MST();
            case "mst2conll":
                return generateMST2CoNLL();
        }
        
        return "";
    }
    
    // generate "conll2mst.py" file
    private static String generateCoNLL2MST() {
        StringBuilder content = new StringBuilder();

        content.append("#! /usr/bin/python\n\n");
        content.append("import re;\n");
        content.append("import sys;\n\n");
        content.append("# Open File\n");
        content.append("f = open(sys.argv[1],'rt');\n\n");
        content.append("wrds = \"\"; pos = \"\"; labs = \"\"; par = \"\";\n\n");
        content.append("for line in f:\n\n");
        content.append("    if len(line.strip()) > 0:\n");
        content.append("        sent = re.split(\"\\t\", line);\n\n");
        content.append("        wrds += sent[1] + \"\\t\";\n");
        content.append("        pos += sent[4] + \"\\t\";\n");
        content.append("        labs += sent[7] + \"\\t\";\n");
        content.append("        par += sent[6] + \"\\t\";\n");
        content.append("    else:\n");
        content.append("        print wrds; wrds = \"\";\n");
        content.append("        print pos; pos = \"\";\n");
        content.append("        print labs; labs = \"\";\n");
        content.append("        print par; par = \"\";\n");
        content.append("        print \"\";\n\n");
        content.append("f.close();\n\n");
        
        return content.toString();
    }
    
    // generate "mst2conll.py" file
    public static String generateMST2CoNLL() {
        StringBuilder content = new StringBuilder();

        content.append("#! /usr/bin/python\n\n");
        content.append("import re;\n");
        content.append("import sys;\n\n");
        content.append("# Open File\n");
        content.append("f = open(sys.argv[1],'rt');\n\n");
        content.append("wrds = \"\";\n");
        content.append("pos = \"\";\n");
        content.append("labs = \"\";\n");
        content.append("par = \"\";\n\n");
        content.append("for line in f:\n\n");
        content.append("    if len(line.strip()) == 0:\n");
        content.append("        w = re.split(\"\\t\", wrds); p = re.split(\"\\t\", pos); l = re.split(\"\\t\", labs); pa = re.split(\"\\t\", par);\n");
        content.append("        cnt = 1;\n");
        content.append("        for t in w:\n");
        content.append("            print str(cnt) + \"\\t\" + t + \"\\t\" + t + \"\\t\" + p[cnt-1] + \"\\t\" + p[cnt-1] + \"\\t_\\t\" + pa[cnt-1] + \"\\t\" + l[cnt-1];\n");
        content.append("            cnt += 1;\n");
        content.append("        print \"\";\n");
        content.append("        wrds = \"\"; pos = \"\"; labs = \"\"; par = \"\";\n");
        content.append("    elif len(wrds) == 0:\n");
        content.append("        wrds = line.strip();\n");
        content.append("    elif len(pos) == 0:\n");
        content.append("        pos = line.strip();\n");
        content.append("    elif len(labs) == 0:\n");
        content.append("        labs = line.strip();\n");
        content.append("    else:\n");
        content.append("        par = line.strip();\n\n");
        content.append("f.close();\n\n");
        
        return content.toString();
    }
}