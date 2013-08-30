package ir.ac.iust.nlp.dependencyparser.utility.parsing;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Mojtaba Khallash
 */
public class MSTSettings extends Settings {

    // CONLL|MST
    public String FileType = "CONLL";
    public String DecodeType = "non-proj";
    public String LossType = "punc";
    public int Order = 2;
    public int TrainingK = 1;
    public int Iteration = 10;
    
    // Separate Labling
    public boolean UseSeparateLabling = false;
    public int SeparateLablingCutoff;
    
    public boolean ComposeFeaturesWithPos = false;
    
    public MSTSettings() {}
    
    public MSTSettings(MSTSettings settings) {
        super(settings);
        
        this.FileType = settings.FileType;
        this.DecodeType = settings.DecodeType;
        this.LossType = settings.LossType;
        this.Order = settings.Order;
        this.TrainingK = settings.TrainingK;
        this.Iteration = settings.Iteration;
        
        this.UseSeparateLabling = settings.UseSeparateLabling;
        this.SeparateLablingCutoff = settings.SeparateLablingCutoff;
        
        this.ComposeFeaturesWithPos = settings.ComposeFeaturesWithPos;
    }
    
    @Override
    protected String[] getTrainParameters() {
        List<String> pars = new LinkedList<>();

        pars.add("format:" + FileType);
        pars.add("train");
        pars.add("train-file:" + Input);
        pars.add("model-name:" + Model);
        pars.add("iters:" + Iteration);
        pars.add("decode-type:" + DecodeType);
        pars.add("training-k:" + TrainingK);
        pars.add("loss-type:" + LossType);
        pars.add("order:" + Order);
        if (UseSeparateLabling == true) {
            pars.add("separate-lab");
            pars.add("separate-lab-cutoff:" + SeparateLablingCutoff);
        }
        if (ComposeFeaturesWithPos == true) {
            pars.add("compose-features-with-pos");
        }
        
        return pars.toArray(new String[0]);
    }
    
    @Override
    protected String[] getTestParameters() {
         List<String> pars = new LinkedList<>();
         
        pars.add("format:" + FileType);
         pars.add("test");
         pars.add("test-file:" + Input);
         pars.add("output-file:" + Output);
         pars.add("model-name:" + Model);
         pars.add("decode-type:" + DecodeType);
         pars.add("order:" + Order);
        if (UseSeparateLabling == true) {
            pars.add("separate-lab");
        }
                
        return pars.toArray(new String[0]);
    }
}