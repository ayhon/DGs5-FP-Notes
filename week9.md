# Lazy evaluation and laws of data structures

## Laws of `IntSet`

For any set `s` and elements `x` and `y`
 * `Empty.contains(x)` = `false`
 * `s.incl(x).contains(x)` = `true`
 * `s.incl(y).contains(x)` = `s.contains(x)`
   * This last two can be summed up as  
   `s.incl(y).contains(x) = y == x || s.contains(x)`

> #### Exercise
> ##### Prompt
> We have `IntSet` defined as:
> ```scala
> abstract class IntSet:
>     def incl(x: Int): IntSet
>     def contains(x: Int): Boolean
> 
> object Empty extends IntSet:
>     def contains(x: Int): Boolean = false
>     def incl(x: Int): IntSet = NonEmpty(x, Empty, Empty)
> 
> case class NonEmpty(elem: Int, left: IntSet, right: IntSet) extends IntSet:
>     def contains(x: Int): Boolean =
>         if x < elem then
>             left.contains(x)
>         else if x > elem then 
>             right.contains(x)
>         else 
>             true
>     def incl(x: Int): IntSet =
>         if x < elem then 
>             NonEmpty(elem, left.incl(x), right)
>         else if x > elem then 
>             NonEmpty(elem, left, right.incl(x))
>         else 
>             this
> ```
> Supposing we add a function `union` to `IntSet`, such that
> ```scala
> object Empty extends IntSet:
> def union(other: IntSet) = other
> 
> class NonEmpty(x: Int, l: IntSet, r: InstSet) extends IntSet:
> def union(other: IntSet) = l.union(r.union(other)).incl(x)
> ```
> Prove the following law
> ```scala
> xs.union(ys).contains(x) = xs.contains(x) || ys.contains(x)
> ```
> 
> ##### Solution
> ###### Base case: `xs = Empty`
> ```scala
> Empty.union(ys).contains(x) =
> ys.contains(x)
> false || ys.contains(x)
> Empty.contains(x) || ys.contains(x)
> ```
> ###### Recursive case: `xs = NonEmpty(y, l, lr)`
> ```scala
> NonEmpty(y,l,r).union(ys).contains(x)
> l.union(r.union(ys)).incl(y).contains(x)
> y == x || l.union(r.union(ys)).contains(x)
> y == x || l.contains(x) || r.union(ys).contains(x)
> y == x || l.contains(x) || r.contains(x) || ys.contains(x)
> NonEmpty(y,l,r).contains(x) || ys.contains(x)
> ```
> Note that we use the previous laws

## LazyLists
They are formed of concatenations of `LazyList.cons` terminated on a `LazyList.empty`.  
`LazyList.cons(T, LazyList.cons | LazyList.empty)`

They can be contracted using the `LazyList` object fabric like regular lists.

The method `to(LazyList)` will transform to a `LazyList`

`LazyList` implements all methods from lists except `::`, which is exchanged by 
`#::`

Mock implementation:
```scala
trait TailLazyList[+A] extends Seq[A]:
    def isEmpty: Boolean
    def head: A
    def tail: TailLazyList[A]

object TailLazyList:
    def cons[T](hd: T, tl: => TailLazyList[T]) = new TailLazyList[T]:
        def isEmpty = false
        def head = hd
        def tail = tl
        override def toString = ”LazyList(” + hd + ”, ?)”

    val empty = new TailLazyList[Nothing]:
        def isEmpty = true
        def head = throw NoSuchElementException(”empty.head”)
        def tail = throw NoSuchElementException(”empty.tail”)
        override def toString = ”LazyList()”
```

> #### Exercise
> ##### Prompt
> Consider
> ```scala
> def lazyRange(lo: Int, hi: Int): TailLazyList[Int] =
>     print(lo+” ”)
>     if lo >= hi then TailLazyList.empty
>     else TailLazyList.cons(lo, lazyRange(lo + 1, hi))
> ```
> When you write `lazyRange(1, 10).take(3)` what gets printed.
> ##### Solution
> `1, 2, 3`


## Lazy evaluation
We optimize `by-name` evaluation by storing the previously computed values in
case they are needed again. We now call this **lazy evaluation**.

To enable it, use the `lazy` modifier before expressions

