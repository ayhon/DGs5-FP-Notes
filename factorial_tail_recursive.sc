@tailrec
def factorialInter(x: Int, acc: Int): Int = 
	if x == 0 then return acc else factorialInter(x-1,acc*x)

def factorial(n: Int): Int = factorialInter(n,1)

@main def main = factorial(5)
