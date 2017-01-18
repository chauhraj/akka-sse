package com.github.chauhraj.sse

import de.heikoseeberger.akkasse.ServerSentEvent
import spray.json.{JsonFormat, JsonWriter, RootJsonFormat}

import scala.reflect._

/**
  * Created by chauhraj on 11/16/16.
  */
object ServerSentEventMarshaller {
  def apply[T: ClassTag](obj: T)(implicit jsoniser: JsonWriter[T]): ServerSentEvent = {
    val data = jsoniser.write(obj).compactPrint
    println("Sending data:" + data)
    ServerSentEvent(data = data, `type` = classTag[T].runtimeClass.getSimpleName)
  }
}
