package gy2023;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

public class Server extends JFrame implements Runnable, Serializable {

    private JTextArea ta;
    private int clientNo = 0;

    public Server() {
        ta = new JTextArea(10,10);
        ta.setEditable(false);
        JScrollPane jScrollPane = new JScrollPane(ta);
        this.add(jScrollPane);
        this.setTitle("Game-Server");
        this.setSize(400,200);
        Thread thread = new Thread(this);
        thread.start();
    }

    class HandleSomeClient implements Runnable{
        private PreparedStatement selectStatementForMaxId, selectStatementForOpen, selectStatementForRank;
        private Socket socket;
        private Connection conn;
        private PreparedStatement insertStatementForMines, insertStatementForRank;
        private ResultSet resultSet;
        public HandleSomeClient(Socket socket, int clientNo){
            this.socket = socket;
        }

        @Override
        public void run() {
            try{
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection("jdbc:sqlite:gy2023Final.db");

                while (true){
                    ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
                    ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
                    insertStatementForMines = conn.prepareStatement("INSERT INTO game VALUES (NULL, ?);");
                    insertStatementForRank = conn.prepareStatement("INSERT INTO top5 VALUES(NULL, ?);");
                    selectStatementForMaxId = conn.prepareStatement("SELECT MAX(id) AS id FROM game");
                    selectStatementForOpen = conn.prepareStatement("SELECT mode FROM game WHERE id = ?;");
                    selectStatementForRank = conn.prepareStatement("SELECT time FROM top5;");
                    ArrayList<Object> temporary = (ArrayList<Object>)fromClient.readObject();
                    if (temporary.size() == 8){
                        Object o = (Object)temporary;
                        insertStatementForMines.setBytes(1, Client.takeByte(o));
                        insertStatementForMines.executeUpdate();
                        ta.append("Database has been updated\n");
                        resultSet = selectStatementForMaxId.executeQuery();
                        while (resultSet.next()){
                            Integer id = resultSet.getInt(1);
                            toClient.writeObject(id);
                            toClient.flush();
                        }
                        resultSet.close();
                        insertStatementForMines.close();
                    }
                    else if(temporary.size() == 1 && !temporary.get(0).equals("Show Top 5")){
                        selectStatementForOpen.setInt(1, (int)temporary.get(0));
                        resultSet = selectStatementForOpen.executeQuery();
                        if(resultSet.next()){
                            byte[] buffer = resultSet.getBytes(1);
                            ArrayList<Object> status = Client.readByte(buffer);
                            toClient.writeObject(status);
                        }
                        else{
                            toClient.writeObject("Can't find in DB");
                        }
                        toClient.flush();
                        resultSet.close();
                        selectStatementForOpen.close();
                    }
                    else if(temporary.size() == 2) {
                        insertStatementForRank.setBytes(1, Client.takeByte((Object) temporary));
                        insertStatementForRank.executeUpdate();
                        ta.append("Database has been updated\n");
                        insertStatementForRank.close();
                    }
                    else if(temporary.size() == 1 && temporary.get(0).equals("Show Top 5")){
                        resultSet = selectStatementForRank.executeQuery();
                        ArrayList<Object> resList = new ArrayList<>();
                        while(resultSet.next()){
                            byte[] buffer = resultSet.getBytes(1);
                            ArrayList<Object> status = Client.readByte(buffer);
                            resList.add(status);
                        }
                        toClient.writeObject(resList);
                        toClient.flush();
                        resultSet.close();
                        selectStatementForRank.close();
                    }
                }
            } catch ( SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException ignored){

            }

        }
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(8000);
            ta.append("Server started at "
                    + new Date() + '\n');
            while(true){
                Socket socket = serverSocket.accept();
                new Thread(new HandleSomeClient(socket, clientNo)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        Server server = new Server();
        server.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        server.setVisible(true);
        server.setLocationRelativeTo(null);
    }
}
