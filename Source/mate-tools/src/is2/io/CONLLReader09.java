package is2.io;

import is2.data.Instances;
import is2.data.SentenceData09;
import is2.parser.Parser;
import is2.util.DB;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * This class reads files in the CONLL-09 format.
 *
 * @author Bernd Bohnet
 */
public class CONLLReader09 extends CONLLReader {

    public static final boolean NORMALIZE = true;
    public static final boolean NO_NORMALIZE = false;
    static public String joint = "";
    private int format = 0;

    public CONLLReader09(boolean normalize) {

        normalizeOn = normalize;
    }

    public CONLLReader09(String file) {
        lineNumber = 0;
        try {
            inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"), 32768);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public CONLLReader09(String file, boolean normalize) {
        this(file);
        normalizeOn = normalize;
    }

    /**
     * Sets the input format:
     *
     * CONLL09 is standard, ONE_LINE
     *
     * @param format the fomrat (see the constants starting with F_).
     */
    public void setInputFormat(int format) {
        this.format = format;
    }

    /**
     *
     */
    public CONLLReader09() {
    }

    /**
     * @param testfile
     * @param formatTask
     */
    public CONLLReader09(String testfile, int formatTask) {
        this(testfile);
    }

    @Override
    public SentenceData09 getNext() {

        if (F_ONE_LINE == format) {
            return getNextOneLine();
        } else {
            return getNextCoNLL09();
        }
    }

    /**
     * @return
     */
    private SentenceData09 getNextOneLine() {

        String line;
        int i = 0;
        try {

            line = inputReader.readLine();
            lineNumber++;

            if (line == null) {
                inputReader.close();
                return null;
            }

            String[] tokens = line.split(" ");
            int length = tokens.length;
            if (line.isEmpty()) {
                length = 0;
            }

            SentenceData09 it = new SentenceData09();

            it.forms = new String[length + 1];

            it.plemmas = new String[length + 1];
            //	it.ppos = new String[length+1];
            it.gpos = new String[length + 1];
            it.labels = new String[length + 1];
            it.heads = new int[length + 1];
            it.pheads = new int[length + 1];
            it.plabels = new String[length + 1];

            it.ppos = new String[length + 1];
            it.lemmas = new String[length + 1];
            it.fillp = new String[length + 1];
            it.feats = new String[length + 1][];
            it.ofeats = new String[length + 1];
            it.pfeats = new String[length + 1];
            it.id = new String[length + 1];

            it.forms[0] = ROOT;
            it.plemmas[0] = ROOT_LEMMA;
            it.fillp[0] = "N";
            it.lemmas[0] = ROOT_LEMMA;

            it.gpos[0] = ROOT_POS;
            it.ppos[0] = ROOT_POS;
            it.labels[0] = NO_TYPE;
            it.heads[0] = -1;
            it.plabels[0] = NO_TYPE;
            it.pheads[0] = -1;
            it.ofeats[0] = NO_TYPE;
            it.id[0] = "0";

            // root is 0 therefore start with 1

            for (i = 1; i <= length; i++) {

                it.id[i] = "" + i;

                it.forms[i] = this.normalizeOn ? normalize(tokens[i - 1]) : tokens[i - 1]; //normalize(
            }

            return it;

        } catch (Exception e) {
            Parser.out.println("\n!!! Error in input file sentence before line: " + lineNumber + " (in sentence line " + i + " ) " + e.toString());
            e.printStackTrace();
            System.exit(0);

            //throw new Exception();
            return null;
        }
    }

    /**
     * i.forms[heads[l]-1]+" "+rel+" "+ Read a instance
     *
     * @return a instance
     * @throws Exception
     */
    public SentenceData09 getNextCoNLL09() {

        String line;
        int i = 0;
        try {

            ArrayList<String[]> lineList = new ArrayList<>();

            line = inputReader.readLine();
            lineNumber++;

            while (line != null && line.length() == 0) {
                line = inputReader.readLine();
                lineNumber++;
                Parser.out.println("skip empty line at line " + lineNumber);
            }

            while (line != null && line.length() != 0 && !line.startsWith(STRING) && !line.startsWith(REGEX)) {
                lineList.add(line.split(REGEX));
                line = inputReader.readLine();
                lineNumber++;
            }

            int length = lineList.size();

            if (length == 0) {
                inputReader.close();
                return null;
            }

            SentenceData09 it = new SentenceData09();

            it.forms = new String[length + 1];

            it.plemmas = new String[length + 1];
            //	it.ppos = new String[length+1];
            it.gpos = new String[length + 1];
            it.labels = new String[length + 1];
            it.heads = new int[length + 1];
            it.pheads = new int[length + 1];
            it.plabels = new String[length + 1];

            it.ppos = new String[length + 1];
            it.lemmas = new String[length + 1];
            it.fillp = new String[length + 1];
            it.feats = new String[length + 1][];
            it.ofeats = new String[length + 1];
            it.pfeats = new String[length + 1];
            it.id = new String[length + 1];

            it.forms[0] = ROOT;
            it.plemmas[0] = ROOT_LEMMA;
            it.fillp[0] = "N";
            it.lemmas[0] = ROOT_LEMMA;

            it.gpos[0] = ROOT_POS;
            it.ppos[0] = ROOT_POS;
            it.labels[0] = NO_TYPE;
            it.heads[0] = -1;
            it.plabels[0] = NO_TYPE;
            it.pheads[0] = -1;
            it.ofeats[0] = NO_TYPE;
            it.id[0] = "0";

            // root is 0 therefore start with 1

            for (i = 1; i <= length; i++) {



                String[] info = lineList.get(i - 1);

                it.id[i] = info[0];
                it.forms[i] = info[1]; //normalize(
                if (info.length < 3) {
                    continue;
                }

                it.lemmas[i] = info[2];
                it.plemmas[i] = info[3];
                it.gpos[i] = info[4];

                if (info.length < 5) {
                    continue;
                }
                it.ppos[i] = info[5];//.split("\\|")[0];
                // feat 6


                // now we try underscore
                it.ofeats[i] = info[6].equals(CONLLWriter09.DASH) ? "_" : info[6];

                if (joint.length() > 0) {

                    StringBuilder b = new StringBuilder();
//					b.append(it.gpos[i]);
                    if (joint.startsWith("cz")) {

                        //	boolean caseFound =false;

                        String[] split = it.ofeats[i].split(PIPE);
                        //		if (!caseFound)
                        for (String s : split) {
                            if (s.startsWith("SubPOS")) {
                                if (b.length() > 0) {
                                    b.append("|");
                                }
                                b.append(s);
                            }
                        }

                        for (String s : split) {
                            if (s.startsWith("Cas")) {
                                if (b.length() > 0) {
                                    b.append("|");
                                }
                                b.append(s);
                            }

                        }

//						for(String s : split) {
//							if (s.startsWith("Num")) {
//								if (b.length()>0 )b.append("|");
//								b.append(s);
//							}
//						}



                    } else if (joint.contains("ger")) {

                        String[] split = it.ofeats[i].split(PIPE);
                        for (String s : split) {
                            if (s.matches("Nom|Acc|Dat|Gen")) {
                                if (b.length() > 0) {
                                    b.append("|");
                                }
                                b.append(s);
                            }
                            if (s.matches("Sg|Pl")) {
                                if (b.length() > 0) {
                                    b.append("|");
                                }
                                b.append(s);
                            }
                        }

                    } else {
                        String[] split = it.ofeats[i].split(PIPE);
                        for (String s : split) {
                            if (s.matches(joint)) {
                                b.append("|").append(s);
                            }
                        }
                    }
                    if (b.length() == 0) {
                        b.append("_");
                    }
                    it.ofeats[i] = b.toString();
                }

                if (info[7].equals(CONLLWriter09.DASH)) {
                    it.feats[i] = null;
                } else {
                    it.feats[i] = info[7].split(PIPE);
                    it.pfeats[i] = info[7];
                }

                if (info[8].equals(US)) {
                    it.heads[i] = -1;
                } else {
                    it.heads[i] = Integer.parseInt(info[8]);// head
                }
                it.pheads[i] = info[9].equals(US) ? it.pheads[i] = -1 : Integer.parseInt(info[9]);// head

                it.labels[i] = info[10];
                it.plabels[i] = info[11];
                it.fillp[i] = info[12];

                if (info.length > 13) {
                    if (!info[13].equals(US)) {
                        it.addPredicate(i, info[13]);
                    }
                    for (int k = 14; k < info.length; k++) {
                        it.addArgument(i, k - 14, info[k]);
                    }
                }
            }
            return it;

        } catch (IOException | NumberFormatException e) {
            Parser.out.println("\n!!! Error in input file sentence before line: " + lineNumber + " (in sentence line " + i + " ) " + e.toString());
            e.printStackTrace();
            System.exit(0);

            //throw new Exception();
            return null;
        }
    }

    @Override
    final public boolean insert(Instances is, SentenceData09 it) throws IOException {

        try {

            if (it == null) {
                inputReader.close();
                return false;
            }

            int i = is.createInstance09(it.length());

            for (int p = 0; p < it.length(); p++) {

                is.setForm(i, p, normalize(it.forms[p]));
                //	is.setFormOrg(i, p, it.forms[p]);
                is.setGPos(i, p, it.gpos[p]);

                //		Parser.out.println(""+is.gpos[i][p]);

                if (it.ppos[p] == null || it.ppos[p].equals(US)) {

                    is.setPPoss(i, p, it.gpos[p]);
                } else {
                    is.setPPoss(i, p, it.ppos[p]);
                }


                if (it.plemmas[p] == null || it.plemmas[p].equals(US)) {
                    is.setLemma(i, p, normalize(it.forms[p]));
                } else {
                    is.setLemma(i, p, normalize(it.plemmas[p]));
                }

                if (it.lemmas != null) {
                    if (it.lemmas[p] == null) { // ||it.org_lemmas[p].equals(US) that harms a lot the lemmatizer
                        is.setGLemma(i, p, it.plemmas[p]);
                    } else {
                        is.setGLemma(i, p, it.lemmas[p]);
                    }
                }


                if (it.feats != null && it.feats[p] != null) {
                    is.setFeats(i, p, it.feats[p]);
                }

                if (it.ofeats != null) {
                    is.setFeature(i, p, it.ofeats[p]);
                }
                if (it.pfeats != null) {
                    is.setPFeature(i, p, it.pfeats[p]);
                }


                is.setRel(i, p, it.labels[p]);
                if (it.plabels != null) {
                    is.setPRel(i, p, it.plabels[p]);
                }

                is.setHead(i, p, it.heads[p]);
                if (it.pheads != null) {
                    is.setPHead(i, p, it.pheads[p]);
                }

                if (it.fillp != null && it.fillp[p] != null && it.fillp[p].startsWith("Y")) {
                    is.pfill[i].set(p);
                } else {
                    is.pfill[i].clear(p);
                }
            }

            if (is.createSem(i, it)) {
                DB.println("count " + i + " len " + it.length());
                DB.println(it.printSem());
            }
        } catch (Exception e) {
            DB.println("head " + it);
            e.printStackTrace();
        }
        return true;
    }
}