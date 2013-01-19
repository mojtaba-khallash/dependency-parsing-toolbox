package is2.tag;

import is2.data.IEncoderPlus;
import is2.data.PipeGen;
import is2.parser.Parser;
import is2.util.DB;
import java.io.*;

/**
 * @author Dr. Bernd Bohnet, 07.01.2011
 *
 *
 */
public class Lexicon {

    public static final String FR = "FR", TAG = "TAG";
    final byte[][] word2tag;

    public Lexicon(byte[][] w2t) {

        word2tag = w2t;
    }

    public Lexicon(String clusterFile, IEncoderPlus mf) {

        final String REGEX = "\t";

        // register words
        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(clusterFile), "UTF-8"), 32768);

            int cnt = 0;
            String line;
            while ((line = inputReader.readLine()) != null) {

                try {
                    String[] split = line.split(REGEX);
                    //	int f = Integer.parseInt(split[2]);
//					if (f>2) {
                    cnt++;
                    mf.register(PipeGen.WORD, split[0]);
                    mf.register(TAG, split[1]); //tag

                    if (split.length > 1) {
                        mf.register(FR, split[1]); // frequency 
                    }//					}
                } catch (Exception e) {
                    Parser.out.println("Error in lexicon line " + cnt + " error: " + e.getMessage());
                }
            }
            Parser.out.println("read number of words from lexicon " + cnt);
            inputReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        word2tag = new byte[mf.getFeatureCounter().get(PipeGen.WORD)][1];
        // insert words
        try {
            String line;
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(clusterFile), "UTF-8"), 32768);

            while ((line = inputReader.readLine()) != null) {

                String[] split = line.split(REGEX);
                int w = mf.getValue(PipeGen.WORD, split[0]);
                if (w < 0) {
                    continue;
                }
                word2tag[w][0] = (byte) mf.getValue(TAG, split[1]);
                //	if (split.length>1) word2tag[w][1]= (byte)mf.getValue(FR, split[2]); // frequency 
            }
            inputReader.close();
            int fill = 0;
            for (int l = 0; l < word2tag.length; l++) {
                if (word2tag[l][0] != 0) {
                    fill++;
                }
            }
            Parser.out.println("filled " + fill + " of " + word2tag.length);

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
    public Lexicon(DataInputStream dis) throws IOException {

        word2tag = new byte[dis.readInt()][1];
        for (int i = 0; i < word2tag.length; i++) {
            word2tag[i][0] = dis.readByte();
//			word2tag[i][1]=dis.readByte();
        }
        DB.println("Read lexicon with " + word2tag.length + " words ");
    }

    /**
     * Write the cluster
     *
     * @param dos
     * @throws IOException
     */
    public void write(DataOutputStream dos) throws IOException {

        dos.writeInt(word2tag.length);
        for (byte[] i : word2tag) {
            dos.writeByte(i[0]);
//			dos.writeByte(i[1]);
        }
    }

    /**
     * @param form
     * @return
     */
    public int getTag(int form) {
        if (word2tag.length < form || form < 0) {
            return -1;
        }
        return word2tag[form][0];
    }

    /**
     * @param form
     * @return
     */
    public int getConf(int form) {
        if (word2tag.length < form || form < 0) {
            return -1;
        }
        return word2tag[form][1];
    }
}