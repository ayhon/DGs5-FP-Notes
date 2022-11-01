# Type-directed computation

Type inference is getting types out of values

Term inference is getting values out of types
 
## Context abstraction
**Context**: What comes with the text but is not in the text
> Examples:
>  * The current configuration
>  * The current scope
>  * The meaning of `<` on this type
>  * The user on behalf of which the operation is performed (Linux)
>  * The security level in effect

So far context is represented in:
 * Global values
 * Global mutable variables
 * ["Monkey patching"](https://en.wikipedia.org/wiki/Monkey_patch)
 * Dependency injection frameworks

## Implicit parameters
```scala
def sort[T](xs: List[T])(using ord: Ordering[T]): List[T] = ...
```
We know that for an expression, Scala can infer the type of a function, like
for example
```scala
def sort[T](ls: List[T]) = ...
sort[Int](List(1,2,3,5,5))
sort[String](List("hey", "ho","lets", "go", "!"))
```
It does this by asking the context what type it should have. That is, it gets
the type that makes most sense, by taking in consideration the parameters it 
took.

Therefore, Scala can answer `sort(List(1,2,3,5))`, but for the function
```scala
def foo[T](x: T, y: T) = (x,y)
```

It has no answer to `foo(10, "hello")`

Scala can do something similar for expressions as well. That is, if an 
expression is lacking, asking the context if there is one obvious value it can
take. Scala will only ask the context if the argument itself declares that it
can be implicitly defined, that is, was defined with the keyword `using`

#### The `using` clause

Multiple parameters can appear, and different clauses can be mixed in. But we 
cannot have parameters with `using` interleaved with parameters without `using`,
that is, explicit and implicit parameters cannot be mixed.
```scala
def f(x:Int)(using a: A, b: B) = ...
```

Implicit parameters can be anonymous (Useful if they are only used to pass on to
other functions)

If you want to pass an implicit argument explicitly, the `using` keyword must be
used.

You can also use **Context bounds** to shorten anonymous implicit arguments 
```scala
def f[T: U1 : U2 : U3](ps)
def f[T](ps)(using U1[T], U2[T], U3[T])
```

#### `given` instances
To make a value eligible for being implicitly guessed, it must be declared using
the `given` keyword, instead of `val`, `lazy val` or `def`.

```scala
scala> given x: String = "Hello world"
lazy val x: String

scala> def foo(using s: String) = s
def foo(using s: String): String

scala> foo
val res0: String = Hello wolrd

scala> given y: String = "Noooooo"
lazy val y: String

scala> foo
-- Error:
1 |foo
  |   ^
  |ambiguous implicit arguments: both given instance y and given instance x match type String of parameter s of method foo

scala>
```

You can create anonymous given instances of classes directly, the compiler will
take care of giving it a name
```scala
given Ordering[X] with
    def compare(a: X, b: X): Int = ...
```

You can explicitly query for an implicit parameter (that's a mouthful), by using
the constant function `summon[Type]`, defined as:

```scala
def summon[T](using x: T) = x
```

## Searching given instances
It includes all the **given instances** that are visible and those found in a
companion object _associated_ with with T, that is
 * Companion objects associated with any of `T`'s inherited types
 * Companion objects associated with any type argument in `T` 
 * If `T` is an inner class, the outer objects in which it is embedded

Given
```scala
trait Foo[T]
trait Bar[T] extends Foo[T]
trait Baz[T] extends Bar[T]
trait X
trait Y extends X
```
When queried for `Bar[Y]`, `Bar`, `Y`, `Foo` and `X` will be looked into.

```
        ┌───┐
        │Foo│
        └───┘
          ▲              ┌─┐
          │              │X│
        ┌─┴─┐            └─┘
        │Bar│             ▲
        └───┘             │
          ▲              ┌┴┐
          │              │Y│
        ┌─┴─┐            └─┘
        │Baz│
        └───┘
```

### Importing given instances
You can import them by name, or using
```scala
import package.given
```
If you want to specify a given for a specific type, use
```scala
import package.{given Type]
```
If there are two possible implicit objects, the ambiguity is reported. There is
no ambiguity if one object is more precise than the other, that is:
 * `a` is in a closer lexical scope than `b`  
   ```scala
   scala> def test =
     |   given b: Int = 10
     |   def scope =
     |     given a: Int = 20
     |     summon[Int]
     |   scope
     |
   def test: Int

scala> test
val res3: Int = 20
   ```
 * `a` is defined in a class or object which is a subclass of the class defining `b`  
```scala
``scala> def test =
     |   class X:
     |     given b : Int = 10
     |   class Y extends X:
     |     given a : Int = 20
     |   object O extends Y:
     |     def n = summon[Int]
     |   O.n
     |
def test: Int

scala> test
val res3: Int = 20
   ```
 * type `A` is a generic instance of type `B`
 ```scala
scala> def test =
     |   given b[T](using x: T): List[T] = List(x)
     |   given a: List[Int] = List(1)
     |   summon[List[Int]]
     |
def test: List[Int]

scala> test
val res4: List[Int] = List(1)
 ```
 * type `A` is a subtype of type `B`
```scala
scala> def test =
     |   given b: Double = 10
     |   given a: Int = 20
     |   summon[Int]
     |
def test: Int

scala> test
val res0: Int = 20
```

### Type classes
A type class is a generic trait that comes with given instances for type 
instances of that trait. For example, the `Ordering` class
```scala
trait Ordering[T]
    def compare(a: T, b: T): Boolean
object Ordering
    given Ordering[Int] with
        def compare(a: Int, b: Int) = a < b
    given Ordering[String] with
        def compare(as: String, bs: String) = ...
```

These are yet another form of polymorphism (Called ad hoc polymorphism)

> #### Exercise
> ##### Prompt
> Implement an instance of the `Ordering` type class for the `Rational` type.
> ```scala
> case class Rational(num: Int, denom: Int)
> ```
> ##### Solution
> ```scala
> given Ordering[Rational] with
>     def compare(a: Rational, b: Rational)(using int: Ordering[Int]): Boolean = (a,b) match
>         case (Rational(a,b), Rational(c,d)) => int.compare(a*d,b*c)
> ```

### Conditional instances
Let's us define special instances if certain conditions apply.
```scala
given listOrdering[A](using elem_ord: Ordering[A]): Ordering[List[A]] with
    def compare(xs: List[A], ys: List[A]) = (xs, ys) match
        case (Nil, Nil) => 0
        case (Nil ,_) => -1
        case (_, Nil) => 1
        case (x :: xs1, y :: ys1) =>
            val c = elem_ord.compare(x,y)
            if c != 0 then c else compare(xs1, ys1)
```

The name can be left out for anonymous given values.

#### Exercise
##### Prompt
Implement an instance of the `Ordering` type class for pairs of type `(A, B)`,
where `A`, `B` have Ordering instances defined on them.
##### Solution
```scala
given pairOrdering[A,B](using a: Ordering[A] b: Ordering[B]): Ordering[(A,B)] with
    def compare(px: (A,B), py: (A,B)) =
        val c = a.compare(px._1, py._1)
        if c == 0 then
            b.compare(px._2, py._2)
        else c
```

A type class may define extension methods. If a type class is in scope, the 
extension methods defined in that class are eligible to be called.


### Abstract algebra with type classes

A semigroup in abstract algebra is any set with a binary associative operation
```scala
trait SemiGroup[T]:
    extension (x: T) def combine(y: T): T
```

Then, we can define traits that work for all semigroups

```scala
def reduce[T: SemiGroup](xs: List[T]): T =
    xs.reduceLeft(_.combine(_))

```

A monoid is a semigroup with a left-and-right unit element

```scala
trait Monoid[T] extends SemiGroup[T]:
    def unit: T
```

> #### Exercise
> ##### Prompt
> Generalize `reduce` to work on lists of T where T has a Monoid instance such
> that it also works for empty lists.
> ##### Solution
> ```scala
> def reduce[T](xs: List[T])(using mon: Monoid[T]): T =
>     xs.foldLeft(mon.unit)(_.combine(_))
> ```

> #### Exercise
> ##### Prompt
> Given
> ```scala
> given sumMonoid: Monoid[Int] with
>     extension (x: Int) def combine(y: Int) : Int = x + y
>     def unit: Int = 0
> 
> given prodMonoid: Monoid[Int] with
>     extension (x: Int) def combine(y: Int) : Int = x * y
>     def unit: Int = 1
> ```
> Define `sum` and `prod` in terms of `reduce`
> ##### Solution
> ```scala
> def sum(xs: List[Int]): Int = reduce(xs)(using sumMonoid)
> def prod(xs: List[Int]): Int = reduce(xs)(using prodMonoid)
> ```

#### Typeclass laws
Algebraic type classes are not just defined by their type signatures but also
by the laws that hold for them. For example, these are the monoid laws:
 * `x.combine(y).combine(z) == x.combine(y.combine(z))` (Associativity)
 * `unit.combine(x) == x` (Left neutral element)
 * `x.combine(unit) == x` (Right neutral element)

### Opaque type aliases
Given `type A = B`, to make the property `A = B` only be known inside the scope
where `type A = B` was used, use the keyword `opaque`

> #### Exercise
> ##### Prompt
> You have seen in week 4 an `enum` for arithmetic expressions. Let’s augment
> it with a `Let` form:
> ```scala
> enum Expr:
>     case Number(num: Int)
>     case Sum(x: Expr, y: Expr)
>     case Prod(x: Expr, y: Expr)
>     case Var(name: String)
>     case Let(name: String, rhs: Expr, body: Expr)
> import Expr._
> ```
> Write an eval function for expressions of this type.
> ```scala
> def eval(e: Expr): Int = ???
> ```
> Let(”x”, e1, e2) should be evaluated like {val x = e1; e2}.
> You can assume that every Var(x) occurs in the body b of an enclosing
> Let(x, e, b).
> 
> ##### Solution
> ```scala
> // 😀 I really like my implementation
> opaque type Environment: Map(Var,Number)
> def eval(e: Expr)(using env: Environment = Map()): Int = 
>     e match
>         case Number(n) => n
>         case Sum(x,y)  => eval(x) + eval(y)
>         case Prod(x,y) => eval(x) * eval(y)
>         case Var(name) => eval(env(e))
>         case Let(name, rhs, body) => eval(body)(using env ++ Var(name) -> Number(eval(rhs)))
> ```

### Lambdas with `using` clauses
We can create lambdas that use as arguments implicit values by defining them with
`?=>` instead of `=>`, both in the type definition and the lambda definition.
```scala
scala> val f: Int ?=> Int = (x: Int) ?=> x * x
val f: (Int) ?=> Int = Lambda$1429/0x00000008406f4840@56b48163
```
