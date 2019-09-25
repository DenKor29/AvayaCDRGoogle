package avayacdr.application;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpPost {

    public String getResponse() {
        return response;
    }

    private String response;

    private String postUrl;
    Map<String,Object> params;
    Map<String,Object> keys;

    public HttpPost(String postUrl ) {

        this.response = null;
        this.postUrl = postUrl;
        this.params = new LinkedHashMap<>();
    }

    public void addParameters(String key, String value){
        params.put(key,value);
    }
    public void clear(){
        if (params != null) params.clear();
    }

    private String getParameters(){

        StringBuilder postData = new StringBuilder();
            int i = 0;
            for (Map.Entry<String,Object> param : params.entrySet()) {
            if (i != 0) postData.append("&");
                postData.append(param.getKey());
                postData.append('=');
                try {
                    postData.append(URLEncoder.encode(param.getValue().toString(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                i++;
            }

        return postData.toString();
    }

    private String getParametersJson(){

        StringBuilder postData = new StringBuilder();
        int i = 0;
            postData.append('{');
            for (Map.Entry<String,Object> param : params.entrySet()) {
                if (i != 0) postData.append(',');
                postData.append('"');
                postData.append(param.getKey());
                postData.append('"');
                postData.append(":");
                postData.append('"');
                postData.append(param.getValue());
                postData.append('"');
                i++;
            }
            postData.append('}');

        return postData.toString();
    }

    public String doPost()  {
        return doPost(getParameters());
    }
    public String doPostJSON()  {

        String parametersJson=getParametersJson();
        clear();

        byte[] bytes = new byte[0];
        try {
            bytes = parametersJson.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String query = Base64.getEncoder().encodeToString(bytes);


        addParameters("name","tarif");
        addParameters("zip","0");
        addParameters("data",query);

        String request = getParameters();


        System.out.println("Json "+ parametersJson );
        System.out.println("Request: "+ request );

        return doPost(request);
    }

        public String doPost(String query)  {

        try {


            URL url = new URL(postUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            String postData = query;

            System.out.println("Query: "+ postData );

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Content-Length",  String.valueOf(postData.length()));
            con.setDoOutput(true);

            this.sendData(con, postData);

            response = read(con.getInputStream());

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
