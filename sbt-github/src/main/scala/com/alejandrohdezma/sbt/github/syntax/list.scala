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

package com.alejandrohdezma.sbt.github.syntax

object list {

  implicit class ListTraverseEither[A](private val list: List[A]) extends AnyVal {

    def traverse[B, C](f: A => Either[B, C]): Either[B, List[C]] =
      list.map(f).foldLeft[Either[B, List[C]]](Right(List())) { (acc, el) =>
        acc.flatMap(list => el.map(list :+ _))
      }

  }

}
