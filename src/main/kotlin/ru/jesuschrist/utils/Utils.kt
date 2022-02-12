package ru.jesuschrist.utils

import kotlin.comparisons.compareBy
import ru.jesuschrist.client.PersonalMessage
import ru.jesuschrist.utils.Writer.Companion.writeMessageInLog
import ru.jesuschrist.utils.Writer.Companion.writeSeparatorInLog
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import java.util.stream.Collectors
import java.util.stream.Stream

internal fun <E> Set<E>.onlyViewed(): Set<E>
        = this.filter { (it as PersonalMessage).isViewed() }.toSet()

internal fun <E> Set<E>.onlyNotViewed(): Set<E>
        = this.filter { (it as PersonalMessage).isViewed().not() }.toSet()

internal fun <E> Set<E>.print()
        = this.forEach(::println)

internal fun <E> Set<E>.printAndWrite(): Set<E> {
    return this.onEach {
        println(it)
        Writer.writeMessage(it as PersonalMessage)
    }
}

var atomicString : AtomicReference<String> = AtomicReference()

internal fun Set<PersonalMessage>.logIt(): Set<PersonalMessage> {
    val log = this.ifNotEmptyWithResult("") {
        StringBuilder().let { sb ->
            this.sortedWith(compareBy(PersonalMessage::receiveDate))
                    .map(::writeMessageInLog)
                    .forEach { sb.appendln(it) }
            sb.appendln(writeSeparatorInLog())
        }.toString()
    }

    atomicString.accumulateAndGet(log) {
        n, m -> n + m
    }

    return this
}

fun Set<PersonalMessage>.printText(text: String): Set<PersonalMessage> {
    println(text)
    return this
}

fun <E> Set<E>.ifNotEmpty(function: () -> Unit): Set<E> {
    if (!this.isEmpty()) {
        function.invoke()
    }
    return this
}

inline fun <E> Set<E>.ifNotEmptyWithResult(defaultResult: String, function: () -> String): String {
    if (!this.isEmpty()) {
        return function.invoke()
    }
    return defaultResult
}

fun <K, V> HashMap<K, V>.putOrUpdate(key: K, value: V) {
    if(key in this) this[key] = value
    else this.put(key, value)
}

fun <T> Stream<T>.toSet(): Set<T> = collect(Collectors.toSet<T>())