package ru.jesuschrist.config

import com.typesafe.config.ConfigFactory
import java.io.File
import java.time.Duration

class ConfigLoader {

    companion object {
        private val conf = ConfigFactory.parseFile(File("jesus-statistics.conf"))
        val login = conf.getString("auth.login")
        val password = conf.getString("auth.password")

        val logoutTimeout = "timeouts.logout-timeout".valueOrDefault(Duration.ofMinutes(2)).toMillis().toInt()

        val blackList = "conditions.black-list".valueOrDefault(listOf(""))
        val allowedHiddenUsers = "conditions.allowed-hidden-users".valueOrDefault(0).zeroIfNegative()
        val searchAfterDate = conf.getString("conditions.search-after-date")

        val proxyEnabled = "proxy.enabled".valueOrDefault(false)
        val proxyHost = "proxy.host".valueOrDefault("")
        val proxyPort = "proxy.port".valueOrDefault(0)

        val lightPersonalMessage = "logging.light-personal-message".valueOrDefault(true)
        val showUsersList = "logging.show-users-list".valueOrDefault(false)

        val retryDelay = "retry.delay".valueOrDefault(Duration.ofMinutes(1)).toMillis()
        val retryCount = "retry.count".valueOrDefault(0)

        val retryRandomPrefix = "retry.delay-random-prefix".valueOrDefault(Duration.ZERO).toMillis().toInt()

        val to = "mail.to".valueOrDefault("")
        val senderLogin = "mail.login".valueOrDefault("")
        val senderPassword = "mail.password".valueOrDefault("")
        val mailEnabled = "mail.enabled".valueOrDefault(false)

        val maxMessageToSend = "mail.max-message-to-send".valueOrDefault(10L)

        val telegramChatId = "telegram.chat-id".valueOrDefault(0L)
        val telegramEnabled = "telegram.enabled".valueOrDefault(false)
        val telegramBotName = "telegram.bot.name".valueOrDefault("")
        val telegramBotToken = "telegram.bot.token".valueOrDefault("")

        val sslVerification = "ssl.validate-certificates".valueOrDefault(true)

        private fun Int.zeroIfNegative(): Int {
            if (this < 0) return 0
            return this
        }

        private fun String.valueOrDefault(default: Boolean): Boolean =
                this.let { if (conf.hasPath(it)) conf.getBoolean(it) else default }

        private fun String.valueOrDefault(default: String): String =
                this.let { if (conf.hasPath(it)) conf.getString(it) else default }

        private fun String.valueOrDefault(default: Long): Long =
                this.let { if (conf.hasPath(it)) conf.getLong(it) else default }

        private fun String.valueOrDefault(default: Int): Int =
                this.let { if (conf.hasPath(it)) conf.getInt(it) else default }

        private fun String.valueOrDefault(default: Duration): Duration =
                this.let { if (conf.hasPath(it)) conf.getDuration(it) else default }

        private fun String.valueOrDefault(default: List<String>): List<String> =
                this.let { if (ConfigLoader.conf.hasPath(it)) ConfigLoader.conf.getStringList(it) else default }
    }
}