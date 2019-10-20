package simplekinesis

import com.typesafe.scalalogging.StrictLogging
import simplekinesis.Model.PutRecords
import software.amazon.awssdk.regions.Region

import scala.concurrent.{ExecutionContext, Future}

class SimpleKinesisWriter(region: Region)(implicit executionContext: ExecutionContext) extends StrictLogging {

  private val simpleKinesisWrapper = SimpleKinesisWrapper(region)

  def write(streamName: String, utf8Text: String): Future[PutRecords] =
    simpleKinesisWrapper.putRecord(streamName, partitionKeyHack(utf8Text), utf8Text)

  private def partitionKeyHack(text: String): String = text.headOption.getOrElse('a').toString

  def shutdown(): Unit = {
    logger.info("Starting shutdown of SimpleKinesisWriter...")
    logger.info("Shutting down netty http client and AWS client...")
    simpleKinesisWrapper.shutdown()
  }
}
