package com.dx.anonymousmessenger.tor;

import android.content.Context;
import android.util.Log;

import com.dx.anonymousmessenger.DxApplication;
import com.dx.anonymousmessenger.call.CallController;
import com.dx.anonymousmessenger.crypto.Entity;
import com.dx.anonymousmessenger.db.DbHelper;
import com.dx.anonymousmessenger.messages.MessageSender;

import net.sf.controller.network.AndroidTorRelay;
import net.sf.controller.network.TorServerSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class ServerSocketViaTor {
    private static final Logger LOG = LoggerFactory.getLogger(ServerSocketViaTor.class);
    private static final int hiddenservicedirport = 5780;
    private static final int localport = 5780;
    private static CountDownLatch serverLatch = new CountDownLatch(2);
    private Context ctx;
    AndroidTorRelay node;
    Thread serverThread;
    private TorServerSocket torServerSocket;
    private Server server;

    public ServerSocketViaTor(Context ctx) {
        this.ctx = ctx;
    }

    public AndroidTorRelay getAndroidTorRelay(){
        return node;
    }

    public void setAndroidTorRelay(AndroidTorRelay atr) {this.node = atr;}

    public void setServerThread(Thread thread) {this.serverThread = thread;}

    public Thread getServerThread() {return serverThread;}

    public void init(DxApplication app) throws IOException {
        if (ctx == null) {
            return;
        }

        if(node!=null){
            boolean exit = false;
            if(serverThread!=null){
                try{
                    serverThread.interrupt();
                    serverThread = null;
                }catch (Exception ignored){}
            }
            if(torServerSocket!=null){
                torServerSocket.getServerSocket().close();
                torServerSocket = null;
            }
            while (!exit){
                try {
                    node.shutDown();
                    Thread.sleep(1000);
                }catch (Exception e){
                    e.printStackTrace();
                }
                try {
                    node = null;
                    exit = true;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        String fileLocation = "torfiles";
        while (node==null){
            try {
                node = new AndroidTorRelay(ctx, fileLocation);
            }catch (Exception e){
                e.printStackTrace();
                try {
                    Thread.sleep(1000);
                }catch (InterruptedException ie){ie.printStackTrace();}
            }
        }

        this.torServerSocket = node.createHiddenService(localport, hiddenservicedirport);
        app.setHostname(torServerSocket.getHostname());
        Entity myEntity = new Entity(app);
        app.setEntity(myEntity);

        ServerSocket ssocks = torServerSocket.getServerSocket();
        this.server = new Server(ssocks,app);

        this.serverThread = new Thread(server);
        serverThread.start();

//        serverLatch.await();
    }

    public void tryKill(){
        if(node!=null){
            boolean exit = false;
            if(serverThread!=null){
                try{
                    serverThread.interrupt();
                    serverThread = null;
                }catch (Exception ignored){}
            }
            if(torServerSocket!=null){
                try {
                    torServerSocket.getServerSocket().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                torServerSocket = null;
            }
            while (!exit){
                try {
                    node.shutDown();
                    Thread.sleep(1000);
                }catch (Exception e){
                    e.printStackTrace();
                }
                try {
                    node = null;
                    exit = true;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private static class Server implements Runnable {
        private final ServerSocket socket;
//        private static final DateFormat df = new SimpleDateFormat("K:mm a, z", Locale.ENGLISH);
        private DxApplication app;

        private Server(ServerSocket socket, DxApplication app) {
            this.socket = socket;
            this.app = app;
        }

        @SuppressWarnings("InfiniteLoopStatement")
        @Override
        public void run() {
            app.setServerReady(true);
            try {
                while (true) {
                    Socket sock = socket.accept();
                    Log.d("SERVER CONNECTION", "RECEIVING SOMETHING");
                    try{
                        new Thread(()->{
                            try{
                                DataOutputStream outputStream = new DataOutputStream(sock.getOutputStream());
                                DataInputStream in=new DataInputStream(sock.getInputStream());
                                String msg = in.readUTF();
                                if(msg.contains("hello-")){
//                                    try {
//                                        outputStream.writeUTF("hello");
//                                        outputStream.flush();
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                    }

                                    if(DbHelper.contactExists(msg.replace("hello-",""),app)){
                                        app.addToOnlineList(msg.replace("hello-",""));
                                        app.queueUnsentMessages(msg.replace("hello-",""));
                                    }
                                    sock.close();
                                    return;
                                }else if(msg.equals("call")){
                                    try {
                                        CallController.callReceiveHandler(sock,app);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return;
                                }else if(msg.equals("media")){
                                    try {
                                        outputStream.writeUTF("ok");
                                        outputStream.flush();
                                        msg = in.readUTF();
                                        if(!msg.trim().endsWith(".onion")){
                                            //no bueno
                                            outputStream.writeUTF("nuf");
                                            outputStream.flush();
                                            sock.close();
                                            return;
                                        }
                                        String address= msg.trim();
                                        if(!DbHelper.contactExists(address,app)){
                                            //no bueno
                                            outputStream.writeUTF("nuf");
                                            outputStream.flush();
                                            sock.close();
                                            return;
                                        }
                                        outputStream.writeUTF("ok");
                                        outputStream.flush();
                                        msg = in.readUTF();
                                        String recMsg = msg;
                                        outputStream.writeUTF("ok");
                                        outputStream.flush();
                                        int fileSize = in.readInt();
                                        //maximum file size 30MB
                                        if(fileSize>(30*1024*1024)){
                                            //no bueno
                                            outputStream.writeUTF("nuf");
                                            outputStream.flush();
                                            sock.close();
                                            return;
                                        }
                                        outputStream.writeUTF("ok");
                                        outputStream.flush();
                                        byte[] buffer;
                                        ByteArrayOutputStream cache = new ByteArrayOutputStream();
                                        int total_read = 0;
                                        int read;
                                        while(total_read < fileSize){
                                            if(in.available() < 1024){
                                                if(in.available() == 0){
                                                    continue;
                                                }
                                                buffer = new byte[in.available()];
                                            }else{
                                                buffer = new byte[1024];
                                            }
                                            read = in.read(buffer,0,buffer.length);
                                            total_read += read;
                                            cache.write(buffer,0,buffer.length);
                                        }
                                        in.close();
                                        System.out.println("TOTAL BYTES READ : "+total_read);
                                        System.out.println("FILE SIZE : "+fileSize);
                                        MessageSender.mediaMessageReceiver(cache.toByteArray(),recMsg,app);
                                    } catch (Exception e) {
                                        Log.e("RECEIVING MEDIA MESSAGE","ERROR BELOW");
                                        e.printStackTrace();
                                    }
                                    return;
                                }
//                                while(!msg.equals("nuf"))
//                                {
                                    final String rec = msg;
                                    new Thread(()-> MessageSender.messageReceiver(rec,app)).start();
                                    outputStream.writeUTF("ack3");
                                    outputStream.flush();
//                                    msg = in.readUTF();
//                                }
                                outputStream.close();
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        }).start();

                    }catch (Exception e){
                        Log.e("SERVER ERRORRRRR", "EROROROROROROROR");
                        e.printStackTrace();
                    }
                }
            } catch (Exception  e) {
                e.printStackTrace();
                app.setServerReady(false);
            }
        }
    }
}
