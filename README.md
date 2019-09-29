# Fucking Kinesis

A Scala client for AWS Kinesis:

* based on the _fucking_ AWS Java SDK _Version 2_ (async!)
* based on the _fucking_ latest version of that
* made for _fucking_ humans to use
* written out of _fucking_ hate

## Reading from Kinesis

It's _fucking_ easy: 

```scala
import fuckingkinesis._
import monix.execution.Scheduler.Implicits.global
import software.amazon.awssdk.regions.Region
import scala.concurrent.duration._

val streamName    = "my-stream-name"
val reader        = new FuckingKinesisReader(Region.EU_CENTRAL_1)
val shardIterator = Model.LATEST

reader.startListening(streamName, shardIterator)(1 second, 1 second) { record =>
  // do something with record!
}

// clean up in the end
reader.shutdown()
```


## Writing to Kinesis

```scala
import fuckingkinesis._
import software.amazon.awssdk.regions.Region

val streamName = "my-stream-name"
val writer     = new FuckingKinesisWriter(Region.EU_CENTRAL_1)

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
sbt:fuckingkinesis> runMain examples.ReadAndWriteExample
```