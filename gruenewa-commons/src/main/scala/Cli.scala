/*
 * Copyright (c) 2011-2012 by Alexander Grünewald
 * 
 * This file is part of gruenewa-commons, a collection of generally
 * useful utility functions.
 *
 * gruenewa-commons is free software: you can redistribute it and/or modify
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

package object cli {

  def parseOption(name: String, args: Array[String]): Option[String] = {
    for (i <- 0 until args.length)
      if (args(i).equalsIgnoreCase(name) && i < args.length - 1 && args(i + 1) != null)
        return Some(args(i + 1))
    return None
  }

  def hasOption(name: String, args: Array[String]): Boolean = {
    for (i <- 0 until args.length)
      if (args(i).equalsIgnoreCase(name))
        return true
    return false
  }
}
