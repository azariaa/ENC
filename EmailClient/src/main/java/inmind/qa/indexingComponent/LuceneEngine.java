package main.java.inmind.qa.indexingComponent;


import com.google.gson.Gson;
import main.java.inmind.qa.models.PiazzaAttachment;
import main.java.inmind.qa.models.PiazzaFeedObject;
import main.java.inmind.qa.utils.Utility;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zal (Salvador Medina)
 * @modified suruchis (Suruchi Shah)
 * @version 0.1
 */
public class LuceneEngine {

  private static double SCORE_THRESHOLD = 0.02;
  /**
   * This method adds a document to be indexed in Lucene
   * Source: PIAZZA Feed
   * @param object PiazzaFeedObject type
   * @throws Exception
   */
  public void addDocument(PiazzaFeedObject object) throws Exception  {
    Document doc = new Document(); // Document to be indexed
    java.lang.reflect.Field[] fields = PiazzaFeedObject.class.getDeclaredFields();
    for (int fieldIndex = 0; fieldIndex < fields.length; fieldIndex++)  {
      fields[fieldIndex].setAccessible(true);
      String fieldName = fields[fieldIndex].getName();
      if(fieldName.equals("followups")) {
        String jsonFollowups = new Gson().toJson(object.followups);
        doc.add(new TextField(fieldName, jsonFollowups, Field.Store.YES));
      } else {
        doc.add(new TextField(fieldName, object.getFieldValue(fieldName).toString(), Field.Store.YES));
      }
    }
    Index.INDEXWRITER.addDocument(doc);
    Index.INDEXWRITER.commit();
  }

  /**
   *
   * @param piazzaAttachment
   * @throws Exception
   */
  public void addDocument(PiazzaAttachment piazzaAttachment) throws Exception {
    Document doc = new Document();
    java.lang.reflect.Field[] fields = PiazzaAttachment.class.getDeclaredFields();
    for (int fieldIndex = 0; fieldIndex < fields.length; fieldIndex++) {
      fields[fieldIndex].setAccessible(true);
      String fieldName = fields[fieldIndex].getName();
      doc.add(new TextField(fieldName, piazzaAttachment.getFieldValue(fieldName).toString(), Field.Store.YES));
    }
    Index.INDEXWRITER.addDocument(doc);
    Index.INDEXWRITER.commit();
  }

  /**
   * This is used to get the string content from only matching fields for the matching documents
   * @param query  Question to be answered
   * @param fields     Fields in which the query terms will be searched
   * @param nHits     Max number of answers to be returned
   * @return          List of the top answers
   * @throws IOException Thrown when the index cannot be read/write
   * @throws ParseException Thrown when the query string is in an invalid encoding 
   */
    public List<String> searchIndex(Query query, String rawQuery, String[] fields, int nHits) throws IOException, ParseException {
    List<String> queryRes = new ArrayList<String>();

    if(Index.getINDEXREADER() != null) {
      IndexSearcher indexSearcher = new IndexSearcher(Index.getINDEXREADER());
      TopScoreDocCollector collector = TopScoreDocCollector.create(nHits);
      indexSearcher.search(query, collector);

      ScoreDoc[] hits = collector.topDocs().scoreDocs;

      for (int i = 0; i < hits.length; ++i) {
        if(hits[i].score > SCORE_THRESHOLD || hits.length<=2) {
          int docId = hits[i].doc;
          Document d = indexSearcher.doc(docId);
          for (String field : fields) {
            QueryParser queryParser = new QueryParser(field, Index.INPUTANALYZER);
            queryParser.setDefaultOperator(QueryParser.OR_OPERATOR);
            Query tempQuery = queryParser.parse(rawQuery);
            Explanation ex = indexSearcher.explain(tempQuery, docId);
            if (ex.isMatch()) {
              queryRes.add(docId + "~~" + field +  "~~" + d.get(field));
            }
          }
        }
      }

      // If no documents are selected even though there were hits, return the top hit
      if(queryRes.size() == 0 && hits.length>0) {
        int docId = hits[0].doc;
        Document d = indexSearcher.doc(docId);

        for (String field : fields) {
          QueryParser queryParser = new QueryParser(field, Index.INPUTANALYZER);
          queryParser.setDefaultOperator(QueryParser.OR_OPERATOR);
          Query tempQuery = queryParser.parse(rawQuery);
          Explanation ex = indexSearcher.explain(tempQuery, docId);
          if (ex.isMatch()) {
            queryRes.add(docId + "~~" + field +  "~~" + d.get(field));
          }
        }
      }
    }
    return queryRes;
  }

