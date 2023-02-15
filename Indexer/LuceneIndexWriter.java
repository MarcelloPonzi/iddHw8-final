import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LuceneIndexWriter {

	private String indexPath = "target/idx";

	private IndexWriter indexWriter = null;

	private Parser parser;

	public int tableCounter = 0;

	public LuceneIndexWriter(String path){
		this.parser = new Parser(path);
	}

	/**
	 * Non serve restituire tableCounter perché nel main viene acceduto direttamente come parametro
	 * dell'istanza di LuceneIndexWriter.
	 * @param jsonFilePath
	 * @throws IOException
	 */
	public void parseAndCreateIndex(String jsonFilePath) throws IOException {
		//Apro l'indice
		Path indexPath = Paths.get(this.indexPath);
		try {
			Directory dir = FSDirectory.open(indexPath);
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setCodec(new SimpleTextCodec());

			//Riscrivi la directory
			iwc.setOpenMode(OpenMode.CREATE);
			indexWriter = new IndexWriter(dir, iwc);
		} catch (Exception e) {
			System.err.println("Error opening the index. " + e.getMessage());
		}

		/**
		 * Inizializzo le variabili che mi servono per la lettura
 		 */
		BufferedReader br = new BufferedReader(new FileReader(jsonFilePath));
		String line;
		this.tableCounter = 0;								//inizializzo tableCounter a zero

		/**
		 * Ciclo su tutte le righe del file e per ciascuna faccio parsing e inserisco nell'indice
		 */
		while ((line=br.readLine()) != null) {				//finché il file contiene ancora righe scorri
			this.tableCounter++;							//incremento il numero di tabelle
			JsonObject table = JsonParser.parseString(line).getAsJsonObject();	//faccio parse dell'oggetto
			String id = String.valueOf(table.get("id"));
			Document doc = new Document();
			JsonArray celle = (JsonArray) table.get("cells");
			doc.add(new TextField("id", id, Field.Store.YES ));
			//System.out.println("Creato doc con id: " + id);
			String cleanedCells = "";

			for (Object c : celle) {
				JsonObject cella = (JsonObject) c;
				cleanedCells = cleanedCells.concat(cella.get("cleanedText").toString()+"\n");
			}

			doc.add(new TextField("keywords",cleanedCells ,Field.Store.YES));
			//System.out.println("Aggiunte al doc le celle con parole chiave " + cleanedCells);
			indexWriter.addDocument(doc);

			if (this.tableCounter % 100000 == 0) {
				System.out.println("Indicizzati " + this.tableCounter + " documenti.");
				indexWriter.commit();			// qui faccio il commit, provare anche a metterlo fuori dal ciclo
			}

		}
		System.out.println("LUCENE INDEX WRITER: Sono stati indicizzati "+this.tableCounter+" documenti.");
		indexWriter.close();
	}

	public void createIndex(){
		JsonArray jsonArray = parser.parseJSONFile();
		openIndex(indexPath);		
		tableCounter = indexDocs(jsonArray);
		finish();
	}

	private int indexDocs(JsonArray jsonArray) {
		int counter = 0;	//variabile importante per eseguire correttamente il merge nel main
		try {
			for (Object o : jsonArray) {	//questo for scorre le tabelle
				counter++;
				JsonObject table = (JsonObject) o;
				String id = String.valueOf(table.get("id"));
				Document doc = new Document();


				// loop array of cells (righe)
				JsonArray celle = (JsonArray) table.get("cells");
				doc.add(new TextField("id", id, Field.Store.YES ));
				System.out.println("Creato doc con id: " + id);
				String cleanedCells = "";

				for (Object c : celle) {
					JsonObject cella = (JsonObject) c;
					cleanedCells = cleanedCells.concat(cella.get("cleanedText").toString()+"\n");
				}

				doc.add(new TextField("keywords",cleanedCells ,Field.Store.YES));
				System.out.println("Aggiunte al doc le celle con parole chiave " + cleanedCells);
				indexWriter.addDocument(doc);
			}
		} catch (IOException e) {
			System.out.println(e);
		}
		System.out.println("LUCENE INDEX WRITER: Sono stati indicizzati "+counter+" documenti.");
		return counter;
	}

	public boolean openIndex(String path){
		Path indexPath = Paths.get(path);
		try {
			Directory dir = FSDirectory.open(indexPath);						
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setCodec(new SimpleTextCodec());

			//Riscrivi la directory
			iwc.setOpenMode(OpenMode.CREATE);
			indexWriter = new IndexWriter(dir, iwc);

			return true;
		} catch (Exception e) {
			System.err.println("Error opening the index. " + e.getMessage());
		}
		return false;
	}


	/**
	 * Write the document to the index and close it
	 */
	public void finish(){
		try {
			indexWriter.commit();
			indexWriter.close();
		} catch (IOException ex) {
			System.err.println("We had a problem closing the index: " + ex.getMessage());
		}
	}


}