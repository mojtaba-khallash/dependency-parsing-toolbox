package is2.util;

import is2.parser.Parser;
import java.io.*;

public class ExtractParagraphs {

    /**
     *
     * @param args
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {

        if (args.length < 1) {
            Parser.out.println("Please provide a file name.");
            System.exit(0);
        }

        File file = new File(args[0]);
        file.isDirectory();
        String[] dirs = file.list();

        BufferedWriter write = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]), "UTF-8"), 32768);
        int cnt = 0;

        for (String fileName : dirs) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0] + fileName), "UTF-8"), 32768);

            int state = 0;

            String s;
            while ((s = reader.readLine()) != null) {

                if (s.startsWith("<P>") || s.startsWith("<p>")) {
                    state = 1; // paragraph start
                    continue;
                }




                if (s.startsWith("</P>") || s.startsWith("</p>")) {
                    state = 2; // paragraph end
                    write.newLine();
                }

                boolean lastNL = false;
                if (state == 1) {
                    String sp[] = s.split("\\. ");
                    for (String p : sp) {
                        write.write(p);
                        //				if (sp.length>1) write.newLine();
                    }
                    cnt++;
                }
            }

            //if (cnt>5000) break;

            reader.close();
        }
        write.flush();
        write.close();

        Parser.out.println("Extract " + cnt + " lines ");
    }
}