  /**
   * The index search returns the PiazzaFeedObject class type objects extracted from the matching documents
   * Score threshold should be greater than SCORE_THRESHOLD
   * @param query query
   * @param nHits number of hits
   * @return List of PiazzaFeedObject
   * @throws IOException
   * @throws ParseException
   */
  public List<Object> searchIndexForMatchingDocuments(Query query, int nHits) throws IOException, ParseException  {

    List<Object> returnDocuments = new ArrayList<Object>();

    if(Index.getINDEXREADER()!=null) {
      IndexSearcher indexSearcher = new IndexSearcher(Index.getINDEXREADER());
      TopScoreDocCollector collector = TopScoreDocCollector.create(nHits);

      indexSearcher.search(query, collector);

      ScoreDoc[] hits = collector.topDocs().scoreDocs;
      System.out.println("Found " + collector.getTotalHits() + " hit(s).");

      PiazzaFeedObject piazzaFeedObject = null;
      PiazzaAttachment piazzaAttachment = null;
      for (int i = 0; i < hits.length; ++i) {
        if(hits[i].score > SCORE_THRESHOLD || hits.length<=2) { // We only consider a document if there is a match greater than 0.2 threshold
          int docId = hits[i].doc;
          Document d = indexSearcher.doc(docId);

          // If returned document is of type "post"
          if (d.get(Utility.SOURCE_TYPE).equals(Utility.SOURCE_TYPE_POST))  {
            piazzaFeedObject = new PiazzaFeedObject();
            java.lang.reflect.Field[] piazzaFeedObjectFields = PiazzaFeedObject.class.getDeclaredFields();
            for (int objFieldIndex = 0; objFieldIndex < piazzaFeedObjectFields.length; objFieldIndex++) {
              piazzaFeedObjectFields[objFieldIndex].setAccessible(true);
              String classFieldName = piazzaFeedObjectFields[objFieldIndex].getName();
              if(classFieldName.equals("indexedDocId"))
                piazzaFeedObject.setFieldValue(classFieldName, docId);
              else
                piazzaFeedObject.setFieldValue(classFieldName, d.get(classFieldName));
            }
            returnDocuments.add(piazzaFeedObject);
          }
          // Else if returned document is of type "attachment"
          else if (d.get(Utility.SOURCE_TYPE).equals(Utility.SOURCE_TYPE_ATTACHMENT))  {
            piazzaAttachment = new PiazzaAttachment();
            java.lang.reflect.Field[] piazzaAttachmentFields = PiazzaAttachment.class.getDeclaredFields();
            for (int objFieldIndex = 0; objFieldIndex < piazzaAttachmentFields.length; objFieldIndex++) {
              piazzaAttachmentFields[objFieldIndex].setAccessible(true);
              String classFieldName = piazzaAttachmentFields[objFieldIndex].getName();
              if(classFieldName.equals("indexedDocId"))
                piazzaAttachment.setFieldValue(classFieldName, docId);
              else
                piazzaAttachment.setFieldValue(classFieldName, d.get(classFieldName));
            }
            returnDocuments.add(piazzaAttachment);
          }
        }
      }

      // If no documents are selected even though there were hits, return the top hit
      if(returnDocuments.size() == 0 && hits.length>0) {
        int docId = hits[0].doc;
        Document d = indexSearcher.doc(docId);
        if (d.get(Utility.SOURCE_TYPE).equals(Utility.SOURCE_TYPE_POST)) {
          piazzaFeedObject = new PiazzaFeedObject();
          java.lang.reflect.Field[] piazzaFeedObjectFields = PiazzaFeedObject.class.getDeclaredFields();
          for (int objFieldIndex = 0; objFieldIndex < piazzaFeedObjectFields.length; objFieldIndex++) {
            piazzaFeedObjectFields[objFieldIndex].setAccessible(true);
            String classFieldName = piazzaFeedObjectFields[objFieldIndex].getName();
            if (classFieldName.equals("indexedDocId"))
              piazzaFeedObject.setFieldValue(classFieldName, docId);
            else
              piazzaFeedObject.setFieldValue(classFieldName, d.get(classFieldName));
          }
          returnDocuments.add(piazzaFeedObject);
        }
        else if (d.get(Utility.SOURCE_TYPE).equals(Utility.SOURCE_TYPE_ATTACHMENT))  {
          piazzaAttachment = new PiazzaAttachment();
          java.lang.reflect.Field[] piazzaAttachmentFields = PiazzaAttachment.class.getDeclaredFields();
          for (int objFieldIndex = 0; objFieldIndex < piazzaAttachmentFields.length; objFieldIndex++) {
            piazzaAttachmentFields[objFieldIndex].setAccessible(true);
            String classFieldName = piazzaAttachmentFields[objFieldIndex].getName();
            if(classFieldName.equals("indexedDocId"))
              piazzaAttachment.setFieldValue(classFieldName, docId);
            else
              piazzaAttachment.setFieldValue(classFieldName, d.get(classFieldName));
          }
          returnDocuments.add(piazzaAttachment);
        }
      }
    }
    return  returnDocuments;
  }

