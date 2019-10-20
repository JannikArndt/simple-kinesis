package fuckingkinesis

import com.typesafe.scalalogging.StrictLogging
import fuckingkinesis.Model._
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.http.async.SdkAsyncHttpClient
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient
import software.amazon.awssdk.services.kinesis.model._

import scala.compat.java8.FutureConverters._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

/**
  * A thin wrapper of Scala-love around the worst SDK I've ever seen.
  */
object FuckingKinesisWrapper extends StrictLogging {
  def apply(region: Region)(implicit executionContext: ExecutionContext): FuckingKinesisWrapper = {

    val nettyClient: SdkAsyncHttpClient =
      NettyNioAsyncHttpClient.builder().maxConcurrency(100).maxPendingConnectionAcquires(10000).build()

    val kinesisClient: KinesisAsyncClient =
      KinesisAsyncClient.builder().httpClient(nettyClient).region(region).build()

    new FuckingKinesisWrapper(kinesisClient, nettyClient)
  }
}

class FuckingKinesisWrapper private (awsClient: KinesisAsyncClient, httpClient: SdkAsyncHttpClient)(
    implicit executionContext: ExecutionContext
) extends StrictLogging {

  def listShards(streamName: String): Future[Seq[Shard]] = {
    val request = ListShardsRequest.builder().streamName(streamName).build()
    awsClient.listShards(request).toScala.map(_.shards().asScala.toSeq)
  }

  def describeStream(streamName: String): Future[StreamDescription] = {
    val request = DescribeStreamRequest.builder().streamName(streamName).build()
    awsClient.describeStream(request).toScala.map(_.streamDescription())
  }

  def listConsumers(arn: String): Future[ListStreamConsumersResponse] = {
    val request = ListStreamConsumersRequest.builder().streamARN(arn).build()
    awsClient.listStreamConsumers(request).toScala
  }

  def registerConsumer(arn: String, consumer: String): Future[RegisterStreamConsumerResponse] = {
    val request = RegisterStreamConsumerRequest.builder().consumerName(consumer).streamARN(arn).build()
    awsClient.registerStreamConsumer(request).toScala

    //    res.map{res =>
    //      while (res.consumer().consumerStatus() == ConsumerStatus.CREATING){
    //        Thread.sleep(30)
    //        listConsumers(arn).map(_.consumers())
    //      }
    //      res
    //    }
  }

  def unregisterConsumer(arn: String, consumer: String): Future[DeregisterStreamConsumerResponse] = {
    val request = DeregisterStreamConsumerRequest.builder().consumerName(consumer).streamARN(arn).build()
    awsClient.deregisterStreamConsumer(request).toScala
  }
  //  def subscribe() = {
  //    val consumer = RegisterStreamConsumerRequest.builder().consumerName("temp").build()
  //    awsClient.registerStreamConsumer(consumer)
  //    val subscriber = SubscribeToShardEvent.builder().build()
  //    val request = SubscribeToShardRequest.builder().consumerARN()
  //    awsClient.subscribeToShard(subscriber)
  //  }

  def getShardIterator(streamName: String, shard: Shard, shardIteratorType: ShardIterator): Future[(Shard, String)] = {
    val builder = GetShardIteratorRequest.builder().streamName(streamName).shardId(shard.shardId())
    val request = shardIteratorType match {
      case AT_SEQUENCE_NUMBER(sequenceNumber) =>
        builder.shardIteratorType(ShardIteratorType.AT_SEQUENCE_NUMBER).startingSequenceNumber(sequenceNumber)
      case AFTER_SEQUENCE_NUMBER(sequenceNumber) =>
        builder.shardIteratorType(ShardIteratorType.AFTER_SEQUENCE_NUMBER).startingSequenceNumber(sequenceNumber)
      case AT_TIMESTAMP(timestamp) => builder.shardIteratorType(ShardIteratorType.AT_TIMESTAMP).timestamp(timestamp)
      case TRIM_HORIZON            => builder.shardIteratorType(ShardIteratorType.TRIM_HORIZON)
      case LATEST                  => builder.shardIteratorType(ShardIteratorType.LATEST)
    }

    awsClient.getShardIterator(request.build()).toScala.map(res => (shard, res.shardIterator()))
  }

  def getShardIterators(streamName: String, shards: Seq[Shard], shardIteratorType: ShardIterator): Future[Seq[(Shard, String)]] =
    Future.sequence(shards.map(shard => getShardIterator(streamName, shard, shardIteratorType)))

  def getRecordsFromIterator(shardIterator: String): Future[KinesisRecordsResponse] = {
    val request = GetRecordsRequest.builder().shardIterator(shardIterator).limit(10000).build()
    awsClient
      .getRecords(request)
      .toScala
      .map(res => KinesisRecordsResponse(res.records().asScala.toSeq, res.nextShardIterator(), res.millisBehindLatest().longValue().millis))
  }

  def putRecord(streamName: String, partitionKey: String, utf8String: String): Future[PutRecords] =
    putRecord(streamName, partitionKey, SdkBytes.fromUtf8String(utf8String))

  def putRecord(streamName: String, partitionKey: String, data: SdkBytes): Future[PutRecords] = {
    val request = PutRecordRequest.builder().streamName(streamName).partitionKey(partitionKey).data(data).build()
    awsClient.putRecord(request).toScala.map(res => PutRecords(res.shardId(), res.sequenceNumber()))
  }

  def shutdown(): Unit = {
    httpClient.close()
    awsClient.close()
  }
}
