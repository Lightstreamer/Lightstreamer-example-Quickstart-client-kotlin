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
import java.util.concurrent.CountDownLatch

class PortfolioOrderEntry {

    internal fun start(serverAddress: String, stockName: String, quantity: Int, isBuy: Boolean) {

        // the portfolio demo available @ http://demos.lightstreamer.com/PortfolioDemo/
        // connects to the DEMO adapter set on push.lightstreamer.com rather than 
        // to a FULLPORTFOLIODEMO one (obviously that DEMO adapter set contains the 
        // needed adapters). 
        // For this reason, when connecting to push.lightstreamer.com we use the DEMO
        // adapter set.

        val adapterSet = if (serverAddress.contains("push.lightstreamer.com")) "DEMO" else "FULLPORTFOLIODEMO"
        val client = LightstreamerClient(serverAddress, adapterSet)

        client.connect()

        val cdl = CountDownLatch(1)
        val sentMessageListener = SystemOutClientMessageListener(cdl)

        val prefix = if (isBuy) "BUY" else "SELL"
        val s = "${prefix}|portfolio1|${stockName}|${quantity}"
        client.sendMessage(s, "orders", 500, sentMessageListener, true)

        cdl.await()

        val future = client.disconnectFuture()
        future.get()
        System.exit(0)
    }

    class SystemOutClientMessageListener(private val cdl: CountDownLatch) : ClientMessageListener {

        override fun onAbort(originalMessage: String, sentOnNetwork: Boolean) {
            if (sentOnNetwork) {
                println("message \"$originalMessage\" was aborted; is not known if it reached the server")
            } else {
                println("message \"$originalMessage\" was aborted and will not be sent to the server")
            }
            cdl.countDown();
        }

        override fun onDeny(originalMessage: String, code: Int, message: String) {
            println("message \"$originalMessage\" was denied by the server because of error $code: $message")
            cdl.countDown();
        }

        override fun onDiscarded(originalMessage: String) {
            println("message \"$originalMessage\" was discarded by the server because it was too late when it was received")
            cdl.countDown();
        }

        override fun onError(originalMessage: String) {
            println("message \"$originalMessage\" was not correctly processed by the server")
            cdl.countDown();
        }

        override fun onProcessed(originalMessage: String, response: String) {
            println("message \"$originalMessage\" sent with response \"$response\"")
            cdl.countDown();
        }
    }

    companion object {

        /**
         * Simple order entry for the portfolio demo that reads orders from the command line.

         * It shows how to connect/disconnect and send messages to Lightstreamer
         * and receive a response to the message submission.
         * It requires that Lightstreamer Server is running with the FULLPORTFOLIODEMO
         * Adapter Set installed.

         * The test is invoked in this way:
         *    java quickstart.PortfolioOrderEntry <serverAddress> <stock> <quantity>
         * where <serverAddress> stands for the full address of Lightstreamer Server
         * (e.g.: https://push.lightstreamer.com),
         * <stock> is the name of one of the stocks supported by the PortfolioDemo,
         * and <quantity> is the quantity to buy (if positive) or to sell (if negative)

         * The effect of the operation can be seen through a concurrently running
         * Portfolio demo.

         * @param args Should specify the address of the Server, stock, and quantity
         */
        @JvmStatic fun main(args: Array<String>) {
            val serverAddress = args[0]
            val stockName = args[1]
            val quantity = Integer.parseInt(args[2])

            PortfolioOrderEntry().start(serverAddress, stockName, Math.abs(quantity), quantity >= 0)
        }
    }

}
