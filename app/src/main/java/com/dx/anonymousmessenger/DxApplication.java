package com.dx.anonymousmessenger;

import android.app.ActivityManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.os.StrictMode;

import androidx.core.app.TaskStackBuilder;

import com.dx.anonymousmessenger.tor.ServerSocketViaTor;
import com.dx.anonymousmessenger.R;

import net.sf.controller.network.AndroidTorRelay;
import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;
import java.io.IOException;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class DxApplication extends Application {

    private ServerSocketViaTor torSocket;
    private String hostname;
    private boolean serverReady = false;
    private DxAccount account;
    private Thread torThread;
    private SQLiteDatabase database;

    public DxAccount getAccount() {
        return account;
    }

    public void setAccount(DxAccount account) {
        this.account = account;
        if(account!=null){
            this.hostname = account.getAddress();
        }
    }

    public ServerSocketViaTor getTorSocket() {
        return torSocket;
    }

    public void enableStrictMode(){
        StrictMode.ThreadPolicy policy =
                new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }
    public void setTorSocket(ServerSocketViaTor torSocket) {
        this.torSocket = torSocket;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public boolean isServerReady() {
        return serverReady;
    }

    public void setServerReady(boolean serverReady) {
        this.serverReady = serverReady;
    }

    public void sendNotification(String title, String msg){
        Intent resultIntent = new Intent(this, AppActivity.class);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        String CHANNEL_ID = "messages";
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this,CHANNEL_ID)
                    .setSmallIcon(R.drawable.notification)
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setContentIntent(resultPendingIntent)
                    .setAutoCancel(true)
                    .setChannelId(CHANNEL_ID).build();
        }else{
            notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.notification)
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setContentIntent(resultPendingIntent)
                    .setAutoCancel(true)
                    .build();
        }
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID,
                    CHANNEL_ID,NotificationManager.IMPORTANCE_HIGH));
        }
        // Issue the notification.
        mNotificationManager.notify(1 , notification);
    }

    public void sendNotification(String title, String msg, boolean isMessage){
        if(isMessage){
            sendNotification(title,msg);
            return;
        }
        String CHANNEL_ID = "status_messages";
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this,CHANNEL_ID)
                    .setSmallIcon(R.drawable.notification)
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setAutoCancel(true)
                    .setChannelId(CHANNEL_ID).build();
        }else{
            notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.notification)
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setAutoCancel(true)
                    .build();
        }
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID,
                    CHANNEL_ID,NotificationManager.IMPORTANCE_LOW));
        }
        // Issue the notification.
        mNotificationManager.notify(2 , notification);
    }

    public Notification sendNotification(String title, String msg, String CHANNEL_ID){
        Notification notification;
        Intent intent = new Intent(this, NotificationHiderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this,CHANNEL_ID)
                    .setSmallIcon(R.drawable.notification)
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setChannelId(CHANNEL_ID).build();
        }else{
            notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.notification)
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();
        }
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID,
                    CHANNEL_ID,NotificationManager.IMPORTANCE_NONE));
        }
//        // Issue the notification.
//        mNotificationManager.notify(1 , notification);
        return notification;
    }

    public AndroidTorRelay getAndroidTorRelay(){
        return torSocket.getAndroidTorRelay();
    }

    public Thread getTorThread() {
        return torThread;
    }

    public void setTorThread(Thread torThread) {
        this.torThread = torThread;
    }

    public boolean getServerReady() {
        return serverReady;
    }

    public void startTor(){
        if(isServiceRunningInForeground(this,MyService.class)){
            return;
        }
        Intent serviceIntent = new Intent(this, MyService.class);
        serviceIntent.putExtra("inputExtra", "wtf bitch");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        }else{
            startService(serviceIntent);
        }
    }

    public Thread startTor(int num){
        if(num==2&&torThread!=null){
            if(torSocket!=null&&torSocket.getAndroidTorRelay()!=null){
                new Thread(()->{
                    try {
                        torSocket.getAndroidTorRelay().shutDown();
                        Thread.sleep(600);
                        torSocket.getAndroidTorRelay().initTor();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            return torThread;
        }
        enableStrictMode();
        Thread torThread = new Thread(() -> {
            try {
                setTorSocket(new ServerSocketViaTor(getApplicationContext()));
                ServerSocketViaTor ssvt = getTorSocket();
                ssvt.init(this);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });
        torThread.start();
        setTorThread(torThread);
        return torThread;
    }

    public SQLiteDatabase getDb(){
        if(this.database==null){
            SQLiteDatabase.loadLibs(this);
            File databaseFile = new File(getFilesDir(), "demo.db");
            SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databaseFile,
                    account.getPassword(),
                    null);
            this.database = database;
            return database;
        }else{
            return this.database;
        }
    }

    public void setDb(SQLiteDatabase database){
        this.database = database;
    }

    public void requestBatteryOptimizationOff(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getApplicationContext().getPackageName();
            PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("package:" + packageName));
                getApplicationContext().startActivity(intent);
            }
        }
    }

    public boolean isServiceRunningInForeground(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }
}