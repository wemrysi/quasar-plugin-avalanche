/*
 * Copyright 2020 Precog Data
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

package quasar.plugin.avalanche

import scala._, Predef._
import scala.concurrent.duration._

import argonaut._, Argonaut._

import cats.implicits._

import org.specs2.mutable.Specification

import quasar.plugin.jdbc.Redacted

object ConnectionConfigSpec extends Specification {

  "JSON codec" >> {
    val fullConfig =
      ConnectionConfig(
        "example.com:12354", "db", Some(4), Some(30.seconds),
        Map("user" -> "alice", "password" -> "secret", "encryption" -> "off"))

    def roundtrips(cc: ConnectionConfig) =
      cc.asJson.as[ConnectionConfig].toEither must beRight(cc)

    "roundtrips full config" >> {
      roundtrips(fullConfig)
    }

    "roundtrips no properties" >> {
      roundtrips(fullConfig.copy(properties = Map.empty))
    }

    "maxConcurrency is optional" >> {
      roundtrips(fullConfig.copy(maxConcurrency = None))
    }

    "maxLifetimeSecs is optional" >> {
      roundtrips(fullConfig.copy(maxLifetime = None))
    }

    "null property omitted" >> {
      val json = """
        {
          "serverName": "example.com:12354",
          "databaseName": "db",
          "maxConcurrency": 4,
          "maxLifetimeSecs": 30,
          "properties": {
            "user": "bob",
            "encryption": null,
            "groupName": "managers"
          }
        }
      """

      val expected =
        fullConfig.copy(properties = Map("user" -> "bob", "groupName" -> "managers"))

      json.decodeOption[ConnectionConfig] must beSome(expected)
    }
  }

  "sanitization" >> {
    val config =
      ConnectionConfig("example.com:12354", "db", Some(4), Some(30.seconds), Map.empty)

    "redacts sensitive properties" >> {
      val ps =
        (ConnectionConfig.SensitiveProps - ConnectionConfig.RoleNameProp)
          .map(_ -> "secret")
          .toMap

      val expected =
        config.copy(properties = ps.as(Redacted))

      config.copy(properties = ps).sanitized must_=== expected
    }

    "does not redact role name without password" >> {
      val withRole = config.copy(properties = Map(ConnectionConfig.RoleNameProp -> "arole"))
      withRole.sanitized must_=== withRole
    }

    "redacts role password, if present" >> {
      val withRolePw =
        config.copy(properties = Map(ConnectionConfig.RoleNameProp -> "admins|s3kre7"))

      val expected =
        config.copy(properties = Map(ConnectionConfig.RoleNameProp -> s"admins|$Redacted"))

      withRolePw.sanitized must_=== expected
    }
  }

  "validation" >> {
    "rejects unsupported properties" >> {
      val c =
        ConnectionConfig(
          "example.com:12354", "db", None, None,
          Map(
            "user" -> "alice",
            "allowRootAccess" -> "true",
            "encryption" -> "on",
            "disableSecurity" -> "true"))

      c.validated.toEither.leftMap(_.toList) must beLeft(exactly(contain("allowRootAccess") and contain("disableSecurity")))
    }
  }
}
