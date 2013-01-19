///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2008 Carnegie Mellon University and 
// (C) 2007 University of Texas at Austin and (C) 2005
// University of Pennsylvania and Copyright (C) 2002, 2003 University
// of Massachusetts Amherst, Department of Computer Science.
//
// This software is licensed under the terms of the Common Public
// License, Version 1.0 or (at your option) any subsequent version.
// 
// The license is approved by the Open Source Initiative, and is
// available from their website at http://www.opensource.org.
///////////////////////////////////////////////////////////////////////////////
package mstparser.mallet;

import edu.umass.cs.mallet.base.classify.Classification;
import edu.umass.cs.mallet.base.classify.Classifier;
import edu.umass.cs.mallet.base.classify.MaxEntTrainer;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.pipe.SerialPipes;
import edu.umass.cs.mallet.base.pipe.Token2FeatureVector;
import edu.umass.cs.mallet.base.types.Alphabet;
import edu.umass.cs.mallet.base.types.*;
import gnu.trove.TObjectIntHashMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.Date;
import mstparser.FeatureVector;
import mstparser.*;

/**
 * @author Dipanjan Das 6/4/08 dipanjan@cs.cmu.edu
 *
 * Adapted from code by Ryan McDonald (ryanmcd@google.com)
 *
 */
public class LabelClassifier {

    private ParserOptions options;
    private int[] instanceLengths;
    private int[] ignore;
    private String trainFile;
    private File trainForest;
    private DependencyParser parentParser;
    private DependencyPipe pipe;
    private Parameters params;
    public TObjectIntHashMap predCounts = null;
    private int cutOff = 0;

    /*
     * for training
     */
    public LabelClassifier(ParserOptions options, int[] instanceLengths, int[] ignore, String trainFile, File trainForest, DependencyParser parser, DependencyPipe pipe) {
        this.options = options;
        this.instanceLengths = instanceLengths;
        this.ignore = ignore;
        this.trainFile = trainFile;
        this.trainForest = trainForest;
        this.parentParser = parser;
        this.pipe = pipe;
        params = new Parameters(pipe.dataAlphabet.size());
        cutOff = options.separateLabCutOff;
    }

    /*
     * for test
     */
    public LabelClassifier(ParserOptions options) {
        cutOff = options.separateLabCutOff;
    }

    /*
     * lab and par do not have the root word
     *
     */
    public String[] outputLabels(Classifier testClassifier, String[] toks, String[] pos, String[] lab, int[] par, String[] depPred, int[] headPred, DependencyInstance instance) {
        Pipe p = (SerialPipes) testClassifier.getInstancePipe();
        String[] newLab = new String[lab.length + 1];
        newLab[0] = newLab[1];
        int[] newPar = new int[lab.length + 1];
        newPar[0] = -1;
        for (int i = 1; i < newLab.length; i++) {
            newLab[i] = lab[i - 1];
            newPar[i] = par[i - 1];
        }

        for (int i = 1; i < newPar.length; i++) {
            String line = newLab[i] + " " + MalletFeatures.getFeats(toks, pos, newLab, newPar, depPred, headPred, i).trim();
            Token t = new Token("");
            String[] tokens = line.split(" ");
            for (int j = 1; j < tokens.length; j++) {
                addFeature(t, tokens[j], 1.0);
            }
            PrintStream ps = DependencyParser.out;
            PrintStream err = System.err;
            System.setErr(ps);
            LabelSequence target = new LabelSequence((LabelAlphabet) p.getTargetAlphabet(), 1);
            target.add(tokens[0]);
            Instance ins = new Instance(t, target.getLabelAtPosition(0), null, null, p);
            Classification c = testClassifier.classify(ins);
            lab[i - 1] = c.getLabeling().getBestLabel().toString();
            System.setErr(err);
        }
        return lab;
    }

