package ru.jesuschrist.client

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class PersonalMessagesSerializer(val serializeFileName: String) {
    fun serializePersonalMessages(personalMessages: Map<Int, PersonalMessage>) {
        val fos = FileOutputStream(serializeFileName)
        val oos = ObjectOutputStream(fos)
        oos.writeObject(personalMessages)
        oos.flush()
        oos.close()
    }

    @Suppress("UNCHECKED_CAST")
    fun deserializePersonalMessages(): HashMap<Int, PersonalMessage> {
        val fis = FileInputStream(serializeFileName)
        val oin = ObjectInputStream(fis)
        return oin.readObject() as HashMap<Int, PersonalMessage>
    }
}