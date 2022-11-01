# Case classes and subtyping

### Pattern matching

We're trying to find a way to access heterogeneous data in a class hierarchy.

We've discarded:
 * Classification and access methods (quadratic explosion)
 * Type tests and casts (unsafe, low-level)
 * Object-oriented decomposition (Causes coupling between data and operations,
   need to touch all classes to add a new method)

Seen in examples:
##### Classification and access methods
```scala
def eval(e: Expr): Int =
    if e.isNumber then e.numValue
    else if e.isSum then eval(e.leftOp) + eval(e.rightOp)
    else throw Error(”Unknown expression ” + e)
```
##### Type tests and casts
```scala
def eval(e: Expr): Int =
    if e.isInstanceOf[Number] then
        e.asInstanceOf[Number].numValue
    else if e.isInstanceOf[Sum] then
        eval(e.asInstanceOf[Sum].leftOp) + eval(e.asInstanceOf[Sum].rightOp)
    else throw Error(”Unknown expression ” + e)
```
##### Object-oriented decomposition
```scala
trait Expr:
    def eval: Int

class Number(n: Int) extends Expr:
    def eval: Int = n

class Sum(e1: Expr, e2: Expr) extends Expr:
    def eval: Int = e1.eval + e2.eval
```

We observe that the only purpose of test and accesssor functions is to `reverse`
the construction process:
 * Which subclass we used?
 * What were the arguments of the constructor?

### Case classes
A _case class_ is similar to a normal class definition, except that it's 
preceded by the modifier `case`.
```scala
trait Expr
case class Number(n: Int) extends Expr
case class Sum(e1: Expr, e2: Expr) extends Expr
```
we can now access the members of the classes using pattern-matching, with the
keyword `match`
```scala
def eval(e: Expr): Int = e match
    case Number(n) => n
    case Sum(e1, e2) => eval(e1) + eval(e2)
```

The `match` statement is preceded by a selector expression (like a `case 
class`), and followed by a sequence of `cases pat => expression`. 

If no pattern matches the value of the selector, a `MatchError` is thrown.

> ##### Forms of patterns
> Patterns are constructed from:
>  * Constructors (`Number`)
>  * Variables (`n`)  
>    Always begin with lowercase letter
>  * Wildcard patterns (`_`)
>  * Constants (`1`)  
>    Begin with capital letters except `true`, `false` and `null`
>  * Type tests (`n: Number`)
> 
> #### Exercise
> ##### Prompt
> Write a function `show` that uses pattern matching to return the representation
> of a given expressions as a string.
> ```scala
> def show(e: Expr): String = ???
> ```
> ##### Solution
> ```scala
> def show(e: Expr): String = e match
>     case Number(n) => s"$n"
>     case Sum(a,b)  => show(a) + "+" + show(b)
> ```

#### Exercise
##### Prompt
Add case classes `Var` for variables `x` and `Prod` for products `x * y` as
discussed previously.

Change your `show` function so that it also deals with products.

Pay attention you get operator precedence right but to use as few
parentheses as possible.
##### Solution
```scala
def show(x: Expr): String = x match
    case Number(n) => s"$n"
    case Sum(a,b)  => s"${show(a)} + ${show(b)}"
    case Var(x)    => x
    case Prod(a,b) => s"${showParenthesis(a)} * ${showParenthesis(b)}"
    def showParenthesis(e: Expr): String = e match
        case e: Sum => s"(${show(e)}"
        case _      => show(e)
```

### Lists
#### Lists vs Arrays
 * Lists are immutable, arrays aren't
 * Lists are recursive, arrays are flat

Lists are homogeneous

They are composed of:
 * The empty list `Nil`
 * The construction operator `::` (Pronounced `cons`)

#### Note
It is a convention in Scala that operators ending in `:` are right
associative

#### Insertion sort
```scala
def isort(xs: List[Int]): List[Int] = xs match
    case Nil     => Nil
    case y :: ys => insert(y, isort(ys))

def insert(x: Int, xs: List[Int]): List[Int] = xs match
    case Nil     => List(x)
    case y :: ys => 
        if y < x then
            y :: insert(x,ys)   
        else
            x :: xs
```

### Enums
They are a way to abstract the `case class`s. It's functionally equivalent
to 
```scala
trait Expr
object Expr:
    case class Var(s: String) extends Expr
    case class Number(n: Int) extends Expr
    case class Sum(c1: Expr, c2: Expr) extends Expr
    case class Prod(c1: Expr, c2: Expr) extends Expr

```
but written more succinctly
```scala
enum Expr:
    case Var(s: String) 
    case Number(n: Int)
    case Sum(c1: Expr, c2: Expr)
    case Prod(c1: Expr, c2: Expr)
```
This are known as **A**lgebraic **D**ata **T**ypes, or ADTs for short.

