package ir.ac.iust.nlp.dependencyparser.training;

import ir.ac.iust.nlp.dependencyparser.BasePanel;
import ir.ac.iust.nlp.dependencyparser.utility.enumeration.ParserType;
import ir.ac.iust.nlp.dependencyparser.utility.parsing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 *
 * @author Mojtaba Khallash
 */
public class RunnableTrain implements Runnable {
    
    BasePanel target;
    ParserType type;
    PrintStream out = System.out;
    
    Settings settings;
    
    public RunnableTrain(BasePanel target, ParserType type, PrintStream out, 
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
        try {
            switch(type) {
                case MaltParser:
                    runMalt();
                    break;
                case MSTParser:
                    runMST();
                    break;
                case MateTools:
                    runMate();
                    break;
                case ClearParser:
                    runClear();
                    break;
            }
        } catch (Exception e) {
        }
        finally {
            if (target != null) {
                target.threadFinished();
            }
        }
    }
    
    private void runMalt() throws Exception {
        MaltSettings st = (MaltSettings)settings;
        Process p = Runtime.getRuntime().exec("java -Xmx8000m -jar lib" + File.separator + "maltParser.jar " + st.getTrainParameters1());

        BufferedReader stdError = new BufferedReader(new InputStreamReader(
                p.getErrorStream()));
        
        String s;
        while ((s = stdError.readLine()) != null) {
            out.println(s);
        }
        p.destroy();
    }
    
    private void runMST() throws Exception {
        MSTSettings st = (MSTSettings)settings;
        
        mstparser.DependencyParser.out = out;
        mstparser.DependencyParser.main(st.getTrainParameters());
    }
    
    private void runMate() throws Exception {
        MateSettings st = (MateSettings)settings;
        
        is2.parser.Parser.out = out;
        is2.parser.Parser.main(st.getTrainParameters());
    }
    
    private void runClear() throws Exception {
        ClearSettings st = (ClearSettings)settings;
        
        clear.engine.DepTrain.out = out;
        clear.engine.DepTrain.main(st.getTrainParameters());
    }
}