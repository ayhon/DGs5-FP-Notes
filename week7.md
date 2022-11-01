# Monads in Scala

### Queries with for

Given the following "database", look at the following queries
```scala    
case class Book(val title: String, val authors: List[String])
val books: List[Book] = List(
    Book(title = ”Structure and Interpretation of Computer Programs”,
         authors = List(”Abelson, Harald”, ”Sussman, Gerald J.”)),
    Book(title = ”Introduction to Functional Programming”,
         authors = List(”Bird, Richard”, ”Wadler, Phil”)),
    Book(title = ”Effective Java”,
         authors = List(”Bloch, Joshua”)),
    Book(title = ”Java Puzzlers”,
         authors = List(”Bloch, Joshua”, ”Gafter, Neal”)),
    Book(title = ”Programming in Scala”,
         authors = List(”Odersky, Martin”, ”Spoon, Lex”, ”Venners, Bill”))
)
```

##### Find books whose author's name is "Bird"
```scala
for
    b <- books
    a <- b.authors
    if a.startsWith("Bird,")
yield b.title
```

```cpp
vector<string> res;
string name = "Bird,"
for(Book & b : books)
    for(string & author : b.authors)
        if (equal(name.begin(),name.end(),author.begin())
            res.push_back(b.title)
```

##### Find all books which have "Program" in the title
```scala
for
    b <- books
    if b.title.indexOf("Program") >= 0
yield b.title
```

```cpp
vector<string> res;
string word = "Program"
for(Book & b : books)
    if(b.title.find(word) != string::npos)
        res.push_back(b.title)
```

##### The names of all authors who have written at least two books
```scala
for
    b1 <- books
    b2 <- books
    if b1 != b2
    a1 <- b1.authors
    a2 <- b2.authors
    if a1 == a2
yield a1
```
This would make the solutions res.push_back(twice. We could avoid this by checking for)
`b1.title < b2.title`.
```cpp
vector<string> res;
for(Book & b1 : books)
    for(Book & b2 : books)
        if(b1.title < b2.title)
            for(string & a1 : b1.authors)
                for(string & a2: b2.authors)
                    if (a1 == a2)
                        res.push_back(a1)
```

##### The names of all authors who have written at least three books
If we use the previous code, the name of the author would still be printed 3
times. We can solve this by calling finally to the method `.distinct`, but 
that's not really elegant. We can dodge the problem altogether by using `Set`
```scala
val bookSet = books.toSet
for
    b1 <- bookSet
    b2 <- bookSet
    if b1 != b2
    a1 <- b1.authors
    a2 <- b2.authors
    if a1 == a2
yield a1
```

```cpp
set<string> res;
set<Book> bookSet(books.begin(),books.end());
for(Book & b1 : bookSet)
    for(Book & b2 : bookSet)
        if(b1 != b2)
            for(stirng & a1 : b1.authors)
                for(stirng & a2 : b2.authors)
                    if(a1 == a2) 
                        res.insert(a1)
```

### Translation of for
`for` statements in Scala are translated to calls to `map`, `flatMap` and a lazy
variant of `filter`.

1. `for x <- e1 yield e2` is translated to `e1.map(x => e2)`
2. `for x <- e1 if f; yield e2` is translated to `for x <- e1.withFilter(x => f); yield e2`
3. `for x <- e1; y <- e2; yield e3` is translated to `e1.flatMap(x => for y <- e2; yield e3)`

> #### Exercise
> ##### Prompt
> Translate
> ```scala
> for b <- books; a <- b.authors if a.startsWith("Bird")
> yield b.title
> ```
> ##### Solution
> ```scala
> books.flatMap( b => 
>     b.authors
>         .withFilter(a => a.startsWith("Bird"))
>         .map(a => b.title)
> )
> ```

This helps us use `for` in our own structures, as long as we define an 
implementation of the methods `map`, `flatMap` and `withFilter`.

The `for` expressions aren't tied to collection-like things. They only depend
on having the previous methods implemented. This means that we can extend the
clause to more classes, like random value generators.

### Random number generator
```scala
trait Generator[+T]:
    def generate(): T
val integers = new Generator[Int]:
    val rand = java.util.Random()
    def generate() = rand.nextInt()
val booleans = new Generator[Boolean]:
    def generate() = integers.generate() > 0
val pairs = new Generator[(Int,Int)]:
    def generate() = (integers.generate(),integers.generate())
```

We want to have the following definitions
```scala
val booleans = for x <- integers yield x > 0
def pairs[T,U](t: Generator[T], u: Generator[U]) = for x <- t; y <- u; yield (x,y)
```
In this case, the `for x <- g` is understood as any `x` which can be obtained 
from `g`. This expands to
```scala
val booleans = integers.map(x => x > 0)
def pairs[T,U](t: Generator[T], u: Generator[U]) = t.flatMap(x => u.map(y => (x,y)))
```

