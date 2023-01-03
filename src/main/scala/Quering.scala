
import java.io.File
import scala.io.Source
import Loading._
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import scala.annotation.tailrec

object Quering {
  /***** REPORT OPTION *****/
  /************************/
  
  /* HIGHEST AND LOWEST COUNTRIES */
  def min (listCountries : List[(String,Int)]) : (String,Int) = listCountries match {
    case Nil => ("error", 0)
    case List(x) => x
    case head :: second :: tail => min ((if (head._2 < second._2) head else second) :: tail)
    case List(_) => ("error", 0)
  }

  def max (listCountries : List[(String,Int)]) : (String,Int) = listCountries match {
    case Nil => ("error", 0)
    case List(x) => x
    case head :: second :: tail => max ((if (head._2 > second._2) head else second) :: tail)
    case List(_) => ("error", 0)
  }

  def length (listCountries : List[(String,Int)]) : Int = listCountries match {
    case Nil => 0
    case List(x) => 1
    case head :: tail => 1 + length(tail)
  }

  def delete (listCountries : List[(String,Int)], toDelete : (String,Int)) : List[(String,Int)] = listCountries match {
    case Nil => List()
    case head :: tail => if (head == toDelete) {
      delete(tail, toDelete)
    } else {
      head :: delete(tail, toDelete)
    }
  }

  /* récupère les 10 pays avec le plus grand nombre d'aéroports */
  def show10highest (listCountries : List[(String,Int)]) : List[(String,Int)] = listCountries match {
    case Nil => List()
    case head :: tail if (length(tail) > 9) => show10highest(delete(listCountries, min(listCountries)))
    case _ => listCountries
  }

  /* récupère les 10 pays avec le plus petit nombre d'aéroports */
  def showLowestCountries (listCountries : List[(String,Int)], min : (String,Int)) : List[(String,Int)] = listCountries match {
    case Nil => List()
    case head :: tail => if (head._2 == min._2) {
      head :: showLowestCountries(tail, min)
    } else {
      showLowestCountries(tail, min)
    }
  }

  def showTop10Countries() = {
    var highest:HashMap[String, Int] = new HashMap()

    airports.foreach
    {
      case (key, value) => if (highest.contains(value.iso_country)) {
        highest.update(value.iso_country, highest.get(value.iso_country).getOrElse(-1000000) + 1)
      } else {
        highest.put(value.iso_country,1)
      }
    }

    println("These are the Top 10 countries with the highest number of airports in the world")
    show10highest(highest.toList.sortBy(_._2)).foreach{
      case (key_x, value_x) => countries.foreach{
        case (key_y,value_y) => if (value_y.code == key_x) {
          println(key_y + " -> " + value_x)
        }
      }
    }

    println("\n")

    println("These are the countries with the lowest number of airports in the world")
    showLowestCountries(highest.toList, min(highest.toList)).foreach{
      case (key_x, value_x) => countries.foreach{
        case (key_y,value_y) => if (value_y.code == key_x) {
          println(key_y + " -> " + value_x)
        }
      }
    }
  }

  /* TYPES OF RUNWAYS */

  def type_in_list(list : List[String], element : String) : Boolean = list match {
    case Nil => false
    case head :: tail => if (element == head){
      true
    } else {
      type_in_list(tail, element)
    }
  }

  def showTypeRunways() = {
    println("loading...")
    var type_runways:HashMap[String, List[String]] = new HashMap()

    airports.foreach{
      case (key_airport, value_airport) => runways.foreach{
        case (key_runway, value_runway) => if (key_airport == value_runway.airport_ref) {
          if (type_runways.contains(value_airport.iso_country)) {
            if (type_in_list(type_runways.get(value_airport.iso_country).getOrElse(List()), value_runway.surface) == false){
              type_runways.update(value_airport.iso_country, value_runway.surface :: type_runways.get(value_airport.iso_country).getOrElse(List("error")))
            }
          } else {
            type_runways.put(value_airport.iso_country, List(value_runway.surface))
          }
        }
      }
    }

    type_runways.foreach{
      case (key, value) => println(key + " -> " + value)
    }
  }

