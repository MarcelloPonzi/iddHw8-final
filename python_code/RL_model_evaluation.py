from future.utils import viewitems

import csv
import collections
import itertools
import pandas as pd


def evaluateDuplicates(found_dupes, true_dupes):
    true_positives = found_dupes.intersection(true_dupes)
    false_positives = found_dupes.difference(true_dupes)
    uncovered_dupes = true_dupes.difference(found_dupes)
    precision = 1 - len(false_positives) / float(len(found_dupes))
    recall = len(true_positives) / float(len(true_dupes))

    print('Falsi positivi : ', len(false_positives))

    print('Veri positivi : ', len(true_positives))

    print('Duplicati trovati : ', len(found_dupes))

    print('Precision : ', precision)

    print('Recall : ', recall)

    print('F-measure : ', 2*(recall*precision)/(recall+precision))


# Funzione che identifica le coppie di ennuple duplicate sulla base della colonna 'rowname'
def dupePairs(filename, rowname):
    dupe_d = collections.defaultdict(list)

    with open(filename) as f:
        reader = csv.DictReader(f, delimiter=',', quotechar='"')
        for row in reader:
            dupe_d[row[rowname]].append(row['Id'])

    if 'x' in dupe_d:
        del dupe_d['x']

    dupe_s = set([])
    for (unique_id, cluster) in viewitems(dupe_d):
        if len(cluster) > 1:
            for pair in itertools.combinations(cluster, 2):
                dupe_s.add(frozenset(pair))

    return dupe_s


# WORKAROUND: dataset creato appositamente per l'evaluation
# il separatore ';' non viene letto correttamente per qualche motivo
# per cui apriamo il file e lo facciamo esportare da python così gli piace
ds = pd.read_csv('ds-eval-true-id.csv', sep=';')
ds.to_csv('Dataset_dummy_with_TrueID.csv')

# dataset che contiene i TrueID, identificatori uguali se le ennuple sono uguali, diversi altrimenti
manual_clusters = 'Dataset_dummy_with_TrueID.csv'

# dataset restituito dal modello con rispettivi Cluster ID e confidence score
dedupe_clusters = 'ds_with_confidenceScore.csv'

true_dupes = dupePairs(manual_clusters, 'True ID')
test_dupes = dupePairs(dedupe_clusters, 'Cluster ID')

evaluateDuplicates(test_dupes, true_dupes)
