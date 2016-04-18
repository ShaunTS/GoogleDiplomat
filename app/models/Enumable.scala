package sts.diplomat.models

abstract class Enumable extends Model[Enumable] {

    def label: String
}

trait EnumMap {

    val idMap: Map[Long, String]

    def labelMap: Map[String, Long] = idMap.toList.map(_.swap).toMap

    def apply(id: Long): Enumable

    def apply(label: String): Enumable
}