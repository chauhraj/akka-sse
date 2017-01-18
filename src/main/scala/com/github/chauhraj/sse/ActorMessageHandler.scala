package com.github.chauhraj.sse

import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}

import scala.annotation.tailrec

/**
  * Created by chauhraj on 1/13/17.
  */
trait ActorMessageHandler[T] {

  this: ActorPublisher[T] ⇒

  var buffer = Vector.empty[T]

  val maximumBufferSize: Int

  def processMessageWhenFull(m: T) = {

  }

  def receive: Receive = {
    case m: T if buffer.size == maximumBufferSize ⇒
      processMessageWhenFull(m)
    case m: T ⇒
      if(buffer.isEmpty && totalDemand > 0) {
        onNext(m)
      } else {
        buffer :+= m
        deliverBuffer()
      }
    case Request(_) =>
      deliverBuffer()
    case Cancel =>
      context.stop(self)

  }

  @tailrec final def deliverBuffer(): Unit =
    if (totalDemand > 0) {
      /*
       * totalDemand is a Long and could be larger than
       * what buf.splitAt can accept
       */
      if (totalDemand <= Int.MaxValue) {
        val (use, keep) = buffer.splitAt(totalDemand.toInt)
        buffer = keep
        use foreach onNext
      } else {
        val (use, keep) = buffer.splitAt(Int.MaxValue)
        buffer = keep
        use foreach onNext
        deliverBuffer()
      }
    }

}
