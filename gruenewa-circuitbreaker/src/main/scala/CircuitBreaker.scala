/*
 * Copyright (c) 2011 by Alexander Grünewald
 * 
 * This file is part of gruenewa-circuitbreaker, an implementation of the
 * circuit breaker pattern described in the book "Release It".
 *
 * gruenewa-grid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package gruenewa.circuitbreaker
  
sealed trait State { 
  val threshold: Int
  val timeout: Long
}
case class Closed(val threshold: Int, val timeout: Long, failureCount: Int) extends State
case class Open(val threshold: Int, val timeout: Long, tripTime: Long) extends State

object CircuitBreaker {

  def isClosed(time: Long, state: State) =
    state match {
      case Open(_, timeout, tripTime) => time > (tripTime + timeout)
      case _ => true
    }

  def onSuccess(time: Long, state: State) = 
    Closed(state.threshold, state.timeout, 0)

  def onFailure(time: Long, state: State) = 
    state match {
      case Closed(threshold, timeout, failureCount) if (threshold > failureCount + 1)
        => Closed(threshold, timeout, failureCount + 1)
      case _ => Open(state.threshold, state.timeout, time)
    }
}    


class CircuitBreakerControl(val threshold: Int, val timeout: Long, val systime: () => Long) {

  @volatile
  var state: State = Closed(threshold, timeout, 0)

  def isClosed() = CircuitBreaker.isClosed(systime(), state)

  def onSuccess() {
    state = CircuitBreaker.onSuccess(systime(), state)
  }
  
  def onFailure() {
    state = CircuitBreaker.onFailure(systime(), state)
  }
}


object CircuitBreakerException extends Exception


class InvocationHandler(val threshold: Int, val timeout: Long, val systime: () => Long) extends java.lang.reflect.InvocationHandler {

  import java.lang.reflect.{Method, InvocationTargetException}

  val control = new CircuitBreakerControl(threshold, timeout, systime)

  def invoke(proxy: AnyRef, m: Method, args: Array[AnyRef]): AnyRef = {
    if(!control.isClosed()) 
      throw CircuitBreakerException
    
    wrapException(m.invoke(args)) match {
      case Right(o) => control.onSuccess(); o
      case Left(e) => control.onFailure(); throw e
    }
  }

  def wrapException[T](block: => T): Either[Throwable, T] =
    try {
      Right(block)
    } catch {
      case e: InvocationTargetException => Left(e.getTargetException())
      case e => Left(e)
    }
}

import org.aopalliance.intercept.{MethodInterceptor, MethodInvocation}

class CircuitBreakerInterceptor(val threshold: Int, val timeout: Long, val systime: () => Long) extends MethodInterceptor {

  val control = new CircuitBreakerControl(threshold, timeout, systime)

  def invoke(i: MethodInvocation): AnyRef = 
    if(!control.isClosed()) 
      throw CircuitBreakerException
    else
      wrapException(i.proceed()) match {
        case Right(o) => control.onSuccess(); o
        case Left(e) => control.onFailure(); throw e
      }

  def wrapException[T](block: => T): Either[Throwable, T] =
    try {
      Right(block)
    } catch {
      case e => Left(e)
    }
}
