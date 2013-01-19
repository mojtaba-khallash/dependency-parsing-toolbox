package ir.ac.iust.nlp.dependencyparser.utility.parsing;

import ir.ac.iust.nlp.dependencyparser.utility.enumeration.Flowchart;

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
    
    public abstract String[] getTrainParameters();
    public abstract String[] getTestParameters();
}