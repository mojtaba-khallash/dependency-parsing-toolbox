package optimizer;

import algorithmTester.AlgorithmTester;
import java.io.*;
import java.util.*;

/**
 *
 * @author Miguel Ballesteros
 *
 */
public class CoNLLHandler {

    BufferedReader br;
    String trainingCorpus;
    String head0;
    int numberOfTrees;
    boolean cposEqPos;
    boolean lemmaBlank;
    boolean featsBlank;
    String danglingFreq = "";

    public String getDanglingFreq() {
        return danglingFreq;
    }

    public void setDanglingFreq(String danglingFreq) {
        this.danglingFreq = danglingFreq;
    }
    String messageDivision = "";

    public String getMessageDivision() {
        return messageDivision;
    }

    public void setMessageDivision(String messageDivision) {
        this.messageDivision = messageDivision;
    }
    int numbSentences;
    public static int numSentences;

    public int getNumbSentences() {
        return numbSentences;
    }

    public void setNumbSentences(int numbSentences) {
        this.numbSentences = numbSentences;
    }

    public int getNumbTokens() {
        return numbTokens;
    }

    public void setNumbTokens(int numbTokens) {
        CoNLLHandler.numbTokens = numbTokens;
    }
    public static int numbTokens;

    public boolean isFeatsBlank() {
        return featsBlank;
    }

    public HashMap<String, Double> getRootlabels() {
        return rootlabels;
    }

    public void setRootlabels(HashMap<String, Double> rootlabels) {
        this.rootlabels = rootlabels;
    }
    String training80;
    String testing20;
    HashMap<String, Double> rootlabels;
    TreeMap<String, String> tree;
    TreeMap<Integer, ArrayList<String>> invtree;
    
    private Writer writer = null;

    public CoNLLHandler(String trainingCorpus) {
        this(trainingCorpus, null);
    }
    
