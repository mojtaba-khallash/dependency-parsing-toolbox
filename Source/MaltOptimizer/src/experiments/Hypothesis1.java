package experiments;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * @author miguel
 *
 */
public class Hypothesis1 {

    private String corpus;
    private boolean outputEnale = true;
    
    private ArrayList<ArrayList<String>> rightParentsCollection;
    public String getRightParents(int index) {
        return rightParentsCollection.get(index).toString();
    }
    public int getRightParentsCount(int index) {
        return rightParentsCollection.get(index).size();
    }
    
    private ArrayList<ArrayList<String>> rightArcsCollection;
    public String getRightArcs(int index) {
        return rightArcsCollection.get(index).toString();
    }
    public int getRightArcsCount(int index) {
        return rightArcsCollection.get(index).size();
    }
    
    private ArrayList<Double> proportionsCollection;
    public double getProportion(int index) {
        return proportionsCollection.get(index);
    }

    public double totalProportions;
    public double getTotalProportion() {
        return this.totalProportions;
    }

    public Hypothesis1() {
    }

    public void setCorpus(String corpus) {
        this.corpus = corpus;
    }

    /**
     * Given a sentence in conll format count the right arcs for the sentence
     * Given a sentence in conll format count the right parents for the sentence
     */
    public void generateStatistics() {

        try {
            BufferedReader br = new BufferedReader(new FileReader(corpus));
            try {
                int contStructures2 = 0;
                double totalProportion = 0.0;
                double numbSentences = 0.0;

                rightParentsCollection = new ArrayList<ArrayList<String>>();
                rightArcsCollection = new ArrayList<ArrayList<String>>();
                proportionsCollection = new ArrayList<Double>();
                        
                ArrayList<String> rightParents = new ArrayList<String>();
                ArrayList<String> rightArcs = new ArrayList<String>();

                boolean remain = false;
                while (br.ready()) {
                    String line = br.readLine();
                    if (!line.equals("")) {
                        remain = true;
                        StringTokenizer st = new StringTokenizer(line, "\t");

                        String id = "";
                        String parent;

                        int cont = 1;
                        while (st.hasMoreTokens()) {
                            String tok = st.nextToken();
                            if (cont == 1) {
                                id = tok;
                            }
                            if (cont == 7) {
                                parent = tok;

                                Integer parentInt = Integer.parseInt(parent);
                                Integer idInt = Integer.parseInt(id);

                                if (idInt > parentInt) {
                                    if (!rightParents.contains(parentInt.toString())) {
                                        if (parentInt != 0) {
                                            rightParents.add(parentInt.toString());
                                        }
                                    }
                                    if (!rightArcs.contains(idInt.toString())) {
                                        rightArcs.add(idInt.toString());
                                    }
                                }
                            }
                            cont++;
                        }
                    } else {
                        remain = false;
                        double proportion = 0.0;
                        
                        if (rightParents.size() > 0) {
                            proportion = (double) rightParents.size() / (double) rightArcs.size();
                        }
                        proportionsCollection.add(proportion);
                        
                        if (this.outputEnale == true) {
                            System.out.println("#rightParents (" + rightParents.size() + ")/#rightArcs (" + rightArcs.size() + ")=" + proportion);
                            System.out.println(rightArcs);
                            System.out.println(rightParents);
                        }
                        if (proportion == 1.0) {
                            contStructures2++;
                        }
                        totalProportion += proportion;
                        numbSentences += 1.0;
                        
                        rightParentsCollection.add(rightParents);
                        rightParents = new ArrayList<String>();
                        
                        rightArcsCollection.add(rightArcs);
                        rightArcs = new ArrayList<String>();
                    }
                }
                if (remain == true) {
                    double proportion = 0.0;

                    if (rightParents.size() > 0) {
                        proportion = (double) rightParents.size() / (double) rightArcs.size();
                    }
                    proportionsCollection.add(proportion);

                    if (this.outputEnale == true) {
                        System.out.println("#rightParents (" + rightParents.size() + ")/#rightArcs (" + rightArcs.size() + ")=" + proportion);
                        System.out.println(rightArcs);
                        System.out.println(rightParents);
                    }
                    if (proportion == 1.0) {
                        contStructures2++;
                    }
                    totalProportion += proportion;
                    numbSentences += 1.0;

                    rightParentsCollection.add(rightParents);

                    rightArcsCollection.add(rightArcs);
                }
                this.totalProportions = totalProportion / numbSentences;
                if (this.outputEnale == true) {
                    System.out.println("Exact Structures 2:" + contStructures2);
                    System.out.println("totalProp/numbSenteces=" + this.totalProportions);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static Hypothesis1 Run(String corpusPath, boolean outputEnale) {
        Hypothesis1 h1 = new Hypothesis1();
        h1.outputEnale = outputEnale;
        h1.setCorpus(corpusPath);
        h1.generateStatistics();
        return h1;
    }
}