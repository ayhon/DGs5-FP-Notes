# Exercise 3.1 - Variance (6 points)

Consider the following trait:

```scala
trait Transform[-A, +B]:
	def apply(x: A): B
	def map[C](f: A => C): Transform[A, C]
	def followedBy[C](t: Transform[B, C]): Transform[A, C]
```

It contains an error, where a type parameter is not used at its correct variance.

 1. Which parameter is it and in which method does it occur?

 2. Can you come up with a modified method signature that is variance correct?

## Context

Given some types `X`, `Y` with a relationship (Like being a subtype, or supertype of one
another), considering the variance of those types with respect to a type 
transformation `T` is but asking _"what can we know about the relationship  of 
`T[X]` and `T[Y]` knowing the relationship of `X` and `Y`"?_.

A **type transformation** is basically a class that takes a type
as an argument. For example, `List` is not a type, as it requires the type
of the elements it will contains. It is a type transformation, it takes a type
and forms another different (but related) type. `List[Int]`, on
the other hand, **is** a type.

_To be clear, **type transformation** is a term I've been made up. I don't know if
 it's used anywhere else, but I've found it to convey more meaning, and that's why
I'll keep using it. In our notes, the term **parametrized type** is used instead._

Asking about variance is equivalent to asking about what relationship is there 
between a type and its transformed type. What properties from `X` does `T`
preserve in `T[X]`? In particular, we wonder about what happens to sutyping
relationships.

We write `X <: Y` or `Y >: X` to say that `X` is a **subtype** of `Y`. In that case,
`Y` is a **supertype** of `X`.

