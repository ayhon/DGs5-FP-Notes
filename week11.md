# State

An object has a state if its behaviour is influenced by its history.

This doesn't mean that having a mutable variable (`var`) makes an object have
state and not having variables makes an object be stateless, although it usually 
turns out to be the case.


### Examples:
This definition of `LazyLists` doesn't have to be stateful if the rest of the 
program is purely functional
```scala
def cons[T](def head: T, tl: => TailLazyList[T]) = new TailLazyList[T]:
    def head = hd
    private var tlOpt: Option[TailLazyList[T]] = None
    def tail: T = tlOpt match
        case Some(x) => x
        case None => tlOpt = Some(tl); tail
```

This object has a state, although not directly (Through its closures)
```scala
class BankAccountProxy(ba: BankAccount):
    def deposit(amount: Int): Unit = ba.deposit(amount)
    def withdraw(amount: Int): Int = ba.withdraw(amount)
```

Having state breaks **referential transparency**, that is:

> "Expressions said to be equal can be substituted by one another without 
> changing evaluation"

#### Example of referential transparency
```scala
val x = E
val y = E
// since E = x, y = x
x == y
```
#### Example of breaking referential transparency
```scala
val x = BankAccount()
val y = BankAccount()
// This is now only true right after they have been instantiated (perhaps, they
// may hold a "bank account id" that breaks referential transparency even sooner)
x == y

x.deposit(100)
// Now referential transparency breaks, because the history of both elements 
// differ
x != y
```

Another example, the following expressions are not the same
```scala
y.deposit(30)
x.withdraw(20)
```
```scala
x.deposit(30)
x.withdraw(20)
```
Therefore, we say that `y` cannot be substituted by `x`, which means that 

## Control flow

We can implement the typical `while` and `for` loops in Scala using functions


### Examples of while
```scala
while(condition){
    statements
}
```
```scala
def while(condition: => Boolean)(command: => Unit): Unit =
    if condition then
        command
        whileDo(condition)(command)
    else ()
```

---

```scala
repeatUntil{
    statements
}(condition)
```
```scala
def repeatUntil(command: => Unit)(condition: => Boolean): Unit =
    if condition then ()
    else
        command
        repeatUntil(condition)(command)
```

---

```scala
repeat {
    statements
} until (condition)
```
```scala
def repeat(command: => Unit): Until = Repeat(command)
    
class Repeat(command: => Unit):
    infix def until(condition: => Boolean): Unit =
        if condition then ()
        else
            command
            repeat (command) until (condition)
```

#### Translation of for-loops
As with `for`-expressions, `for`-loops are but syntactic sugar over the use of
the method `foreach`. That is, in the same way that
```scala
for x <- xs yield x
```
translated to
```scala
xs.map(x => x)
```

We have that 
```scala
for i <- a until b do println(i)
```
translates to
```scala
(a until b).foreach(i => println(i))
```
