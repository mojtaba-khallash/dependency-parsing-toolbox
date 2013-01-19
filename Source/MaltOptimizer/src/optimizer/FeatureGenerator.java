package optimizer;

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 *
 * @author Miguel Ballesteros
 *
 */
public class FeatureGenerator {

    String language = "lang";
    
    private Writer writer = null;

    public FeatureGenerator(String language) {
        this(language, null);
    }
    
    public FeatureGenerator(String language, Writer writer) {
        this(writer);
        this.language = language;
    }

    public FeatureGenerator() {
        this.writer = null;
    }
    
    public FeatureGenerator(Writer writer) {
        this.writer = writer;
    }

    public String generate() {
        String feature = "";
        if (language.equals("en")) {
            feature += "<?xml version='1.0' encoding='UTF-8'?>";
            feature += "\n\t<featuremodels>";
            feature += "\n\t\t<featuremodel name=" + language + "Model>";
            feature += "\n\t\t\t<feature>InputColumn(POSTAG, Stack[0])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(POSTAG, Input[0])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(POSTAG, Input[1])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(POSTAG, Input[2])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(POSTAG, Input[3])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(POSTAG, Stack[1])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(POSTAG, Stack[2])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(POSTAG, head(Stack[0]))</feature>";
            feature += "\n\t\t\t<feature>InputColumn(POSTAG, ldep(Stack[0]))</feature>";
            feature += "\n\t\t\t<feature>InputColumn(POSTAG, rdep(Stack[0]))</feature>";
            feature += "\n\t\t\t<feature>InputColumn(POSTAG, ldep(Input[0]))</feature>";
            feature += "\n\t\t\t<feature>OutputColumn(DEPREL, Stack[0])</feature>";
            feature += "\n\t\t\t<feature>OutputColumn(DEPREL, ldep(Stack[0]))</feature>";
            feature += "\n\t\t\t<feature>OutputColumn(DEPREL, rdep(Stack[0]))</feature>";
            feature += "\n\t\t\t<feature>OutputColumn(DEPREL, ldep(Input[0]))</feature>";
            feature += "\n\t\t\t<feature>InputColumn(FORM, Stack[0])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(FORM, Input[0])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(FORM, Input[1])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(FORM, head(Stack[0]))</feature>";
            feature += "\n\t\t</featuremodel>";
            feature += "\n\t</featuremodels>";
        }

        if (language.equals("es")) {
            feature += "<?xml version='1.0' encoding='UTF-8'?>";
            feature += "\n\t<featuremodels>";
            feature += "\n\t\t<featuremodel name=" + language + "Model>";
            feature += "\n\t\t\t<feature>InputColumn(POSTAG, Stack[0])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(POSTAG, Input[0])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(POSTAG, Input[1])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(POSTAG, Input[2])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(POSTAG, Input[3])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(POSTAG, Stack[1])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(POSTAG, head(Stack[0]))</feature>";
            feature += "\n\t\t\t<feature>InputColumn(POSTAG, ldep(Stack[0]))</feature>";
            feature += "\n\t\t\t<feature>InputColumn(POSTAG, rdep(Stack[0]))</feature>";
            feature += "\n\t\t\t<feature>InputColumn(POSTAG, ldep(Input[0]))</feature>";
            feature += "\n\t\t\t<feature>InputColumn(POSTAG, succ(Stack[0]))</feature>";
            feature += "\n\t\t\t<feature>InputColumn(POSTAG, pred(Stack[0]))</feature>";
            feature += "\n\t\t\t<feature>InputColumn(POSTAG, Stack[2])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(FEATS, Stack[0])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(FEATS, Input[0])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(FEATS, Input[1])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(FEATS, head(Stack[1]))</feature>";
            feature += "\n\t\t\t<feature>OutputColumn(DEPREL, rdep(Stack[0]))</feature>";
            feature += "\n\t\t\t<feature>OutputColumn(DEPREL, ldep(Input[0]))</feature>";
            feature += "\n\t\t\t<feature>InputColumn(FORM, Stack[0])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(FORM, Input[0])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(LEMMA, Stack[0])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(LEMMA, Input[0])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(LEMMA, ldep(Stack[0]))</feature>";
            feature += "\n\t\t\t<feature>InputColumn(CPOSTAG, Stack[0])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(CPOSTAG, Input[0])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(CPOSTAG, Input[1])</feature>";
            feature += "\n\t\t\t<feature>InputColumn(CPOSTAG, head(Stack[0]))</feature>";
            feature += "\n\t\t</featuremodel>";
            feature += "\n\t</featuremodels>";
        }

        return feature;
    }

