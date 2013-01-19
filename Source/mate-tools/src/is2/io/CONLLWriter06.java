package is2.io;

import is2.data.SentenceData09;
import is2.parser.Parser;
import is2.util.DB;
import java.io.*;
import java.util.StringTokenizer;

public class CONLLWriter06 extends CONLLWriter {

    public CONLLWriter06() {
    }

    public static void main(String args[]) throws IOException {


        if (args.length == 2) {
            File f = new File(args[0]);
            File f2 = new File(args[1]);
            BufferedReader ir = new BufferedReader(new InputStreamReader(new FileInputStream(f), "ISO-8859"), 32768);
            BufferedWriter br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f2), "UTF-8"));;
            boolean found = false;
            boolean tab = false;
            while (true) {
                String l = ir.readLine();
                if (l == null) {
                    break;
                }
                String x = l.trim();
                if (x.endsWith("\t")) {
                    tab = true;
                }
                br.write(x);
                br.newLine();
                if (!l.equals(x)) {
                    found = true;
                }

            }
            ir.close();
            br.flush();
            br.close();

            if (found) {
                DB.println("found diff. found tab? " + tab);
            }
        } else if (args.length == 3) {
            File f1 = new File(args[1]);
            File f2 = new File(args[2]);

            BufferedReader ir1 = new BufferedReader(new InputStreamReader(new FileInputStream(f1), "ISO-8859"), 32768);
            BufferedReader ir2 = new BufferedReader(new InputStreamReader(new FileInputStream(f2), "UTF-8"), 32768);

            int line = 0, alltabs1 = 0, alltabs2 = 0;
            while (true) {
                String l1 = ir1.readLine();
                String l2 = ir2.readLine();

                if (l1 == null && l2 != null) {
                    DB.println("files do not end at the same line ");
                }
                if (l1 != null && l2 == null) {
                    DB.println("files do not end at the same line ");
                }
                if (l1 == null) {
                    break;
                }
                StringTokenizer t1 = new StringTokenizer(l1, "\t");
                StringTokenizer t2 = new StringTokenizer(l2, "\t");
                int tabs1 = 0;
                while (t1.hasMoreTokens()) {

                    t1.nextElement();
                    tabs1++;
                    alltabs1++;
                }

                int tabs2 = 0;
                while (t2.hasMoreTokens()) {

                    t2.nextElement();
                    tabs2++;
                    alltabs2++;
                }
                line++;
                if (tabs1 != tabs2) {
                    DB.println("number of tabs different in line " + line + " file1-tabs " + tabs1 + " file2-tabs " + tabs2);
                    System.exit(0);
                }


            }
            DB.println("checked lines " + line + " with tabs in file 1 " + alltabs1 + " in file2 " + alltabs2);

        } else {
            File f = new File(args[0]);
            String[] dir = f.list();
            for (String fx : dir) {
                BufferedReader ir = new BufferedReader(new InputStreamReader(new FileInputStream(args[0] + File.separatorChar + fx), "UTF-8"), 32768);
                Parser.out.println("check file " + fx);
                while (true) {
                    String l = ir.readLine();
                    if (l == null) {
                        break;
                    }
                    if (l.endsWith("\t")) {
                        DB.println("found tab in file " + fx);
                        break;
                    }
                }
                ir.close();
            }
        }

    }

    public CONLLWriter06(String file) {

        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CONLLWriter06(String outfile, int formatTask) {
        this(outfile);
    }

    @Override
    public void write(SentenceData09 inst) throws IOException {

        for (int i = 0; i < inst.length(); i++) {

            writer.write(Integer.toString(i + 1));
            writer.write('\t');	// id
            writer.write(inst.forms[i]);
            writer.write('\t'); 	// form

            if (inst.lemmas != null && inst.lemmas[i] != null) {
                writer.write(inst.lemmas[i]);
            } else {
                writer.write(DASH);									// lemma
            }
            writer.write('\t');

//				writer.write(DASH);	// cpos
//				writer.write('\t');


            writer.write(inst.gpos[i]); // cpos has to be included
            writer.write('\t');

            writer.write(inst.ppos[i]); // ppos
            writer.write('\t');


            if (inst.ofeats[i].isEmpty() || inst.ofeats[i].equals(" ")) {
                writer.write(DASH);
            } else {
                writer.write(inst.ofeats[i]);
            }
            writer.write('\t');


            //writer.write(DASH); writer.write('\t'); 					// pfeat

            writer.write(Integer.toString(inst.heads[i]));
            writer.write('\t');  // head

            if (inst.labels[i] != null) {
                writer.write(inst.labels[i]); 	// rel                  
            } else {
                writer.write(DASH);
            }
            writer.write('\t');

            writer.write(DASH);
            writer.write('\t');

            writer.write(DASH);
            writer.write('\t');

            writer.newLine();
        }
        writer.newLine();
    }
}