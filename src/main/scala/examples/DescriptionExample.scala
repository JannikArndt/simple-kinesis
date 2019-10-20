package examples

import com.typesafe.scalalogging.StrictLogging
import simplekinesis.SimpleKinesisWrapper
import monix.execution.Scheduler.Implicits.global
import software.amazon.awssdk.regions.Region

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object DescriptionExample extends App with StrictLogging {

  val stream = sys.env.getOrElse("STREAM_NAME", throw new RuntimeException("Expected env var $STREAM_NAME not found!"))

  // you usually don't want to use the Wrapper directly
  val wrapper = SimpleKinesisWrapper(Region.EU_CENTRAL_1)

  val streamDescription = Await.result(wrapper.describeStream(stream), Duration.Inf)
  logger.info(s"Stream: $streamDescription\n\n")

  val consumers = Await.result(wrapper.listConsumers(streamDescription.streamARN()), Duration.Inf)
  logger.info(s"Consumers: $consumers\n\n")

  logger.info("Shutting down...")
  wrapper.shutdown()
  logger.info("Complete")

}
