
# CMapSummaries Baseline System

This is the implementation of a system that summarizes document collections in the form of concept maps.


## Requirements

To run the system, please make sure that you have the following available in your system:

* Java (tested with 1.8)
* Maven

The software is implemented as a Java Maven project. All dependencies are specified in `pom.xml` and can be obtained with Maven from central repositories.


## Data

The system expects the corpus to be available in `data` in the following structure:

* data
	* CMapSummaries
		* train (documents and reference map)
			* topic1
				* topic1.cmap
				* doc1.txt
				* ...
			* topic2
			* ...
		* train_system (maps created by the system)
			* topic1
				* baseline.cmap
			* ...
		* test
			* ...
		* test_system
			* ...

Other datasets should have a similar structure.


## Usage

Before running the system, make sure all requirements are satisfied, dependencies available and the source code has been compiled. 

If the system is run on another dataset as the default one, please adjust the paths accordingly. All necessary paths are defined directly in the classes mentioned below as static variables (no command line arguments yet).

### A) Run the system on input documents

#### 1. Preprocessing

Run `preprocessing.PipelinePreprocessing` to tokenize, segment, part-of-speech tag and parse all source documents using the DKPro UIMA framework. Results will be stored as binary UIMA Cas serializations in the original document folder. 

By default, the program will use the model for the Stanford parser stored in `lib/englishRNN.ser`. We used the English RNN parsing model from version 3.6.0 (2015-12-09) that can be downloaded at [http://nlp.stanford.edu/software/lex-parser.shtml#Download](http://nlp.stanford.edu/software/lex-parser.shtml#Download). Alternatively, you can remove the path from the code and let DKPro load the default model.


#### 2. Summary Concept Map Generation

Once all source documents are preprocessed, run `pipeline.Pipeline` on the dataset to generate a summary concept map for every document cluster. The pipeline will extract all potential concepts and relations between them, merge similar concepts, score all concepts with a classifier, select one relation for each pair of concepts and then build a concept map with 25 of the highest-scored concepts such that it is connected. The output folder and file name for the maps can be defined in the aforementioned class.


### B) Evaluate generated concept maps

Assuming you ran the previous steps with the default configuration, you can evaluate the result using `cmapsum-eval`. For example, the following runs a ROUGE evaluation, assuming that `cmapsum-eval` is located next to this package and the command is executed from `cmapsum-eval`.

```bash
./run_eval.sh -rouge ../cmapsum-baseline/data/CMapSummaries/train ../cmapsum-baseline/data/CMapSummaries/train_system/
```

For more details, please refer to `cmapsum-eval`'s documentation.


### C) Train a new model

If you want to train a new concept selection model, you have to perform the following steps:

1. Preprocessing

	Preprocess your documents as explained above.

2. Generating Training Data

	Run `pipeline.PipelineTrainingData` on your training data set. That will extract potential concepts with features from all topics. Features and labels are stored in Weka's arff-format. Run `util.MergeArff` afterwards to join the arff-files for every topic into a single merged file.

3. Training a model

	Train a classifier using Weka. Code for this is not included, but it can be easily done using the GUI. Place the serialized model in `models` and adapt the path in `extraction.MapBuilderClassifier` accordingly. We used Weka 3.6.

4. Applying a model

	Apply the model to your dataset as described in A.

