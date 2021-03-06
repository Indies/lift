/*
 * Copyright 2007-2010 WorldWide Conferencing, LLC
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

package net.liftweb {
package util {

import _root_.org.specs._
import _root_.org.specs.runner._
import common._
import json.JsonParser.parse
import json.Serialization.{read, write => swrite}

class JsonTypesSpecTest extends JUnit4(JsonTypesSpec)
object JsonTypesSpec extends Specification {
  implicit val formats = net.liftweb.json.DefaultFormats + new JsonBoxSerializer

  "Extract empty age" in {
    parse("""{"name":"joe"}""").extract[Person] mustEqual Person("joe", Empty, Empty)
  }

  "Extract boxed age" in {
    parse("""{"name":"joe", "age":12}""").extract[Person] mustEqual Person("joe", Full(12), Empty)
  }

  "Extract boxed mother" in {
    val json = """{"name":"joe", "age":12, "mother": {"name":"ann", "age":53}}"""
    val p = parse(json).extract[Person]
    p mustEqual Person("joe", Full(12), Full(Person("ann", Full(53), Empty)))
    (for { a1 <- p.age; m <-p.mother; a2 <- m.age } yield a1+a2) mustEqual Full(65)
  }

  "Render with age" in {
    swrite(Person("joe", Full(12), Empty)) mustEqual """{"name":"joe","age":12,"mother":null}"""
  }

  "Serialize failure" in {
    val exn1 = SomeException("e1")
    val exn2 = SomeException("e2")
    val p = Person("joe", Full(12), Failure("f", Full(exn1), Failure("f2", Full(exn2), Empty)))
    val ser = swrite(p)
    read[Person](ser) mustEqual p
  }

  "Serialize param failure" in {
    val exn = SomeException("e1")
    val p = Person("joe", Full(12), ParamFailure("f", Full(exn), Empty, "param value"))
    val ser = swrite(p)
    read[Person](ser) mustEqual p
  }
}

case class SomeException(msg: String) extends Exception

case class Person(name: String, age: Box[Int], mother: Box[Person])

}
}
