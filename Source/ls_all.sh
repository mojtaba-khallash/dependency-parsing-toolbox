#!/bin/bash
recurse() {
 for i in "$1"/*;do
    if [ -d "$i" ];then
	trimmed=$i
        echo "${trimmed}/*.java \c"
        recurse "$i"
    #elif [ -f "$i" ]; then
    #    echo "file: $i"
    fi
 done
}

echo `pwd`
cd ClearParser/src/
recurse .
echo  ""
echo  ""
cd ../..

echo `pwd`
cd Ensemble/src
recurse .
echo  ""
echo  ""
cd ../..

echo `pwd`
cd MaltOptimizer/src
recurse .
echo  ""
echo  ""
cd ../..

echo `pwd`
cd mate-tools/src
recurse .
echo  ""
echo  ""
cd ../..

echo `pwd`
cd MSTParser/src
recurse .
echo  ""
echo  "
cd ../..


echo `pwd`
cd DependencyParser/src
recurse .
echo  ""
echo  ""
cd ../..

echo "enjoy!"
