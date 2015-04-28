import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;

/**
 * Created by pritom on 4/21/2015.
 */
public class Indexing {
//    public static final String FILES_TO_INDEX_DIRECTORY = "E:\\GitHub\\LuceneIndexing\\input";
    public static final String FILES_TO_INDEX_DIRECTORY = "E:\\GitHub\\WebCrawler\\output";
    public static final String INDEX_DIRECTORY = "E:\\GitHub\\LuceneIndexing\\indexDirectory";

    public static final String FIELD_PATH = "E:\\GitHub\\LuceneIndexing";
    public static final String FIELD_CONTENTS = "contents";

    public static void main(String[] args) throws Exception {
        int count = 0;
        while (count < 500000) {
            count +=5000;
            long startTime = System.currentTimeMillis();
            constructIndex(count);
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            System.out.println("Total Time taken : " + totalTime + " for " + count + " files");
        }
//        searchLuceneIndex("edu");
//        searchLuceneIndex("California");
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

    public static void constructIndex(int fileCount) throws URISyntaxException, IOException {
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
        if (files != null) {
            for (File file : files) {
                indexDocs(writer, file.toPath());
                if (fileCount == 0) {
                    break;
                }
                fileCount--;
            }
        }

        writer.close();
    }

    public static void indexDocs(IndexWriter writer, Path file) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {
            Document doc = new Document();
            Field filePath = new StringField("path", file.toString(), Field.Store.YES);
            doc.add(filePath);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            String title = bufferedReader.readLine();
            String metaDescription = bufferedReader.readLine();
            if (metaDescription != null && metaDescription.startsWith("Description : ")) {
                metaDescription = metaDescription.substring(14);
//                System.out.println(metaDescription);
            } else {
                metaDescription = "";
            }

            String url = bufferedReader.readLine();
            if (url != null && url.startsWith("URL : ")) {
                url = url.substring(6);
            } else {
                url = "";
            }

            StringBuilder linksBuilder = new StringBuilder();
            String linkStart = bufferedReader.readLine();
            String link = "";
            if (linkStart != null && linkStart.startsWith("Links : ")) {
                link = bufferedReader.readLine();
                while (!link.startsWith("Text : ")) {
                    linksBuilder.append(link);
                    link = bufferedReader.readLine();
                }
            }

            StringBuilder textBuilder = new StringBuilder();
            String s;
            while ((s=bufferedReader.readLine())!=null) {
                textBuilder.append(s);
            }

//            doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));
            doc.add(new TextField("contents", textBuilder.toString(), Field.Store.NO));
            doc.add(new StringField("title", title == null ? "" : title, Field.Store.YES));
            doc.add(new StringField("metaDescription", metaDescription, Field.Store.YES));
            doc.add(new StringField("url", url, Field.Store.YES));
            doc.add(new TextField("links", linksBuilder.toString(), Field.Store.NO));
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

        System.out.println(results.totalHits + " total matching documents");
        for (ScoreDoc hit : hits) {
            Document doc = searcher.doc(hit.doc);
            String path = doc.get("path");
            String url = doc.get("url");
            String title = doc.get("title");
            String metaDescription = doc.get("metaDescription");
            System.out.println("url " + url + " title " + title + " meta description " + metaDescription);
        }
        reader.close();
    }


}