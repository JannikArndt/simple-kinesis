package examples

import java.time.Instant

import com.typesafe.scalalogging.StrictLogging
import fuckingkinesis._
import monix.execution.Scheduler.Implicits.global
import software.amazon.awssdk.regions.Region

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.StdIn

object ListenerExample extends App with StrictLogging {

  val streamName    = sys.env.getOrElse("STREAM_NAME", throw new RuntimeException("Expected env var $STREAM_NAME not found!"))
  val client        = new FuckingKinesisReader(Region.EU_CENTRAL_1)
  val shardIterator = Model.AT_TIMESTAMP(Instant.now.minusSeconds(180))

  client.startListening(streamName, shardIterator)(1 second, 1 second) { record =>
    logger.info(s"Received message at ${Instant.now().toString}: ${record.dataAsString}")
  }

  logger.info("Started listening. Press ENTER to stop!")
  StdIn.readLine()

  logger.info("Shutting down...")
  Await.result(client.shutdown(), 30 seconds)
  logger.info("Complete")

}
