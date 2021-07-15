package gsxconfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.SocketClient;

public class GSXSocketClient extends SocketClient {
	public InputStream getInputStream() {return _input_;}
	public OutputStream getOutputStream() {return _output_;}
	public void close() {
		try {
			_input_.close();
			_output_.close();
			_socket_.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
