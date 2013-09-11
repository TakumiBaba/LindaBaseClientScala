package com.takumibaba.lindabase.client

import com.takumibaba.eventemitter.EventEmitter
import scala.util.parsing.json.JSONArray

object App{
  def main(args: Array[String]) {
    print("Hello com.takumibaba.LindaBaseClient!")

    var linda:Linda = new Linda()
    linda.io.on("connect", Map(), (resultTuple:List[Any])=>{
      println("connect to "+linda.io.uri)
      linda.tupleSpace.write(new JSONArray(List("hoge")), new JSONArray(List("fuga")))
//      var hoge:List[Any] = linda.tupleSpace.read(new JSONArray(List("babascript")))
//      println(hoge)
//      linda.tupleSpace.watch(new JSONArray(List()), (resultTuple:List[Any])=>{
//        println(resultTuple)
//      })
    })
    linda.connect()

  }
}