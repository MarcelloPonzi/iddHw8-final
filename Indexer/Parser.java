import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Parser {

    private String jsonFilePath;

    public Parser(String path) {
        this.jsonFilePath = path;
    }

    /**
     * Parse a Json file.
     */
    public JsonArray parseJSONFile() {

        //Get the JSON file, in this case is in ~/Resources/sampleDataSet.json
        JsonArray jsonArray = new JsonArray();
        try {

            //Si legge il file riga riga per riga eseguendo il parse, da testare
            BufferedReader bf = new BufferedReader(new FileReader(jsonFilePath));
            String s;

            //per evitare il problema dell'out of memory di Java teniamo un ciclo ridotto
            //che scorre soltanto le prime i linee (quindi tabelle)
            int i = 10;
            while ( i>0) {
                s = bf.readLine();
                JsonObject object = JsonParser.parseString(s).getAsJsonObject();
                jsonArray.add(object);
                i--;
            }
            //Metodo precedente, commentato perchè non funzionante con la formattazione del table.json
            //jsonArray = (JSONArray)new JSONParser().parse(new FileReader(jsonFilePath));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return jsonArray;
    }


}