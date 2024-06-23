package datastreams

import org.apache.flink.api.common.functions.{FlatMapFunction, MapFunction, ReduceFunction}
import org.apache.flink.api.common.serialization.SimpleStringEncoder
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink
import org.apache.flink.streaming.api.scala.{DataStream, KeyedStream, StreamExecutionEnvironment}
import org.apache.flink.util.Collector

object EssentialStreams {

  private def applicationTemplate(): Unit = {
    // 1. Execution environment
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    // 2. Any computation in between
    import org.apache.flink.api.scala._ // 2.a Implicit import type information (here for elements)
    val simpleIntegerStream: DataStream[Int] = env.fromElements(1, 2, 3, 4)

    // 2.b. Perform some actions
    simpleIntegerStream.print()

    // 3. At the end
    env.execute() // Trigger all computations in the environment
  }

  // Transformations
  private def demoTransformations(): Unit = {
    // 1. Execution environment
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    // 2. Any computation in between
    import org.apache.flink.api.scala._ // 2.a Implicit import type information (here for elements)
    val numbers: DataStream[Int] = env.fromElements(1, 2, 3, 4)

    // Checking parallelism
    println(s"Current parallelism ${env.getParallelism}")

    // 3. Transformations

    // maps
    val doubleNumbers: DataStream[Int] = numbers.map(x => x * 2)
    val expandedNumbers: DataStream[Int] = numbers.flatMap(n => List(n, n + 1))

    // filter
    val filteredNumbers: DataStream[Int] = numbers.filter(n => n % 2 == 0)

    // 4. Action
    expandedNumbers.writeAsText("output/expandedStreams.txt")
    // creates folder expandedStreams.txt with files in it, Current parallelism 8, 8 files generated

    env.setParallelism(1) // Can Set Parallelism any time
    println(s"Current parallelism ${env.getParallelism}")
    doubleNumbers.writeAsText("output/doubleNumbers.txt")
    // creates file doubleNumbers.txt with data in it, Current parallelism 1, 1 file generated

    // Can set Parallelism to Sink, Parallelism of environment remains same
    val writeFilteredToText = filteredNumbers.writeAsText("output/filteredResult.txt")
    writeFilteredToText.setParallelism(2)
    println(s"Current parallelism ${env.getParallelism}")
    env.execute()
  }

  private case class FizzBuzzResult(number: Long, output: String)
  def fizBuzzExercise(): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    val numbers = env.fromSequence(1, 100)

    import org.apache.flink.api.scala._

    val fizzBuzz  = numbers.map[FizzBuzzResult] {(element: Long) => element match {
      case n if n % 3 == 0 && n % 5 == 0 => FizzBuzzResult(element, "fizzbuzz")
      case n if n % 3 == 0 => FizzBuzzResult(element, "fizz")
      case n if n % 5 == 0 => FizzBuzzResult(element, "buzz")
      case n => FizzBuzzResult(element, s"$element")
    }}.filter(_.output == "fizzbuzz").map(_.number)

    // Sink - generates part files
    fizzBuzz.addSink(
      StreamingFileSink
        .forRowFormat(
          new Path("output/fizzbuzz.sink"),
          new SimpleStringEncoder[Long]("UTF-8")
        ).build()
    ).setParallelism(1)

    env.execute()
  }

  private def demoExplicitTransformations(): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    import org.apache.flink.api.scala._

    val numbers = env.fromSequence(1, 3)

    // explicit transformations
    // Map
    val doubleNumbers = numbers.map(new MapFunction[Long, Long] {
      // declare fields, methods
      override def map(value: Long): Long = value * 2
    })


    val expandedNumbersV1 = numbers.flatMap(n => (1L to n).toList)
    // FLatMap
    val expandedNumbersV2 = numbers.flatMap(new FlatMapFunction[Long, Long] { // new FlatMapFunction[Input, Output]
      override def flatMap(value: Long, out: Collector[Long]): Unit =
        (1L to value).foreach {i =>
          out.collect(i) // imperative style - pushes the element downstream
        }
    })

    // expandedNumbersV2.print()

    // process - general function
    val expandedNumbersV3 = numbers.process(new ProcessFunction [Long, Long]{
      override def processElement(value: Long, ctx: ProcessFunction[Long, Long]#Context, out: Collector[Long]): Unit =
        (1L to value).foreach { i =>
          out.collect(i) // imperative style - pushes the element downstream
        }
    })

    // expandedNumbersV3.print()

    // reduce
    // works on keyed streams KeyedStream[Value, Key] for every value attach a key
    // true and false key both are dumping sums in the stream
    /*
    * 1> 3
      1> 4
      5> 2
    * true = 2 -th 5 , false = 4 -th 1
  * */
    val keyedStreamV1: KeyedStream[Long, Boolean] = numbers.keyBy(n => n % 2 == 0)
    val sumByKey = keyedStreamV1.reduce(_ + _)
    // sumByKey.print()

    // explicit
    val sumByKeyExplicit = keyedStreamV1.reduce(new ReduceFunction[Long] {
      override def reduce(value1: Long, value2: Long): Long = value1 + value2
    })

    sumByKeyExplicit.print()
    env.execute()
  }
  def main(args: Array[String]): Unit = {
    // applicationTemplate()
    // demoTransformations()
    // fizBuzzExercise()
    demoExplicitTransformations()
  }

}

/*Result: threadNumber> Numbers, Reason flink's distributed computing
  1> 1
  2> 2
  4> 4
  3> 3
*/

