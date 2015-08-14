package inmind.email;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Created by Amos Azaria on 15-Jul-15.
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
}
