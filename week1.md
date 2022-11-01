# Elements of programming

Every programming language provides ways to:
 * primitive expressions (types)
 * ways to combine expressions (operator)
 * ways to abstract expressions (functions)

Scala comes with a REPL (Read-eval-print loop), and it
sometimes helps thinking of functional programming languages
as really fancy calculators.

```scala
scala> 87 + 145
res0: Int = 232

scala> def size = 2
size: Int

scala> 5 * size
res1: Int = 10
```

Assignment is achieved with the keyword `def`. 

Any non-primitive expression is evaluated as follows
 1. Take the leftmost operator
 2. Evaluate its operands (left before right)
 3. Apply operator to the operands

In definitions, we can define functions with parameters
```scala
scala> def square(x: Double) = x * x
square: (x: Double)Double

scala> square(2)
res2: Double = 4.0
```

#### Primitive types in scala
| Name | Description |
| Int|32-bit integer|
| Long|64-bit integers|
| Float|32-bit floating point numbers|
| Double|64-bit floating point numbers|
| Char|16-bit unicode characters|
| Short|16-bit integers|
| Byte|8-bit integers|
| Boolean|boolean values `true` and `false`|

#### Functions as expressions in Scala
In Scala, recursive functions must specify its return type
```scala
def funcname(arg1: ArgType, ...): RetType = ...
```
This also follows the rules of the non-primitive expressions.
The function "application" basically replaces the rhs of the function,
with the now appropiate arguments, in place of the function call.

This is key in $\lambda-calculus$

**The interpreter reduces function arguments before
rewritting the function application.** This means that
argument expressions are evaluated before the function
call. This is called **C**all **B**y **V**alue (CBV).
The opposite would be **C**all **B**y **N**ame (CBN)

When we pass arguments to a function, Scala by default
uses CBV. If we want to use CBN, we must say so explicitely
in the type of the argument we wish to lazily evaluate, by
starting it with `=>`
```scala
def funcname(byValue: Type, byName: => Type): RetType = ...
```
#### Assignment

The `def` keyword can be used to assign a constant, where the
value won't be evaluated before the assignment. We could say
`def` works like CBN.

The CBV equivalent of `def` is `val`, which evaluates expressions
before assignment
```scala
val x = 1 / 0 // fail
def x = 1 / 0 // allowed
```

#### Control flow
In Scala, the `if ... then ... else ...` construction is an expression.
This is equivalent to C's ternary operator `... ? ... : ...`.
```scala
def abs(x: Double): Double = if x > 0 then x else -x
```

#### Example: `sqrt` function implementation
```scala
def abs(x: Double): Double = if x > 0 then x else -x
def square(x: Double): Double = x * x
def sqrtIter(guess: Double, x: Double): Double =
	if isGoodEnough(guess,x) then guess
	else sqrtIter(improve(guess,x),x)
def isGoodEnough(guess: Double, x: Double): Boolean = 
	abs(square(guess) - x) < 0.001
def improve(guess: Double, x: Double): Double = 
	(guess + x/guess)/2
def sqrt(x: Double) = sqrtIter(1.0, x)
```

#### Improving code visibility

Taking the past example, let's improve it.

For example, we have many auxiliary functions that we don't want the user to
access. We can hide this functions by including them inside the `sqrt`
function

```scala
def abs(x: Double): Double = if x > 0 then x else -x
def square(x: Double): Double = x * x

def sqrt(x: Double) = {
	def sqrtIter(guess: Double, x: Double): Double =
		if isGoodEnough(guess,x) then guess
		else sqrtIter(improve(guess,x),x)

	def isGoodEnough(guess: Double, x: Double): Boolean = 
		abs(square(guess) - x) < 0.001

	def improve(guess: Double, x: Double): Double = 
		(guess + x/guess)/2

	sqrtIter(1.0, x)
}
```

A block is delimited by braces, and it can contain a sequence of 
definitions or expressions. The last statement of a block in an expression
defines the value of the block. **A block is considered an expression, so it
can appear anywhere an expression can appear.**

In Scala3, the braces are optional if indentation makes it obvious.

Definitions inside a block are only visible within the block and shadows 
outside definitions. 

> #### Exercise
> ###### What does this output?
> ```scala
> val x = 0
> def f(y:Int) = y + 1
> val y = 
> 	val x = f(3)
> 	x * x
> val result = y + x
> ```
> ###### Result
> 16

**In a block, outside values that are not shadowed are visible.**

```scala

def abs(x: Double): Double = if x > 0 then x else -x
def square(x: Double): Double = x * x

def sqrt(x: Double) = {
	def sqrtIter(guess: Double): Double =
		if isGoodEnough(guess) then guess
		else sqrtIter(improve(guess))

	def isGoodEnough(guess: Double): Boolean = 
		abs(square(guess) - x) < 0.001

	def improve(guess: Double): Double = 
		(guess + x/guess)/2

	sqrtIter(1.0, x)
}
```

An statement is either a definition or an expression

#### Semicolons `;`
The semicolon is used as a statement delimiter. You can put many statements
in the same line by separating them with a `;`. 
```scala
val f(x:Int):Int = x+10; f(10)
```
#### Tail recursion and looping
In functional programming languages, recursion is a pretty common way to
iterate over values. There is not much looping, but the equivalent way to 
achieve that is with tail recursion.

As we've seen, we can understand Scala execution as a reduction of 
expressions when they are evaluated.
```scala
def gcd(a: Int, b:Int):Int = 
	if a % b == 0 then b else gcd(b, a % b)
```
```scala
def factorial(n: Int): Int = 
	if n == 0 then 1 else n * factorial(n-1)
```

In the case of `gcd`, there is no operations after the recursive call, but
in the case of `factorial`, we still have to multiply by `n`. 

We say that `gcd` is a tail recursion. For tail recursion, we don't occupy
the stack, which let's us do longer loops.

In general, if the last action of a function is calling another function, 
it's a tail call

You can use `@tailrec` to require a function to be tail recursive. That way,
if the compiler notices that a function with `@tailrec` is not tail 
recursive, it'll throw an error. `tailrec` is found in the `scala.annotation`
package.
```scala
import scala.annotation.tailrec
```

#### EXERCISE: Design a tail recursive version of factorial
```scala
@tailrec
def factorialInter(x: Int, acc: Int): Int = 
	if x == 0 then return acc else factorialInter(x-1,acc*x)
def factorial(n: Int): Int = factorialInter(n,1)
```

