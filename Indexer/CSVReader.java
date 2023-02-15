import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


public class CSVReader {
    private final String NOMI_COLONNE = ",name,market_cap,country,ceo,industry,web_page,founded_year,stock";
    private final String TIPO_SEPARATORE = ";";
    public List<RecordAzienda> readCSV(String filePath) {
        List<RecordAzienda> aziende = new LinkedList<>();
        String line;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            while ((line = br.readLine()) != null) {
                //soluzione naive per fargli scartare la prima riga
                //if(!(line.equals(NOMI_COLONNE)) ) //condizionale commentata perchè ho eliminato la prima riga manualmente

                String[] values = line.split(TIPO_SEPARATORE + "(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                RecordAzienda azienda = new RecordAzienda(values[1], values[2], values[3], values[4], values[5], values[6], values[7], values[8]);
                aziende.add(azienda);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return aziende;
    }

}

