package examples

import com.typesafe.scalalogging.StrictLogging
import simplekinesis._
import monix.execution.Scheduler.Implicits.global
import software.amazon.awssdk.regions.Region

import scala.io.StdIn

object ConsoleExample extends App with StrictLogging {

  val streamName = sys.env.getOrElse("STREAM_NAME", throw new RuntimeException("Expected env var $STREAM_NAME not found!"))
  val writer     = new SimpleKinesisWriter(Region.EU_CENTRAL_1)

  var running = true
  println("Start typing!")
  while (running) {
    val input = StdIn.readLine()
    println(s"> $input")
    input match {
      case ""   => running = false
      case text => writer.write(streamName, text)
    }
  }

  logger.info("Shutting down...")
  writer.shutdown()
  logger.info("Complete")

}
