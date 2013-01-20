In the name of Allah


DependencyParser version 1.0
===================
      8 December 2012

This is the README for the *"DependencyParser" toolbox* that integrates different 
algorithms related to dependency parsing in one place. This toolbox has been 
developed by [Mojtaba Khallash] (mailto: mkhallash@gmail.com) from _Iran University of 
Science and Technology (IUST)_.

The home page for the project is:
  http://www.nlp.iust.ac.ir
	
If you want to use this software for research, please refer to this web address 
in your papers.

The toolbox can be used freely for non-commercial research and educational 
purposes. It comes with no warranty, but we welcome all comments, bug reports, 
and suggestions for improvements.

Table of contents
------------------

1. Compiling
2. Example of usage
3. Running the toolbox
   <ul>
	a. Read From Treebank<br/>
	b. Create Dependency Graph<br/>
   	c. Projectivize tree<br/>
   	d. Deprojectivize tree<br/>
   	e. Optimizer<br/>
   	f. Training<br/>
   	g. Parsing<br/>
   	h. Evaluation<br/>
     	i. Hybrid
     	<ul>
		i1. Ensemble<br/>
     		i2. Stacking
	</ul>
   <ul>
4. References

1. Compiling
----------------

Requirements:
* Version 1.7 or later of the [Java 2 SDK] (http://java.sun.com)
You must add java binary file to system path. <br/>In linux, your
can open `~/.bashrc` file and append this line:
`PATH=$PATH:/<address-of-bin-folder-of-JRE>`
* [Perl 5] (http://www.perl.org/) or later for *"MaltBlender"* tools

To compile the code, first decompress the package:

in linux:
> tar -xvzf DependencyParser.tgz<br/>
> cd DependencyParser<br/>
> sh compile_all.sh

in windows:
> decompress the DependencyParser.zip<br/>
> compile_all.bat

You can open the all projects in NetBeans 7.1 (or maybe later) too.

2. Example of Usage
---------------------

For any tools in the DependencyParser package a sample Persian treebank exist in 
"Treebank" folder. 
(the full treebank can be download freely from http://dadegan.ir/en).

3. Running the toolbox
-------------------------

This toolbox run in two mode: 

* gui [default mode]<br/>
Simply double click on jar file or run the following command:

> java -jar DependencyParser.jar

* command-line<br/>
In order to running toolbox in command-line mode must be set -v flag (visible) 
to 0:

> java -jar DependencyParser.jar -v 0

for determining of operational mode, must be set `-mode` flag to one the following 
values: `proj|deproj|optimizer|train|parse|eval|ensemble|stack`<br/>
details of each operaional mode describe in the next sections. for obtain more 
information about specific parameters of each operational mode, use `-help` flag:

> java -jar DependencyParser.jar -v 0 -help <operational-mode>

3a. Reading From Treebank
-------------------------

This operational mode is only available in gui mode. In this mode, the 
dependency tree of each sentence and length distribution plot of a CoNLL format 
corpus can be shown.

Requirements:
* "[Chart2D.jar] (http://chart2d.sourceforge.net/index.php)" for drawing plots.
* "[MaltEval.jar] (http://w3.msi.vxu.se/users/jni/malteval/)" [1] for drawing 
   dependency trees.

3b. Create Dependency Graph
----------------------------

This operational mode is only available in gui mode. In this mode you can enter 
a sentence word by word. For each word you must specify POS tag. After adding 
each word, you can manipulate dependency relations between words. You can save 
the generated tree in *CoNLL format*.

3c. Projectivize tree
-----------------------

In order to convert non-projective dependency parsing before training the corpus 
you can use this toolbox. this toolbox have six parameters (for more details 
see [2]):

<table>
<tr><td>-i &lt;input conll file&gt;</td><td>input file which you want to projectivize</td></tr>
<tr><td>-o &lt;projectivized output&gt;</td><td>name of output file</td></tr>
<tr><td>-m &lt;projectivizing model name&gt;</td><td>name of model which will be created by deprojective process</td></tr>
<tr><td>-mark &lt;marking-strategy (None|Baseline|Head|Path|Head+Path)&gt;</td><td>marking strategy (default: Head)</td></tr>
<tr><td>-covered &lt;covered-root (None|Ignore|Left|Right|Head)&gt;</td><td>convered root (default: Head)</td></tr>
<tr><td>-lift_order &lt;lifting-order (Shortest|Deepest)&gt;</td><td>lifting order (default: Shortest)</td></tr>
</table>

For example:

> java -jar DependencyParser.jar -v 0 -mode proj -i input.conll -o output.conll -m langModel.mco

Requirements:
* "[maltParser.jar] (http://www.maltparser.org/index.html)" [3] for projectivize 
   tree.
	
3d. Deprojectivize tree
-------------------------

This operational mode is used for de-projective dependency parsing after parsing 
test corpus that need projectivized model created by the section 3c.

<table>
<tr><td>-i &lt;input conll file&gt;</td><td>projectivized parse file</td></tr>
<tr><td>-m &lt;existing projectivizing model name&gt;</td><td>model that created after projectivizing</td></tr>
<tr><td>-o &lt;deprojectivizing output&gt;</td><td>name of output file</td></tr>
</table>

For example:

> java -jar DependencyParser.jar -v 0 -mode deproj -i input.conll -m langModel.mco -o output.conll

3e. Optimizer
---------------

Goal of this section is choosing best algorithm for giving training corpus and 
then optimizing their parameters and feature model.

<table>
<tr><td>-i &lt;training-corput&gt;</td><td>training corpus that used for optimization</td></tr>
<tr><td>-parser &lt;parser-type (malt)&gt;</td><td>only supports maltparser currently</td></tr>
<tr>
	<td>-phase &lt;optimizing phase (1|2|3|all) [default: all for running all phases]&gt;</td>
	<td>optimization involves three phases:
		<ol>
			<li>Data Characteristics: gathers information about the following properties of the training set.</li>
			<li>Parsing Algorithm: explores a subset of the parsing algorithms implemented in MaltParser, based on the results of the data analysis.</li>
			<li>Features of Model and Learning Algorithm: tries to optimize the featurea of the model given the parameters chosen so far</li>
		</ol>
	</td>
</tr>
<tr><td>-cross_val &lt;using 5-fold cross-validation (0|1) [default: 0]&gt;</td><td>using "cross-validation" for small training set, or "development set" for big training set.</td></tr>
</table>

For example:

> java -jar DependencyParser.jar -v 0 -mode optimizer -i input.conll -parser malt -phase all

Requirements:
* "[MaltOptimizer.jar] (http://nil.fdi.ucm.es/maltoptimizer/install.html)" [4] for optimizer.

3f. Training
---------------

Until now four data-driven dependency parsers is supporting in this part. Two of 
them are transtion-based ("MaltParser" and "ClearParser") and others are 
graph-based ("MSTParser" and Mate-Tools).

<table>
<tr><td>-i &lt;input training corpus&gt;</td><td>use data in training corpus to train the parser</td></tr>
<tr><td>-m &lt;name of training model&gt;</td><td>name of training model that will be made after training phase</td></tr>
<tr>
	<td>-parser &lt;parser-type (malt|clear|mst|mate) [default: malt]&gt;</td>
	<td>	<b>malt</b> parameters:
		<table>
			<tr><td>-option &lt;option-file&gt;</td><td>maltparser have 10 parsing and 2 learning algorithms that can be express in option file with xml format</td></tr>
			<tr><td>-guide &lt;guide-file&gt;</td><td>each of parsing algorithms in maltparser have many parameters that can be express in guide file with xml format</td></tr>
		</table>
		<b>clear</b> parameters:
		<table>
			<tr><td>-option &lt;option-file&gt;</td><td>clearparser have 2 parsing algorithm and one learning algorithm that can be express in option file with xml format</td></tr>
			<tr><td>-guide &lt;guide-file&gt;</td><td>each of parsing algorithms in clearparser have many parameters that can be express in guide file with xml format</td></tr>
			<tr><td>-bootstrap &lt;bootstrapping-level [default: 2]&gt;</td><td>number of iteration to repeat training phase and improve results</td></tr>
		</table>
		<b>mst</b> parameters:
		<table>
			<tr>
				<td>-decode &lt;decode-type (proj|non-proj) [default: non-proj]&gt;</td>
				<td>type of dependency tree that want to learn"proj" use the projective parsing algorithm during training (Eisner algorithm)
				"non-proj" use the non-projective parsing algorithm during training (Chu-Liu-Edmonds algorithm)</td>
			</tr>
			<tr><td>-loss &lt;loss-type (punc|nopunc) [default: punc]&gt;</td><td>"punc" include punctuation in hamming loss calculation "nopunc" do not include punctuation in hamming loss calculation</td></tr>
			<tr>
				<td>-order &lt;order (1|2) [default: 2]&gt;</td>
				<td>specifies the order/scope of features.
					<ul>
						<li>order 1: pairwise feature between head and dependent  (over single edges)</li>
						<li>order 2: feature between siblings or between child and grandparent or both (pairs of adjacent edges)</li>
					</ul>
				</td>
			</tr>
			<tr>
				<td>-k &lt;training k-best [default: 1]&gt;</td>
				<td>
					specifies the k-best parse set size to create 
					constraints during training (For non-projective parsing 
					algorithm, k-best decoding is approximate)
				</td>
			</tr>
			<tr><td>-iter &lt;training iterations [default: 10]&gt;</td><td>number of iteration to stop training</td></tr>
		</table>
		<b>mate</b> parameters:
		<table>
			<tr><td>-decode &lt;decode-type (proj|non-proj) [default: non-proj]&gt;</td><td>type of dependency tree that want to learn</td></tr>
			<tr>
				<td>-threshold &lt;nonprojective-threshold (0-1) [default: 0.3]&gt;</td>
				<td>
					threshold of the non-projective approximation. A higher 
					threshold does causes less non-projective links.  A 
					threshold of 0.3 has proven for English, German, and 
					Czech as a very good choice.
				</td>
			</tr>
			<tr><td>-creation &lt;feature-creation (multiplicative|shift) [default: multiplicative]&gt;</td><td>two ways of creation of features</td></tr>
			<tr><td>-core &lt;number-of-core [default: max-exiting-cores]&gt;</td><td>mate-tools support multicore to speedup training time. this parameter determine number of core that want to use in training</td></tr>
			<tr><td>-iter &lt;training iterations [default: 10]&gt;</td><td>number of iteration to stop training</td></tr>
		</table>
	
	</td>
</tr>
</table>

For example:

> // MaltParser<br/>
> java -jar DependencyParser.jar -v 0 -mode train -i input.conll -m langModel.mco -parser malt -option options.xml -guide guides.xml

Requirements:
* "[maltParser.jar] (http://www.maltparser.org/index.html) for training" [3] MaltParser.

> // ClearParser<br/>
> java -jar DependencyParser.jar -v 0 -mode train -i input.conll -m langModel.mco -parser clear -option config.xml 
-guide feature.xml -bootstrap 2

Requirements:
* "[ClearParser.jar] (http://code.google.com/p/clearparser)" [5] for training ClearParser.

> MSTParser<br/>
> java -jar DependencyParser.jar -v 0 -mode train -i input.conll -m langModel.mco -parser mst -decode non-proj 
-loss punc -order 2 -k 1 -iter 10

Requirements:
* "[MSTParser.jar] (http://www.seas.upenn.edu/~strctlrn/MSTParser/MSTParser.html)" [6] for training MSTParser.

> Mate-Tools<br/>
> java -jar DependencyParser.jar -v 0 -mode train -i input.conll -m langModel.mco -parser mst -decode non-proj 
-threshold 0.3 -core 4 -iter 10

Requirements:
* "[mate-tools.jar] (http://code.google.com/p/mate-tools/)" [7] for training Mate-Tools.

3g. Parsing
---------------

This section is also includes 4 dependency parsers described above. For all of 
algorithms, you must use same value as training phase.

<table>
<tr><td>-i &lt;input parsing file&gt;</td><td>input parse file</td></tr>
<tr><td>-m &lt;name of trined model&gt;</td><td>name of pre-trained model</td></tr>
<tr><td>-o &lt;output parsed name&gt;</td><td>name of output parse file</td></tr>
<tr>
	<td>-parser &lt;parser-type (malt|clear|mst|mate) [default: malt]&gt;</td>
	<td>	<b>malt</b> parameters:
		<table>
			<tr><td>[None]</td><td>any parameter need for parse can be read from informations writen to model during training.</td></tr>
		</table>
		<b>clear</b> parameters:
		<table>
			<tr><td>-option &lt;option-file&gt;</td><td>configuration file need during parsing in xml format</td></tr>
		</table>
		<b>mst</b> parameters:
		<table>
			<tr><td>-decode &lt;decode-type (proj|non-proj) [default: non-proj]&gt;</td><td>type of dependency tree that want to parse</td></tr>
			<tr><td>-order &lt;order (1|2) [default: 2]&gt;</td><td>order of feature that use for parsing</td></tr>
		</table>
		<b>mate</b> parameters:
		<table>
			<tr><td>-decode &lt;decode-type (proj|non-proj) [default: non-proj]&gt;</td><td>type of dependency tree that want to parse</td></tr>
			<tr>
				<td>-threshold &lt;nonprojective-threshold (0-1) [default: 0.3]&gt;</td>
				<td>
					threshold of the non-projective approximation. A higher 
					threshold does causes less non-projective links. A 
					threshold of 0.3 has proven for English, German, and 
					Czech as a very good choice.
				</td>
			</tr>
			<tr><td>-creation &lt;feature-creation (multiplicative|shift) [default: multiplicative]&gt;</td><td>two ways of creation of features</td></tr>
			<tr><td>-core &lt;number-of-core [default: max-exiting-cores]&gt;</td><td>determine number of core that want to use in parsing</td></tr>
		</table>
		<b>[NEEDS TO HAVE THE SAME VALUE OF THE TRAINED MODEL]</b>
	</td>
</tr>
</table>

For example:

> MaltParser<br/>
> java -jar DependencyParser.jar -v 0 -mode parse -i input.conll -m langModel.mco -o output.conll -parser malt

> ClearParser<br/>
> java -jar DependencyParser.jar -v 0 -mode parse -i input.conll -m langModel.mco -o output.conll -parser clear 
-option config.xml

> MSTParser<br/>
> java -jar DependencyParser.jar -v 0 -mode parse -i input.conll -m langModel.mco -o output.conll -parser mst 
-decode non-proj -order 2

> Mate-Tools<br/>
> java -jar DependencyParser.jar -v 0 -mode parse -i input.conll -m langModel.mco -o output.conll -parser mst 
-decode non-proj -threshold 0.3 -core 4

Requirements:
* Same as previous section.

3h. Evaluation
----------------

Two type of evaluations can be done in dependency parsing. 
* quantitative evaluation: standard evaluation software for dependency structure 
  which does not produce visualization of dependency structure.
* qualitative evaluation: produce visualization of dependency structure and also 
  has the ability to highlight discrepancies between the gold-standard files and 
  the parsed files

<table>
<tr><td>-i &lt;input parsed file&gt;</td><td>input parsed file that want to evaluate</td></tr>
<tr><td>-g &lt;gold file&gt;</td><td>input gold standard file that use to compare with parsed file</td></tr>
<tr><td>-o &lt;output eval log&gt;</td><td>name of file that write logs and results in it</td></tr>
<tr><td>-metric &lt;metric 
			(LAS|LA|UAS|AnyRight|AnyWrong|BothRight|BothWrong|HeadRight|
			 HeadWrong|LabelRight|LabelWrong|DirectionRight|GroupedHeadToChildDistanceRight|
			 HeadToChildDistanceRight) [default: LAS]&gt;</td><td>evaluation metric that used for evaluation. NOTE: for selecting multiple metrics, separate them by comma.</td></tr>
<tr><td>-group &lt;group-by 
			(Token|Wordform|Lemma|Cpostag|Postag|Feats|Deprel|
			 Sentence|RelationLength|GroupedRelationLength|SentenceLength|StartWordPosition|EndWordPosition|
			 ArcDirection|ArcDepth|BranchingFactor|ArcProjectivity|Frame) [default: Token]&gt;</td><td>type grouping for express evaluation results.</td></tr>
</table>

For example:

> java -jar DependencyParser.jar -v 0 -mode eval -i input.conll -g gold.conll -o output.conll -metric LAS,UAS -group Token

Requirements:
* "[MaltEval.jar] (http://w3.msi.vxu.se/users/jni/malteval/)" [1].

3i. Hybrid
------------

Two class of hybrid algorithms used in this section:
* Ensemble: combine baseline parsers in parse time.
* Stacking: combine baseline parsers in train time.

3i1. Ensemble
---------------

Implements a linear interpolation of several baseline parsing models.

<table>
<tr><td>-i &lt;input baseline parsers file (separate by comma)&gt;</td><td>name of baseline parsers</td></tr>
<tr><td>-g &lt;gold file&gt;</td><td>gold file contain error free data</td></tr>
<tr><td>-o &lt;output file&gt;</td><td>name of output file after ensemble</td></tr>
<tr>
	<td>-method &lt;method (majority|attardi|eisner|chu_liu_edmond) [default: majority]&gt;</td>
	<td>
		methd of combining baseline parser:
		<ul>
			<li>majority: simple combining by applying majority vote</li>
			<li>attardi: gready top-down approach to combining parser results</li>
			<li>eisner: reparsing algorithm that generate projective tree</li>
			<li>chu_liu_edmond: reparsing algorithm that generate non-projective tree</li>
		</ul>
	</td>
</tr>
</table>
	
For example:

> java -jar DependencyParser.jar -v 0 -mode ensemble -i malt.conll,clear.conll,mst.conll,mate.conll -g gold.conll 
-o ensemble.conll -method attardi

Requirements:
* "[Ensemble.jar] (http://www.surdeanu.name/mihai/ensemble/)" [8] for voting, attardi and eisner.
* "[MaltBlender.jar] (http://w3.msi.vxu.se/users/jni/blend/)" [9] for chu-liu-edmonds.

3i2. Stacking
---------------

This parser explores a stacked framework for learning to predict dependency 
structures for natural language sentences. A second predictor is trained to 
improve the performance of the first, used to approximate rich non-local 
features in the second parser, without sacrificing efficient, model-optimal 
prediction.

<table>
<tr><td>-i &lt;input train file&gt;</td><td>input file for train level0 parser (-l 0 or -l all) or level1 parser (-l 1)</td></tr>
<tr><td>-t &lt;input test file&gt;</td><td>input file for parse level0 parser (-l 0 or -l all) or level1 parser (-l 1)</td></tr>
<tr><td>-l &lt;level (0|1|all) [default: all for running both level]&gt;</td><td>run level0, level1 or both level</td></tr>
<tr><td>-l0_part &lt;level0 augmented parts [default: 5]&gt;</td><td>number of part for augment train and test with predictions of level0 parser</td></tr>
<tr><td>-l0_out_train &lt;level0 output augmented train&gt;</td><td>name of augmented train file after level0</td></tr>
<tr><td>-l0_out_parse &lt;level0 output ougmented parse&gt;</td><td>name of augmented test file after level0</td></tr>
<tr>
	<td>-l0_parser &lt;level0 parser-type (malt|mst) [default: mst]&gt;</td>
	<td>
		malt parameters:
		<table>
			<tr><td>-l0_option &lt;level0 option-file&gt;</td></tr>
			<tr><td>-l0_guide &lt;level0 guide-file&gt;</td></tr>
		</table>
		mst parameters:
		<table>
			<tr><td>-l0_decode &lt;level0 decode-type (proj|non-proj) [default: non-proj]&gt;</td></tr>
			<tr><td>-l0_loss &lt;level0 loss-type (punc|nopunc) [default: punc]&gt;</td></tr>
			<tr><td>-l0_order &lt;level0 order (1|2) [default: 2]&gt;</td></tr>
			<tr><td>-l0_k &lt;level0 training k-best [default: 1]&gt;</td></tr>
			<tr><td>-l0_iter &lt;level0 training iterations [default: 10]&gt;</td></tr>
		</table>
	</td>
</tr>
<tr><td>-l1_pe &lt;level1 use predicted edge (0|1) [default: 1]&gt;</td><td>indicates whether the candidate edge was present, and what was its label</td></tr>
<tr><td>-l1_ps &lt;level1 use previous sibling (0|1) [default: 1]&gt;</td><td>Lemma, POS, link label, distance and direction of attachment of the previous predicted siblings</td></tr>
<tr><td>-l1_ns &lt;level1 use next sibling (0|1) [default: 1]&gt;</td><td>Lemma, POS, link label, distance and direction of attachment of the next predicted siblings</td></tr>
<tr><td>-l1_gp &lt;level1 use grandparent (0|1) [default: 1]&gt;</td><td>Lemma, POS, link label, distance and direction of attachment of the grandparent of the current modifier</td></tr>
<tr><td>-l1_ac &lt;level1 use all childs (0|1) [default: 1]&gt;</td><td>sequence of POS and link labels of all the predicted children of the candidate head</td></tr>
<tr><td>-l1_ph &lt;level1 use predicted head (0|1) [default: 1]&gt;</td><td>predicted head of the candidate modifier (if PredEdge=0)</td></tr>
<tr><td>-l1_v &lt;level1 use valency (0|1) [default: 1]&gt;</td><td>predicted childs of the candidate modifier</td></tr>
<tr>
	<td>-l1_parser &lt;level1 parser-type (mst) [default: mst]&gt;</td>
	<td>mst parameters:
		<table>
			<tr><td>-l1_decode &lt;level1 decode-type (proj|non-proj) [default: non-proj]&gt;</td></tr>
			<tr><td>-l1_loss &lt;level1 loss-type (punc|nopunc) [default: punc]&gt;</td></tr>
			<tr><td>-l1_order &lt;level1 order (1|2) [default: 2]&gt;</td></tr>
			<tr><td>-l1_k &lt;level1 training k-best [default: 1]&gt;</td></tr>
			<tr><td>-l1_iter &lt;level1 training iterations [default: 10]&gt;</td></tr>
        	</table>
	</td>
</tr>
<tr><td>-l1_output &lt;level1 parsed output&gt;</td><td>final output parse file after level1</td></tr>
</table>
	
For example:

> java -jar DependencyParser.jar -v 0 -mode stack -i l0_train.conll -t l0_test.conll -l 0 -l0_out_train aug_train.conll  -l0_out_parse aug_test.conll -l0_parser mst

> java -jar DependencyParser.jar -v 0 -mode stack -i aug_train.conll -t aug_test.conll -l 1 -l1_parser mst -l1_output output.conll

Requirements:
* [Extention of "MSTParser.jar"] (http://www.ark.cs.cmu.edu/MSTParserStacked/) [10] for stacking.

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
