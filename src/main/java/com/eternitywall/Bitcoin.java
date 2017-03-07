package com.eternitywall;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by casatta on 06/03/17.
 */
public class Bitcoin {

    private String authString;
    private String urlString;

    private Bitcoin() {
    }

    public Bitcoin(Properties bitcoinConf) {
        authString = String.valueOf(Base64Coder.encode( String.format("%s:%s",bitcoinConf.getProperty("rpcuser"), bitcoinConf.getProperty("rpcpassword")).getBytes()));
        urlString = String.format("http://%s:%s", bitcoinConf.getProperty("rpcconnect"), bitcoinConf.getProperty("rpcport"));
    }

    public static Properties readBitcoinConf() {
        String home = System.getProperty("user.home");

        List<String> list= Arrays.asList("/.bitcoin/bitcoin.conf", "\\AppData\\Roaming\\Bitcoin\\bitcoin.conf", "/Library/Application Support/Bitcoin/bitcoin.conf");
        for(String dir : list) {
            Properties prop = new Properties();
            InputStream input = null;

            try {

                input = new FileInputStream(home+dir);

                // load a properties file
                prop.load(input);

                // get the property value and print it out
                if(prop.getProperty("rpcuser")!=null && prop.getProperty("rpcpassword")!=null) {
                    if(prop.getProperty("rpcconnect")==null)
                        prop.setProperty("rpcconnect","127.0.0.1");
                    if(prop.getProperty("rpcport")==null)
                        prop.setProperty("rpcport","8332");
                    return prop;
                }

            } catch (IOException ex) {
                //ex.printStackTrace();
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public String getInfo() {
        JSONObject json = new JSONObject();
        json.put("id", "curltest");
        json.put("method", "getinfo");
        return callRPC(json);
    }

    private String callRPC(JSONObject query) {
        try {
            String s=query.toString();
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Basic " + authString);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Length", ""
                    + Integer.toString(s.length()));
            urlConnection.setUseCaches(false);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            // Send request
            DataOutputStream wr = new DataOutputStream(urlConnection
                    .getOutputStream());
            wr.writeBytes(s);
            wr.flush();
            wr.close();

            InputStream is = urlConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);

            int numCharsRead;
            char[] charArray = new char[1024];
            StringBuffer sb = new StringBuffer();
            while ((numCharsRead = isr.read(charArray)) > 0) {
                sb.append(charArray, 0, numCharsRead);
            }
            String result = sb.toString();
            return result;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
