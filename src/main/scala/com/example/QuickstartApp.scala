package com.example

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.example.JsonFormats.heroJsonFormat
import spray.json.DefaultJsonProtocol.listFormat
import spray.json._

import scala.language.postfixOps
import scala.util.{Failure, Success}


//#main-class
object QuickstartApp {
  //#start-http-server
  private def startHeroesHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    import system.executionContext
    val heroesBinding = Http().newServerAt("127.0.0.1", 8080).bindFlow(routes)
    heroesBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Heroes server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  private def loadHeroesData(path: String): List[Hero] = {
    val jsonContent = scala.io.Source.fromFile(path).mkString
    val jsonData = jsonContent.parseJson
    jsonData.convertTo[List[Hero]]
  }

  //#start-http-server
  def main(args: Array[String]): Unit = {

    //#server-bootstrapping

    val rootBehavior = Behaviors.setup[Nothing] { context =>

      val heroRegistryActor = context.spawn(HeroRegistry(), "HeroRegistryActor")
      context.watch(heroRegistryActor)
      val heroRoutes = new HeroRoutes(heroRegistryActor)(context.system)
      startHeroesHttpServer(heroRoutes.heroRoutes)(context.system)

      val heroes: Seq[Hero] = HeroPersistence.getAllHeroes().heroes
      if (heroes.isEmpty) initDatabase
      heroes.foreach(hero => heroRoutes.createHero(hero))

      Behaviors.empty
    }
    implicit val system = ActorSystem[Nothing](rootBehavior, "HelloAkkaHttpServer")
    //#server-bootstrapping
  }

  private def initDatabase(): Unit = {
    val inputFile = "src/main/resources/data.json"
    val heroes = loadHeroesData(inputFile).sortBy(_.id)
    HeroPersistence.insertHeroes(heroes)
  }
}
//#main-class
