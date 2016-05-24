
package sts.libs.db

import anorm._
import org.postgresql.util.PGobject
import sts.util.debug.helpers._


object PostgresHelpers {

    def className(that: Any): String =
    if (that == null) "<null>" else that.getClass.getName

    /**
     *  @todo clean up printlns, add option/null/json handling
     */
    implicit val columnToPGobject: Column[PGobject] =
        Column.nonNull[PGobject] { (value, meta) =>
            val MetaDataItem(qualified, nullable, clazz) = meta

            value match {
                case obj: PGobject => pl("type") {
                    s"""
                        type: ${obj.getType}

                        value >${obj.getValue}<
                    """
                }
            }
            value match {
                case obj: PGobject => Right(obj)
                case _ => pl("toColumn LEFT") {
                    Left(TypeDoesNotMatch(s"Cannot convert $value: ${className(value)} to String for column $qualified"))
                }
            }
        }
}
