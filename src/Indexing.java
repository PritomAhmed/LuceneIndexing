import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Iterator;

/**
 * Created by pritom on 4/21/2015.
 */
public class Indexing {
    public static final String FILES_TO_INDEX_DIRECTORY = "E:\\GitHub\\LuceneIndexing\\input";
    public static final String INDEX_DIRECTORY = "E:\\GitHub\\LuceneIndexing\\indexDirectory";

    public static final String FIELD_PATH = "E:\\GitHub\\LuceneIndexing";
    public static final String FIELD_CONTENTS = "contents";

    public static void main(String[] args) throws Exception {
        constructIndex();
        searchLuceneIndex("edu");
        searchLuceneIndex("California");
//        createIndex();
//        Searching.searchIndex("california");
//        Searching.searchIndex("education");
    }

    /*public static void createIndex() throws IOException {
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
    }*/

    public static void constructIndex() throws URISyntaxException, IOException {
        boolean create = true;
        Directory dir = FSDirectory.open(new File(INDEX_DIRECTORY).toPath());
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

        if (create) {
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        } else {
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        }

        IndexWriter writer = new IndexWriter(dir, iwc);
        Path docDir = Paths.get(FILES_TO_INDEX_DIRECTORY);
        File[] files = docDir.toFile().listFiles();
        for (File file : files) {
            indexDocs(writer, file.toPath());
        }

        writer.close();
    }

    public static void indexDocs(IndexWriter writer, Path file) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {
            Document doc = new Document();
            Field filePath = new StringField("path", file.toString(),
                    Field.Store.YES);
            doc.add(filePath);
            String title = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).readLine();
            doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));
            doc.add(new StringField("title", title == null ? "" : title, Field.Store.YES));
            if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
                writer.addDocument(doc);
            } else {
                writer.updateDocument(new Term("path", file.toString()), doc);
            }

        }
    }

   /* public static void searchIndex(String searchString) throws ParseException, IOException {
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
    }*/

    public static void searchLuceneIndex(String searchQuery) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException {
        String index = "E:\\GitHub\\LuceneIndexing\\indexDirectory";
        String field = "contents";

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();

        QueryParser parser = new QueryParser(field, analyzer);
        Query query = parser.parse(searchQuery);

        TopDocs results = searcher.search(query, 100);
        ScoreDoc[] hits = results.scoreDocs;

        System.out.println(results.totalHits + "total matching documents");
        for (ScoreDoc hit : hits) {
            Document doc = searcher.doc(hit.doc);
            String path = doc.get("path");
            String title = doc.get("title");
            System.out.println("path " + path + " title " + title);
        }

        reader.close();
    }


}