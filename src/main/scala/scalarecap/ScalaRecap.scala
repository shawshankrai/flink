package scalarecap
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Try, Success, Failure}

object ScalaRecap {

  // value
  val aBoolean: Boolean = false

  // variable
  private var aVariable: Int = 56
  aVariable += 1

  // expression
  val anIfExpression: String = if(2 > 4) "bigger" else "smaller"

  // instruction
  val theUnit: Unit = print("Something") // Unit === Void

  // OOP
  class Animal
  class Cat extends Animal
  trait Carnivore {
    def eat(animal: Animal): Unit
  }

  // inheritance: extends <= 1 class, but inherit from >=0 traits
  class Crocodile extends Animal with Carnivore {
    override def eat(animal: Animal): Unit = {
      println(s"eating this $animal")
    }
  }

  // singleton
  object MySingleton

  // companions: can access each other private fields
  object Carnivore

  // case class
  case class Person(name: String, age: Int)

  // generics
  class MyList[A] // can hold only A type of objects

  // method notations
  // crocodile.eat(animal) Or crocodile eat animal
  val three: Int = 1 + 2
  val three_v2: Int = 1.+(2)

  // FP
  private val incrementer: Int => Int = x => x + 1
  val incremented: Int = incrementer(2)
  val incremented_v2: Int = incrementer.apply(4)

  // map, flatMap, filter = HOFs
  val processedList: List[Int] = List(1, 2, 3).map(incrementer) // [2, 3, 4]
  val aLongerList: List[Int] = List(1, 2, 3).flatMap(x => List(x, x+ 1)) // [[1,2], [2,3], [3, 4]] flattening [1, 2, 2, 3, 3, 4]

  // for-comprehension
  val checkerboard: List[(Int, String)] = List(1, 2, 3).flatMap(n => List("a", "b", "c").map(c => (n, c)))
  val checkerboard_v2: List[(Int, String)] = for {
    n <- List(1, 2, 3)
    c <- List("a", "b", "c")
  } yield (n, c)

  // options and try
  private val anOption: Option[Int] = Option(43) // Companion object of Option
  val doubleOption: Option[Int] = anOption.map(_ * 2)

  private val anAttempt: Try[Int] = Try(12)
  val modifiedAttempt: Try[Int] = anAttempt.map(_ * 2)

  // Pattern matching
  private val anUnknown: Any = 45
  val medal: String = anUnknown match {
    case 1 => "gold"
    case 2 => "silver"
    case 3 => "bronze"
    case _ => "no medal"
  }

  val optionDescription: String = anOption match {
    case Some(value) => s"Option is not empty: $value"
    case None => "Option is empty"
  }

  // future
  implicit private val ec: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))
  private val aFuture: Future[Int] = Future(1 + 99)(ec)

  // register a callback
  aFuture.onComplete {
    case Success(value) => println(s"the async meaning of life $value")
    case Failure(exception) => println(s"the meaning of value failed $exception")
  }

  // Partial Function: here try is argument type and unit is return
  private val aPartialFunction: PartialFunction[Try[Int], Unit] = {
    case Success(value) => println(s"the async meaning of life $value")
    case Failure(exception) => println(s"the meaning of value failed $exception")
  }
  // map, flatmap, filter
  val doubleAsyncMOL: Future[Int] = aFuture.map(_ * 2)

  // implicits
  // 1 - pass implicit arguments
  implicit val timeout: Int = 3000

  // () is a function
  private def setTimeout(f: () => Unit)(implicit tout: Int): Unit = {
    Thread.sleep(tout)
    f()
  }
  setTimeout(() => println("timeout"))

  // 2 - extension methods
  implicit class MyRichInteger(number: Int) {
    def isEven: Boolean = number % 2 == 0
  }

  val is2Even: Boolean = 2.isEven


  // conversion - discouraged
  implicit def stringToPerson(name: String): Person = {
    Person(name, 57)
  }

  val per: Person = "Person" // stringToPerson("Person")

  def main(args: Array[String]): Unit = {

  }
}
