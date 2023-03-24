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

import com.lightstreamer.client.ClientListener
import com.lightstreamer.client.LightstreamerClient

class SystemOutClientListener : ClientListener {

    override fun onListenEnd() {
        println("Stops listening to client events")
    }

    override fun onListenStart() {
        println("Start listening to client events")
    }

    override fun onPropertyChange(property: String) {
        println("Client property changed: $property")
    }

    override fun onServerError(code: Int, message: String) {
        println("Server error: $code: $message")
    }

    override fun onStatusChange(newStatus: String) {
        println("Connection status changed to $newStatus")
    }
}
