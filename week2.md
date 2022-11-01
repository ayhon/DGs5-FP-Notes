# Higher order functions
This means that functions can be taken as parameters and returned 
as a result

#### Example
Let's implement a function which takes the sum of numbers between
$a$ and $b$
```scala
def sumInts(a: Int, b: Int): Int = 
    if a > b then 0 else a + sumInts(a+1, b)
```
Now we decide to define a similar function, but which takes the
sum of the cubed numbers between $a$ and $b$
```scala
def cube(x: Int): Int = x * x * x
def sumCubes(a: Int, b: Int): Int = 
    if a > b then 0 else cube(a) + sumInts(a+1,b)
```
Imagine that now we wanted to do the same thing but with the `factorial`
function. It starts to get repetitive
```scala   
def fact(n: Int): Int = if n == 0 then 1 else n * fact(n-1)
def sumFactorials(a: Int, b: Int): Int = 
    if a > b then 0 else fact(a) + sumInts(a+1,b)
```
We want to be able to take the function we are applying to each integer
as a parameter
```scala
def sum(f: Int => Int, a: Int, b: Int): Int = 
    if a > b then 0
    else f(a) + 
```
The type `A => B` is the type of a function that takes a parameter of type
`A` and returns a value of type `B`.

Now we can do `sum(fact, 1, 10)` instead of `sumFact(1,10)`.

However, this convenience we get from being able to define `sum` generally
is overshadowed by the fact that we still need to define and name the 
function we are passing to the function. This is equivalent to us doing
the following when we want to print a string
```scala
val str = "Hello wordl"
println(str)
```
We want to have a way to define an anonymous function the same way we can
have literal strings (Not attached to any variable name). We achieve this
with the usage of lambda functions
```scala
(arg1: Type1, arg2: Type2) => expr
```
We could say that anonymous functions are actually a syntactic sugar for the
programmer that lets it not name a function if it's not important.

The types can be left out if they can be inferred

> #### Exercise
> ##### Prompt
> Replace the `???` to write a tail-recursive version of our past `sum` function
> ```scala
> def sum(f: Int => Int, a: Int, b: Int): Int =
>     def loop(a: Int, acc: Int): Int =
>         if ??? then ???
>         else loop(???,???)
>     loop(???,???)
> ```
> ##### Solution
> ```scala
> def sum(f: Int => Int, a: Int, b: Int): Int =
>     def loop(a: Int, acc: Int): Int =
>         if a > b then acc
>         else loop(a+1,acc+f(a))
>     loop(a,0)
> ```

### Currying
Because functions can return function, the past `sum` function could
be implemented like this
```scala
def sum(f: Int => Int): (Int, Int) => Int =
    def loop(a: Int, b: Int): =
        if a > b then 0 else f(a) + loop(a+1,b)
    loop
```
This construct is so common, we actually have some syntactic sugar to
simplify it in Scala
```scala
def sum(f: Int => Int)(a: Int, b: Int): Int =>
    if a > b then 0 else f(a) + sum(f)(a+1,b)
```
The parenthesis basically separate the parameters of the different functions
returned. This can be done with more than just one function being returned,
but with a function returning a function which in turn returns another function.

These are both equivalent
```scala   
def f(ps1)(ps2)(ps3)(ps4)...(psN) = E
def f = (ps1 => (ps2 => ... (psN => E) .. ))
```
This way of writing functions is called **currying**.

> #### Exercise
> ##### Prompt
>  1. Write a `product` function that calculates the product of the values of a
>     function for the points on a given interval
>  2. Write `factorial` in terms of product
>  3. Give a more general function which generalized both `sum` and `product`
> 
> ##### Solution
> ```scala
> // 1 //
> def product(f: Int => Int)(beg: Int, end: Int): Int =
>     if beg > end then 1 else f(beg) * product(f)(beg+1, end)
> 
> // 2 // 
> def factorial(n: Int) = 
>     if n == 0 then 1 
>     else product(x => x)(1,n)
> 
> // 3 //
> def applyInterval(f: Int => Int, operation: Int => Int, unit: Int)
>                  (beg: Int, end: Int): Int =
>     def recur(curr: Int): Int =
>         if curr > end then unit 
>         else operation(f(curr),recur(curr+1))
>     recur(beg)
> 
> def sum(f: Int => Int) = applyInterval(f, (x,y) => x+y, 0)
> def product(f: Int => Int) = applyInterval(f, (x,y) => x*y, 1)
> ```

### Example: Finding fixed point
We say that $x$ is a _fixed point_ of a function $f$ if $f(x) = x$

For some functions, it's enough with taking an initial value and
then applying the function repetitively until it ends on one point.

```scala
val tolerance = 0.0001

def isCloseEnough(x: Double, y: Double) =
    abs( (x-y)/x ) < tolerance

def fixedPoint(f: Double => Double)(firstGuess: Double): Double =
    def iterate(guess: Double): Double = 
        val next = f(guess)
        if isCloseEnough(guess, next) then guess
        else iterate(next)
    iterate(firstGuess)
```

