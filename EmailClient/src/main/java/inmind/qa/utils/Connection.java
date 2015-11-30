package main.java.inmind.qa.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Amos Azaria on 29-Nov-15.
 */
public class Connection
{

        static private final String USER_AGENT = "Mozilla/5.0";

        public static String callServer(String url, Map<String, String> parameters, boolean isJsonFormat)
        {
            HttpClient httpClient = HttpClientBuilder.create().build();

            try
            {
                HttpPost request = new HttpPost(url);
                if (isJsonFormat)
                    request.addHeader("Content-Type", "application/json");//request.addHeader("Content-Type","text/plain");//"content-type", "application/x-www-form-urlencoded");
                else
                    request.addHeader("Content-Type", "application/x-www-form-urlencoded");


                //HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                //using post
                //con.setRequestMethod("POST");
                //con.setRequestProperty("User-Agent", USER_AGENT);
                //con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                //if (isJsonFormat)
                //con.setRequestProperty("Content-Type", "application/json");

                boolean firstParam = true;
                StringBuilder parms = new StringBuilder();
                JSONObject jsonObject = null;
                if (isJsonFormat)
                {
                    jsonObject = new JSONObject();
                    //parms.append(" {");
                }
                for (String parm : parameters.keySet())
                {
                    if (!firstParam) //if not first
                    {
//                    if (isJsonFormat)
//                        parms.append(",");
//                    else
                        if (!isJsonFormat)
                            parms.append("&");
                    }
                    if (isJsonFormat)
                    {
                        jsonObject.accumulate(parm, parameters.get(parm));
                        //if valid json, don't add the quotes
                        //parms.append("\"" + parm + "\":\"" + parameters.get(parm) + "\""); //building a json, should actually do it right...
                    }
                    else
                    {
                        parms.append(parm + "=" + parameters.get(parm));
                    }
                    firstParam = false;
                }
                if (isJsonFormat)
                    parms.append(" ").append(jsonObject.toString());//for some odd reason the first byte doesn't arrive, so I add extra " " //parms.append("}");

                // Send post request
                //DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                //wr.writeBytes(parms.toString());
                //wr.flush();
                //wr.close();

                request.setEntity(new StringEntity(parms.toString()));//, ContentType.APPLICATION_JSON));
                HttpResponse httpResponse = httpClient.execute(request);

                //int responseCode = con.getResponseCode();
                if (httpResponse.getStatusLine().getStatusCode() != 200)
                {
                    System.out.println("S: error. (response code is: " + httpResponse.getStatusLine().getStatusCode() + ")");
                }

                String response = new BasicResponseHandler().handleResponse(httpResponse);//httpResponse.getEntity().toString();
                return response;
//                    BufferedReader in = new BufferedReader(
//                new InputStreamReader(con.getInputStream()));
//            String inputLine;
//            StringBuilder response = new StringBuilder();
//
//
//            while ((inputLine = in.readLine()) != null)
//            {
//                response.append(inputLine + "\n");
//            }
//            in.close();

                // handle response here...
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }

            //return response.toString();
            return "";
        }
    }

