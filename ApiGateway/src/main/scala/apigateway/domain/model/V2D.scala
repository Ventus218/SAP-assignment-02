package apigateway.domain.model;

case class V2D(x: Double = 0, y: Double = 0)

extension (a: V2D)
  inline def +(b: V2D): V2D =
    V2D(a.x + b.x, a.y + b.y)

  inline def -(b: V2D): V2D =
    V2D(a.x - b.x, a.y - b.y)

  inline def *(b: V2D): V2D =
    V2D(a.x * b.x, a.y * b.y)

  inline def /(b: V2D): V2D =
    V2D(a.x / b.x, a.y / b.y)
