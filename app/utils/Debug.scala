package sts.util.debug


/** Simple println utilities for debugging purposes */
package object helpers {

    def pl[T](tag: String)(block: =>T): T = {
        val lineLength = 80
        val label = s"---{ $tag }"
        val tb = "    "

        println("\n" + label + "-".*(lineLength - label.length) + "\n")
        val res: T = block

        res match {
            case _: Unit => ()
            case _ => println(tb + res)
        }
        println("\n" + "-".*(lineLength) + "\n")

        res
    }

    def printErr(e: Throwable) = {
        val cause = e.getCause
        val msg = e.getMessage
        val stack = e.getStackTrace.take(3)

        println("\tcause: " +cause)
        println("\n\tmsg: " +msg)
        println("\n\t" + stack.mkString("\n\t"))
        if(stack.length > 3)
            println(s"\n\t${stack.length - 3} remaining in stack.")
    }
}