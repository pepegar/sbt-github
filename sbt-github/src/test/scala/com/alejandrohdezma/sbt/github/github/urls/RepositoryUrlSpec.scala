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

package com.alejandrohdezma.sbt.github.github.urls

import sbt.util.Logger

import com.alejandrohdezma.sbt.github.http.Authentication
import com.alejandrohdezma.sbt.github.http.Authentication.Token
import com.alejandrohdezma.sbt.github.withServer
import org.http4s.dsl.io._
import org.specs2.mutable.Specification

class RepositoryUrlSpec extends Specification {

  "Repository" should {

    "provide implicit value based on others" >> withServer {
      case GET -> Root =>
        Ok("""{ "repository_url": "miau" }""")
    } { uri =>
      implicit val noOpLogger: Logger                 = Logger.Null
      implicit val githubEntryPoint: GithubEntryPoint = GithubEntryPoint(uri)
      implicit val auth: Authentication               = Token("1234")

      val repoUrl = implicitly[Repository]

      val expected = Repository("miau")

      repoUrl must be equalTo expected
    }

    "provide url for specific repository" >> withServer {
      case GET -> Root =>
        Ok("""{ "repository_url": "http://example.com/{owner}/{repo}" }""")
    } { uri =>
      implicit val noOpLogger: Logger                 = Logger.Null
      implicit val githubEntryPoint: GithubEntryPoint = GithubEntryPoint(uri)
      implicit val auth: Authentication               = Token("1234")

      val repoUrl = implicitly[Repository]

      repoUrl.get("owner", "repo") must be equalTo "http://example.com/owner/repo"
    }

  }

}
