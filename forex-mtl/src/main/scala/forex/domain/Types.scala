package forex.domain

import cats.effect.{IO, Ref}

object Types {
  type SharedState = Ref[IO, Map[Currency, Rate]]
}
