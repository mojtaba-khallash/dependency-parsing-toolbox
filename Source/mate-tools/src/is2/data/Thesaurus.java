package is2.data;

import is2.parser.Parser;
import is2.util.DB;
import java.io.*;
import java.util.ArrayList;

/**
 * @author Dr. Bernd Bohnet, 28.10.2010
 *
 *
 */
final public class Thesaurus {

    public static final String LPATH = "LP";
    public static final String SPATH = "SP";
    // [word][p]  p = [0:long-path | 1:short-path]  
    final private int[][] word2path;

    public Thesaurus() {
        word2path = new int[0][];
    }

    /**
     * @param clusterFile
     * @param mf
     *
     */
    public Thesaurus(String clusterFile, IEncoderPlus mf, int ls) {

        final String REGEX = "\t";

        // register words
        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(clusterFile), "UTF-8"), 32768);

            int cnt = 0;
            String line;
            while ((line = inputReader.readLine()) != null) {

                cnt++;
                try {
                    String[] split = line.split(REGEX);
                    //		mf.register(LPATH, split[0].length()<ls?split[0]:split[0].substring(0,ls));
                    mf.register(PipeGen.WORD, split[0]);
                    mf.register(PipeGen.WORD, split[1]);
                } catch (Exception e) {
                    Parser.out.println("Error in cluster line " + cnt + " error: " + e.getMessage());
                }
            }
            Parser.out.println("read number of thesaury entries " + cnt);
            inputReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        word2path = new int[mf.getFeatureCounter().get(PipeGen.WORD)][];


        // insert words
        try {
            String line;
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(clusterFile), "UTF-8"), 32768);

            int startWd = -1;
            ArrayList<Integer> wrds = new ArrayList<>();
            while ((line = inputReader.readLine()) != null) {

                String[] split = line.split(REGEX);
                int wd = mf.getValue(PipeGen.WORD, split[0]);
                //	DB.println("wd "+wd+" "+startWd);
                if (startWd == wd) {
                    int thesaurusWrd = mf.getValue(PipeGen.WORD, split[1]);
                    if (thesaurusWrd != wd) {
                        wrds.add(thesaurusWrd);
                    }
                } else if (startWd != -1) {
                    int[] ths = new int[wrds.size()];
                    for (int k = 0; k < ths.length; k++) {
                        ths[k] = wrds.get(k);
                    }
                    word2path[startWd] = ths;
                    //	DB.println(""+wrds+" size "+ths.length);
                    wrds.clear();
                    int thesaurusWrd = mf.getValue(PipeGen.WORD, split[1]);
                    if (thesaurusWrd != wd) {
                        wrds.add(thesaurusWrd);
                    }
                }
                startWd = wd;
            }

            if (!wrds.isEmpty()) {
                // put rest of the words
                int[] ths = new int[wrds.size()];
                for (int k = 0; k < ths.length; k++) {
                    ths[k] = wrds.get(k);
                }
                word2path[startWd] = ths;
                //	DB.println(""+wrds+" size "+ths.length);
                wrds.clear();




            }

            inputReader.close();
            int fill = 0;
            for (int l = 0; l < word2path.length; l++) {
                if (word2path[l] != null) {
                    fill++;
                }
            }
            /*
             * for(int l = 0; l<word2path.length; l++ ){ if (word2path[l][1]!=0)
             * fillL++; if (word2path[l][1]<-1) Parser.out.println("lower
             * "+word2path[l][1]); }
             */
            Parser.out.println("filled " + fill + " of " + word2path.length);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Read the cluster
     *
     * @param dos
     * @throws IOException
     */
    public Thesaurus(DataInputStream dis) throws IOException {

        word2path = new int[dis.readInt()][];
        for (int i = 0; i < word2path.length; i++) {
            int len = dis.readInt();
            if (len > 0) {
                word2path[i] = new int[len];
                for (int j = 0; j < len; j++) {
                    word2path[i][j] = dis.readInt();

                }
            }

            word2path[i][0] = dis.readShort();
        }
        DB.println("Read cluster with " + word2path.length + " words ");
    }

    /**
     * Write the cluster
     *
     * @param dos
     * @throws IOException
     */
    public void write(DataOutputStream dos) throws IOException {

        dos.writeInt(word2path.length);
        for (int[] i : word2path) {
            dos.writeInt(i == null ? 0 : i.length);

            if (i != null) {
                for (int j = 0; j < i.length; j++) {

                    dos.writeInt(i[j]);
                }
            }
        }
    }

    /**
     * @param form the id of a word form
     * @return the short path to the word form in the cluster
     *
     * final public int getSP(int form) { if (word2path.length<form) return -1;
     * return word2path[form][0]; }
     */
    /**
     * get the long path to a word form in the cluster
     *
     * @param form the id of a word form
     * @return the long path to the word
     */
    final public int get(int form, int k) {
        if (word2path.length < form || word2path[form] == null) {
            return -1;
        }
        return word2path[form][k];
    }
}