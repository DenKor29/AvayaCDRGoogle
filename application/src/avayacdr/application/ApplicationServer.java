package avayacdr.application;

import avayacdr.network.TCPConnection;
import avayacdr.network.TCPConnectionListener;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.ArrayList;

public class ApplicationServer implements TCPConnectionListener {

    private ServerSocket serverSocket;
    private ArrayList <TCPConnection> connections = new ArrayList<>();

    private Thread rxThread;
    private ApplicationServerListener eventListener;
    private String token;


    protected String NameServer;

    public String getNameServer() {
        return NameServer;
    }

    public ApplicationServer(ApplicationServerListener eventListener)  {

        this(eventListener,"Application Server");


    }

    public ApplicationServer(ApplicationServerListener eventListener, String nameServer)  {

        this.eventListener = eventListener;
        this.NameServer = nameServer;
        this.token = "";
    }

    public  void start(int port,int timeoutAcept){

        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                connected(port,timeoutAcept);
                disconnected();
            }
        });
        rxThread.start();

    }
    private void GooglePost(String value){
        try {
            URL url = new URL("https://script.google.com/macros/s/AKfycbxtBR7lt25OQWFN0q_-l3nBnQlLFPuQ0sLD3E8p1eQ/dev");
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "text/plain");
            OutputStream os = con.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            osw.write(value);
            osw.flush();
            osw.close();
            os.close();  //don't forget to close the OutputStream
            con.connect();
        } catch (IOException e) {
            System.out.println("Exeption: "+e );
        }
    }

    private void connected(int port,int timeoutAcept) {

        System.out.println("Start "+ NameServer + " - listening port " + port);

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(timeoutAcept);
            eventListener.onConnectionServer(ApplicationServer.this);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        };

        while (!rxThread.isInterrupted()) {
            try {
                  new TCPConnection(this,serverSocket.accept());
            } catch (IOException e)
            {
                if(!(e instanceof SocketTimeoutException))
                    System.out.println("TCPConnection exeption:" + e);
            };
        };

    }

    public void interrupt(){
        System.out.println(NameServer + " interrupt ..." );
        rxThread.interrupt();
    }

    private void disconnected() {

        System.out.println(NameServer + " disconnecting ..." );



        //Отключаем клиентов
        int cnt = connections.size();
        for (int i = 0; i < cnt; i++) {
            connections.get(i).disconnected();
        };


        //Удаляем список клиентов
        connections.clear();


        //Закрывем серверный сокет
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("TCPServer exeption:"  + e );
        };
        System.out.println(NameServer + " closed." );
        eventListener.onDisconnection(ApplicationServer.this);
    }

    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
    connections.add(tcpConnection);
    eventListener.onConnectionReady(this,tcpConnection);
    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {

        eventListener.onMessageString(ApplicationServer.this,tcpConnection,value);
        GooglePost(value);
    }

    @Override
    public synchronized void onDisconnection(TCPConnection tcpConnection) {
    if (connections.contains(tcpConnection)) connections.remove(tcpConnection);
    eventListener.onDisconnectionReady(this,tcpConnection);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exeption:"  + e );
    }

    public synchronized void sendString(TCPConnection tcpConnection,String value){

            tcpConnection.sendString(value);
    }
}
