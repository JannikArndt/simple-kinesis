package fuckingkinesis

import java.util.concurrent.CompletionException

import com.typesafe.scalalogging.StrictLogging
import fuckingkinesis.Model.{KinesisRecord, ShardIterator, State}
import monix.execution.schedulers.SchedulerService
import monix.execution.{Cancelable, Scheduler}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.kinesis.model.{KinesisException, Shard}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Reads messages from Kinesis and allows you to do something with them.
  *
  * Initialize and start with
  *
  * {{{
  *   val streamName    = "myStream"
  *   val client        = new FuckingKinesisReader(Region.EU_CENTRAL_1)
  *   val shardIterator = Model.LATEST // or other
  *
  *   client.startListening(streamName, shardIterator)(1 second, 1 second) { record =>
  *     // do something with record!
  *   }
  * }}}
  *
  * In the end, shut down with
  *
  * {{{
  *   client.shutdown()
  * }}}
  *
  * @param region The AWS Region your Kinesis queue is located in.
  */
class FuckingKinesisReader(region: Region) extends StrictLogging {

  private implicit val schedulerService: SchedulerService = Scheduler.io("fucking-kinesis-scheduler")

  private val fuckingKinesisWrapper = FuckingKinesisWrapper(region)

  private def getInitialState(streamName: String, shardIterator: ShardIterator): Future[Seq[State]] = {
    logger.debug(s"Getting initial state of stream $streamName")
    val requestInitialIterators = for {
      shards: Seq[Shard]                   <- fuckingKinesisWrapper.listShards(streamName)
      shardIterators: Seq[(Shard, String)] <- fuckingKinesisWrapper.getShardIterators(streamName, shards, shardIterator)
    } yield shardIterators

    requestInitialIterators.map(iterators => iterators.map(res => State(res._1, res._2, Seq[KinesisRecord]().empty)))
  }

  def startListening(streamName: String, shardIterator: ShardIterator)(
      initialDelay: FiniteDuration = 1 second,
      delayBetweenReads: FiniteDuration = 1 second
  )(action: KinesisRecord => Unit): Future[Cancelable] = {
    logger.debug(s"Starting listening to stream $streamName")

    val runner = new Runner(fuckingKinesisWrapper, delayBetweenReads, action)

    getInitialState(streamName, shardIterator)
      .map { initialState =>
        logger.debug(s"Initial state loaded, ${initialState.size} shard(s). Scheduling reader every $initialDelay")
        schedulerService.scheduleOnce(initialDelay)(runner.readAndSchedule(initialState))
      }
      .recover {
        case e: CompletionException =>
          e.getCause match {
            case ex: KinesisException =>
              ex.awsErrorDetails().errorCode() match {
                case "ExpiredTokenException" =>
                  logger.error("The aws security token has expired!", e)
                  throw ex
              }
          }
        case t: Throwable =>
          logger.error("Error getting initial state!", t)
          throw t
      }
  }

  def shutdown()(implicit executionContext: ExecutionContext): Future[Boolean] = {
    logger.info("Starting shutdown of FuckingKinesisReader...")

    logger.info("Shutting down scheduler...")
    schedulerService.shutdown()
    logger.info("Shutting down netty http client and AWS client...")
    fuckingKinesisWrapper.shutdown()
    logger.info("Waiting for scheduler to finish shutdown...")
    schedulerService
      .awaitTermination(30.seconds, monix.execution.Scheduler.global)
      .andThen {
        // Note: This Future needs to run on a different ExecutionContext then the one being shut down!
        case Failure(exception) => logger.error("Error while shutting down reader!", exception)
        case Success(_)         => logger.info("Reader shut down successfully.")
      }(executionContext)
  }

  private class Runner(fuckingKinesisWrapper: FuckingKinesisWrapper,
                       delayBetweenReads: FiniteDuration = 1 second,
                       action: KinesisRecord => Unit)(
      implicit schedulerService: SchedulerService
  ) extends StrictLogging {

    def readAndSchedule(state: Seq[State]): Unit =
      readAndGetNextState(state).foreach { newState =>
        logger.debug(s"Read ${newState.map(_.records.size).sum} records. Starting action...")
        newState.map(_.records.map(action))
        logger.debug(s"Action done. Next read in $delayBetweenReads")
        schedulerService.scheduleOnce(delayBetweenReads)(readAndSchedule(newState))
      }

    private def readAndGetNextState(lastState: Seq[State]): Future[Seq[State]] = {
      val res: Seq[Future[State]] = lastState.map { state =>
        logger.debug(s"Reading new state...")

        fuckingKinesisWrapper
          .getRecordsFromIterator(state.shardIterator)
          .map(res => State(state.shard, res.nextShardIterator, res.records.map(KinesisRecord.apply)))
      }
      Future.sequence(res)
    }
  }
}
