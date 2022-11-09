package pta;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class AnswerPrinter {

	PrintStream ps;
	String buffer;

	AnswerPrinter(String file) {
		try {
			ps = new PrintStream(new FileOutputStream(new File(file)));
			buffer = "";
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	void append(String str) {
		buffer = buffer + str;
	}

	void append(Integer i) {
		buffer = buffer + i;
	}

	void append(char ch) {
		buffer = buffer + ch;
	}

	void append(char[] str) {
		buffer = buffer + str;
	}

	void flush() {
		ps.print(buffer);
	}

	void close() {
		ps.close();
	}

}
