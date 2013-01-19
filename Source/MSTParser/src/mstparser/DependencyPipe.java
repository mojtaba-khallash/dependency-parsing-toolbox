package mstparser;

import gnu.trove.TIntArrayList;
import java.io.*;
import mstparser.io.DependencyReader;
import mstparser.io.DependencyWriter;

public class DependencyPipe {

    public Alphabet dataAlphabet;
    public Alphabet typeAlphabet;
    private DependencyReader depReader;
    private DependencyWriter depWriter;
    public String[] types;
    public int[] typesInt;
    public boolean labeled = false;
    private boolean isCONLL = true;
    public boolean separateLab = false; // afm 06-03-08
    private ParserOptions options;

    public DependencyPipe(ParserOptions options) throws IOException {
        this.options = options;

        if (!options.format.equals("CONLL") && !options.format.equals("CONLL2008")) { // afm 04-04-2008 --- Added second part (CONLL2008)
            isCONLL = false;
        }

        separateLab = options.separateLab;

        dataAlphabet = new Alphabet();
        typeAlphabet = new Alphabet();

        depReader = DependencyReader.createDependencyReader(options.format, options.discourseMode, options.stackedLevel1, options.useStemmingIfLemmasAbsent);
    }

    public void initInputFile(String file) throws IOException {
        labeled = depReader.startReading(file);
    }

    public void initOutputFile(String file) throws IOException {
        depWriter =
                DependencyWriter.createDependencyWriter(options.format, labeled);
        depWriter.startWriting(file);
    }

    public void outputInstance(DependencyInstance instance) throws IOException {
        depWriter.write(instance);
    }

    public void close() throws IOException {
        if (null != depWriter) {
            depWriter.finishWriting();
        }
    }

    public String getType(int typeIndex) {
        return types[typeIndex];
    }

    protected final DependencyInstance nextInstance() throws IOException {
        DependencyInstance instance = depReader.getNext();
        if (instance == null || instance.forms == null) {
            return null;
        }

        instance.setFeatureVector(createFeatureVector(instance));

        String[] labs = instance.deprels;
        int[] heads = instance.heads;

        StringBuilder spans = new StringBuilder(heads.length * 5);
        for (int i = 1; i < heads.length; i++) {
            spans.append(heads[i]).append("|").append(i).append(":").append(typeAlphabet.lookupIndex(labs[i])).append(" ");
        }
        instance.actParseTree = spans.substring(0, spans.length() - 1);

        return instance;
    }

    // afm 04-15-2008
    public void printModelStats(Parameters params) {
        double norm1total = 0.0;
        int num_stacked = 0;
        double norm1stacked = 0.0;
        int num_wordfeat = 0;
        double norm1wordfeat = 0.0;

        DependencyParser.out.println("No. Features: " + dataAlphabet.numEntries);

        Object[] keys = dataAlphabet.toArray();
        for (int i = 0; i < keys.length; i++) {
            int num = dataAlphabet.lookupIndex(keys[i]);
            String feat = (String) keys[i];
            double val = params != null ? params.parameters[num] : 0.0;
            //DependencyParser.out.println(feat+" = "+val);

            if (val < 0) {
                val -= val;
            }
            norm1total += val;

            if (feat.startsWith("STK_")) // Stacked feature!
            {
                num_stacked++;
                norm1stacked += val;
            } else if (feat.startsWith("FF") || feat.startsWith("LF")) {
                num_wordfeat++;
                norm1wordfeat += val;
            }
        }
        DependencyParser.out.println("No. Stacked Features: " + num_stacked);
        DependencyParser.out.println("No. Word+Feat Features: " + num_wordfeat);
        DependencyParser.out.println("L1 norm of weight vector: " + norm1total);
        DependencyParser.out.println("L1 norm of weight stacked subvector: " + norm1stacked);
        DependencyParser.out.println("L1 norm of weight word+feat subvector: " + norm1wordfeat);
    }

    public int[] createInstances(String file,
            File featFileName) throws IOException {

        createAlphabet(file);

        DependencyParser.out.println("Num Features: " + dataAlphabet.size());

        if (options.separateLab == true) {
            printModelStats(null);
        }
        
        labeled = depReader.startReading(file);

        TIntArrayList lengths = new TIntArrayList();

        ObjectOutputStream out = options.createForest
                ? new ObjectOutputStream(new FileOutputStream(featFileName))
                : null;

        DependencyInstance instance = depReader.getNext();
        int num1 = 0;

        DependencyParser.out.println("Creating Feature Vector Instances: ");
        while (instance != null) {
            DependencyParser.out.print(num1 + " ");

            instance.setFeatureVector(createFeatureVector(instance));

            String[] labs = instance.deprels;
            int[] heads = instance.heads;

            StringBuilder spans = new StringBuilder(heads.length * 5);
            for (int i = 1; i < heads.length; i++) {
                spans.append(heads[i]).append("|").append(i).append(":").append(typeAlphabet.lookupIndex(labs[i])).append(" ");
            }
            instance.actParseTree = spans.substring(0, spans.length() - 1);

            lengths.add(instance.length());

            if (options.createForest) {
                writeInstance(instance, out);
            }
            instance = depReader.getNext();

            num1++;
        }

        DependencyParser.out.println();

        closeAlphabets();

        if (options.createForest) {
            out.close();
        }

        return lengths.toNativeArray();

    }

    private void createAlphabet(String file) throws IOException {

        DependencyParser.out.print("Creating Alphabet ... ");

        labeled = depReader.startReading(file);

        DependencyInstance instance = depReader.getNext();

        while (instance != null) {

            String[] labs = instance.deprels;
            for (int i = 0; i < labs.length; i++) {
                typeAlphabet.lookupIndex(labs[i]);
            }

            createFeatureVector(instance);

            instance = depReader.getNext();
        }

        closeAlphabets();

        DependencyParser.out.println("Done.");
    }

    public void closeAlphabets() {
        dataAlphabet.stopGrowth();
        typeAlphabet.stopGrowth();

        types = new String[typeAlphabet.size()];
        Object[] keys = typeAlphabet.toArray();
        for (int i = 0; i < keys.length; i++) {
            int indx = typeAlphabet.lookupIndex(keys[i]);
            types[indx] = (String) keys[i];
        }

        KBestParseForest.rootType = typeAlphabet.lookupIndex("<root-type>");
    }

    // add with default 1.0
    public final void add(String feat, FeatureVector fv) {
        int num = dataAlphabet.lookupIndex(feat);
        if (num >= 0) {
            fv.add(num, 1.0);
        }
    }

    public final void add(String feat, double val, FeatureVector fv) {
        int num = dataAlphabet.lookupIndex(feat);
        if (num >= 0) {
            fv.add(num, val);
        }
    }

