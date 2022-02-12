package ru.jesuschrist.telegram

import org.telegram.telegrambots.generics.BotSession

class BotControl {
    lateinit var thread: Thread
    lateinit var botSession: BotSession

    fun stop() {
        botSession.stop()
        thread.interrupt()
    }
}