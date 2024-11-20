package users.domain;

import users.domain.model.*;
import shared.ports.persistence.Repository;

class UsersServiceImpl(private val usersRepository: Repository[Username, User])
    extends UsersService:

  def rechargeCredit(username: Username, rechargeAmount: Credit): Credit = ???

  def checkCredit(username: Username): Credit = ???

  def registerUser(username: Username): User = ???