    public FeatureVector createFeatureVector(DependencyInstance instance) {

        final int instanceLength = instance.length();

        String[] labs = instance.deprels;
        int[] heads = instance.heads;

        FeatureVector fv = new FeatureVector();
        for (int i = 0; i < instanceLength; i++) {
            if (heads[i] == -1) {
                continue;
            }
            int small = i < heads[i] ? i : heads[i];
            int large = i > heads[i] ? i : heads[i];
            boolean attR = i < heads[i] ? false : true;
            addCoreFeatures(instance, small, large, attR, fv);
            if (labeled) {
                if (!separateLab) { // afm 06-03-08
                    addLabeledFeatures(instance, i, labs[i], attR, true, fv);
                    addLabeledFeatures(instance, heads[i], labs[i], attR, false, fv);
                }
            }
        }

        addExtendedFeatures(instance, fv);

        return fv;
    }

    protected void addExtendedFeatures(DependencyInstance instance,
            FeatureVector fv) {
    }

    public void addCoreFeatures(DependencyInstance instance,
            int small,
            int large,
            boolean attR,
            FeatureVector fv) {

        String[] forms = instance.forms;
        String[] pos = instance.postags;
        String[] posA = instance.cpostags;

        String att = attR ? "RA" : "LA";

        int dist = Math.abs(large - small);
        String distBool;
        if (dist > 10) {
            distBool = "10";
        } else if (dist > 5) {
            distBool = "5";
        } else {
            distBool = Integer.toString(dist - 1);
        }

        String attDist = "&" + att + "&" + distBool;

        addLinearFeatures("POS", pos, small, large, attDist, fv);
        addLinearFeatures("CPOS", posA, small, large, attDist, fv);


        //////////////////////////////////////////////////////////////////////

        int headIndex = small;
        int childIndex = large;
        if (!attR) {
            headIndex = large;
            childIndex = small;
        }

        addTwoObsFeatures("HC", forms[headIndex], pos[headIndex],
                forms[childIndex], pos[childIndex], attDist, fv);

        // afm 06-03-2008 --- McDonald's ACL08 code adds also features for 3-size prefixes and suffixes of forms

        if (isCONLL) {

            addTwoObsFeatures("HCA", forms[headIndex], posA[headIndex],
                    forms[childIndex], posA[childIndex], attDist, fv);

            addTwoObsFeatures("HCC", instance.lemmas[headIndex], pos[headIndex],
                    instance.lemmas[childIndex], pos[childIndex],
                    attDist, fv);

            addTwoObsFeatures("HCD", instance.lemmas[headIndex], posA[headIndex],
                    instance.lemmas[childIndex], posA[childIndex],
                    attDist, fv);

            if (options.discourseMode) {
                // Note: The features invoked here are designed for
                // discourse parsing (as opposed to sentential
                // parsing). It is conceivable that they could help for
                // sentential parsing, but current testing indicates that
                // they hurt sentential parsing performance.

                addDiscourseFeatures(instance, small, large,
                        headIndex, childIndex,
                        attDist, fv);

            } else {
                // Add in features from the feature lists. It assumes
                // the feature lists can have different lengths for
                // each item. For example, nouns might have a
                // different number of morphological features than
                // verbs.

                /*///////////////////////////////////////////////////////////////
                // Agreement Feature
                boolean headAttsMatched [] = new boolean[instance.feats[headIndex].length];
                boolean depAttsMatched [] = new boolean[instance.feats[childIndex].length];

                String hPOS = posA[headIndex]; 	// grab head CPOS
                String dPOS = posA[childIndex]; // grab dep CPOS

                for (int i=0; i<instance.feats[headIndex].length; i++) { // for each head attr
                    for (int j=0; j<instance.feats[childIndex].length; j++) { // for each dep attr
                        String headItem = instance.feats[headIndex][i]; // "item": attr=val
                        String depItem = instance.feats[childIndex][j];
		
                        if (headItem.contains("=") && depItem.contains("=")) { // if not "_"
                            String headAtt = instance.feats[headIndex][i].split("=")[0];
                            String depAtt = instance.feats[childIndex][j].split("=")[0];
                            String headVal = instance.feats[headIndex][i].split("=")[1];
                            String depVal = instance.feats[childIndex][j].split("=")[1];
			
                            if (depAtt.equals(headAtt)) { // if same attr
				headAttsMatched[i] = true ; // found a match for this attr
				depAttsMatched[j] = true ;
				
				if (depVal.equals(headVal)) // if same value, add "agrees"
					add(headAtt+"_agrees , head ="+hPOS+",dep ="+dPOS, fv);
				else // if different, add "disagrees"
					add(headAtt+"_disagrees , head ="+hPOS+",dep ="+dPOS, fv);
                            }
                        }
                    }
                }

                for (int i=0; i<headAttsMatched.length; i++) // for each head attr
                    if (!headAttsMatched[i]) { // if unmatched
                        String headItem = instance.feats[headIndex][i]; // add asymmetric
                        add("head_"+headItem+",head ="+hPOS+",dep ="+dPOS, fv);
                    }
	
                for ( int i=0; i<depAttsMatched.length; i++) // for each dep attr
                    if (!depAttsMatched[i]) { // if unmatched
                        String depItem = instance.feats[childIndex][i]; // add asymmetric
                        add("dep_"+depItem+",head ="+hPOS+",dep ="+dPOS, fv);
                    }
                /**///////////////////////////////////////////////////////////////
                
                for (int i = 0; i < instance.feats[headIndex].length; i++) {
                    for (int j = 0; j < instance.feats[childIndex].length; j++) {
                        // afm 06-12-08 --- This lead to an explosion of (irrelevant) features
                        // To do something more similar to McDonald ACL'08, replace the two calls below by:
                        if (options.composeFeaturesWithPOS) {
                            addTwoObsFeatures("POSFEAT" + i + "*" + j,
                                    instance.postags[headIndex],
                                    instance.feats[headIndex][i],
                                    instance.postags[childIndex],
                                    instance.feats[childIndex][j],
                                    attDist, fv);
                        } else {
                            addTwoObsFeatures("FF" + i + "*" + j,
                                    instance.forms[headIndex],
                                    instance.feats[headIndex][i],
                                    instance.forms[childIndex],
                                    instance.feats[childIndex][j],
                                    attDist, fv);

                            addTwoObsFeatures("LF" + i + "*" + j,
                                    instance.lemmas[headIndex],
                                    instance.feats[headIndex][i],
                                    instance.lemmas[childIndex],
                                    instance.feats[childIndex][j],
                                    attDist, fv);
                        }
                    }
                }
            }

            if (instance.stacked) // afm 03-10-08
            {
                addCoreStackedFeatures(instance, headIndex, childIndex, attDist, fv);
            }

        } else {
            // We are using the old MST format.  Pick up stem features
            // the way they used to be done. This is kept for
            // replicability of results for old versions.
            int hL = forms[headIndex].length();
            int cL = forms[childIndex].length();
            if (hL > 5 || cL > 5) {
                addOldMSTStemFeatures(instance.lemmas[headIndex],
                        pos[headIndex],
                        instance.lemmas[childIndex],
                        pos[childIndex],
                        attDist, hL, cL, fv);
            }
        }

    }

