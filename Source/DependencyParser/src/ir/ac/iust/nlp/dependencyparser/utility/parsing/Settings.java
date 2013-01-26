package ir.ac.iust.nlp.dependencyparser.utility.parsing;

import ir.ac.iust.nlp.dependencyparser.utility.enumeration.Flowchart;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Mojtaba Khallash
 */
public abstract class Settings {

    public Flowchart Chart;
    public String Input;
    public String Output;
    public String Gold;
    public String Model;
    
    public Settings() {}
    
    public Settings(Settings settings) {
        this.Chart = settings.Chart;
        this.Input = settings.Input;
        this.Output = settings.Output;
        this.Gold = settings.Gold;
        this.Model = settings.Model;
    }
    
    public String[] getParameters() {
        switch (Chart) {
            case Train:
                return getTrainParameters();
            case Parse:
                return getTestParameters();
            case Eval:
                return getEvalParameters();
        }
        
        return null;
    }
    protected abstract String[] getTrainParameters();
    protected abstract String[] getTestParameters();
    
    private String[] getEvalParameters() {
        List<String> pars = new LinkedList<>();
        
        pars.add("eval");
        pars.add("gold-file:" + Gold);
        pars.add("output-file:" + Output);
        pars.add("format:CONLL");
        
        return pars.toArray(new String[0]);
    }
        
    protected void copyToDestination(String source, String destination) {
        File from = new File(source).getAbsoluteFile();
        File to = new File(destination);
        if (!from.equals(to)) {
            try { FileUtils.copyFile(from, to); } catch (IOException e) {}
        }
    }
}