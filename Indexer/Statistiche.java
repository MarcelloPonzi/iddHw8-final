import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Statistiche {

    // numero totale di tabelle nel dataset calcolato durante l'indexing.
    static final int numeroTabelle = 550271;

    private String jsonFilePath = "";
    private Long startTime;
    private Long endTime;
    private String infoTime;

    public Statistiche(String path) {       //costruttore con cui assegnare il path del json a jsonFilePath
        this.jsonFilePath = path;
    }

    //TODO numero di tabelle
    /**
     * Per ora non faccimo un metodo qui dentro perché il numero di tabelle lo abbiamo già.
     */

    //TODO numero medio di righe
    /**
     * Ogni tabella ha un campo maxDimensions che è un Object di due campi: "row" e "column" che riportano
     * rispettivamente il numero di righe e di colonne della tabella.
     * Dobbiamo sommare le righe di tutte le tabelle e dividere il totale per il numero di tabelle.
     */
    public void calcolaNumeroMedioDiDimensioni() throws IOException {
        int numeroMedioDiRighe = 0;
        int numeroMedioDiColonne = 0;
        int righeTotali = 0;
        int colonneTotali = 0;

        BufferedReader br = new BufferedReader(new FileReader(jsonFilePath));
        String line;


        while ((line=br.readLine()) != null) {
            JsonObject table = JsonParser.parseString(line).getAsJsonObject();
            JsonObject maxDimensions = table.get("maxDimensions").getAsJsonObject();
            righeTotali += maxDimensions.get("row").getAsInt();
            colonneTotali += maxDimensions.get("column").getAsInt();
        }

        numeroMedioDiRighe = righeTotali/numeroTabelle;
        numeroMedioDiColonne = colonneTotali/numeroTabelle;
        System.out.println("Numero medio di righe = "+numeroMedioDiRighe);
        System.out.println("Numero medio di colonne = "+numeroMedioDiColonne);
    }


    //TODO numero medio di valori nulli per tabella
    /**
     * In alcune tabelle c'è il campo "cleanedText":"Null" quindi andiamo a contare tutti i valori nulli
     * in tutte le tabelle e dividiamo per il numero di tabelle
     */
    public void calcolaNumeroMedioValoriNulli() throws IOException {
        int valoriNulliTotali = 0;
        int numeroMedioValoriNulli = 0;

        BufferedReader br = new BufferedReader(new FileReader(jsonFilePath));
        String line;

        while ((line=br.readLine()) != null) {
            JsonObject table = JsonParser.parseString(line).getAsJsonObject();
            JsonArray celle = (JsonArray) table.get("cells");

            for (Object c : celle) {
                JsonObject cella = (JsonObject) c;
                if(cella.get("cleanedText").getAsString().equals("")) {
                    valoriNulliTotali++;
                }
            }
        }

        numeroMedioValoriNulli = valoriNulliTotali/numeroTabelle;
        System.out.println("Numero medio di valori nulli per tabella = "+numeroMedioValoriNulli);
    }




    //TODO Distribuzione numero di righe (quante tabelle hanno 1,2,3,4, etc. righe)
    /**
     * Creiamo una mappa che ha come chiave il numero di righe preso da maxDimensions.
     * Scorriamo tutte le tabelle e ogni volta che troviamo una tabella che ha un numero di righe presente
     * nella mappa, incrementiamo il valore corrispondente a tale chiave.
     */
    public void calcolaDistribuzioneRigheColonne() throws IOException {
        HashMap<Integer,Integer> distribuzioneRighe = new HashMap<Integer, Integer>();
        HashMap<Integer,Integer> distribuzioneColonne = new HashMap<Integer, Integer>();

        BufferedReader br = new BufferedReader(new FileReader(jsonFilePath));
        String line;

        while ((line=br.readLine()) != null) {
            JsonObject table = JsonParser.parseString(line).getAsJsonObject();
            JsonObject maxDimensions = table.get("maxDimensions").getAsJsonObject();
            if(distribuzioneRighe.containsKey(maxDimensions.get("row").getAsInt())) {
                distribuzioneRighe.put(maxDimensions.get("row").getAsInt(), distribuzioneRighe.get(maxDimensions.get("row").getAsInt()) + 1);
            } else {
                distribuzioneRighe.put(maxDimensions.get("row").getAsInt(), 1);
            }
            if(distribuzioneColonne.containsKey(maxDimensions.get("column").getAsInt())) {
                distribuzioneColonne.put(maxDimensions.get("column").getAsInt(), distribuzioneColonne.get(maxDimensions.get("column").getAsInt()) + 1);
            } else {
                distribuzioneColonne.put(maxDimensions.get("column").getAsInt(), 1);
            }
        }

        for (Integer key : distribuzioneRighe.keySet()) {
            System.out.println(distribuzioneRighe.get(key)+" tabelle hanno "+key+" righe");
        }
        System.out.println("\n\n\n");
        for (Integer key : distribuzioneColonne.keySet()) {
            System.out.println(distribuzioneColonne.get(key)+" tabelle hanno "+key+" colonne");
        }
    }



    //TODO Distribuzione valori distinti (quante colonne hanno 1, 2, 3, 4, etc valori distinti)
    /**
     * Altra mappa che usa come chiave il valore di cleanedText e ogni volta che trova esattamente quella chiave
     * in una tabella ne incrementa il valore.
     */
    public void calcolaDistribuzioneValoriDistinti() throws IOException {
        HashMap<Integer,Integer> distribuzioneValoriDistinti = new HashMap<Integer,Integer>();

        BufferedReader br = new BufferedReader(new FileReader(jsonFilePath));
        String line;

        while ((line=br.readLine()) != null) {
            JsonObject table = JsonParser.parseString(line).getAsJsonObject();
            JsonArray celle = (JsonArray) table.get("cells");
            ArrayList<String> valoriDistintiPerTabella = new ArrayList<String>();

            for (Object c : celle) {
                JsonObject cella = (JsonObject) c;
                if(!(valoriDistintiPerTabella.contains(cella.get("cleanedText").getAsString()))) {  //se la lista non contiene quel valore
                    valoriDistintiPerTabella.add(cella.get("cleanedText").getAsString());
                }
            }

            int dimensioneLista = valoriDistintiPerTabella.size();
            if(distribuzioneValoriDistinti.containsKey(dimensioneLista)) {
                distribuzioneValoriDistinti.put(dimensioneLista, distribuzioneValoriDistinti.get(dimensioneLista)+1);
            } else {
                distribuzioneValoriDistinti.put(dimensioneLista, 1);
            }
        }

        for (Integer key : distribuzioneValoriDistinti.keySet()) {
            System.out.println(distribuzioneValoriDistinti.get(key)+" tabelle hanno "+key+" valori distinti");
        }
    }

    public void startTimer(String infoTime){
        startTime = Instant.now().toEpochMilli();
        this.infoTime = infoTime;
    }

    public void stopTimer(){
        endTime = Instant.now().toEpochMilli();
        double elapsedTime = (endTime - startTime)/1000.0;
        System.out.println("Tempo di esecuzione per " + infoTime + ": " + elapsedTime + " secondi");

        //stampa un log su file text

        try (FileWriter writer = new FileWriter("Tempi di esecuzione.txt", true)) {
            writer.write("Tempo di esecuzione per " + infoTime + " : " + elapsedTime + " secondi" + System.lineSeparator());
        } catch (IOException e) {
            System.out.println("Errore durante la scrittura sul file: " + e.getMessage());
        }
    }

}



