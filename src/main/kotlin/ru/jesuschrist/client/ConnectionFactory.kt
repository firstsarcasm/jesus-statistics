package ru.jesuschrist.client

import ru.jesuschrist.config.ConfigLoader
import ru.jesuschrist.config.ConfigLoader.Companion.sslVerification
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ru.jesuschrist.metrics.Connections

class ConnectionFactory {
    fun createConnection(url: String, authentication: Boolean = false, timeout: Int = 90000): Document {
        val connection = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows; U; MSIE 9.0; Windows NT 6.0; Win64; x64; Trident/5.0; .NET CLR 3.8.50799; Media Center PC 6.0; .NET4.0E)")
                .timeout(timeout)

        if (ConfigLoader.proxyEnabled) {
            connection.setProxy()
        }

        connection.validateTLSCertificates(sslVerification)

        if (authentication) {
            connection.setCookie()
        }

        Connections.connections.incrementAndGet()
        val get = connection.get()
        Connections.connections.decrementAndGet()
        return get
    }

    private fun Connection.setCookie(): Connection? =
        this.cookie("w3t_myname", ConfigLoader.login)
                .cookie("w3t_mypass", ConfigLoader.password)
                .cookie("w3t_visit", "-announce=-")

    private fun Connection.setProxy(): Connection =
        this.proxy(ConfigLoader.proxyHost, ConfigLoader.proxyPort)
}