We can use this idea to generalize the function `sqrt`, because
`sqrt(x)` is actually the number such that `sqrt(x) = x / sqrt(x)`.

That is, the result of `sqrt(x)` is finding the fixed point of the
function `y => x / y`
```scala
def sqrt(x: Int) = fixedPoint(y => x /y)(1.0)
```
This wouldn't converge though, because it'll rotate between 2 and 1
for `sqrt(2)`. To prevent this, we could use an average of successive 
values (the current and previous attempt) in  the `y => x / y`. That is,
implement it this way instead
```scala
def sqrt(x: Int) = fixedPoint(y => (x+y)/y/2)(1.0)
```
Then it'd actually work.

The idea of dampening the function given is actually pretty common.
Common enough to merit being abstracted as its own function
```scala
def averageDamp(f: Double => Double)(x: Double): Double = 
    (x + f(x))/2
```
So then, `sqrt` ends up being written as 
```scala
def sqrt(x: Int) = fixedPoint(averageDamp(f))(1.0)
```

### Scala Syntax Summary (S³)
We use Extended Backus-Naur form (EBNF) to give a context-free syntax
 * `|` denotes an alternative
 * `[...]` an option (0 or 1)
 * `{...}` a repetition (0 or more)

This is what we know for now of Scala
#### Types
```haskell
Type = SimpeType | FunctionType
FunctionType = SimpleType '=>' Type
             | '(' [Types] ')' '=>' Type
SimpleType = ident
Types = Type {',' Type}
```

#### Expressions
```haskell
Expr = InfixExpr | FunctionExpr
     | if Expr then Expr else Expr
InfixExpr = PrefixExpr | InfixExpr Operator InfixExpr
Operator = ident
PrefixExpr = ['+' |  '-' | '!' | '~' ] SimpleExpr
SimpleExpr = ident | literal | SimpleExpr '.' ident | Block
FunctionExpr = Bindings '=>' Expr
Bindings = ident | '(' [Binding {',' Binding}] ')'
Binding = ident [':' Type]
Block = '{' {Def ';'} Expr '}' 
      | <indent> {Def ';'} Expr <outdent>
```
#### Definitions
```haskell 
Def = FunDef | ValDef
FunDef = def ident {'(' [Parameters] ')'}
         [':' Type] '=' Expr
ValDef = val ident [':' Type]  '=' Expr
Parameters = Parameter {',' Parameter}
Parameter = ident ':' [ '=>' ] Type
```
### Functions and Data

We're going to learn how functions create and encapsulate data.

Let's start with the example of rational numbers

```scala
class Rational(x: Int, y: Int):
    def number = x
    def denom = y
```
This creates a new type `Rational`, and a new constructor `Rational(int, int)`

We call the elements of a class type objects. You can access the members of
an object using the infix operator `.`

With this we can now define the arithmetic functions that implement the standard
rules
$$
    \frac{n_1}{d_1} + \frac{n_2}{d_2} = \frac{n_1d_2+n_2d_2}{d_1d_2}\\\quad\\
    \frac{n_1}{d_1}-\frac{n_2}{d_2} = \frac{n_1d_2-n_2d_1}{d_1d_2}\\\quad\\
    \frac{n_1}{d_1}\cdot \frac{n_2}{d_2} = \frac{n_1n_2}{d_1d_2}\\\quad\\
    \frac{n_1}{d_1} \div \frac{n_2}{d_2} = \frac{n_1d_2}{d_1n_2}\\\quad\\
    \frac{n_1}{d_1} = \frac{n_2}{d_2} \iff n_1d_2 = d_1n_2\quad\\
$$
We could write the functions like so
```scala
def addRational(r: Rational, s: Rational): Rational = 
    Rational(
        r.numer * s.denom + s.numer * r.denom,
        r.denom * s.denom
    )
def makeSting(r: Rational): String = 
    s"${r.numer}/${r.denom}"
```
> ### Interpolated strings
> In Scala, `s"..."` is an interpolated string. This are regular strings
> that recognize the expressions inside the pattern `${}` as expressions
> that return a string, which must be used inside. If the expressions are
> just a simple variable, the `{}` may be omitted. 

We can also package the relevant methods inside class itself. These are
called methods
```scala
class Rational(x: Int, y: Int):
    def numer = x
    def denom = y
    def add(r: Rational) = 
        Rational(numer * r.denom + r.numer * denom, 
                 denom * r.denom)
    def mul(r: Rational) = ...
    ...
    override def toString = s"$numer/$denom"
```
The `toString` method is a default method that Scala classes use
when they need to transform a class into a string. To override
it, redefine it prepending the `override` keyword modifier.