  /* TOP 10 MOST COMMON RUNWAY LATITUDE */
  def showTop10RunwaysLatitude() = {
    var latitude_runways:HashMap[String, Int] = new HashMap()

    runways.foreach{
      case (key,value) => if (latitude_runways.contains(value.le_ident)) {
        latitude_runways.update(value.le_ident, latitude_runways.get(value.le_ident).getOrElse(-100000) + 1)
      } else {
        latitude_runways.put(value.le_ident, 1)
      }
    }

    println("The top 10 most common runway latitude are :")
    show10highest(latitude_runways.toList.sortBy(_._2)).foreach{
      case (key,value) => println(key + " -> " + value)
    }
  }

  /***** QUERY OPTION *****/
  /************************/

  def showCountry(byIsoCode: Boolean = false): Unit = {
    if (byIsoCode){
      println("Type in the country code you want to browse")
    } else{
      println("Type in the country name you want to browse")
    }
    val country = scala.io.StdIn.readLine()
    searchCountryAirportsAndRunways(country, byIsoCode)
  }

  def fuzzy_search(string_1: String, string_2: String): Boolean = {
    val string_2_short = string_2.slice(0,string_1.length())
    if (string_1.toLowerCase() == string_2_short.toLowerCase()){
      true
    } else {
      false
    }
  }

  /* le problème vient du iso_country, rappel -> on fait par NOM de pays et non par code */
  def tri_par_pays(list: List[(String, Airport)], country: String, empty_list: List[(String, Airport)]): List[(String, Airport)] = list match {
    case Nil => empty_list
    case (key,value) :: tail if (value.iso_country == country) => tri_par_pays(tail, country, (key,value) :: empty_list)
    case (key,value) :: tail => tri_par_pays(tail, country, empty_list)
  }

  def searchCountryAirportsAndRunways(country: String, byIsoCode: Boolean): Unit = {
    if(byIsoCode == true){
      /* select from code column */
      println("You choose to browse by country code : $country")
      airports.foreach{
        case (key_airport, value_airport) => if (country == value_airport.iso_country) {
          print("This is airport : " + value_airport.name + " in country : " + value_airport.iso_country + "\n")
          runways.foreach{
            case (key_runway, value_runway) => if (key_airport == value_runway.airport_ref) {   /* vérifier qu'il existe des runway pour tel aéroport -> si on a pas de runway on affiche pas*/
              println(key_runway + " -> " + value_runway)
            }
          }
          println("\n")
        }
      }
      println("/// SOME AIRPORTS MIGHT HAVE NO RUNWAYS REGISTERED IN OUR DATABASE ///")
    }
    else{
      /* select from name column */
      println(s"You choose to browse by country name : $country")
      var empty: List[(String, Airport)] = List()
      var country_found = ""

      countries.foreach{
        case (key,value) => if (fuzzy_search(country,key)) {country_found = key}
      } 

      var current_country = countries.get(country_found) match { 
        case None => Country(0, "error", "error", "error", "error", "error")
        case Some(value) => value
      }

      var airports_tri: List[(String, Airport)] = tri_par_pays(airports.toList, current_country.code, empty)

      airports_tri.foreach{
        case (key_airport, value_airport) => if (current_country.code == value_airport.iso_country) {
          print("This is airport : " + value_airport.name + " in country : " + country_found + "\n")
          runways.foreach{
            case (key_runway, value_runway) => if (key_airport == value_runway.airport_ref) {   /* vérifier qu'il existe des runway pour tel aéroport -> si on a pas de runway on affiche pas*/
              println(key_runway + " -> " + value_runway)
            }
          }
          println("\n")
        }
      }

      println("// SOME AIRPORTS MIGHT HAVE NO RUNWAYS REGISTERED IN OUR DATABASE //")
    }
  }
}