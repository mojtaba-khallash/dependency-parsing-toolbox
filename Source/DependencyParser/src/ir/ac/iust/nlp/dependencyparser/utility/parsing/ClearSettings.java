package ir.ac.iust.nlp.dependencyparser.utility.parsing;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Mojtaba Khallash
 */
public class ClearSettings extends Settings {

    public String OptionsFile;
    public String GuidesFile;
    public int BootstrappingLevel = 2;

    public ClearSettings() {}
    
    public ClearSettings(ClearSettings settings) {
        super(settings);
        
        this.OptionsFile = settings.OptionsFile;
        this.GuidesFile = settings.GuidesFile;
    }
    
    @Override
    public String[] getTrainParameters() {
        List<String> pars = new LinkedList<>();
        
        pars.add("-m");
        pars.add(Model);
        pars.add("-c");
        pars.add(OptionsFile);
        pars.add("-t");
        pars.add(GuidesFile);
        pars.add("-n");
        pars.add(String.valueOf(BootstrappingLevel));
        pars.add("-i");
        pars.add(Input);

        return pars.toArray(new String[0]);
    }

    @Override
    public String[] getTestParameters() {
         List<String> pars = new LinkedList<>();
         
        pars.add("-m");
        pars.add(Model);
        pars.add("-c");
        pars.add(OptionsFile);
        pars.add("-i");
        pars.add(Input);
        pars.add("-o");
        pars.add(Output);
         
        return pars.toArray(new String[0]);
    }
}