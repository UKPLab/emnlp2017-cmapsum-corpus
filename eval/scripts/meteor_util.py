'''
meteor format:
0   tstLen
1   refLen
2   tstFuncWords
3   refFuncWords
4   stage1tstMatchesContent
5   stage1refMatchesContent
6   stage1tstMatchesFunction
7   stage1refMatchesFunction
8   s2tc
9   s2rc    
10  s2tf 
11  s2rf 
12  s3tc
13  s3rc 
14  s3tf 
15  s3rf 
16  s4tc 
17  s4rc 
18  s4tf 
19  s4rf 
20  chunks
21  tstwordMatches
22  refWordMatches
'''
import math
import numpy as np


# meteor parameter
w = [1.0, 0.6, 0.8, 0.6]
p = [0.85, 0.2, 0.6, 0.75]
    
    
def score_pair(pair_stats):
    ''' calculate a meteor score from stats for a single pair '''
    
    pre = 0
    for i in range(0,4):
        pre += w[i] * (p[3]*pair_stats[4+4*i] + (1-p[3])*pair_stats[6+4*i])
    pre /= (p[3]*(pair_stats[0]-pair_stats[2]) + (1-p[3])*pair_stats[2])
        
    rec = 0
    for i in range(0,4):
        rec += w[i] * (p[3]*pair_stats[5+4*i] + (1-p[3])*pair_stats[7+4*i])
    rec /= (p[3]*(pair_stats[1]-pair_stats[3]) + (1-p[3])*pair_stats[3])

    if pre == 0 and rec == 0:
        return 0,0,0,0
    else:
        fmean = (pre*rec) / (p[0]*pre + (1-p[0]) * rec)
    
    if pair_stats[21] == pair_stats[0] and pair_stats[22] == pair_stats[1] and pair_stats[20] == 1:
        frag = 0
    else:
        frag = pair_stats[20] / ((pair_stats[21]+pair_stats[22]) / 2)
    pen = p[2] * math.pow(frag, p[1])
    
    score = (1-pen) * fmean
    return pre, rec, pen, score


def load_meteor_scores(file, sys_props, ref_props):
    ''' loads meteor stats file and collects scores in matrix '''
    
    # read in meteor output
    with open(file) as f:
        stats = [l.strip().split() for l in f]
        stats = [[float(x) for x in l] for l in stats]

    # collect pairwise meteor scores
    assert len(stats) == sys_props * ref_props

    scores = np.zeros((sys_props,ref_props))
    for i in range(0,sys_props):
        for j in range(0,ref_props):
            _,_,_,s = score_pair(stats[i*ref_props+j])
            scores[i,j] = s
        
    return scores
    
    
def compute_metrics(scores):
    ''' compares a system and reference concept map based on meteor scores between all proposition pairs '''

    pre = scores.max(axis=1).sum() / scores.shape[0]
    rec = scores.max(axis=0).sum() / scores.shape[1]
    f1 = 2*pre*rec / (pre+rec)
    
    return pre, rec, f1


