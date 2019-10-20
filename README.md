# Simple Kinesis

A Scala client for AWS Kinesis:

* based on the AWS Java SDK _Version 2_ (async!)
* based on the latest version of that
* made for humans to use

## Reading from Kinesis

It's easy: 

```scala
import simplekinesis._
import monix.execution.Scheduler.Implicits.global
import software.amazon.awssdk.regions.Region
import scala.concurrent.duration._

val streamName    = "my-stream-name"
val reader        = new SimpleKinesisReader(Region.EU_CENTRAL_1)

reader.startListening(streamName, Model.LATEST)(1.second, 1.second) { record =>
  // do something with record!
}

// clean up in the end
reader.shutdown()
```


## Writing to Kinesis

```scala
import simplekinesis._
import software.amazon.awssdk.regions.Region

val streamName = "my-stream-name"
val writer     = new SimpleKinesisWriter(Region.EU_CENTRAL_1)

writer.write(streamName, "Hallo Welt!")

// clean up in the end
writer.shutdown()
```

## Trying locally

Your environment should be set in a way that you have access to a Kinesis stream. On a local machine, you usually do
```zsh
aws sts assume-role --role-arn arn:aws:iam::<projectId>:role/<some-role> --role-session-name temp-name --profile profile-name
```
and from that set your `AWS_SESSION_TOKEN`.

## Examples

There are a few examples that you can run using `sbt`:

```sbt
sbt:simplekinesis> runMain examples.ReadAndWriteExample
```

## Mental Model

* Kinesis has several **streams** (think: topics). 
* They can be **sharded** (think: distributed) across different computers.
* A **record** (think: message) has a **shardKey** that determines the **shard**.
* Every **record** has a **sequenceNumber** that is unique within **its shard** (not globally!).
* To make sure you get _every_ message, you have to make use of the **shardIterator**.
* Whenever you get one or more messages, you also get the **updated shardIterator**.
* In the next read request, you send the latest **shardIterator** and request everything that's newer.

For the initial read, there are different **shardIterators** to choose from:

* `LATEST` — from now on
* `TRIM_HORIZON` — from the beginning of the stream (max 7 days, default 24h)
* `AT_TIMESTAMP(timestamp)` — for example now minus 5 minutes
* `AFTER_SEQUENCE_NUMBER(sequenceNumber)` — if you stored that in a database
* `AT_SEQUENCE_NUMBER(sequenceNumber)` — if you stored that in a database and don't trust Kinesis