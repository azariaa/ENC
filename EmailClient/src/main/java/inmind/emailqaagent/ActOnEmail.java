package main.java.inmind.emailqaagent;

import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by Amos Azaria on 26-Nov-15.
 */
public class ActOnEmail
{

    static final int sleepTimeInSec = 10;

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
            prop.load(new FileReader("resources/default.properties"));
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
        EmailActor emailActor = new EmailActor(username, password, LOG, pythonScriptPath, indexLocation, course_id);
        while (true)
        {
            emailActor.checkEmailsAndAct();
            try
            {
                Thread.sleep(sleepTimeInSec*1000);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }


}