    public void addInputNivreEager(String original, String newFeature) {
        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(original));
            boolean inputFound = false;
            boolean inputClose = false;
            int inputCounter = 0;
            while (br.ready()) {
                String line = br.readLine();
                if ((line.contains("POSTAG")) && line.contains("Input[0]")) {
                    inputFound = true;
                    inputCounter++;
                } else if (inputFound && (line.contains("POSTAG")) && line.contains("Input[")) {
                    inputCounter++;
                } else if (inputFound && (!line.contains("Input["))) {
                    inputClose = true;
                    inputFound = false;
                }
                if (inputClose) {
                    inputClose = false;
                    lines.append("\t\t<feature>InputColumn(POSTAG, Input[").append(inputCounter).append("])</feature>\n");
                }
                lines.append(line).append("\n");
            }
            
            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
    }

    public void printFeature(String original) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(original));
            while (br.ready()) {
                String line = br.readLine();
                println(line);
            }
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
    }

    public void removeInputNivreEager(String original, String newFeature) {
        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(original));
            boolean inputFound = false;
            boolean inputClose = false;
            int inputCounter = 0;
            String anterior = "";
            int cont = 0;
            while (br.ready()) {
                cont++;
                String line = br.readLine();
                if ((line.contains("POSTAG")) && line.contains("Input[0]")) {
                    inputFound = true;
                    inputCounter++;
                } else if (inputFound && (line.contains("POSTAG")) && line.contains("Input[")) {
                    inputCounter++;
                } else if (inputFound && (!line.contains("Input["))) {
                    inputClose = true;
                    inputFound = false;
                }
                if (inputClose) {
                    inputClose = false;
                    anterior = line;
                } else {
                    if (cont != 0) {
                        if (!anterior.equals("")) {
                            lines.append(anterior).append("\n");
                        }
                    }
                    anterior = line;
                }
            }

            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
    }

    public void addStack(String original, String newFeature) {
        String structure = "Stack";
        if (Optimizer.bestAlgorithm.contains("cov")) {
            structure = "Left";
        }
        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(original));
            boolean StackFound = false;
            boolean StackClose = false;
            int StackCounter = 0;
            while (br.ready()) {
                String line = br.readLine();
                if ((line.contains("POSTAG")) && line.contains(structure + "[0]")) {
                    StackFound = true;
                    StackCounter++;
                } else if (StackFound && (line.contains("POSTAG")) && line.contains(structure + "[")) {
                    StackCounter++;
                } else if (StackFound && (!line.contains(structure + "["))) {
                    StackClose = true;
                    StackFound = false;
                }
                if (StackClose) {
                    StackClose = false;
                    lines.append("\t\t<feature>StackColumn(POSTAG, ").append(structure).append("[").append(StackCounter).append("])</feature>\n");
                }
                lines.append(line).append("\n");
            }

            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
    }

    public void removeStack(String original, String newFeature) {
        String structure = "Stack";
        if (Optimizer.bestAlgorithm.contains("cov")) {
            structure = "Left";
        }
        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(original));
            boolean StackFound = false;
            boolean StackClose = false;
            int StackCounter = 0;
            String anterior = "";
            int cont = 0;
            while (br.ready()) {
                cont++;
                String line = br.readLine();
                if ((line.contains("POSTAG")) && line.contains("Stack[0]")) {
                    StackFound = true;
                    StackCounter++;
                } else if (StackFound && (line.contains("POSTAG")) && line.contains(structure + "[")) {
                    StackCounter++;
                } else if (StackFound && (!line.contains(structure + "["))) {
                    StackClose = true;
                    StackFound = false;
                }
                if (StackClose) {
                    StackClose = false;
                    anterior = line;
                } else {
                    if (cont != 0) {
                        if (!anterior.equals("")) {
                            lines.append(anterior).append("\n");
                        }
                    }
                    anterior = line;
                }
            }

            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
    }

    public boolean addLookAheadStackLazy(String original, String newFeature) {
        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(original));
            boolean inputFound = false;
            boolean inputClose = false;
            int inputCounter = 0;
            while (br.ready()) {
                String line = br.readLine();
                if ((line.contains("POSTAG")) && line.contains("Lookahead[0]")) {
                    inputFound = true;
                    inputCounter++;
                } else if (inputFound && (line.contains("POSTAG")) && line.contains("Lookahead[")) {
                    inputCounter++;
                } else if (inputFound && (!line.contains("Lookahead["))) {
                    inputClose = true;
                    inputFound = false;
                }
                if (inputClose) {
                    inputClose = false;
                    lines.append("\t\t<feature>InputColumn(POSTAG, Lookahead[").append(inputCounter).append("])</feature>\n");
                }
                lines.append(line).append("\n");
            }            

            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
        return true;
    }

    public boolean removeLookAheadStackLazy(String original, String newFeature) {
        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(original));
            boolean inputFound = false;
            boolean inputClose = false;
            int inputCounter = 0;
            String anterior = "";
            int cont = 0;
            while (br.ready()) {
                cont++;
                String line = br.readLine();
                if ((line.contains("POSTAG")) && line.contains("Lookahead[0]")) {
                    inputFound = true;
                    inputCounter++;
                } else if (inputFound && (line.contains("POSTAG")) && line.contains("Lookahead[")) {
                    inputCounter++;
                } else if (inputFound && (!line.contains("Lookahead["))) {
                    inputClose = true;
                    inputFound = false;
                }
                if (inputClose) {
                    inputClose = false;
                    anterior = line;
                } else {
                    if (cont != 0) {
                        if (!anterior.equals("")) {
                            lines.append(anterior).append("\n");
                        }
                    }
                    anterior = line;
                }
            }

            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
        return true;
    }

    /**
     * ***********************
     *
     * @param featureModel
     * @param newFeature
     * @param window
     */
    public void removeStackWindow(String featureModel, String newFeature, 
            String window) {
        String structure = "Stack";
        if (Optimizer.bestAlgorithm.contains("cov")) {
            structure = "Left";
        }
        int max = findMaxStack(window, featureModel);
        if (max >= 0) {
            if (window.equals("DEPREL")) {
                println("  rm OutputColumn(" + window + "," + structure + "[" + max + "])");
            } else {
                println("  rm InputColumn(" + window + "," + structure + "[" + max + "])");
            }
            try {
                String filter = "(" + window + ", " + structure + "[" + max;
                removeFromFeatureModel(featureModel, filter, newFeature);
            } catch (FileNotFoundException e) {
                println("=> FileNotFoundException: " + e.getMessage());
            } catch (IOException e) {
                println("=> IOException: " + e.getMessage());
            }
        }
    }

    public void removeInputWindow(String featureModel, String newFeature, 
            String window) {

        int max = findMaxInput(window, featureModel, Optimizer.InputLookAhead);
        if (max >= 0) {

            if (window.equals("DEPREL")) {
                println("  rm OutputColumn(" + window + "," + Optimizer.InputLookAhead + "[" + max + "])");
            } else {
                println("  rm InputColumn(" + window + "," + Optimizer.InputLookAhead + "[" + max + "])");
            }
            try {
                String filter = "(" + window + ", " + Optimizer.InputLookAhead + "[" + max;
                removeFromFeatureModel(featureModel, filter, newFeature);
            } catch (FileNotFoundException e) {
                println("=> FileNotFoundException: " + e.getMessage());
            } catch (IOException e) {
                println("=> IOException: " + e.getMessage());
            }
        }
    }

    public void removeInputWindowSpecial(String featureModel, 
            String newFeature, String window) {

        int max = findMaxInput(window, featureModel, "Input");
        if (max >= 0) {
            println("  rm InputColumn(" + window + ",Input[" + max + "])");
            try {
                String filter = "(" + window + ", Input[" + max;
                removeFromFeatureModel(featureModel, filter, newFeature);
            } catch (FileNotFoundException e) {
                println("=> FileNotFoundException: " + e.getMessage());
            } catch (IOException e) {
                println("=> IOException: " + e.getMessage());
            }
        }
    }

    public void removeLeftContextWindowSpecial(String featureModel, 
            String newFeature, String window) {

        int max = findMaxInput(window, featureModel, "LeftContext");
        println("  rm InputColumn(" + window + ",LeftContext[" + max + "])");
        try {
            String filter = "(" + window + ", LeftContext[" + max;
            removeFromFeatureModel(featureModel, filter, newFeature);
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
    }

    public void removeRightContextWindowSpecial(String featureModel, 
            String newFeature, String window) {

        int max = findMaxInput(window, featureModel, "RightContext");
        println("  rm InputColumn(" + window + ",RightContext[" + max + "])");
        try {
            String filter = "(" + window + ", RightContext[" + max;
            removeFromFeatureModel(featureModel, filter, newFeature);
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
    }

    public void addStackWindow(String featureModel, String newFeature, 
            String window, String inputLookAhead, String inOrOut) {

        String structure = "Stack";
        if (Optimizer.bestAlgorithm.contains("cov")) {
            structure = "Left";
        }
        int max = findMaxStack(window, featureModel);
        int v = max + 1;

        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(featureModel));
            while (br.ready()) {
                String line = br.readLine();
                lines.append(line).append("\n");
                int val = max + 1;
                if (line.contains("(" + window + ", " + structure + "[" + max)) {
                    if ((line.contains("Merge") && (!line.contains("Merge3")))) {
                        lines.append("\t\t<feature>Merge(").append(inOrOut).append("(").append(window).append(", ").append(structure).append("[").append(val).append("]), InputColumn(").append(window).append(", ").append(inputLookAhead).append("[").append(val).append("]))</feature>\n");
                    } else if (line.contains("Merge3")) {
                        lines.append("\t\t<feature>Merge3(").append(inOrOut).append("(").append(window).append(", ").append(structure).append("[").append(val).append("]), InputColumn(").append(window).append(", ").append(structure).append("[").append(max).append("]), InputColumn(").append(window).append(", ").append(inputLookAhead).append("[").append(max).append("]))</feature>\n");
                    } else {
                        println("  add " + inOrOut + "(" + window + ", " + structure + "[" + val + "])");
                        lines.append("\t\t<feature>").append(inOrOut).append("(").append(window).append(", ").append(structure).append("[").append(val).append("])</feature>\n");
                    }
                }
            }

            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
    }

    public void removeInputWindow(String featureModel, String newFeature, 
            String window, String InputLookAhead) {

        int max = findMaxInput(window, featureModel, InputLookAhead);
        if (max >= 0) {
            if (window.equals("DEPREL")) {
                println("  rm OutputColumn(" + window + "," + InputLookAhead + "[" + max + "])");
            } else {
                println("  rm InputColumn(" + window + "," + InputLookAhead + "[" + max + "])");
            }
            try {
                String filter = "(" + window + ", " + InputLookAhead + "[" + max;
                removeFromFeatureModel(featureModel, filter, newFeature);
            } catch (FileNotFoundException e) {
                println("=> FileNotFoundException: " + e.getMessage());
            } catch (IOException e) {
                println("=> IOException: " + e.getMessage());
            }
        }
    }

    public boolean removeDeprelWindow(String featureModel, String newFeature, 
            String window, String InputLookAhead, int i) {

        int max = findMaxInput(window, featureModel, InputLookAhead);
        if (max >= 0) {
            try {
                StringBuilder lines = new StringBuilder();
                BufferedReader br = new BufferedReader(new FileReader(featureModel));
                boolean removed = false;
                int count = 0;
                while (br.ready()) {
                    String line = br.readLine();
                    if (line.contains("OutputColumn(" + window) && !removed) {
                        count++;
                        if (count == i) {
                            removed = true;
                            String s = line.replaceAll("<feature>", "");
                            s = s.replaceAll("</feature>", "");
                            s = s.replaceAll("\t", "");
                            println(" rm " + s);
                        } else {
                            lines.append(line).append("\n");
                        }
                    } else {
                        lines.append(line).append("\n");
                    }
                }
                
                writeFile(newFeature, lines.toString());
            } catch (FileNotFoundException e) {
                println("=> FileNotFoundException: " + e.getMessage());
            } catch (IOException e) {
                println("=> IOException: " + e.getMessage());
            }
            return true;
        }
        return false;
    }

    public boolean replicatePostagDeprel(String featureModel, String newFeature, 
            String window, String InputLookAhead, int i) {

        String structure = "Stack";
        if (Optimizer.bestAlgorithm.contains("cov")) {
            structure = "Left";
        }
        boolean usado = false;
        int max = findMaxInput(window, featureModel, InputLookAhead);

        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(featureModel));
            boolean used = false;
            boolean toInclude = false;
            int count = 0;
            while (br.ready()) {
                String line = br.readLine();
                if (line.contains("OutputColumn(" + window) && !used) {
                    count++;
                    if (count == i) {
                        used = true;
                        lines.append(line).append("\n");
                        StringTokenizer st = new StringTokenizer(line, ",");
                        String s = "";
                        if (st.hasMoreTokens()) {
                            s = st.nextToken();
                        }
                        if (st.hasMoreTokens()) {
                            s = st.nextToken();
                        }
                        s = s.replaceAll("</feature>", "");
                        s = s.substring(1, s.length());
                        String newLine;
                        if (s.contains(structure + "[0]") && (!s.contains("ldep") || !s.contains("rdep")) && Optimizer.bestAlgorithm.equals("NivreEager")) {
                            println("  add InputColumn(POSTAG, " + "head(" + s + ")");
                            newLine = "\t\t<feature>InputColumn(POSTAG, " + "head(" + s + ")</feature>\n";
                            toInclude = true;
                        } else {
                            int max2 = findMaxInput("POSTAG", featureModel, InputLookAhead);
                            newLine = "\t\t<feature>InputColumn(POSTAG, " + s + "</feature>\n";
                            if (isNotIncluded(newLine, featureModel)) {
                                println("  add InputColumn(POSTAG, " + s);
                                toInclude = true;
                            }
                        }

                        if (toInclude) {
                            lines.append(newLine);
                            toInclude = false;
                            usado = true;
                        }
                    } else {
                        lines.append(line).append("\n");
                    }
                } else {
                    lines.append(line).append("\n");
                }
            }

            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
        return usado;
    }

    private boolean isNotIncluded(String newLine, String featureModel) {

        String n = newLine.replaceAll("\n", "");
        n = n.replaceAll("\t", "");

        try {
            try (BufferedReader br = new BufferedReader(new FileReader(featureModel))) {
                while (br.ready()) {
                    String line = br.readLine();
                    if (line.contains(n)) {
                        return false;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
        return true;
    }

    public void addPredSucc(String featureModel, String newFeature, 
            String window, String inputStack, String predSucc) {

        println("  add InputColumn(" + window + "," + predSucc + "(" + inputStack + "))");
        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(featureModel));
            while (br.ready()) {
                String line = br.readLine();
                if (line.contains("InputColumn(" + window) && line.contains("inputStack")) {
                    lines.append(line).append("\n");
                    StringTokenizer st = new StringTokenizer(line, ",");
                    String s = "";
                    if (st.hasMoreTokens()) {
                        s = st.nextToken();
                    }
                    if (st.hasMoreTokens()) {
                        s = st.nextToken();
                    }
                    s = s.replaceAll("</feature>", "");
                    s = s.substring(1, s.length());
                    String newLine = "\t\t<feature>InputColumn(POSTAG, " + predSucc + "(" + s + ")" + "</feature>\n";
                    lines.append(newLine);
                } else {
                    lines.append(line).append("\n");
                }
            }

            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
    }

    public boolean removeMergeDeprelWindow(String featureModel, 
            String newFeature, String window, String InputLookAhead, int i) {

        int count = 0;
        boolean removed = false;
        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(featureModel));
            while (br.ready()) {
                String line = br.readLine();
                if (line.contains("OutputColumn(" + window) && line.contains("Merge") && !removed) {
                    count++;

                    if (count == i) {
                        removed = true;
                        String s = line.replaceAll("<feature>", "");
                        s = s.replaceAll("</feature>", "");
                        s = s.replaceAll("\t", "");
                        if (s.equals("")) {
                            return false;
                        }
                        println("  rm " + s);
                    } else {
                        lines.append(line).append("\n");
                    }
                } else {
                    lines.append(line).append("\n");
                }
            }

            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
        if (removed) {
            return true;
        }
        return false;
    }

    public void addInputWindow(String featureModel, String newFeature, 
            String window, String inputLookAhead, String inOrOut) {

        int max = findMaxInput(window, featureModel, inputLookAhead);
        int v = max + 1;
        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(featureModel));
            while (br.ready()) {
                String line = br.readLine();
                lines.append(line).append("\n");
                int val = max + 1;
                if (line.contains("(" + window + ", " + inputLookAhead + "[" + max)) {
                    if ((line.contains("Merge") && (!line.contains("Merge3")))) {
                        lines.append("\t\t<feature>Merge(").append(inOrOut).append("(").append(window).append(", ").append(inputLookAhead).append("[").append(val).append("]), InputColumn(").append(window).append(", ").append(inputLookAhead).append("[").append(val).append("]))</feature>\n");
                    } else if (line.contains("Merge3")) {
                        lines.append("\t\t<feature>Merge3(").append(inOrOut).append("(").append(window).append(", ").append(inputLookAhead).append("[").append(val).append("]), InputColumn(").append(window).append(", ").append(inputLookAhead).append("[").append(max).append("]), InputColumn(").append(window).append(", ").append(inputLookAhead).append("[").append(max).append("]))</feature>\n");
                    } else {
                        lines.append("\t\t<feature>").append(inOrOut).append("(").append(window).append(", ").append(inputLookAhead).append("[").append(val).append("])</feature>\n");
                        println("  add " + inOrOut + "(" + window + ", " + inputLookAhead + "[" + val + "])");
                    }
                }
            }

            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
    }

    public boolean addInputWindowSpecialCase(String featureModel, 
            String newFeature, String window, String inputLookAhead, 
            String inOrOut) {

        int max = findMaxInput(window, featureModel, "Input");
        int v = max + 1;
        boolean algunoEncaja = false;
        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(featureModel));
            while (br.ready()) {
                String line = br.readLine();
                int val = max + 1;
                lines.append(line).append("\n");
                if (line.contains("(" + window + ", Input[" + max)) {
                    lines.append("\t\t<feature>").append(inOrOut).append("(").append(window).append(", Input[").append(val).append("])</feature>\n");
                    println("  add " + inOrOut + "(" + window + ", Input[" + val + "])");
                    algunoEncaja = true;
                }
            }

            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
        if (algunoEncaja) {
            return true;
        }
        return false;
    }

    public void addLeftContextWindowSpecialCase(String featureModel, 
            String newFeature, String window, String inputLookAhead, 
            String inOrOut) {

        int max = findMaxInput(window, featureModel, "LeftContext");
        int v = max + 1;
        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(featureModel));
            while (br.ready()) {
                String line = br.readLine();
                lines.append(line).append("\n");
                int val = max + 1;
                if (line.contains("(" + window + ", LeftContext[" + max)) {
                    lines.append("\t\t<feature>").append(inOrOut).append("(").append(window).append(", LeftContext[").append(val).append("])</feature>\n");
                    println("  add " + inOrOut + "(" + window + ", LeftContext[" + val + "])");
                } else {
                    if (line.contains("</featuremodel>")) {
                        lines.append("\t\t<feature>").append(inOrOut).append("(").append(window).append(", LeftContext[").append(val).append("])</feature>\n");
                        println("  add " + inOrOut + "(" + window + ", LeftContext[" + val + "])");
                    }
                }
            }

            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
    }

    public void addRightContextWindowSpecialCase(String featureModel, 
            String newFeature, String window, String inputLookAhead, 
            String inOrOut) {

        int max = findMaxInput(window, featureModel, "RightContext");
        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(featureModel));
            while (br.ready()) {
                String line = br.readLine();
                lines.append(line).append("\n");
                int val = max + 1;
                if (line.contains("(" + window + ", RightContext[" + max)) {
                    lines.append("\t\t<feature>").append(inOrOut).append("(").append(window).append(", RightContext[").append(val).append("])</feature>\n");
                    println("  add " + inOrOut + "(" + window + ", RightContext[" + val + "])");
                } else {
                    if (line.contains("</featuremodel>")) {
                        lines.append("\t\t<feature>").append(inOrOut).append("(").append(window).append(", RightContext[").append(val).append("])</feature>\n");
                        println("  add " + inOrOut + "(" + window + ", RightContext[" + val + "]");
                    }
                }
            }

            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
    }

    public ArrayList<Integer> getListOfValuesFeatures(String featureModel, 
            String window, String stackInputLookAhead) {

        ArrayList<Integer> alist = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(featureModel));

            while (br.ready()) {
                String line = br.readLine();

                if (line.contains("(" + window + ", " + stackInputLookAhead + "[")) {
                    StringTokenizer st = new StringTokenizer(line, "[");
                    String valString = "";
                    if (st.hasMoreTokens()) {
                        st.nextToken();
                    }
                    if (st.hasMoreTokens()) {
                        valString = st.nextToken();
                    }
                    valString = valString.substring(0, 1);
                    Integer i = Integer.parseInt(valString);
                    alist.add(i);
                }
            }
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
        return alist;
    }

    public void addMergeFeatures(String featureModel, String newFeature, 
            String window1, String window2, String inputLookAheadStack, 
            String inOrOut, int val) {

        String structure = "Stack";
        if (Optimizer.bestAlgorithm.contains("cov")) {
            structure = "Left";
        }
        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(featureModel));
            while (br.ready()) {
                String line = br.readLine();
                lines.append(line).append("\n");
                if (line.contains("(" + window2 + ", " + inputLookAheadStack + "[" + val) && (!line.contains("Split"))) {
                    String stackInput = inputLookAheadStack;
                    int valAux = val;
                    if (Optimizer.bestAlgorithm.contains("stack")) {
                        if (inputLookAheadStack.equals("LookAhead")) {
                            if (val == 0) {
                                stackInput = structure;
                                valAux = 0;
                            } else {
                                valAux = val - 1;
                                stackInput = "LookAhead";
                            }
                        }
                        if (inputLookAheadStack.equals(structure)) {
                            valAux = val + 1;
                        }
                    }
                    if (isNotIncluded("\t\t<feature>Merge(" + inOrOut + "(" + window1 + ", " + stackInput + "[" + valAux + "]), InputColumn(" + window2 + ", " + stackInput + "[" + valAux + "]))</feature>\n", featureModel)) {
                        lines.append("\t\t<feature>Merge(").append(inOrOut).append("(").append(window1).append(", ").append(stackInput).append("[").append(valAux).append("]), InputColumn(").append(window2).append(", ").append(stackInput).append("[").append(valAux).append("]))</feature>\n");
                        println("  add Merge(" + inOrOut + "(" + window1 + ", " + stackInput + "[" + valAux + "]), InputColumn(" + window2 + ", " + stackInput + "[" + valAux + "]))");
                    }
                }
            }

            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
    }

    public void addMergeFeaturesS0(String featureModel, String newFeature, String window1, String window2, String inputLookAheadStack, String inOrOut, int val) {

        String structure = "Stack";
        if (Optimizer.bestAlgorithm.contains("cov")) {
            structure = "Left";
        }
        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(featureModel));
            while (br.ready()) {
                String line = br.readLine();
                lines.append(line).append("\n");
                if (line.contains("(" + window2 + ", " + inputLookAheadStack + "[" + val) && (!line.contains("Split")) && (!line.contains("Merge"))) {
                    String stackInput = inputLookAheadStack;
                    int valAux = val;
                    if (Optimizer.bestAlgorithm.contains("stack")) {
                        if (inputLookAheadStack.equals("LookAhead")) {
                            if (val == 0) {
                                stackInput = structure;
                                valAux = 0;
                            } else {
                                valAux = val - 1;
                                stackInput = "LookAhead";
                            }
                        }
                        if (inputLookAheadStack.equals(structure)) {
                            valAux = val + 1;
                        }
                    }
                    if (Optimizer.bestAlgorithm.contains("stack")) {
                        lines.append("\t\t<feature>Merge(").append(inOrOut).append("(").append(window1).append(", Stack[1]), ").append(inOrOut).append("(").append(window2).append(", ").append(stackInput).append("[").append(valAux).append("]))</feature>\n");
                        println("  add Merge(" + inOrOut + "(" + window1 + ", Stack[1]), " + inOrOut + "(" + window2 + ", " + stackInput + "[" + valAux + "])");
                    } else {
                        lines.append("\t\t<feature>Merge(").append(inOrOut).append("(").append(window1).append(", ").append(structure).append("[0]), ").append(inOrOut).append("(").append(window2).append(", ").append(stackInput).append("[").append(valAux).append("]))</feature>\n");
                        println("  add Merge(" + inOrOut + "(" + window1 + ", " + structure + "[0]), " + inOrOut + "(" + window2 + ", " + stackInput + "[" + valAux + "])");
                    }
                }
            }

            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
    }

    public void addMergeFeaturesI0(String featureModel, String newFeature, 
            String window1, String window2, String inputLookAheadStack, 
            String inOrOut, int val) {

        String structure = "Stack";
        if (Optimizer.bestAlgorithm.contains("cov")) {
            structure = "Left";
        }
        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(featureModel));
            while (br.ready()) {
                String line = br.readLine();
                lines.append(line).append("\n");
                if (line.contains("(" + window2 + ", " + inputLookAheadStack + "[" + val) && (!line.contains("Split")) && (!line.contains("Merge"))) {
                    String stackInput = inputLookAheadStack;
                    int valAux = val;
                    if (Optimizer.bestAlgorithm.contains("stack")) {
                        if (inputLookAheadStack.equals("LookAhead")) {
                            if (val == 0) {
                                stackInput = structure;
                                valAux = 0;
                            } else {
                                valAux = val - 1;
                                stackInput = "LookAhead";
                            }
                        }
                        if (inputLookAheadStack.equals("Stack")) {
                            valAux = val + 1;
                        }
                    }
                    if (Optimizer.bestAlgorithm.contains("stack")) {
                        lines.append("\t\t<feature>Merge(").append(inOrOut).append("(").append(window1).append(", ").append(structure).append("[0]), ").append(inOrOut).append("(").append(window2).append(", ").append(stackInput).append("[").append(valAux).append("]))</feature>\n");
                        println("  add Merge(" + inOrOut + "(" + window1 + ", " + structure + "[0]), " + inOrOut + "(" + window2 + ", " + stackInput + "[" + valAux + "]))");
                    } else {
                        lines.append("\t\t<feature>Merge(").append(inOrOut).append("(").append(window1).append(", ").append(Optimizer.InputLookAhead).append("[0]), ").append(inOrOut).append("(").append(window2).append(", ").append(stackInput).append("[").append(valAux).append("]))</feature>\n");
                        println("  add Merge(" + inOrOut + "(" + window1 + ", " + Optimizer.InputLookAhead + "[0]), " + inOrOut + "(" + window2 + ", " + stackInput + "[" + valAux + "]))");
                    }
                }
            }

            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
    }

    public void addMergeFeaturesMerge3(String featureModel, String newFeature, 
            String window1, String window2, String inputLookAheadStack, 
            String inOrOut, int val) {

        String structure = "Stack";
        if (Optimizer.bestAlgorithm.contains("cov")) {
            structure = "Left";
        }
        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(featureModel));
            while (br.ready()) {
                String line = br.readLine();
                lines.append(line).append("\n");
                if (line.contains("(" + window2 + ", " + inputLookAheadStack + "[" + val) && (!line.contains("Split")) && (!line.contains("Merge"))) {

                    String stackInput = inputLookAheadStack;
                    int valAux = val;
                    if (Optimizer.bestAlgorithm.contains("stack")) {
                        if (inputLookAheadStack.equals("LookAhead")) {
                            if (val == 0) {
                                stackInput = "Stack";
                                valAux = 0;
                            } else {
                                valAux = val - 1;
                                stackInput = "LookAhead";
                            }
                        }
                        if (inputLookAheadStack.equals(structure)) {
                            valAux = val + 1;
                        }
                    }
                    if (Optimizer.bestAlgorithm.contains("stack")) {
                        lines.append("\t\t<feature>Merge3(").append(inOrOut).append("(").append(window1).append(", ").append(structure).append("[0]), ").append(inOrOut).append("(").append(window1).append(", ").append(structure).append("[1]), InputColumn(").append(window2).append(", ").append(stackInput).append("[").append(valAux).append("]))</feature>\n");
                        println("  add Merge3(" + inOrOut + "(" + window1 + ", " + structure + "[0]), " + inOrOut + "(" + window1 + ", " + structure + "[1]), InputColumn(" + window2 + ", " + stackInput + "[" + valAux + "]))");
                    } else {
                        lines.append("\t\t<feature>Merge3(").append(inOrOut).append("(").append(window1).append(", ").append(Optimizer.InputLookAhead).append("[0]), ").append(inOrOut).append("(").append(window1).append(", ").append(structure).append("[0]), InputColumn(").append(window2).append(", ").append(inputLookAheadStack).append("[").append(val).append("]))</feature>\n");
                        println("  add Merge3(" + inOrOut + "(" + window1 + ", " + Optimizer.InputLookAhead + "[0]), " + inOrOut + "(" + window1 + ", " + structure + "[0]), InputColumn(" + window2 + ", " + inputLookAheadStack + "[" + val + "]))");
                    }
                }
            }

            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
    }

    public void addMergeFeaturesMerge3SpecialCase(String featureModel, 
            String newFeature, String window1, int val, String inOrOut) {

        println("  add Merge3(" + inOrOut + "(" + window1 + ", Stack[0]), " + inOrOut + "(" + window1 + ", Input[0])," + inOrOut + "(" + window1 + ", LookAhead[0]))");
        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(featureModel));
            while (br.ready()) {
                String line = br.readLine();
                if (line.contains("</featureModel>")) {
                    lines.append("\t\t<feature>Merge3(").append(inOrOut).append("(").append(window1).append(", Stack[0]), ").append(inOrOut).append("(").append(window1).append(", Input[0]),").append(inOrOut).append("(").append(window1).append(", LookAhead[0]))</feature>\n");
                }
                lines.append(line).append("\n");
            }

            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
    }

    public void addFeature(String featureModel, String newFeature, 
            String window, String value) {

        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(featureModel));
            while (br.ready()) {
                String line = br.readLine();

                if (line.contains("</featuremodel>")) {
                    String nLinea = "\t\t<feature>InputColumn(" + window + ", " + value + ")</feature>";
                    println("  add InputColumn(" + window + ", " + value + ")");
                    lines.append(nLinea).append("\n");
                }
                lines.append(line).append("\n");
            }

            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
    }

    public void addSplitFeature(String featureModel, String newFeature, 
            String window, String value) {

        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(featureModel));
            while (br.ready()) {
                String line = br.readLine();

                if (line.contains("</featuremodel>")) {
                    String nLinea = "\t\t<feature>Split(InputColumn(" + window + ", " + value + ")," + "\\|" + ")</feature>";
                    println("  add Split(InputColumn(" + window + ", " + value + ")," + "|" + ")");
                    lines.append(nLinea).append("\n");
                }
                lines.append(line).append("\n");
            }

            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
    }

    public void addHeadIterativeWindow(String featureModel, String newFeature, 
            String window, String inputLookAhead, String inOrOut) {

        String structure = "Stack";
        if (Optimizer.bestAlgorithm.contains("cov")) {
            structure = "Left";
        }

        String toAddAfter = findMaxHeadIterativeStack(window, featureModel);
        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(featureModel));
            while (br.ready()) {
                String line = br.readLine();
                lines.append(line).append("\n");
                if (line.contains(window) && line.contains("head(" + structure + "[")) {
                    if (line.equals(toAddAfter)) {
                        String newS = "";
                        int headCounter = 0;
                        boolean added = false;
                        StringTokenizer st = new StringTokenizer(toAddAfter, "(");
                        while (st.hasMoreTokens()) {
                            String s = st.nextToken();

                            if (s.contains("head")) {
                                if (!(newS.charAt(newS.length() - 1) == '(')) {
                                    newS += "(" + s;
                                } else {
                                    newS += s;
                                }
                                if (!added) {
                                    if (s.equals("head")) {
                                        newS += "head(";
                                    } else {
                                        newS += "(head(";
                                    }
                                    added = true;
                                } else {
                                    if (!(newS.charAt(newS.length() - 1) == '(')) {
                                        newS += "(";
                                    }
                                }
                                headCounter++;
                            } else {

                                newS += s;
                            }
                        }
                        newS = newS.replace("</feature>", "");
                        newS += ")";
                        newS += "</feature>";

                        if (newS.equals("")) {
                            newS = "\t<feature>InputColumn(FORM, head(Stack[0]))</feature>\n";
                        }
                        String s = newS.replace("</feature>", "");
                        s = s.replace("<feature>", "");
                        s = s.replace("\t", "");
                        println("  add " + s);
                        lines.append(newS).append("\n");
                    }
                }
            }

            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
    }

    public boolean removeIterativeWindow(String featureModel, String newFeature, 
            String window, String inputLookAhead, String inOrOut) {

        String structure = "Stack";
        if (Optimizer.bestAlgorithm.contains("cov")) {
            structure = "Left";
        }
        String toRemove = findMaxHeadIterativeStack(window, featureModel);
        if (toRemove.equals("")) {
            return false;
        }
        String s = toRemove.replaceAll("<feature>", "");
        s = s.replaceAll("</feature>", "");
        s = s.replaceAll("\t", "");
        if (!s.equals("")) {
            println("  rm " + s);
        }
        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(featureModel));
            while (br.ready()) {
                String line = br.readLine();
                if (line.contains("(" + window + ", head(" + structure + "[")) {
                    if (!line.equals(toRemove)) {
                        lines.append(line).append("\n");
                    } else {
                    }
                } else {
                    lines.append(line).append("\n");
                }
            }

            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
        return true;
    }

    private String findMaxHeadIterativeStack(String window, String featureModel) {

        int max = 0;
        String maxS = "";
        String structure = "Stack";
        if (Optimizer.bestAlgorithm.contains("cov")) {
            structure = "Left";
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(featureModel));
            while (br.ready()) {
                String line = br.readLine();
                if (line.contains(window) && line.contains("head(" + structure + "[")) {
                    StringTokenizer st = new StringTokenizer(line, "(");
                    int val = 0;
                    while (st.hasMoreTokens()) {
                        String sst = st.nextToken();
                        if (sst.contains("head")) {
                            val++;
                        }
                    }
                    if (val > max) {
                        max = val;
                        maxS = line;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
        return maxS;
    }

    public int findMaxStack(String window, String featureModel) {

        int max = -1;
        String structure = "Stack";
        if (Optimizer.bestAlgorithm.contains("cov")) {
            structure = "Left";
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(featureModel));
            while (br.ready()) {
                String line = br.readLine();
                if (line.contains("(" + window + ", " + structure + "[") && (!line.contains("Merge"))) {
                    String ss[] = line.split("\\[");
                    String s = ss[1].substring(0, 1);
                    int val = Integer.parseInt(s);
                    if (val > max) {
                        max = val;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
        return max;
    }

    public int findMaxInput(String window, String featureModel, String InputLookAhead) {

        int max = -1;
        try {
            BufferedReader br = new BufferedReader(new FileReader(featureModel));
            while (br.ready()) {
                String line = br.readLine();
                if (line.contains("(" + window + ", " + InputLookAhead + "[") && (!line.contains("Merge"))) {
                    String ss[] = line.split("\\[");
                    String s = ss[1].substring(0, 1);
                    int val = Integer.parseInt(s);
                    if (val > max) {
                        max = val;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
        return max;
    }

    public void addRdepWindow(String featureModel, String newFeature, String window) {

        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(featureModel));
            while (br.ready()) {
                String line = br.readLine();

                if (line.contains("</featuremodel>")) {
                    String nLinea;
                    if (window.equals("DEPREL")) {
                        nLinea = "\t\t<feature>OutputColumn(" + window + ",rdep(Stack[0]))</feature>";
                        println("  add OutputColumn(" + window + ",rdep(Stack[0]))");
                    } else {
                        nLinea = "\t\t<feature>InputColumn(" + window + ",rdep(Stack[0]))</feature>";
                        println("  add InputColumn(" + window + ",rdep(Stack[0]))");
                    }
                    lines.append(nLinea).append("\n");
                }
                lines.append(line).append("\n");
            }

            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////
    public ArrayList<String> removeAllStack(String featureModel, String newFeature,
            String window) {

        ArrayList<String> pool = new ArrayList<>();
        String structure = "Stack";
        if (Optimizer.bestAlgorithm.contains("cov")) {
            structure = "Left";
        }

        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(featureModel));
            while (br.ready()) {
                String line = br.readLine();

                if (line.contains(structure) && line.contains(window)) {
                    if (!line.contains("Merge")) {
                        line = line.replace("\t", "");
                        pool.add(line);
                    }
                } else {
                    lines.append(line).append("\n");
                }
            }

            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
        return pool;
    }

    public void emptyFeatureModel(String featureModel, String newFeature) {

        try {
            String filter = "featuremodel";
            removeFromFeatureModel(featureModel, filter, newFeature);
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
    }

    public void addFeatureLine(String featureModel, String newFeature, String newLines) {

        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(featureModel));
            while (br.ready()) {
                String line = br.readLine();

                if (line.contains("</featuremodel>")) {
                    lines.append(newLines).append("\n");
                    lines.append(line).append("\n");
                } else {
                    lines.append(line).append("\n");
                }
            }

            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
    }

    public void addFeatureLineBefore(String featureModel, String newFeature, String newLines, String cad1, String cad2) {

        try {
            StringBuilder lines = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(featureModel));
            boolean ant = false;
            while (br.ready()) {
                String line = br.readLine();

                if (line.contains(cad1) && line.contains(cad2)) {
                    ant = true;
                    lines.append(line).append("\n");
                } else {
                    if (ant) {
                        lines.append(newLines).append("\n");
                        ant = false;
                    }
                    lines.append(line).append("\n");
                }
            }
            
            writeFile(newFeature, lines.toString());
        } catch (FileNotFoundException e) {
            println("=> FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            println("=> IOException: " + e.getMessage());
        }
    }
    
    private void writeFile(String name, String content) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(name))) {
            bw.write(content);
        }        
    }
    
    private void removeFromFeatureModel(String featureModel, String filter, 
            String newFeature) throws FileNotFoundException, IOException {
        // Read All and Filter
        StringBuilder lines = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(featureModel));
        while (br.ready()) {
            String line = br.readLine();
            if (!line.contains(filter)) {
                lines.append(line).append("\n");
            }
        }

        writeFile(newFeature, lines.toString());
    }
    
    public void println(String text) {
        Optimizer.out.println(text);
        if (writer != null) {
            try {
                writer.write(text + "\n");
            }
            catch (Exception e) {}
        }
    }

    public static void main(String[] args) {
        FeatureGenerator f = new FeatureGenerator();
        Optimizer.bestAlgorithm = "nivreeager";
        System.out.println(f.removeAllStack("NivreEager.xml", "asndj1.xml", "POSTAG"));
    }
}