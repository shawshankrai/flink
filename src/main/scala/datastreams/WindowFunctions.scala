package datastreams

import generators.gaming._
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.streaming.api.scala.function.{AllWindowFunction, ProcessAllWindowFunction}
import org.apache.flink.streaming.api.scala.{AllWindowedStream, DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

import java.time.Instant
import scala.concurrent.duration._

object WindowFunctions {

  val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

  implicit val serverStartTime: Instant = Instant.parse("2022-02-02T00:00:00.000Z")
  val events: List[ServerEvent] = List(
    bob.register(2.seconds),
    bob.online(2.seconds),
    sam.register(3.seconds),
    sam.online(4.seconds),
    rob.register(4.seconds),
    alice.register(4.seconds),
    mary.register(6.seconds),
    mary.online(6.seconds),
    carl.register(8.seconds),
    rob.online(10.seconds),
    alice.online(10.seconds),
    carl.online(10.seconds)
  )

  import org.apache.flink.api.scala._
  private val eventStream: DataStream[ServerEvent] = env.fromCollection(events)
    .assignTimestampsAndWatermarks(
      WatermarkStrategy
        .forBoundedOutOfOrderness(java.time.Duration.ofMillis(500))
        .withTimestampAssigner(new SerializableTimestampAssigner[ServerEvent] {
          override def extractTimestamp(element: ServerEvent, recordTimestamp: Long): Long = element.eventTime.toEpochMilli
        })
    )

  // how many players register every 3 seconds?
  private val threeSecondsTumblingWindow: AllWindowedStream[ServerEvent, TimeWindow] =
    eventStream.windowAll(TumblingEventTimeWindows.of(Time.seconds(3)))

  // count by windowAll
  private class CountByWindowAll extends AllWindowFunction[ServerEvent, String, TimeWindow] {
    override def apply(window: TimeWindow, input: Iterable[ServerEvent], out: Collector[String]): Unit = {
      val registrationEventCount = input.count(event => event.isInstanceOf[PlayerRegistered])
      out.collect(s"Window [${window.getStart} - ${window.getEnd}] $registrationEventCount")
    }
  }

  private class CountByWindowAllProcess extends ProcessAllWindowFunction[ServerEvent, String, TimeWindow] {

    override def process(context: Context, elements: Iterable[ServerEvent], out: Collector[String]): Unit = {
      val window = context.window
      val registrationEventCount = elements.count(event => event.isInstanceOf[PlayerRegistered])
      out.collect(s"Window [${window.getStart} - ${window.getEnd}] $registrationEventCount")
    }
  }

  private class CountByWindowAllAggregate extends AggregateFunction[ServerEvent, Long, Long] {
  //                                                               ^input       ^agg  ^ output

    override def createAccumulator(): Long = 0L

    override def add(value: ServerEvent, accumulator: Long): Long = if (value.isInstanceOf[PlayerRegistered]) accumulator + 1 else accumulator

    override def getResult(accumulator: Long): Long = accumulator // pushing acc in out

    override def merge(a: Long, b: Long): Long = a + b // merging accumulators
  }

  private def demoCountByWindow(): Unit = {
    val registeredPerThreeSeconds: DataStream[String] = threeSecondsTumblingWindow.apply(new CountByWindowAll)
    registeredPerThreeSeconds.print()
    env.execute()
  }

  private def demoCountByProcess(): Unit = {
    val registeredPerThreeSeconds: DataStream[String] = threeSecondsTumblingWindow.process(new CountByWindowAllProcess)
    registeredPerThreeSeconds.print()
    env.execute()
  }

  private def demoCountByAggregate(): Unit = {
    val registeredPerThreeSeconds: DataStream[Long] = threeSecondsTumblingWindow.aggregate(new CountByWindowAllAggregate)
    registeredPerThreeSeconds.print()
    env.execute()
  }

  def main(args: Array[String]): Unit = {
    // demoCountByWindow
    // demoCountByProcess() // much lower level API; Just like Predicate and Function
    demoCountByAggregate()
  }
}

/*
* Result
* 1> Window [1643760003000 - 1643760006000] 3
  2> Window [1643760006000 - 1643760009000] 2
  3> Window [1643760009000 - 1643760012000] 0
  8> Window [1643760000000 - 1643760003000] 1
* */

/*
* Steps:
* 1. Convert stream to Timestamped and Watermarked stream: define what will be the timestamp in the event
* 2. Create window
* 3. Create Window function
* 4. Call window function on window
* */

/*
* Result: Aggregate
* 1> 3
  8> 1
  3> 0
  2> 2
* */