#!/bin/bash

####################################################################################
# Run an evaluation that compares generated and reference concept maps
# on a dataset with several topics
#  
# Usage:
# run_eval.sh -strict|-meteor|-rouge <gold-folder> <system-folder> [<map-name>]
#  
# Both folders have to contain subfolders for each topic
# and the corresponding concept maps as *.cmap files.
#  
# Example:
# ./run_eval.sh -strict sample_data/corpus_xyz/gold sample_data/corpus_xyz/system
####################################################################################
#
# SETTINGS
#
# path to folder with ROUGE perl script (ROUGE-1.5.5.pl)
ROUGE_DIR="/media/sf_shared/ROUGE-1.5.5"
#
# path to folder with METEOR jar (meteor-1.5.jar)
METEOR_DIR="/media/sf_shared/meteor-1.5"
#
####################################################################################


# check arguments 

if [ "$#" -lt 3 ]; then 
	echo usage: $0 "-strict|-meteor|-rouge <gold-folder> <system-folder> [<map-name>]"
	exit
fi
if [[ "$1" != "-strict" && "$1" != "-meteor" && "$1" != "-rouge" ]]; then 
	echo "argument 1 has be one of -strict|-meteor|-rouge"
	exit
fi
if [ ! -d "$2" ]; then
	echo "$2 is not a directory"
	exit
fi
if [ ! -d "$3" ]; then
	echo "$3 is not a directory"
	exit
fi


# check form of concept maps

echo "======================================================"
echo checking concept maps
echo "======================================================"

java -cp cmapsum-eval.jar eval.CheckConceptMaps $3 $4

if [ $? != 0 ]; then
	echo "ERROR: concept maps are not well-formed"
	#exit
fi


# run strict evaluation
if [ "$1" == '-strict' ]; then
	echo "======================================================"
	echo running evaluation - strict matching
	echo "======================================================"
	java -cp cmapsum-eval.jar eval.RunEvaluation $2 $3 $4
fi


# run meteor evaluation
if [ "$1" == '-meteor' ]; then
	echo "======================================================"
	echo "running evaluation - METEOR matching (slow!)"
	echo "======================================================"
	python scripts/prepare_files_rouge_meteor.py $2 $3 $4 > /dev/null
	DIR=$(ls -t -d -1 $PWD/eval_tmp/** | head -1)
	python scripts/run_meteor.py $DIR $METEOR_DIR
fi


# run rouge evaluation
if [ "$1" == '-rouge' ]; then
	echo "======================================================"
	echo running evaluation - ROUGE
	echo "======================================================"
	python scripts/prepare_files_rouge_meteor.py $2 $3 $4
	DIR=$(ls -t -d -1 $PWD/eval_tmp/** | head -1)
	cd $ROUGE_DIR
	perl ROUGE-1.5.5.pl -n 2 -x -m -c 95 -r 1000 -f A -p 0.5 -t 0 -d -a $DIR/config.xml
fi