We can therefore define this more convenient version of `Generator`

```scala
trait Generator[T]:
    def generate(): T

    def map(f: T => S) = new Generator[S]:
        def generate() = f(this.generate())
    def flatMap(f: T => Generator[S]) = new Generator[S]:
        def generate = f(this.generate()).generate()
```

Which lets us use previous expressions.
```scala
val booleans = for x <- integers yield x > 0
def pairs[T,U](t: Generator[T], u: Generator[U]) = for x <- t; y <- u; yield (x,y)
```
And define many more generators
```scala
def single[T](x: T): Generator[T] = new Generator[T]: 
    def generate() = x
def range(lo: Int, hi: Int): Generator[Int] = for x <- integers 
    yield lo + x.abs% (hi-lo)

def oneOf[T](xs: T*) = for idx <- range(0, xs.length) yield xs(idx)

def lists: Generator[List[Int]] = 
    def emptyLists = single(Nil)
    def nonEmptyLists =
        for
            head <- integers
            tail <- lists
        yield head :: tail

    for
        isEmpty <- booleans
        list <- if isEmpty then emptyLists else nonEmptyLists
    yield list
```

> #### Exercise
> ##### Prompt
> Implement a generator that creates random `Tree` objects, defined as
> ```scala
> enum Tree:
>     case Inner(left: Tree, right: Tree)
>     case Leaf(x: Int)
> ```
> ##### Solution
> ```scala
> def trees = 
>     def leafs = for x <- integers yield Tree.Leaf(x)
>     def inners = for
>             left <- trees
>             right <- trees
>         yield Tree.Inner(left, right)
>     for
>         isLeaf <- booleans
>         tree   <- if isLeaf then leafs else inners
>     yield tree
> ```

We're going to use the random number generators to make a `test` function, 
which makes sure a certain function succeeds, by giving it random arguments
```scala
def test[T](g: Genreator[T], numTimes: Int = 100)
           (test: T => Boolean): Unit =
    for i <- 0 until numTimes do
        val value = g.generate()
        assert(test(value), s"test failed for value $value")
    println(s"passed $numTimes tests")
```

Which would be used as so:
```scala
test(pairs(lists,lists)){
    (xs, ys) => (xs ++ ys).length > xs.legth
}
```

With this implementation, instead of writing tests, we write properties that
are assumed to hold, and these are in turn checked for a random number of
inputs

This is implemented in the `ScalaCheck` tool
```scala
forAll { (l1: List[Int], l2: List[Int]) =>
    l1.size + l2.size == (l1 ++ l2).size
}
```

### Monads

These are structures that implement `map` and `flatMap` and follow some 
algebraic laws. But more officially:

A monad `M` is a parametric type with two operations
 * `flatMap: (M[T], T => M[U]) => M[U]`
 * `unit: T => M[T]`

In the literature, `flatMap` is also called `bind`

#### Examples
 * List is a monad with `unit(x) = List(x)`
 * Set is a monad with `unit(x) = Set(x)`
 * Option is a monad with `unit(x) = Some(x)`
 * Generator is a monad with `unit(x) = single(x)`

`map` can be defined as a combination of `flatMap` and `unit`
```scala
m.map(f) == m.flatMap(x => unit(f(x)))
```
Using the `andThen` infix operator (Which is the equivalent of mathematical
composition), we can also say
```scala
m.map(f) == m.flatMap(f andThen unit)
```

The monad laws are the following laws:
 1. **Associativity**:  
    `m.flatMap(f).flatMap(g) == m.flatMap(f(_).flatMap(g))`
 2. **Left unit**:  
    `unit(x).flatMap(f) == f(x)`
 3. **Right unit**:  
    `m.flatMap(f).flatMap(g) == m.flatMap(f(_).flatMap(g))`

_"Monads are just Monoids in the category of endofunctors"_

Monad-typed expressions are typically written as for expressions, so it's 
useful to see how these laws affect their use
 1. **Associativity**:  
    It basically says that one can `inline` nested for expressions
    ```scala
    for 
        y <- for x <- m; y <- f(x) yield y
        z <- g(y)
    yield z
    ```
    is equal to
    ```scala
    for
        x <- m
        y <- f(x)
        z <- g(y)
    yield z
    ```
 2. **Right unit**:  
    ```scala
    for x <- yield x
    ```
    is equal to
    ```scala
    m
    ```
 3. **Left unit**:  
    Actually, nothing relevant 
