# Lists

| Method           | Description |
|------------------|---|
|`xs.length`       | The list's number of elements|
|`xs.last`         | The list's last element|
|`xs.init`         | The list's elements without the last |
|`xs.take(n)`      | A list containing the first `n` elements of `xs` or `xs` itself|
|`xs.drop(n)`      | A list containing the elements of `xs` without the first `n` elements|
|`xs(n)`           | The element of `xs` at index `n`|
|`xs ++ ys`        | The concatenation of `xs` and `ys`|
|`xs.reverse`      | The reversed version of the list|
|`xs.updated(n, x)`| The list but with element `x` at index `n`|
|`xs.indexOf(x)`   | The index of the first element of the list equal to `x` or `-1`|
|`xs.contains(x)`  | Whether the list has an element `x` or not|

> #### Exercise
> ##### Prompt
> Implement `init` as an external function
> ```scala
> def init[T](xs: List[T]): List[T] = xs match
>     case List() => throw Error(”init of empty list”)
>     case List(x) => ???
>     case y :: ys => ???
> ```
> ##### Solution
> ```scala
> def init[T](xs: List[T]): List[T] = xs match
>     case List()  => throw Error(”init of empty list”)
>     case List(x) => List()
>     case y :: ys => y :: init(ys)
> ```

> #### Exercise
> ##### Prompt
> Remove the n ’th element of a list xs . If n is out of bounds, return xs itself.
> ```scala
> def removeAt[T](n: Int, xs: List[T]) = ???
> ```
> Usage example:
> ```scala
> removeAt(1, List(’a’, ’b’, ’c’, ’d’)) // List(a, c, d)
> ```
> ##### Solution
> ```scala
> def removeAt[T](n: Int, xs: List[T]) = xs match
>     case Nil => Nil
>     case y :: ys => if n == 0 then ys else y :: removeAt(n-1,ys)
> ```

> #### Exercise
> ##### Prompt
> Flatten a list structure:
> ```scala
> def flatten(xs: List[Any]): List[Any] = ???
> ```
> ```scala
> flatten(List(List(1, 1), 2, List(3, List(5, 8)))) // res0: List[Any] = List(1, 1, 2, 3, 5, 8)
> ```
> ##### Solution
> ```scala
> def flatten(xs: List[Any]): List[Any] = xs match
>     case Nil => Nil
>     case (head: List[Any]) :: rest =>
>         flatten(head) ++ flatten(rest)
>     case head :: rest =>
>         head :: flatten(rest)
> ```

### Tuples and generic methods
#### Merge sort
My first attempt
```scala
def msort(xs: List[Int]): List[Int] = 
    val n = xs.length / 2
    if n == 0 then 
        xs
    else
        def merge(xs: List[Int], ys: List[Int]): List[Int] = xs match
            case Nil => ys
            case x :: x_rest => ys match
                case Nil => xs
                case y :: y_rest =>
                    if x <= y then
                        x :: merge(x_rest, ys)
                    else 
                        y :: merge(xs, y_rest)
        merge( 
            msort(xs.take(n)),
            msort(xs.drop(n))
        )
```
`ls.splitAt(n)` returns two lists, split at position `n`. These are returned in
a pair. This are written as `(x,y)` in Scala.

For small $n$ (up to 22), the type `(T1, ..., Tn)` is an abbreviation for
`scala.TupleN[T1,...,Tn]`.

The fields of a tuple can be accessed with `._1`, `._2`, ..., `._n`.

After this, redefine `msort`, where we parametrize the order function

```scala
def msort[T](ls: (T, T) => Boolean)(xs: List[T]): List[T] = 
    val n = xs.length / 2
    if n == 0 then 
        xs
    else
        def merge(xs: List[T], ys: List[T]): List[T] = (xs, ys) match
            case (Nil, ys)      => ys
            case (xs, Nil)      => xs
            case (x :: x_rest, y :: y_rest) =>
                if ls(x,y)
                then x :: merge(x_rest, ys)
                else y :: merge(xs, y_rest)
        val (fst, snd) = xs.splitAt(n)
        merge( msort(ls)(fst), msort(ls)(snd))
```

