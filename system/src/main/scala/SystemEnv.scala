package lila.system

import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.commons.conversions.scala._
import com.redis.RedisClient
import com.typesafe.config._

import ai._

trait SystemEnv {

  val config: Config

  def server = new Server(
    repo = gameRepo,
    ai = ai)

  lazy val ai: Ai = new StupidAi

  def gameRepo = new GameRepo(
    mongodb(config getString "mongo.collection.game"))

  def mongodb = MongoConnection(
    config getString "mongo.host",
    config getInt "mongo.port"
  )(config getString "mongo.dbName")

  def redis = new RedisClient(
    config getString "redis.host",
    config getInt "redis.port")
}

object SystemEnv extends EnvBuilder {

  def apply(overrides: String = "") = new SystemEnv {
    val config = makeConfig(overrides, "/home/thib/lila/lila.conf")
  }

  def apply(c: Config) = new SystemEnv {
    val config = c
  }
}

trait EnvBuilder {

  import java.io.File

  def makeConfig(sources: String*) = sources.foldLeft(ConfigFactory.defaultOverrides) {
    case (config, source) if source isEmpty ⇒ config
    case (config, source) if source contains '=' ⇒
      config.withFallback(ConfigFactory parseString source)
    case (config, source) ⇒
      config.withFallback(ConfigFactory parseFile (new File(source)))
  }
}
