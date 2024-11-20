package users.domain;

import users.domain.model.*;

trait UsersService:

  def registerUser(username: Username): User

  def checkCredit(username: Username): Credit

  def rechargeCredit(username: Username, rechargeAmount: Credit): Credit

  // TODO: healthCheck
