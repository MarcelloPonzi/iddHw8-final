import pandas as pd
import os
from pathlib import Path
import time

############################
### FUNZIONI DI SUPPORTO ###
############################

# Restituisce un dataframe, qualunque sia il formato,
# aprendolo con la funzione corretta.
def read_any(file_path):
    file_name, file_extension = os.path.splitext(file_path)
    if file_extension == '.csv':
        try:
            df = pd.read_csv(file_path)
        except UnicodeDecodeError:
            df = pd.read_csv(file_path, encoding='cp1252')
    elif file_extension == '.json':
        df = pd.read_json(file_path)
    elif file_extension == '.jsonl':
        df = pd.read_json(file_path, lines=True)
    elif file_extension == '.xls':
        df = pd.read_excel(file_path)
    return df


# Clona il dataset nel folderPath in una nuova directory,
# mettendo in lowercase tutte le colonne.
def put_all_columns_in_lowercase(folder_path):
    files = os.listdir(folder_path)
    print(files)
    for file in files:
        print(file)
        df = read_any(folder_path + file)
        df.columns = df.columns.str.lower()

        # Prendi file_name, che contiene tutto il path, e trasformalo
        # per ottenere solo il nome del file senza estensione
        file_name, file_extension = os.path.splitext(folder_path + file)
        file_name_without_extension = Path(file_name).stem
        print(file_name_without_extension)

        # Salva in csv dentro la directory specificata come parametro.
        # Se la directory non esiste, la crea.
        output_directory_path = "../DatasetHW8Lowercase/"
        is_exist = os.path.exists(output_directory_path)
        if not is_exist:
            os.makedirs(output_directory_path)
            print("Directory di output creata!")
        df.to_csv(output_directory_path + file_name_without_extension + ".csv")


# Legge i dataset all'interno di una directory, specificata
# con il suo path, e restituisce un array di dataframe.
def dataset_to_dataframe_array(folder_path):
    df_array = []
    for file in os.listdir(folder_path):
        print("Appending " + file + " to the dataframe array")
        df_array.append(read_any(folder_path + file))
    return df_array


# La seguente funzione sistema il dataset DDD-companiesmarketcap.com.csv
# in cui alcune righe vanno a capo in modo errato.
def append_lines(file_path):
    # Open the file for reading
    with open(file_path, 'r', encoding="utf8") as file:
        # Read the file line by line
        lines = file.readlines()

    # Create a new list to store the modified lines
    new_lines = []

    # Keep track of the previous line
    prev_line = ""

    # Loop through each line in the file
    for line in lines:
        print("LINE = " + line)
        print("PREVIOUS LINE = " + prev_line)
        # If the line starts with double quotes, append it to the previous line
        if line.startswith('"'):
            prev_line = prev_line.strip() + line
            print("PREVIOUS lINE IN THE IF CONDITION = " + prev_line)
        # Otherwise, add the previous line to the new list of lines, and reset the previous line
        else:
            new_lines.append(prev_line)
            prev_line = line

    # Add the last line to the new list of lines
    new_lines.append(prev_line)

    # Open the file for writing
    with open(file_path+"2", 'w', encoding="utf8") as file:
        # Write the modified lines back to the file
        for line in new_lines:
            file.write(line)

#################################
### FINE FUNZIONI DI SUPPORTO ###
#################################

cwd = os.getcwd()
print("Current Working Directory is: "+cwd)

# Una volta eseguita la funzione non serve rifarlo ogni volta
# append_lines("../DatasetHW8Formattato/DDD-companiesmarketcap.com.csv")
put_all_columns_in_lowercase('../DatasetHW8Formattato/')


## FASE DI MERGE

df_array = dataset_to_dataframe_array('../DatasetHW8Lowercase/')
print(df_array)

df_merged = pd.DataFrame(columns=["name"])
i = 0
st = time.time()
# Fa il merge dei df
for df in df_array:
    df = df.applymap(
        lambda x: str(x) if not isinstance(x, str) else x)  # converte tutte le colonne in stringa, se non lo sono già
    df_merged = df_merged.merge(df, how='outer', on=None)   # on=None fa in modo che il merge sia automatico sulle colonne in comune
    print("merged " + str(i))   # contatore per capire l'avanzamento
    i = i + 1
lapsed_time = time.time() - st
print("Tempo di esecuzione del merge: " + time.strftime("%H:%M:%S", time.gmtime(lapsed_time)))
## SALVATAGGIO DEL MERGED DATASET

# Controlla se esiste la directory di output, in caso contrario la crea
output_directory_path = "../MergedDS"
if not os.path.exists(output_directory_path):
    os.makedirs(output_directory_path)
    print("Directory di output creata!")
# Salva i risultati in un file json
df_merged.to_json("../MergedDS/ds_output.json")
# Salva i risultati in un file csv
df_merged.to_csv("../MergedDS/ds_output.csv", sep=';')

## FASE DI PULIZIA DEL DATAFRAME, VORREMMO RIMUOVERE TUTTE LE COLONNE CHE NON FANNO PARTE DELLO SCHEMA MEDIATO DEFINITO

# lista delle colonne che ci servono per lo schema mediato
cols_to_keep = ["stock", "name", "industry", "market_cap", "ceo", "country", "founded_year", "web_page"]
cols_to_drop = [col for col in df_merged.columns if col not in cols_to_keep]  #prende le colonne che non fanno parte dello schema mediato DA SISTEMARE
print(cols_to_drop)
df_filtered = df_merged.drop(axis=1, columns=cols_to_drop)
df_filtered.to_csv("../MergedDS/ds_output-filtered.csv", sep=';')
df_filtered.to_json("../MergedDS/ds_output-filtered.json")