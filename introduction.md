# Programming paradign
A paradigm describes distintct concepts or thought patterns in some scientific discipline

#### Imperative programming
Imperative programming is about
 * modifying mutable variables
 * Using asignments
 * Using control structures

The most common way to understand it is as instructions for the Von Neumann computer

There's a strong correspondance between 
 * mutable variables and memory cells
 * variable dereferences and load instructions
 * vairable assignments and store instructions
 * control structures and jumps

The problem comes when scaling up.

...

### What is a theory
A theory consists of 
 * One or more data types
 * Operations within such types
 * Laws that describe the relationship between values and operations

We want to implement high-level concepts following their mathematical theories.
There is no place for mutation

From this concept, Functional Programming is born

### Functional programming
In a restricted sense, FP means programming without mutable variables,
assignments, loops, and other imperative control structures

In a wiser sense, a FP language enables construction of elegant programs that
focus on functions and inmutable data structures

In particular, in FP functions are first-class citizens.
 * They can be defined anywhere
 * They can be passed as parameters to functions
 * There exists a set of operators to compose functions

#### Examples of programming languages
 * Lisp, Scheme, Racket, Clojure
 * SML, Ocaml, F#
 * Haskell
 * Scala


Java lacks the features necessary to be considered a functional programming
language, like efficiency in immutable data types or being able to define 
functions anywhere
| Year  | Language      |
|-------|---------------|
| 1959  | Lisp          |
|1975-77|ML, FP, Scheme |
| 1978  |(Smalltalk)    |
| 1986  |Standard ML    |
| 1990  |Haskell, Erlang|
| 2000  |OCaml          |
| 2003  |Scala          |
| 2005  |F#             |
| 2007  |Clojure        |
| 2017  |Idris          |
| 2020  |Scala 3        |

### Origins of FP
In the 1930s, lambda calculus is started by Alonzo Church, show to be 
equivalent to Turing Machine. It still stays relevant today as the foundations
of FP

In 1959, Lisp is introduced, which allows functions and recursive data tools,
used for artificial intelligence research

In 1980/90s, ML, Haskell and other languages are introduces with new type 
systems with a strong connection to mathematical logic

ML → Method Language (?)

ML introduces some common type system features, like the generics found in Java

#### Why functional programming?
 * Reduce errors
 * Improve modularity
 * Higher-level abstractions
 * Shorter code
 * Increased developer productivity

#### Why functional programming now?
 1. It's an effective tool to handle concurrency and parallelism,
    on every scale
 2. Our computers are not Van-Neumann machines anymore, having:  
     * Parallel cores
     * Clusters of servers
     * Distribution in the cloud

     Which introduces new challenges such as  

     * Cache coherency
     * non-determinism


## Recommended books
 1. [Structure and Interpretation of Computer Programs](https://mitpress.mit.edu/sites/default/files/sicp/full-text/book/book-Z-H-4.html)
 2. Programming in Scala, 4th edition

Other recommended books
 * Hands-on Scala
 * Programming Scala
 * Scala in depth

Also check [Scala's website](https://scala-lang.org). Remember that we'll be
using Scala 3
