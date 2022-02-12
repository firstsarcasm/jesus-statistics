package ru.jesuschrist

import ru.jesuschrist.config.ConfigLoader
import ru.jesuschrist.client.JesusClient
import ru.jesuschrist.telegram.JesusBot
import java.io.IOException
import java.lang.Thread.sleep
import java.util.*

object Main {
    val client = JesusClient()
    val rnd = Random()
    val maxRandomValue = ConfigLoader.retryRandomPrefix
    val baseTimeToSleep = ConfigLoader.retryDelay

    val botControl = JesusBot.getBotControl()

    private fun JesusClient.silentLogout(): Boolean =
            try { this.logout(); true }
            catch (e: IOException) { println(e); false }

    @Throws(InterruptedException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                println("Shutdown:")
                if(client.silentLogout()) {
                    println("Выполнен завершающий выход из аккаунта")
                } else {
                    println("При попытке завершающего выхода из аккаунта произошла ошибка")
                }
                client.serialize()
                println("Хранилище сообщений сохранено на диск")
                botControl.stop()
            }
        })

        val allowedRetryCount = ConfigLoader.retryCount
        var currentRetryCount = 0

        if (ConfigLoader.telegramEnabled) {
            botControl.thread.start()
        }

        while (allowedRetryCount > currentRetryCount || allowedRetryCount <= 0) {
            try {
                if (client.checkContext()) {
                    client.getAndParsePrivateMessagesPage()
                } else {
                    println("Один из подозрительных пользователей онлайн или есть скрытые пользователи." +
                            " Запрос личных сообщений не будет выполнен")
                }
                val randomValue = rnd.nextInt(maxRandomValue)
                sleep(baseTimeToSleep + randomValue)
                currentRetryCount++
            } catch (e: Exception) {
                println("При выполнении запроса произошла ошибка: ${e}")
                if(client.silentLogout()) {
                    println("Пробую снова...(Был выполнен выход из аккаунта).")
                } else {
                    println("Пробую снова...(Из аккаунта выйти тоже не удалось. Может в следующий раз).")
                }
            }
        }
        botControl.stop()
    }
}