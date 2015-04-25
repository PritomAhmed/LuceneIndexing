import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by pritom on 4/21/2015.
 */
public class Searching {
    public static final String INDEX_DIRECTORY = "indexDirectory";
    public static final String FIELD_PATH = "E:\\GitHub\\LuceneIndexing";
    public static final String FIELD_CONTENTS = "contents";

    public static void searchIndex(String searchString) throws ParseException, IOException {
        System.out.println("Searching for '" + searchString + "'");
        Directory directory = FSDirectory.getDirectory(INDEX_DIRECTORY);
        IndexReader indexReader = IndexReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        Analyzer analyzer = new StandardAnalyzer();
        QueryParser queryParser = new QueryParser(FIELD_CONTENTS, analyzer);
        Query query = queryParser.parse(searchString);
        Hits hits = indexSearcher.search(query);
        System.out.println("Number of hits: " + hits.length());

        Iterator<Hit> it = hits.iterator();
        while (it.hasNext()) {
            Hit hit = it.next();
            Document document = hit.getDocument();
            String path = document.get(FIELD_PATH);
            System.out.println("Hit: " + path);
        }
    }
}
