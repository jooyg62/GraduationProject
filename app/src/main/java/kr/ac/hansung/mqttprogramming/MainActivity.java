package kr.ac.hansung.mqttprogramming;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
//
//    // Broker URL or IP Address
//    private static final String MQTT_BROKER = "223.194.130.87";
//    // Broker port
//    //private static final String MQTT_PORT = "1833";

//    String topic = "simpletest";
//    String content = "Message from MqttPublishSample";
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
