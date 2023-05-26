package com.example

import akka.NotUsed
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.alpakka.mongodb.DocumentUpdate
import akka.stream.alpakka.mongodb.scaladsl.{MongoSink, MongoSource}
import akka.stream.scaladsl.{Sink, Source}
import com.example.HeroRegistry.Command
import com.mongodb.client.model.{Filters, Updates}
import com.mongodb.reactivestreams.client.{MongoClients, MongoCollection}
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._

import scala.annotation.nowarn
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.collection.JavaConverters._

object HeroPersistence {
  sealed trait Operation
  final case class GetAllHeroes() extends Operation

  import akka.actor.ActorSystem

  implicit val system = ActorSystem()

  val codecRegistry =
    fromRegistries(fromProviders(classOf[Hero]), DEFAULT_CODEC_REGISTRY): @nowarn(
      "msg=match may not be exhaustive")

  private val client = MongoClients.create(s"mongodb://localhost:27017")
  private val database = client.getDatabase("MongoSinkSpec").withCodecRegistry(codecRegistry)
  private val heroesCollection: MongoCollection[Hero] =
    database.getCollection("heroesSink", classOf[Hero]).withCodecRegistry(codecRegistry)

  def getAllHeroes(): Heroes = {
    val source: Source[Hero, NotUsed] =
      MongoSource(heroesCollection.find(classOf[Hero]))
    val rows: Future[Seq[Hero]] = source.runWith(Sink.seq)
    val result = Await.result(rows, 5 seconds)
    Heroes(result)
  }

  def getOneHero(id: Int): Option[Hero] = {
    val source: Source[Hero, NotUsed] =
      MongoSource(heroesCollection.find(classOf[Hero]))
    val rows: Future[Seq[Hero]] = source.runWith(Sink.seq)
    val result = Await.result(rows, 5 seconds).find(_.id == id)
    result
  }
  
  def insertHeroes(heroList: List[Hero]): Unit = {
    val source = Source(heroList).map(it => Hero.apply(it.id, it.name))
    source.grouped(2).runWith(MongoSink.insertMany[Hero](heroesCollection))
  }

  private def insertHero(hero: Hero): Unit = {
    val source = Source.single(hero)
    source.runWith(MongoSink.insertOne(heroesCollection))
  }

  def updateHero(hero: Hero): Unit = {
    val source = Source.single(hero).map(it =>
      DocumentUpdate(filter = Filters.eq("id", it.id), update = Updates.set("name", it.name)))
    source.runWith(MongoSink.updateOne(heroesCollection))
  }

  def deleteHero(heroId: Int): Unit = {
    val source = Source.single(heroId).map(it =>
      Filters.eq("id", heroId))
    source.runWith(MongoSink.deleteOne(heroesCollection))
  }
}