    private void addCoreStackedFeatures(DependencyInstance instance,
            int headIndex,
            int childIndex,
            String attDist,
            FeatureVector fv) {
        final int instanceLength = instance.length();

        String[] labs_pred = instance.deprels_pred;
        int[] heads_pred = instance.heads_pred;
        String[] pos = instance.postags; // or cpostags?
        String[] lemmas = instance.lemmas;
        String[] forms = instance.forms;
        int index;
        int j;
        String pos_index, lab_index, lemma_index, form_index;
        if (headIndex == -1) {
            return;
        }
        boolean attR = childIndex < headIndex ? false : true;

        boolean isPredEdge = (heads_pred[childIndex] == headIndex);

        boolean use_lemmas = true;
        boolean use_forms = true;

        if (options.stackedFeats.usePredEdge) {
            add("STK_EDGE" + "=" + isPredEdge, fv); // Is predicted edge?
            add("STK_EDGE_POS" + "=" + isPredEdge + " " + pos[childIndex] + " " + pos[headIndex], fv); // afm 06-07-08		 	    
            if (isPredEdge) {
                if (options.stackedFeats.useLabels) {
                    add("STK_EDGE_LBL" + "=" + labs_pred[childIndex], fv); // afm 03-14-08 --- Label of predicted edge
                    add("STK_EDGE_LBL_POS" + "=" + labs_pred[childIndex] + " " + pos[childIndex] + " " + pos[headIndex], fv); // afm 06-07-08		 	    
                }
            }
        }

        // afm 04-03-2008 --- Predicted head for this child, if this edge was not predicted
        if (options.stackedFeats.usePredHead) {
            if (!isPredEdge) {
                pos_index = null;
                lemma_index = null; // To be used later
                form_index = null; // To be used later

                if (heads_pred[childIndex] >= 0) {
                    pos_index = pos[heads_pred[childIndex]];
                    lemma_index = lemmas[heads_pred[childIndex]];
                    form_index = forms[heads_pred[childIndex]];
                }
                lab_index = labs_pred[childIndex];

                // Head pos, predicted head lemma and pos
                add("STK_HEAD" + "_HL" + "=" + pos[childIndex] + " " + " " + pos[headIndex] + " " + lemma_index + " " + pos_index + "*" + attDist, fv);
                add("STK_HEAD" + "_HL" + "=" + pos[childIndex] + " " + " " + pos[headIndex] + " " + lemma_index + " " + pos_index, fv);
                if (options.stackedFeats.useLabels) {
                    add("STK_HEAD_LBL" + "_HL" + "=" + pos[childIndex] + " " + pos[headIndex] + " " + lemma_index + " " + pos_index + " " + lab_index + "*" + attDist, fv);
                    add("STK_HEAD_LBL" + "_HL" + "=" + pos[childIndex] + " " + pos[headIndex] + " " + lemma_index + " " + pos_index + " " + lab_index, fv);
                }

                if (use_forms) {
                    // Head pos, predicted head form and pos
                    add("STK_HEAD" + "_HF" + "=" + pos[childIndex] + " " + " " + pos[headIndex] + " " + form_index + " " + pos_index + "*" + attDist, fv);
                    add("STK_HEAD" + "_HF" + "=" + pos[childIndex] + " " + " " + pos[headIndex] + " " + form_index + " " + pos_index, fv);
                    if (options.stackedFeats.useLabels) {
                        add("STK_HEAD_LBL" + "_HF" + "=" + pos[childIndex] + " " + pos[headIndex] + " " + form_index + " " + pos_index + " " + lab_index + "*" + attDist, fv);
                        add("STK_HEAD_LBL" + "_HF" + "=" + pos[childIndex] + " " + pos[headIndex] + " " + form_index + " " + pos_index + " " + lab_index, fv);
                    }
                }

                // Head pos, predicted head pos
                add("STK_HEAD" + "=" + pos[childIndex] + " " + pos[headIndex] + " " + pos_index + "*" + attDist, fv);
                add("STK_HEAD" + "=" + pos[childIndex] + " " + pos[headIndex] + " " + pos_index, fv);
                if (options.stackedFeats.useLabels) {
                    add("STK_HEAD_LBL" + "=" + pos[childIndex] + " " + pos[headIndex] + " " + pos_index + " " + lab_index + "*" + attDist, fv);
                    add("STK_HEAD_LBL" + "=" + pos[childIndex] + " " + pos[headIndex] + " " + pos_index + " " + lab_index, fv);
                }
            }
        }

        // afm 03-27-2008 --- All predicted children (not using labels so far --- it could help)
        if (options.stackedFeats.useAllChildren) {
            String featname;
            String allchildren = "";
            String allchildren_labs = "";
            for (j = 0; j < instanceLength; j++) {
                if (headIndex == heads_pred[j]) {
                    if (j == childIndex) {
                        allchildren += "[[C]]" + " "; // This means that the child was predicted at this position		 	    				
                        allchildren_labs += "[[C]]]" + " ";
                    } else {
                        allchildren += pos[j] + " ";
                        allchildren_labs += labs_pred[j] + " ";
                    }
                } else if (j == headIndex) {
                    allchildren += "[H]" + " ";
                    allchildren_labs += "[H]" + " ";
                } else if (j == childIndex) {
                    allchildren += "[C]" + " ";
                    allchildren_labs += "[C]" + " ";
                }
            }
            // afm 06-13-2008 --- with the head lemma
            featname = "STK_ALLCHILD_HL_" + isPredEdge + " " + lemmas[headIndex] + " " + pos[headIndex] + " " + pos[childIndex] + " " + allchildren;
            add(featname, fv);
            if (options.stackedFeats.useLabels) {
                featname = "STK_ALLCHILD_LBL_HL_" + isPredEdge + " " + lemmas[headIndex] + " " + pos[headIndex] + " " + pos[childIndex] + " " + allchildren_labs;
                add(featname, fv);
            }

            if (use_forms) {
                // afm 06-13-2008 --- with the head form
                featname = "STK_ALLCHILD_HF_" + isPredEdge + " " + forms[headIndex] + " " + pos[headIndex] + " " + pos[childIndex] + " " + allchildren;
                add(featname, fv);
                if (options.stackedFeats.useLabels) {
                    featname = "STK_ALLCHILD_LBL_HF_" + isPredEdge + " " + forms[headIndex] + " " + pos[headIndex] + " " + pos[childIndex] + " " + allchildren_labs;
                    add(featname, fv);
                }
            }

            // afm 06-14-2008 --- smoothed version, without the head lemma
            featname = "STK_ALLCHILD_" + isPredEdge + " " + pos[headIndex] + " " + pos[childIndex] + " " + allchildren;
            add(featname, fv);
            if (options.stackedFeats.useLabels) {
                featname = "STK_ALLCHILD_LBL_" + isPredEdge + " " + pos[headIndex] + " " + pos[childIndex] + " " + allchildren_labs;
                add(featname, fv);
            }
        }

        int grandp = heads_pred[headIndex]; // Predicted grandparent

        int valency = 0;
        for (int i = 0; i < instanceLength; i++) {
            if (heads_pred[i] == headIndex) {
                valency++;
            }
        }

        if (options.stackedFeats.useValency) {
            // afm 06-13-2008 --- +isPredEdge     	    	
            add("STK_VAL_HL" + "=" + isPredEdge + " " + pos[childIndex] + " " + lemmas[headIndex] + " " + pos[headIndex] + "*" + attDist + "$" + valency, fv); // afm 06-14-08 -- Predicted valency
            add("STK_VAL" + "=" + isPredEdge + " " + pos[childIndex] + " " + pos[headIndex] + "*" + attDist + "$" + valency, fv); // afm 06-14-08 -- Predicted valency
            add("STK_VAL_HL" + "=" + isPredEdge + " " + pos[childIndex] + " " + lemmas[headIndex] + " " + pos[headIndex] + "$" + valency, fv); // afm 03-15-08 -- Predicted valency
            add("STK_VAL" + "=" + isPredEdge + " " + pos[childIndex] + " " + pos[headIndex] + "$" + valency, fv); // afm 03-15-08 -- Predicted valency

            if (use_forms) {
                add("STK_VAL_HF" + "=" + isPredEdge + " " + pos[childIndex] + " " + forms[headIndex] + " " + pos[headIndex] + "*" + attDist + "$" + valency, fv); // afm 06-14-08 -- Predicted valency
                add("STK_VAL_HF" + "=" + isPredEdge + " " + pos[childIndex] + " " + forms[headIndex] + " " + pos[headIndex] + "$" + valency, fv); // afm 03-15-08 -- Predicted valency
            }
        }

        boolean isSiblMid = false; // Sibling in the middle between (candidate) head and modifier.
        String prefix;

        // Get previous and next (predicted) siblings
        // Note: for next sibling, isSiblMid is always false.
        for (int t = 0; t <= 2; t++) // t = 0 means previous sibling (in the direction head -> modifier); t = 1 means next
        {
            if (t == 0) {
                prefix = "STK_PRVSBL";
            } else if (t == 1) {
                prefix = "STK_NXTSBL";
            } else {
                prefix = "STK_GRANDP";
            }

            if (t == 0 && options.stackedFeats.usePrevSibl == false) {
                continue;
            }
            if (t == 1 && options.stackedFeats.useNextSibl == false) {
                continue;
            }
            if (t == 2 && options.stackedFeats.useGrandparents == false) {
                continue;
            }

            if ((t == 0 && attR) || // prev: head, sibl, modif, or sibl, head, modif
                    t == 1 && !attR) // next: sibl, modif, head
            {
                for (j = childIndex - 1; j >= 0; j--) {
                    if (headIndex == heads_pred[j]) {
                        break;
                    }
                }
                if (j >= 0) {
                    index = j;
                    if (index > headIndex) {
                        isSiblMid = true;
                    } else {
                        isSiblMid = false;
                    }
                } else {
                    index = -1;
                }
            } else if (t != 2) // prev: modif, sibl, head, or modif, head, sibl
            // next: head, modif, sibl
            {
                for (j = childIndex + 1; j < instanceLength; j++) {
                    if (headIndex == heads_pred[j]) {
                        break;
                    }
                }
                if (j < instanceLength) {
                    index = j;
                    if (index < headIndex) {
                        isSiblMid = true;
                    } else {
                        isSiblMid = false;
                    }
                } else {
                    index = -1;
                }
            } else {
                index = grandp;
            }


            if (index < 0) {
                pos_index = "null";
                lab_index = "null";
            } else {
                pos_index = pos[index];
                lab_index = (t == 2) ? labs_pred[headIndex] : labs_pred[index]; // afm 06-13-2008
            }


            // Write features:

            if (options.stackedFeats.useValency) {
                // afm 06-13-2008 --- +isPredEdge     	    	

                add(prefix + "_HL" + "=" + isPredEdge + " " + pos[childIndex] + " " + lemmas[headIndex] + " " + pos[headIndex] + " " + pos_index + "*" + attDist + "$" + valency, fv); // afm 06-14-08 -- Predicted valency
                add(prefix + "=" + isPredEdge + " " + pos[childIndex] + " " + pos[headIndex] + " " + pos_index + "*" + attDist + "$" + valency, fv); // afm 06-14-08 -- Predicted valency
                add(prefix + "_HL" + "=" + isPredEdge + " " + pos[childIndex] + " " + lemmas[headIndex] + " " + pos[headIndex] + " " + pos_index + "$" + valency, fv); // afm 03-15-08 -- Predicted valency
                add(prefix + "=" + isPredEdge + " " + pos[childIndex] + " " + pos[headIndex] + " " + pos_index + "$" + valency, fv); // afm 03-15-08 -- Predicted valency

                if (use_forms) {
                    add(prefix + "_HF" + "=" + isPredEdge + " " + pos[childIndex] + " " + forms[headIndex] + " " + pos[headIndex] + " " + pos_index + "*" + attDist + "$" + valency, fv); // afm 06-14-08 -- Predicted valency
                    add(prefix + "_HF" + "=" + isPredEdge + " " + pos[childIndex] + " " + forms[headIndex] + " " + pos[headIndex] + " " + pos_index + "$" + valency, fv); // afm 03-15-08 -- Predicted valency
                }

            }


            // Includes the head lemma and POS:
            if (use_lemmas) {
                if (t == 0) {
                    // afm 06-13-2008 --- +isPredEdge
                    add(prefix + "_HL" + "=" + isPredEdge + " " + pos[childIndex] + " " + lemmas[headIndex] + " " + pos[headIndex] + " " + pos_index + "*" + attDist + "#" + isSiblMid, fv);
                    add(prefix + "_HL" + "=" + isPredEdge + " " + pos[childIndex] + " " + lemmas[headIndex] + " " + pos[headIndex] + " " + pos_index + "#" + isSiblMid, fv);
                    if (options.stackedFeats.useLabels) {
                        add(prefix + "_HL_LBL" + "=" + isPredEdge + " " + pos[childIndex] + " " + lemmas[headIndex] + " " + pos[headIndex] + " " + pos_index + lab_index + "*" + attDist + "#" + isSiblMid, fv); // afm 03-14-08 --- Sibling label
                        add(prefix + "_HL_LBL" + "=" + isPredEdge + " " + pos[childIndex] + " " + lemmas[headIndex] + " " + pos[headIndex] + " " + pos_index + lab_index + "#" + isSiblMid, fv); // afm 03-14-08 --- Sibling label
                    }
                }

                // afm 06-13-2008 --- +isPredEdge
                add(prefix + "_HL" + "=" + isPredEdge + " " + pos[childIndex] + " " + lemmas[headIndex] + " " + pos[headIndex] + " " + pos_index + "*" + attDist, fv);
                add(prefix + "_HL" + "=" + isPredEdge + " " + pos[childIndex] + " " + lemmas[headIndex] + " " + pos[headIndex] + " " + pos_index, fv);
                if (options.stackedFeats.useLabels) {
                    add(prefix + "_HL_LBL" + "=" + isPredEdge + " " + pos[childIndex] + " " + lemmas[headIndex] + " " + pos[headIndex] + " " + pos_index + lab_index + "*" + attDist, fv); // afm 03-14-08 --- Sibling label
                    add(prefix + "_HL_LBL" + "=" + isPredEdge + " " + pos[childIndex] + " " + lemmas[headIndex] + " " + pos[headIndex] + " " + pos_index + lab_index, fv); // afm 03-14-08 --- Sibling label
                }
            }

            // afm 06-14-2008 --- Includes the head form and POS:
            if (use_forms) {
                if (t == 0) {
                    // afm 06-13-2008 --- +isPredEdge
                    add(prefix + "_HF" + "=" + isPredEdge + " " + pos[childIndex] + " " + forms[headIndex] + " " + pos[headIndex] + " " + pos_index + "*" + attDist + "#" + isSiblMid, fv);
                    add(prefix + "_HF" + "=" + isPredEdge + " " + pos[childIndex] + " " + forms[headIndex] + " " + pos[headIndex] + " " + pos_index + "#" + isSiblMid, fv);
                    if (options.stackedFeats.useLabels) {
                        add(prefix + "_HF_LBL" + "=" + isPredEdge + " " + pos[childIndex] + " " + forms[headIndex] + " " + pos[headIndex] + " " + pos_index + lab_index + "*" + attDist + "#" + isSiblMid, fv); // afm 03-14-08 --- Sibling label
                        add(prefix + "_HF_LBL" + "=" + isPredEdge + " " + pos[childIndex] + " " + forms[headIndex] + " " + pos[headIndex] + " " + pos_index + lab_index + "#" + isSiblMid, fv); // afm 03-14-08 --- Sibling label
                    }
                }

                // afm 06-13-2008 --- +isPredEdge
                add(prefix + "_HF" + "=" + isPredEdge + " " + pos[childIndex] + " " + forms[headIndex] + " " + pos[headIndex] + " " + pos_index + "*" + attDist, fv);
                add(prefix + "_HF" + "=" + isPredEdge + " " + pos[childIndex] + " " + forms[headIndex] + " " + pos[headIndex] + " " + pos_index, fv);
                if (options.stackedFeats.useLabels) {
                    add(prefix + "_HF_LBL" + "=" + isPredEdge + " " + pos[childIndex] + " " + forms[headIndex] + " " + pos[headIndex] + " " + pos_index + lab_index + "*" + attDist, fv); // afm 03-14-08 --- Sibling label
                    add(prefix + "_HF_LBL" + "=" + isPredEdge + " " + pos[childIndex] + " " + forms[headIndex] + " " + pos[headIndex] + " " + pos_index + lab_index, fv); // afm 03-14-08 --- Sibling label
                }
            }


            // Includes the head POS:
            if (t == 0) // For t == 1, the isSiblMid feature is always false, so it's useless
            {
                // afm 06-13-2008 --- +isPredEdge
                add(prefix + "=" + isPredEdge + " " + pos[childIndex] + " " + pos[headIndex] + " " + pos_index + "*" + attDist + "#" + isSiblMid, fv);
                add(prefix + "=" + isPredEdge + " " + pos[childIndex] + " " + pos_index + "*" + attDist + "#" + isSiblMid, fv);
                add(prefix + "=" + isPredEdge + " " + pos[childIndex] + " " + pos[headIndex] + " " + pos_index + "#" + isSiblMid, fv);
                add(prefix + "=" + isPredEdge + " " + pos[childIndex] + " " + pos_index + "#" + isSiblMid, fv);
                if (options.stackedFeats.useLabels) {
                    add(prefix + "_LBL" + "=" + isPredEdge + " " + pos[childIndex] + " " + pos[headIndex] + " " + pos_index + lab_index + "*" + attDist + "#" + isSiblMid, fv); // afm 03-14-08 --- Sibling label
                    add(prefix + "_LBL" + "=" + isPredEdge + " " + pos[childIndex] + " " + pos_index + lab_index + "*" + attDist + "#" + isSiblMid, fv); // afm 03-14-08 --- Sibling label
                    add(prefix + "_LBL" + "=" + isPredEdge + " " + pos[childIndex] + " " + pos[headIndex] + " " + pos_index + lab_index + "#" + isSiblMid, fv); // afm 03-14-08 --- Sibling label
                    add(prefix + "_LBL" + "=" + isPredEdge + " " + pos[childIndex] + " " + pos_index + lab_index + "#" + isSiblMid, fv); // afm 03-14-08 --- Sibling label
                }
            }

            // afm 06-13-2008 --- +isPredEdge
            add(prefix + "=" + isPredEdge + " " + pos[childIndex] + " " + pos[headIndex] + " " + pos_index + "*" + attDist, fv);
            add(prefix + "=" + isPredEdge + " " + pos[childIndex] + " " + pos_index + "*" + attDist, fv);
            add(prefix + "=" + isPredEdge + " " + pos[childIndex] + " " + pos[headIndex] + " " + pos_index, fv);
            add(prefix + "=" + isPredEdge + " " + pos[childIndex] + " " + pos_index, fv);
            if (options.stackedFeats.useLabels) {
                add(prefix + "_LBL" + "=" + isPredEdge + " " + pos[childIndex] + " " + pos[headIndex] + " " + pos_index + lab_index + "*" + attDist, fv); // afm 03-14-08 --- Sibling label
                add(prefix + "_LBL" + "=" + isPredEdge + " " + pos[childIndex] + " " + pos_index + lab_index + "*" + attDist, fv); // afm 03-14-08 --- Sibling label
                add(prefix + "_LBL" + "=" + isPredEdge + " " + pos[childIndex] + " " + pos[headIndex] + " " + pos_index + lab_index, fv); // afm 03-14-08 --- Sibling label
                add(prefix + "_LBL" + "=" + isPredEdge + " " + pos[childIndex] + " " + pos_index + lab_index, fv); // afm 03-14-08 --- Sibling label
            }
        }
    }

