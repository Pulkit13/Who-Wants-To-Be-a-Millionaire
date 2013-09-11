import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;

import javax.speech.Central;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.jsgf.JSGFGrammar;

public class trial {
	public static JFrame frame = null;
	public static boolean mSignPosition = false;
	public static JButton questionButton;
	public static JButton answer1;
	public static JButton answer2;
	public static JButton answer3;
	public static JButton answer4;
	
	
	public static Result result;
	public static JLabel text;
	public static JPanel j, boardP, qPanel;
	public static Font f = new Font("Dialog", Font.PLAIN, 18);
	public static ImagePanel imgPanel;
	public static Handlerclass handler;
	public static final String VOICE_KEVIN = "kevin";
	private Voice voice;
	public static ConfigurationManager cm;
	public static Recognizer recognizer;
	public static Synthesizer synthesizer;

	public static JSGFGrammar grammarManager;
	static trial me;
	public static Thread t1;
	public static int optionFlag=0;
	public volatile static boolean introThread = false;
	public volatile static boolean qThread = false;
	public volatile static boolean surity = false;
	public volatile static boolean dismiss = false;
	public volatile static boolean phone = false;
	public static boolean audPoll = false;
	public static boolean fifty_fifty = false;
	public static ArrayList<String> questions;
	public static ArrayList<ArrayList<String>> answers;
	public static ArrayList<String> correct;
	public static HashMap<Integer, Integer> map;
	public static HashMap<Integer, String> wordMap;
	public static int qNumber = 0;
	public static int answerFlag = 0;
	public static int lastStage = 0;
	public static int wrongAnswers=0, correctAnswers=0;
	
	public trial() {

	}