> #### Exercise
> ##### Prompt
> ```scala
> def expr =
>     val x = { print("x"); 1 }
>     lazy val y = { print("y"); 2 }
>     def z = { print("z"); 3 }
>     z + y + x + z + y + x
> ```
> ##### Solution
> The text printed is: `xzyz`


The `LazyList` has a lazy implementation of both `isEmpty`, `head` and `tail`
by maintaining a lazy state variable
```scala
class LazyList[+T](init: => State[T]):
    lazy val state: State[T] = init

enum State[T]:
    case Empty
    case Cons(hd: T, tl: LazyList[T])
```

### Sieve of Eratosthenes with lazy evaluation
```scala
def sieve(s: LazyLsist[Int]): LazyList[Int] = s match
    case (prime #:: rest) => 
        prime #:: sieve(rest.filter(_ % prime != 0))
val primes = sieve(from(2))
```

### Square roots using lazy evaluation
Notice how the `LazyList` is defined in term of itself.
```scala
def sqrtSeq(x: Double): LazyList[Double] =
    def improve(guess: Double) = (guess + x / guess) / 2
    lazy val guesses: LazyList[Double] = 1 #:: guesses.map(improve)
    guesses

def isGoodEnough(guess: Double, x: Double) = 
    ((guess * guess - x) / x).abs < 0.0001
```

> #### Exercise
> ##### Prompt
> Consider the following ways to express the infinite list of multiples of a given
> number `N`
> ```scala
> val xs = from(1).map(_ * N)
> 
> val ys = from(1).filter(_ % N == 0)
> ```
> which one is faster?
> ##### Solution
> `xs`, because it returns a new result in constant time, while `ys` must check
> N-1 numbers before finding a solution

## The water pouring problem

 * You are given some glasses of different sizes
 * Your task is to produce a glass with a given amount of water in it
 * You don't have a measure or balance
 * All you can do is:
   * Fill a glass (completely)
   * Empty a glass
   * Pour from one glass to another until the first glass is empty or the second
     glass is full

For example, you have two glasses of 7 and 4 units of water respectively. Produce
a glass filled with 6 units of water.

### Solving
Given a list of glasses G, for one value x we can obtain for each glass g either
x + g where there is a glass h that x + g $\leq$ h and abs(x - g):
You also cannot use the glass where the water is at right now, which means not
all g's are available.

```scala

def possibilities(glasses: List[Int]): LazyList[Int] = 
    val topGlass = 
    lazy val poss = 0 #:: poss.flatMap( (x: Int) => 
        (for g <- glasses yield (g-x).abs) ++ (for g <- glasses if x + g <=
    )
```

## Solution
```scala
type Glass = Int
type State = Vector[Int]

class Pouring(full: State):
    enum Move:
        case Empty(glass: Glass)
        case Fill(glass: Glass)
        class Pour(from: Glass, to: Glass)

        def apply(state: State): State = this match
            case Empty(glass) => state.updated(glass, 0)
            case Fill(glass) => state.updated(glass, full(glass))
            case Pour(from: Glass, to: Glass) =>
                val amount = state(from) min (full(to) - state(to))
                state.updated(from, state(from)-amount)
                     .updated(to, state(to)+amount)
    val moves =
        val glasses: Range = 0 until full.length
        (for g <- glasses yield Move.Empty(g))
        ++ (for g <- glasses yield Move.fill(g))
        ++ (for g1 <- glasses; g2 <- glasses if g1 != g2 yield Move.Pour(g1,g2))

    class Path(history: List[Move], val endState: State):
        def extend(move: Move) = Path(move :: history, move(endState))
        override def toString = s"${history.reverse.mkString(" ")} --> $endState"

    val empty: State = fullmap(x => 0)
    val start = Path(Nil, empty)

    def pathsFrom(paths: List[Path], explored: Set[State]): LazyList[List[Path]] =
        val frontier = 
            for
                path <- paths
                move <- moves
                next = path.extend(move)
                if !explored.contains(next.endState)
            yield next
        paths #:: pathsFrom(frontier, explored ++ frontier.map(_.endState))

    def solutions(target: Int): LazyList[Path] =
        for
            paths <- pathsFrom(List(start), Set(empty))
            path <- paths
            if path.endState.contains(target)
        yield path

val problem  = Pouring(Vector(4,7))
problem.solutions(6).head
```
