import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;


public class MakeSound {
	private final int BUFFER_SIZE = 128000;
	private File soundFile;
	private AudioInputStream audioStream;
	private AudioFormat audioFormat;
	private SourceDataLine sourceLine;
	
	public void playSound(String filename){
		String strfile = filename;
		
		try{
			soundFile = new File(strfile);
			audioStream = AudioSystem.getAudioInputStream(soundFile);
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
		audioFormat = audioStream.getFormat();
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		try{
			sourceLine = (SourceDataLine) AudioSystem.getLine(info);
			sourceLine.open(audioFormat);
		}
		catch(Exception e1){
			e1.printStackTrace();
			System.exit(1);
		}
		sourceLine.start();
		int nBytesRead = 0;
		byte[] abData = new byte[BUFFER_SIZE];
		while(nBytesRead != -1){
			try{
				nBytesRead = audioStream.read(abData, 0, abData.length);
			}
			catch(Exception e3){
				e3.printStackTrace();
			}
			if(nBytesRead >= 0){
				int nBytesWritten = sourceLine.write(abData, 0, nBytesRead);
			}
		}
		
		sourceLine.drain();
		sourceLine.close();
	}
}
