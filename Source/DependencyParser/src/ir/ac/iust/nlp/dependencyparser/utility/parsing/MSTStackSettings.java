package ir.ac.iust.nlp.dependencyparser.utility.parsing;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Mojtaba Khallash
 */
public class MSTStackSettings extends MSTSettings {
    
    // 0 or 1
    public int Level;
    
    public int AugmentNParts = 5;
    
    public String AugmentedTrainFile;
    
    // Stack Features
    public boolean UsePredEdge = true;
    public boolean UsePrevSibling = true;
    public boolean UseNextSibling = true;
    public boolean UseGrandParents = true;
    public boolean UseAllchildren = true;
    public boolean UsePredHead = true;
    public boolean UseValency = true;
    
    public MSTStackSettings() {}
    
    public MSTStackSettings(MSTStackSettings settings) {
        super(settings);
        
        this.Level = settings.Level;
        this.AugmentNParts = settings.AugmentNParts;
        this.UsePredEdge = settings.UsePredEdge;
        this.UsePrevSibling = settings.UsePrevSibling;
        this.UseNextSibling = settings.UseNextSibling;
        this.UseGrandParents = settings.UseGrandParents;
        this.UseAllchildren = settings.UseAllchildren;
        this.UsePredHead = settings.UsePredHead;
        this.UseValency = settings.UseValency;
    }
    
    @Override
    protected String[] getTrainParameters() {
        List<String> pars = new LinkedList<>(Arrays.asList(super.getTrainParameters()));
        
        pars.add("stacked-level" + Level);
        if (Level == 0) {
            pars.add("augment-nparts:" + AugmentNParts);
            pars.add("output-file:" + AugmentedTrainFile);
        }
        else if (Level == 1) {
            pars.add("stackedfeat-pred-edge:" + (UsePredEdge == true ? "1" : "0"));
            pars.add("stackedfeat-prev-sibl:" + (UsePrevSibling == true ? "1" : "0"));
            pars.add("stackedfeat-next-sibl:" + (UseNextSibling == true ? "1" : "0"));
            pars.add("stackedfeat-grandparents:" + (UseGrandParents == true ? "1" : "0"));
            pars.add("stackedfeat-allchildren:" + (UseAllchildren == true ? "1" : "0"));
            pars.add("stackedfeat-pred-head:" + (UsePredHead == true ? "1" : "0"));
            pars.add("stackedfeat-valency:" + (UseValency == true ? "1" : "0"));
        }
        
        return pars.toArray(new String[0]);
    }
    
    @Override
    protected String[] getTestParameters() {
        List<String> pars = new LinkedList<>(Arrays.asList(super.getTestParameters()));
        
        pars.add("stacked-level" + Level);
        if (Level == 1) {
            pars.add("stackedfeat-pred-edge:" + (UsePredEdge == true ? "1" : "0"));
            pars.add("stackedfeat-prev-sibl:" + (UsePrevSibling == true ? "1" : "0"));
            pars.add("stackedfeat-next-sibl:" + (UseNextSibling == true ? "1" : "0"));
            pars.add("stackedfeat-grandparents:" + (UseGrandParents == true ? "1" : "0"));
            pars.add("stackedfeat-allchildren:" + (UseAllchildren == true ? "1" : "0"));
            pars.add("stackedfeat-pred-head:" + (UsePredHead == true ? "1" : "0"));
            pars.add("stackedfeat-valency:" + (UseValency == true ? "1" : "0"));
        }
        
        return pars.toArray(new String[0]);
    }
    
    public void preProcess() throws IOException {
        switch (Chart) {
            case Parse:
                Input = Gold;
                break;
        }
    }
    
    public void postProcess() throws IOException {
        switch (Chart) {
            case Parse:
                File model = new File(Model);
                if (model != null) {
                    FileUtils.forceDelete(model);
                }
                break;
        }
    }
}