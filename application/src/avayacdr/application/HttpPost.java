package avayacdr.application;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpPost {

    public String getResponse() {
        return response;
    }

    private String response;
    private String postUrl;
    Map<String,Object> params;

    public HttpPost(String postUrl ) {

        this.response = null;
        this.postUrl = postUrl;
        this.params = new LinkedHashMap<>();
    }

    public void addParameters(String key, String value){
        params.put(key,value);
    }
    public void clear(){
        params.clear();
    }

    private String getParameters(){

        StringBuilder postData = new StringBuilder();
        try {

            for (Map.Entry<String,Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return postData.toString();
    }

        public String doPost()  {
            return doPost(getParameters());
        }

        public String doPost(String query)  {

        try {

            URL url = new URL(postUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String postData = "e=" + encodedQuery;

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Content-Length",  String.valueOf(postData.length()));
            con.setDoOutput(true);

            this.sendData(con, postData);

            response = read(con.getInputStream());
            System.out.println(response);
        } catch(IOException e) {
            e.printStackTrace();
        }


        return response;
    }

    protected void sendData(HttpURLConnection con, String data) throws IOException {
        DataOutputStream wr = null;
        try {
            wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(data);
            wr.flush();
            wr.close();
        } catch(IOException exception) {
            throw exception;
        } finally {
            this.closeQuietly(wr);
        }
    }

    private String read(InputStream is) throws IOException {
        BufferedReader in = null;
        String inputLine;
        StringBuilder body;
        try {
            in = new BufferedReader(new InputStreamReader(is));

            body = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                body.append(inputLine);
            }
            in.close();

            return body.toString();
        } catch(IOException ioe) {
            throw ioe;
        } finally {
            this.closeQuietly(in);
        }
    }

    protected void closeQuietly(Closeable closeable) {
        try {
            if( closeable != null ) {
                closeable.close();
            }
        } catch(IOException ex) {

        }
    }
}
