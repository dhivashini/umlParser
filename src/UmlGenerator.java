
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import net.sourceforge.plantuml.SourceStringReader;

public class UmlGenerator {
	public void umlCreator(String source) {

		OutputStream png = null;
		try {
			png = new FileOutputStream("C://uml//uml.jpeg");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		SourceStringReader reader = new SourceStringReader(source);
		// Write the first image to "png"
		try {
			reader.generateImage(png);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}