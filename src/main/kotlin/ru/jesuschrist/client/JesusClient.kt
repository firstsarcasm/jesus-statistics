package ru.jesuschrist.client

import ru.jesuschrist.config.ConfigLoader
import ru.jesuschrist.utils.*
import java.io.*
import java.text.SimpleDateFormat

class JesusClient {
    private val connectionFactory = ConnectionFactory()
    private val newDateFormat = SimpleDateFormat("yy-MM-dd HH-mm")

    private val SERIALIZE_FILE_NAME = "${ConfigLoader.login}-personal-messages.out"
    private val serializer = PersonalMessagesSerializer(SERIALIZE_FILE_NAME)

    private var blackListUserIsOnline = false
    private var hiddenUsers = 0
    private var usersOnlineList = ArrayList<String>()
    private var personalMessages = HashMap<Int, PersonalMessage>()

    init {
        if (File(SERIALIZE_FILE_NAME).isFile) {
            personalMessages = serializer.deserializePersonalMessages()
        }
    }

    fun serialize() {
        serializer.serializePersonalMessages(personalMessages)
    }

    private fun clearWhoIsOnlinePageContext() {
        hiddenUsers = 0
        usersOnlineList.clear()
    }

    private fun Set<PersonalMessage>.printLastPersonalMessages(): Set<PersonalMessage> = this
            .apply {
                onlyViewed()
                        .printText("Новые просмотренные сообщения:")
                        .logIt()
                        .printAndWrite()
                        .sendToEmail()

                onlyNotViewed()
                        .printText("\nНепросмотренные сообщения:")
                        .print()
            }

    private fun isViewedOrProcessed(messageId: String): Pair<Boolean, Boolean> {
        var isViewedBefore = false
        val isAlreadyProcessed = personalMessages.filter {
            val pm = it.value
            val storedId = pm.id.toString()
            messageId.startsWith(storedId)
        }.onEach {
            val pm = it.value
            isViewedBefore = pm.isViewed()
        }.isNotEmpty()

        return isViewedBefore to isAlreadyProcessed
    }

    fun checkContext(): Boolean {
        getAndParseWhoIsOnlinePage()
        val result = (hiddenUsers > ConfigLoader.allowedHiddenUsers || blackListUserIsOnline).not()
        clearWhoIsOnlinePageContext()
        return result
    }

    private fun getAndParseWhoIsOnlinePage() {
        val whoIsOnlinePage = connectionFactory.createConnection("http://jesuschrist.ru/forum/online.php?Cat=")
        val onlineUsers = Manager.UsersManager(whoIsOnlinePage)
                .getOnlineUsers()

        usersOnlineList = onlineUsers.list
        hiddenUsers = onlineUsers.hiddenUsers
        blackListUserIsOnline = usersOnlineList.any { it in ConfigLoader.blackList }

        println("\nСкрытых пользователей: ${hiddenUsers}")
        println("Есть пользователи из черного списка: $blackListUserIsOnline\n")

        if (ConfigLoader.showUsersList) println("Список пользователей онлайн:\n${onlineUsers}")
    }

    fun getAndParsePrivateMessagesPage() {
        val allPrivateMessagesPage = connectionFactory.createConnection("https://jesuschrist.ru/forum/login.php?Cat=", true)

        Manager.getMessagesTableFromPage(allPrivateMessagesPage).parallelStream()
                .map(Manager::MessageManager)
                .filter { messagesManager ->
                    val (isViewedBefore, isAlreadyProcessed) = isViewedOrProcessed(messagesManager.messageId)
                    val receivedDateIsApplied = ConfigLoader.searchAfterDate.isNullOrEmpty()
                            || messagesManager.receiveDateIsAfter(ConfigLoader.searchAfterDate)

                    receivedDateIsApplied && (!isAlreadyProcessed || !isViewedBefore)
                }
                .map { messagesManager ->
                    val messageStatus = if (messagesManager.isNewMessage) Status.NOT_VIEWED else Status.VIEWED
                    val (textBody, htmlBody) = getPrivateMessageBody(messagesManager)
                    val head = getDirectionFromHead(messagesManager.head)

                    messagesManager.run {
                        PersonalMessage(messageId.toInt(), messageStatus, messageHeader, senderName, messageUrl,
                                newDateFormat.format(receiveDate), textBody.orEmpty(), htmlBody.orEmpty(), head)
                    }
                }
                .toSet()
                .printLastPersonalMessages()
                .associateTo(personalMessages, { it.id to it })

        serialize()

        logout()
    }

    private fun getPrivateMessageBody(messageManager: Manager.MessageManager): Pair<String?, String?> {
        if (messageManager.isNewMessage) {
            return null to null
        }
        val privateMessageBodyPage = connectionFactory.createConnection(messageManager.messageUrl, true)
        return messageManager.getMessageTextAndHtml(privateMessageBodyPage)
    }

    fun logout() = connectionFactory.createConnection(
            "https://jesuschrist.ru/forum/logout.php?Cat=",
            true,
            ConfigLoader.logoutTimeout
    )
}

private fun getDirectionFromHead(head: String?): String? {
    if (head == null) return null

    return when {
        head.contains("От") -> head.dropAndReplace(25)
        head.contains("Кому") -> head.dropAndReplace(21)
        else -> head
    }
}

private fun String.dropAndReplace(n: Int): String {
    return this.dropLast(n).replace(":", "_")
}