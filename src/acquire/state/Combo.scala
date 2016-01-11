package acquire.state

object Combo {
  /**
    * Generate all combination of k integers in the range [0, len)
    * from https://gist.github.com/kaja47/7350525
    * @param len The upper bound [0, len)
    * @param k The size of each combination
    * @return An iterator of combinations (each is an array of ints)
    */
  def numCombinations(len: Int, k: Int): Iterator[Array[Int]] = {
    val arr = Array.range(0, k)
    arr(k-1) -= 1
    val end = k-1

    Iterator.continually {
      arr(end) += 1
      if (arr(end) >= len) {
        var i = end
        while (arr(i) > len-k+i && i != 0) {
          arr(i-1) += 1
          i -= 1
        }
        i = 1
        while (i <= end) {
          if (arr(i) > len-k+i)
            arr(i) = arr(i-1)+1
          i += 1
        }
      }
      if (arr(0) > len-k) null else arr
    } takeWhile (_ != null)
  }

  /**
    * Generate all combinations of arr.
    * from https://gist.github.com/kaja47/7350525
    * @param arr The array of ints to get combinations of
    * @param k The size of each combination
    * @return An iterator of combinations (each is an array of ints)
    */
  def combinations(arr: Array[Int], k: Int): Iterator[Array[Int]] =
    numCombinations(arr.length, k) map { nums =>
      val n = new Array[Int](k)
      var i = 0
      while (i < k) {
        n(i) = arr(nums(i))
        i += 1
      }
      n
    }
}
