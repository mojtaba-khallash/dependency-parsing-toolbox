#!/bin/bash
alias java=/usr/lib/jvm/jdk1.7.0/bin/java
alias javac=/usr/lib/jvm/jdk1.7.0/bin/javac
alias jar=/usr/lib/jvm/jdk1.7.0/bin/jar

build_module() {
  echo "test $SRC_FILES"
  echo "compiling $MODULE .."
  cd $MODULE/src
  rm -R ../dist
  mkdir ../dist
  rm -R ../build
  mkdir ../build
  mkdir ../build/classes
  javac -classpath ".:../lib/*" -d ../build/classes $SRC_FILES
  cp ../../$MANIFEST ../build/classes/manifest.mf
  cd ../build/classes
  jar -cvfm ../../dist/${MODULE}.jar manifest.mf $CLASS_FILES
  cd ../../src
  jar -uvfm ../dist/${MODULE}.jar ../build/classes/manifest.mf $OTHER_FILES
  cd ..
  cp -r lib dist/
  cd ..
  
}

MANIFEST=manifest.mf
MODULE="ClearParser"
SRC_FILES="*.java ./clear/decode/*.java ./clear/dep/*.java ./clear/dep/feat/*.java ./clear/dep/srl/*.java ./clear/engine/*.java ./clear/experiment/*.java ./clear/ftr/*.java ./clear/ftr/map/*.java ./clear/ftr/xml/*.java ./clear/helper/*.java ./clear/model/*.java ./clear/morph/*.java ./clear/parse/*.java ./clear/pos/*.java ./clear/propbank/*.java ./clear/reader/*.java ./clear/train/*.java ./clear/train/algorithm/*.java ./clear/train/kernel/*.java ./clear/treebank/*.java ./clear/util/*.java ./clear/util/cluster/*.java ./clear/util/tuple/*.java"
CLASS_FILES="*.class ./clear/decode/*.class ./clear/dep/*.class ./clear/dep/feat/*.class ./clear/dep/srl/*.class ./clear/engine/*.class ./clear/experiment/*.class ./clear/ftr/*.class ./clear/ftr/map/*.class ./clear/ftr/xml/*.class ./clear/helper/*.class ./clear/model/*.class ./clear/morph/*.class ./clear/parse/*.class ./clear/pos/*.class ./clear/propbank/*.class ./clear/reader/*.class ./clear/train/*.class ./clear/train/algorithm/*.class ./clear/train/kernel/*.class ./clear/treebank/*.class ./clear/util/*.class ./clear/util/cluster/*.class ./clear/util/tuple/*.class"
OTHER_FILES=""
build_module

MODULE="Ensemble"
SRC_FILES="./edu/stanford/nlp/parser/ensemble/*.java ./edu/stanford/nlp/parser/ensemble/utils/*.java"
CLASS_FILES="./edu/stanford/nlp/parser/ensemble/*.class ./edu/stanford/nlp/parser/ensemble/utils/*.class"
OTHER_FILES="./edu/stanford/nlp/parser/ensemble/appdata/dataformat/*.xml ./edu/stanford/nlp/parser/ensemble/samples/*.txt"
build_module

MODULE="MaltOptimizer"
SRC_FILES="./algorithmTester/*.java ./experiments/*.java ./optimizer/*.java"
CLASS_FILES="./algorithmTester/*.class ./experiments/*.class ./optimizer/*.class"
OTHER_FILES=""
build_module

MODULE="mate-tools"
SRC_FILES="./decoder/*.java ./examples/*.java ./extractors/*.java ./is2/data/*.java ./is2/io/*.java ./is2/lemmatizer/*.java ./is2/mtag/*.java ./is2/parser/*.java ./is2/parserR2/*.java ./is2/tag/*.java ./is2/tools/*.java ./is2/util/*.java"
CLASS_FILES="./decoder/*.class ./examples/*.class ./extractors/*.class ./is2/data/*.class ./is2/io/*.class ./is2/lemmatizer/*.class ./is2/mtag/*.class ./is2/parser/*.class ./is2/parserR2/*.class ./is2/tag/*.class ./is2/tools/*.class ./is2/util/*.class"
OTHER_FILES=""
build_module

