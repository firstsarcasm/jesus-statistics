package ru.jesuschrist.client

import ru.jesuschrist.config.ConfigLoader
import java.io.File
import java.io.Serializable
import java.util.*

data class PersonalMessage(val id: Int, val status: Status, val subject: String, val sender: String, val url: String, val receiveDate: String, val textBody: String, val htmlBody: String, val head: String?) : Serializable {
    fun isViewed(): Boolean = this.status == Status.VIEWED

    override fun toString(): String =
            if (ConfigLoader.lightPersonalMessage) {
                "id=$id, status=$status, subject='$subject', sender='$sender', receiveDate='$receiveDate', body='${textBody.isNotEmpty()}'"
            } else {
                "id=$id, status=$status, subject='$subject', sender='$sender', url='$url', receiveDate='$receiveDate', body='$textBody'"
            }

    companion object {
        fun buildSubject(message: PersonalMessage): String =
                "${ConfigLoader.login}_messages${File.separator}${message.receiveDate}.${message.subject}_${message.head}"

        fun buildBody(message: PersonalMessage): String =
                StringJoiner("<br>")
                        .add(message.subject)
                        .add(message.head)
                        .add(message.htmlBody)
                        .toString()
    }
}

enum class Status {
    VIEWED,
    NOT_VIEWED
}