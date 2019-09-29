package fuckingkinesis

import com.typesafe.scalalogging.StrictLogging
import fuckingkinesis.Model.PutRecords
import software.amazon.awssdk.regions.Region

import scala.concurrent.{ExecutionContext, Future}

class FuckingKinesisWriter(region: Region)(implicit executionContext: ExecutionContext) extends StrictLogging {

  private val fuckingKinesisWrapper = FuckingKinesisWrapper(region)

  def write(streamName: String, utf8Text: String): Future[PutRecords] =
    fuckingKinesisWrapper.putRecord(streamName, partitionKeyHack(utf8Text), utf8Text)

  private def partitionKeyHack(text: String): String = text.headOption.getOrElse('a').toString

  def shutdown(): Unit = {
    logger.info("Starting shutdown of FuckingKinesisWriter...")
    logger.info("Shutting down netty http client and AWS client...")
    fuckingKinesisWrapper.shutdown()
  }
}
