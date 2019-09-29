# Fucking Kinesis

A Scala client for AWS Kinesis:

* based on the _fucking_ AWS Java SDK _Version 2_ (async!)
* based on the _fucking_ latest version of that
* made for _fucking_ humans to use
* written out of _fucking_ hate

## Reading from Kinesis

It's _fucking_ easy: 

```scala
```

Your environment should be set in a way that you have access to a Kinesis stream. On a local machine, you usually do
```zsh
aws sts assume-role --role-arn arn:aws:iam::<projectId>:role/<some-role> --role-session-name temp-name --profile profile-name
```
and from that set your `AWS_SESSION_TOKEN`.