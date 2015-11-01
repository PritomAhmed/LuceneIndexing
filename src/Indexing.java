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
    public static String FILES_TO_BE_INDEXED_DIRECTORY = "E:\\GitHub\\result";
    public static String INDEX_DIRECTORY = "E:\\GitHub\\LuceneIndexing\\indexDirectory";

    public static void main(String[] args) throws Exception {
//        FILES_TO_BE_INDEXED_DIRECTORY = args[0];
//        INDEX_DIRECTORY = args[1];
        //int count = 200000;
//        while (count < 400000) {
//            count +=5000;
        long startTime = System.currentTimeMillis();
//        constructIndex(count);
//        int count = constructIndex();
        searchLuceneIndex("Kazi");
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
//            System.out.println("Total Time taken : " + totalTime + " for " + count + " files");

//        System.out.println(totalTime + " " + count);
        System.out.println(totalTime);
//        }
    }

//    public static void constructIndex(int fileCount) throws URISyntaxException, IOException {
    public static int constructIndex() throws URISyntaxException, IOException {
        int count = 0;
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
        Path docDir = Paths.get(FILES_TO_BE_INDEXED_DIRECTORY);
        File[] files = docDir.toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                indexDocs(writer, file.toPath());
                count++;
//                if (fileCount == 0) {
//                    break;
//                }
//                fileCount--;
            }
        }

        writer.close();
        return count;
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
            if (link.startsWith("Text : ")) {
                textBuilder.append(link.substring(6));
            }
            String s;
            while ((s = bufferedReader.readLine()) != null) {
                textBuilder.append(s);
            }

//            doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));
            TextField contentField = new TextField("contents", textBuilder.toString(), Field.Store.NO);
            contentField.setBoost(3);
            doc.add(contentField);

            doc.add(new TextField("title", title == null ? "" : title, Field.Store.YES));
            doc.add(new TextField("metaDescription", metaDescription, Field.Store.NO));
            doc.add(new StringField("url", url, Field.Store.YES));
            //doc.add(new TextField("links", linksBuilder.toString(), Field.Store.NO));
            if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
                writer.addDocument(doc);
            } else {
                writer.updateDocument(new Term("path", file.toString()), doc);
            }

        }
    }

    public static void searchLuceneIndex(String searchQuery) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException {
        String field = "contents";

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_DIRECTORY)));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();

        QueryParser parser = new QueryParser(field, analyzer);
        Query query = parser.parse(searchQuery);

        TopDocs results = searcher.search(query, 10);
        ScoreDoc[] hits = results.scoreDocs;

        System.out.println(results.totalHits + " total matching documents");
        for (ScoreDoc hit : hits) {
            Document doc = searcher.doc(hit.doc);
//            String path = doc.get("path");
            String url = doc.get("url");
            String title = doc.get("title");
            String metaDescription = doc.get("metaDescription");
            System.out.println("url " + url + " title " + title + " meta description " + metaDescription);
        }
        reader.close();
    }




}