package EmailClient.src.main.java.inmind.testing;


import java.io.FileReader;
import java.util.Properties;
import java.util.Date;
import EmailClient.src.main.java.inmind.email.*;
import EmailClient.src.main.java.inmind.qa.IndexDocument;
import EmailClient.src.main.java.inmind.qa.IndexingOperations;
import EmailClient.src.main.java.inmind.qa.LuceneEngine;
import com.google.api.client.util.DateTime;

import javax.jdo.annotations.Index;

/**
 * Created by ziy on 8/5/15.
 */
public class Main {

  public static void main(String[] args) throws Exception {
    Properties prop = new Properties();
    prop.load(new FileReader("default.properties"));
    String username = prop.getProperty("username");
    String password = prop.getProperty("password");
    String filePath = prop.getProperty("emailOutputFilePath");
    String pythonScriptPaths = prop.getProperty("pythonScriptsPath");
    String sourceDomainFilePath = prop.getProperty("sourceDomainFilePath");


    EmailOperations emailOperations = new EmailOperations(username, password, username);
    Date lastFetchDate = new Date();
    //n * 24 * 3600 * 1000 l (n is the number of days to add or subtract)
    lastFetchDate = new Date(lastFetchDate.getTime() - 4 * 24 * 3600 * 1000l );
    emailOperations.extractLastEmails(lastFetchDate, filePath);
    IndexingOperations indexingOperations = new IndexingOperations("TestingFiles/");
    indexingOperations.addEmailDocuments(emailOperations.preprocessExtractedEmails(filePath, sourceDomainFilePath, pythonScriptPaths));


//    emailOperations.convertPdfToText("Sample.pdf");

//      PiazzaApi piazzaApi = new PiazzaApi(username, password);
//      //piazzaApi.askQuestion("ie348yjty84c7","Amos testing", "Does this still work?", "other");
//      //piazzaApi.getFeed("ie348yjty84c7");
//      piazzaApi.getSyllabus("ie348yjty84c7");

  }

}
