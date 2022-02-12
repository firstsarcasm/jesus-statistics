package ru.jesuschrist.telegram

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import ru.jesuschrist.config.ConfigLoader
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.TelegramBotsApi
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.exceptions.TelegramApiException
import org.telegram.telegrambots.logging.BotLogger
import ru.jesuschrist.metrics.Connections
import ru.jesuschrist.utils.atomicString
import ru.jesuschrist.utils.putOrUpdate
import java.util.logging.Level
import kotlin.reflect.KFunction1

class JesusBot : TelegramLongPollingBot() {

    val usersNotify = HashMap<Long, Boolean>()
    val suspends = HashMap<Long, Job>()
    val helpMessage = """
         Список доступных комманд:
            /connections - показать количество соединений установленных с сайтом в данный момент
            /notify_on - включить отправку нотификаций о прочитанных сообщениях
            /notify_off - выключить отправку нотификаций
            /help - отобразить это информационное сообщение
    """.trimIndent()

    companion object {
        @JvmStatic
        fun getBotControl(): BotControl {
            val controlBuilder = BotControl()
            controlBuilder.thread = Thread({
                ApiContextInitializer.init()
                val botapi = TelegramBotsApi()

                try {
                    BotLogger.setLevel(Level.OFF)
                    val botSession = botapi.registerBot(JesusBot())
                    controlBuilder.botSession = botSession
                } catch (e: TelegramApiException) {
                    println("Телеграм бот вернул ошибку: ${e}")
                }
            })
            return controlBuilder
        }
    }

    override fun getBotUsername() = ConfigLoader.telegramBotName

    override fun onUpdateReceived(update: Update) {
        if (!(update.hasMessage() && update.message.hasText())) return
        val incomingMessage = update.message

        val messageText = incomingMessage.text
        val chatId = incomingMessage.getChatId()

        val outgoingMessage = SendMessage().setChatId(chatId)

        fun send(text: String): Message? = execute<Message, SendMessage>(outgoingMessage.setText(text))

        if (chatId != ConfigLoader.telegramChatId) {
            send("Permission denied")
            return
        }

        if ("/help" in messageText) {
            send(helpMessage)
            return
        }

        if ("/connections" in messageText) {
            send(Connections.connections.get().toString())
            return
        }

        if ("/notify_off" in messageText) {
            if (chatId !in usersNotify || !usersNotify[chatId]!!) return
            usersNotify[chatId] = false
            suspends[chatId]!!.cancel()
            return
        }

        if ("/notify_on" in messageText) {
            if (chatId in usersNotify && usersNotify[chatId]!!) return
            usersNotify.putOrUpdate(chatId, true)

            runBlocking {
                val job = startNotify(::send)
                suspends.put(chatId, job)
            }
        }
    }

    private suspend fun startNotify(send: KFunction1<@ParameterName(name = "text") String, Message?>): Job =
            launch {
                while (true) {
                    val jesusSenderLog = atomicString.get()
                    if (jesusSenderLog.messageCompleted()) {
                        send.invoke(jesusSenderLog)
                        atomicString.lazySet("")
                    }
                    delay(1000)
                }
            }

    override fun getBotToken() = ConfigLoader.telegramBotToken
}

private fun String.messageCompleted(): Boolean =
        this.isNotEmpty() && this != "null"

