package forex.domain

import cats.effect.Ref

object Types {

  /**
   * This is the common cache of forex rates, keyed by Currency (means xyzUSD) for brevity,
   * can be changed later to Pair if we want to support cross pairs (like EURCHF) directly
   */
  type SharedState[F[_]] = Ref[F, Map[Currency, Rate]]
}
