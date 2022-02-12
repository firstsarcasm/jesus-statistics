package ru.jesuschrist.utils

import com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException
import java.util.*
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import ru.jesuschrist.client.PersonalMessage
import ru.jesuschrist.client.PersonalMessage.Companion.buildBody
import ru.jesuschrist.client.PersonalMessage.Companion.buildSubject
import ru.jesuschrist.config.ConfigLoader

private class EmailSender {

    private val to = ConfigLoader.to
    private val mailServerHost = "smtp.gmail.com"
    private val properties: Properties = System.getProperties()

    init {
        properties.setProperty("mail.smtp.host", mailServerHost)
        properties.setProperty("mail.smtp.socketFactory.port", "465");
        properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.smtp.auth", "true")
    }

    private fun send(personalMessage: PersonalMessage) {
        val session = Session.getDefaultInstance(properties, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(
                        ConfigLoader.senderLogin, ConfigLoader.senderPassword)
            }
        })

        try {
            val message = MimeMessage(session)
            message.addRecipient(Message.RecipientType.TO, InternetAddress(to))

            val mailSubject = buildSubject(personalMessage)
            message.setContent(buildBody(personalMessage), "text/html; charset=utf-8")
            message.subject = mailSubject

            Transport.send(message)
            println("Сообщение ${mailSubject} отправлено на адрес ${ConfigLoader.to}\n")
        } catch (mex: MessagingException) {
            mex.printStackTrace()
        }
    }

    fun sendMessage(personalMessage: PersonalMessage) {
        send(personalMessage)
    }
}

private fun sendMessage(pm: PersonalMessage) = EmailSender().sendMessage(pm)
internal fun Set<PersonalMessage>.sendToEmail() {
    if (ConfigLoader.mailEnabled.not()) return
    if (this.size > ConfigLoader.maxMessageToSend) return

    this.ifNotEmpty {
        println("\nСообщения отправленные на почту:")
    }.run {
        forEach(::sendMessage)
    }
}