We can also combine several simple cases in one list
```scala
enum DayOfWeek:
    case Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
```
They can also take parameters and define methods.
```scala
enum Direction(val dx: Int, val dy: Int):
    case Right extends Direction( 1, 0)
    case Up    extends Direction( 0, 1)
    case Left  extends Direction(-1, 0)
    case Down  extends Direction( 0,-1)
    
    def leftTurn = Direction.values((ordinal + 1)%4)
```
However, enumeration cases that pass parameters must use an explicit extends 
clause.

### Subtyping and generics
Like `s: Int` means that `s` must be an `Int`, we can use `>:` and `<:` to 
indicate that a variable must be either a superset or a subset of a given
class, respectively. 

You can also mix lower and upper bounds to define a range of classes
```scala
[S >: NonEmpty <: IntSet]
```
Be careful though, `A <: B` doesn't imply `C[A] <: C[B]`.

It it does happen, we say that C is covariant.

### Variance
Roughly speaking, a type that accepts the mutation of its elements should not
be covariant. Immutable types can be covariant, if some conditions on methods
are met.

Given `C[T]` and `A <: B` then:
 * If `C[A] <: C[B]` , `C` is covariant.  
   This is show in Scala by using a `+` in the type generic  
   ```scala
   class C[+T]
   ```
 * If `C[A] >: C[B]` , `C` is contravariant.  
   This is show in Scala by using a `-` in the type generic  
   ```scala
   class C[-T]
   ```
 * Else, `C` is nonvariant  
   This is the default behaviour

> #### Exercise
> ##### Prompt
> Assume that the following typ hierarchy and two function types:
> ```scala   
> trait Fruit
> class Apple extends Fruit
> class Orange extends Fruit
> 
> type FtoO = Fruit => Orange
> type AtoF = Apple => Fruit
> ```
> Which of the following should be true:
>  - [ ] `FtoO <: AtoF`
>  - [ ] `AtoO <: FtoF`
>  - [ ] `FtoO` and `AtoF` are unrelated
> ##### Solution
>  - [X] `FtoO <: AtoF`
>  - [ ] `AtoF <: FtoO`  
>  - You can't give `AtoF` an `Orange`, but `FtoO` accepts it
>  - [ ] `FtoO` and `AtoF` are unrelated

In general we have:  
_If `A2 <: A2` and `B1 <: B2`, then `A1 => B1 <: A2 => B2`_

So functions are:
 * `contravariant` in their argument type
 * `covariant` in their result type

Which leads to the following revised definition of the `Function1` trait
```scala
trait Function[-T,+U]:
    def apply(x: T): U
```
We're basically saying that the first parameter is `T`, although something 
"stronger" would be fine, and that the second parameter should be `U`, although
something "weaker" would be fine.

In general:
 * Covariant type arguments can only appear in return types
 * Contravariant type arguments can only appear in argument types
 * Nonvariant type arguments can appear anywhere

> #### Exercise
> ##### Prompt
> Why does the following code not type-check?
> ```scala
> trait List[+T]:
>     def prepend(elem: T): List[T] = ::(elem, this)
> ```
> Possible answers:
>  - [ ] `prepend` turns List into a mutable class.
>  - [ ] `prepend` fails variance checking.
>  - [ ] `prepend`'s right-hand side contains a type error.
> ##### Solution
>  - [ ] `prepend` turns List into a mutable class.
>  - [X] `prepend` fails variance checking.  
>    As `Apple <: Fruit` but `List[Apple] <: List[Fruit]` because `List[Fruit]` 
>    accepts `Orange` but `List[Apple]` not.
>  - [ ] `prepend`'s right-hand side contains a type error.

To make `prepend` covariant, we can use lower bounds.

> #### Exercise
> ##### Prompt
> Assume prepend in trait List is implemented like this:
> ```scala
> def prepend [U >: T] (elem: U): List[U] = ::(elem, this)
> ```
> What is the result type of this function:
> ```scala
> def f(xs: List[Apple], x: Orange) = xs.prepend(x)
> ```
> Possible answers:
>  - [ ] does not type check
>  - [ ] List[Apple]
>  - [ ] List[Orange]
>  - [ ] List[Fruit]
>  - [ ] List[Any]
> ##### Solution
>  - [ ] does not type check
>  - [ ] List[Apple]
>  - [ ] List[Orange]
>  - [X] List[Fruit]
>  - [ ] List[Any]
