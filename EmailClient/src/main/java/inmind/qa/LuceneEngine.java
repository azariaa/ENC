import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zal (Salvador Medina)
 * @version 0.1
 */
public class LuceneEngine {
  StandardAnalyzer  LE_InputAnalyzer; // Text analyzer for fact input and query
  IndexWriter       LE_IndexWriter;   // Index writer instance
  Directory         LE_IndexDir;      // Index location in local disk
  
  /**
   * 
   * @param indexPath Location in local disk where the index is going to be stored
   * @throws IOException Thrown when the indexPath cannot be accessed
   */
  public LuceneEngine(String indexPath) throws IOException{
    //StandardAnalyzer: 
    //tokenizes, removes punctutation marks and stopwords and lowerercases
    LE_InputAnalyzer = new StandardAnalyzer(); 

    // set the index location and create its writer
    //TODO: Check which other indexes exist as in DB or local disk
    LE_IndexDir = FSDirectory.open(Paths.get(indexPath));
    IndexWriterConfig config = new IndexWriterConfig(LE_InputAnalyzer);
    LE_IndexWriter = new IndexWriter(LE_IndexDir, config);
  }
  
  /**
   * 
   * @param fact Piazza fact to be stored
   * @throws IOException Thrown when the index cannot be read/write
   */
  public void addFact(String fact, String source) throws IOException {
    Document doc = new Document(); //Document to be indexed
    
    //TODO: Need to think of more fields in index
    doc.add(new TextField("fact", fact, Field.Store.YES));
    doc.add(new TextField("source", source, Field.Store.YES));
    LE_IndexWriter.addDocument(doc);
  }
  
  /**
   * 
   * @param queryStr  Question to be answered
   * @param nHits     Max number of answers to be returned
   * @return          List of the top answers
   * @throws IOException Thrown when the index cannot be read/write
   * @throws ParseException Thrown when the query string is in an invalid encoding 
   */
  public List<String> query(String queryStr, int nHits) throws IOException, ParseException {
    List<String> queryRes = new ArrayList<String>();
   
    Query q = new QueryParser( "fact", LE_InputAnalyzer).parse(queryStr);
    
    IndexReader reader = DirectoryReader.open(LE_IndexDir);
    IndexSearcher searcher = new IndexSearcher(reader);
    TopScoreDocCollector collector = TopScoreDocCollector.create(nHits);
    
    searcher.search(q, collector);
    ScoreDoc[] hits = collector.topDocs().scoreDocs;
    
    for(int i=0;i<hits.length;++i) {
      int docId = hits[i].doc;
      Document d = searcher.doc(docId);
      queryRes.add(d.get("fact"));
    }
    
    return queryRes;
  }
}
