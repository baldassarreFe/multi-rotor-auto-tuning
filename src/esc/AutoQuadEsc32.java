package esc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

import gnu.io.SerialPort;

public class AutoQuadEsc32 {
	private InputStream input;
	private OutputStream output;
	private ReaderThread reader;
	private static HashSet<String> telemetryParameters = new HashSet<>();
	public static String[] allParameters = {"INPUT MODE", "RUN MODE", "ESC STATE", "PERCENT IDLE", "COMM PERIOD", "BAD DETECTS", "FET DUTY", "RPM", "AMPS AVG", "AMPS MAX", "BAT VOLTS", "MOTOR VOLTS", "DISARM CODE", "CAN NET ID"};

	public AutoQuadEsc32(SerialPort port) throws IOException {
		this.input = port.getInputStream();
		this.output = port.getOutputStream();
		AutoQuadEsc32.telemetryParameters.addAll(Arrays.asList(allParameters));
	}
	
	public AutoQuadEsc32 sleep(long millis){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	public AutoQuadEsc32 setRPM(int rpm) {
		return sendCommand("rpm " + rpm);
	}
	
	public AutoQuadEsc32 arm() {
		return sendCommand("arm");
	}
	
	public AutoQuadEsc32 disarm() {
		return sendCommand("disarm");
	}
	
	public AutoQuadEsc32 start() {
		return sendCommand("start");
	}
	
	public AutoQuadEsc32 stop() {
		return sendCommand("stop");
	}
	
	/**
	 * @param from starting rpm
	 * @param to ending rpm
	 * @param pace acceleration in rpm / s 
	 * @return
	 */
	public AutoQuadEsc32 accelerate(int from, int to, double pace) {
		if (pace == 0 || from == to) {
			throw new IllegalArgumentException("Cannot accelerate");
		}
		
		int deltaRpm = pace>0 ? 1 : -1;
		long deltaT = Math.round((deltaRpm / pace) * 1000);
		
		if (from < to && pace>0) {
			setRPM(from);
			while(from<=to){
				from+=deltaRpm;
				setRPM(from);
				try {
					Thread.sleep(deltaT);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else if (from > to && pace<0) {
			setRPM(from);
			while(from>=to){
				from+=deltaRpm;
				setRPM(from);
				try {
					Thread.sleep(deltaT);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else {
			throw new IllegalArgumentException("from, to, pace not compatible");
		}
		return this;
	}
	
	public AutoQuadEsc32 sendCommand(String command){
		try {
			// l'ESC usa la codifica UTF-8 e ha bisogno dei caratteri di LF e CR
			output.write(command.trim().getBytes("UTF-8"));
			output.write(13); // LF
			output.write(10); // CR
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	public AutoQuadEsc32 startTelemetry(int frequency, Writer writer){
		if (frequency<=0 || frequency>60) {
			throw new IllegalArgumentException("frequenza non valida per la telemetry " + frequency);
		}
		if (writer == null) {
			throw new IllegalArgumentException("writer nullo");
		}
		sendCommand("telemetry " + frequency);
		if (reader != null) {
			reader.shouldRead.set(false);
		}
		reader = new ReaderThread(writer);
		reader.start();
		return this;
	}
	
	public AutoQuadEsc32 stopTelemetry() {
		sendCommand("telemetry 0");
		if (reader != null) {
			reader.shouldRead.set(false);
		}
		return this;
	}

	public void addTelemetryParameter(String parameter){
		telemetryParameters.add(parameter);
	}
	
	public void setTelemetryParameters(String[] parameters){
		telemetryParameters.clear();
		telemetryParameters.addAll(Arrays.asList(parameters));
	}
	
	public void removeTelemetryParameter(String parameter){
		telemetryParameters.remove(parameter);
	}
	
	private class ReaderThread extends Thread {
		private Writer writer;
		private ByteArrayOutputStream inputBuffer;
		protected AtomicBoolean shouldRead = new AtomicBoolean(true);
		public ReaderThread(Writer writer){
			this.setName("ReaderThread");
			this.writer = writer;
			this.inputBuffer = new ByteArrayOutputStream();
		}
		public void run() {
			try {
				byte singleData;
				while (shouldRead.get() == true && (singleData = (byte) input.read()) != -1) {
					inputBuffer.write(singleData);
					if (singleData == 10) {
						ByteArrayInputStream bin = new ByteArrayInputStream(inputBuffer.toByteArray());
						BufferedReader reader = new BufferedReader(new InputStreamReader(bin, "UTF-8"));
						String line = reader.readLine();
						for (String parameter : telemetryParameters) {
							if (line.startsWith(parameter))
								writer.write(line+"\n");
						}
						inputBuffer.reset();
					}
				}
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
