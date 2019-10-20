package examples

import java.time.LocalDateTime

import com.typesafe.scalalogging.StrictLogging
import fuckingkinesis.FuckingKinesisWriter
import monix.execution.Scheduler
import monix.execution.schedulers.SchedulerService
import software.amazon.awssdk.regions.Region

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Helper that writes a message with the current time every 3 seconds.
  */
class TestWriter(streamName: String) extends StrictLogging {
  implicit private val schedulerService: SchedulerService = Scheduler.io("test-writer")

  val writer = new FuckingKinesisWriter(Region.EU_CENTRAL_1)

  schedulerService.scheduleAtFixedRate(1.second, 3.seconds) {
    writer.write(streamName, s"Sending message at ${LocalDateTime.now().toString}.")
    ()
  }

  def shutdown()(implicit executionContext: ExecutionContext): Future[Boolean] = {
    logger.info("Shutting down TestWriter...")
    schedulerService.shutdown()
    writer.shutdown()
    schedulerService
      .awaitTermination(5.seconds, monix.execution.Scheduler.global)
      .andThen {
        case Failure(exception) => logger.error("Error while shutting down TestWriter!", exception)
        case Success(_)         => logger.info("TestWriter shut down successfully.")
      }(executionContext)
  }

}
