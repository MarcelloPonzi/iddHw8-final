import os
import csv
import re
import logging
import optparse
import time

import dedupe
import pandas as pd
from unidecode import unidecode


def preProcess(column):
    """
    Do a little bit of data cleaning with the help of Unidecode and Regex.
    Things like casing, extra spaces, quotes and new lines can be ignored.
    """
    column = unidecode(column)
    column = re.sub('  +', ' ', column)
    column = re.sub('\n', ' ', column)
    column = column.strip().strip('"').strip("'").lower().strip()
    # If data is missing, indicate that by setting the value to `None`
    if not column:
        column = None
    return column


def readData(filename):
    """
    Read in our data from a CSV file and create a dictionary of records,
    where the key is a unique record ID and each value is dict
    """

    data_d = {}
    with open(filename, encoding="utf8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            clean_row = [(k, preProcess(v)) for (k, v) in row.items()]
            row_id = str(row['name'])
            data_d[row_id] = dict(clean_row)

    return data_d


if __name__ == '__main__':

    # ## Logging

    # Dedupe uses Python logging to show or suppress verbose output. This
    # code block lets you change the level of logging on the command
    # line. You don't need it if you don't want that. To enable verbose
    # logging, run `python examples/csv_example/csv_example.py -v`
    optp = optparse.OptionParser()
    optp.add_option('-v', '--verbose', dest='verbose', action='count',
                    help='Increase verbosity (specify multiple times for more)'
                    )
    (opts, args) = optp.parse_args()
    log_level = logging.WARNING
    if opts.verbose:
        if opts.verbose == 1:
            log_level = logging.INFO
        elif opts.verbose >= 2:
            log_level = logging.DEBUG
    logging.getLogger().setLevel(log_level)

    # ## Setup

    # usare questo se si vuole usare il dataset completo
    # input_file = '../MergedDS/ds_output-filtered.csv'

    # WORKAROUND : dataset creato appositamente per l'evaluation
    # il separatore ';' non viene letto correttamente per qualche motivo
    # per cui apriamo il file e lo facciamo esportare da python così gli piace
    ds = pd.read_csv('Dataset_dummy_semicolon_delimited.csv', sep=';')
    ds.to_csv('Dataset_dummy.csv')

    # usare questo se si vuole usare il sub-dataset su cui conosciamo i giusti cluster ID
    # input_file = 'ds-to-rl-COMMADEL.csv'
    input_file = 'Dataset_dummy.csv'

    output_file = 'ds_with_confidenceScore.csv'
    settings_file = './DedupeConfigFiles/csv_example_learned_settings'
    training_file = './DedupeConfigFiles/csv_example_training.json'

    st = time.time()
    print('importing data ...')
    data_d = readData(input_file)
    et = time.time()
    print('Tempo di lettura del dataset: ', (et - st), 'secondi')

    # If a settings file already exists, we'll just load that and skip training
    if os.path.exists(settings_file):
        print('reading from', settings_file)
        with open(settings_file, 'rb') as f:
            deduper = dedupe.StaticDedupe(f)
    else:
        # ## Training

        # Define the fields dedupe will pay attention to
        fields = [
            {'field': 'name', 'type': 'String'},
            {'field': 'country', 'type': 'String', 'has missing': True},
            {'field': 'stock', 'type': 'String', 'has missing': True},
            {'field': 'ceo', 'type': 'String', 'has missing': True},
            {'field': 'founded_year', 'type': 'String', 'has missing': True},
            {'field': 'industry', 'type': 'String', 'has missing': True},
            {'field': 'web_page', 'type': 'String', 'has missing': True},
            {'field': 'market_cap', 'type': 'String', 'has missing': True},
        ]

        # Create a new deduper object and pass our data model to it.
        deduper = dedupe.Dedupe(fields)

        # If we have training data saved from a previous run of dedupe,
        # look for it and load it in.
        # __Note:__ if you want to train from scratch, delete the training_file
        if os.path.exists(training_file):
            print('reading labeled examples from ', training_file)
            with open(training_file, 'rb') as f:
                deduper.prepare_training(data_d, f)
        else:
            deduper.prepare_training(data_d)

        # ## Active learning
        # Dedupe will find the next pair of records
        # it is least certain about and ask you to label them as duplicates
        # or not.
        # use 'y', 'n' and 'u' keys to flag duplicates
        # press 'f' when you are finished
        print('starting active labeling...')

        dedupe.console_label(deduper)

        # Using the examples we just labeled, train the deduper and learn
        # blocking predicates
        deduper.train()

        if not os.path.exists('./DedupeConfigFiles'):
            os.makedirs('./DedupeConfigFiles')

        # When finished, save our training to disk
        with open(training_file, 'w') as tf:
            deduper.write_training(tf)

        # Save our weights and predicates to disk.  If the settings file
        # exists, we will skip all the training and learning next time we run
        # this file.
        with open(settings_file, 'wb') as sf:
            deduper.write_settings(sf)

    # ## Clustering

    # `partition` will return sets of records that dedupe
    # believes are all referring to the same entity.

    print('clustering...')
    st = time.time()
    clustered_dupes = deduper.partition(data_d, 0.5)
    et = time.time()
    print('# duplicate sets', len(clustered_dupes))
    print('Durata del clustering ', et - st, 'secondi')
    # ## Writing Results

    # Write our original data back out to a CSV with a new column called
    # 'Cluster ID' which indicates which records refer to each other.

    cluster_membership = {}
    for cluster_id, (records, scores) in enumerate(clustered_dupes):
        for record_id, score in zip(records, scores):
            cluster_membership[record_id] = {
                "Cluster ID": cluster_id,
                "confidence_score": score
            }

    with open(output_file, 'w', encoding="utf8") as f_output, open(input_file, encoding="utf8") as f_input:

        reader = csv.DictReader(f_input)
        fieldnames = ['Cluster ID', 'confidence_score'] + reader.fieldnames

        writer = csv.DictWriter(f_output, fieldnames=fieldnames)
        writer.writeheader()

        for row in reader:
            row_id = str(row['name'])
            row.update(cluster_membership[row_id])
            writer.writerow(row)