    public CoNLLHandler(String trainingCorpus, Writer writer) {
        
        this.writer = writer;
        
        rootlabels = new HashMap<>();
        this.trainingCorpus = trainingCorpus;
        if (trainingCorpus.contains("/")) {
            StringTokenizer st = new StringTokenizer(trainingCorpus, "/");
            String relPath = "";
            while (st.hasMoreTokens()) {
                relPath = st.nextToken("/");
            }
            //Echo(relPath);
            training80 = relPath.replaceAll(".conll", "");
            testing20 = relPath.replaceAll(".conll", "");
            training80 += "_train80.conll";
            testing20 += "_test20.conll";
        } else {
            training80 = trainingCorpus.replaceAll(".conll", "");
            testing20 = trainingCorpus.replaceAll(".conll", "");
            training80 += "_train80.conll";
            testing20 += "_test20.conll";
        }
        try {
            br = new BufferedReader(new FileReader(trainingCorpus));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int getNumberOfTrees() {
        return numberOfTrees;
    }

    public void setNumberOfTrees(int numberOfTrees) {
        this.numberOfTrees = numberOfTrees;
    }

    public int getNumberOfSentences() {
        return numberOfTrees;
    }

    /**
     *
     * @return A simple Text plain random sentence from the corpus.
     */
    public String getSamplePlainText() {

        Random r = new Random();
        int nrand = r.nextInt(15);
        //int nrand=0;
        boolean buscando = true;
        int i = 0;
        try {
            br = new BufferedReader(new FileReader(trainingCorpus));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        while (buscando) {
            String line;
            try {
                line = br.readLine();
                if (line != null && line.equals("")) {
                    i++;
                    if (i == nrand) {
                        buscando = false;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        boolean nofinfrase = true;
        String cat = "";
        while (nofinfrase) {
            String line;
            try {
                line = br.readLine();
                //Echo(line);
                StringTokenizer st = new StringTokenizer(line);
                if (st.hasMoreTokens()) {
                    st.nextToken();
                }
                if (st.hasMoreTokens()) {
                    cat += st.nextToken() + " ";
                }
                if (line != null && line.equals("")) {
                    nofinfrase = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Echo(cat);
        return cat;
    }

    /**
     *
     * @return true if the root labels are for the word ROOT, otherwise false.
     *
     */
    public boolean rootLabels() {
        int numberRoots = 0;
        boolean diferentes = false;
        try {
            br = new BufferedReader(new FileReader(trainingCorpus));
            while (br.ready()) {
                String line;
                try {
                    line = br.readLine();
                    //Echo(line);
                    String head = getColumn(line, 7);
                    if (head.equals("0")) {
                        numberRoots++;
                        String root = getColumn(line, 8);
                        Double d = rootlabels.get(root);
                        if (d == null) {
                            d = new Double(0);
                        }
                        d = d + 1.0;
                        rootlabels.put(root, d);
                        head0 = root;
                        if (root.equals("ROOT")) {
                            diferentes = true;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        int numb = 0;
        Set<String> set = rootlabels.keySet();
        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            numb++;
            String s = it.next();
            Double nr = new Double(numberRoots);
            rootlabels.put(s, rootlabels.get(s) / nr);
        }
        if (numb > 1) {
            return true;
        }
        return false;
    }

    public String getTraining80() {
        return training80;
    }

    public void setTraining80(String training80) {
        this.training80 = training80;
    }

    public String getTesting20() {
        return testing20;
    }

    public void setTesting20(String testing20) {
        this.testing20 = testing20;
    }

    /**
     *
     * @return true if the HEAD=0 for punctuation tokens is covered by another
     * arc, otherwise false.
     *
     */
    public boolean danglingPunctuation() {
        int contNonProjLocal = 0;
        int numberOfCovered = 0;
        int sentencesCount = 0;
        boolean dangling = false;
        int contCoveredRoots = 0;
        TreeMap<String, String> treeDangling;
        TreeMap<String, String> treeForms;
        try {
            br = new BufferedReader(new FileReader(trainingCorpus));
            treeDangling = new TreeMap<>();
            treeForms = new TreeMap<>();
            while (br.ready()) {
                String line;
                try {
                    line = br.readLine();
                    //Echo(line);
                    if (line == null || line.equals("")) {
                        sentencesCount++;
                        treeDangling = new TreeMap<>();
                        treeForms = new TreeMap<>();
                        if (dangling) {
                            contCoveredRoots++;
                        }
                        dangling = false;
                    }
                    if (line != null && !line.equals("")) {

                        String id = getColumn(line, 1);
                        String head = getColumn(line, 7);
                        treeDangling.put(id, head);
                        String form = getColumn(line, 2);
                        treeForms.put(id, form);
                        int contador = 0;
                        //if (head.equals("0")){
                        int intid = Integer.parseInt(id);
                        int inthead = Integer.parseInt(head);
                        contNonProjLocal = 0;
                        if (inthead < intid) {
                            //COVERED ROOTS!!
                            for (int i = inthead + 1; i < intid; i++) {
                                String numb = "" + i;
                                String formN = treeForms.get(numb);
                                if (formN.equals(",") || 
                                    formN.equals(".") || 
                                    formN.equals(";") || 
                                    formN.equals("-") || 
                                    formN.equals("\"") || 
                                    formN.equals("'")) {
                                    //Echo(numb);
                                    String headOld = treeDangling.get(numb);
                                    //Echo(headOld);
                                    if (headOld != null) {
                                        int intHeadOld = Integer.parseInt(headOld);
                                        if (intHeadOld == 0) {
                                            if (headOld != null) {
                                                int hold = Integer.parseInt(headOld);
                                                if (hold < inthead) {
                                                    contador++;
                                                    contNonProjLocal++;
                                                } else if (hold > intid) {
                                                    contador++;
                                                    contNonProjLocal++;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (contNonProjLocal > 0) {
                        //Echo(line);
                        dangling = true;
                        numberOfCovered += contNonProjLocal;
                        contNonProjLocal = 0;

                        //return true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Echo(contCoveredRoots);
        double frequency = ((double) contCoveredRoots / (double) sentencesCount) * 100;
        String freq = String.format(Optimizer.pattern, frequency);
        //Echo("Frequency of Sentences with Covered Roots (dangling punctuation): "+freq);
        //Echo("Number of Covered Roots (punctuation): "+numberOfCovered);
        danglingFreq = "" + numberOfCovered;
        if (contCoveredRoots > 0) {
            return true;
        }
        return false;
    }

    /**
     *
     * @return true if the HEAD=0 covered by another Arc, otherwise false.
     *
     */
    public boolean coveredRoots() {
        int contNonProjLocal = 0;
        int numberOfCovered = 0;
        int sentencesCount = 0;
        boolean dangling = false;
        int contCoveredRoots = 0;
        TreeMap<String, String> treeDangling;
        try {
            br = new BufferedReader(new FileReader(trainingCorpus));
            treeDangling = new TreeMap<>();
            while (br.ready()) {
                String line;
                try {
                    line = br.readLine();
                    //Echo(line);
                    if (line == null || line.equals("")) {
                        sentencesCount++;
                        treeDangling = new TreeMap<>();
                        if (dangling) {
                            contCoveredRoots++;
                        }
                        dangling = false;
                    }
                    if (line != null && !line.equals("")) {

                        String id = getColumn(line, 1);
                        String head = getColumn(line, 7);
                        treeDangling.put(id, head);
                        String form = getColumn(line, 2);
                        int contador = 0;
                        //if (head.equals("0")){
                        int intid = Integer.parseInt(id);
                        int inthead = Integer.parseInt(head);
                        contNonProjLocal = 0;
                        if (inthead < intid) {
                            //COVERED ROOTS!!
                            for (int i = inthead + 1; i < intid; i++) {
                                String numb = "" + i;
                                //Echo(numb);
                                String headOld = treeDangling.get(numb);
                                //Echo(headOld);
                                if (headOld != null) {
                                    int intHeadOld = Integer.parseInt(headOld);
                                    if (intHeadOld == 0) {
                                        if (headOld != null) {
                                            int hold = Integer.parseInt(headOld);
                                            if (hold < inthead) {
                                                contador++;
                                                contNonProjLocal++;
                                            } else if (hold > intid) {
                                                contador++;
                                                contNonProjLocal++;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (contNonProjLocal > 0) {
                        //Echo(line);
                        dangling = true;
                        numberOfCovered += contNonProjLocal;
                        contNonProjLocal = 0;
                        //return true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        double frequency = (double) contCoveredRoots / (double) sentencesCount;
        String freq = "" + frequency;
        if (freq.length() > 6) {
            freq = freq.substring(0, 6);
        }
        println("Frequency of Sentences with Covered Roots: " + freq);
        println("Number of Covered Roots: " + numberOfCovered);
        if (contCoveredRoots > 0) {
            return true;
        }
        return false;
    }

    /**
     *
     * @return true if the HEAD=0 covered by another Arc, otherwise false.
     *
     */
    public boolean coveredRootsWithoutChildren() {
        String concat = "";
        int contNonProjLocal = 0;
        int numberOfCovered = 0;
        int sentencesCount = 0;
        boolean dangling = false;
        int contCoveredRoots = 0;
        ArrayList<String> candidates = new ArrayList<>();
        TreeMap<String, String> treeDangling;
        try {
            br = new BufferedReader(new FileReader(trainingCorpus));
            treeDangling = new TreeMap<>();
            while (br.ready()) {
                String line;
                try {
                    line = br.readLine();
                    //Echo(line);
                    if (line == null || line.equals("")) {
                        sentencesCount++;
                        Iterator<String> it = candidates.iterator();
                        boolean covered = true;
                        if (!it.hasNext()) {
                            covered = false;
                        }
                        while (it.hasNext()) {
                            String c = it.next();
                            ///int candidato=Integer.parseInt(c);
                            boolean esCovered = true;
                            Set<String> idSet = treeDangling.keySet();
                            Iterator<String> ids = idSet.iterator();
                            while (ids.hasNext()) {
                                String id = ids.next();
                                String headN = treeDangling.get(id);
                                if (headN.equals(c)) {
                                    covered = false;
                                    esCovered = false;
                                }
                            }
                            if (esCovered) {
                                numberOfCovered++;
                            }
                        }
                        //if (covered) println(concat);
                        //concat="";

                        treeDangling = new TreeMap<>();
                        candidates = new ArrayList<>();
                        if (covered) {
                            contCoveredRoots++;
                        }
                        dangling = false;
                    }
                    if (line != null && !line.equals("")) {

                        //concat+=line+"\n";
                        String id = getColumn(line, 1);
                        String head = getColumn(line, 7);
                        treeDangling.put(id, head);
                        String form = getColumn(line, 2);
                        int contador = 0;
                        //if (head.equals("0")){
                        int intid = Integer.parseInt(id);
                        int inthead = Integer.parseInt(head);
                        contNonProjLocal = 0;
                        if (inthead < intid) {
                            //COVERED ROOTS!!
                            for (int i = inthead + 1; i < intid; i++) {
                                String numb = "" + i;
                                //Echo(numb);
                                String headOld = treeDangling.get(numb);
                                //Echo(headOld);
                                if (headOld != null) {
                                    int intHeadOld = Integer.parseInt(headOld);
                                    if (intHeadOld == 0) {
                                        if (headOld != null) {
                                            int hold = Integer.parseInt(headOld);
                                            if (hold < inthead) {
                                                contador++;
                                                contNonProjLocal++;
                                                candidates.add(numb);
                                            } else if (hold > intid) {
                                                contador++;
                                                contNonProjLocal++;
                                                candidates.add(numb);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (contNonProjLocal > 0) {
                        //Echo(line);
                        dangling = true;
                        contNonProjLocal = 0;
                        //return true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Echo(contCoveredRoots);
        //Echo(numbSentences);
        double frequency = (double) contCoveredRoots / (double) sentencesCount;

        //Echo(frequency);
        String freq = "" + frequency;
        if (freq.length() > 6) {
            freq = freq.substring(0, 6);
        }
        println("Frequency of Sentences with Covered Roots (without children): " + freq);
        println("Number of Covered Roots (without children): " + numberOfCovered);
        if (contCoveredRoots > 0) {
            return true;
        }
        return false;
    }

    public String extraDataCharacteristics() {
        String characteristics = "";
        int numTokens = 0;
        int sentencesCount = 0;
        cposEqPos = true;
        lemmaBlank = true;
        featsBlank = true;
        try {
            br = new BufferedReader(new FileReader(trainingCorpus));
            while (br.ready()) {
                String line;
                try {
                    line = br.readLine();
                    if ((line == null) || (line.equals(""))) {
                        sentencesCount++;
                    } else if (line != null && (!line.equals(""))) {
                        numTokens++;
                        String cpos = getColumn(line, 4);
                        String pos = getColumn(line, 5);
                        if (!pos.equals(cpos)) {
                            cposEqPos = false;
                        }
                        String lemma = getColumn(line, 3);
                        if (!lemma.equals("_")) {
                            lemmaBlank = false;
                        }
                        String feats = getColumn(line, 3);
                        if (!feats.equals("_")) {
                            //Echo(feats);
                            featsBlank = false;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        println("Your training set consists of " + sentencesCount + " sentences and " + numTokens + " tokens.");
        println("Testing Java Heap ... ");
        //JAVA HEAP HANDLING
        boolean validHeap = false;
        String javaHeap = "";
        AlgorithmTester at = new AlgorithmTester("lang", this, this.trainingCorpus, writer);
        int val = numTokens + 1;
        int nMaxTokens = val;
        //Echo(nMaxTokens);
        while (!validHeap) {
            javaHeap = calculateJavaHeapValue(nMaxTokens);
            generateDivision8020();
            /*
             * println("Trying with "+Optimizer.javaHeapValue);
			println(Optimizer.nMaxTokens);
             */
            validHeap = at.executeCovNonProjEagerDefaultJavaHeapTesting("CovingtonNonProjective.xml");
            if (!validHeap) {
                if (nMaxTokens > 700000) {
                    nMaxTokens = 700000;
                } else if (nMaxTokens > 650000) {
                    nMaxTokens = 650000;
                } else if (nMaxTokens > 500000) {
                    nMaxTokens = 500000;
                } else {
                    nMaxTokens -= 20000;
                }
                Optimizer.nMaxTokens = nMaxTokens;
                if (nMaxTokens < 0) {
                    println("MaltParser cannot work.");
                    System.exit(0);
                }
            }
            //Echo(nMaxTokens);
        }
        characteristics += javaHeap;
        if (nMaxTokens != val) {
            println("MaltOptimizer inferred that your system cannot allocate enough memory to run experiments with the whole corpus.");
            //Echo("MaltOptimizer will reduce the size of the training set.");
            double percentage = (double) nMaxTokens * 100 / numTokens;
            String perc = String.format(Optimizer.pattern, percentage);
            //Echo("The performance is going to be affected by this fact.");
            //Echo("We recommend the use of a system with higher memory allocation.");
            println("MaltOptimizer will reduce the size of the training set and use only " + nMaxTokens + " tokens (" + perc + "%).");
            Optimizer.nMaxTokens = nMaxTokens;
        }
        //Echo(Optimizer.nMaxTokens);
        //calculateJavaHeapValue(numTokens);
        //


        if (cposEqPos) {
            characteristics += "CPOSTAG and POSTAG are identical in your training set.\n";
            Optimizer.cposEqPos = true;
        } else {
            characteristics += "CPOSTAG and POSTAG are distinct in your training set.\n";
            Optimizer.cposEqPos = false;
        }
        if (lemmaBlank) {
            characteristics += "The LEMMA column is not used in your training set.\n";
            Optimizer.lemmaBlank = true;
        } else {
            characteristics += "The LEMMA column is used in your training set.\n";
            Optimizer.lemmaBlank = false;
        }
        if (featsBlank) {
            characteristics += "The FEATS column is not used in your training set.";
            Optimizer.featsBlank = true;
        } else {
            characteristics += "The FEATS column is used in your training set.";
            Optimizer.featsBlank = false;
        }
        CoNLLHandler.numbTokens = numTokens;
        CoNLLHandler.numSentences = sentencesCount;
        return characteristics;
    }

    private String calculateJavaHeapValue(int numTokens) {

        if (numTokens <= 70000) {
            Optimizer.javaHeapValue = "-Xmx2048m";
            return "MaltOptimizer has inferred that MaltParser needs at least 1.5Gb of free memory.\n";
        } else if (numTokens <= 100000) {
            Optimizer.javaHeapValue = "-Xmx2560m";
            return "MaltOptimizer has inferred that MaltParser needs at least 2Gb of free memory.\n";
        } else if (numTokens <= 150000) {
            Optimizer.javaHeapValue = "-Xmx3072m";
            return "MaltOptimizer has inferred that MaltParser needs at least 2.5Gb of free memory.\n";
        } /*
         * else if (numTokens<=250000) { Optimizer.javaHeapValue="-Xmx3072m";
         * return "MaltOptimizer has inferred that MaltParser needs at least 3Gb
         * of free memory.\n";
		}
         */ else if (numTokens <= 350000) {
            Optimizer.javaHeapValue = "-Xmx5120m";
            return "MaltOptimizer has inferred that MaltParser needs at least 4Gb of free memory.\n";
        } else if (numTokens <= 400000) {
            Optimizer.javaHeapValue = "-Xmx6144m";
            return "MaltOptimizer has inferred that MaltParser needs at least 5Gb of free memory.\n";
        } else if (numTokens <= 450000) {
            Optimizer.javaHeapValue = "-Xmx7168m";
            return "MaltOptimizer has inferred that MaltParser needs at least 6Gb of free memory.\n";
        } else if (numTokens <= 500000) {
            Optimizer.javaHeapValue = "-Xmx8192m";
            return "MaltOptimizer has inferred that MaltParser needs at least 7Gb of free memory.\n";
        } else if (numTokens <= 600000) {
            Optimizer.javaHeapValue = "-Xmx10240m";
            return "MaltOptimizer has inferred that MaltParser needs at least 8Gb of free memory.\n";
        } /*
         * else if (numTokens<=6500000) { Optimizer.javaHeapValue="-Xmx20480m";
         * return "MaltOptimizer has inferred that MaltParser needs at least 9Gb
         * of free memory.\n";
		}
         */ else if (numTokens <= 700000) {
            Optimizer.javaHeapValue = "-Xmx16384m";
            return "MaltOptimizer has inferred that MaltParser needs at least 16Gb of free memory.\n";
        }/*
         * else if (numTokens<=1200000){ Optimizer.javaHeapValue="-Xmx20480m";
         * return "MaltOptimizer has inferred that MaltParser needs at least
         * 16Gb of free memory.\n";
		}
         */ else {
            Optimizer.javaHeapValue = "-Xmx20480m";
            return "MaltOptimizer has inferred that MaltParser needs at least 20Gb of free memory.\n";
        }

        //AlgorithmTester at=new AlgorithmTester();
    }

    /**
     *
     * @return the percentage of non-projective trees in the training set
     *
     */
    public double projectiveOrNonProjective() {
        //CROSSING EDGES--->contador++
        int numberOfNonProjectives = 0;
        int treesCount = 0;
        int contador = 0;
        int anteriorHead = -1;
        int anteriorId = -1;
        int contProjectivities = 0;
        int numberOfArcs = 0;
        String cat = "";
        int cont = 0;
        tree = new TreeMap<>();
        invtree = new TreeMap<>();
        try {
            br = new BufferedReader(new FileReader(trainingCorpus));
            try {
                while (br.ready()) {
                    String line;
                    line = br.readLine();
                    if (line != null && line.equals("")) {
                        cont++;
                        treesCount++;
                        //Echo(cat);
                        cat = "";
                        boolean nonprojective = false;
                        if (cont > 0) {
                            nonprojective = inorder(invtree);
                        }

                        //if (contador>0){
                        if (nonprojective) {
                            //Echo("Non Projective");
                            numberOfNonProjectives++; //number of non-projectivities in the previous sentence
                            contador = 0;
                        }
                        tree = new TreeMap<>();
                        invtree = new TreeMap<>();
                    }
                    if (line != null && (!line.equals(""))) {
                        cat += getColumn(line, 2) + " ";
                        numberOfArcs++;
                        String head = getColumn(line, 7);
                        String id = getColumn(line, 1);
                        tree.put(id, head);
                        Integer ihead = Integer.parseInt(head);

                        ArrayList<String> children = invtree.get(ihead);
                        if (children == null) {
                            children = new ArrayList<>();
                        }
                        children.add(id);


                        invtree.put(ihead, children);
                    }
                }

                /*
                 * int intid = Integer.parseInt(id); int
                 * inthead=Integer.parseInt(head); int contNonProjLocal=0; if
                 * (inthead<intid) { //Se trata de encontrar una dependencia que
                 * vaya por delante de donde depende este. //Es decir, //John
                 * saw a dog yesterday which was a Yorkshire terrier. //Estamos
                 * en WAS (7) //Was tiene como HEAD DOG (4) (4<7) //Pero
                 * Yesterday (5) que está por delante de dog (4) tiene como HEAD
                 * Saw (2) //Los arcos de was a dog y de yesterday a Saw se
                 * CRUZAN... //Esto es lo que hay que detectar for(int
                 * i=inthead+1;i<intid;i++){ String numb=""+i; String
                 * headOld=tree.get(numb); if (headOld!=null) { int
                 * intHeadOld=Integer.parseInt(headOld);
                 *
                 * if (headOld!=null) { int hold=Integer.parseInt(headOld); if
                 * (hold<inthead) { contador++; contNonProjLocal++; } else if
                 * (hold>intid) { contador++; contNonProjLocal++; } } } } //if
                 * (contNonProjLocal==0){ for(int i=inthead+1;i<intid;i++){
                 * String numb=""+i; String headOld=tree.get(numb); if
                 * (headOld!=null) { int intHeadOld=Integer.parseInt(headOld);
                 * /*System.out.print("("+numb+",");
                 * println(headOld+")"); if (!head.equals("0") &&
                 * (!(headOld.equals(head)))){//&& (!(headOld.equals(id)))) {
                 * //Echo(line); boolean
                 * out=expandNodeRoot(intHeadOld,inthead); //is I "dominated" by
                 * head? (in a transitive way) //if i is dominated by head
                 * return false, else return true and it is a non projective arc
                 * //(numb is i) if (out) { //Echo("GOOD!");
                 * contador++; contNonProjLocal++; } } } } //}
                 *
                 *
                 *
                 * //}//
                 *
                 *
                 * /*if (headOld!=null) { int hold=Integer.parseInt(headOld); if
                 * (hold<inthead) { contador++; contNonProjLocal++; } else if
                 * (hold>intid) { contador++; contNonProjLocal++; } /*else {
                 * String headOldS=tree.get(headOld); //father of headOld if
                 * (headOldS!=null){ Integer holds=Integer.parseInt(headOldS);
                 * if (holds<inthead) { contador++; contNonProjLocal++; } /*else
                 * if (holds>intid) { contador++; contNonProjLocal++;
                 *
                 * }
                 * }
                 *
                 * }
                 * }
                 *
                 * if (contNonProjLocal>0) { contProjectivities++;
                 * //Echo("NonProjective Arc:"+getColumn(line,2)
                 * +" head:"+head); }
                 *
                 * //} /*else if (inthead>intid) { for(int i=inthead-1;i>1;i--){
                 * String numb=""+i; String headOld=tree.get(numb); if
                 * (headOld!=null) { int hold=Integer.parseInt(headOld); if
                 * (hold<inthead) { contador++; } }
                 *
                 * }
                 * }
                 * anteriorHead=inthead; anteriorId=intid;
							}
                 */
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        double value = 0;
        /*
         * this.numberOfTrees=numberOfTrees; if (this.numberOfTrees>0){
         * value=((double)numberOfNonProjectives/(double)numberOfTrees)*100;
		}
         */
        double k = ((double) numberOfNonProjectives / (double) treesCount) * 100;
        String ntrees = String.format(Optimizer.pattern, k);
        //Echo(k);
        //Echo(ntrees+"% of the trees contain non-projective arcs");
        this.numberOfTrees = treesCount;
        this.numbSentences = treesCount;
        /*
         * if (this.numberOfTrees>0){
         * value=((double)contProjectivities/(double)numberOfArcs)*100;
		}
         */
        //Echo(value);

        return k;//(numberOfNonProjectives/numberOfTrees)*100;
    }

    private boolean inorder(TreeMap<Integer, ArrayList<String>> tree2) {
        //leftmost child
        String cat = "";
        Set<Integer> keySet = tree2.keySet();
        //Echo(keySet);
        Integer head = 0;
        ArrayList<String> children = tree2.get(head);
        cat += getSubtree(tree2, head, children);

        //Echo(cat);
        StringTokenizer st = new StringTokenizer(cat);
        Integer anterior = 0;
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            Integer is = Integer.parseInt(s);
            Integer shouldBe = anterior + 1;
            if ((is != (shouldBe))) {
                if (cat.contains(shouldBe.toString())) {
                    return true;
                }
                //else 
                //	anterior=shouldBe;
            }
            anterior = is;
        }
        //Echo(cat);
        return false;
    }

    private String getSubtree(TreeMap<Integer, ArrayList<String>> tree2, Integer head, ArrayList<String> children) {
        String cat = "";

        //Echo("("+head+","+children+")");
        Iterator<String> itch = children.iterator();
        if (!itch.hasNext()) {
            cat += " " + head;
        } else {
            while (itch.hasNext()) {
                String child = itch.next();
                Integer intChild = Integer.parseInt(child);

                ArrayList<String> nietos = tree2.get(intChild);
                if (head < intChild && (!cat.contains(head.toString())) && (head != 0)) {
                    cat += " " + head;
                }
                if (nietos == null) {
                    cat += " " + child;
                    //if ((intChild==head-1))
                    if ((intChild == head - 1) && (!cat.contains(head.toString()))) {
                        cat += " " + head;
                    }
                } else {
                    /*
                     * if (head!=0) cat+=getSubtree(tree2,intChild,nietos) +
                     * head.toString(); else
                     */
                    cat += getSubtree(tree2, intChild, nietos);
                }
            }
        }
        if (!cat.contains(head.toString())) {
            cat += " " + head;
        }
        return cat;
    }

    private boolean expandNodeRoot(int head, int originalHead) {

        if (head == 0) {
            return true; //si llegas a ROOT antes que a originalHead el arco original es non-projective	
        }
        if (head == originalHead) {
            return false;
        }
        /*
         * String strHead=""+head; String nhead=tree.get(strHead);
         */

        int nIntHead = head;
        while (nIntHead != 0) {
            String strHeadN = "" + nIntHead;
            String nheadN = tree.get(strHeadN);
            //Echo("("+originalHead+","+nheadN+")");
            if (nheadN == null) {
                return true;
            }
            nIntHead = Integer.parseInt(nheadN);
            if (nIntHead == 0) {
                return true; //si llegas a ROOT antes que a originalHead el arco original es non-projective	
            }
            if (nIntHead == originalHead) {
                return false;
            }
        }
        /*
         * if (nhead!=null) { //Echo(nhead); int
         * nIntHead=Integer.parseInt(nhead); return expandNodeRoot(nIntHead,
         * originalHead);
		}
         */
        return true;
    }

    private String getColumn(String line, int columna) {
        StringTokenizer st = new StringTokenizer(line, "\t");
        String ret = "";
        for (int i = 0; i < columna; i++) {
            if (st.hasMoreTokens()) {
                ret = st.nextToken();
            }
        }
        return ret;
    }

    public String getHead0() {
        return head0;
    }

    public void generateDivision8020() {
        //CROSSING EDGES--->contador++
        //Echo("Generating training and test corpus");
        double percent80 = 0.8 * numberOfTrees;
        int numbLinesTrain = 0;
        int numbSentencesTest = 0;
        int numbSentencesTrain = 0;
        double percent20 = 0.2 * numberOfTrees;
        String concatTrain = "";
        String concatTest = "";
        int trainTimes = 0;
        String lastTest = "";
        String line;
        boolean trainTurn = false;

        int ntokens = 0;
        int ntokensTrain = 0;
        int ntokensTest = 0;
        //4 for train, 1 for test
        try {
            br = new BufferedReader(new FileReader(trainingCorpus));
            BufferedWriter bwTrain = new BufferedWriter(new FileWriter(training80));
            BufferedWriter bwTest = new BufferedWriter(new FileWriter(testing20));
            boolean metido = false;
            try {
                while (br.ready()) {
                    line = br.readLine();
                    ntokens++;
                    if (line != null && line.equals("")) {
                        ntokens++;
                        if (trainTimes < 4) {
                            trainTimes++;
                            trainTurn = true;
                            numbSentencesTrain++;
                        } else {
                            trainTimes = 0;
                            trainTurn = false;
                            numbSentencesTest++;
                        }
                        int total = numbSentencesTrain + numbSentencesTest;
                        /*
                         * if (total%150==0)
							System.out.print(".");
                         */
                    }
                    if (ntokens < Optimizer.nMaxTokens - 10000) {
                        if (trainTurn) {
                            if (numbLinesTrain > 0) {
                                //concatTrain+=line+"\n";
                                //Echo(line+"\n");
                                bwTrain.write(line + "\n");
                                ntokensTrain++;
                            }
                            numbLinesTrain++;
                        } else {
                            bwTest.write(line + "\n");
                            ntokensTest++;
                            lastTest = line;
                            //concatTest+=line+"\n";
                        }
                    } else {
                        if (!metido && !line.equals("")) {
                            if (trainTurn) {
                                if (numbLinesTrain > 0) {
                                    //concatTrain+=line+"\n";
                                    //Echo(line+"\n");
                                    bwTrain.write(line + "\n");
                                    ntokensTrain++;
                                }
                                numbLinesTrain++;
                            } else {
                                bwTest.write(line + "\n");
                                ntokensTrain++;
                                lastTest = line;
                                //concatTest+=line+"\n";
                            }
                        } else {
                            if (line.equals("")) {
                                metido = true;
                            }
                        }
                    }
                }

                //Echo("");

                //bwTrain.write(concatTrain);
                bwTrain.close();
                //bwTest.write(concatTest);
                //bwTest.write("\n");
                //bwTest.write("\n");
                if (!lastTest.equals("")) {
                    bwTest.write("\n");
                }
                bwTest.close();
                numbSentencesTest++;
                numbSentencesTrain--;
                /*
                 * println("Testing Set of "+numbSentencesTest +"
                 * sentences generated"); println("Training Set of
                 * "+numbSentencesTrain +" sentences generated");
                 */
                messageDivision = "Generated training set (" + ntokensTrain + " tokens) and devtest set (" + ntokensTest + " tokens).";

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } //Echo("\nCorpora generated");
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generate5FoldCrossCorpora() {

        if (!Optimizer.pseudoRandomizeSelection) {
            this.generate5FoldCrossCorporaNoPseudo();
        } else {
            this.generate5FoldCrossCorporaPseudo();
        }
    }

    public void generate5FoldCrossCorporaNoPseudo() {

        //Generate 5 Small Folds and 5 corresponding big folds. Save the names as follows.
        //fold_train1
        //fold_test1
        //fold_train2
        //fold_test2
        //fold_train3
        //...

        //CROSSING EDGES--->contador++
        //Echo("Generating training and test corpus");
        int numbLinesTrain = 0;
        int numbSentencesTest = 0;
        int numbSentencesTrain = 0;
        //this.n

        int numberOfLines = Optimizer.numbTokens + Optimizer.numbSentences;
        /*
         * println(Optimizer.numbTokens);
         * println(Optimizer.numbSentences);
         * println(numberOfLines);
         * /*println(numberOfLines);
		println(numSentences);
         */
        double percent20 = 0.2 * (numberOfLines);
        double percent40 = 0.4 * (numberOfLines);
        double percent60 = 0.6 * (numberOfLines);
        double percent80 = 0.8 * (numberOfLines);

        /*
         * println(percent20); println(percent40);
         * println(percent60); println(percent80);
		println(numberOfLines);
         */

        //1 empieza en 0 para test
        //2 empieza en 20% para test
        //3 empieza en 40% para test
        //4 empieza en 60% para test
        //5 empieza en 60% para test

        double limitInfTest = 0;
        double limitSupTest = percent20;
        for (int i = 1; i < 6; i++) {
            String training = "fold_train_" + i + ".conll";
            String test = "fold_test_" + i + ".conll";

            int trainTimes = 0;
            String lastTest = "";
            String line;
            boolean trainTurn = false;

            int ntokens = 0;
            //4 for train, 1 for test
            try {
                br = new BufferedReader(new FileReader(trainingCorpus));
                BufferedWriter bwTrain = new BufferedWriter(new FileWriter(training));
                BufferedWriter bwTest = new BufferedWriter(new FileWriter(test));
                try {

                    boolean testTurn = false;
                    if (i == 1) {
                        limitInfTest = 0;
                        limitSupTest = percent20;
                        testTurn = true;
                    }
                    if (i == 2) {
                        limitInfTest = percent20;
                        limitSupTest = percent40;
                    }
                    if (i == 3) {
                        limitInfTest = percent40;
                        limitSupTest = percent60;
                    }
                    if (i == 4) {
                        limitInfTest = percent60;
                        limitSupTest = percent80;
                    }
                    if (i == 5) {
                        limitInfTest = percent80;
                        limitSupTest = numberOfLines;
                    }
                    int nSentencesTest = 0;
                    String concat = "";
                    while (br.ready()) {
                        line = br.readLine();
                        ntokens++;
                        if (!line.equals("")) {
                            concat += line + "\n";
                        } else {
                            if (ntokens > limitInfTest && ntokens <= limitSupTest) {
                                bwTest.write(concat + "\n");
                                concat = "";
                            } else {
                                bwTrain.write(concat + "\n");
                                concat = "";
                            }
                        }
                    }
                    //Echo("");

                    //bwTrain.write(concatTrain);
                    bwTrain.close();
                    //bwTest.write(concatTest);
                    //bwTest.write("\n");
                    //bwTest.write("\n");
                    if (!lastTest.equals("")) {
                        bwTest.write("\n");
                    }
                    bwTest.close();
                    numbSentencesTest++;
                    numbSentencesTrain--;
                    //Echo("Test Fold "+i +"; Train Fold "+i+" generated");
                    messageDivision = "Test Fold " + i + "; Train Fold " + i + " generated";

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void generate5FoldCrossCorporaPseudo() {
        //CROSSING EDGES--->contador++
        int numbLinesTrain = 0;
        int numbSentencesTest = 0;
        int numbSentencesTrain = 0;

        int trainTimes = 0;
        String lastTest = "";
        String line;
        boolean trainTurn = false;
        boolean firstTime = true;

        int ntokens = 0;
        //4 for train, 1 for test
        for (int i = 1; i < 6; i++) {
            String training = "fold_train_" + i + ".conll";
            String test = "fold_test_" + i + ".conll";
            int contLinesTest = 0;
            try {
                br = new BufferedReader(new FileReader(trainingCorpus));
                BufferedWriter bwTrain = new BufferedWriter(new FileWriter(training));
                BufferedWriter bwTest = new BufferedWriter(new FileWriter(test));
                try {
                    while (br.ready()) {
                        line = br.readLine();
                        ntokens++;
                        if (line != null && line.equals("")) {
                            ntokens++;
                            if ((trainTimes < i) || (trainTimes > i)) {
                                trainTurn = true;
                                numbSentencesTrain++;
                                if (trainTimes == 5) {
                                    trainTimes = 0;
                                } else {
                                    trainTimes++;
                                }
                            } else { //trainTimes==i
                                trainTurn = false;
                                firstTime = true;
                                numbSentencesTest++;
                                if (trainTimes == 5) {
                                    trainTimes = 0;
                                } else {
                                    trainTimes++;
                                }
                            }
                        }
                        //if (ntokens<Optimizer.nMaxTokens) {
                        if (trainTurn) {
                            if (numbLinesTrain > 0) {
                                //concatTrain+=line+"\n";
                                bwTrain.write(line + "\n");
                            }
                            numbLinesTrain++;
                        } else {
                            if (contLinesTest == 0 && line.equals("")) {
                                firstTime = false;
                            } else {
                                bwTest.write(line + "\n");
                                lastTest = line;
                                contLinesTest++;
                            }
                            //concatTest+=line+"\n";
                        }
                        //}
                    }
                    //Echo("");

                    //bwTrain.write(concatTrain);
                    bwTrain.close();

                    if (!lastTest.equals("")) {
                        bwTest.write("\n");
                    }

                    bwTest.close();
                    numbSentencesTest++;
                    numbSentencesTrain--;
                    /*
                     * println("Testing Set of "+numbSentencesTest +"
                     * sentences generated"); println("Training Set
                     * of "+numbSentencesTrain +" sentences generated");
                     */
                    //Echo("FOLD: "+i+" generated.");

                    //messageDivision="Generated training set ("+numbSentencesTrain +" sentences) and test set ("+numbSentencesTest +" sentences).";

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } //Echo("\nCorpora generated");
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        println("Five cross-validation folds generated.");
    }

    public double evaluator(String testCorpus, String goldStandard) {
        switch (Optimizer.evaluationMeasure) {
            case "LAS":
                return evalLAS(testCorpus, goldStandard);
            case "UAS":
                return evalUAS(testCorpus, goldStandard);
            case "LCM":
                return evalLCM(testCorpus, goldStandard);
            case "UCM":
                return evalUCM(testCorpus, goldStandard);
        }
        return evalLAS(testCorpus, goldStandard);
    }

    public static double evalLAS(String testCorpus, String goldStandard) {
        return evalX(testCorpus, goldStandard, EvalType.LAS);
    }
    
    public double evalUAS(String testCorpus, String goldStandard) {
        return evalX(testCorpus, goldStandard, EvalType.UAS);
    }
    
    private enum EvalType { LAS, UAS }

    private static double evalX(String testCorpus, String goldStandard, EvalType type) {
        int total = 0;
        int ucorrect = 0;
        int lcorrect = 0;

        try {
            BufferedReader ss;
            try (BufferedReader gs = new BufferedReader(new FileReader(goldStandard))) {
                ss = new BufferedReader(new FileReader(testCorpus));
                String gl, sl;
                int lineCount = 0;
                while ((gl = gs.readLine()) != null) {
                    lineCount++;
                    gl = gl.trim();
                    sl = ss.readLine();
                    sl = sl.trim();

                    if (gl.length() == 0 && sl.length() != 0) {
                        throw new RuntimeException("GOLD sentence ended before Test at line " + lineCount);
                    }
                    if (gl.length() != 0 && sl.length() == 0) {
                        throw new RuntimeException("Test sentence ended before GOLD at line " + lineCount);
                    }

                    if (gl.length() == 0) {
                        continue; // EOS
                    }
                    String[] gtoks = gl.split("[\t]+");
                    String[] stoks = sl.split("[\t]+");

                    if (gtoks.length <= Math.min(6, 7)) {
                        gs.close();
                        ss.close();
                        return -1f;
                    }

                    int ghead = Integer.parseInt(gtoks[6]);
                    String glabel = normLabel(gtoks[7]);
                    int shead = Integer.parseInt(stoks[6]);
                    String slabel = normLabel(stoks[7]);

                    total++;
                    if (ghead == shead) {
                        ucorrect++;
                        if (glabel.equalsIgnoreCase(slabel)) {
                            lcorrect++;
                        }
                    }
                }
            }
            ss.close();
        }
        catch(IOException | RuntimeException e) {
            return -1f;
        }

        switch(type) {
            case LAS:
                return 100.0 * (double) lcorrect / (double) total;
            case UAS:
                return 100.0 * (double) ucorrect / (double) total;
        }
        
        return -1f;
    }
    
    public static String normLabel(String l) {
        if (l.equalsIgnoreCase("null")) {
            return "root";
        }
        return l.toLowerCase();
    }

    public double evalLCM(String testCorpus, String goldStandard) {

        BufferedReader tc;
        BufferedReader gs;

        double correctNodes = 0;
        double totNodes = 0;

        double totSentences = 0;
        double correctSentences = 0;

        //4 for train, 1 for test
        try {
            tc = new BufferedReader(new FileReader(testCorpus));
            gs = new BufferedReader(new FileReader(goldStandard));

            try {
                String lineTc;
                String lineGs;
                while (tc.ready() && gs.ready()) {
                    lineTc = tc.readLine();
                    lineGs = gs.readLine();
                    if ((lineTc != null && lineGs != null) && (!lineTc.equals("") && !lineGs.equals(""))) {
                        String tok = getColumn(lineTc, 2);
                        String headTc = getColumn(lineTc, 7);
                        String headGs = getColumn(lineGs, 7);
                        String deprelTc = getColumn(lineTc, 8);
                        String deprelGs = getColumn(lineGs, 8);
                        //if (!((tok.equals(","))||(tok.equals("."))||(tok.equals(":"))||(tok.equals("-"))||(tok.equals(";"))||(tok.equals("'"))||(tok.equals('"'))||(tok.equals("^"))||(tok.equals("-"))||(tok.equals("..."))||(tok.equals("_")))) {
                        if (!Optimizer.includePunctuation) {
                            if (tok.length() == 1) {
                                if (Character.getType(tok.charAt(0)) != 20 && Character.getType(tok.charAt(0)) != 21 && Character.getType(tok.charAt(0)) != 22 && Character.getType(tok.charAt(0)) != 23 && Character.getType(tok.charAt(0)) != 24 && Character.getType(tok.charAt(0)) != 29 && Character.getType(tok.charAt(0)) != 30) {
                                    /*
                                     * totNodes=totNodes+1.0; if
                                     * (headTc.equals(headGs) &&
                                     * deprelTc.equals(deprelGs)) {
                                     * correctNodes=correctNodes+1.0;
							}
                                     */
                                } else {
                                    totNodes = totNodes + 1.0;
                                    if (headTc.equals(headGs) && deprelTc.equals(deprelGs)) {
                                        correctNodes = correctNodes + 1.0;
                                    }
                                }
                            } else {
                                totNodes = totNodes + 1.0;
                                if (headTc.equals(headGs) && deprelTc.equals(deprelGs)) {
                                    correctNodes = correctNodes + 1.0;
                                }
                            }
                        } else {
                            totNodes = totNodes + 1.0;
                            if (headTc.equals(headGs) && deprelTc.equals(deprelGs)) {
                                correctNodes = correctNodes + 1.0;
                            }
                        }
                        //}
                    } else {
                        totSentences++;
                        if (totNodes == correctNodes) {
                            correctSentences++;
                        }
                        totNodes = 0;
                        correctNodes = 0;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        /*
         * println(correctNodes);
	println(totNodes);
         */
        println("\tLAS:" + evalLAS(testCorpus, goldStandard));
        return (correctSentences / totSentences) * 100;
    }

    public double evalUCM(String testCorpus, String goldStandard) {


        BufferedReader tc;
        BufferedReader gs;

        double correctNodes = 0;
        double totNodes = 0;

        double totSentences = 0;
        double correctSentences = 0;

        //4 for train, 1 for test
        try {
            tc = new BufferedReader(new FileReader(testCorpus));
            gs = new BufferedReader(new FileReader(goldStandard));

            try {
                String lineTc;
                String lineGs;
                while (tc.ready() && gs.ready()) {
                    lineTc = tc.readLine();
                    lineGs = gs.readLine();
                    if ((lineTc != null && lineGs != null) && (!lineTc.equals("") && !lineGs.equals(""))) {
                        String tok = getColumn(lineTc, 2);
                        String headTc = getColumn(lineTc, 7);
                        String headGs = getColumn(lineGs, 7);
                        String deprelTc = getColumn(lineTc, 8);
                        String deprelGs = getColumn(lineGs, 8);
                        //if (!((tok.equals(","))||(tok.equals("."))||(tok.equals(":"))||(tok.equals("-"))||(tok.equals(";"))||(tok.equals("'"))||(tok.equals('"'))||(tok.equals("^"))||(tok.equals("-"))||(tok.equals("..."))||(tok.equals("_")))) {
                        if (!Optimizer.includePunctuation) {
                            if (tok.length() == 1) {
                                if (Character.getType(tok.charAt(0)) != 20 && Character.getType(tok.charAt(0)) != 21 && Character.getType(tok.charAt(0)) != 22 && Character.getType(tok.charAt(0)) != 23 && Character.getType(tok.charAt(0)) != 24 && Character.getType(tok.charAt(0)) != 29 && Character.getType(tok.charAt(0)) != 30) {
                                    /*
                                     * totNodes=totNodes+1.0; if
                                     * (headTc.equals(headGs)) {
                                     * correctNodes=correctNodes+1.0;
							}
                                     */
                                } else {
                                    totNodes = totNodes + 1.0;
                                    if (headTc.equals(headGs)) {
                                        correctNodes = correctNodes + 1.0;
                                    }
                                }
                            } else {
                                totNodes = totNodes + 1.0;
                                if (headTc.equals(headGs)) {
                                    correctNodes = correctNodes + 1.0;
                                }
                            }
                        } else {
                            totNodes = totNodes + 1.0;
                            if (headTc.equals(headGs)) {
                                correctNodes = correctNodes + 1.0;
                            }
                        }
                    } else {
                        totSentences++;
                        if (totNodes == correctNodes) {
                            correctSentences++;
                        }
                        totNodes = 0;
                        correctNodes = 0;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        /*
         * println(correctNodes);
	println(totNodes);
         */
        println("\tLAS:" + evalLAS(testCorpus, goldStandard));
        return (correctSentences / totSentences) * 100;
    }

    public static void main(String[] args) {
        CoNLLHandler ch = new CoNLLHandler("goldtags.train_renumber_root_0");
        //ch.extraDataCharacteristics();
        //ch.generate5FoldCrossCorporaPseudo();
        System.out.println(ch.projectiveOrNonProjective());
        //Optimizer.includePunctuation=false;
        //double eval=ch.evaluator("spanish_cast3lb_train_test20.conll","outNivreEager.conll");
        //System.out.println(eval);
    }
    
    public void print(String text) {
        Optimizer.out.print(text);
        if (writer != null) {
            try {
                writer.write(text);
            }
            catch (Exception ex) {}
        }
    }
    
    public void println(String text) {
        Optimizer.out.println(text);
        if (writer != null) {
            try {
                writer.write(text + "\n");
            }
            catch (Exception ex) {}
        }
    }
}