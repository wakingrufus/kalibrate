package com.github.wakingrufus.kalibrate


import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import mu.KLogging
import java.net.ServerSocket
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet

@SuppressFBWarnings("PREDICTABLE_RANDOM")
class PortHelper {
    companion object : KLogging() {
        /**
         * The default minimum value for port ranges used when finding an available
         * socket port.
         */
        private const val PORT_RANGE_MIN = 32_768

        /**
         * The default maximum value for port ranges used when finding an available
         * socket port.
         */
        private const val PORT_RANGE_MAX = 65_535


        private val random = Random()
        private val usedPorts = CopyOnWriteArraySet<Int>()

        /**
         * Release a port to be chosen again, if you are sure that you don't need it anymore
         *
         * @param port
         */
        fun releasePort(port: Int) {
            usedPorts.remove(port)
        }

        fun freePort(firstGuess: Int = findRandomPort(PORT_RANGE_MIN, PORT_RANGE_MAX)): Int {
            var foundPort = 0
            var portTry = firstGuess
            while (foundPort == 0 && usedPorts.size < PORT_RANGE_MAX - PORT_RANGE_MIN) {
                if (usedPorts.contains(portTry)) {
                    portTry = findRandomPort(PORT_RANGE_MIN, PORT_RANGE_MAX)
                } else {
                    ServerSocket(portTry).use {
                        logger.debug("PortHelper found new port={}", portTry)
                        foundPort = it.localPort
                        usedPorts.add(foundPort)
                    }.runCatching {
                        usedPorts.add(portTry)
                        portTry = findRandomPort(PORT_RANGE_MIN, PORT_RANGE_MAX)
                    }
                }
            }
            return foundPort
        }

        /**
         * Find a pseudo-random port number within the range
         * [{@code minPort}, {@code maxPort}].
         *
         * @param minPort the minimum port number
         * @param maxPort the maximum port number
         * @return a random port number within the specified range
         */
        fun findRandomPort(minPort: Int, maxPort: Int): Int {
            val portRange = maxPort - minPort
            return minPort + random.nextInt(portRange + 1)
        }
    }
}
