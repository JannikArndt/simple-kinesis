import fuckingkinesis._
import monix.execution.Scheduler.Implicits.global
import org.scalatest.{FlatSpecLike, Matchers}
import software.amazon.awssdk.regions.Region

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}

class FuckingKinesisReaderSpec extends FlatSpecLike with Matchers {

  val streamName: String = sys.env.getOrElse("STREAM_NAME", throw new RuntimeException("Expected env var $STREAM_NAME not found!"))

  "FuckingKinesisReader" should "read from Kinesis" in {
    // given
    val reader = new FuckingKinesisReader(Region.EU_CENTRAL_1)
    val writer = new FuckingKinesisWriter(Region.EU_CENTRAL_1)

    val promise = Promise[String]()
    val future  = promise.future

    // when we're listening
    reader.startListening(streamName, Model.LATEST)(1 second, 1 second) { record =>
      promise.success(record.dataAsString)
    }

    // and when someone writes
    writer.write(streamName, "success")

    // then
    Await.result(future, 30 seconds) shouldBe "success"

    // clean up
    writer.shutdown()
    Await.result(reader.shutdown(), Duration.Inf)
  }
}
