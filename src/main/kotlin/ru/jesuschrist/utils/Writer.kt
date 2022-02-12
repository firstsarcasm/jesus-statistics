package ru.jesuschrist.utils

import ru.jesuschrist.client.PersonalMessage
import ru.jesuschrist.client.PersonalMessage.Companion.buildBody
import ru.jesuschrist.client.PersonalMessage.Companion.buildSubject
import ru.jesuschrist.config.ConfigLoader
import java.io.File
import java.time.Instant

class Writer {
    companion object {
        @JvmStatic
        fun writeMessage(message: PersonalMessage) {

            val path = "${buildSubject(message)}.html"
            val file = File(path)

            file.parentFile.mkdirs()
            file.createNewFile()

            val messageToWrite = buildBody(message)
            file.printWriter().use { out ->
                out.println(messageToWrite)
            }
        }

        fun writeMessageInLog(message: PersonalMessage):String {
            val path = "${ConfigLoader.login}.log"
            val file = File(path)
            if (file.exists().not()) {
                file.createNewFile()
                file.appendText("Новые просмотренные сообщения:\n")
            }
            val messageToWrite = "[${Instant.now()}]:\n${message}\n"
            file.appendText(messageToWrite)
            return messageToWrite
        }

        fun writeTextInLog(text: String):String {
            val path = "${ConfigLoader.login}.log"
            val file = File(path)
            if (file.exists().not()) {
                file.createNewFile()
            }
            file.appendText(text)
            return text
        }
        fun writeSeparatorInLog():String =
                writeTextInLog("---------------------------------------\n")
    }
}