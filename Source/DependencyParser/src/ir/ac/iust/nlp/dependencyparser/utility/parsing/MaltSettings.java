package ir.ac.iust.nlp.dependencyparser.utility.parsing;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

/**
 *
 * @author Mojtaba Khallash
 */
public class MaltSettings extends Settings {

    private String currentTempFolder = "";
    
    private String absoluteOutput;
    private String absoluteModel;
    
    public String OptionsFile;
    
    public String GuidesFile;
    
    public String WorkingDirectory;
    
    public MaltSettings() {}
    
    public MaltSettings(MaltSettings settings) {
        super(settings);
        
        this.absoluteModel = Model;
        this.OptionsFile = settings.OptionsFile;
        this.GuidesFile = settings.GuidesFile;
        this.WorkingDirectory = settings.WorkingDirectory;
    }
    
    @Override
    protected String[] getTrainParameters() {
        return null;
    }
    public String getTrainParameters1() {
        StringBuilder pars = new StringBuilder();

        // flowchart: Flow chart
        //  -learn	[Learn a Single MaltParser configuration]
        pars.append("-m ");
        pars.append("learn");

        // name: Configuration name
        pars.append(" -c ");
        pars.append(Model);

        // workingdir: Working directory
        pars.append(" -w ");
        pars.append(WorkingDirectory);

        // infile: Path to input file
        pars.append(" -i ");
        pars.append(WorkingDirectory).append(Input);

        pars.append(" -f ");
        pars.append(WorkingDirectory).append(OptionsFile);

        pars.append(" -F ");
        pars.append(WorkingDirectory).append(GuidesFile);

        return pars.toString();
    }
    
    @Override
    protected String[] getTestParameters() {
        return null;
    }
    public String getTestParameters1() {
        StringBuilder pars = new StringBuilder();

        // flowchart: Flow chart
        //  -parse	[Parse with a Single MaltParser configuration]
        pars.append("-m ");
        pars.append("parse");

        // name: Configuration name
        pars.append(" -c ");
        pars.append(Model);

        // workingdir: Working directory
        pars.append(" -w ");
        pars.append(WorkingDirectory);

        // infile: Path to input file
        pars.append(" -i ");
        pars.append(WorkingDirectory).append(Input);

        // outfile: Path to output file
        pars.append(" -o ");
        pars.append(WorkingDirectory).append(Output);

        return pars.toString();
    }
    
    public void preProcess() throws IOException {
        if (WorkingDirectory.charAt(WorkingDirectory.length() - 1) != File.separatorChar) {
            WorkingDirectory += File.separator;
        }
        currentTempFolder = String.valueOf(
                Calendar.getInstance().getTimeInMillis()) + File.separator;
        WorkingDirectory = WorkingDirectory + currentTempFolder;
        
        switch(Chart) {
            case Train:
                //** Options **//
                OptionsFile = copyToWorkingDirectory(OptionsFile);

                //** Guide **//
                GuidesFile = copyToWorkingDirectory(GuidesFile);

                //** Input **//
                // Ensure have an absolute path
                Input = copyToWorkingDirectory(Input);

                //** Model **//
                this.absoluteModel = Model;
                // Ensure have an absolute path
                Model = new File(Model).getName();
                break;
            case Parse:
                //** Input **//
                Input = copyToWorkingDirectory(Input);

                //** Model **//
                // Ensure have an absolute path
                this.absoluteOutput = Output;
                Output = new File(Output).getName();

                //** Model **//
                // Ensure have an absolute path
                this.absoluteModel = Model;
                Model = copyToWorkingDirectory(Model);
                break;
        }
    }
    
    public void postProcess() throws IOException {
        switch (Chart) {
            case Train:
                //** Model **//
                copyToDestination(WorkingDirectory + Model, absoluteModel);
                break;
            case Parse:
                //** Output **//
                copyToDestination(WorkingDirectory + Output, absoluteOutput);
                break;
        }
    }
    
    protected String copyToWorkingDirectory(String source) {
        File from = new File(source).getAbsoluteFile();
        String name = from.getName();
        copyToDestination(source, WorkingDirectory + name);
        return name;
    }
}