    private void addLinearFeatures(String type, String[] obsVals,
            int first, int second,
            String attachDistance,
            FeatureVector fv) {

        String pLeft = first > 0 ? obsVals[first - 1] : "STR";
        String pRight = second < obsVals.length - 1 ? obsVals[second + 1] : "END";
        String pLeftRight = first < second - 1 ? obsVals[first + 1] : "MID";
        String pRightLeft = second > first + 1 ? obsVals[second - 1] : "MID";

        // feature posR posMid posL
        StringBuilder featPos =
                new StringBuilder(type + "PC=" + obsVals[first] + " " + obsVals[second]);

        for (int i = first + 1; i < second; i++) {
            String allPos = featPos.toString() + ' ' + obsVals[i];
            add(allPos, fv);
            add(allPos + attachDistance, fv);

        }

        addCorePosFeatures(type + "PT", pLeft, obsVals[first], pLeftRight,
                pRightLeft, obsVals[second], pRight, attachDistance, fv);

    }

    private void addCorePosFeatures(String prefix,
            String leftOf1, String one, String rightOf1,
            String leftOf2, String two, String rightOf2,
            String attachDistance,
            FeatureVector fv) {

        // feature posL-1 posL posR posR+1

        add(prefix + "=" + leftOf1 + " " + one + " " + two + "*" + attachDistance, fv);

        StringBuilder feat =
                new StringBuilder(prefix + "1=" + leftOf1 + " " + one + " " + two);
        add(feat.toString(), fv);
        feat.append(' ').append(rightOf2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2=" + leftOf1 + " " + two + " " + rightOf2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "3=" + leftOf1 + " " + one + " " + rightOf2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "4=" + one + " " + two + " " + rightOf2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        /////////////////////////////////////////////////////////////
        prefix = "A" + prefix;

        // feature posL posL+1 posR-1 posR
        add(prefix + "1=" + one + " " + rightOf1 + " " + leftOf2 + "*" + attachDistance, fv);

        feat = new StringBuilder(prefix + "1=" + one + " " + rightOf1 + " " + leftOf2);
        add(feat.toString(), fv);
        feat.append(' ').append(two);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2=" + one + " " + rightOf1 + " " + two);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "3=" + one + " " + leftOf2 + " " + two);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "4=" + rightOf1 + " " + leftOf2 + " " + two);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        ///////////////////////////////////////////////////////////////
        prefix = "B" + prefix;

        //// feature posL-1 posL posR-1 posR
        feat = new StringBuilder(prefix + "1=" + leftOf1 + " " + one + " " + leftOf2 + " " + two);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        //// feature posL posL+1 posR posR+1
        feat = new StringBuilder(prefix + "2=" + one + " " + rightOf1 + " " + two + " " + rightOf2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

    }

    /**
     * Add features for two items, each with two observations, e.g. head, head
     * pos, child, and child pos.
     *
     * The use of StringBuilders is not yet as efficient as it could be, but
     * this is a start. (And it abstracts the logic so we can add other features
     * more easily based on other items and observations.)
     *
     */
    private void addTwoObsFeatures(String prefix,
            String item1F1, String item1F2,
            String item2F1, String item2F2,
            String attachDistance,
            FeatureVector fv) {

        StringBuilder feat = new StringBuilder(prefix + "2FF1=" + item1F1);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2FF1=" + item1F1 + " " + item1F2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2FF1=" + item1F1 + " " + item1F2 + " " + item2F2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2FF1=" + item1F1 + " " + item1F2 + " " + item2F2 + " " + item2F1);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2FF2=" + item1F1 + " " + item2F1);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2FF3=" + item1F1 + " " + item2F2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);


        feat = new StringBuilder(prefix + "2FF4=" + item1F2 + " " + item2F1);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2FF4=" + item1F2 + " " + item2F1 + " " + item2F2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2FF5=" + item1F2 + " " + item2F2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2FF6=" + item2F1 + " " + item2F2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2FF7=" + item1F2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2FF8=" + item2F1);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2FF9=" + item2F2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

    }

    public void addLabeledFeatures(DependencyInstance instance,
            int word,
            String type,
            boolean attR,
            boolean childFeatures,
            FeatureVector fv) {

        if (!labeled) {
            return;
        }

        String[] forms = instance.forms;
        String[] pos = instance.postags;

        String att;
        if (attR) {
            att = "RA";
        } else {
            att = "LA";
        }

        att += "&" + childFeatures;

        String w = forms[word];
        String wP = pos[word];

        String wPm1 = word > 0 ? pos[word - 1] : "STR";
        String wPp1 = word < pos.length - 1 ? pos[word + 1] : "END";

        add("NTS1=" + type + "&" + att, fv);
        add("ANTS1=" + type, fv);
        for (int i = 0; i < 2; i++) {
            String suff = i < 1 ? "&" + att : "";
            suff = "&" + type + suff;

            add("NTH=" + w + " " + wP + suff, fv);
            add("NTI=" + wP + suff, fv);
            add("NTIA=" + wPm1 + " " + wP + suff, fv);
            add("NTIB=" + wP + " " + wPp1 + suff, fv);
            add("NTIC=" + wPm1 + " " + wP + " " + wPp1 + suff, fv);
            add("NTJ=" + w + suff, fv); //this

        }
        
        /*///////////////////////////////////////////////////////////////
        // Agreement feature
        if (childFeatures && ( instance.heads[word] != -1)) {
            String[] headFeats = instance.feats[instance.heads[word]];
            String[] childFeats = instance.feats[word];

            String hPOS = instance.cpostags[instance.heads[word]] ; // grab head CPOS
            String dPOS = instance.cpostags[word];                  // grab dep CPOS

            boolean headAttsMatched [] = new boolean[headFeats.length];
            boolean depAttsMatched [] = new boolean[childFeats.length];

            for (int i=0 ; i<childFeats.length; i++) { // for each head attr
                for (int j=0 ; j<headFeats.length ; j++) { // for each dep attr
                    if (headFeats[j].contains("=") && childFeats[i].contains("=")) {
                        String headAtt = headFeats[j].split("=")[0];
                        String depAtt = childFeats[i].split("=")[0];
                        String headVal = headFeats[j].split("=")[1];
                        String depVal = childFeats[i].split("=")[1];

                        if ( depAtt.equals(headAtt) ) { // if same attribute
                            headAttsMatched[j] = true; // found a match for this attr
                            depAttsMatched[i] = true;

                            if (depVal.equals(headVal)) // if same value, add "agrees"
                                add(depAtt+"_agrees & label ="+type+",head ="+hPOS+",dep ="+dPOS, fv);
                            else
                                add(depAtt+"_disagrees & label ="+type+",head ="+hPOS+",dep ="+dPOS, fv);
                        }
                    }
                }
            }

            for (int i=0; i<headAttsMatched.length; i++) // for each head attr
                if (!headAttsMatched[i]) { // if unmatched
                    String headItem = headFeats[i]; // add asymmetric
                    add("head_"+headItem+",head ="+hPOS+",dep ="+dPOS+",label ="+type, fv);
                }

            for (int i=0; i<depAttsMatched.length; i++) // for each dep att
                if (!depAttsMatched[i]) { // if unmatched
                    String depItem = childFeats[i]; // add asymmetric
                    add("dep_"+depItem+",head ="+hPOS+",dep ="+dPOS+",label ="+type, fv);
                }
        }
        /**///////////////////////////////////////////////////////////////
        
        if (instance.stacked) { // afm 03-11-08
            addLabeledStackedFeatures(instance, word, type, attR, childFeatures, fv);
        }
    }

    // afm 03-11-08
    private void addLabeledStackedFeatures(DependencyInstance instance,
            int index,
            String label,
            boolean attR,
            boolean isChild,
            FeatureVector fv) {
        // Add labeled stacked features here --- afm 03-11-08
    }

    private void addDiscourseFeatures(DependencyInstance instance,
            int small,
            int large,
            int headIndex,
            int childIndex,
            String attDist,
            FeatureVector fv) {

        addLinearFeatures("FORM", instance.forms, small, large, attDist, fv);
        addLinearFeatures("LEMMA", instance.lemmas, small, large, attDist, fv);

        addTwoObsFeatures("HCB1", instance.forms[headIndex],
                instance.lemmas[headIndex],
                instance.forms[childIndex],
                instance.lemmas[childIndex],
                attDist, fv);

        addTwoObsFeatures("HCB2", instance.forms[headIndex],
                instance.lemmas[headIndex],
                instance.forms[childIndex],
                instance.postags[childIndex],
                attDist, fv);

        addTwoObsFeatures("HCB3", instance.forms[headIndex],
                instance.lemmas[headIndex],
                instance.forms[childIndex],
                instance.cpostags[childIndex],
                attDist, fv);

        addTwoObsFeatures("HC2", instance.forms[headIndex],
                instance.postags[headIndex],
                instance.forms[childIndex],
                instance.cpostags[childIndex], attDist, fv);

        addTwoObsFeatures("HCC2", instance.lemmas[headIndex],
                instance.postags[headIndex],
                instance.lemmas[childIndex],
                instance.cpostags[childIndex],
                attDist, fv);


        //// Use this if your extra feature lists all have the same length.
        for (int i = 0; i < instance.feats.length; i++) {

            addLinearFeatures("F" + i, instance.feats[i], small, large, attDist, fv);

            addTwoObsFeatures("FF" + i,
                    instance.forms[headIndex],
                    instance.feats[i][headIndex],
                    instance.forms[childIndex],
                    instance.feats[i][childIndex],
                    attDist, fv);

            addTwoObsFeatures("LF" + i,
                    instance.lemmas[headIndex],
                    instance.feats[i][headIndex],
                    instance.lemmas[childIndex],
                    instance.feats[i][childIndex],
                    attDist, fv);

            addTwoObsFeatures("PF" + i,
                    instance.postags[headIndex],
                    instance.feats[i][headIndex],
                    instance.postags[childIndex],
                    instance.feats[i][childIndex],
                    attDist, fv);

            addTwoObsFeatures("CPF" + i,
                    instance.cpostags[headIndex],
                    instance.feats[i][headIndex],
                    instance.cpostags[childIndex],
                    instance.feats[i][childIndex],
                    attDist, fv);


            for (int j = i + 1; j < instance.feats.length; j++) {

                addTwoObsFeatures("CPF" + i + "_" + j,
                        instance.feats[i][headIndex],
                        instance.feats[j][headIndex],
                        instance.feats[i][childIndex],
                        instance.feats[j][childIndex],
                        attDist, fv);

            }

            for (int j = 0; j < instance.feats.length; j++) {

                addTwoObsFeatures("XFF" + i + "_" + j,
                        instance.forms[headIndex],
                        instance.feats[i][headIndex],
                        instance.forms[childIndex],
                        instance.feats[j][childIndex],
                        attDist, fv);

                addTwoObsFeatures("XLF" + i + "_" + j,
                        instance.lemmas[headIndex],
                        instance.feats[i][headIndex],
                        instance.lemmas[childIndex],
                        instance.feats[j][childIndex],
                        attDist, fv);

                addTwoObsFeatures("XPF" + i + "_" + j,
                        instance.postags[headIndex],
                        instance.feats[i][headIndex],
                        instance.postags[childIndex],
                        instance.feats[j][childIndex],
                        attDist, fv);


                addTwoObsFeatures("XCF" + i + "_" + j,
                        instance.cpostags[headIndex],
                        instance.feats[i][headIndex],
                        instance.cpostags[childIndex],
                        instance.feats[j][childIndex],
                        attDist, fv);
            }
        }

        // Test out relational features
        if (options.useRelationalFeatures) {

            //for (int rf_index=0; rf_index<2; rf_index++) {
            for (int rf_index = 0;
                    rf_index < instance.relFeats.length;
                    rf_index++) {

                String headToChild =
                        "H2C" + rf_index + instance.relFeats[rf_index].getFeature(headIndex, childIndex);

                addTwoObsFeatures("RFA1",
                        instance.forms[headIndex],
                        instance.lemmas[headIndex],
                        instance.postags[childIndex],
                        headToChild,
                        attDist, fv);

                addTwoObsFeatures("RFA2",
                        instance.postags[headIndex],
                        instance.cpostags[headIndex],
                        instance.forms[childIndex],
                        headToChild,
                        attDist, fv);

                addTwoObsFeatures("RFA3",
                        instance.lemmas[headIndex],
                        instance.postags[headIndex],
                        instance.forms[childIndex],
                        headToChild,
                        attDist, fv);

                addTwoObsFeatures("RFB1",
                        headToChild,
                        instance.postags[headIndex],
                        instance.forms[childIndex],
                        instance.lemmas[childIndex],
                        attDist, fv);

                addTwoObsFeatures("RFB2",
                        headToChild,
                        instance.forms[headIndex],
                        instance.postags[childIndex],
                        instance.cpostags[childIndex],
                        attDist, fv);

                addTwoObsFeatures("RFB3",
                        headToChild,
                        instance.forms[headIndex],
                        instance.lemmas[childIndex],
                        instance.postags[childIndex],
                        attDist, fv);

            }
        }
    }

    public void fillFeatureVectors(DependencyInstance instance,
            FeatureVector[][][] fvs,
            double[][][] probs,
            FeatureVector[][][][] nt_fvs,
            double[][][][] nt_probs, Parameters params) {

        final int instanceLength = instance.length();

        // Get production crap.		
        for (int w1 = 0; w1 < instanceLength; w1++) {
            for (int w2 = w1 + 1; w2 < instanceLength; w2++) {
                for (int ph = 0; ph < 2; ph++) {
                    boolean attR = ph == 0 ? true : false;

                    int childInt = attR ? w2 : w1;
                    int parInt = attR ? w1 : w2;

                    FeatureVector prodFV = new FeatureVector();
                    addCoreFeatures(instance, w1, w2, attR, prodFV);
                    double prodProb = params.getScore(prodFV);
                    fvs[w1][w2][ph] = prodFV;
                    probs[w1][w2][ph] = prodProb;
                }
            }
        }

        if (labeled) {
            if (!separateLab) { // afm 06-03-08
                for (int w1 = 0; w1 < instanceLength; w1++) {
                    for (int t = 0; t < types.length; t++) {
                        String type = types[t];
                        for (int ph = 0; ph < 2; ph++) {

                            boolean attR = ph == 0 ? true : false;
                            for (int ch = 0; ch < 2; ch++) {

                                boolean child = ch == 0 ? true : false;

                                FeatureVector prodFV = new FeatureVector();
                                addLabeledFeatures(instance, w1,
                                        type, attR, child, prodFV);

                                double nt_prob = params.getScore(prodFV);
                                nt_fvs[w1][t][ph][ch] = prodFV;
                                nt_probs[w1][t][ph][ch] = nt_prob;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Write an instance to an output stream for later reading.
     *
     *
     */
    protected void writeInstance(DependencyInstance instance, ObjectOutputStream out) {

        int instanceLength = instance.length();

        try {

            for (int w1 = 0; w1 < instanceLength; w1++) {
                for (int w2 = w1 + 1; w2 < instanceLength; w2++) {
                    for (int ph = 0; ph < 2; ph++) {
                        boolean attR = ph == 0 ? true : false;
                        FeatureVector prodFV = new FeatureVector();
                        addCoreFeatures(instance, w1, w2, attR, prodFV);
                        out.writeObject(prodFV.keys());
                    }
                }
            }
            out.writeInt(-3);

            if (labeled) {
                if (!separateLab) { // afm 06-03-08
                    for (int w1 = 0; w1 < instanceLength; w1++) {
                        for (int t = 0; t < types.length; t++) {
                            String type = types[t];
                            for (int ph = 0; ph < 2; ph++) {
                                boolean attR = ph == 0 ? true : false;
                                for (int ch = 0; ch < 2; ch++) {
                                    boolean child = ch == 0 ? true : false;
                                    FeatureVector prodFV = new FeatureVector();
                                    addLabeledFeatures(instance, w1,
                                            type, attR, child, prodFV);
                                    out.writeObject(prodFV.keys());
                                }
                            }
                        }
                    }
                    out.writeInt(-3);
                }
            }

            writeExtendedFeatures(instance, out);

            out.writeObject(instance.fv.keys());
            out.writeInt(-4);

            out.writeObject(instance);
            out.writeInt(-1);

            out.reset();

        } catch (IOException e) {
        }
    }

    /**
     * Override this method if you have extra features that need to be written
     * to disk. For the basic DependencyPipe, nothing happens.
     *
     */
    protected void writeExtendedFeatures(DependencyInstance instance, ObjectOutputStream out)
            throws IOException {
    }

    /**
     * Read an instance from an input stream.
     *
     *
     */
    public DependencyInstance readInstance(ObjectInputStream in,
            int length,
            FeatureVector[][][] fvs,
            double[][][] probs,
            FeatureVector[][][][] nt_fvs,
            double[][][][] nt_probs,
            Parameters params) throws IOException {

        try {

            // Get production crap.		
            for (int w1 = 0; w1 < length; w1++) {
                for (int w2 = w1 + 1; w2 < length; w2++) {
                    for (int ph = 0; ph < 2; ph++) {
                        FeatureVector prodFV = new FeatureVector((int[]) in.readObject());
                        double prodProb = params.getScore(prodFV);
                        fvs[w1][w2][ph] = prodFV;
                        probs[w1][w2][ph] = prodProb;
                    }
                }
            }
            int last = in.readInt();
            if (last != -3) {
                DependencyParser.out.println("Error reading file.");
                System.exit(0);
            }

            if (labeled) {
                if (!separateLab) { // afm 06-04-08
                    for (int w1 = 0; w1 < length; w1++) {
                        for (int t = 0; t < types.length; t++) {
                            String type = types[t];

                            for (int ph = 0; ph < 2; ph++) {
                                for (int ch = 0; ch < 2; ch++) {
                                    FeatureVector prodFV = new FeatureVector((int[]) in.readObject());
                                    double nt_prob = params.getScore(prodFV);
                                    nt_fvs[w1][t][ph][ch] = prodFV;
                                    nt_probs[w1][t][ph][ch] = nt_prob;
                                }
                            }
                        }
                    }
                    last = in.readInt();
                    if (last != -3) {
                        DependencyParser.out.println("Error reading file.");
                        System.exit(0);
                    }
                }
            }

            FeatureVector nfv = new FeatureVector((int[]) in.readObject());
            last = in.readInt();
            if (last != -4) {
                DependencyParser.out.println("Error reading file.");
                System.exit(0);
            }

            DependencyInstance marshalledDI;
            marshalledDI = (DependencyInstance) in.readObject();
            marshalledDI.setFeatureVector(nfv);

            last = in.readInt();
            if (last != -1) {
                DependencyParser.out.println("Error reading file.");
                System.exit(0);
            }

            return marshalledDI;

        } catch (ClassNotFoundException e) {
            DependencyParser.out.println("Error reading file.");
            System.exit(0);
        }

        // this won't happen, but it takes care of compilation complaints
        return null;
    }

    /**
     * Get features for stems the old way. The only way this differs from
     * calling addTwoObsFeatures() is that it checks the lengths of the full
     * lexical items are greater than 5 before adding features.
     *
     */
    private void addOldMSTStemFeatures(String hLemma, String headP,
            String cLemma, String childP, String attDist,
            int hL, int cL, FeatureVector fv) {

        String all = hLemma + " " + headP + " " + cLemma + " " + childP;
        String hPos = headP + " " + cLemma + " " + childP;
        String cPos = hLemma + " " + headP + " " + childP;
        String hP = headP + " " + cLemma;
        String cP = hLemma + " " + childP;
        String oPos = headP + " " + childP;
        String oLex = hLemma + " " + cLemma;

        add("SA=" + all + attDist, fv); //this
        add("SF=" + oLex + attDist, fv); //this
        add("SAA=" + all, fv); //this
        add("SFF=" + oLex, fv); //this

        if (cL > 5) {
            add("SB=" + hPos + attDist, fv);
            add("SD=" + hP + attDist, fv);
            add("SK=" + cLemma + " " + childP + attDist, fv);
            add("SM=" + cLemma + attDist, fv); //this
            add("SBB=" + hPos, fv);
            add("SDD=" + hP, fv);
            add("SKK=" + cLemma + " " + childP, fv);
            add("SMM=" + cLemma, fv); //this
        }
        if (hL > 5) {
            add("SC=" + cPos + attDist, fv);
            add("SE=" + cP + attDist, fv);
            add("SH=" + hLemma + " " + headP + attDist, fv);
            add("SJ=" + hLemma + attDist, fv); //this

            add("SCC=" + cPos, fv);
            add("SEE=" + cP, fv);
            add("SHH=" + hLemma + " " + headP, fv);
            add("SJJ=" + hLemma, fv); //this
        }
    }
}