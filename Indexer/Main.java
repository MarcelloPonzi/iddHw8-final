import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


public class Main {

	private static final String PATH_CSV_INPUT = "MergedDS/dummy180K.csv";
	private static final String PATH_CSV_OUTPUT = "MergedDS/dsAziendeTable.csv";

	public static void main(String[] args) throws IOException, ParseException {

//		//FASE DI INDICIZZAZIONE COMMENTABILE SE GIà ESEGUITA!!
//		// questo costruttore invoca a sua volta il costrutture del parser con il path inserito
//		//Il file tables.json va inserito manualmente all'interno delle Resources
//		LuceneIndexWriter liw = new LuceneIndexWriter("tables/tables.json");
//
//		/**
//		 * Questo metodo dovrebbe evitare il problema della memoria facendo commit ad ogni riga
//		 */
//		liw.parseAndCreateIndex("tables/tables.json");
//
//		/**
//		 * Calcolo delle statistiche
//		 */
		Statistiche statistiche = new Statistiche("tables/tables.json");
//		statistiche.calcolaNumeroMedioDiDimensioni();
//		statistiche.calcolaNumeroMedioValoriNulli();
//		statistiche.calcolaDistribuzioneRigheColonne();
//		statistiche.calcolaDistribuzioneValoriDistinti();

		Path idxpath = Paths.get("target/idx");
		Directory idxdir = FSDirectory.open(idxpath);
		AziendaSearcher as = new AziendaSearcher(idxdir); //questo ci mette tanto perchè legge tutto l'indice
		/**
		 * Passaggio di un csv in input per la query
		 */
		//Avvio timer lettura del csv e creazione oggetti
		statistiche.startTimer("lettura del csv e creazione oggetti");

		//Istanze
		CSVReader reader = new CSVReader();
		CSVWriter writer = new CSVWriter(PATH_CSV_OUTPUT);

		//Creazione lista di oggetti contenenti i record delle aziende del csv
		List<RecordAzienda> aziende = reader.readCSV(PATH_CSV_INPUT);

		//Fermo timer lettura del csv e creazione oggetti
		statistiche.stopTimer();

		//Avvio timer query in tables per ogni RecordAzienda
		statistiche.startTimer("query dei RecordAzienda");

		//query tra i docs per ogni record con assegnazione successiva del migliore all'azienda
		for (RecordAzienda azienda : aziende){
			String linkDB = as.searchAzienda(azienda.getName(), 1);
			azienda.setLinkDB(linkDB);
			azienda.toString();
		}

		//Fermo timer query in tables per ogni RecordAzienda
		statistiche.stopTimer();

		//Avvio timer per scrittura dei RecordAzienda su csv
		statistiche.startTimer("scrittura dei RecordAzienda su csv");
		/**
		 * Codice che con input array di oggetti {@link RecordAzienda} produce un csv
		 */
		writer.writeCsvFromObject(aziende);

		//Fermo timer per scrittura dei RecordAzienda su csv
		statistiche.stopTimer();
	}
}
