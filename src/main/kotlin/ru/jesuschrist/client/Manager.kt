package ru.jesuschrist.client

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.text.SimpleDateFormat

class Manager {

    class UsersManager(whoIsOnlinePage: Document) {
        private val usersCounterIdentifier = "td[colspan=5]"
        private val onlineUsersPageCounter = whoIsOnlinePage.select(usersCounterIdentifier).first().text()

        private val pageCountCurrentUsersOnline = onlineUsersPageCounter.filter(Char::isDigit).toInt()

        private val onlineUsersElements = whoIsOnlinePage
                .select("table[cellpadding=3]")
                .select("td[valign=middle]")
                .select("a")

        fun getOnlineUsers(): OnlineUsers = OnlineUsers(onlineUsersElements)

        inner class OnlineUsers(elements: Elements) {
            private val string: String
            val list = ArrayList<String>()

            private var onlineUsersRealCounter: Int
            var hiddenUsers: Int
            private set

            init {
                val builder = StringBuilder()
                elements.forEach {
                    list.add(it.getUserName())
                    builder.append("${it.getUserName()}: ${it.getUserAccountUrl()}\n")
                }
                onlineUsersRealCounter  = list.size
                hiddenUsers = pageCountCurrentUsersOnline - onlineUsersRealCounter
                string = builder.toString()
            }

            private fun Element.getUserName() = this.text()
            private fun Element.getUserAccountUrl() = this.attr("href")

            override fun toString(): String = string
        }
    }

    class MessageManager(value: Element) {
        val messageId: String
        val isNewMessage: Boolean

        init {
            val tempMessageId = value.getElementsByAttribute("type").first().attr("value")
            if (tempMessageId.contains("NEW")) {
                messageId = tempMessageId.dropLast(4)
                isNewMessage = true
            } else {
                messageId = tempMessageId
                isNewMessage = false
            }
        }

        private val forbiddenSymbols =
                listOf('*', '.', '"', '/', '\\', '[', ']', ':', ';', '|', '=',
                        ',', '?', '«', '<', '>', '+', '«', '\'', ',', ',', '?', ',', '~',
                        '@', '#', '$', '%', '.', '\'', '^', '\'', '-', '_', '(', ')', '{', '}', '\'', '`')

        private fun String.replaceForbiddenSymbols(): String = this.filter { (it in forbiddenSymbols).not() }

        private val baseDateFormat = SimpleDateFormat("dd-MM-yy HH-mm")

        private val allHrefs = value.getElementsByAttribute("href")

        private val headerLine = allHrefs.first()

        private val receiveDateString
                = value.getElementsByTag("td")[3].text()
                .replace("/", "-")
                .replace(":", "-")
        val receiveDate = baseDateFormat.parse(receiveDateString)

        val messageHeader = headerLine.text().replaceForbiddenSymbols()
        val messageUrl = headerLine.attr("href").toString()
        val senderName = allHrefs[1].text()

        fun receiveDateIsAfter(date: String) = receiveDate.after(baseDateFormat.parse(date))

        var head: String? = null
        fun getMessageTextAndHtml(messageUrl: Document): Pair<String?, String?> {

            val allDarkTables = messageUrl.getElementsByClass("darktable")
            val head = allDarkTables[1].text()
            this.head = head

            val messageText = messageUrl.getElementsByClass("lighttable").text()

            val metaString = "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">"
            val messageHtml = "$metaString\n${messageUrl.getElementsByClass("lighttable")}"

            return messageText to messageHtml
        }
    }

    companion object {
        @JvmStatic
        fun getMessagesTableFromPage(doc: Document) =
                doc.getElementsByClass("lighttable")
    }
}