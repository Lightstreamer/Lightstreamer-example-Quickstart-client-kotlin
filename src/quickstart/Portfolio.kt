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

import com.lightstreamer.client.ItemUpdate
import com.lightstreamer.client.LightstreamerClient
import com.lightstreamer.client.Subscription
import com.lightstreamer.client.SubscriptionListener
import java.util.concurrent.CountDownLatch

class Portfolio {

    internal fun start(serverAddress: String) {
        // the portfolio demo available @ http://demos.lightstreamer.com/PortfolioDemo/
        // connects to the DEMO adapter set on push.lightstreamer.com rather than 
        // to a FULLPORTFOLIODEMO one (obviously that DEMO adapter set contains the 
        // needed adapters). 
        // For this reason, when connecting to push.lightstreamer.com we use the DEMO
        // adapter set.

        val adapterSet = if (serverAddress.contains("push.lightstreamer.com")) "DEMO" else "FULLPORTFOLIODEMO"
        val client = LightstreamerClient(serverAddress, adapterSet)

        val clientListener = SystemOutClientListener()
        client.addListener(clientListener)

        val sub = Subscription("COMMAND", "portfolio1", arrayOf("key", "command", "qty"))
        sub.requestedSnapshot = "yes"
        sub.dataAdapter = "PORTFOLIO_ADAPTER"
        sub.commandSecondLevelDataAdapter = "QUOTE_ADAPTER"
        sub.setCommandSecondLevelFields(arrayOf("stock_name", "last_price")) //the key values from the 1st level are used as item names for the second level

        val subListener = SystemOutSubscriptionListener()
        sub.addListener(subListener)

        client.subscribe(sub)
        client.connect()
    }

    private class SystemOutSubscriptionListener : SubscriptionListener {

        override fun onClearSnapshot(itemName: String?, itemPos: Int) {
            println("Server has cleared the current status of the portfolio")
        }

        override fun onCommandSecondLevelItemLostUpdates(lostUpdates: Int, key: String) {
            println("$lostUpdates  messages were lost ($key)")
        }

        override fun onCommandSecondLevelSubscriptionError(code: Int, message: String?, key: String) {
            println("Cannot subscribe (2nd-level item $key) because of error $code: $message")
        }

        override fun onEndOfSnapshot(itemName: String?, itemPos: Int) {
            println("Initial portfolio received")
        }

        override fun onItemLostUpdates(itemName: String?, itemPos: Int, lostUpdates: Int) {
            println("$lostUpdates  messages were lost")
        }

        override fun onItemUpdate(update: ItemUpdate) {
            val command = update.getValue("command")
            when (command) {
                "ADD" -> println("first update for this key (" + update.getValue("key") + "), the library is now automatically subscribing the second level item for it")
                "UPDATE" -> {
                    val updateString = StringBuilder().apply {
                        append("Update for ")
                        append(update.getValue("stock_name")) //2nd level field
                        append(", last price is ")
                        append(update.getValue("last_price")) //2nd level field
                        append(", we own ")
                        append(update.getValue("qty")) //1st level field
                    }

                    println(updateString)

                    //there is the possibility that a second update for the first level is received before the first update for the second level
                    //thus we might print a message that contains a few NULLs
                }
                "DELETE" -> println("key (${update.getValue("key")}), was removed, the library is now automatically unsubscribing the second level item for it")
            }
        }

        override fun onListenEnd(subscription: Subscription) {
            println("Stop listeneing to subscription events")
        }

        override fun onListenStart(subscription: Subscription) {
            println("Start listeneing to subscription events")
        }

        override fun onSubscription() {
            println("Now subscribed to the portfolio item")
        }

        override fun onSubscriptionError(code: Int, message: String?) {
            println("Cannot subscribe because of error $code: $message")
        }

        override fun onUnsubscription() {
            println("Now unsubscribed from portfolio item")
        }
        
        override fun onRealMaxFrequency(frequency: String?) {
          println("Frequency is " + frequency)
      	}

    }

    companion object {

        /* 
         * @param args Should specify the address the Server
         */
        @JvmStatic fun main(args: Array<String>) {
            val serverAddress = args[0]

            Portfolio().start(serverAddress)

            CountDownLatch(1).await() //just wait
        }
    }

}
