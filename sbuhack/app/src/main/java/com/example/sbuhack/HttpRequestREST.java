package com.example.sbuhack;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class HttpRequestREST {
    final static String positionURL = "https://fabled-progress-253502.appspot.com/position/";

    public Position getPosition(int idx){
        String urlString = positionURL + idx;
        Position pos = new Position();

        try {
            // call API by using HTTPURLConnection
            URL url = new URL(urlString);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            //urlConnection.setRequestMethod("GET");

            if (urlConnection.getResponseCode() != HttpsURLConnection.HTTP_OK)
                System.out.println("@@@@@@@@@");
//            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
//            urlConnection.setReadTimeout(DATARETRIEVAL_TIMEOUT);

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            JSONObject json = new JSONObject(getStringFromInputStream(in));

            pos.setLatitude(json.getDouble("latitude"));
            pos.setLongitude(json.getDouble("longitude"));
            pos.setName(json.getString("name"));
        }catch(MalformedURLException e){
            System.err.println("Malformed URL");
            e.printStackTrace();
            return null;
        }catch(JSONException e) {
            System.err.println("JSON parsing error");
            e.printStackTrace();
            return null;
        }catch(IOException e){
            System.err.println("URL Connection failed");
            e.printStackTrace();
            return null;
        }

        return pos;
    }

    private static String getStringFromInputStream(InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

}