#### Exercise
##### Prompt
 1. Add a method `neg` to the class Rational that is used like so
    ```scala
    x.neg // evaluates to -x
    ```
 2. Add a method `sub` to subtract two rational numbers
 3. With the following values:
    ```scala
    val x = Rational(1,3)
    val y = Rational(5,7)
    val z = Rational(3,2)
    ```
 what is the result of `x - y - z`
##### Solution
```scala
class Rational(x: Int, y: Int):
    def numer = x
    def denom = y

    def add(r: Rational) = 
        Rational(numer * r.denom + r.numer * denom, 
                 denom * r.denom)

    def mul(r: Rational) = 
        Rational(numer * r.numer,
                 denom * r.denom)

    // 1 //
    def neg = Rational(-numer, denom)

    // 2 //
    def sub(r: Rational) = add(r.neg)

    override def toString = s"$numer/$denom"
```
The result of `x - y - z` would be 
```scala
scala> x.sub(y).sub(z)
val res0: Rational = -79/42
```

#### Data abstraction

For our previous example, we would like to have the 
rationals be represented by a simplified version. For this
we could define a private member, `gcd`. We do so with the
`private` modifier
```scala
class Rational(x: Int, y: Int):
    private def gcd(a: Int, b: Int): Int =
        if b == 0 then a else gcd(b, a%b)
    def numer = x/gcd(x,y)
    def denom = y/gcd(x,y)
```
Also, if you expect members to be called often, you can make
their definitions using `val` to make sure the computation
is done once

The keyword `this` inside the method of a class is the instance
of the class from which the method was called.

We can enforce requirements from a function using the function
`require`, which takes a condition and an optional error message
```scala
class Rational(x: Int, y: Int):
    require(y > 0, "denominator must be positive")
    ...
```
A similar function is `assert`, except that `require` throws an
`IllegalArgumentException` and `assert` an `AssertionException`.
This is because:
 * `require` is used to enforce a precondition on the caller function
 * `assert` is used to check the code of the function itself

##### Constructors
In Scala, a class implicitly introduces a constructor. This is called
the primary constructor of the class.

More constructors can be added in the form of methods named `this`. You
can't give these methods a return type, and the very first statement
must be calling other constructor.
```scala
class Rational(x: Int, y: Int):
    require(y > 0, "denominator must be positive")

    private def gcd(a: Int, b: Int): Int =
        if b == 0 then a else gcd(b, a%b)
    val g = gcd(x,y)
    val numer = x/g
    val denom = y/g

    def this(x: Int) = 
        this(x, 1)
```

### Evaluation and Operators
In the model we had, were a function was but a substitution of
values, classes with parameters are already considered values
themselves. The question now is, given a class `C` with a method
`f`: 
```scala
def C(v1, ..., vN):
    ...
    def f(w1, ..., wN):
        b
    ...
```
how would we understand the expression
```scala
C(x1,...,xN).f(y1, ..., yN)
```
Well, there are 3 substitutions happening
 1. The parameters of the constructor `x1` substitute the references to `v1` and
    so on
 2. The parameters of the method `y1` substitute the references to `w1` and so 
    on
 3. The appearances of `this` in the body of the method with `C(v1,...,vN)`

#### Extension methods
Methods that don't require access to the internals of a class are called
extension methods.
```scala
extension (r: Rational)
    def min(s: Rational): Boolean = if s.less(r) then s else r
    def abs: Rational = Rational(r.numer.abs, r.denom)
```
With extension methods, we can add new members, but there are some caveats:
 * You cannot override existing methods
 * Extensions cannot refer to other class members via this

The idea is that extensions add functionality to a class without modifying
the original functionality of the class

The way we reconcile this functionality with the substitution model we had
beforehand is by changing the last substitution from affecting `this` to the
extension parameter

#### Operators
Identifiers in Scala can be:
 * Alphanumeric, if they start with a letter
 * Symbolic, if they start with an operator symbol, followed by other operator
   symbols
Where `_` counts as a letter and alphanumeric identifiers can end in an 
underscore followed by operator symbols

Here are some example identifiers
```scala   
x1
*
+?%&
vector_++
counter_=
```
Since operators are identifiers, we can use them as method names

Also, an operator method (symbolic identifier) with a single parameter can 
be used as an infix operator, and alphanumeric methods to if they are preceded
by an `infix` modifier

The precedence of an operator is determined by its first character. In 
increasing order of priority:
| Operator by increasing order of priority |
|------------------------------------------|
|                    `|`                   |
|                    `^`                   |
|                    `&`                   |
|                  `<` `>`                 |
|                    `=` `!`               |
|                    `:`                   |
|                    `+` `-`               |
|                    `*` `/` `%`           |
|       all other special characters       |

#### Exercise
##### Prompt
Provide a fully parenthesized version of
```scala
a + b ^? c ?^ d less a ==> b | c
```
##### Solution
```scala
((a + b) ^? (c ?^ d)) less ((a ==> b) | c)
```
