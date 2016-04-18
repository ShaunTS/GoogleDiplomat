package sts.diplomat.models

abstract class Model[T] {

    def id: Option[Long]

    def withId(id: Long): T
}