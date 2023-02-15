import java.io.FileWriter;
import java.io.IOException;

public class CSVDummy {

    public static void main(String[] args) throws IOException{
        try {
            FileWriter writer = new FileWriter("MergedDS/dummy180K.csv");
            // Scrivi l'intestazione del file
            writer.append(";name;market_cap;country;ceo;industry;web_page;founded_year;stock;db_page\n");

            // Scrivi ogni riga del file
            for (int i =0; i<=180000; i++) {
                writer.append("56410;REGIONS FINANCIAL CORPORATION;23.95 Billion USD;USA;John M. Turner Jr.;;;1971.0;\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
