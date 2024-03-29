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
@file:JvmName("Main")

package quickstart

import java.util.concurrent.CountDownLatch
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    when (args[0]) {
        "chat" -> Chat().start(args[1])
        "stocklist" -> {
            Stocklist().start(args[1])
            slumber()
        }
        "portfolio" -> {
            Portfolio().start(args[1])
            slumber()
        }
        "orderentry" -> {
            PortfolioOrderEntry.main(args.sliceArray(1 until args.size))
        }
    }
    exitProcess(0)
}

private fun slumber() {
    CountDownLatch(1).await()
}
