package sts.diplomat.models

/** Represents the set of 'price levels' used by Google Places api to represent
 *  the relative cost of products or services offered by a given 'place'.
 */
case class PriceLevel(id: Option[Long], label: String) extends Enumable {

    def withLabel(label: String): PriceLevel = this.copy(label = label)

    def withId(id: Long): PriceLevel = this.copy(id = Some(id))
}

object PriceLevel extends EnumMap {

    def empty = PriceLevel(None, "")

    val idMap: Map[Long, String] = Map(
        0L -> "Free",
        1L -> "Inexpensive",
        2L -> "Moderate",
        3L -> "Expensive",
        4L -> "Very Expensive"
    )

    def apply(id: Long): PriceLevel = empty.withId(id).withLabel(idMap(id))

    def apply(label: String): PriceLevel = empty.withLabel(label).withId(labelMap(label))
}