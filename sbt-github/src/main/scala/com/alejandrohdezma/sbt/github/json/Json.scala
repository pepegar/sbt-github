/*
 * Copyright 2019-2020 Alejandro Hernández <https://github.com/alejandrohdezma>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alejandrohdezma.sbt.github.json

import scala.util.parsing.combinator.JavaTokenParsers

object Json extends JavaTokenParsers {

  type Result[A] = Either[Fail, A]

  /** Parse the provided string into a [[Json.Value]] */
  def parse(s: String): Result[Json.Value] =
    parseAll(`json-value`, s).map(Right(_)).getOrElse(Left(Fail.NotAValidJSON(s)))

  @SuppressWarnings(Array("all"))
  private def `json-value`: Parser[Json.Value] = {
    val stripQuotes   = (x: String) => x.substring(1, x.length - 1)
    val `json-line`   = stringLiteral ~ ":" ~ `json-value`
    val `json-field`  = `json-line` ^^ { case name ~ ":" ~ value => stripQuotes(name) -> value }
    val `json-object` = "{" ~> repsep(`json-field`, ",") <~ "}" ^^ (Map() ++ _) ^^ Json.Object
    val `json-array`  = "[" ~> repsep(`json-value`, ",") <~ "]" ^^ Json.Collection
    val `json-string` = stringLiteral ^^ stripQuotes ^^ Json.Text
    val `json-number` = floatingPointNumber ^^ (_.toDouble) ^^ Json.Number
    val `json-null`   = "null" ^^^ Json.Null
    val `json-true`   = "true" ^^^ Json.True
    val `json-false`  = "false" ^^^ Json.False

    `json-object` | `json-array` | `json-string` | `json-number` | `json-null` | `json-true` | `json-false`
  }

  /**
   * Represents a decoding failure.
   *
   * Left open so users can add more types of failures.
   */
  trait Fail {

    /** Returns this failure as a readable message */
    def readableMessage: String = this match {
      case Fail.NotAJSONObject(value) => s"is not a valid JSON object: $value"
      case Fail.NotAList(value)       => s"is not a valid JSON array: $value"
      case Fail.NotAString(value)     => s"is not a valid JSON string: $value"
      case Fail.NotANumber(value)     => s"is not a valid JSON number: $value"
      case Fail.NotABoolean(value)    => s"is not a valid JSON boolean: $value"
      case Fail.NotADateTime(value)   => s"is not a valid date time: $value"
      case Fail.Path(value, fail)     => s"$value => ${fail.readableMessage}"
      case Fail.NotAValidJSON(string) => s"$string is not a valid JSON"
      case Fail.Unknown(cause)        => s"An error occurred: ${cause.getMessage}"
      case Fail.URLNotFound(url)      => s"$url was not found"
      case Fail.NotFound              => "was not found"
    }

  }

  object Fail {

    final case class NotAJSONObject(value: Json.Value) extends Fail
    final case class NotAList(value: Json.Value)       extends Fail
    final case class NotAString(value: Json.Value)     extends Fail
    final case class NotANumber(value: Json.Value)     extends Fail
    final case class NotABoolean(value: Json.Value)    extends Fail
    final case class NotADateTime(value: Json.Value)   extends Fail
    final case class Path(value: String, fail: Fail)   extends Fail
    final case class NotAValidJSON(string: String)     extends Fail
    final case class Unknown(cause: Throwable)         extends Fail
    final case class URLNotFound(url: String)          extends Fail
    case object NotFound                               extends Fail

  }

  sealed trait Value

  final case class Object(fields: Map[String, Value]) extends Value
  final case class Collection(elements: List[Value])  extends Value
  final case class Text(value: String)                extends Value
  final case class Number(value: Double)              extends Value
  case object False                                   extends Value
  case object True                                    extends Value
  case object Null                                    extends Value

}
