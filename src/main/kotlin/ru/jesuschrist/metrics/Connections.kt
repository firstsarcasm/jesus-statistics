package ru.jesuschrist.metrics

import java.util.concurrent.atomic.AtomicInteger

class Connections {
    companion object {
        val connections = AtomicInteger()
    }
}