### Higher-order methods
These are `List`'s methods which use higher-order functions
 * `map`. Takes a function and returns the list of results of applying said 
   function to the elements of the list.
 * `filter`. Takes a predicate and returns the items in the list which follow the predicate.
 * `filterNot`. The complementary to `filter`.
 * `partition`. Same as `(xs,p) => (xs.filter(p), xs.filterNot(p))`.
 * `takeWhile`. The longest prefix of elements that satisfy a given predicate.
 * `dropWhile`. The remainder after doing `takeWhile`
 * `span`. The same as `(xs,p) => (xs.takeWhile(p), xs.dropWhile(p))`.

<!-- The `map` function is an extension method of a `List` which, given a function, --> 
<!-- returns a list where each element is the result of applying the function to the -->
<!-- corresponding element on the original list. -->

<!-- Something similar to: -->
<!-- ```scala -->
<!-- extension [T](xs: List[T]) -->
<!--     def map[U](f: T => U): List[U] = xs match -->
<!--         case Nil => xs -->
<!--         case x :: xs => f(x) :: xs.map(f) -->
<!-- ``` -->

> #### Exercise
> ##### Prompt
> Write a function `pack` that packs consecutive duplicates of list elements into
> sublists. For instance
> ```scala
> pack(List("a","a","a","b","c","c","a"))
> ```
> should return
> ```scala
> List(List("a","a","a"),List("b"),List("c","c"), List("a"))
> ```
> ##### Solution
> ```scala
> def pack[T](xs: List[T]): List[List[T]] = xs match
>     case Nil      => Nil
>     case x :: xs1 => 
>         val (e,rest) = xs.span(y => y == x)
>         e :: pack(rest)
> ```

> #### Exercise
> ##### Prompt
> Using `pack`, implement the function `encode` that produces the run-length 
> encoding of a list.
> 
> The idea is to encode `n` consecutive duplicates of an element `x` as a pair
> `(x,n)`. For instance,
> ```scala
> encode(List("a","a","a","b","c","c","a"))
> ```
> should give
> ```scala
> List(("a",3),("b",1),("c",2),("a",1))
> ```
> ##### Solution
> ```scala
> def encode[T](xs: List[T]): List[(T,Int)] =  pack(xs).map(ys => (ys.head,as.size))
> ```

The `reduceLeft` method takes an operation between elements an returns the 
result of applying said operation to all the elements of the array.

### A shorter way to write functions
We can leave out the parameters and `=>` on the syntax for an arrow function by
using `_` as the positional arguments. The only problem is that each appearance
of `_` implies a different argument name (by position), so one argument cannot
be used more than once, and they must be used in a given order
```scala
def sum(xs:List[Int]) = (0::xs).reduceLeft(_ + _)
def product(xs:List[Int]) = (1::xs).reduceLeft(_ * _)
```
Is equivalent to 
```scala
def sum(xs:List[Int]) = (0::xs).reduceLeft((x,y) => x + y)
def product(xs:List[Int]) = (1::xs).reduceLeft((x,y) => x * y)
```

The `foldLeft` method is a generalization of `reduceLeft`, which also takes an
accumulator as the first parameter, serving as the result when used over an 
empty list.

Both have their equivalent `foldRight` and `reduceRight`.

> #### Exercise
> ##### Prompt
> Complete the following definitions of the basic functions map and length
> on lists, such that their implementation uses foldRight :
> ```scala
> def mapFun[T, U](xs: List[T], f: T => U): List[U] =
>     xs.foldRight(List[U]())( ??? )
> ```
> ```scala
> def lengthFun[T](xs: List[T]): Int =
>     xs.foldRight(0)( ??? )
> ```
> ##### Solution
> ```scala
> def mapFun[T, U](xs: List[T], f: T => U): List[U] =
>     xs.foldRight(List[U]())((x, acc) => f(x)::acc)
> ```
> ```scala
> def lengthFun[T](xs: List[T]): Int =
>     xs.foldRight(0)((x,acc) => acc + 1)
> ```

### Referential transparency
_"A term is always equivalent to the term it reduces"_
