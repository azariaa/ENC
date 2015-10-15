package EmailClient.src.main.java.inmind.qa.indexingComponent;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 *  The interface to the Lucene index.
 *  @author suruch is (Suruchi Shah)
 *  @date 10/12/15
 */
public class Index {

    private static IndexReader INDEXREADER=null;
    private static DocLengthStore DOCLENGTHSTORE;
    private static Directory INDEXDIRECTORY = null;
    public static IndexWriter INDEXWRITER = null;
    public static EnglishAnalyzer INPUTANALYZER;

    public static void initialize (String indexPath) throws IllegalArgumentException, IOException {
        INPUTANALYZER = new EnglishAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(INPUTANALYZER);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        INDEXDIRECTORY = FSDirectory.open(Paths.get(indexPath));

        Index.INDEXWRITER = new IndexWriter(INDEXDIRECTORY, config);

        //  Open the Lucene index
        if(DirectoryReader.indexExists(INDEXDIRECTORY)) {
            Index.INDEXREADER = DirectoryReader.open(INDEXDIRECTORY);
            //  Lucene doesn't store field lengths the way that we want them, so we have our own document length store.
            Index.DOCLENGTHSTORE = new DocLengthStore (Index.INDEXREADER);
            if (Index.DOCLENGTHSTORE == null) {
                throw new IllegalArgumentException ("Unable to open the document length store.");
            }
        }
    }

    /**
     *  Get the number of documents that contain the specified field.
     *  @param fieldName the field name
     *  @return the number of documents that contain the field
     *  @throws IOException Error accessing the Lucene index.
     */
    static int getDocCount (String fieldName) throws IOException {
        return getINDEXREADER().getDocCount (fieldName);
    }

    /**
     *  Get the total number of term occurrences contained in all
     *  instances of the specified field in the corpus (e.g., add up the
     *  lengths of every TITLE field in the corpus).
     *  @param fieldName The field name.
     *  @return The total number of term occurrence
     *  @throws IOException Error accessing the Lucene index.
     */
    public static long getSumOfFieldLengths (String fieldName)
            throws IOException {
        return getINDEXREADER().getSumTotalTermFreq(fieldName);
    }

    public static IndexReader getINDEXREADER() throws IOException {
        if(DirectoryReader.indexExists(INDEXDIRECTORY) && INDEXREADER == null) {
            INDEXREADER = DirectoryReader.open(INDEXDIRECTORY);
            return INDEXREADER;
        }
        else if (DirectoryReader.indexExists(INDEXDIRECTORY)) {
            DirectoryReader dirReader = DirectoryReader.open(INDEXDIRECTORY);
            DirectoryReader newReader = DirectoryReader.openIfChanged(dirReader);
            if(newReader!=null) {
                return newReader;
            }
            else    {
                return INDEXREADER;
            }
        }
        return null;
    }
}
