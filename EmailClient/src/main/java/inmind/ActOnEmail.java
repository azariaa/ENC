package main.java.inmind;

import main.java.inmind.Calendar.EventDetector;
import main.java.inmind.email.EmailInstance;
import main.java.inmind.email.EmailOperations;
import main.java.inmind.qa.piazza.PiazzaQA;

import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by Amos Azaria on 26-Nov-15.
 */
public class ActOnEmail
{
    private static Logger LOG;
    private static FileHandler FH;
    private static String username;
    private static String password;
    private static String pythonScriptPath;
    private static String course_id;
    private static String indexLocation;
    public static void main(String[] args)
    {
        LOG = Logger.getLogger(main.java.inmind.qa.indexingComponent.IndexingOperations.class.getName());
        try {
            Date date = new Date() ;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss") ;
            FH = new FileHandler("log/log-" + dateFormat.format(date) + ".log");
            LOG.addHandler(FH);
            SimpleFormatter formatter = new SimpleFormatter();
            FH.setFormatter(formatter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Properties prop = new Properties();
        try
        {
            prop.load(new FileReader("default.properties"));
        } catch (IOException e)
        {
            e.printStackTrace();
            return;
        }
        username = prop.getProperty("username");
        password = prop.getProperty("password");
        pythonScriptPath = prop.getProperty("pythonScriptsPath");
        course_id = prop.getProperty("course_id");
        indexLocation = prop.getProperty("index_location");
        while (true)
        {
            checkEmailsAndAct();
            try
            {
                Thread.sleep(20*1000);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void checkEmailsAndAct()
    {

        EmailOperations emailOperations = new EmailOperations(username, password, username);
        //String tempEmailFileName = prop.getProperty("temp_email_file");
        Date d = PiazzaQA.loadLastFetchDate(pythonScriptPath);
        Date fetchDate = new Date();
        List<EmailInstance> recentEmails = emailOperations.extractLastEmails(d);

        boolean invokedPiazzaQA = false;
        for (EmailInstance email : recentEmails)
        {
            LOG.info("new email: " + email);
            //System.out.println(email);
            if (email.senderList.contains("no-reply@piazza.com"))
            {
                if (!invokedPiazzaQA)
                {
                    LOG.info("Found Piazza Email!");
                    PiazzaQA.invoke(username, password, pythonScriptPath, indexLocation, course_id, LOG);
                    invokedPiazzaQA = true;
                }
            }
            else
            {
                EventDetector.EventInfo eventInfo = EventDetector.emailEventInfo(email);
                LOG.info("email info: type:" + eventInfo.eventType);
                if (eventInfo.eventType == EventDetector.EventType.meetingRequest)
                {
                    LOG.info("email info: what:" + eventInfo.what);
                    LOG.info("email info: who:" + eventInfo.who);
                    LOG.info("email info: when:" + eventInfo.when.toString());
                }
            }
        }

        PiazzaQA.updateLastFetchDate(fetchDate, pythonScriptPath);

    }
}
