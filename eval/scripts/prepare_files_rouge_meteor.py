
'''
prepare concept maps for ROUGE and METEOR evaluation
'''
import sys, os, glob, datetime, codecs, re
from __builtin__ import file

gold_folder = sys.argv[1]
map_folder = sys.argv[2]
tmp_folder = 'eval_tmp'
map_pattern = sys.argv[3] if len(sys.argv) > 3 else '.*'

# create folder
d = datetime.datetime.now()
folder_name = d.strftime("%Y-%m-%d-%I-%M-%S")
folder = os.path.join(tmp_folder, folder_name)
os.mkdir(folder)

# create text files 
topic_data = []
for topic in glob.glob(gold_folder+'/*'):
    if not os.path.isdir(topic):
        continue
    t = os.path.split(topic)[1]
        
    # reference summaries
    references = []
    for file in glob.glob(os.path.join(topic, '*.cmap')):
        with codecs.open(file, encoding='utf-8') as f:
            props = [l.strip().split('\t') for l in f]
        reference_file = os.path.join(folder, t+'.R.'+t+'.cmap')
        with codecs.open(reference_file, 'w', encoding='utf-8') as f:
            for p in props:
                f.write(p[0] + ' ' + p[1] + ' ' + p[2] + '.\n')
            references.append(t+'.R.'+os.path.split(file)[1])
    
    # system summaries
    peers = []
    system_folder = os.path.join(map_folder, os.path.split(topic)[1])
    for file in glob.glob(os.path.join(system_folder, '*.cmap')):
        if re.match(map_pattern, os.path.split(file)[1]):
            with codecs.open(file, encoding='utf-8') as f:
                props = [l.strip().split('\t') for l in f]
            peer_file = os.path.join(folder, t+'.S.'+os.path.split(file)[1])
            with codecs.open(peer_file, 'w', encoding='utf-8') as f:
                for p in props:
                    if len(p) < 3:
                        print 'WARNING: Invalid format:', p
                        continue
                    f.write(p[0] + ' ' + p[1] + ' ' + p[2] + '.\n')
                peers.append(t+'.S.'+os.path.split(file)[1])
            
    topic_data.append((t,peers,references))

# create rouge config file
with codecs.open(os.path.join(folder, 'config.xml'), 'w', encoding='utf-8') as f:
    f.write('<ROUGE-EVAL version="1.0">\n')
    for topic,peers,references in topic_data:
        f.write('<EVAL ID="'+topic+'">\n')
        lfolder = os.path.join(os.path.abspath(tmp_folder), folder_name)
        f.write('<PEER-ROOT>'+lfolder+'</PEER-ROOT>\n<MODEL-ROOT>'+lfolder+'</MODEL-ROOT>\n')
        f.write('<INPUT-FORMAT TYPE="SPL"></INPUT-FORMAT>\n<PEERS>\n')
        for i,p in enumerate(peers):
            f.write('<P ID="'+str(i)+'">'+p+'</P>\n')
        f.write('</PEERS>\n<MODELS>\n')
        for i,r in enumerate(references):
            f.write('<M ID="'+str(i)+'">'+r+'</M>\n')
        f.write('</MODELS>\n</EVAL>\n')
    f.write('</ROUGE-EVAL>')
    
print 'ROUGE peer ids'
for i,p in enumerate(topic_data[0][1]):
    print i, p
