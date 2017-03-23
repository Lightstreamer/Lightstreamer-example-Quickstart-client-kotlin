/*
 * Copyright (c) Lightstreamer Srl
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package quickstart

import com.lightstreamer.client.*
import java.text.SimpleDateFormat
import java.util.*

class Chat {

    internal fun start(serverAddress: String) {

        // the chat demo available @ http://demos.lightstreamer.com/ChatDemo/
        // connects to the DEMO adapter set on push.lightstreamer.com rather than 
        // to a CHAT one (obviously that DEMO adapter set contains the CHAT_ROOM
        // adapters). 
        // For this reason, when connecting to push.lightstreamer.com we use the DEMO
        // adapter set, so you can open a browser to the above address and chat
        // with yourself.

        val adapterSet = if (serverAddress.contains("push.lightstreamer.com")) "DEMO" else "CHAT"
        val client = LightstreamerClient(serverAddress, adapterSet)

        val clientListener = SystemOutClientListener()
        client.addListener(clientListener)

        val sub = Subscription("DISTINCT", "chat_room", arrayOf("raw_timestamp", "message", "IP"))
        sub.requestedSnapshot = "yes"
        sub.dataAdapter = "CHAT_ROOM"

        val subListener = SystemOutSubscriptionListener()
        sub.addListener(subListener)

        client.subscribe(sub)
        client.connect()

        val sentMessageListener = SystemOutClientMessageListener()

        val PREFIX = "CHAT|"
        println("========NOW READING FROM SYSTEM IN=========")
        var queueWhileDisconnected = true
        System.`in`.bufferedReader().useLines { lines ->
            lines.forEach { mex ->
                try {
                    //none of these calls blocks
                    when (mex.trim()) {

                    // connect/disconnect commands
                        "STOP" -> client.disconnect()
                        "START" -> client.connect()

                    // subscribe / unsubscribe commands
                        "UNSUB" -> client.unsubscribe(sub)
                        "SUB" -> client.subscribe(sub)

                    // specifiying true as last parameter, if a message is sent while not connected
                    // to Lightstreamer, the client will queue it and will send as soon as
                    // a new connection is available. Use these commands to switch the flag for the
                    // next messages
                        "NOQUEUE" -> {
                            queueWhileDisconnected = false
                            println("=== Not queuing messages if disconnected")
                        }
                        "QUEUE" -> {
                            println("=== Queuing messages if disconnected")
                            queueWhileDisconnected = true
                        }

                    // exiting this loop there will be no more non-daemon threads and the application
                    // will terminate
                        "EXIT" -> return

                        "" -> {
                            // skip blank line
                        }

                        else -> client.sendMessage(PREFIX + mex, "chat", 500, sentMessageListener, queueWhileDisconnected)
                    }
                } catch (e: IllegalArgumentException) {
                    //some of the above calls might throw exceptions, see API docs for details
                    System.err.println(e.message)
                } catch (e: IllegalStateException) {
                    System.err.println(e.message)
                }
            }
        }
    }


    class SystemOutSubscriptionListener : SubscriptionListener {

        override fun onClearSnapshot(itemName: String, itemPos: Int) {
            println("Server has cleared the current status of the chat")
        }

        override fun onCommandSecondLevelItemLostUpdates(lostUpdates: Int, key: String) {
            //not on this subscription
        }

        override fun onCommandSecondLevelSubscriptionError(code: Int, message: String, key: String) {
            //not on this subscription
        }

        override fun onEndOfSnapshot(arg0: String, arg1: Int) {
            println("Snapshot is now fully received, from now on only real-time messages will be received")
        }

        override fun onItemLostUpdates(itemName: String, itemPos: Int, lostUpdates: Int) {
            println("$lostUpdates messages were lost")
        }

        internal var dateFormatter = SimpleDateFormat("E hh:mm:ss")

        override fun onItemUpdate(update: ItemUpdate) {
            val timestamp = java.lang.Long.parseLong(update.getValue("raw_timestamp"))
            val time = Date(timestamp)
            println("MESSAGE @ ${dateFormatter.format(time)} |${update.getValue("IP")}:${update.getValue("message")}")
        }

        override fun onListenEnd(subscription: Subscription) {
            println("Stop listeneing to subscription events")
        }

        override fun onListenStart(subscription: Subscription) {
            println("Start listeneing to subscription events")
        }

        override fun onSubscription() {
            println("Now subscribed to the chat item, messages will now start coming in")
        }

        override fun onSubscriptionError(code: Int, message: String) {
            println("Cannot subscribe because of error $code: $message")
        }

        override fun onUnsubscription() {
            println("Now unsubscribed from chat item, no more messages will be received")
        }
        
        fun onRealMaxFrequency(frequency: String) {
          println("Frequency is " + frequency)
      	}

    }

    class SystemOutClientMessageListener : ClientMessageListener {

        override fun onAbort(originalMessage: String, sentOnNetwork: Boolean) {
            if (sentOnNetwork) {
                println("message \"$originalMessage\" was aborted; is not known if it reached the server")
            } else {
                println("message \"$originalMessage\" was aborted and will not be sent to the server")
            }

        }

        override fun onDeny(originalMessage: String, code: Int, message: String) {
            println("message \"$originalMessage\" was denied by the server because of error $code: $message")
        }

        override fun onDiscarded(originalMessage: String) {
            println("message \"$originalMessage\" was discarded by the server because it was too late when it was received")
        }

        override fun onError(originalMessage: String) {
            println("message \"$originalMessage\" was not correctly processed by the server")
        }

        override fun onProcessed(originalMessage: String) {
            println("message \"$originalMessage\" processed correctly")
        }
    }

    companion object {

        /**
         * Simple chat that reads/prints chat messages from/to the command line.

         * It shows how to connect/disconnect, subscribe/unsubscribe and
         * send messages to Lightstreamer.
         * It requires that Lightstreamer Server is running with the CHAT
         * Adapter Set installed.

         * The test is invoked in this way:
         * java quickstart.Chat
         * where  stands for the full address of Lightstreamer Server
         * (e.g.: https://push.lightstreamer.com)


         * Some special commands can be issues to see how the client behaves:
         * STOP - disconnects the client
         * START - reconnects the client
         * UNSUB - unsubscribes from the chat
         * SUB - subscribes to the chat
         * EXIT - exits the application
         * (the application starts by connecting and subscribing)


         * @param args Should specify the address the Server
         */
        @JvmStatic fun main(args: Array<String>) {
            val serverAddress = args[0]

            Chat().start(serverAddress)
        }
    }

}
