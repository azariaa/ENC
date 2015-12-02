package main.java.inmind.email;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Amos Azaria on 29-Nov-15.
 */
public class EmailInstance
{
    public List<String> senderList;
    public List<String> recipientList;
    public Date receiveDate;
    public Date sentDate;
    public String subject;
    public String content;


    public EmailInstance()
    {
        senderList = new LinkedList<>();
        recipientList = new LinkedList<>();
    }

    @Override
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("FROM: ");
        stringBuilder.append(String.join(";", senderList));
        stringBuilder.append("\n");
        stringBuilder.append("RECIPIENT: ");
        stringBuilder.append(String.join(";", recipientList) + "\n");

        stringBuilder.append("RECEIVEDATE:" + receiveDate + "\n");
        stringBuilder.append("SENTDATE:" + sentDate + "\n");
        stringBuilder.append("SUBJECT:" + subject + "\n");
        stringBuilder.append("CONTENT:" + content + "\n");

        return stringBuilder.toString();

    }

    public String getAllSenders()
    {
        return String.join(";", senderList);
    }

    public String getAllRecipients()
    {
        return String.join(";", recipientList);
    }
}
