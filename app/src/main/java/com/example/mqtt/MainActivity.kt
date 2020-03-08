package com.example.mqtt

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*


class MainActivity : AppCompatActivity() {
    var adapter : HistoryAdapter? = null

    var mqttAndroidClient : MqttAndroidClient? = null

    val serverUri = "tcp::iot.eclipse.org:1883"

    var clientId = "ExampleAndroidClient"
    val subscriptionTopic = "exampleAndroidTopic"
    val publishTopic = "exampleAndroidPublishTopic"
    val publishMessage = "Hello Word!"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { publishMessage() }

        val recyclerView = findViewById<RecyclerView>(R.id.history_recycler_view)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        adapter = HistoryAdapter(ArrayList())
        recyclerView.adapter = adapter

        clientId += System.currentTimeMillis()

        mqttAndroidClient = MqttAndroidClient(applicationContext, serverUri, clientId)
        mqttAndroidClient?.setCallback(object : MqttCallbackExtended{
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                if (reconnect) {
                    addToHistory("Reconnected to $serverURI")
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic()
                }
                else {
                    addToHistory("Connected to: $serverURI");
                }
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                addToHistory("Incoming message: " + (message?.payload ?: arrayOf(0)).toString());
            }

            override fun connectionLost(cause: Throwable?) {
                addToHistory("The Connection was lost.");
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {

            }
        })

        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = false

        try {
            mqttAndroidClient?.connect(mqttConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    val disconnectedBufferOptions = DisconnectedBufferOptions()
                    disconnectedBufferOptions.isBufferEnabled = true
                    disconnectedBufferOptions.bufferSize = 100
                    disconnectedBufferOptions.isPersistBuffer = false
                    disconnectedBufferOptions.isDeleteOldestMessages = false
                    mqttAndroidClient?.setBufferOpts(disconnectedBufferOptions)
                    subscribeToTopic()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    addToHistory("Failed to connect to: $serverUri");
                }
            })
        } catch (e : MqttException)
        {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item)
    }

    private fun addToHistory(mainText: String) {
        println("LOG: $mainText")
        adapter?.add(mainText)
    }

    private fun subscribeToTopic() {
        try {
            mqttAndroidClient?.subscribe(subscriptionTopic, 0, null, object : IMqttActionListener{
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    addToHistory("Subscribed!");
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    addToHistory("Failed to subscribe");
                }
            })

            // THIS DOES NOT WORK!
            mqttAndroidClient?.subscribe(subscriptionTopic, 0, object : IMqttMessageListener{
                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    // message Arrived!
                    System.out.println("Message: " + topic + " : " + message?.payload);
                }
            })
        }
        catch (e : MqttException) {
            println("Exception whilst subscribing")
            e.printStackTrace();
        }
    }

    private fun publishMessage() {
        try {
            val message = MqttMessage()
            message.payload = publishMessage.toByteArray()
            mqttAndroidClient?.publish(publishTopic, message)
            addToHistory("Message published")
            if (mqttAndroidClient?.isConnected != true) {
                addToHistory("${mqttAndroidClient?.bufferedMessageCount} messages in buffer.")
            }
        } catch(e : MqttException) {
            println("Error Publishing: ${e.message}")
            e.printStackTrace()
        }
    }
}
