package com.takumibaba.lindabase.client

import com.takumibaba.eventemitter.EventEmitter
import java.net.URI
import scala.util.parsing.json.JSONArray
import scala.concurrent.ops._
import akka.actor.{Cancellable, ActorSystem}
import scala.concurrent.duration._

/**
 * Created with IntelliJ IDEA.
 * User: takumi
 * Date: 2013/09/11
 * Time: 17:41
 * To change this template use File | Settings | File Templates.
 */
class Linda extends EventEmitter{
  val tupleSpace:TupleSpace = new TupleSpace(this);
//  var io:WebSocketIO = new WebSocketIO(new URI("ws://linda.masuilab.org:10010/"), this)
  var io:WebSocketIO = new WebSocketIO(new URI("ws://0.0.0.0:10010/"), this);
  var session:String = "";
  var timeout: Cancellable = null
  val system  = ActorSystem("Linda")

  def connect(){
    io.connect()
  }

  class TupleSpace(var linda:Linda){
    var name:String = "takumibaba"

    def write(tuple:JSONArray, opts:JSONArray = new JSONArray(List())){
      io.push("__linda_write", List(name, tuple, opts));
    }
    def read(tuple:JSONArray, callback: (List[Any])=> Any){
      var callback_id:String = callbackId()
      io.once("__linda_read_callback_"+callback_id, callback)
      io.push("__linda_read", List(name, tuple, callback_id))
    }
//    未実装　Thread.sleep だと、ioも動かなくなる
//    def read(tuple:JSONArray):List[Any] = {
//      println("sync read")
//      var callback_id:String = callbackId()
//      var resultTuple:List[Any] = List()
//      io.once("__linda_read_callback_"+callback_id, (tuple:List[Any])=> {
//        println("sync read done")
//        resultTuple = tuple
//        println(resultTuple)
//      })
//      io.push("__linda_read", List(name, tuple, callback_id))
//      while(resultTuple.isEmpty){
//        Thread.sleep(1000)
//        println("sleep")
//      }
//      return resultTuple               ja
//    }
    def take(tuple:JSONArray, callback: (List[Any])=> Any){
      val callback_id:String = callbackId()
      io.once("__linda_take_callback_"+callback_id, callback)
      io.push("__linda_take", List(name, tuple, callback_id))

    }
//    未実装　同期Readと同じ
//    def take(tuple:JSONArray){
//      val callback_id:String = callbackId()
//
//    }
    def watch(tuple:JSONArray, callback: (List[Any])=> Any){
      var callback_id:String = callbackId()
      linda.io.on("__linda_watch_callback_"+callback_id, Map(), callback)
      linda.io.push("__linda_watch", List(name, tuple, callback_id))
    }
    def callbackId():String = {
      "%d_%s".format(System.currentTimeMillis(), Math.floor(Math.random()*1000000));
    }
  }
}