  /**
   * Check if the Piazza post already exists. If it does, we do not index it again
   * @param queryStr id String
   * @param field "id"
   * @param nHits 1
   * @return boolean value
   * @throws IOException
   * @throws ParseException
   */
  public boolean ifPostExists(String queryStr, String field, int nHits) throws IOException, ParseException {
    QueryParser queryParser = new QueryParser(field, Index.INPUTANALYZER);
    Query query = queryParser.parse(queryStr);
    IndexReader reader = Index.getINDEXREADER();
    if(reader!=null) {
      IndexSearcher indexSearcher = new IndexSearcher(Index.getINDEXREADER());
      TopScoreDocCollector collector = TopScoreDocCollector.create(nHits);
      indexSearcher.search(query, collector);
      ScoreDoc[] hits = collector.topDocs().scoreDocs;
      if (hits.length > 0) {
        return true;
      }
    }

    return false;
  }

  /**
   *
   * @param queryStr    Question to be answered
   * @param field       Field in which the query terms will be searched
   * @param returnField Field whose value needs to be returned
   * @param nHits       Max number of answers to be returned
   * @return            List of the top answers
   * @throws IOException
   * @throws ParseException
   */
  public List<String> searchIndexDefineReturnObjects(String queryStr, String field, List<String> returnField, int nHits) throws IOException, ParseException {
    List<String> queryRes = new ArrayList<String>();

    QueryParser queryParser = new QueryParser(field, Index.INPUTANALYZER);
    Query query = queryParser.parse(queryStr);
    if(Index.getINDEXREADER()!=null) {
      IndexSearcher indexSearcher = new IndexSearcher(Index.getINDEXREADER());
      TopScoreDocCollector collector = TopScoreDocCollector.create(nHits);
      indexSearcher.search(query, collector);

      ScoreDoc[] hits = collector.topDocs().scoreDocs;
      System.out.println("Found " + collector.getTotalHits() + " hit(s).");

      for (int i = 0; i < hits.length; ++i) {
        int docId = hits[i].doc;
        Document d = indexSearcher.doc(docId);
        for (int j = 0; j < returnField.size(); j++)
          queryRes.add(docId + "~" + returnField.get(j) + "~:" + d.get(returnField.get(j)));
      }
    }
    return queryRes;
  }


  /**
   * Delete the documents from the index consisting the term
   * @param term
   */
  public void deleteDocument(Term term) {
    try {
      Index.INDEXWRITER.deleteDocuments(term);
      Index.INDEXWRITER.commit();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
