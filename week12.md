Desugaring code is basically just replacing a code construct with an equivalent
expression using other features of the language

### Example
Having the following implementation of function definition and function calling
```scala
type Identifier = String
enum Expr:
    ...
    case Fun(arg, body)
    case Call(fun, arg)
    ...
```
We can derive an implementation for calling by name instead of calling by value
(that is, getting/giving expressions yet to be reduced as arguments instead of
the actual values). Since we're deferring the action of getting the value of the
arguments, we can encapsulate that deferred computation using functions
```scala
enum Expr:
    ...
    case FunByName(arg: Identifier, body: Expr)
    case CallByName(fun: Name, arg: Expr)

...

def desugar(expr: Expr) = expr match
    ...
    case FunByName(arg, body) = 
        Fun(arg,
            sust(body,
                arg,
                Call(Name(arg), C(0)) // Any parameter could be used instead
            )
        ) // Since the argument is expected to be a deferred computation, we
        // must get its value in every computation
    case CallByName(fun, arg) = 
        Call(fun,
            Fun("unused_argument", arg)
        ) // Instead of giving the argument, that value is received as a 
        // constant function that must be called to get it
```

When implementing recursivity without a naming convention, the information of 
the definition of the function itself must be given to the function as a 
parameter. Therefore, an implementation of a recursive function like `fibonacci`
takes itself as an argument.
```scala
def fibonacci(self)(n: Int): Int = if n == 0 
                                    then 1
                                    else self(self)(n-1) + self(self)(n-2)
```

The idea of taking a function and making it recursive can be abstracted in a
combinator, to be precise the **Y** combinator

```
λ f . (λ x . f (x x)) (λ x . f (x x))
```
