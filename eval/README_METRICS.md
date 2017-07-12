
# Concept Map Evaluation

This document explains different evaluation metrics for summary concept maps.

For all metrics, we assume as input a system generated concept map `S` and a reference concept map `R`. 
Both are represented as sets of propsitions, i.e. a set in which each element is the concatenation of a relation label with its two concept labels:

```tex
P_x = { 'conceptALabel relationLabel conceptBLabel', 'conceptALabel relationLabel conceptCLabel', ... }
```


## A) Strict Proposition Match

Precision and Recall are calculated directly between the sets `P_S` and `P_R` as follows:

```tex
Pre = \frac{1}{|P_S|} \sum_{p \in P_S} 1(match(p,P_R))
```

```tex
Rec = \frac{1}{|P_R|} \sum_{p \in P_R} 1(match(p,P_S))
```

The function `match(p,P)` returns true if `p` matches at least one proposition in `P`. Propositions are compared after stemming and removal of a list of determiners. For instance, "students" and "the student" are considered a match.

The f1-score is the equally weighted harmonic mean of precision and recall. Given precision, recall and f1-scores for each topic, we macro-average them to obtain the final results.


## B) METEOR Proposition Match

For each pair of propositions `p_s \in P_S` and `p_r \in P_R`, we use METEOR[1] to compare them, obtaining a score `meteor(p_s,p_r) \in [0,1]`. In addition to exact matcing, METEOR also takes synonyms and paraphrases into account.

We run METEOR 1.5 with default settings ("-l en", "-norm" and default parameters and weights).

Given the METEOR scores, precision and recall are computed as follows:

```tex
Pre = \frac{1}{|P_S|} \sum_{p \in P_S} max{meteor(p,p_r) | p_r \in P_R}
```

```tex
Rec = \frac{1}{|P_R|} \sum_{p \in P_R} max{meteor(p,p_s) | p_s \in P_S}
```

The f1-score is the equally weighted harmonic mean of precision and recall. Given precision, recall and f1-scores for each topic, we macro-average them to obtain the final results.


## C) ROUGE Comparison

For both sets, we concatenate all propositions into a single string, `s_S` and `s_R`, representing the full concept map. We separate propositions with a "." to ensure that no bigrams span across propositions and the metric is therefore independent of the (arbitrary) ordering of the propositions.

We run ROUGE 1.5.5[2] with `s_S` as the peer summary and `s_R` as a single model summary to obtain the ROUGE-2 score. We use the following parameters:

```perl
perl ROUGE-1.5.5.pl -n 2 -x -m -c 95 -r 1000 -f A -p 0.5 -t 0 -d -a
```

As the final results, we use the averages computed by the ROUGE script.

## References
1. Denkowski, Michael; Lavie, Alon (2014): Meteor Universal: Language Specific Translation Evaluation for Any Target Language. In: Proceedings of the Ninth Workshop on Statistical Machine Translation. Baltimore, Maryland, USA, S. 376–380. [PDF](http://www.aclweb.org/anthology/W14-3348)
2. Lin, Chin-Yew (2004): ROUGE: A Package for Automatic Evaluation of Summaries. In: Text Summarization Branches Out: Proceedings of the ACL-04 Workshop. Barcelona, Spain, S. 74–81. [PDF]( http://www.aclweb.org/anthology/W04-1013)
