package EmailClient.src.main.java.inmind.testing;


import java.io.FileReader;
import java.util.Date;
import java.util.Properties;

import EmailClient.src.main.java.inmind.email.EmailOperations;
import EmailClient.src.main.java.inmind.piazza.PiazzaApi;
import EmailClient.src.main.java.inmind.qa.answeringComponent.AnswerPreprocess;
import EmailClient.src.main.java.inmind.qa.indexingComponent.Index;
import EmailClient.src.main.java.inmind.qa.indexingComponent.IndexingOperations;

/**
 * Created by ziy on 8/5/15.
 * @modifiedby suruchis (Suruchi Shah)
 */
public class Main {

    public Main() {
    }

    public static void main(String[] args) throws Exception {
      Properties prop = new Properties();
      prop.load(new FileReader("default.properties"));
      String username = prop.getProperty("username");
      String password = prop.getProperty("password");
      String filePath = prop.getProperty("emailOutputFilePath");
      String pythonScriptPaths = prop.getProperty("pythonScriptsPath");
      String sourceDomainFilePath = prop.getProperty("sourceDomainFilePath");
      String piazzaUserName = prop.getProperty("piazza_username");
      String piazzaPassword = prop.getProperty("piazza_password");

      IndexingOperations indexingOperations = new IndexingOperations(pythonScriptPaths, username, password);
      PiazzaApi piazzaApi = new PiazzaApi(username, password);
      Index.initialize("PiazzaFeedIndex/");
      String course_id = "ie348yjty84c7";

      indexingOperations.addPiazzaPosts(piazzaApi.getFeed(course_id), piazzaApi, course_id);

//      piazzaApi.getFeed("ic53erv8juk488");
//      piazzaApi.getSyllabus("ie348yjty84c7");
//      PiazzaApi piazzaApi = new PiazzaApi(username, password);
//      piazzaApi.askQuestion("ie348yjty84c7","Amos testing", "Does this still work?", "other");
//      emailOperations.convertPdfToText("Sample.pdf");

      //      EmailOperations emailOperations = new EmailOperations(username, password, username);
//      Date lastFetchDate = new Date();
//      //n * 24 * 3600 * 1000 l (n is the number of days to add or subtract)
//      lastFetchDate = new Date(lastFetchDate.getTime() - 4 * 24 * 3600 * 1000l );
//      emailOperations.extractLastEmails(lastFetchDate, filePath);
//      IndexingOperations indexingOperations = new IndexingOperations("TestingFiles/");
//      indexingOperations.addEmailDocuments(emailOperations.preprocessExtractedEmails(filePath, sourceDomainFilePath, pythonScriptPaths));

  }

}
