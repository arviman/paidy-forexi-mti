package forex.domain

import cats.effect.{IO, Ref}

object Types {
  type SharedStateIO = IO[Ref[IO, Map[Currency, Rate]]]
}
