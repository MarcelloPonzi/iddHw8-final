import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AziendaSearcher {

    private IndexReader reader;

    private IndexSearcher searcher;

    private Map<String, Integer> set2count;

    public AziendaSearcher(Directory directory) throws IOException {
        this.reader = DirectoryReader.open(directory);
        this.searcher = new IndexSearcher(reader);
    }

    public String searchAzienda(String terms, int size) throws IOException, ParseException {
        this.set2count = new LinkedHashMap<>();
        QueryParser parser = new QueryParser("keywords", new StandardAnalyzer());
        Query query = parser.parse(terms);
        TopDocs hits = searcher.search(query, size);

        System.out.println("\nRicerca in corso sul termine: " + terms);

        for (int i = 0; i < hits.scoreDocs.length; i++) {
            ScoreDoc scoreDoc = hits.scoreDocs[i];
            Document doc = searcher.doc(scoreDoc.doc);
            String id = String.valueOf(doc.get("id"));

            if (this.set2count.containsKey(id)) {
                this.set2count.put(id, this.set2count.get(id) + 1);
                System.out.println("Incremento contatore per la tabella " + id + " contentente: " + terms);
            } else {
                System.out.println("Aggiungo al set2count la tabella " + id + " contenente: " + terms);
                set2count.put(id, 1);
            }
        }

        Map<String, Integer> sortedMap = set2count
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
        if(!(sortedMap.keySet().iterator().hasNext())){
            System.out.println("Nessun documento trovato per questa azienda");
            return null;
        }
        String highestKey = sortedMap.keySet().iterator().next();
        System.out.println("L'id del documento con il numero di hit più alto è: " + highestKey);
        return highestKey;
    }
}