	public trial(String voiceName) {
		VoiceManager vm = VoiceManager.getInstance();
		voice = vm.getVoice(voiceName);
		voice.setPitch(120);
		voice.setRate(120);

		if (voice == null) {
			System.err.println("Cannot find a voice named " + voiceName
					+ ".  Please specify a different voice.");
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		// load questions from json to hashmap.
		questions = new ArrayList<String>();
		answers = new ArrayList<ArrayList<String>>();
		correct = new ArrayList<String>();
		loadQuestions();
		loadAnswers();
		loadMap();
		editGrammarFile("millionaire");

		trial t = new trial();
		handler = t.new Handlerclass();
		t1 = new Thread(t.new Handlerclass());

		cm = new ConfigurationManager(
				trial.class.getResource("jsgf\\jsgf.config.xml"));

		
		grammarManager = (JSGFGrammar) cm.lookup("jsgfGrammar");

		frame = new JFrame("Millionaire");
		frame.setLayout(new BorderLayout());
		frame.setJMenuBar(createMenuBar());

		JPanel panel = new JPanel(new BorderLayout());
		frame.add(panel, BorderLayout.CENTER);

		// paste image on this panel.
		imgPanel = new ImagePanel(new ImageIcon(
				"images\\mBg.jpg").getImage());
		frame.getContentPane().add(imgPanel);

		panel.addMouseListener(handler);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setVisible(true);
		frame.setResizable(false);

		new MakeSound().playSound("src\\play.wav");
		introThread = true;
		t1.start();
		handler.terminate();

		JOptionPane.showMessageDialog(frame, "Please read the instructions first. Then go to File and click Begin to start the game!",
				"Let's Play", JOptionPane.INFORMATION_MESSAGE);
	}

	private static void editGrammarFile(String grammarName) {
		StringBuilder output = new StringBuilder();
		// Header
		output.append("#JSGF V1.0;\n\n/**\n * JSGF Grammar for Millionaire: "
				+ grammarName + "\n */\n\n");
		grammarName = grammarName.toLowerCase().replace(" ", "");
		String delims = "[ ]+";
		output.append("grammar " + grammarName + ";\n\n");
		// Body
		output.append("public <answer> = ");

		// answer options.
		// quit, close, end, exit.
		// fifty, audience, poll, audience poll, fifty fifty, phone a friend,
		// phone, friend, call.
		// lifeline.
		
		//String[] tokens = resultText.split(delims);
		for(int a=0;a< answers.size() - 1;a++){
			for(int b=0;b<4;b++){
				String[] words = answers.get(a).get(b).split(delims);
				for(int c=0;c<words.length;c++){
					output.append(words[c] + " | ");
				}
			}
		}
		
		for (int j = 0; j < 4; j++) {
			String[] words = answers.get(answers.size()-1).get(j).split(delims);
			for(int c=0;c<words.length;c++){
				output.append(words[c]);
				if (j != 3)
					output.append(" | ");
				else if(j==3 && c!= (words.length - 1)){
					output.append(" | ");
				}
				else if(j==3 && c== (words.length - 1)){
					output.append(";");
				}
			}
		}

		output.append("\n\n");
		output.append("public <lifeline> = ");
		output.append("fifty | audience | poll | friend | phone | call ;");
		
		output.append("\n\n");
		output.append("public <doubles> = ");
		output.append("(fifty | audience | phone)(fifty | poll | a friend);");
		

		output.append("\n\n");
		output.append("public <options> = ");
		output.append("quit | close | end | exit;");

		output.append("\n\n");
		output.append("public <choices> = ");
		output.append("A | B | C | D;");
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
					"src/jsgf/millionaire.gram")));
			bw.write(output.toString());
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void loadMap() {
		// TODO Auto-generated method stub
		map = new HashMap<Integer, Integer>();
		wordMap = new HashMap<Integer, String>();
		map.put(0, 0);
		map.put(1, 100);
		map.put(2, 200);
		map.put(3, 300);
		map.put(4, 500);
		map.put(5, 1000);
		map.put(6, 2000);
		map.put(7, 4000);
		map.put(8, 8000);
		map.put(9, 16000);
		map.put(10, 32000);
		map.put(11, 64000);
		map.put(12, 125000);
		map.put(13, 250000);
		map.put(14, 500000);
		map.put(15, 1000000);

		wordMap.put(0, "zero");
		wordMap.put(100, "one hundred");
		wordMap.put(200, "two hundred");
		wordMap.put(300, "three hundred");
		wordMap.put(500, "five hundred");
		wordMap.put(1000, "one thousand");
		wordMap.put(2000, "two thousand");
		wordMap.put(4000, "four thousand");
		wordMap.put(8000, "eight thousand");
		wordMap.put(16000, "sixteen thousand");
		wordMap.put(32000, "thirty two thousand");
		wordMap.put(64000, "sixty four thousand");
		wordMap.put(125000, "one hundred and twenty five");
		wordMap.put(250000, "two hundred and fifty");
		wordMap.put(500000, "half a million dollars");
		wordMap.put(1000000, "one million");
	}

	private static void loadAnswers() {
		// TODO Auto-generated method stub
		try {
			FileInputStream fstream = new FileInputStream(
					"src\\answers.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			ArrayList<String> alllines = new ArrayList<String>();

			while ((line = br.readLine()) != null) {
				alllines.add(line);
				/*
				 * if (alllines.size() % 4 == 0) { // add alllines to answers
				 * arraylist. answers.add(alllines);
				 * correct.add(answers.get(i).get(0)); i++; alllines.clear();
				 * //alllines.remove(alllines); }
				 */
			}
			int j = 0;
			for (int i = 0; i < (alllines.size()) / 4; i++) {
				ArrayList<String> C = new ArrayList<String>();
				C.add(alllines.get(j));
				C.add(alllines.get(j + 1));
				C.add(alllines.get(j + 2));
				C.add(alllines.get(j + 3));
				j = j + 4;
				answers.add(C);
				correct.add(answers.get(i).get(0));
			}

			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void loadQuestions() {
		// TODO Auto-generated method stub
		// load questions from questions.txt into the arraylist "questions".
		try {
			FileInputStream fstream = new FileInputStream(
					"src\\questions.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = br.readLine()) != null) {
				// add line to questions arraylist of strings.
				questions.add(line);
			}
			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public class Handlerclass implements MouseListener, Runnable {

		// recognizer.allocate();
		private volatile boolean running = true;

		@Override
		public void run() {
			// TODO Auto-generated method stub

			if (running) {
				if (introThread) {
					speakIntro();
				} else if (qThread) {
					try {
						answer();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (surity) {
					try {
						wrongAnswer();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (phone) {
					try {
						phoneFriend();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else
					systemSpeakContents();
			}
		}

		public void begin() {
			running = true;
		}

		public void terminate() {
			running = false;
		}

		private void phoneFriend() throws Exception {
			try {
				System.setProperty("freetts.voices",
						"com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
				Central.registerEngineCentral("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral");
				synthesizer = Central
						.createSynthesizer(new SynthesizerModeDesc(Locale.US));
				synthesizer.allocate();
				synthesizer.resume();

				me = new trial(trial.VOICE_KEVIN);
			} catch (Exception e) {
				e.printStackTrace();
			}

			me.open();
			Thread.sleep(2000);
			me.speak("All right. You want to use phone a friend. Lets call your friend");
			Thread.sleep(3000);
			String p = JOptionPane
					.showInputDialog("Your friend thinks the answer is A. "
							+ answers.get(qNumber).get(0)
							+ ". Click OK if you want to go with him and lock your answer.");
			if (p != null) {
				qThread = true;
				surity = false;
				Thread t3 = new Thread(this);
				t3.start();
			} else {

			}
		}

		private void speakIntro() {
			// TODO Auto-generated method stub
			try {
				System.setProperty("freetts.voices",
						"com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
				Central.registerEngineCentral("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral");
				synthesizer = Central
						.createSynthesizer(new SynthesizerModeDesc(Locale.US));
				synthesizer.allocate();
				synthesizer.resume();

				me = new trial(trial.VOICE_KEVIN);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// speak out the question and answer options.
			me.open();
			String intro = "Welcome to Who wants to be a Millionaire!";
			me.speak(intro);
			me.close();
			// deallocateSynthesizer();
		}

		@Override
		public void mouseClicked(MouseEvent event) {
			// TODO Auto-generated method stub
			// add voice listener and begin recognizing user speech.
			// store it as a string
			// tokenize it

		}

		public void wrongAnswer() throws Exception {
			try {
				System.setProperty("freetts.voices",
						"com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
				Central.registerEngineCentral("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral");
				synthesizer = Central
						.createSynthesizer(new SynthesizerModeDesc(Locale.US));
				synthesizer.allocate();
				synthesizer.resume();

				me = new trial(trial.VOICE_KEVIN);
			} catch (Exception e) {
				e.printStackTrace();
			}
			me.open();
			switch (answerFlag) {
			case 2:
				answer2.setBackground(Color.ORANGE);
				me.speak("locking your answer");
				Thread.sleep(3000);
				answer1.setBackground(Color.GREEN);
				break;

			case 3:
				answer3.setBackground(Color.ORANGE);
				me.speak("locking your answer");
				Thread.sleep(3000);
				answer1.setBackground(Color.GREEN);
				break;

			case 4:
				answer4.setBackground(Color.ORANGE);
				me.speak("locking your answer");
				Thread.sleep(3000);
				answer1.setBackground(Color.GREEN);
				break;

			default:
			}

			
			me.speak("I am sorry. Your answer is wrong. You will take away "
					+ wordMap.get(map.get(lastStage)) + " dollars");

			Thread.sleep(1000);
			me.speak("Goodbye. Thanks for playing.");

			frame.validate();
			
			//generate status report.
			JOptionPane.showMessageDialog(frame, "The statistics are :\n\n Answers correctly recognized : " + correctAnswers + "\n Answers wrongly recognized : " + wrongAnswers + "\n System Accuracy : " + ((double)correctAnswers/(correctAnswers+wrongAnswers))*100 + "%", "Evaluation Report", JOptionPane.INFORMATION_MESSAGE);
			
			me.close();

		}

		public void answer() throws InterruptedException {
			try {
				System.setProperty("freetts.voices",
						"com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
				Central.registerEngineCentral("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral");
				synthesizer = Central
						.createSynthesizer(new SynthesizerModeDesc(Locale.US));
				synthesizer.allocate();
				synthesizer.resume();

				me = new trial(trial.VOICE_KEVIN);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// speak out the question and answer options.
			qNumber++;
			me.open();

			answer1.setBackground(Color.ORANGE);
			me.speak("locking your answer");
			Thread.sleep(3000);

			answer1.setBackground(Color.GREEN);
			me.speak("Your answer is correct! You have won "
					+ wordMap.get(map.get(qNumber))
					+ " dollars! Congratulations.");

			switch (qNumber) {
			case 5:
				lastStage = 1000;
				me.speak("You will now take home at least "
						+ wordMap.get(lastStage) + " dollars");
				break;

			case 10:
				lastStage = 32000;
				me.speak("You will now take home at least "
						+ wordMap.get(lastStage) + " dollars");
				break;

			case 15:
				lastStage = 1000000;
				me.speak("This is amazing. You have won the game. You are now a millionaire! Congratulations!");
				break;

			default:
				break;
			}

			

			if (qNumber != 15) {
				frame.getContentPane().remove(boardP);
				BoardPanel boardPanel = new BoardPanel(new ImageIcon("images\\" + 
						(qNumber + 1) + ".jpg").getImage());
				boardP.setBackground(Color.BLUE);
				boardP.add(boardPanel);
				frame.getContentPane().add(boardP, BorderLayout.EAST);

				// frame.getContentPane().remove(qPanel);
				/*
				 * QuestionPanel mQuestion = new QuestionPanel(new
				 * BorderLayout(), qNumber); mQuestion.addPanel();
				 * //mQuestion.add(big, BorderLayout.WEST);
				 * qPanel.setBackground(Color.BLACK); qPanel.add(mQuestion);
				 * frame.getContentPane().add(qPanel, BorderLayout.SOUTH);
				 */
				// addQuestionToScreen(qNumber);

				questionButton.setText(questions.get(qNumber));
				answer1.setText(answers.get(qNumber).get(0));
				answer1.setBackground(new Color(28, 54, 104));
				answer2.setText(answers.get(qNumber).get(1));
				answer2.setBackground(new Color(28, 54, 104));
				answer3.setText(answers.get(qNumber).get(2));
				answer3.setBackground(new Color(28, 54, 104));
				answer4.setText(answers.get(qNumber).get(3));
				answer4.setBackground(new Color(28, 54, 104));
			}
			frame.validate();

			Thread.sleep(2000);
			if (qNumber == 15) {
				// qNumber is 15.
				me.speak("Thanks for playing. Goodbye!");
				Thread.sleep(1000);
				me.close();
				System.exit(1);
			}
			me.close();
			systemSpeakContents();
		}

		private void speakContents() throws InterruptedException {
			// TODO Auto-generated method stub
			int flag = 5;
			//Result result = null;
			//result = recognizer.recognize();
			//System.out.println(result);
			String resultText;
			if (result != null) {
				resultText = result.getBestFinalResultNoFiller().toLowerCase();
				System.out.println(resultText);
				// tokenize the resultText.
				String delims = "[ ]+";
				String[] tokens = resultText.split(delims);
				String str;
				optionFlag=0;
				for(int k=0;k<tokens.length;k++){
					if(tokens[k].equalsIgnoreCase("A")){
						optionFlag=1;
						String s = JOptionPane.showInputDialog("Do you mean option A " + correct.get(qNumber));
						if(s!=null){
							correctAnswers++;
							qThread = true;
							surity = false;
							answerFlag = 1;
							Thread optionA = new Thread(this);
							optionA.start();
						}
						else{
							wrongAnswers++;
						}
						break;
					}
					else if(tokens[k].equalsIgnoreCase("B")){
						optionFlag=1;
						String s = JOptionPane.showInputDialog("Do you mean option B " + answers.get(qNumber).get(1));
						if(s!=null){
							correctAnswers++;
							qThread = false;
							surity = true;
							answerFlag = 2;
							Thread optionB = new Thread(this);
							optionB.start();
						}
						else{
							wrongAnswers++;
						}
						break;
					}
					else if(tokens[k].equalsIgnoreCase("C")){
						optionFlag=1;
						String s = JOptionPane.showInputDialog("Do you mean option C " + answers.get(qNumber).get(2));
						if(s!=null){
							correctAnswers++;
							qThread = false;
							surity = true;
							answerFlag = 3;
							Thread optionC = new Thread(this);
							optionC.start();
						}
						else{
							wrongAnswers++;
						}
						break;
					}
					else if(tokens[k].equalsIgnoreCase("D")){
						optionFlag=1;
						String s = JOptionPane.showInputDialog("Do you mean option D " + answers.get(qNumber).get(3));
						if(s!=null){
							correctAnswers++;
							qThread = false;
							surity = true;
							answerFlag = 4;
							Thread optionD = new Thread(this);
							optionD.start();
						}
						else{
							wrongAnswers++;
						}
						break;
					}
				}
				
				if(optionFlag==0){
				
				String[] answerTokens = correct.get(qNumber).split(delims);
				ArrayList<String> A = new ArrayList<String>();
				for (int j = 0; j < answerTokens.length; j++)
					A.add(answerTokens[j].toLowerCase());

				// find if any word uttered is present in answerTokens. if yes,
				// set flag =1.
				for (int i = 0; i < tokens.length; i++) {
					if (A.contains(tokens[i])) {
						flag = 1;
						break;
					}
				}
				// process each word. If that word has an answer option, then
				// ask user to confirm it.
				// make arraylist.
				
				if (flag == 1) { // answer chosen is correct.
					// ask user to confirm. If he says "yes" then proceed
					// "If he says "no" then ask him to repeat.
					str = JOptionPane.showInputDialog("Do you mean A. "
							+ correct.get(qNumber));
					if (str != null) // pressed OK button.
					{
						correctAnswers++;
						qThread = true;
						surity = false;
						// find which option number is it and set answerFlag.
						answerFlag = 1;
						Thread t3 = new Thread(this);
						t3.start();
						// qThread = false;
					} else {
						wrongAnswers++;
						// pressed cancel button.
						// dismiss dialog box and resume.
					}
				} else {
					// user answer not correct. He has uttered an answer which
					// is wrong. Find out what answer option he chose.
					ArrayList<ArrayList<String>> incorrectAnswers = new ArrayList<ArrayList<String>>();
					for (int j = 1; j < 4; j++) {
						// tokenize each incorrect answer and add to
						// corresponing arraylist.
						ArrayList<String> inc = new ArrayList<String>();
						String[] incorrectTokens = answers.get(qNumber).get(j)
								.toLowerCase().split(delims);
						for (int n = 0; n < incorrectTokens.length; n++) {
							inc.add(incorrectTokens[n]);
						}
						incorrectAnswers.add(inc);
					}

					for (int i = 0; i < tokens.length; i++) {
						// find which answer option is this token in.
						for (int x = 0; x < incorrectAnswers.size(); x++) {
							if (incorrectAnswers.get(x).contains(tokens[i].toLowerCase())) {
								flag = x + 2;
							}
						}
					}

					switch (flag) {
					case 2:
						str = JOptionPane.showInputDialog("Do you mean B. "
								+ answers.get(qNumber).get(1));
						if (str != null) {
							// if OK, then tell him answer is wrong.
							correctAnswers++;
							qThread = false;
							surity = true;
							answerFlag = flag;
							Thread t4 = new Thread(this);
							t4.start();
						} else {
							wrongAnswers++;
							// else dismiss dialog box and resume.
							// pressed cancel. resume.
						}
						break;

					case 3:
						str = JOptionPane.showInputDialog("Do you mean C. "
								+ answers.get(qNumber).get(2));
						if (str != null) {
							// if OK, then tell him answer is wrong.
							correctAnswers++;
							qThread = false;
							surity = true;
							answerFlag = flag;
							Thread t4 = new Thread(this);
							t4.start();
						} else {
							wrongAnswers++;
							// else dismiss dialog box and resume.
							// pressed cancel. resume.
						}
						break;

					case 4:
						str = JOptionPane.showInputDialog("Do you mean D. "
								+ answers.get(qNumber).get(3));
						if (str != null) {
							// if OK, then tell him answer is wrong.
							correctAnswers++;
							qThread = false;
							surity = true;
							answerFlag = flag;
							Thread t4 = new Thread(this);
							t4.start();
						} else {
							wrongAnswers++;
							// else dismiss dialog box and resume.
							// pressed cancel. resume.
						}
						break;

					default:
						// not in any answer option.
						// did he say any of quit/end/close/exit?
						for (int i = 0; i < tokens.length; i++) {
							if (tokens[i].equalsIgnoreCase("quit")
									|| tokens[i].equalsIgnoreCase("exit")
									|| tokens[i].equalsIgnoreCase("end")
									|| tokens[i].equalsIgnoreCase("close")) {
								str = JOptionPane
										.showInputDialog("Do you want to quit. You will take away "
												+ map.get(qNumber)
												+ " dollars. Click OK to Quit the game.");
								if (str != null) {
									correctAnswers++;
									JOptionPane.showMessageDialog(frame, "The statistics are :\n\n Answers correctly recognized : " + correctAnswers + "\n Answers wrongly recognized : " + wrongAnswers + "\n System Accuracy : " + ((double)correctAnswers/(correctAnswers+wrongAnswers))*100 + "%", "Evaluation Report", JOptionPane.INFORMATION_MESSAGE);
									recognizer.deallocate();
									//System.exit(1);
								} else {
									wrongAnswers++;
									// resume.
									break;
								}
							} else {
								// did he ask for a lifeline.
								// fifty | audience | poll | friend | phone |
								// call
								if(tokens[i].equalsIgnoreCase("fifty")){
									//remove B and D.
									if(fifty_fifty == true){
										str = JOptionPane.showInputDialog("Sorry you have already used this lifeline");
										break;
									}
									else{
									
										str = JOptionPane.showInputDialog("Do you want to use fift-fifty lifeline?");
									if(str!=null){
										fifty_fifty=true;
										correctAnswers++;
										answer2.setText(" ");
										answer4.setText(" ");
									}
									else{
										wrongAnswers++;
										break;
									}
									}
								}
								else if (tokens[i].equalsIgnoreCase("friend")
										|| tokens[i].equalsIgnoreCase("phone")
										|| tokens[i].equalsIgnoreCase("call")) {
									// wants to phone a friend? confirm first.
									str = JOptionPane
											.showInputDialog("Do you want to phone a friend?");
									if (str != null) {
										// yes, he wants to phone a friend.
										correctAnswers++;
										qThread = false;
										surity = false;
										if (phone == false) {
											phone = true;
											Thread ph = new Thread(this);
											ph.start();
										} else {
											String str1 = JOptionPane
													.showInputDialog("Sorry you have already used this lifeline.");
											// resume.
										}
										break;
									}
									else{
										wrongAnswers++;
									}
								} else if (tokens[i]
										.equalsIgnoreCase("audience")
										|| tokens[i].equalsIgnoreCase("poll")) {
									// audience | poll
									if(audPoll == true){
										str = JOptionPane
												.showInputDialog("Sorry. You have already used this lifeline");
										break;
									}
									else{
										str = JOptionPane
											.showInputDialog("Do you want to use audience poll?");
									if (str != null) {
										// yes, use audience poll.
										audPoll = true;
										correctAnswers++;
										String s1 = "A. "
												+ answers.get(qNumber).get(0)
												+ " : 60%";
										String s2 = "B. "
												+ answers.get(qNumber).get(1)
												+ " : 12%";
										String s3 = "C. "
												+ answers.get(qNumber).get(2)
												+ " : 20%";
										String s4 = "D. "
												+ answers.get(qNumber).get(3)
												+ " : 8%";
										String[] choices = { s1, s2, s3, s4 };

										int response = JOptionPane
												.showOptionDialog(
														null,
														"Select your choice",
														"Audience Poll",
														JOptionPane.OK_CANCEL_OPTION,
														JOptionPane.QUESTION_MESSAGE,
														null, choices, null);
										switch (response) {
										case 0:
											// option A selected.
											qThread = true;
											surity = false;
											answerFlag = 1;
											Thread optionA = new Thread(this);
											optionA.start();
											break;

										case 1:
											qThread = false;
											surity = true;
											answerFlag = 2;
											Thread optionB = new Thread(this);
											optionB.start();
											break;

										case 2:
											qThread = false;
											surity = true;
											answerFlag = 3;
											Thread optionC = new Thread(this);
											optionC.start();
											break;

										case 3:
											qThread = false;
											surity = true;
											answerFlag = 4;
											Thread optionD = new Thread(this);
											optionD.start();
											break;

										default:
											break;
										} //switch ends
										break;
									}  
									else{
										wrongAnswers++;
									}
									} //else if audience poll ends.
								}
								else{
									wrongAnswers++;
								}//else ends.
							} //for loop ends.

						} //switch flag ends.

					} //else ends.

				}}}
			else{
				JOptionPane.showInputDialog("Could not recognize what you said.");
			}
			// recognizer.deallocate();
			// deallocateSynthesizer();
		}

		private void systemSpeakContents() {
			try {
				System.setProperty("freetts.voices",
						"com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
				Central.registerEngineCentral("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral");
				synthesizer = Central
						.createSynthesizer(new SynthesizerModeDesc(Locale.US));
				synthesizer.allocate();
				synthesizer.resume();
				me = new trial(trial.VOICE_KEVIN);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			//new MakeSound().playSound("src\\newQuestion.wav");
			// speak out the question and answer options.
			me.open();
			String resultText = "Here is the question for " + wordMap.get(map.get(qNumber+1)) + " dollars";
			me.speak(resultText);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			me.speak(questions.get(qNumber));

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			me.speak("and the options are " + answers.get(qNumber).get(0) + " "
					+ answers.get(qNumber).get(1) + " "
					+ answers.get(qNumber).get(2) + " "
					+ answers.get(qNumber).get(3));
			me.close();
			// deallocateSynthesizer();
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			// deallocate recognizer.
			recognizer.deallocate();
			
			//System.out.println(result);
			
			try {
				speakContents();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//recognizer.deallocate();
		}

		
		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			result=null;
			recognizer = (Recognizer) cm.lookup("recognizer");
			recognizer.allocate();
			
			Microphone microphone = (Microphone) cm.lookup("microphone");
			if (!microphone.isRecording()) {
				if (!microphone.startRecording()) {
					// System.out.println("Cannot start microphone.");
					recognizer.deallocate();
					System.out.println("Cannot start microphone.");
					System.exit(1);
				}
			}

			// System.out.println("Say: (Good morning | Hello) ( Bhiksha | Evandro | Paul | Philip | Rita | Will )");
			//String res = JOptionPane.showInputDialog("Click OK and then start speaking");
			//if(res!=null){
				result = recognizer.recognize();
			//}
			/*try {
				Thread.sleep(2000);
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}*/
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub

		}

	}

	public void speak(String msg) {
		voice.speak(msg);

	}

	public static void addQuestionToScreen(int index) {
		// TODO Auto-generated method stub
		QuestionPanel mQuestion = new QuestionPanel(new BorderLayout(), index);
		mQuestion.addPanel();
		// mQuestion.add(big, BorderLayout.WEST);
		qPanel.setBackground(Color.BLACK);
		qPanel.add(mQuestion);
		frame.getContentPane().add(qPanel, BorderLayout.SOUTH);
	}

	public void open() {
		voice.allocate();
	}

	public void close() {
		voice.deallocate();
	}

	private static JMenuBar createMenuBar() {
		// TODO Auto-generated method stub
		JMenuBar menuBar;
		JMenu menu, menu2;
		JMenuItem menuItem;

		// create the menu bar.
		menuBar = new JMenuBar();

		// Build the first menu.
		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);

		menu2 = new JMenu("Instructions");
		menu2.setMnemonic(KeyEvent.VK_I);
		menuBar.add(menu2);

		// a group of JMenuItems
		menuItem = new JMenuItem("About the game", KeyEvent.VK_A);
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Learn how the game was made");
		menuItem.addActionListener(new aboutGame());
		menu2.add(menuItem);

		menuItem = new JMenuItem("Important Notes", KeyEvent.VK_N);
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Important notes");
		menuItem.addActionListener(new notes());
		menu2.add(menuItem);
		
		menuItem = new JMenuItem("How to answer a question", KeyEvent.VK_Q);
		menuItem.getAccessibleContext().setAccessibleDescription(
				"How to answer a question");
		menuItem.addActionListener(new answerQuestion());
		menu2.add(menuItem);
		
		menuItem = new JMenuItem("How to use a lifeline", KeyEvent.VK_L);
		menuItem.getAccessibleContext().setAccessibleDescription(
				"How to use a lifeline");
		menuItem.addActionListener(new useLifeline());
		menu2.add(menuItem);
		
		menuItem = new JMenuItem("How to quit the game", KeyEvent.VK_G);
		menuItem.getAccessibleContext().setAccessibleDescription(
				"How to quit the game");
		menuItem.addActionListener(new quitGame());
		menu2.add(menuItem);
	
		menuItem = new JMenuItem("Begin", KeyEvent.VK_B);
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Begin the game");
		menuItem.addActionListener(new beginApp());
		menu.add(menuItem);

		menuItem = new JMenuItem("Exit", KeyEvent.VK_X);
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Exit the game");
		menuItem.addActionListener(new exitApp());
		menu.add(menuItem);

		return menuBar;
	}

	static class beginApp implements ActionListener {
		@SuppressWarnings("deprecation")
		public void actionPerformed(ActionEvent e) {
			// JOptionPane.showMessageDialog(frame, "About game",
			// "Instructions", JOptionPane.INFORMATION_MESSAGE);

			// handler.unpause();
			boardP = new JPanel();
			BoardPanel boardPanel = new BoardPanel(
					new ImageIcon(
							"images\\1.jpg")
							.getImage());
			boardP.setBackground(Color.BLACK);
			boardP.add(boardPanel);
			frame.getContentPane().add(boardP, BorderLayout.EAST);

			mSignPosition = true;

			ImagePanel mSign = new ImagePanel(
					new ImageIcon(
							"images\\background.jpg")
							.getImage());
			// mSign.add(j);
			frame.getContentPane().add(j, BorderLayout.CENTER);

			// change image at the south to question.jpg
			// QuestionPanel mQuestion = new QuestionPanel(new
			// ImageIcon("C:\\Users\\Pulkit\\WorkspaceJuno\\trial\\question.jpg").getImage());
			qPanel = new JPanel();
			addQuestionToScreen(0);

			frame.addMouseListener(handler);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(800, 600);
			frame.setVisible(true);

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			beginThread();
		}

		private void beginThread() {
			// TODO Auto-generated method stub
			introThread = false;
			handler.begin();
			t1 = new Thread(new trial().new Handlerclass());
			t1.start();
		}
	}

	static class aboutGame implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			/*JOptionPane.showMessageDialog(frame, "About game", "Instructions",
					JOptionPane.INFORMATION_MESSAGE);*/
			JTextArea about = new JTextArea(14, 40);
			String msg = "Welcome to the game of Who Wants To Be A Millionaire. " + 
					"This is a dialogue \nsystem implemented using CMU's Sphinx4 " + 
					"for speech recognition and FreeTTS\n for text-to-speech synthesis\n\n" + 
					"The game involves a Host(computer) and a Player(you). Your goal is " +
					"to answer\n 15 consecutive questions with four options (one of which is correct) " + 
					"correctly to \nbecome a millionaire. As each question is correctly answered, " +
					"the amount of\n money won increases according to the question's worth. " + 
					"You can Quit the game\n whenever you want. You can make use of following " +
					"lifelines :\n\n\"Fifty-Fifty\" : removes two incorrect options\n\"Phone A Friend\" : take advice from a friend\n" +
					"\"Audience Poll\" : take the audience's opinion\n\nHope you enjoy the game! \n\nLet's Play!";
			
			about.setText(msg);
			about.setEditable(false);
			JScrollPane scrollPane = new JScrollPane(about);
			JOptionPane.showMessageDialog(frame, scrollPane);
		}
	}

	static class exitApp implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if((correctAnswers + wrongAnswers)!=0)
			JOptionPane.showMessageDialog(frame, "The statistics are :\n\n Answers correctly recognized : " + correctAnswers + "\n Answers wrongly recognized : " + wrongAnswers + "\n System Accuracy : " + ((double)correctAnswers/(correctAnswers+wrongAnswers))*100 + "%", "Evaluation Report", JOptionPane.INFORMATION_MESSAGE);
			System.exit(1);
		}
	}

	static class notes implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String s = "Notes:\n" + 
						"1. Wait for the host to read the question and the answer options.\n" +
						"2. You must press down the left mouse button, wait for 2-3 seconds as the system takes time to load the recognizer and to start the microphone,\n record your speech and then release the mouse.\n" +
						"3. Before you press the mouse button, make sure the cursor is not on the question panel.\n" +
						"4. At the end of each session, a system performance report is generated which lets the user " +
						"know the system's accuracy for the last concluded session.";
	
			JOptionPane.showMessageDialog(frame, s, "Important Notes", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	static class quitGame implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String s = "Exit/Quit the game:\n" + 
						"Example sentences ->\n" +
						"1. Quit the game\n" +
						"2. Exit\n" +
						"3. Close\n" +
						"4. End\n" +
						"5. Quit\n\n" + 
						"You can also go to File and click Exit to quit the game";
			JOptionPane.showMessageDialog(frame, s, "How to quit the game", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	static class useLifeline implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String s = "Using lifelines :\n" +
						"Example sentences ->\n" +
						"1. audience\n" +
						"2. audience poll\n" +
						"3. phone\n" +
						"4. phone a friend\n" +
						"5. fifty fifty\n" +
						"6. poll";
			JOptionPane.showMessageDialog(frame, s, "How to use a lifeline", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	static class answerQuestion implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String s = "How to answer a question:\n" +
			"Hold down the left mouse button (make sure the cursor is on the " + "screen). Wait for 2-3 seconds and then speak. " +
			"After you are done speaking, release the mouse button.\n\n" +
				"If you think the answer is for instance option A : Elephant, you can tell the host about your choice in several ways:\n" +
					"a) A.\n" +
					"b) A elephant.\n" +
					"c) elephant.\n";
			JOptionPane.showMessageDialog(frame, s, "How to answer a question", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	

	static class QuestionPanel extends JPanel {
		private Image img;
		public JPanel big;

		public QuestionPanel(String img) {
			this(new ImageIcon(img).getImage());
		}

		public void addPanel() {
			// TODO Auto-generated method stub
			this.add(big, BorderLayout.WEST);
		}

		public QuestionPanel() {
		}

		public QuestionPanel(Image image) {
			// TODO Auto-generated constructor stub
			this.img = image;
			Dimension size = new Dimension(img.getWidth(null),
					img.getHeight(null));
			setPreferredSize(size);
			setMinimumSize(size);
			setMaximumSize(size);
			setSize(size);
			setLayout(null);
		}

		public QuestionPanel(BorderLayout borderLayout, int i) {
			// TODO Auto-generated constructor stub
			big = new JPanel();
			big.setLayout(new BorderLayout());

			JPanel panel = new JPanel(new GridLayout(2, 1));

			JPanel qtn = new JPanel();
			qtn.setLayout(new GridLayout(1, 1));
			questionButton = new JButton();
			questionButton.setText(questions.get(i));
			questionButton.setBackground(new Color(28, 54, 104));
			questionButton.setForeground(Color.WHITE);
			questionButton.setFont(f);
			qtn.add(questionButton);
			panel.add(qtn);

			JPanel a = new JPanel(new GridLayout(2, 2));
			answer1 = new JButton(answers.get(i).get(0));
			answer2 = new JButton(answers.get(i).get(1));
			answer3 = new JButton(answers.get(i).get(2));
			answer4 = new JButton(answers.get(i).get(3));

			answer1.setIcon(new ImageIcon(
					"images\\A.jpg"));
			answer1.setHorizontalAlignment(SwingConstants.LEFT);
			answer1.setBackground(new Color(28, 54, 104));
			answer1.setForeground(Color.WHITE);
			answer1.setFont(f);

			answer2.setIcon(new ImageIcon(
					"images\\B.jpg"));
			answer2.setHorizontalAlignment(SwingConstants.LEFT);
			answer2.setBackground(new Color(28, 54, 104));
			answer2.setForeground(Color.WHITE);
			answer2.setFont(f);

			answer3.setIcon(new ImageIcon(
					"images\\C.jpg"));
			answer3.setHorizontalAlignment(SwingConstants.LEFT);
			answer3.setBackground(new Color(28, 54, 104));
			answer3.setForeground(Color.WHITE);
			answer3.setFont(f);

			answer4.setIcon(new ImageIcon(
					"images\\D.jpg"));
			answer4.setHorizontalAlignment(SwingConstants.LEFT);
			answer4.setBackground(new Color(28, 54, 104));
			answer4.setForeground(Color.WHITE);
			answer4.setFont(f);

			a.add(answer1);
			a.add(answer2);
			a.add(answer3);
			a.add(answer4);
			panel.add(a);

			panel.setPreferredSize(new Dimension(800, 200));

			big.add(panel, BorderLayout.CENTER);

		}

	}

	static class ImagePanel extends JPanel {
		private Image img;

		public ImagePanel(String img) {
			this(new ImageIcon(img).getImage());
		}

		public ImagePanel(Image image) {
			// TODO Auto-generated constructor stub

			// add mouse listener to j
			j = new JPanel(new BorderLayout());
			JPanel newpan = new JPanel();
			JLabel jl = new JLabel(new ImageIcon(image));

			this.img = image;
			Dimension size = new Dimension(img.getWidth(null),
					img.getHeight(null));
			jl.setPreferredSize(size);
			jl.setMinimumSize(size);
			jl.setMaximumSize(size);
			jl.setSize(size);
			jl.setOpaque(true);


			j.add(jl, BorderLayout.CENTER);
			j.add(newpan, BorderLayout.SOUTH);
			j.setPreferredSize(new Dimension(100, 100));
			// setLayout(null);

		}

		public void paintComponent(Graphics g) {

			if (!mSignPosition)
				g.drawImage(img, 0, 0, 800, 600, null);
			else
				g.drawImage(img, 0, 0, 614, 340, null);

		}
	}

	static class BoardPanel extends JPanel {
		private Image img;

		public BoardPanel(String img) {
			this(new ImageIcon(img).getImage());
		}

		public BoardPanel(Image image) {
			// TODO Auto-generated constructor stub
			this.img = image;
			Dimension size = new Dimension(img.getWidth(null),
					img.getHeight(null));
			setPreferredSize(size);
			setMinimumSize(size);
			setMaximumSize(size);
			setSize(size);
			setLayout(null);
		}

		public void paintComponent(Graphics g) {
			g.drawImage(img, 0, 0, img.getWidth(null), img.getHeight(null)-5,
					null);
		}
	}

}
