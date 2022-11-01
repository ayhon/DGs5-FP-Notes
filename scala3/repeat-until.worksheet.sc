object RepeatWithoutCondition:
    def withCommand(cmd: => Unit): RepeatWithoutCondition =
        val x = new RepeatWithoutCondition
        x.command = cmd
        x

class RepeatWithoutCondition:
    var command: Unit = ()
    infix def until(condition: => Boolean): Unit =
        command
        if condition then () else this until condition

def repeat(cmd: => Unit): RepeatWithoutCondition =
    RepeatWithoutCondition.withCommand(cmd)

var x = 10;
repeat {
    println("Hello world")
    x -= 1
} until (x <= 0)