package ir.ac.iust.nlp.dependencyparser.parsing;

import ir.ac.iust.nlp.dependencyparser.BasePanel;
import ir.ac.iust.nlp.dependencyparser.DependencyParser;
import ir.ac.iust.nlp.dependencyparser.utility.enumeration.Flowchart;
import ir.ac.iust.nlp.dependencyparser.utility.enumeration.ParserType;
import ir.ac.iust.nlp.dependencyparser.utility.parsing.*;
import java.io.*;

/**
 *
 * @author Mojtaba Khallash
 */
public class RunnableParse implements Runnable {
    
    BasePanel target;
    ParserType type;
    PrintStream out = System.out;
    
    Settings settings;
    
    public RunnableParse(BasePanel target, ParserType type, PrintStream out, 
            Settings settings) {
        this.target = target;
        this.type = type;
        if (out != null) {
            this.out = out;
        }
        this.settings = settings;
    }
    
    @Override
    public void run() {
        settings.Chart = Flowchart.Parse;
        try {
            switch (type) {
                case MaltParser:
                    parseMalt();
                    break;
                case MSTParser:
                    parseMST();
                    break;
                case MateTools:
                    parseMate();
                    break;
                case ClearParser:
                    parseClear();
                    break;
            }
        } catch(StreamCorruptedException e) {
            out.println("\nInvalid trained model.");
        } catch (Exception e) {
            out.println("\nError: " + e.getMessage());
        }
        finally {
            if (target != null) {
                target.threadFinished();
            }
        }
    }
    
    private void parseMalt() throws Exception {
        MaltSettings st = new MaltSettings((MaltSettings)settings);
        Process p = null;
        try
        {
            st.preProcess();

            String ram = "";
            if (DependencyParser.maxRam.length() > 0) { ram += "-Xmx" + DependencyParser.maxRam + " "; }
            if (DependencyParser.minRam.length() > 0) { ram += "-Xms" + DependencyParser.minRam + " "; }
            p = Runtime.getRuntime().exec("java " + ram + "-jar lib" + File.separator + "maltParser.jar " + st.getTestParameters1());

            BufferedReader stdError = new BufferedReader(new InputStreamReader(
                    p.getErrorStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));

            String s;
            while ((s = stdError.readLine()) != null) {
                out.println(s);
            }
            while ((s = stdIn.readLine()) != null) {
                out.println(s);
            }
        }
        finally {
            if (p != null) {
                p.destroy();
            }
            
            st.postProcess();
        }
    }
    
    private void parseMST() throws Exception {
        MSTSettings st = (MSTSettings)settings;
        
        mstparser.DependencyParser.out = out;
        mstparser.DependencyParser.main(st.getParameters());
    }
    
    private void parseMate() throws Exception {
        MateSettings st = (MateSettings)settings;
        
        is2.parser.Parser.out = out;
        is2.parser.Parser.main(st.getParameters());
    }
    
    private void parseClear() throws Exception {
        ClearSettings st = (ClearSettings)settings;
        
        clear.engine.DepPredict.out = out;
        clear.engine.DepPredict.main(st.getParameters());
    };
}