package EmailClient.src.main.java.inmind.email;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.plaf.synth.SynthTextAreaUI;
import java.io.*;
import java.util.Date;
import java.util.Properties;

/**
 * Created by Amos Azaria on 15-Jul-15.
 * @modifiedBy suruchis (Suruchi Shah) on 13-Sept-15
 */
public class EmailOperations
{
    String username;
    String password;
    String emailAddr;

    public EmailOperations(String username, String password, String emailAddr)
    {
        this.username = username;
        this.password = password;
        this.emailAddr = emailAddr;
    }

    public void printLastEmails(int emailsToFetch)
    {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        try {
            Session session = Session.getInstance(props, null);
            //session.setDebug(true);
            Store store = session.getStore();
            store.connect("imap.gmail.com", username, password);
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            for (int idx = inbox.getMessageCount() - emailsToFetch; idx < inbox.getMessageCount(); idx++)
            {
                System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                System.out.println("EMAIL INDEX:" + idx);
                Message msg = inbox.getMessage(idx);
                Address[] in = msg.getFrom();
                for (Address address : in)
                {
                    System.out.println("FROM:" + address.toString());
                }
                String bodyStr = "Error!";
                Object msgContent = msg.getContent();
                if (msgContent instanceof String)
                    bodyStr = (String)msgContent;
                else if (msgContent instanceof Multipart)
                {
                    BodyPart bp = ((Multipart)msgContent).getBodyPart(0);
                    bodyStr = bp.getContent().toString();
                }
                System.out.println("SENT DATE:" + msg.getSentDate());
                System.out.println("SUBJECT:" + msg.getSubject());
                System.out.println("CONTENT:" + bodyStr);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method extracts the emails received after the last fetch date
     * @param lastFetchDate The last time the emails were extracted
     * @param fileName This is the path of the file where the output will be stored
     * @author suruchi.shah
     */
    public void extractLastEmails(Date lastFetchDate, String fileName)    {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        BufferedWriter writer = null;
        StringBuilder sb = new StringBuilder();
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(fileName)), "utf-8"));
            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            store.connect("imap.gmail.com", username, password);
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            for(int counter = inbox.getMessageCount(); counter > 0; counter--)  {
                Message msg = inbox.getMessage(counter);
                if(msg.getSentDate().after(lastFetchDate))  {
                    sb.append("<EMAIL>\n");
                    sb.append("EMAILID:" + counter + "\n");
                    Address[] in = msg.getFrom();
                    sb.append("FROM:");
                    for (Address address : in) {
                        sb.append(address.toString() + ",");
                    }
                    sb.append("\n");
                    in = msg.getAllRecipients();
                    sb.append("RECIPIENT:");
                    for(Address address: in)    {
                        sb.append(address.toString()+",");
                    }
                    sb.append("\n");
                    String bodyStr = "Error!";
                    Object msgContent = msg.getContent();
                    if (msgContent instanceof String)
                        bodyStr = (String)msgContent;
                    else if (msgContent instanceof Multipart)
                    {
                        BodyPart bp = ((Multipart)msgContent).getBodyPart(0);
                        bodyStr = bp.getContent().toString();
                    }
                    sb.append("RECEIVEDATE:" + msg.getReceivedDate() + "\n");
                    sb.append("SENTDATE:" + msg.getSentDate() + "\n");
                    sb.append("SUBJECT:" + msg.getSubject() + "\n");
                    sb.append("CONTENT:" + bodyStr + "\n");
                    sb.append("</EMAIL>\n");
                }
                else    {
                    break;
                }
            }
            writer.write(sb.toString());
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendEmail(String subject, String body, String recipient)
    {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailAddr));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param inputFileName file containing the content of the emails (emailsContent.txt)
     * @param sourceFile domain source file (sourceDomains.txt)
     * @param pythonScriptsPath path to the python script (PythonScripts/)
     * @author suruchis (Suruchi Shah)
     */
    public JSONArray preprocessExtractedEmails(String inputFileName, String sourceFile, String pythonScriptsPath) {

        String s = null;
        JSONArray allEmail = null;
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"python", pythonScriptsPath + "/extractEmailContent.py", inputFileName, sourceFile});

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            // read any errors from the attempted command
            boolean errorExists = false;
            while ((s = stdError.readLine()) != null) {
                errorExists = true;
            }
            if(errorExists) {
                System.out.println("Error in the system: " + s);
                throw new Exception("Error in pre processing emails. Source: python script");
            }

            // read the output from the command
            StringBuilder outputJsonString = new StringBuilder();
            while ((s = stdInput.readLine()) != null) {
                outputJsonString.append(s);
            }
            JSONObject jsonObj = new JSONObject(outputJsonString.toString());
            allEmail = jsonObj.getJSONArray("AllEmails");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return  allEmail;
    }

    /**
     * This method takes a PDF file as input and extracts the text from it using Apache PDF Box
     * @param fileName Input PDF File
     * @author suruchis (Suruchi Shah)
     */
    public void convertPdfToText(String fileName)  {
        File inputFile = new File(fileName);
        try {
            PDDocument pd = PDDocument.load(inputFile);
            System.out.println("Number of Pages:" + pd.getNumberOfPages());
            System.out.println("Is Encrypted:" + pd.isEncrypted());
            PDFTextStripper stripper = new PDFTextStripper();
            System.out.println("CONTENT:" + stripper.getText(pd));
            if (pd != null) {
                pd.close();
            }
        } catch (Exception e)   {
            e.printStackTrace();
        }

    }
}
