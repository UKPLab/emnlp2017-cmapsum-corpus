'''
compares system concept maps with reference maps using meteor

params:
1   folder with model and references maps per topic
2   meteor folder

currently quite slow, as meteor is called multiple times

'''
import sys, os, codecs, subprocess
from meteor_util import *


if len(sys.argv) < 3:
    print 'ERROR usage: run_meteor.py <folder> <meteor-folder>'
    exit(4)
folder = sys.argv[1]

meteor_jar = os.path.join(sys.argv[2], 'meteor-1.5.jar')
if not os.path.exists(meteor_jar):
    print 'path to meteor invalid:', meteor_jar
    exit(4)
meteor_cmd = ['java', '-Xmx2g', '-jar', meteor_jar, '-l', 'en', '-norm', '-ssOut']


# determine topics and systems to be evaluated
files = os.listdir(folder)
files = [f for f in files if f[-5:] == '.cmap']

topics = set([f.split('.')[0] for f in files])
topics = sorted(list(topics))
systems = set([f.split('.')[2] for f in files if f.split('.')[1]=='S'])
systems = sorted(list(systems))

print 'topics', len(topics)
print 'systems', len(systems)


# for each topic
metrics = {}
for t in topics:
    print
    print t
    
    # load reference map
    ref_name = t+'.R.'+t+'.cmap'
    with codecs.open(os.path.join(folder,ref_name), encoding='utf-8') as f:
        ref_props = [l.strip()[:-1] for l in f]
    ref_n = len(ref_props)
       
    for system in systems:
        
        # load system map
        sys_name = t+'.S.'+system+'.cmap'
        with codecs.open(os.path.join(folder,sys_name), encoding='utf-8') as f:
            sys_props = [l.strip()[:-1] for l in f]
        sys_n = len(sys_props)
            
        # create files with all pairs of propositions
        name = t+'.'+system+'.meteor'
        sys_name = os.path.join(folder,name+'.sys')
        with codecs.open(sys_name, 'w', encoding='utf-8') as f:
            for p in sys_props:
                f.write('\n'.join([p]*ref_n) + '\n')
        ref_name = os.path.join(folder,name+'.ref')
        with codecs.open(ref_name, 'w', encoding='utf-8') as f:
            for i in range(0,sys_n):
                f.write('\n'.join(ref_props) + '\n')

                
        # run meteor 
        print 'running meteor'
        meteor_name = os.path.join(folder,name+'.res')
        with codecs.open(meteor_name, 'w', encoding='utf-8') as f:
            cmd = meteor_cmd[:4] + [sys_name, ref_name] + meteor_cmd[4:]
            proc = subprocess.call(cmd, stdout=f)
        
        
        # load scores and compute metrics
        scores = load_meteor_scores(meteor_name, sys_n, ref_n)
        pre, rec, f1 = compute_metrics(scores)

        print 'P: {:.5f}  R: {:.5f}  F1: {:.5f}  {}'.format(pre, rec, f1, system)
        
        metrics[system] = metrics.get(system,[]) + [(pre,rec,f1)]
        

# report macro-averages
print 
print 'macro-averages over topics'

def mean(l):
    return sum(l) / float(len(l))

for system in systems:
    pre = mean([m[0] for m in metrics[system]])
    rec = mean([m[1] for m in metrics[system]])
    f1 = mean([m[2] for m in metrics[system]])
    print 'P: {:.5f}  R: {:.5f}  F1: {:.5f}  {}'.format(pre, rec, f1, system)

    