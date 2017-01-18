package com.github.chauhraj

import java.time.{Instant, LocalDateTime, ZoneId}
import java.util.TimeZone

import spray.json.{JsNumber, JsValue, JsonFormat, _}

/**
  * Created by chauhraj on 11/18/16.
  */
package object sse {
  implicit object RichLocalDateTime extends JsonFormat[LocalDateTime] {
    def write(x: LocalDateTime) = JsNumber(x.atZone(ZoneId.systemDefault()).toEpochSecond)
    def read(value: JsValue) = value match {
      case JsNumber(ts) => LocalDateTime.ofInstant(Instant.ofEpochSecond(ts.toLong), TimeZone.getDefault().toZoneId())
      case x => deserializationError("Expected Long as JsNumber, but got " + x)
    }
  }

}
