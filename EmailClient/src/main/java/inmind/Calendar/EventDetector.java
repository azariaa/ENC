package main.java.inmind.Calendar;

import main.java.inmind.email.EmailInstance;
import main.java.inmind.qa.utils.Connection;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Amos Azaria on 29-Nov-15.
 */
public class EventDetector
{
    public enum EventType {none, meetingRequest, eventAccept, error}

    static public class EventInfo
    {
        public Date when;
        public String what;
        public String who;
        public EventType eventType;

        public EventInfo()
        {
        }
    }

    public static EventInfo emailEventInfo(EmailInstance email)
    {
        EventInfo eventInfo = new EventInfo();
        eventInfo.eventType = EventType.error;
        try
        {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("Body", email.content); //"Do you want to meet at 2:00 to discuss about the birthday party for Bob?");
            parameters.put("Sender", email.getAllSenders());//"Alan Black");
            parameters.put("Subject", email.subject);//"Birthday party");
            String response = Connection.callServer("http://birch.speech.cs.cmu.edu:5000/distract", parameters, true);
            JSONObject emailAsJson = new JSONObject(response);
            eventInfo.eventType = EventType.none;
            if (emailAsJson.get("Type").toString().equals("1"))
            {
                eventInfo.eventType = EventType.meetingRequest;
                eventInfo.what = emailAsJson.get("what").toString();
                eventInfo.who = emailAsJson.get("who").toString();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                try
                {
                    eventInfo.when = dateFormat.parse(emailAsJson.get("When").toString());
                } catch (ParseException e)
                {
                    e.printStackTrace();
                }
            }
            else if (emailAsJson.get("Type").toString().equals("2"))
            {
                eventInfo.eventType = EventType.eventAccept;
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return eventInfo;
    }
}
