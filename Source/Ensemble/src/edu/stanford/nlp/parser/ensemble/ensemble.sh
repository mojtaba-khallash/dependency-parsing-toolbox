#!/bin/sh

if [ "$#" != "2" ]; then
	echo "Usage: ensemble.sh <PROPERTIES FILE> <TEST CORPUS>"
	exit 1
fi

PROPERTIES=$1
INPUT=$2

java -ea -Xmx4g -cp ensemble.jar:lib/ra.jar:lib/jsr173_1.0_api.jar:lib/liblinear-1.33-with-deps.jar:lib/libsvm.jar:lib/log4j-1.2.15.jar:lib/stax-1.2.0.jar edu.stanford.nlp.parser.ensemble.Ensemble --arguments $PROPERTIES --run test --testCorpus $INPUT


