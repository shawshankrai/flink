package datastreams

import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

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

  def main(args: Array[String]): Unit = {
    // applicationTemplate()
    demoTransformations()
  }

}

/*Result: threadNumber> Numbers, Reason flink's distributed computing
  1> 1
  2> 2
  4> 4
  3> 3
*/

