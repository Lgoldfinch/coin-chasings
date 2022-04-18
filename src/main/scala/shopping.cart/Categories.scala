package shopping.cart

import shopping.cart.models.{Category, CategoryId, CategoryName}

trait Categories[F[_]]{
  def findAll(): F[List[Category]]
  def create(name: CategoryName): F[Category]
}
