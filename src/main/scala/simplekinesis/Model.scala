package simplekinesis

import java.nio.charset.StandardCharsets
import java.time.Instant

import software.amazon.awssdk.services.kinesis.model.{Record, Shard}

import scala.concurrent.duration.Duration

object Model {
  sealed trait ShardIterator
  case class AT_SEQUENCE_NUMBER(sequenceNumber: String)    extends ShardIterator
  case class AFTER_SEQUENCE_NUMBER(sequenceNumber: String) extends ShardIterator
  case class AT_TIMESTAMP(timestamp: Instant)              extends ShardIterator
  case object TRIM_HORIZON                                 extends ShardIterator
  case object LATEST                                       extends ShardIterator

  case class State(shard: Shard, shardIterator: String, records: Seq[KinesisRecord])

  case class KinesisRecord(data: Array[Byte], sequenceNumber: String, approximateArrivalTime: Instant, partitionKey: String) {
    def dataAsString = new String(data, StandardCharsets.UTF_8)
  }

  object KinesisRecord {
    def apply(record: Record): KinesisRecord =
      new KinesisRecord(record.data().asByteArray(), record.sequenceNumber(), record.approximateArrivalTimestamp(), record.partitionKey())
  }

  case class KinesisRecordsResponse(records: Seq[Record], nextShardIterator: String, behindLatest: Duration)

  case class PutRecords(shardId: String, sequenceNumber: String)

}
