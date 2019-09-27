package avayacdr.server;

import avayacdr.application.ApplicationServer;
import avayacdr.application.ApplicationServerListener;
import avayacdr.core.BaseCDRData;
import avayacdr.core.AvayaCDRData;
import avayacdr.core.ConfigurationSettings;
import avayacdr.network.TCPConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;


public class MainWindow extends JFrame  implements ApplicationServerListener {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainWindow();
            }
        });
    }
    private JButton buttonstartServer ;
    private JButton buttonstopServer;
    private JTextArea log;

    private ApplicationServer app;

    private ArrayList<BaseCDRData> connectionsCDR = new ArrayList<>();


    private boolean Running = false;

    private int port;
    private int  timeoutacept;
    private int appserverstart;
    private static final int WIDTH = 350;
    private static final int HEIGHT = 95;

    private MainWindow(){


        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH,HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        JPanel button_panel = new JPanel();
        button_panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Сервер"));
        button_panel.setLayout(new BorderLayout());

        buttonstartServer = new JButton("Старт");
        button_panel.add(buttonstartServer, BorderLayout.NORTH);

        buttonstopServer = new JButton("Стоп");
        button_panel.add(buttonstopServer,BorderLayout.CENTER);


        add(button_panel,BorderLayout.WEST);

        JPanel text_panel = new JPanel();
        text_panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Мониторинг"));
        text_panel.setLayout(new BorderLayout());

        log = new JTextArea();
        log.setEnabled(false);
        log.setLineWrap(true);
        JScrollPane scroll= new JScrollPane(log, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setSize(250, 150);
        scroll.setLocation(10,10);
        text_panel.add(scroll,BorderLayout.CENTER);

        add(text_panel,BorderLayout.CENTER);

        enableButtonServer(true);


        setVisible(true);
        pack();
        setIconImage(getImage("icon"));
        setLocationRelativeTo(null);

        ConfigurationSettings configurationSettings = new ConfigurationSettings("application.xml", "avaya");



         port = configurationSettings.getInt("appport",9100);
         timeoutacept = configurationSettings.getInt("apptimeoutacept",30000);
         appserverstart = configurationSettings.getInt("appserverstart",1);

        app = new ApplicationServer(this);


        buttonstartServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               if (!Running) {
                   MainWindow.this.enableButtonServer(false);
                   app.start(port, timeoutacept);
                   Running = true;


               }
            }
        });

        buttonstopServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               if (Running) {
                   Running = false;
                   printMessage(app.getNameServer()+" Interrupt ...");
                   app.interrupt();
               }
            }
        });

            if (appserverstart == 1)  buttonstartServer.doClick();


    }
    private void enableButtonServer(boolean status){

        buttonstartServer.setEnabled(status);
        buttonstopServer.setEnabled(!status);

    }

    @Override
    public void onConnectionServer(ApplicationServer applicationServer) {
        String message = applicationServer.getNameServer() + " Start.";

        System.out.println(message );
        printMessage(message);

    }

    @Override
    public void onConnectionReady(ApplicationServer applicationServer, TCPConnection tcpConnection) {

    }

    @Override
    public void onDisconnectionReady(ApplicationServer applicationServer, TCPConnection tcpConnection) {

    }

    @Override
    public void onMessageString(ApplicationServer applicationServer, TCPConnection tcpConnection,String value) {



    }



    @Override
    public void onDisconnection(ApplicationServer applicationServer) {
        String message = applicationServer.getNameServer() + " Stop.";

        System.out.println(message );
        printMessage(message);

        Running = false;
        enableButtonServer(true);
    }

    @Override
    public void onException(ApplicationServer applicationServer, Exception e) {
        System.out.println(app.getNameServer() + " Exeption:" + e);
    }
    private synchronized  void printMessage(String value){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(value+"\r\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }
    private Image getImage (String name){
        String filename = "img/" + name + ".png";
        ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(filename)));
        return icon.getImage();

    }



}
