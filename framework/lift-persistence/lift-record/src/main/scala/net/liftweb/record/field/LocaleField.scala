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
package record {
package field {

import _root_.scala.xml._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.http.{S, SHtml}
import _root_.java.util.{Locale}
import S._
import Helpers._

object LocaleField {
  lazy val localeList = Locale
    .getAvailableLocales.toList
    .sort(_.getDisplayName < _.getDisplayName)
    .map(lo => (lo.toString, lo.getDisplayName))
}

class LocaleField[OwnerType <: Record[OwnerType]](rec: OwnerType) extends StringField(rec, 16) {

  override def defaultValue = Locale.getDefault.toString

  def isAsLocale: Locale = Locale.getAvailableLocales.filter(_.toString == value).toList match {
    case Nil => Locale.getDefault
    case x :: xs => x
  }

  /** Label for the selection item representing Empty, show when this field is optional. Defaults to the empty string. */
  def emptyOptionLabel: String = ""

  /** Build a list of string pairs for a select list. */
  def buildDisplayList: List[(String, String)] =
    if (optional_?) ("", emptyOptionLabel)::LocaleField.localeList else LocaleField.localeList

  private def elem = SHtml.select(buildDisplayList, Full(valueBox.map(_.toString) openOr ""), set) % ("tabindex" -> tabIndex.toString)

  override def toForm = {
    var el = elem

    uniqueFieldId match {
      case Full(id) =>
        <div id={id+"_holder"}><div><label for={id+"_field"}>{displayName}</label></div>{el % ("id" -> (id+"_field"))}<lift:msg id={id}/></div>
      case _ => <div>{el}</div>
    }
  }

  override def asXHtml: NodeSeq = {
    var el = elem

    uniqueFieldId match {
      case Full(id) =>  el % ("id" -> (id+"_field"))
      case _ => el
    }
  }
}

import _root_.java.sql.{ResultSet, Types}
import _root_.net.liftweb.mapper.{DriverType}

class DBLocaleField[OwnerType <: Record[OwnerType]](rec: OwnerType) extends LocaleField(rec)
  with JDBCFieldFlavor[String] {

  def targetSQLType = Types.VARCHAR

  /**
   * Given the driver type, return the string required to create the column in the database
   */
  def fieldCreatorString(dbType: DriverType, colName: String): String = colName+" VARCHAR("+16+")"

  def jdbcFriendly(field : String) : String = value

}

}
}
}
