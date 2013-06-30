In the name of Allah

-----------------------------
DependencyParser version 1.0
      8 December 2012
-----------------------------

This is the README for the "DependencyParser" toolbox that integrates different 
algorithms related to dependency parsing in one place. This toolbox has been 
developed by Mojtaba Khallash (mkhallash@gmail.com) from Iran University of 
Science and Technology (IUST).

The home page for the project is:
	http://nlp.iust.ac.ir

The code is available on Github at:
	https://github.com/mojtaba-khallash/dependency-parsing-toolbox
	
If you want to use this software for research, please refer to this web address 
in your papers.

-------------------------

The toolbox can be used freely for non-commercial research and educational 
purposes. It comes with no warranty, but we welcome all comments, bug reports, 
and suggestions for improvements.

------------------
Table of contents
------------------

1. Compiling

2. Example of usage

3. Running the toolbox
   a. Read From Treebank
   b. Create Dependency Graph
   c. Projectivize tree
   d. Deprojectivize tree
   e. Optimizer
   f. Training
   g. Parsing
   h. Evaluation
   i. Hybrid
   i1. Ensemble
   i2. Stacking

4. References

----------------
1. Compiling
----------------

Requirements:
* Version 1.7 or later of the Java 2 SDK (http://java.sun.com)
  You must add java binary file to system path. In linux, your
  can open ~/.bashrc file and append this line:
    PATH=$PATH:/<address-of-bin-folder-of-JRE>
* Perl 5 or later (http://www.perl.org/) for "MaltBlender" tools

To compile the code, first decompress the package:

in linux:
$ tar -xvzf DependencyParser.tgz
$ cd DependencyParser
$ sh compile_all.sh

in windows:
- decompress the DependencyParser.zip
- compile_all.bat

You can open the all projects in NetBeans 7.1 (or maybe later) too.

---------------------
2. Example of Usage
---------------------

For any tools in the DependencyParser package a sample Persian treebank exist in 
"Treebank" folder. 
(the full treebank can be download freely from http://dadegan.ir/en).

-------------------------
3. Running the toolbox
-------------------------

This toolbox run in two mode: 

* gui [default mode]
Simply double click on jar file or run the following command:

> java -jar DependencyParser.jar

* command-line
In order to running toolbox in command-line mode must be set -v flag (visible) 
to 0:

> java -jar DependencyParser.jar -v 0

for determining of operational mode, must be set -mode flag to one the following 
values:
	- proj|deproj|optimizer|train|parse|eval|ensemble|stack
details of each operaional mode describe in the next sections. for obtain more 
information about specific parameters of each operational mode, use -help flag:

> java -jar DependencyParser.jar -v 0 -help <operational-mode>

-------------------------
3a. Reading From Treebank
-------------------------

This operational mode is only available in gui mode. In this mode, the 
dependency tree of each sentence and length distribution plot of a CoNLL format 
corpus can be shown.

For example:

> java -jar DependencyParser.jar -v 0 -mode read -i input.conll

Requirements:
** "Chart2D.jar" (http://chart2d.sourceforge.net/index.php) for drawing plots.
** "MaltEval.jar" [1] (http://w3.msi.vxu.se/users/jni/malteval/) for drawing 
   dependency trees.

----------------------------
3b. Create Dependency Graph
----------------------------

This operational mode is only available in gui mode. In this mode you can enter 
a sentence word by word. For each word you must specify POS tag. After adding 
each word, you can manipulate dependency relations between words. You can save 
the generated tree in conll format.

-----------------------
3c. Projectivize tree
-----------------------

In order to convert non-projective dependency parsing before training the corpus 
you can use this toolbox. this toolbox have six parameters (for more details 
see [2]):

-i <input conll file>
	input file which you want to projectivize
	
-o <projectivized output>
	name of output file
	
-m <projectivizing model name>
	name of model which will be created by deprojective process
	
-mark <marking-strategy (None|Baseline|Head|Path|Head+Path)>
	marking strategy (default: Head)
	
-covered <covered-root (None|Ignore|Left|Right|Head)>
	convered root (default: Head)
	
-lift_order <lifting-order (Shortest|Deepest)>
	lifting order (default: Shortest)

For example:

> java -jar DependencyParser.jar -v 0 -mode proj -i input.conll -o \
  output.conll -m langModel.mco

Requirements:
** "maltParser.jar" [3] (http://www.maltparser.org/index.html) for projectivize 
   tree.
	
-------------------------
3d. Deprojectivize tree
-------------------------

This operational mode is used for de-projective dependency parsing after parsing 
test corpus that need projectivized model created by the section 3c.

-i <input conll file>
	projectivized parse file
	
-m <existing projectivizing model name>
	model that created after projectivizing
	
-o <deprojectivizing output>
	name of output file

For example:

> java -jar DependencyParser.jar -v 0 -mode deproj -i input.conll \
  -m langModel.mco -o output.conll

---------------
3e. Optimizer
---------------

Goal of this section is choosing best algorithm for giving training corpus and 
then optimizing their parameters and feature model.

-i <training-corput>
	training corpus that used for optimization
	
-parser <parser-type (malt)>
	only supports maltparser currently
	
-phase <optimizing phase (1|2|3|all) [default: all for running all phases]>
	optimization involves three phases:
		1. Data Characteristics: gathers information about the following 
		   properties of the training set.
		2. Parsing Algorithm: explores a subset of the parsing 
		   algorithms implemented in MaltParser, based on the results of 
		   the data analysis.
		3. Features of Model and Learning Algorithm: tries to optimize 
		   the featurea of the model given the parameters chosen so far
	
-cross_val <using 5-fold cross-validation (0|1) [default: 0]>
	using "cross-validation" for small training set, or "development set" 
	for big training set.

For example:

> java -jar DependencyParser.jar -v 0 -mode optimizer -i input.conll \
  -parser malt -phase all

Requirements:
** "MaltOptimizer.jar" [4] (http://nil.fdi.ucm.es/maltoptimizer/install.html) 
   for optimizer.

---------------
3f. Training
---------------

Until now four data-driven dependency parsers is supporting in this part. Two of 
them are transtion-based ("MaltParser" and "ClearParser") and others are 
graph-based ("MSTParser" and Mate-Tools).

-i <input training corpus>
	use data in training corpus to train the parser

-m <name of training model>
	name of training model that will be made after training phase

-parser <parser-type (malt|clear|mst|mate) [default: malt]>
	>> malt parameters:
		-option <option-file>
			maltparser have 10 parsing and 2 learning algorithms 
			that can be express in option file with xml format
        -guide <guide-file>
			each of parsing algorithms in maltparser have many 
			parameters that can be express in guide file with xml 
			format
        
	>> clear parameters:
		-option <option-file>
			clearparser have 2 parsing algorithm and one learning 
			algorithm that can be express in option file with xml 
			format
        -guide <guide-file>
			each of parsing algorithms in clearparser have many 
			parameters that can be express in guide file with xml 
			format
        -bootstrap <bootstrapping-level [default: 2]>
			number of iteration to repeat training phase and improve 
			results
		
	>> mst parameters:
		-decode <decode-type (proj|non-proj) [default: non-proj]>
			type of dependency tree that want to learn
			"proj" use the projective parsing algorithm during 
			training (Eisner algorithm)
			"non-proj" use the non-projective parsing algorithm 
			during training (Chu-Liu-Edmonds algorithm)
        -loss <loss-type (punc|nopunc) [default: punc]>
			"punc" include punctuation in hamming loss calculation
			"nopunc" do not include punctuation in hamming loss 
			calculation
        -order <order (1|2) [default: 2]>
			specifies the order/scope of features.
			- order 1: pairwise feature between head and dependent 
			(over single edges)
			- order 2: feature between siblings or between child and 
			grandparent or both (pairs of adjacent edges)
        -k <training k-best [default: 1]>
			specifies the k-best parse set size to create 
			constraints during training (For non-projective parsing 
			algorithm, k-best decoding is approximate)
        -iter <training iterations [default: 10]>
			number of iteration to stop training
		
	>> mate parameters:
		-decode <decode-type (proj|non-proj) [default: non-proj]>
			type of dependency tree that want to learn
		-threshold <nonprojective-threshold (0-1) [default: 0.3]>
			threshold of the non-projective approximation. A higher 
			threshold does causes less non-projective links.  A 
			threshold of 0.3 has proven for English, German, and 
			Czech as a very good choice.
		-creation <feature-creation (multiplicative|shift) 
                            [default: multiplicative]>
			two ways of creation of features
		-core <number-of-core [default: max-exiting-cores]>
			mate-tools support multicore to speedup training time. 
			this parameter determine number of core that want to use 
			in training
		-iter <training iterations [default: 10]>
			number of iteration to stop training

For example:

** MaltParser
> java -jar DependencyParser.jar -v 0 -mode train -i input.conll \
  -m langModel.mco -parser malt -option options.xml -guide guides.xml

Requirements:
** "maltParser.jar" [3] (http://www.maltparser.org/index.html) for training 
   MaltParser.

** ClearParser
> java -jar DependencyParser.jar -v 0 -mode train -i input.conll \
  -m langModel.mco -parser clear -option config.xml -guide feature.xml \
  -bootstrap 2

Requirements:
** "ClearParser.jar" [5] (http://code.google.com/p/clearparser) for training 
   ClearParser.

** MSTParser
> java -jar DependencyParser.jar -v 0 -mode train -i input.conll \
  -m langModel.mco -parser mst -decode non-proj -loss punc -order 2 -k 1 \
  -iter 10

Requirements:
** "MSTParser.jar" [6] 
   (http://www.seas.upenn.edu/~strctlrn/MSTParser/MSTParser.html) for training 
   MSTParser.

** Mate-Tools
> java -jar DependencyParser.jar -v 0 -mode train -i input.conll \
  -m langModel.mco -parser mst -decode non-proj -threshold 0.3 -core 4 -iter 10

Requirements:
** "mate-tools.jar" [7] (http://code.google.com/p/mate-tools/) for training 
   Mate-Tools.

---------------
3g. Parsing
---------------

This section is also includes 4 dependency parsers described above. For all of 
algorithms, you must use same value as training phase.

-i <input parsing file>
	input parse file

-m <name of trined model>
	name of pre-trained model

-o <output parsed name>
	name of output parse file

-parser <parser-type (malt|clear|mst|mate) [default: malt]>
	>> malt parameters: [None]
		any parameter need for parse can be read from informations 
		writen to model during training.
	
	>> clear parameters:
		-option <option-file>
			configuration file need during parsing in xml format
		
	>> mst parameters:
		-decode <decode-type (proj|non-proj) [default: non-proj]>
			type of dependency tree that want to parse
		-order <order (1|2) [default: 2]>
			order of feature that use for parsing
			
	>> mate parameters:
		-decode <decode-type (proj|non-proj) [default: non-proj]>
			type of dependency tree that want to parse
		-threshold <nonprojective-threshold (0-1) [default: 0.3]>
			threshold of the non-projective approximation. A higher 
			threshold does causes less non-projective links. A 
			threshold of 0.3 has proven for English, German, and 
			Czech as a very good choice.
		-creation <feature-creation (multiplicative|shift) 
                            [default: multiplicative]>
			two ways of creation of features
		-core <number-of-core [default: max-exiting-cores]>
			determine number of core that want to use in parsing
			
	[NEEDS TO HAVE THE SAME VALUE OF THE TRAINED MODEL]

For example:

** MaltParser
> java -jar DependencyParser.jar -v 0 -mode parse -i input.conll \
  -m langModel.mco -o output.conll -parser malt

** ClearParser
> java -jar DependencyParser.jar -v 0 -mode parse -i input.conll \
  -m langModel.mco -o output.conll -parser clear -option config.xml

** MSTParser
> java -jar DependencyParser.jar -v 0 -mode parse -i input.conll \
  -m langModel.mco -o output.conll -parser mst -decode non-proj -order 2

** Mate-Tools
> java -jar DependencyParser.jar -v 0 -mode parse -i input.conll \
  -m langModel.mco -o output.conll -parser mst -decode non-proj -threshold 0.3 \
  -core 4

Requirements:
Same as previous section.

----------------
3h. Evaluation
----------------

Two type of evaluations can be done in dependency parsing. 
* quantitative evaluation: standard evaluation software for dependency structure 
  which does not produce visualization of dependency structure.
* qualitative evaluation: produce visualization of dependency structure and also 
  has the ability to highlight discrepancies between the gold-standard files and 
  the parsed files

-i <input parsed file>
	input parsed file that want to evaluate

-g <gold file>
	input gold standard file that use to compare with parsed file

-o <output eval log>
	name of file that write logs and results in it

-metric <metric (LAS|LA|UAS|AnyRight|AnyWrong|BothRight|BothWrong|HeadRight|
                 HeadWrong|LabelRight|LabelWrong|DirectionRight|
                 GroupedHeadToChildDistanceRight|HeadToChildDistanceRight) 
                 [default: LAS]>
	evaluation metric that used for evaluation. NOTE: for selecting multiple 
	metrics, separate them by comma.
-group <group-by (Token|Wordform|Lemma|Cpostag|Postag|Feats|Deprel|Sentence|
                  RelationLength|GroupedRelationLength|SentenceLength|
                  StartWordPosition|EndWordPosition|ArcDirection|ArcDepth|
                  BranchingFactor|ArcProjectivity|Frame) [default: Token]>
	type grouping for express evaluation results.
	
For example:

> java -jar DependencyParser.jar -v 0 -mode eval -i input.conll -g gold.conll \
  -o output.conll -metric LAS,UAS -group Token

Requirements:
** "MaltEval.jar" [1] (http://w3.msi.vxu.se/users/jni/malteval/).

------------
3i. Hybrid
------------

Two class of hybrid algorithms used in this section:
* Ensemble: combine baseline parsers in parse time.
* Stacking: combine baseline parsers in train time.

---------------
3i1. Ensemble
---------------

Implements a linear interpolation of several baseline parsing models.

-i <input baseline parsers file (separate by comma)>
	name of baseline parsers

-g <gold file>
	gold file contain error free data

-o <output file>
	name of output file after ensemble

-method <method (majority|attardi|eisner|chu_liu_edmond) [default: majority]>
	methd of combining baseline parser:
		majority: simple combining by applying majority vote
		attardi: gready top-down approach to combining parser results
		eisner: reparsing algorithm that generate projective tree
		chu_liu_edmond: reparsing algorithm that generate non-projective 
		tree
	
For example:

> java -jar DependencyParser.jar -v 0 -mode ensemble \
  -i malt.conll,clear.conll,mst.conll,mate.conll -g gold.conll \
  -o ensemble.conll -method attardi

Requirements:
** "Ensemble.jar" [8] (http://www.surdeanu.name/mihai/ensemble/) for voting, 
   attardi and eisner.
** "MaltBlender.jar" [9] (http://w3.msi.vxu.se/users/jni/blend/) for 
   chu_liu_edmond.

---------------
3i2. Stacking
---------------

This parser explores a stacked framework for learning to predict dependency 
structures for natural language sentences. A second predictor is trained to 
improve the performance of the first, used to approximate rich non-local 
features in the second parser, without sacrificing efficient, model-optimal 
prediction.

-i <input train file>
	input file for train level0 parser (-l 0 or -l all) or level1 parser (-l 1)
	
-t <input test file>
	input file for parse level0 parser (-l 0 or -l all) or level1 parser (-l 1)

-l <level (0|1|all) [default: all for running both level]>
	run level0, level1 or both level

-l0_part <level0 augmented parts [default: 5]>
	number of part for augment train and test with predictions of level0 
	parser

-l0_out_train <level0 output augmented train>
	name of augmented train file after level0
	
-l0_out_parse <level0 output ougmented parse>
	name of augmented test file after level0

-l0_parser <level0 parser-type (malt|mst) [default: mst]>
    >> malt parameters:
        -l0_option <level0 option-file>
        -l0_guide <level0 guide-file>
    >> mst parameters:
        -l0_decode <level0 decode-type (proj|non-proj) [default: non-proj]>
        -l0_loss <level0 loss-type (punc|nopunc) [default: punc]>
        -l0_order <level0 order (1|2) [default: 2]>
        -l0_k <level0 training k-best [default: 1]>
        -l0_iter <level0 training iterations [default: 10]>
		
-l1_pe <level1 use predicted edge (0|1) [default: 1]>
	indicates whether the candidate edge was present, and what was its label

-l1_ps <level1 use previous sibling (0|1) [default: 1]>
	Lemma, POS, link label, distance and direction of attachment of the 
	previous predicted siblings

-l1_ns <level1 use next sibling (0|1) [default: 1]>
	Lemma, POS, link label, distance and direction of attachment of the next 
	predicted siblings

-l1_gp <level1 use grandparent (0|1) [default: 1]>
	Lemma, POS, link label, distance and direction of attachment of the 
	grandparent of the current modifier

-l1_ac <level1 use all childs (0|1) [default: 1]>
	sequence of POS and link labels of all the predicted children of the 
	candidate head
	
-l1_ph <level1 use predicted head (0|1) [default: 1]>
	predicted head of the candidate modifier (if PredEdge=0)
	
-l1_v <level1 use valency (0|1) [default: 1]>
	predicted childs of the candidate modifier
	
-l1_parser <level1 parser-type (mst) [default: mst]>
    >> mst parameters:
        -l1_decode <level1 decode-type (proj|non-proj) [default: non-proj]>
        -l1_loss <level1 loss-type (punc|nopunc) [default: punc]>
        -l1_order <level1 order (1|2) [default: 2]>
        -l1_k <level1 training k-best [default: 1]>
        -l1_iter <level1 training iterations [default: 10]>
-l1_output <level1 parsed output>
	final output parse file after level1
	
For example:

> java -jar DependencyParser.jar -v 0 -mode stack -i l0_train.conll \
  -t l0_test.conll -l 0 -l0_out_train aug_train.conll \
  -l0_out_parse aug_test.conll -l0_parser mst
> java -jar DependencyParser.jar -v 0 -mode stack -i aug_train.conll \
  -t aug_test.conll -l 1 -l1_parser mst -l1_output output.conll

Requirements:
** Extention of "MSTParser.jar" [10] (http://www.ark.cs.cmu.edu/MSTParserStacked/) for stacking.

------------
References
------------
[1]	J. Nilsson and J. Nivre, "Malteval: An evaluation and visualization tool 
    for dependency parsing", in Proceedings of the Sixth International Language 
    Resources and Evaluation, Marrakech, Morocco, May. LREC, Marrakech, Morocco, 
    2008.
	
[2]	J. Nivre and J. Nilsson, "Pseudo-projective dependency parsing", in 
    Proceedings of the 43rd Annual Meeting of the Association for Computational 
    Linguistics (ACL '05), Ann Arbor, Michigan, pp. 99-106, 2005.
	
[3] J. Nivre, et al., "MaltParser: A language-independent system for data-driven 
    dependency parsing", Natural Language Engineering, vol. 13, pp. 95-135, 2007.

[4]	M. Ballesteros and J. Nivre, "MaltOptimizer: A System for MaltParser 
    Optimization", in Proceedings of the Eighth International Conference on 
    Language Resources and Evaluation (LREC 2012), Istanbul, Turkey, pp. 23-27, 
    2012.
	
[5]	J. D. Choi and M. Palmer, "Getting the most out of transition-based 
    dependency parsing", in Proceedings of the 49th Annual Meeting of the 
    Association for Computational Linguistics: Human Language Technologies, 
    Portland, Oregon, USA, pp. 687-692, 2011.
	
[6]	R. McDonald, et al., "Non-projective dependency parsing using spanning 
    tree algorithms", in Proceedings of HLT/EMNLP, pp. 523-530, 2005.
	
[7] B. Bohnet, "Top Accuracy and Fast Dependency Parsing is not a Contradiction", 
    The 23rd International Conference on Computational Linguistics (COLING 2010), 
    Beijing, China, 2010.
	
[8]	M. Surdeanu and C. D. Manning, "Ensemble models for dependency parsing: 
    cheap and good?", in Proceedings of the North American Chapter of the 
    Association for Computational Linguistics Conference (NAACL-2010), 
    pp. 649-652, 2010.
	
[9]	J. Hall, et al., "Single malt or blended? A study in multilingual parser 
    optimization", in Proceedings of the Conference on Empirical Methods in 
    Natural Language Processing and Conference on Computational Natural Language 
    Learning (EMNLP-CoNLL), Prauge, Czech Republic, pp. 933-939, 2007.
	
[10]	A. F. T. Martins, et al., "Stacking dependency parsers", in Proceedings 
	of the Conference on Empirical Methods in Natural Language Processing (EMNLP), 
    pp. 157-166, 2008.