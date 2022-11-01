# Collections

### `Vectors`

Use the `Vector` to get a more usual implementation of arrays (in other 
programming languages)

 * `x +: xs` creates a vector with the element `x`, followed by `xs`
 * `xs :+ x` creates a vector with the element `x`, preceded by `xs`

`Vector` and `List` are both subclasses of `Set`, which is in turn a subclass
of `Iterable`.

`Array`s and `String`s support the same operations as `Seq` and can be converted
implicitly to sequences if needed, but they are not its subclasses because they
come from Java.

### Ranges
Use the operators `to` (inclusive), `until` (exclusive) and `by` (to determine 
step)
```scala
val r: Range = 1 to 10 by 2
val s: Range = 20 until 0 by -1
```

### Sequence operations
 * `xs.exists(p)` whether there is an element who satisfies `p`
 * `xs.forall(p)` whether all elements satisfy `p`
 * `xs.zip(ys)` a list of pairs from `xs` and `ys`
 * `xs.unzip` splits a sequence of pairs
 * `xs.flatMap(f)` Applies a collection-valued function to all elements of `xs`
   and concatenates the result.
 * `xs.sum` The sum of all elements of the numeric collection
 * `xs.product` The product of all elements of the numeric collection
 * `xs.max`
 * `xs.min`

To list all combinations of numbers $x$ and $y$ where $x$ is drawn from $1,
\ldots,M$ and y is drawn from $1,\ldots,N$ :
```scala
(1 to M).flatMap(x => (1 to N).map(y => (x, y)))
```

Compute the scalar product of two vectors
```scala
def scalarProduct(xs: Vector[Double], ys: Vector[Double]): Double =
    xs.zip(ys).map( (x,y) => x * y).sum
```

> #### Exercise
> ##### Prompt
> A number $n$ is prime if the only divisors of $n$ are $1$ and $n$ itself.
> Write a high level test of primality of numbers.
> ```scala
> def isPrime(n: Int): Boolean = ???
> ```
> ##### Solution
> ```scala
> def isPrime(n: Int): Boolean =
>     !(2 until n).exists(n % _ == 0)
> ```

### For-expression
```scala
for p <- persons if p.age > 20 yield p.name
```
is equivalent to
```scala
persons
    .filter(p => p.age > 20)
    .map(p => p.name)
```

The expression is of the form `for s yield e` where `s` is a sequence of 
**generators** and **filters**, and `e` is an expression whose value is returned in
the iteration. This can be though of as a list comprehension in python.
 * A **generator** is of the form `p <- e`, where `p` is a pattern and `e` an
   expression whose value is a collection
 * A **filter** is of the for `if f` where `f` is a boolean expression.

The sequence must start with a generator. If there are more than one, the latter
generators vary earlier than the first ones.

```scala
for
    i <- 1 until n
    j <- 1 until i
    if isPrime(i+j)
yield (i,j)
```

> #### Exercise
> ##### Prompt
> Write a version of `scalarProduct` that makes use of a `for`:
> ```scala
> def scalarProduct(xs: Vector[Double], ys: Vector[Double]): Double = ???
> ```
> ##### Solution
> ```scala
> def scalarProduct(xs: Vector[Double], ys: Vector[Double]): Double =
>     (for (x,y) <- xs.zip(ys) yield x * y).sum
> ```

For-expressions may have decompositions on its assignments
```scala
for 
    (a,b) <- (1 to 10) zip (10 to 1 by -1)
yield a + b
```

They may also use the keyword `case` before the pattern-matching to only
match certain expressions.
```scala
for
    case (a, 1) <- (10 to 1 by -1) zip (1 to 20 by 2)
    case (b, 3) <- (10 to 1 by -1) zip (1 to 20 by 2)
yield a + b
```

### Sets
Defined as `Set(item, item, ...)`, a sequence can be transformed into a `Set`
using `toSet`. Most operations on sequences are also available on sets, like
`map`, `filter` or `nonEmpty`, to check `!isEmpty`

The fundamental differences of `Set` and sequences are:
 * `Set`s are unordered
 * `Set`s do not have duplicate elements
 * The fundamental operation of `Set`s is `.contains`.

#### N-queens 
```scala
def queens(n: Int) =
    def placeQueens(k: Int): Set[List[Int]] =
        if k == 0 then 
            Set(List())
        else
            for
                queens <- placeQueens(k-1)
                col <- 0 until n
                if isSafe(col, queens)
            yield col :: queens
    placeQueens(n)
```

