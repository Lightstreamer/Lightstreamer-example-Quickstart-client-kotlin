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

class Stocklist {

    internal fun start(serverAddress: String) {
        val client = LightstreamerClient(serverAddress, "DEMO")

        val clientListener = SystemOutClientListener()
        client.addListener(clientListener)

        val sub = Subscription("MERGE", items, fields)
        sub.requestedSnapshot = "yes"
        sub.dataAdapter = "QUOTE_ADAPTER"

        val subListener = SystemOutSubscriptionListener()
        sub.addListener(subListener)

        client.subscribe(sub)
        client.connect()
    }

    private class SystemOutSubscriptionListener : SubscriptionListener {

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

        override fun onItemUpdate(update: ItemUpdate) {

            println("====UPDATE====> " + update.itemName)

            val changedValues = update.changedFields.entries.iterator()
            while (changedValues.hasNext()) {
                val field = changedValues.next()
                println("Field ${field.key} changed: ${field.value}")
            }

            println("<====UPDATE====")
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

    companion object {

        /**
         * Items supplied by StockList Demo Data Adapter
         */
        private val items = arrayOf("item1", "item2", "item3", "item4", "item5", "item6", "item7", "item8", "item9", "item10", "item11", "item12", "item13", "item14", "item15")

        /**
         * Fields supplied by StockList Demo Data Adapter
         */
        private val fields = arrayOf("last_price", "time", "pct_change", "bid_quantity", "bid", "ask", "ask_quantity", "min", "max", "ref_price", "open_price")

        @JvmStatic fun main(args: Array<String>) {
            val serverAddress = args[0]

            Stocklist().start(serverAddress)

            CountDownLatch(1).await() //just wait
        }
    }

}