MODULE="MSTParser"
SRC_FILES="./mstparser/*.java ./mstparser/io/*.java ./mstparser/mallet/*.java" 
CLASS_FILES="./mstparser/*.class ./mstparser/io/*.class ./mstparser/mallet/*.class"
OTHER_FILES=""
build_module

cd DependencyParser/lib
rm ClearParser.jar Ensemble.jar MaltOptimizer.jar mate-tools.jar MSTParser.jar
pwd
cp ../../ClearParser/dist/ClearParser.jar .
cp ../../Ensemble/dist/Ensemble.jar .
cp ../../MaltOptimizer/dist/MaltOptimizer.jar .
cp ../../mate-tools/dist/mate-tools.jar .
cp ../../MSTParser/dist/MSTParser.jar .
cd ../..

MANIFEST=dp_manifest.mf
MODULE="DependencyParser"
SRC_FILES="./ir/ac/iust/nlp/dependencyparser/*.java ./ir/ac/iust/nlp/dependencyparser/converter/*.java ./ir/ac/iust/nlp/dependencyparser/dependencygraph/*.java ./ir/ac/iust/nlp/dependencyparser/enumeration/*.java ./ir/ac/iust/nlp/dependencyparser/evaluation/*.java ./ir/ac/iust/nlp/dependencyparser/hybrid/*.java ./ir/ac/iust/nlp/dependencyparser/inputoutput/*.java ./ir/ac/iust/nlp/dependencyparser/optomization/*.java ./ir/ac/iust/nlp/dependencyparser/parsing/*.java ./ir/ac/iust/nlp/dependencyparser/phrasestructuregraph/*.java ./ir/ac/iust/nlp/dependencyparser/projection/*.java ./ir/ac/iust/nlp/dependencyparser/training/*.java ./ir/ac/iust/nlp/dependencyparser/utility/*.java ./ir/ac/iust/nlp/dependencyparser/utility/enumeration/*.java ./ir/ac/iust/nlp/dependencyparser/utility/parsing/*.java"
CLASS_FILES="./ir/ac/iust/nlp/dependencyparser/*.class ./ir/ac/iust/nlp/dependencyparser/converter/*.class ./ir/ac/iust/nlp/dependencyparser/dependencygraph/*.class ./ir/ac/iust/nlp/dependencyparser/enumeration/*.class ./ir/ac/iust/nlp/dependencyparser/evaluation/*.class ./ir/ac/iust/nlp/dependencyparser/hybrid/*.class ./ir/ac/iust/nlp/dependencyparser/inputoutput/*.class ./ir/ac/iust/nlp/dependencyparser/optomization/*.class ./ir/ac/iust/nlp/dependencyparser/parsing/*.class ./ir/ac/iust/nlp/dependencyparser/phrasestructuregraph/*.class ./ir/ac/iust/nlp/dependencyparser/projection/*.class ./ir/ac/iust/nlp/dependencyparser/training/*.class ./ir/ac/iust/nlp/dependencyparser/utility/*.class ./ir/ac/iust/nlp/dependencyparser/utility/enumeration/*.class ./ir/ac/iust/nlp/dependencyparser/utility/parsing/*.class"
OTHER_FILES="./ir/ac/iust/nlp/dependencyparser/*.form ./ir/ac/iust/nlp/dependencyparser/converter/*.form ./ir/ac/iust/nlp/dependencyparser/dependencygraph/*.form ./ir/ac/iust/nlp/dependencyparser/evaluation/*.form ./ir/ac/iust/nlp/dependencyparser/evaluation/*.png ./ir/ac/iust/nlp/dependencyparser/hybrid/*.form ./ir/ac/iust/nlp/dependencyparser/inputoutput/*.form ./ir/ac/iust/nlp/dependencyparser/optomization/*.form ./ir/ac/iust/nlp/dependencyparser/parsing/*.form ./ir/ac/iust/nlp/dependencyparser/phrasestructuregraph/*.form ./ir/ac/iust/nlp/dependencyparser/projection/*.form ./ir/ac/iust/nlp/dependencyparser/training/*.form ./ir/ac/iust/nlp/dependencyparser/utility/parsing/*.form"
build_module

cd DependencyParser
cp -r appdata dist/
cp -r Treebank dist/
cp -r LICENSE.TXT dist/
cp -r README.TXT dist/