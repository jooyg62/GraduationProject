package kr.ac.hansung.gualapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MainActivity extends AppCompatActivity implements MqttCallback{

    // Debug TAG
    private static final String MQTT_TAG = "MqttService";

    int qos = 0;
    String broker = "tcp://172.30.1.9:1883";
    String clientId = "JavaSample";

    MemoryPersistence persistence = new MemoryPersistence();

    private Handler mConnHandler;     // Seperate Handler thread for networking

    private MqttClient testClient;
    private MqttConnectOptions connOpts;

    private String receivedMsg;
    TextView textView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HandlerThread thread = new HandlerThread("MQTT_THREAD_NAME");
        thread.start();

        mConnHandler = new Handler(thread.getLooper());

        try {
            testClient = new MqttClient(broker, clientId, persistence);
            connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
        } catch(MqttException e){
            e.printStackTrace();
        }

        mConnHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    testClient.connect(connOpts);
                    testClient.subscribe("Test/WaterPump", 0);
                    testClient.setCallback(MainActivity.this);

                } catch(MqttException e) {
                    e.printStackTrace();
                }
            }
        });


        //물주기버튼..
        Button waterSupplyBtn = (Button) findViewById(R.id.WaterSupplyBtn);
        waterSupplyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msg = "1";

                MqttMessage message = new MqttMessage(msg.getBytes());
                message.setQos(qos);
                MqttTopic mkeepAliveTopic = testClient.getTopic("Test/WaterPump");
                try {
                    testClient.publish("Test/WaterPump", message);
                } catch(MqttException e){
                    e.printStackTrace();
                }
            }
        });

        //testView
        textView = (TextView) findViewById(R.id.textView);

        //동기화버튼..
        Button synchronizeBtn = (Button) findViewById(R.id.SynchronizeBtn);
        synchronizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText(receivedMsg);
            }
        });

        //notification-----------------------------------------------------
        Button notificationBtn = (Button) findViewById(R.id.notificationBtn);

        notificationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

                // PendingIntent를 등록 하고, noti를 클릭시에 어떤 클래스를 호출 할 것인지 등록.
                PendingIntent intent = PendingIntent.getActivity(
                        MainActivity.this, 0,
                        new Intent(MainActivity.this, NotificationConfirm.class), 0);

                // status bar 에 등록될 메시지(Tiker, 아이콘, 그리고 noti가 실행될 시간)
                Notification notification = new Notification();
                notification.flags |= Notification.FLAG_SHOW_LIGHTS;
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                notification.defaults = Notification.DEFAULT_ALL;
                notification.when = System.currentTimeMillis();
                // noti를 클릭 했을 경우 자동으로 noti Icon 제거
//          notification.flags = notification.flags | notification.FLAG_AUTO_CANCEL;

                Notification.Builder builder = new Notification.Builder(MainActivity.this)
                        .setContentIntent(intent)
                        .setSmallIcon(android.R.drawable.btn_star)
                        .setContentTitle("Title입니다.")
                        .setContentText("TextMessage입니다.")
                        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                notification = builder.build();

                // 1234 notification 의 고유아이디
                nm.notify(1234, notification);
                Toast.makeText(MainActivity.this, "Notification Registered.",
                        Toast.LENGTH_SHORT).show();

            }
        });

        //----------------------------------notification end..


    }//onCreate() end..


    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        //topic이 무엇이냐에 따라
        receivedMsg = new String(message.getPayload());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }


}