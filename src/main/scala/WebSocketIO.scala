package com.takumibaba.lindabase.client

import io.netty.bootstrap.Bootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.channel.socket.SocketChannel
import io.netty.channel._
import io.netty.handler.codec.http.websocketx._
import io.netty.handler.codec.http._
import io.netty.util.CharsetUtil
import java.net.URI
import scala.util.parsing.json._
import scala.util.parsing.json.JSONArray
import scala.util.parsing.json.JSONObject
import com.takumibaba.eventemitter.EventEmitter

/**
 * Created with IntelliJ IDEA.
 * User: takumi
 * Date: 2013/09/11
 * Time: 17:45
 * To change this template use File | Settings | File Templates.
 */
class WebSocketIO(val uri:URI, var linda:Linda) extends EventEmitter{
  var connectionChannel:Channel = null;
  var session:String = "";

  def connect() = {
    var group:EventLoopGroup = new NioEventLoopGroup();
    try{
      var b:Bootstrap = new Bootstrap();
      var protocol:String = uri.getScheme();
      if(protocol != "ws"){
        throw new IllegalArgumentException("unsupported protocol " + protocol);
      }

      var headers:HttpHeaders = new DefaultHttpHeaders();
      headers.add("header", "value");

      val handler:WebSocketClientHandler = new WebSocketClientHandler(WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false, headers));

      b.group(group).channel(classOf[NioSocketChannel]).handler(new ChannelInitializer[SocketChannel] {
        override def initChannel(ch:SocketChannel) = {
          var pipeline:ChannelPipeline = ch.pipeline();
          pipeline.addLast("http-doc", new HttpClientCodec());
          pipeline.addLast("aggregator", new HttpObjectAggregator(8192));
          pipeline.addLast("ws-handler", handler);
        }
      });

      connectionChannel = b.connect(uri.getHost, uri.getPort).sync().channel();
      handler.handshakeFuture().sync();

    } catch {
      case e:Exception => println("exception!");
    } finally {
      group.shutdownGracefully();
    }
  }
  def disConnect() = {
    connectionChannel.closeFuture().sync();

  }

  def push(typ:String, tuple:List[Any]) = {
    var msg:JSONObject = new JSONObject(Map(
      "type" -> typ,
      "data" -> new JSONArray(tuple),
      "session" -> linda.session
    ));
    println("push:"+msg.toString())
    val frame:TextWebSocketFrame = new TextWebSocketFrame(msg.toString())
    println(frame)
    connectionChannel.write(frame);
  }

  class WebSocketClientHandler(var handshaker:WebSocketClientHandshaker) extends SimpleChannelInboundHandler[Object]{
    var _handshakeFuture:ChannelPromise = null;

    def handshakeFuture():ChannelPromise = return _handshakeFuture;
    override def handlerAdded(ctx:ChannelHandlerContext) = _handshakeFuture = ctx.newPromise();
    override def channelActive(ctx:ChannelHandlerContext) = handshaker.handshake(ctx.channel());
    override def channelInactive(ctx:ChannelHandlerContext) = linda.io.emit("disconnect", List());
    override def channelRead0(ctx:ChannelHandlerContext, msg:Object):Unit = {
      var ch:Channel = ctx.channel();
      if(!handshaker.isHandshakeComplete()){
        handshaker.finishHandshake(ch, msg.asInstanceOf[FullHttpResponse]);
        linda.io.emit("connect", List())
//        _handshakeFuture.setSuccess()
        return;
      }

      if (msg.isInstanceOf[FullHttpResponse]) {
        var response:FullHttpResponse = msg.asInstanceOf[FullHttpResponse];
        throw new Exception("Unexpected FullHttpResponse (getStatus=" + response.getStatus() + ", content="
          + response.content().toString(CharsetUtil.UTF_8) + ')');
      }
      var frame:WebSocketFrame = msg.asInstanceOf[WebSocketFrame]
      if(frame.isInstanceOf[TextWebSocketFrame]){
        //        var obj:Option[Any] = ;
        var msg:Map[String, Any] = JSON.parseFull(frame.asInstanceOf[TextWebSocketFrame].text()).get.asInstanceOf[Map[String, Any]];
        println("hoge"+msg)
        msg.get("type").get match{
          case "__session_id" => linda.session = msg.get("data").get.toString
          case "__linda_write.*" => println("write"); // linda.emit("__linda_write_callback_", msg.get("data").asInstanceOf[List[Any]])
          case "__linda_watch.*" => println("watch"); // linda.emit("watch", msg.get("data").asInstanceOf[List[Any]])
          case "__linda_read.*" =>  println("read");  // linda.emit("read", msg.get("data").asInstanceOf[List[Any]])
          case "__linda_take.*" =>  println("take");  // linda.emit("take", msg.get("data").asInstanceOf[List[Any]])
          case _ => println(msg.get("type").get);

        }

      } else if(frame.isInstanceOf[PongWebSocketFrame]){

      } else if(frame.isInstanceOf[CloseWebSocketFrame]){
        ch.close();
      }

    }
  }

}

