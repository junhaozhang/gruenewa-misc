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

object CircuitBreaker {
  
  sealed trait State { 
    val threshold: Int
    val timeout: Long
  }
  
  case class Closed(val threshold: Int, val timeout: Long, failureCount: Int) extends State
  case class Open(val threshold: Int, val timeout: Long, tripTime: Long) extends State

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

// val cb = new gruenewa.circuitbreaker.CircuitBreaker(2, 1000, System.currentTimeMillis _)

class CircuitBreaker(val threshold: Int, val timeout: Long, val systime: () => Long) {
  
  import CircuitBreaker._

  @volatile
  var state: State = Closed(threshold, timeout, 0)

  def apply[F](f: => F): F = {
    if(!isClosed(systime(), this.state)) 
      throw CircuitBreakerException

    try {
      val o = f
      this.state = onSuccess(systime(), this.state)
      o
    } catch {
      case e => this.state = onFailure(systime(), this.state); throw e
    }
  }
}


object CircuitBreakerException extends Exception


class InvocationHandler(val threshold: Int, val timeout: Long, val systime: () => Long) extends java.lang.reflect.InvocationHandler {

  import java.lang.reflect.{Method, InvocationTargetException}

  val cb = new CircuitBreaker(threshold, timeout, systime)

  def invoke(proxy: AnyRef, m: Method, args: Array[AnyRef]): AnyRef =
    cb.apply(m.invoke(args))

}

import org.aopalliance.intercept.{MethodInterceptor, MethodInvocation}

class CircuitBreakerInterceptor(val threshold: Int, val timeout: Long, val systime: () => Long) extends MethodInterceptor {

  val cb = new CircuitBreaker(threshold, timeout, systime)

  def invoke(i: MethodInvocation): AnyRef = cb.apply(i.proceed()) 

}