> #### Exercise
> ##### Prompt
> Write a function
> ```scala
> def isSafe(col: Int, queens: List[Int]): Boolean
> ```
> which tests if a queen placed in an indicated column `col` is secure amongst
> the other placed queens.
> ##### Solution
> ```scala
> def isSafe(col: Int, queens: List[Int]): Boolean =
>     val row = queens.length
>     def check(queens: List[Int], y: Int): Boolean = queens match
>         case Nil     => true
>         case x :: qs =>
>             x - y != col - row && // Same diagonal \
>             x + y != col + row && // Same diagonal /
>             x != col &&           // Same column
>             y != row &&           // Same row
>             check(qs,y-1)
>     check(queens,queens.length-1)
> ```

### Map
A class `Map[Key, Value]`, declared as:
```scala
val romanNumberals = Map("I" -> 1, "V" -> 5, "X" -> 10)
```

`Map[Key, Value]` can be iterated over pairs `(Key, Value)`, so they support
methods such as `map`. In fact, `->` is just an alternative way to write a pair.

They can also be used as functions, were calling a with a key returns the 
corresponding value. However, if the key doesn't exist, it throws an error. 
Use the `get` method to get an `Option` value

### Option
Like `Maybe` in Haskell, it has two types
```scala
trait Option[+A]
case class Some[+A](value: A) extends Option[A]
object None extends Option[Nothing]
```
It supports quite a few operations of other collections (Like `map`).

### Updating maps
 * `m + (k->v)` returns the map which takes `k` to `v`, and otherwise uses `m`
 * `m ++ kvs` the map `m` updated with `+` with the pairs in `kvs`

### Sorted
You can get a sorted collection using the `sorted` method, or specify an order
function with `sortWith(order)`.

### GroupBy
Partitions a collection using a discriminator function
```scala
val fruit = List("apple", "pear", "orange", "pinaple")
fruit.groupBy(_.head) // Map with keys 'a', 'p' and 'o'
```

### Variable length argument lists
```scala
def func(param: Type*) = ???
```
The `param` argument is actually seen as a `Seq[Type]` argument, but where the
bindings can be specified freely in the argument list
```scala
func(a,b,c) // where a,b,c of type Type
```

```scala
class Polynom(nonZeroTerms: Map[Int, Double]):
    def this(bindings: (Int, Double)*) = this(bindings.toMap)

    def terms = nonZeroTerms.withDefaultValue(0.0)
    def + (other: Polynom) =
        Polynom(terms ++ other.terms.map( (exp, coeff) => (exp, terms(exp) + coeff)))

    override def toString =
        val termStrings = for (exp,coeff) <- terms.toList.sorted.reverse
            yield
                val exxponent = if exp == 0 then "" else s"x^$exp"
                s"$coeff$exponent"
        if terms.isEmpty then "0" else termStrings.mkString(" + ")
```

> #### Exercise
> ##### Prompt
> The `+` operation on `Polynom` used map concatenation with `++`. Design another
> version of `+` in terms of `foldLeft`
> ```scala
> def + (other: Polynom) = 
>     Polynom(other.terms.foldLeft(???)(addterm))
> 
> def addTerm(terms: Map[Int, Double], term: (Int, Double)) =
>     ???
> ```
> Which of the two versions do you believe is more efficient?
> 
> ##### Solution
> ```scala
> def + (other: Polynom) = 
>     Polynom(other.terms.foldLeft(this)(addterm))
> 
> def addTerm(terms: Map[Int, Double], term: (Int, Double)) =
>     val (exp, coeff1) = term
>     val (_, coeff2) = terms(exp)
>     terms + (exp, coeff1 + coeff2)
> ```
> Which of the two versions do you believe is more efficient?
>  * Since `foldLeft` is using the same principle behind `++`, but doesn't require
>    building a new map, it's probably faster.

```scala
class Coder(words: List[String]):
    val mnemonics = Map(
        ’2’ -> ”ABC”, ’3’ -> ”DEF”, ’4’ -> ”GHI”, ’5’ -> ”JKL”,
        ’6’ -> ”MNO”, ’7’ -> ”PQRS”, ’8’ -> ”TUV”, ’9’ -> ”WXYZ”
    )

    private val charCode: Map[Char, Char] =
        for
            (digit, characters) <- mnemonics
            letter <- characters
        yield letter -> digit

    private def wordCode(word: String): String = word.toUpperCase.map(charCode)

    private val wordsForNum: Map[String, List[String]] =
        words.groupBy(wordCode).withDefaultValue(Nil)

    def encode(number: String): Set[List[String]] =
        if number.isEmpty then
            Set(Nil)
        else
            for
                splitPoint <- (1 to number.length).toSet
                word <- wordsForNum(number.take(splitPoint))
                rest <- encode(number.drop(splitPoint))
            yield word :: rest
```
