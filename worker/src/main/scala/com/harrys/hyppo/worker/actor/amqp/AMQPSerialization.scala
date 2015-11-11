package com.harrys.hyppo.worker.actor.amqp

import java.io._

import org.apache.commons.io.IOUtils

import scala.reflect._

/**
 * Created by jpetty on 9/16/15.
 */
final class AMQPSerialization {

  def serialize(o: AnyRef) : Array[Byte] = {
    val bytes  = new ByteArrayOutputStream(1024)
    val stream = new ObjectOutputStream(bytes)
    try {
      stream.writeObject(o)
    } finally {
      IOUtils.closeQuietly(stream)
    }
    bytes.toByteArray
  }

  def deserialize[T : ClassTag](bytes: Array[Byte]) : T = {
    val rtClass = classTag[T].runtimeClass.asInstanceOf[Class[T]]
    deserialize(bytes, rtClass)
  }

  def deserialize[T](bytes: Array[Byte], klass: Class[T]) : T = {
    val stream = new LookaheadObjectInputStream(klass, new ByteArrayInputStream(bytes))
    try {
      stream.readObject().asInstanceOf[T]
    } finally {
      IOUtils.closeQuietly(stream)
    }
  }

  final class LookaheadObjectInputStream(onlyAllow: Class[_], inputStream: InputStream) extends ObjectInputStream(inputStream) {
    private var firstCall: Boolean = true

    @throws[ClassNotFoundException]("if no associated class can be found")
    @throws[UnsupportedOperationException]("if the identified class is not valid for the declared serialization classes")
    override protected def resolveClass(objectStreamClass: ObjectStreamClass) : Class[_] = {
      if (firstCall){
        firstCall = false
        val resolved = super.resolveClass(objectStreamClass)
        if (onlyAllow.isAssignableFrom(resolved)) {
          resolved
        } else {
          throw new UnsupportedOperationException(s"Illegal serialization of class: ${ resolved.getName }. Supported objects must be instances of ${ onlyAllow.getName }")
        }
      } else {
        super.resolveClass(objectStreamClass)
      }
    }
  }
}
