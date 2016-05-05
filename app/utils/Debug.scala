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

package object psqlHelpers {
    import java.sql.{Connection, DatabaseMetaData, SQLException}
    import org.postgresql.util.{PSQLException, PSQLState, ServerErrorMessage}

    def printMeta(implicit c: Connection) {
        val info = c.getMetaData
        println("db-url = " +info.getURL)
    }

    def printServerMsg(msg: ServerErrorMessage) {

        List(
            "getDetail",
            "getHint",
            "getTable",
            "getConstraint",
            "getMessage",
            "getSchema",
            "getFile",
            "getWhere",
            "getColumn",
            "getInternalQuery",
            "getSeverity",
            "getRoutine",
            "toString"
        ).zip(List(
            msg.getDetail,
            msg.getHint,
            msg.getTable,
            msg.getConstraint,
            msg.getMessage,
            msg.getSchema,
            msg.getFile,
            msg.getWhere,
            msg.getColumn,
            msg.getInternalQuery,
            msg.getSeverity,
            msg.getRoutine,
            msg.toString)
        ).map {
                case(tag, thing) => "\t" + tag + ": " + thing
            }.foreach(println)
    }

    def printExc(e: PSQLException) = {
        println("\n------------------------------------------------------------")

        List("e", "msg", "state", "code", "next").map("\t" + _)
            .zip(List(e, e.getMessage, e.getSQLState, e.getErrorCode, e.getNextException))
            .map {
                case(tag, thing) => "\t" + tag + ": " + thing + "\n"
            }.foreach(println)
        println("------------------------------------------------------------")
        printServerMsg(e.getServerErrorMessage)
        println("------------------------------------------------------------\n")
    }
}