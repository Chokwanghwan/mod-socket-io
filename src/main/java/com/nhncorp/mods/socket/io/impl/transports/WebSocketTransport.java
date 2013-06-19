package com.nhncorp.mods.socket.io.impl.transports;

import com.nhncorp.mods.socket.io.impl.ClientData;
import com.nhncorp.mods.socket.io.impl.Manager;
import com.nhncorp.mods.socket.io.impl.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.ServerWebSocket;

import java.util.List;

/**
 * @author Keesun Baik
 */
public class WebSocketTransport extends Transport {

	private static final Logger log = LoggerFactory.getLogger(WebSocketTransport.class);

	private ServerWebSocket webSocket;

	public WebSocketTransport(Manager manager, ClientData clientData) {
		super(manager, clientData);

		webSocket = clientData.getSocket();

		if(webSocket != null) {
			webSocket.exceptionHandler(new Handler<Throwable>() {
                @Override
                public void handle(Throwable throwable) {
                    end("socket error " + ((throwable != null) ? throwable.getMessage() : ""));
                }
            });

			webSocket.closeHandler(new Handler<Void>() {
                @Override
                public void handle(Void aVoid) {
                    end("socket end");
                }
            });

			webSocket.dataHandler(new Handler<Buffer>() {
				public void handle(Buffer buffer) {
					onMessage(parser.decodePacket(buffer.toString()));
				}
			});
		}
	}

	/**
	 * Closes the connection.
	 *
	 * @see "WebSocket.prototype.doClose"
	 */
	@Override
	protected void doClose() {
		if(webSocket != null) {
			try {
				webSocket.close();
			} catch (IllegalStateException e) {
				log.debug(getName() + " was already closed");
			}
		}
	}

	@Override
	public void payload(List<Buffer> buffers) {
		this.write(parser.encodePayload(buffers));
	}

	@Override
	public void write(String encodedPacket) {
		if(webSocket != null) {
			log.debug(getName() + " writing " + encodedPacket);
			webSocket.writeTextFrame(encodedPacket);
		}
	}

	@Override
	protected String getName() {
		return "websocket";
	}
}
