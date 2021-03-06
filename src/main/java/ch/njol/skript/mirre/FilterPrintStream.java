package ch.njol.skript.mirre;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import ch.njol.skript.command.Commands;

public class FilterPrintStream extends PrintStream {

	
	public FilterPrintStream(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
		super(out, autoFlush, encoding);
	}

	public FilterPrintStream(OutputStream out, boolean autoFlush) {
		super(out, autoFlush);
	}

	public FilterPrintStream(OutputStream out) {
		super(out);
	}
	
	@Override
	public synchronized void println(@SuppressWarnings("null") String string){
		if(Commands.suppressUnknownCommandMessage && string.contains("Unknown command. Type")){
			Commands.suppressUnknownCommandMessage = false;
			return;
		}
		super.println(string);
	}

}
