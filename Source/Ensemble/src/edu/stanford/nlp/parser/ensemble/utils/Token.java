package edu.stanford.nlp.parser.ensemble.utils;

import org.maltparser.core.helper.SystemLogger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public class Token implements Dependency {

    int id;
    String form;
    String lemma;
    String pos;
    String cpos;
    String feats;
    int head;
    String label;
    double score;
    // List of models that proposed this dependency 
    List<Integer> models;
    // Max number of votes received for a dependency with this modifier (useful for the isolated dependency analysis)
    int maxVotes;

    public Token(int id, String f, String pos, int h, String lbl) {
        construct(id, f, pos, h, lbl, Double.NEGATIVE_INFINITY);
    }

    public Token(int id, String f, String pos, int h, String lbl, double score) {
        construct(id, f, pos, h, lbl, score);
    }

    public Token(int id, String f, String l, String cpos, String pos, String feats, int h, String lbl) {
        construct(id, f, cpos, h, lbl, Double.NEGATIVE_INFINITY);
        
        this.lemma = l;
        this.pos = pos;
        this.feats = feats;
    }
    
    public Token(int id, String f, String l, String cpos, String pos, String feats, int h, String lbl, double score) {
        construct(id, f, pos, h, lbl, score);
        
        this.lemma = l;
        this.pos = pos;
        this.feats = feats;
    }

    private void construct(int id, String f, String pos, int h, String l, double score) {
        this.id = id;
        this.form = f;
        this.lemma = f;
        this.pos = pos;
        this.cpos = pos;
        this.feats = "_";
        this.head = h;
        this.label = l;
        this.score = score;
        this.models = null;
        maxVotes = 0;
    }

    @Override
    public int head() {
        return head;
    }

    @Override
    public int mod() {
        return id;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public double score() {
        return score;
    }

    public void setScore(double s) {
        score = s;
    }

    void addModel(int m) {
        if (models == null) {
            models = new ArrayList<Integer>();
        }
        models.add(m);
    }

    List<Integer> getModels() {
        return models;
    }

    /**
     * Prints the token in CoNLL-X format
     */
    @Override
    public String toString() {
        StringBuilder os = new StringBuilder();
        os.append(id).append("\t").append(form).append("\t").append(lemma).append("\t").append(cpos).append("\t").append(pos).append("\t").append(feats).append("\t").append(head).append("\t").append(label.toUpperCase()).append("\t_\t_");
        return os.toString();
    }

    @Override
    public boolean sameDependency(Dependency other) {
        if (head == other.head() && label.equalsIgnoreCase(other.label())) {
            return true;
        }
        return false;
    }

    public static List<Token> readNextSentCoNLLX(BufferedReader is) throws IOException {
        List<Token> sent = new ArrayList<Token>();
        String line;

        while ((line = is.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) {
                break;
            }

            String[] toks = line.split("[\t]+");
            int id = Integer.parseInt(toks[0]);
            String form = toks[1];
            String lemma = toks[2];
            String cpos = toks[3];
            String pos = toks[4];
            String feats = toks[5];
            int head = Integer.parseInt(toks[6]);
            String label = normLabel(toks[7]);
            sent.add(new Token(id, form, lemma, cpos, pos, feats, head, label));
        }

        if (sent.isEmpty()) {
            return null;
        }
        return sent;
    }

    public static String normLabel(String l) {
        if (l.equalsIgnoreCase("null")) {
            return "root";
        }
        return l.toLowerCase();
    }

    public static List<Token> readNextSentCoNLL08(BufferedReader is) throws IOException {
        List<Token> sent = new ArrayList<Token>();
        String line;

        while ((line = is.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) {
                break;
            }

            String[] toks = line.split("[\t]+");
            int id = Integer.parseInt(toks[0]);
            String form = toks[5];
            String pos = toks[7];
            int head = Integer.parseInt(toks[8]);
            String label = normLabel(toks[9]);
            sent.add(new Token(id, form, pos, head, label));
        }

        if (sent.isEmpty()) {
            return null;
        }
        return sent;
    }

    public static void writeSentCoNLLX(List<Token> sentence, BufferedWriter os) throws IOException {
        for (Token tok : sentence) {
            os.write(tok.toString() + "\n");
        }
        os.newLine(); // blank line separate sentences
    }

    public static List<Token> mergeSentences(List<Token>[] sents) {
        List<Token> uniques = new ArrayList<Token>();
        for (int mod = 0; mod < sents[0].size(); mod++) {
            boolean[] used = new boolean[sents.length];
            List<Token> chosen = new ArrayList<Token>();
            int maxVotes = 0;

            for (int model = 0; model < sents.length; model++) {
                if (used[model] == true) {
                    continue;
                }
                Token crt = sents[model].get(mod);
                crt.addModel(model);
                int votes = 1;
                for (int i = model + 1; i < sents.length; i++) {
                    Token other = sents[i].get(mod);
                    if (crt.sameDependency(other)) {
                        crt.addModel(i);
                        used[i] = true;
                        votes++;
                    }
                }
                if (maxVotes < votes) {
                    maxVotes = votes;
                }
                chosen.add(crt);
            }

            for (Token u : chosen) {
                u.maxVotes = maxVotes;
                uniques.add(u);
            }
        }
        return uniques;
    }
    public static final Set<String> NO_ROOT_POS =
            new HashSet<String>(Arrays.asList(new String[]{".", ",", "``", "''", ":", "-LRB-", "-RRB-"}));

    public static void fixMultipleRoots(List<Token> deps) {
        List<Token> roots = new ArrayList<Token>();
        Token realRoot = null;
        for (int i = 0; i < deps.size(); i++) {
            if (deps.get(i).head == 0) {
                roots.add(deps.get(i));
                if (realRoot == null && !NO_ROOT_POS.contains(deps.get(i).pos)) {
                    realRoot = deps.get(i);
                }
            }
        }
        if (roots.size() > 1) {
            if (realRoot == null) {
                SystemLogger.logger().debug("Found no real root! Roots: " + roots);
                realRoot = roots.get(0);
            }
            for (Token w : roots) {
                if (w != realRoot) {
                    w.head = realRoot.id;
                }
            }
        }
    }
}