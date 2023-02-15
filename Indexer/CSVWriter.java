import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVWriter {

    private String csvOutPath;
    public CSVWriter(String csvOutPath) {
        this.csvOutPath = csvOutPath;
    }
    public void writeCsvFromObject(List<RecordAzienda> aziende) {
        try {
            FileWriter writer = new FileWriter(this.csvOutPath);
            // Scrivi l'intestazione del file
            writer.append(";name;market_cap;country;ceo;industry;web_page;founded_year;stock;db_page\n");

            // Scrivi ogni riga del file
            for (RecordAzienda azienda : aziende) {
                writer.append(String.valueOf(aziende.indexOf(azienda)));
                writer.append(";"+ azienda.getName() + ";");
                writer.append(azienda.getMarketCap() + ";");
                writer.append(azienda.getCountry() + ";");
                writer.append(azienda.getCeo() + ";");
                writer.append(azienda.getIndustry() + ";");
                writer.append(azienda.getWebPage() + ";");
                writer.append( azienda.getFoundedYear() + ";");
                writer.append(azienda.getStock() + ";");
                writer.append(azienda.getLinkDB() + ";");
                writer.append("\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

