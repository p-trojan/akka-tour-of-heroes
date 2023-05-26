package com.example


import akka.NotUsed
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.alpakka.mongodb.DocumentUpdate
import akka.stream.alpakka.mongodb.scaladsl.{MongoSink, MongoSource}
import akka.stream.scaladsl.{Sink, Source}
import com.mongodb.client.model.{Filters, Updates}
import com.mongodb.reactivestreams.client.{MongoClients, MongoCollection}
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._

import scala.annotation.nowarn
import scala.collection.immutable
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

final case class Hero(id: Int, name: String)
final case class Heroes(heroes: immutable.Seq[Hero])

object HeroRegistry {
  sealed trait Command
  final case class GetHeroes(replyTo: ActorRef[Heroes]) extends Command
  final case class CreateHero(hero: Hero, replyTo: ActorRef[HeroActionPerformed]) extends Command
  final case class UpdateHero(hero: Hero, replyTo: ActorRef[HeroActionPerformed]) extends Command
  final case class GetHero(id: Int, replyTo: ActorRef[GetHeroResponse]) extends Command
  final case class DeleteHero(id: Int, replyTo: ActorRef[HeroActionPerformed]) extends Command

  final case class GetHeroResponse(maybeHero: Option[Hero])
  final case class HeroActionPerformed(description: String)

  def apply(): Behavior[Command] = registry(Set.empty)

  private def registry(heroes: Set[Hero]): Behavior[Command] = {
    Behaviors.receiveMessage {
      case GetHeroes(replyTo) =>
        replyTo ! Heroes(heroes.toSeq)
        Behaviors.same
      case CreateHero(hero, replyTo) =>
        replyTo ! HeroActionPerformed(s"Hero ${hero.name} created")
        registry(heroes + hero)
      case UpdateHero(hero, replyTo) =>
        replyTo ! HeroActionPerformed(s"Hero ${hero.name} updated")
        registry(heroes.filterNot(_.id == hero.id) + hero)
      case GetHero(id, replyTo) =>
        replyTo ! GetHeroResponse(heroes.find(_.id == id))
        Behaviors.same
      case DeleteHero(id, replyTo) =>
        replyTo ! HeroActionPerformed(s"Hero $id deleted.")
        registry(heroes.filterNot(_.id == id))
    }
  }
}
