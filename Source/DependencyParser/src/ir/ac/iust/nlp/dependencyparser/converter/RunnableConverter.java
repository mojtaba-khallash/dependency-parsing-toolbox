package ir.ac.iust.nlp.dependencyparser.converter;

import ir.ac.iust.nlp.dependencyparser.enumeration.Format;
import java.io.*;

/**
 *
 * @author Mojtaba Khallash
 */
public class RunnableConverter implements Runnable {

    ConverterPanel target;
    
    PrintStream out = System.out;
    
    String inputPath;
    Format sourceFormat;
    
    String mappedFile;
    
    String outputPath;
    Format targetFormat;
    
    public RunnableConverter(ConverterPanel target, PrintStream out, 
            String inputPath, Format sourceFormat,
            String outputPath, Format targetFormat, String mapping) {
        this.target = target;
        this.out = out;
        
        this.inputPath = inputPath;
        this.sourceFormat = sourceFormat;
        this.outputPath = outputPath;
        this.targetFormat = targetFormat;
        this.mappedFile = mapping;
    }

    @Override
    public void run() {
        try {
            out.println("Start Converting " + sourceFormat.toString() + " to " + targetFormat.toString());
            switch(sourceFormat) {
                case CONLL:
                    switch (targetFormat) {
                        case MST:
                            runScript("conll2mst.py");
                            break;
                        default:
                            out.println("This convert not support.");
                            break;
                    }
                    break;
                case MST:
                    switch (targetFormat) {
                        case CONLL:
                            runScript("mst2conll.py");
                            break;
                        default:
                            out.println("This convert not support.");
                            break;
                    }
                    break;
                case Tiger:
                    switch (targetFormat) {
                        case Malt_XML:
                            runMaltConverter("tiger2malt");
                            break;
                        case Malt_TAB:
                            runMaltConverter("tiger2tab");
                            break;
                        default:
                            out.println("This convert not support.");
                            break;
                    }
                    break;
                case Malt_XML:
                    switch (targetFormat) {
                        case Tiger:
                            runMaltConverter("malt2tiger");
                            break;
                        case Malt_TAB:
                            runMaltConverter("malt2tab");
                            break;
                        case Malt_XML:
                            runMaltConverter("malt2malt");
                            break;
                        default:
                            out.println("This convert not support.");
                            break;
                    }
                    break;
                case Malt_TAB:
                    switch (targetFormat) {
                        case Malt_XML:
                            runMaltConverter("tab2malt");
                            break;
                        case Tiger:
                            runMaltConverter("tab2tiger");
                            break;
                        case Malt_TAB:
                            runMaltConverter("tab2tab");
                            break;
                        default:
                            out.println("This convert not support.");
                            break;
                    }
                    break;
                default:
                    out.println("This convert not support.");
                    break;
            }
            out.println("\nFinished.");
        } finally {
            if (target != null) {
                target.threadFinished();
            }
        }
    }
     
    private void runScript(String converterName) {
        boolean converterExist = false;
        try {
            File val = new File(converterName);
            converterExist = val.exists();
            if (!converterExist) {
                BufferedWriter bwValidateFormat;
                try {
                    bwValidateFormat = new BufferedWriter(new FileWriter(converterName));
                    bwValidateFormat.write(ConverterGenerator.generateConverter(converterName));
                    bwValidateFormat.close();
                } catch (IOException e) {}
            }
            
            Process pro = Runtime.getRuntime().exec(
                    "java -Dfile.encoding=UTF8 -jar lib" + File.separator + 
                    "jython.jar " + converterName + " " + inputPath);
            
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(
                pro.getInputStream(),"UTF8"));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(
                pro.getErrorStream(),"UTF8"));
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                     new FileOutputStream(outputPath, true), "UTF-8"))) {
                String s;
                int i = 0;
                while ((s = stdInput.readLine()) != null) {
                    if (s.length() == 0) {
                        i++;
                        out.print(i + " ");
                        if (i % 21 == 0) {
                            out.println();
                        }
                    }
                    writer.write(s + "\n");
                }
                while ((s = stdError.readLine()) != null) {
                    System.out.println(s);
                }
                writer.close();
            }
            pro.destroy();
        } catch(Exception ex){}
        finally {
            if (converterExist == false) {
                new File(converterName).delete();
            }
        }
    }
    
    private void runMaltConverter(String converterName) {
        try {
            Process pro = Runtime.getRuntime().exec("java -Dfile.encoding=UTF8 -jar lib" + File.separator + "MaltConverter.jar " + 
                converterName + " " +       // conversion
                mappedFile + " " +          // mapfile
                inputPath + " " +           // infile
                outputPath);                // outfile
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(
                pro.getInputStream(),"UTF8"));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(
                pro.getErrorStream(),"UTF8"));

            String s;
            while ((s = stdInput.readLine()) != null) {
                out.println(s);
            }
            while ((s = stdError.readLine()) != null) {
                out.println(s);
            }
            pro.destroy();
       } catch(Exception ex){}
    }
}