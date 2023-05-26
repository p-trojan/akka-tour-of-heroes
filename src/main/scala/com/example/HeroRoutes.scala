package com.example

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.Future
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{cors, corsRejectionHandler}
import com.example.HeroRegistry._

class HeroRoutes(heroRegistry: ActorRef[HeroRegistry.Command])(implicit val system: ActorSystem[_]) {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._

  //  private implicit val timeout: Timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))
  private implicit val timeout: Timeout = Timeout.create(java.time.Duration.ofSeconds(5))

  def getHeroes(): Future[Heroes] =
    heroRegistry.ask(GetHeroes)

  def getHero(id: Int): Future[GetHeroResponse] =
    heroRegistry.ask(GetHero(id, _))

  def createHero(hero: Hero): Future[HeroActionPerformed] =
    heroRegistry.ask(CreateHero(hero, _))

  def updateHero(hero: Hero): Future[HeroActionPerformed] =
    heroRegistry.ask(UpdateHero(hero, _))

  def deleteHero(id: Int): Future[HeroActionPerformed] =
    heroRegistry.ask(DeleteHero(id, _))

  val heroRoutes: Route = handleRejections(corsRejectionHandler) {
    cors() {
      pathPrefix("api") {
        concat(
          path("heroes") {
            concat(
              get {
                complete(getHeroes())
              },
              post {
                entity(as[Hero]) { hero =>
                  onSuccess(createHero(hero)) { performed =>
                    complete((StatusCodes.Created, performed))
                  }
                }
              },
              put {
                entity(as[Hero]) { hero =>
                  onSuccess(updateHero(hero)) { performed =>
                    complete((StatusCodes.OK, performed))
                  }
                }
              }
            )
          },
          path("heroes" / IntNumber) { id =>
            concat(
              get {
                rejectEmptyResponse {
                  onSuccess(getHero(id)) { response =>
                    complete(response.maybeHero)
                  }
                }
              },
              delete {
                onSuccess(deleteHero(id)) { performed =>
                  complete((StatusCodes.OK, performed))
                }
              }
            )
          })
        }

      }
    }
}
