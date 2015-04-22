import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Created by pritom on 4/21/2015.
 */
public class Indexing {
    public static final String FILES_TO_INDEX_DIRECTORY = "input";
    public static final String INDEX_DIRECTORY = "indexDirectory";

    public static final String FIELD_PATH = "E:\\GitHub\\LuceneIndexing";
    public static final String FIELD_CONTENTS = "contents";

    public static void main(String[] args) throws Exception {
        createIndex();
//        Searching.searchIndex("california");
//        Searching.searchIndex("education");
    }

    public static void createIndex() throws IOException {
        Analyzer analyzer = new StandardAnalyzer();
        boolean recreateIndexIfExists = true;
        IndexWriter indexWriter = new IndexWriter(INDEX_DIRECTORY, analyzer, recreateIndexIfExists);
        File dir = new File(FILES_TO_INDEX_DIRECTORY);
        File[] files = dir.listFiles();
        for (File file : files) {
            Document document = new Document();

            String path = file.getCanonicalPath();
            document.add(new Field(FIELD_PATH, path, Field.Store.YES, Field.Index.UN_TOKENIZED));

            Reader reader = new FileReader(file);
            document.add(new Field(FIELD_CONTENTS, reader));

            indexWriter.addDocument(document);
        }
        indexWriter.optimize();
        indexWriter.close();
    }
}
