package com.ustc.location.network;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
public class JSONParser {

    static InputStream is = null;
    static JSONObject jObj = null;
    static JSONArray jarray = null;
    static String json = "";

    // constructor
    public JSONParser() {
    }

    //从url处得到JSONArray数据
    public static JSONArray getJSONArrayFromUrl(String url) {

        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);

        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();

            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                Log.e("==>", "Failed to download file");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(builder.toString());
        // Parse String to JSON object
        try {
            jarray = new JSONArray( builder.toString());
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        return jarray;
    }


    public static boolean makeHttpRequest(String Url, String method, List<NameValuePair> params) {
        // making HTTP Request
        try {
            // check for request method
            if(method == "POST"){
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(Url);
                httpPost.setEntity(new UrlEncodedFormEntity(params));
                HttpResponse httpResponse = httpClient.execute(httpPost);

                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
            }else
                // check for request method
                if(method == "GET"){
                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    String paramString = URLEncodedUtils.format(params, "utf-8");
                    Url += "?" + paramString;
                    HttpGet httpGet = new HttpGet(Url);
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    HttpEntity httpEntity = httpResponse.getEntity();
                    is = httpEntity.getContent();
                }

        } catch (Exception e) {
            Log.d("Network Error",e.toString());
            return false;
        }

        try {
            BufferedReader  reader = new BufferedReader(
                    new InputStreamReader(is, "utf-8"),8);
            StringBuilder sb = new StringBuilder();
            String  line = null;

            while((line = reader.readLine()) != null){
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        } catch (IOException e) {
            Log.d("Buffer Error","Error Converting Reesult "+e.toString());
            return false;
        }
        // try parse the string to JSON Object
        int flag;
        try {
            jObj = new JSONObject(json);
            flag=(int)jObj.get("success");
        } catch (JSONException e) {
            Log.d("JSON Parser", "Error Parsing data" + e.toString());
            return false;
        }
        return flag==1;
    }
}