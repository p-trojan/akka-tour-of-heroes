package com.example

import com.example.HeroRegistry.HeroActionPerformed
import com.example.UserRegistry.UserActionPerformed
import spray.json._

//#json-formats
import spray.json.DefaultJsonProtocol

object JsonFormats extends DefaultJsonProtocol {
  // import the default encoders for primitive types (Int, String, Lists etc)
//  import DefaultJsonProtocol._

  case class HeroesJsonFormat[T](res: List[T])

  implicit object HeroesJsonFormat extends RootJsonFormat[Heroes] {
    def write(heroSequence: Heroes): JsValue = heroSequence.heroes.sortBy(_.id).toJson
    def read(value: JsValue) = Heroes(value.convertTo[List[Hero]])
  }

  implicit val userJsonFormat: RootJsonFormat[User] = jsonFormat3(User.apply)
  implicit val usersJsonFormat: RootJsonFormat[Users] = jsonFormat1(Users.apply)

  implicit val heroJsonFormat: RootJsonFormat[Hero] = jsonFormat2(Hero.apply)
//  implicit val heroesJsonFormat: RootJsonFormat[Heroes] = jsonFormat1(Heroes.apply)
  implicit def heroesJsonFormat[T: JsonFormat] = jsonFormat1(HeroesJsonFormat.apply[T])

  implicit val userActionPerformedJsonFormat: RootJsonFormat[UserActionPerformed] = jsonFormat1(UserActionPerformed.apply)
  implicit val heroActionPerformedJsonFormat: RootJsonFormat[HeroActionPerformed] = jsonFormat1(HeroActionPerformed.apply)
}
//#json-formats