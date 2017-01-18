package com.github.chauhraj.sse

import java.io.FileInputStream
import java.nio.file.Paths
import java.time.{Instant, LocalDateTime}
import java.util.TimeZone
import java.util.zip.GZIPInputStream

import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}

import scala.annotation.tailrec
import scala.io.Source

/**
  * Created by chauhraj on 1/13/17.
  */
class FillActor() extends ActorPublisher[Fill] with ActorMessageHandler[Fill] {

  val maximumBufferSize: Int = 10

  def toInputstream = Source.fromInputStream( new GZIPInputStream(new FileInputStream(Paths.get(sys.props("user.dir"), "fills.gz").toFile)) )

  def toFill(source: Iterator[String]): Runnable = () ⇒ {
      source.next.split(" ") match {
        case Array(_, ts, symbol, price, quantity, side) ⇒
          val fill = Fill(symbol,
            side,
            quantity.toInt,
            price.toDouble,
            LocalDateTime.ofInstant(Instant.ofEpochSecond(ts.toLong), TimeZone.getDefault().toZoneId()))
          self ! fill
        case _ ⇒ ???
      }
    }

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    import scala.concurrent.duration._

    import scala.concurrent.ExecutionContext.Implicits.global
    context.system.scheduler.schedule(1 second, 1 second, toFill(toInputstream.getLines()))
  }

}
