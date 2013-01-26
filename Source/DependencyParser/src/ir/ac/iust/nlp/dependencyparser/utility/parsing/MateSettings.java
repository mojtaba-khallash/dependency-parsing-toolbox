package ir.ac.iust.nlp.dependencyparser.utility.parsing;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Mojtaba Khallash
 */
public class MateSettings extends Settings {
 
    public String DecodeType = "non-proj";
    public int Iteration = 10;
    public int Cores;
    // multiplicative
    // shift
    public String FeatureCreation = "multiplicative";
    public double NonProjectivityThreshold = 0.3;
    
    public MateSettings() {
        Runtime runtime = Runtime.getRuntime();
	Cores = runtime.availableProcessors();
    }
    
    public MateSettings(MateSettings settings) {
        super(settings);
        
        this.DecodeType = settings.DecodeType;
        this.Iteration = settings.Iteration;
        this.Cores = settings.Cores;
        this.FeatureCreation = settings.FeatureCreation;
    }
    
    @Override
    protected String[] getTrainParameters() {
        List<String> pars = new LinkedList<>();
        
        pars.add("-model");
        pars.add(Model);
        pars.add("-train");
        pars.add(Input);
        pars.add("-i");
        pars.add(String.valueOf(Iteration));
        pars.add("-decode");
        pars.add(DecodeType);
        pars.add("-decodeTH");
        pars.add(String.valueOf(NonProjectivityThreshold));
        pars.add("-feature_creation");
        pars.add(FeatureCreation);
        pars.add("-cores");
        pars.add(String.valueOf(Cores));
        pars.add("-format");
        pars.add("6");
        
        return pars.toArray(new String[0]);
    }
    
    @Override
    protected String[] getTestParameters() {
        List<String> pars = new LinkedList<>();
        
        pars.add("-model");
        pars.add(Model);
        pars.add("-test");
        pars.add(Input);
        pars.add("-out");
        pars.add(Output);
        pars.add("-decode");
        pars.add(DecodeType);
        pars.add("-decodeTH");
        pars.add(String.valueOf(NonProjectivityThreshold));
        pars.add("-feature_creation");
        pars.add(FeatureCreation);
        pars.add("-cores");
        pars.add(String.valueOf(Cores));
        pars.add("-format");
        pars.add("6");
        
        return pars.toArray(new String[0]);
    }    
}