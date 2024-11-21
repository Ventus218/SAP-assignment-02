package users.domain.model;

import upickle.default.*

case class User(username: Username, credit: Credit) derives ReadWriter

extension (u: User)
  def rechargeCredit(credit: Credit): User =
    u.copy(credit = Credit(u.credit.amount + credit.amount))
