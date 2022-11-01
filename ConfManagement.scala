/*

SYSTEM TO DISCUSS PAPERS SUBMITED IN CONFERENCE
-----------------------------------------------

 · The papers have already been given a score by the reviewers
 · To discuss, reviewers need to see various pieces of information 
   about the papers
 · Some reviewers are also authors of papers
 · An author of a paper should never see at this phase the score the 
   paper received from the other viewers

   Therefore, every query of the conference needs to know who is seeing 
  the results of the operation, and this needs to be propagated.

   For a given toplevel query, the set of persons seeing its results 
  will largley stay the same, but may vary when a reviewer delegates 
  part of the task to another person.

*/

case class Person(name: String)
case class Paper(title: String, authors: List[Person], body: String)

object ConfManagement:
  //1 type Viewers  = Set[Person]
  opaque type Viewers  = Set[Person]

  type Viewed[T] = Viewers ?=> T //2 An expression of type T that takes Viewers into consideration when computed

  def viewers(using vs: Viewers) = vs //1 or `def viewers = summon[Viewers]`

  class Conference(ratings: (Paper, Int)*):
    private val realScore = ratings.toMap

    def papers: List[Paper] = ratings.map(_._1).toList

    //1 def score(paper: Paper, viewers: Viewers): Int =
    //2 def score(paper: Paper)(using Viewers): Int =
    def score(paper: Paper): Viewerd[Int] =
      if paper.author.exists(viewers.contains) then -100
      else realScore(paper)
  
    //1 def rankings(viewers: Viewers): List[Paper] = 
    //2 def rankings(using Viewers): List[Paper] = 
    def rankings: Viewed[List[Paper]] = 
      papers.sortBy(score).reverse

    //2 def ask[T](p: Person, query: Viewers => T) =
    //2   query(Set(p))
    def ask[T](p: Person, query: Viewed[T]) =
      query(using Set(p))

    //2 def delegateTo(p: Person, query: Viewers => T)(using Viewers): T =
    //2  query(viewers + p)
    def delegateTo(p: Person, query: Viewed[T]): Viewed[T] =
      query(using viewers + p)

  end Conference
end ConfManagement
