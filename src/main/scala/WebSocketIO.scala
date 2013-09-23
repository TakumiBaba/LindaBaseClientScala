package com.takumibaba.lindabase.client

import java.net.URI
import scala.util.parsing.json.{JSON, JSONArray, JSONObject}
import com.takumibaba.eventemitter.EventEmitter
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import io.backchat.hookup._
import akka.actor.{Cancellable, ActorSystem}
import scala.concurrent.duration._
import java.io.File
import scala.Some
import io.backchat.hookup.HookupClientConfig
import io.backchat.hookup.IndefiniteThrottle
import java.util.concurrent.atomic.AtomicInteger
import org.json4s.JsonAST.{JObject, JValue}
import scala.concurrent.ops._

/**
 * Created with IntelliJ IDEA.
 * User: takumi
 * Date: 2013/09/11
 * Time: 17:45
 * To change this template use File | Settings | File Templates.
 */
//class WebSocketIO(_uri:URI, var linda:Linda) extends WebSocketClient(_uri) with EventEmitter{
class WebSocketIO(var uri:URI, var linda:Linda) extends  EventEmitter{
  val system  = ActorSystem("WebSocketIO")
  var timeout: Cancellable = null
  val messageCounter = new AtomicInteger(0)
  val bufferedCounter = new AtomicInteger(0)
  var client:HookupClient = null
  var isRunning:Boolean = false

  def connect() = {
    client = new HookupClient {
      def receive = {
        case Connected => {
          println(Connected)
          isRunning = true
        }
        case Reconnecting => {
          println("reconnecting")
          isRunning = false
        }
        case Disconnected(_) =>{
          println("disconnected")
          isRunning = false
        }

        case m @ Error(exOpt) =>
          System.err.println("Received an error: " + m)
          exOpt foreach { _.printStackTrace(System.err) }
        case m: TextMessage =>
          println("RECV: " + m)
        case m: JsonMessage =>
          var tuple:Map[String, Any] = m.content.asInstanceOf[JObject].values
          var eventType:String = tuple.get("type").get.toString()
          println(tuple)
          eventType match {
            case "__session_id" =>{
              println("__session_id")
              linda.session = tuple.get("data").get.toString()
              emit("connect",List())
            }
            case e:String if eventType.contains("__linda") =>{
              var data:List[Any] = tuple.get("data").get.asInstanceOf[List[Any]]
              emit(e, data)
            }
            case _ => println(eventType)
          }
        case _ => println("_")

      }

      val settings:HookupClientConfig = HookupClientConfig(
        uri,
        throttle = IndefiniteThrottle(5 seconds, 30 minutes),
        buffer = Some(new FileBuffer(new File("./work/buffer.log")))
      )

      connect() onSuccess {
        case _ => {
          println("connect")

        }

      }
    }
  }

  def push(typ:String, tuple:List[Any]) = {
    var msg:JSONObject = new JSONObject(Map(
      "type" -> typ,
      "data" -> new JSONArray(tuple),
      "session" -> linda.session
    ))
    client.send(msg.toString())
  }
}

