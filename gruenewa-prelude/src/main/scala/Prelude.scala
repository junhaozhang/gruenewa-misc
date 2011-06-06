/*
 * Copyright (c) 2011 by Alexander Grünewald
 * 
 * This file is part of gruenewa-prelude, a collection of generally
 * useful utility functions.
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

package gruenewa

package object prelude {

  /**
   * Thrush combinator that allows to use
   * F# pipline syntax when in scope.
   */
  final class Thrush[A](a: A) {
    def |>[B](f: A => B): B = f(a)
  }

  implicit def |>[A](a: => A) = new Thrush(a)

  /**
   * Using statement that models an ARM block
   * to auto-close resources.
   */ 
  type Closable = { def close() }
  
  def using[T <: Closable, A](resource: T)(block: T => A): A = {
    try {
      block(resource)
    } finally {
      if (resource != null) {
        resource.close()
      }
    }
  }

  def wrapException[T](block: => T): Either[Throwable, T] =    
    try {
      Right(block)
    } catch {
      case e => Left(e)
    }

  /**
   * Very simple time measurement function.
   */ 
  def time[F](f: => F) = {
    val t0 = System.nanoTime
    val result = f
    val t1 = System.nanoTime
    printf("Elapsed: %.9f secs\n", 1e-9*(t1-t0))
    result
  }

}
