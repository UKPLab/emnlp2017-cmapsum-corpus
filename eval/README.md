
# CMapSummaries Evaluation Scripts

This package contains software to evaluate automatically generated concept maps against reference concept maps.

It provides three different metrics:
* Strict Proposition Match
* METEOR Proposition Match
* ROUGE Comparison

For details on how these metrics are calculated, please refer to `README_METRICS.md`.


## Requirements

Please make sure that you have the following available in your system:

* Bash
* Java (tested with 1.8)
* for METEOR Proposition Match
	* Python 2 with numpy
* for ROUGE Comparison
	* Python 2
	* Perl

In order to use the METEOR and ROUGE metrics, you have to install the corresponding software on your system:

* ROUGE 1.5.5
	1. Obtain ROUGE 1.5.5 from [http://www.berouge.com/Pages/default.aspx](http://www.berouge.com/Pages/default.aspx) and extract the archive.
	2. Set `ROUGE_DIR` in the startup script `run_eval.sh` to the ROUGE main folder containing `ROUGE-1.5.5.pl`.
* METEOR 1.5
	1. Download METEOR 1.5 from [http://www.cs.cmu.edu/~alavie/METEOR/index.html](http://www.cs.cmu.edu/~alavie/METEOR/index.html) and extract the archive.
	2. Set `METEOR_DIR` in the startup script `run_eval.sh` to the METEOR main folder containing `meteor-1.5.jar`.


## Usage

To run an evaluation, the `run_eval.sh` bash script can be used:

```perl
run_eval.sh -strict|-meteor|-rouge <gold-folder> <system-folder>
```

The first argument has to be one of the three metrics. 

The second and third both have to be folders that contain subfolders for each topic and the corresponding concept maps as `*.cmap` files. Refer to the folder `sample_data` for an example. The reference concept maps (one per topic) should have the same name as the topic folder, while the system maps (several per topic) should be named consistent across all topics.

Example:

```bash
./run_eval.sh -strict sample_data/corpus_xyz/gold sample_data/corpus_xyz/system
```
