package com.github.chauhraj.sse

import java.time.LocalDateTime

import akka.actor.Props
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

import scala.reflect.runtime.universe._

/**
  * Created by chauhraj on 11/5/16.
  */
object FillProvider {
  def apply[T:TypeTag] : Props = {
    typeOf[T] match {
      case t if t =:= typeOf[Fill] ⇒ Props(classOf[FillActor])
      case t if t =:= typeOf[Employee] ⇒ Props(classOf[EmployeeActor])
    }
  }
}

object Fill extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val jsonizedFill = jsonFormat5(Fill.apply)
}

object Employee extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val jsoniser = jsonFormat6(Employee.apply)
}

case class Employee(name: String, position: String, office: String, age: String, startDate: String, salary: String)

case class Fill(symbol: String, side: String, quantity: Int, price: Double, ts: LocalDateTime)