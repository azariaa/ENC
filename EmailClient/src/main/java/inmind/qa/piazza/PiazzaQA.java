package main.java.inmind.qa.piazza;

import main.java.inmind.qa.indexingComponent.Index;
import main.java.inmind.qa.indexingComponent.IndexingOperations;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * @author suruchis (Suruchi Shah)
 * updated by amos
 */
public class PiazzaQA
{

    public static void invoke(String username, String password, String pythonScriptPaths, String indexLocation, String course_id, Logger LOG)
    {
        try
        {
            PiazzaApi piazzaApi = new PiazzaApi(username, password);
            IndexingOperations indexingOperations = new IndexingOperations(pythonScriptPaths, piazzaApi, LOG);
            Index.initialize(indexLocation);
            boolean answerQuestions = true;
            indexingOperations.addPiazzaPosts(piazzaApi.getFeed(course_id), piazzaApi, course_id, answerQuestions);
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

//    public static void old()
//    {
//        LOG = Logger.getLogger(IndexingOperations.class.getName());
//        try {
//            Date date = new Date() ;
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss") ;
//            FH = new FileHandler("log/log-" + dateFormat.format(date) + ".log");
//            LOG.addHandler(FH);
//            SimpleFormatter formatter = new SimpleFormatter();
//            FH.setFormatter(formatter);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        Properties prop = new Properties();
//        prop.load(new FileReader("default.properties"));
//        String username = prop.getProperty("username");
//        String password = prop.getProperty("password");
//        String pythonScriptPaths = prop.getProperty("pythonScriptsPath");
//        String course_id = prop.getProperty("course_id");
//
//        PiazzaApi piazzaApi = new PiazzaApi(username, password);
//        IndexingOperations indexingOperations = new IndexingOperations(pythonScriptPaths, piazzaApi, LOG);
//        Index.initialize(prop.getProperty("index_location"));
//        boolean answerQuestions = Boolean.parseBoolean(prop.getProperty("answer_questions"));
//        boolean useEmails = Boolean.parseBoolean(prop.getProperty("use_email"));
//
//        if(useEmails) { // Indexing via Email
//            String tempEmailFileName = prop.getProperty("temp_email_file");
//            EmailOperations emailOperations = new EmailOperations(username, password, username);
//            Date d = loadLastFetchDate(pythonScriptPaths);
//            emailOperations.extractLastEmails(d,tempEmailFileName);
//            String sourceFile = pythonScriptPaths + "gazeteer/sourceDomains.txt";
//            indexingOperations.addPiazzaPostsFromEmail(emailOperations.preprocessExtractedEmails(tempEmailFileName, sourceFile, pythonScriptPaths), piazzaApi, answerQuestions);
//            System.out.println("Done indexing emails");
//            updateLastFetchDate(pythonScriptPaths);
//        } else  { // Indexing via Piazza Feed
//            indexingOperations.addPiazzaPosts(piazzaApi.getFeed(course_id), piazzaApi, course_id, answerQuestions);
//        }
//    }

    public static Date loadLastFetchDate(String pythonScriptPaths) {
        String filePath = pythonScriptPaths + "/tempContent/lastFetchDate.txt";
        SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy");
        Date d = null;
        try
        {
            File fin = new File(filePath);
            FileInputStream fis = new FileInputStream(fin);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = br.readLine();
            try
            {
                d = formatter.parse(line);
            }
            catch (Exception ex)
            {
                d = new Date(System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000));
            }
            br.close();
        } catch (Exception e)   {
            e.printStackTrace();
        }
        return d;
    }

    public static void updateLastFetchDate(Date fetchDate, String pythonScriptPaths)    {
        String filePath = pythonScriptPaths + "/tempContent/lastFetchDate.txt";
        SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy");
        try {
            File fin = new File(filePath);
            FileOutputStream fis = new FileOutputStream(fin);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fis));
            bw.write(formatter.format(fetchDate));
            bw.close();
        } catch (Exception e)   {
            e.printStackTrace();
        }
    }

}