    public void passThroughInstancesToSelectFeatureThresholds() throws Exception {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(trainForest));
        predCounts = new TObjectIntHashMap();
        int numInstances = instanceLengths.length;
        int j = 0;
        DependencyParser.out.println("Passing through instances to select feature thresholds...");
        DependencyParser.out.print("  [");
        long startTime = (new Date()).getTime();
        for (int i = 0; i < numInstances; i++) {
            if ((i + 1) % 500 == 0) {
                DependencyParser.out.print((i + 1) + ",");
            }
            //afm 03-06-08
            int length = instanceLengths[i];

            // Get production crap.
            FeatureVector[][][] fvs = new FeatureVector[length][length][2];
            double[][][] probs = new double[length][length][2];
            FeatureVector[][][][] nt_fvs = new FeatureVector[length][pipe.types.length][2][2];
            double[][][][] nt_probs = new double[length][pipe.types.length][2][2];
            FeatureVector[][][] fvs_trips = new FeatureVector[length][length][length];
            double[][][] probs_trips = new double[length][length][length];
            FeatureVector[][][] fvs_sibs = new FeatureVector[length][length][2];
            double[][][] probs_sibs = new double[length][length][2];

            DependencyInstance inst;
            if (options.secondOrder) {
                inst = ((DependencyPipe2O) pipe).readInstance(in, length, fvs, probs,
                        fvs_trips, probs_trips,
                        fvs_sibs, probs_sibs,
                        nt_fvs, nt_probs, params);
            } else {
                inst = pipe.readInstance(in, length, fvs, probs, nt_fvs, nt_probs, params);
            }
            if (ignore[i] != 0) {
                continue;
            }
            updatePredCounts(inst);
            j++;
        }
        in.close();
        Object[] keys = predCounts.keys();
        for (int i = 0; i < keys.length; i++) {
            if (predCounts.get(keys[i]) <= cutOff) {
                predCounts.remove(keys[i]);
            }
        }
        long endTime = (new Date()).getTime();
        DependencyParser.out.println(" |Time:" + (endTime - startTime) + "]");
        DependencyParser.out.println("No of active features:" + predCounts.size());
    }

    public void updatePredCounts(DependencyInstance inst) {
        String[] toks = inst.forms;
        String[] pos = inst.postags;
        String[] lab = inst.deprels;
        int[] par = inst.heads;
        String[] dep_pred = null;
        int[] heads_pred = null;

        if (options.stackedLevel1) {
            dep_pred = inst.deprels_pred;
            heads_pred = inst.heads_pred;
        }

        for (int i = 1; i < par.length; i++) {
            String line = lab[i] + " " + MalletFeatures.getFeats(toks, pos, lab, par, dep_pred, heads_pred, i).trim();
            String[] tokens = line.split(" ");
            for (int j = 1; j < tokens.length; j++) {
                if (predCounts.contains(tokens[j])) {
                    predCounts.increment(tokens[j]);
                } else {
                    predCounts.put(tokens[j], 1);
                }
            }
        }
    }

    public void addFeatureVectorsForInstance(DependencyInstance inst, InstanceList trainData, Pipe p) {
        String[] toks = inst.forms;
        String[] pos = inst.postags;
        String[] lab = inst.deprels;
        int[] par = inst.heads;
        String[] dep_pred = null;
        int[] heads_pred = null;

        if (options.stackedLevel1) {
            dep_pred = inst.deprels_pred;
            heads_pred = inst.heads_pred;
        }

        for (int i = 1; i < par.length; i++) {
            String line = lab[i] + " " + MalletFeatures.getFeats(toks, pos, lab, par, dep_pred, heads_pred, i).trim();
            Token t = new Token("");
            String[] tokens = line.split(" ");
            for (int j = 1; j < tokens.length; j++) {
                addFeature(t, tokens[j], 1.0);
            }
            LabelSequence target = new LabelSequence((LabelAlphabet) p.getTargetAlphabet(), 1);
            target.add(tokens[0]);
            Instance ins = new Instance(t, target.getLabelAtPosition(0), null, null, p);
            trainData.add(ins);
        }
    }

    public void addFeature(Token t, String f, double v) {
        if (predCounts == null || predCounts.size() == 0 || predCounts.contains(f)) {
            t.setFeatureValue(f, v);
        }
    }

    public Classifier trainClassifier(int numIters) throws Exception {
        Pipe p = new SerialPipes(new Pipe[]{
                    new Token2FeatureVector(true, true)
                });

        p.setDataAlphabet(new Alphabet());
        p.setTargetAlphabet(new LabelAlphabet());

        InstanceList trainData = new InstanceList(p);
        passThroughInstancesToSelectFeatureThresholds();
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(trainForest));
        int numInstances = instanceLengths.length;
        int j = 0;
        DependencyParser.out.println("Storing feature vectors in Mallet data structure...");
        DependencyParser.out.print("  [");
        long startTime = (new Date()).getTime();
        for (int i = 0; i < numInstances; i++) {
            if ((i + 1) % 500 == 0) {
                DependencyParser.out.print((i + 1) + ",");
            }
            int length = instanceLengths[i];

            // Get production crap.
            FeatureVector[][][] fvs = new FeatureVector[length][length][2];
            double[][][] probs = new double[length][length][2];
            FeatureVector[][][][] nt_fvs = new FeatureVector[length][pipe.types.length][2][2];
            double[][][][] nt_probs = new double[length][pipe.types.length][2][2];
            FeatureVector[][][] fvs_trips = new FeatureVector[length][length][length];
            double[][][] probs_trips = new double[length][length][length];
            FeatureVector[][][] fvs_sibs = new FeatureVector[length][length][2];
            double[][][] probs_sibs = new double[length][length][2];

            DependencyInstance inst;
            if (options.secondOrder) {
                inst = ((DependencyPipe2O) pipe).readInstance(in, length, fvs, probs,
                        fvs_trips, probs_trips,
                        fvs_sibs, probs_sibs,
                        nt_fvs, nt_probs, params);
            } else {
                inst = pipe.readInstance(in, length, fvs, probs, nt_fvs, nt_probs, params);
            }
            if (ignore[i] != 0) {
                continue;
            }
            addFeatureVectorsForInstance(inst, trainData, p);
            j++;
        }
        in.close();
        predCounts = null;
        Alphabet dataAlph = p.getDataAlphabet();
        Alphabet tAlph = p.getTargetAlphabet();
        long endTime = (new Date()).getTime();
        DependencyParser.out.println(" |Time:" + (endTime - startTime) + "]");
        DependencyParser.out.println("Number of labels: " + tAlph.size());
        DependencyParser.out.println("Number of Predicates: " + dataAlph.size());
        DependencyParser.out.println("Training size: " + trainData.size());
        MaxEntTrainer met = new MaxEntTrainer(1.0);
        met.setNumIterations(numIters);
        PrintStream ps = DependencyParser.out;
        PrintStream err = System.err;
        System.setErr(ps);
        Classifier classifier = met.train(trainData);
        dataAlph.stopGrowth();
        ((LabelAlphabet) p.getTargetAlphabet()).stopGrowth();
        DependencyParser.out.println("Done Training Labeler\nReturning classifier");
        System.setErr(err);
        return classifier;
    }
}