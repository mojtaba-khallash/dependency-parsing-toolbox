package is2.util;

import is2.parser.Parser;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.StringTokenizer;

public class Split {

    /**
     * Splits a tokenized sentences into one word per line format:
     *
     * Input > I am an text . > Sentence two ...
     *
     * Output: I	_	_	_ ... am	_	_	_ ... ...
     *
     * @param args
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {

        if (args.length != 1) {
            Parser.out.println("Please provide a file name.");
            System.exit(0);
        }

        String filename = args[0];
//		Charset charset = Charset.forName("UTF-8");		

        FileInputStream in = new FileInputStream(filename);
        FileChannel channel = in.getChannel();
        CharsetDecoder decoder = Charset.defaultCharset().newDecoder();//charset.newDecoder();
        Reader infile = Channels.newReader(channel, decoder, 16 * 1024);
        BufferedReader bInfile = new BufferedReader(infile);

//		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(options.modelName)));


        String s;
        while ((s = bInfile.readLine()) != null) {


            // do the first tokens contain a colon?
            int colon = 0;
            for (int k = 0; k < 12; k++) {
                if (s.length() <= k) {
                    break;
                }
                if (s.charAt(k) == ':') {

                    colon++;
                    break;
                }
                if (s.charAt(k) == ' ') {
                    break;
                }
            }

            String prefix = colon > 0 ? s.substring(0, s.indexOf(":")) + "_" : "";

            if (colon > 0) {
                s = s.substring(s.indexOf(":") + 1);
            }

            StringTokenizer t = new StringTokenizer(s);
            int i = 1;
            boolean found = false;
            while (t.hasMoreTokens()) {
                found = true;
                String tk = t.nextToken();
                if (tk.contains("=")) {
                    continue;
                }
                Parser.out.print(prefix + i + "\t");
                Parser.out.print(tk);
                Parser.out.println("\t_\t_\t_\t_\t_\t_\t_\t_\t_\t_\t_\t_\t_");
                i++;
            }
            if (found) {
                Parser.out.println();
            }
        }
        bInfile.close();
    }
}