If `X <: Y`, that means that everything you can do with `Y`, you can do with `X`,
or in more "scientific" terms, everywhere a type `Y` is needed, we can use `X`
instead. I reflect on this to explain that, going back to the "real" world, if we have
two classes `A` and `B`, saying `A <: B` means being able to use `A` everywhere 
`B` is needed, which menas all the operations (And by operations I'm specifically
referring to a classes' methods) I can do with `A` must be able to be used in place
of the same method in `B`. To be more precise, if `A <: B`, then the signatures of
the methods of `A` must also be subtypes of the signatures of the methods of `B`,
since when replacing `A` for `B`, I must be able to use them everywhere a method
of `B` is used.

 * If a transformation `T` is covariant, it preserves the relationship between its 
type parameters. 
   ```
   X <: Y  ------>  T[X] <: T[Y]
   ```

 * If a transformation `T` is contravariant, it inverts the relationship between its
type parameters
   ```
   X <: Y  ------>  T[X] >: T[Y]
   ```

 * If a transformation `T` is invariant, it doesn't preserve any relationship at all.
   ```
   X <: Y  ------>  Nothing. Neither T[X] <: T[Y] nor T[X] >: T[Y]
   ```

Finally, let's look at an example of a type transformation which is ubiquitous
in functional programming: functions (It's in the name!). To be precise, the 
transformations I'm talking about are `=> C` and `C =>`, that is:

 1. `=> C`: Given a type `X`, the functions that takes `X`. I'll call this the 
      **argument type transformation**.

 2. `C =>`: Given a type `Y`, the functions that return `Y`. I'll call this the
      **return type transformation**.

Let's assume that `X <: Y`.

1. For the **argument type transformation**, we observe that if a function takes 
   arguments of type `Y`, it can also take arguments of type `X` (Since `X` can
   be used everywhere `Y` is used, `X <: Y`), which means that every function 
   `Y => C` can be used in place of functions `X => C`, which the definition of
   being a subtype. That is
   ```
   X <: Y  ------>  (X => C) >: (Y => C)
   ```
   This means that the argument type transformation is contravariant.

2. For the **return type transformation**, we observe that if a function returns a
   value of type `X`, then it's also returning a value of type `Y` (Since `X` can
   be used everywhere `Y` is used, `X <: Y`), which means that every function 
   `C => X` can be used in place of function `C => Y`, which is (again) the 
   definition of being a subtype. That is
   ```
   X <: Y  ------>  (C => X) <: (C => Y)
   ```
   This means that the return type transformation is covariant.

## Solution

### Part 1: Which parameter is it and in which method does it occur?

The `trait` we're given, `Transform`, is not a type transformation, as we defined
above, since it takes **2** type parameters, not 1. However, we can ignore this
subtlety by just studying `Transform` one parameter at a time, while assuming the
rest of the parameters are fixed.

The definition of `Transform` tells us it takes 2 type parameters, where the first
one is **contravariant** and the second one is **covariant**.

Therefore, to check whether `Transform` was defined correctly or not, let's check
if `A` is indeed contravariant and `B` is covariant.

As we noted earlier, to check if `Transform[A, B]` is really contravariant in `A`
is equivalent to checking whether all methods of `Transform[A,B]` are 
contravariant in `A`. The same, but with covariance, is true for `Transform[A, B]`
with respect to `B`.

We can forget about the distinction of methods and 
attributes if we consider that an attribute is just a constant method, that is, 
a method which takes no arguments, and therefore has no argument type, just 
return type.

Methods are just functions that "live" inside a class, which means that their
signatures are the same as that of functions. Knowing this, we use what we know
of variance about functions, namely that types on the arguments are contravariant
and types on the return types are covariant.

From now on, we'll assume that `X <: Y`

#### Checking `apply` method
```scala
def apply(x: A): B
```
We rewrite the signature of `apply` to be that of a function
```scala
def apply: A => B
```
##### Checking A is `contravariant`
Since we've seen already that functions are contravariant in argument types, we
already know that `A` is indeed contravariant.

##### Checking B is `covariant`
Likewise, we've seen already that functions are covariant in return types, we
already know that `B` is indeed covariant.

#### Checking `map` method
```scala
def map[C](f: A => C): Transform[A, C]
```
We rewrite the signature of `map` to be that of a function
```scala
def map[C] = (A => C) => Transform[A, C]
```
We don't need to care about the type argument `C`, as it is fixed when we use
`map`.

##### Checking A is `contravariant`
Because `X <: Y`, then `( X => C ) >: ( Y => C )`. Considering `Y => C` and `X => C`
as new types, and using again that functions are contravariant in their argument 
types , we get
```
( (X => C) => Transform[...] ) <: ( (Y => C) => Transform[...] )
```

Forgetting about the intermediary relationship then, we have 
```
X <: Y  ------>  ( (X => C) => Tr... ) <: ( (Y => C) => Tr... )
```
which is basically the definition of covariance.

However, this makes no sense since `A` should be covariant according to the
definition of `Transform[A,B]`!

> ###### Note: About the second appearance of A
> It may be confusing to see that we only consider substituting `X` and `Y` in
> place of the first appearance of `A`, and not in both at the same time, so I'll 
> address this now.
> 
> When we arrived at the conclusion that `A` must be covariant, this didn't imply
> that the method `map[C]` was covariant on `A`. For that to be true, we'd have
> to study the second appearance of `A`. The only thing we arrived at is that if
> `map[C]` preserved some kind of relationship between using `X` or `Y`, it must
> be that of covariance. 
> 
> However, if we continue with our analysis, we see that it's not the case, that 
> in fact, `map[C]` is invariant with respect to `A`.
> 
> If we have `X <: Y`, then `Transform[X, C] >: Transform[Y, C]` (Because we're
> told by `Transform`'s definition that it's invariant in its argument type). And
> since functions are covariant in their return types, we have
> ```
> ( ... => Transform[X, C] ) >: ( ... => Transform[Y, C] )
> ```
> which again means that
> ```
> X <: Y  ------>  ( ... => Transform[X, C] ) >: ( ... => Transform[Y, C] )
> ```
> which is the definition of contravariance.
> 
> This means that if `map[C]` preserves some kind of relationship in its `A` type,
> it must be `covariance` and `invariance` at the same time, which is nonsensical.
> 
> Therefore, `map[C]` is invariant in `A`, which shows in a more complicated way
> that `A` cannot be contravariant in `Transform[A,B]`

#### Checking `followedBy` method
For completeness sake, let's look at the case with `followedBy` as well.
```scala
def followedBy[C](t: Transform[B, C]): Transform[A, C]
```
We rewrite the signature of `followedBy` to be that of a function
```scala
def followedBy[C]: Transform[B, C] => Transform[A, C]
```
##### Checking A is `contravariant`
This is true, and to check that we only have to see the note in the `map` method,
where we prove that
```
X <: Y  ------>  ( ... => Transform[X, C] ) >: ( ... => Transform[Y, C] )
```
which is the definition of contravariance.

##### Checking B is `covariant`

Because `X <: Y`, then `Transform[X,C] >:  Transform[Y,C]`. Therefore, 
```
( Transform[X, C] => ... ) <: ( Transform[Y, C] => ... )
```
since (again) functions are contravariant in their argument type. What we have
now proved is that
```
X <: Y  ------>  ( Transform[X, C] => ... ) <: ( Transform[Y, C] => ... )
```
which is the definition of covariance.


### Part 2: Can you come up with a modified method signature that is variance correct?

We could just be tricky and say that since the argument `f: A => C` messes up
the variance, we just try change it to `f: C => A` and we're done. However,
a more elegant solution would be to add a bound to our `map` method. In
this case, a higher bound, and we'll see why shortly.
```scala
def map[D <: A, C]: (D => C) => Transform[D, C]
```

This way, we must have that both `X` and `Y` are subtypes of `A`. Now, if we 
have `X <: Y`, we substitute them in place of `A`to have the following 2 `map`
functions
```scala
def map[D <: X, C]: (D => C) => Transform[D, C]
def map[D <: Y, C]: (D => C) => Transform[D, C]
```
And since `X` is a subtype of `Y`, any subtype `D` of `X` is also subtype of `Y`,
which means that anywhere we have `map[D <: X, C]` we can use `map[D <: Y, C]`, 
which is the definition of being a subtype. We have then that
```
X <: Y  ------>  map[D <: X, C] >: map[D <: Y, C]
```

For completeness sake, if we had a broken method on the covariant `B`, like 
the following
```scala
def example[C]: B => C
```
we could fix it by setting a lower bound
```scala
def example[D >: B, C]: D => C
```
since then, given `X <: Y`, we have the functions
```scala
def example[D >: X, C]: D => C
def example[D >: Y, C]: D => C
```

which works because if `D` is a supertype of `Y`, it's also a supertype of `X`
(since `X <: Y <: D`), which means that `map[D >: X, C]` can be used everywhere
`map[D >: Y, C]` is used. That's the definition of `map[D >: X, C] <: map[D >: Y, C]`,
which means we've proved
```
X <: Y  ------>  map[D >: X, C] <: map[D >: Y, C]
```
which is the definition of covariance.



_Made by Fernando_
