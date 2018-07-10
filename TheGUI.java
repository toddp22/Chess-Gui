import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.JTextComponent;
@SuppressWarnings("unused")

public class TheGUI implements ActionListener{
		private final int BOARD_SIZE = 4;
		private final int NUM_OF_COOR = 3;
		private final int LENGTH = 15;
		private final int HEIGHT = 25;
		private JFrame frame;
		private JPanel panel;
		private JPanel panelOne;
		private JPanel panelTwo;
		private JPanel panelThree;
		private JPanel panelFour;
		private JTextArea subRules;
		private JTextArea boards;
		private JTextArea files;
		private JTextArea directory;
		private JScrollPane scrollDirectory;
		private JScrollPane scrollFiles;
		private JScrollPane scrollSubRules;
		private JScrollPane scrollBoards;
		private JLabel label1;
		private JLabel label2;
		private JLabel label3;
		private JLabel label4;
		private String dirName = "";
		private JButton back;
		private Path dir;
		private File file;
		private String fileDirName;
		private String board[][] = new String[BOARD_SIZE][BOARD_SIZE];
		//board variables
		private JPanel boardViewer;
		private ImageIcon iibk; //image icon for black king
		private ImageIcon iiwk; //image icon for white king
		private ImageIcon iiwr; //image icon for white rook
		
		//constructor
		public TheGUI(){
			//initial layout of app
			frame = new JFrame("Chess Board Visualizer");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setPreferredSize(new Dimension(1000,500));
			frame.setResizable(false);
			panel = new JPanel(new FlowLayout());
			panelOne = new JPanel();     
			panelOne.setLayout(new BoxLayout(panelOne, BoxLayout.Y_AXIS));
			panelTwo = new JPanel();    
			panelTwo.setLayout(new BoxLayout(panelTwo, BoxLayout.Y_AXIS));
			panelThree = new JPanel();   
			panelThree.setLayout(new BoxLayout(panelThree, BoxLayout.Y_AXIS));
			panelFour = new JPanel();
			panelFour.setLayout(new BoxLayout(panelFour, BoxLayout.Y_AXIS));
			panelFour.setPreferredSize(new Dimension(250,420));		
			//directory section of app
			label4 = new JLabel("Directory");
			panelOne.add(label4);
			directory = new JTextArea(HEIGHT,LENGTH);
			scrollDirectory = new JScrollPane(directory,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			directory.setEditable(false);
			//what happens when clicking on this section
			directory.addMouseListener(new MouseAdapter(){
				public void mouseClicked(MouseEvent e) {
					DefaultHighlighter h1 = (DefaultHighlighter)(directory.getHighlighter());
					h1.removeAllHighlights();
					h1.setDrawsLayeredHighlights(false);
					try{
						int line = directory.getCaretPosition();
						int start = directory.getLineStartOffset(directory.getLineOfOffset(line));	
						int end = directory.getLineEndOffset(directory.getLineOfOffset(line));
						dirName = dirName + "\\" + (directory.getText(start, end - start)).replaceAll("\n", "");
						loadDirectoryContents();
						h1.addHighlight(start, end, new DefaultHighlighter.DefaultHighlightPainter(Color.CYAN));
						
						back.setEnabled(true);
					}catch(Exception e1){
					}
					directory.setCaretPosition(0);
					files.setCaretPosition(0);
				}
			});
			panelOne.add(scrollDirectory);
			//button to go back to the home directory
			back = new JButton("<<<");
			back.setSize(75, 30);
			back.addActionListener(this);
			panelOne.add(back);
			
			//files section of app
			label1 = new JLabel("Files: ");
			panelTwo.add(label1);
			files = new JTextArea(HEIGHT,LENGTH);
			files.setLineWrap(true);
			files.setWrapStyleWord(true);
			scrollFiles = new JScrollPane(files,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			files.setEditable(false);
			//what happens when you click on this section
			files.addMouseListener(new MouseAdapter(){
				public void mouseClicked(MouseEvent e) {
					DefaultHighlighter h2 = (DefaultHighlighter)(files.getHighlighter());
					h2.removeAllHighlights();
					h2.setDrawsLayeredHighlights(false);
					try{
						int line = files.getCaretPosition();
						int start = files.getLineStartOffset(files.getLineOfOffset(line));	
						int end = files.getLineEndOffset(files.getLineOfOffset(line));
						fileDirName = dir.toAbsolutePath().toString() + "\\" + (files.getText(start, end - start)).replaceAll("\n", "");
						if(fileDirName.endsWith(".out") || fileDirName.endsWith(".OUT")){
							displaySubrules();
						}
						h2.addHighlight(start, end, new DefaultHighlighter.DefaultHighlightPainter(Color.CYAN));
						
						back.setEnabled(true);
					}catch(Exception e1){
					}	
					subRules.setCaretPosition(0);
					clearBoards();
					scrollBoards.updateUI();
				}
			});
			panelTwo.add(scrollFiles);
			
			//subrules section
			label2 = new JLabel("SubRules: ");
			panelThree.add(label2);
			subRules = new JTextArea(HEIGHT,LENGTH+10);
			scrollSubRules = new JScrollPane(subRules,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			subRules.setEditable(false);
			//what happens when someone clicks on it
			subRules.addMouseListener(new MouseAdapter(){
				public void mouseClicked(MouseEvent e) {
					DefaultHighlighter h2 = (DefaultHighlighter)(subRules.getHighlighter());
					h2.removeAllHighlights();
					h2.setDrawsLayeredHighlights(false);
					try{
						int line = subRules.getLineOfOffset(subRules.getCaretPosition());
						int start = subRules.getLineStartOffset(line);	
						int end = subRules.getLineEndOffset(line);
						String subRule = (subRules.getText(start, end - start)).replaceAll("\n", "");
						h2.addHighlight(start, end, new DefaultHighlighter.DefaultHighlightPainter(Color.CYAN));
						//if(subRule.matches(".*\\d+.*")){
						if(subRule.startsWith("BestRule is:")){
							clearBoards();
							String temp = getNextLine(line+1);
							while(!(temp.startsWith("BestRule is:")) && line + 1 < subRules.getLineCount()){
								if(temp.matches(".*\\d+.*")){
									subRule = subRule + "" + temp;
								}
								line += 1;
								temp = getNextLine(line);					
							}
							convertSubrules(subRule.replaceAll("BestRule is:", ""));
						}else if(subRule.matches(".*\\d+.*")){
							clearBoards();
							convertSubrules(subRule);
						}
						scrollBoards.updateUI();
						back.setEnabled(true);
					}catch(Exception e1){
					}	
				}
			});
			panelThree.add(scrollSubRules);
			//initializes the board section of the app
			label3 = new JLabel("Boards: ");
			panelFour.add(label3);
			boardViewer = new JPanel(new GridLayout(1,1,5,15));
			boardViewer.setBackground(Color.orange);
			boardViewer.setOpaque(true);
			scrollBoards = new JScrollPane(boardViewer,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollBoards.getVerticalScrollBar().setUnitIncrement(16);
			panelFour.add(scrollBoards);
			//adds the four panels to the app
			panel.add(panelOne);
			panel.add(panelTwo);
			panel.add(panelThree);
			panel.add(panelFour);
			frame.getContentPane().add(panel);
			frame.pack();
			frame.setVisible(true);
		}
		//used to get the next line when parsing "the best rule is" scenario
		public String getNextLine(int line){
			try {
				int start = subRules.getLineStartOffset(line);
				int end = subRules.getLineEndOffset(line);
				return (subRules.getText(start, end - start)).replaceAll("\n", "");
				
			} catch (BadLocationException e) {
				e.printStackTrace();
			}	
			return " ";
		}		
		//resets everything
		public void actionPerformed(ActionEvent arg) {
			if(arg.getSource() == back){
				clearBoards();
				loadHomeDirectory();
				scrollBoards.updateUI();
			}
		}
		//initializes app
		void run(){
			loadHomeDirectory();
			//load all pictures necessary to display chess boards
			try {
				Image bk = ImageIO.read(new File("BK.png"));
				Image wk = ImageIO.read(new File("WK.png"));
				Image wr = ImageIO.read(new File("WR.png"));
				iibk = new ImageIcon(bk.getScaledInstance(25, 25, Image.SCALE_SMOOTH));
				iiwk = new ImageIcon(wk.getScaledInstance(25, 25, Image.SCALE_SMOOTH));
				iiwr = new ImageIcon(wr.getScaledInstance(25, 25, Image.SCALE_SMOOTH));
			} catch (IOException e) {
			}	
		}
		//where the directory starts
		//in this case it starts at desktop
		private void loadHomeDirectory(){
			File home = FileSystemView.getFileSystemView().getHomeDirectory(); //starts at desktop
			//File home = new File("C:\\");   //starts at C drive
			dirName = home.getAbsolutePath();
			loadDirectoryContents();
			subRules.setText("");
			back.setEnabled(false);
		}
		//loads the contents of the directory 
		//if folders it displays them in the directory section
		//if they have the .out extension they are displayed in the files section
		private void loadDirectoryContents(){
			if(Files.isDirectory(Paths.get(dirName.trim()))){
				dir = Paths.get(dirName.trim());
				directory.setText("");
				
				try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) { //"[*]$(^[^.].)"
				    for (Path file2: stream) {
				    	if(Files.isDirectory(file2)){
				    		directory.append(file2.getFileName() + "\n");
				    	}
				    }
				} catch (IOException | DirectoryIteratorException x) {
				    //System.err.println(x);
				}
			}else{
				file = new File(dirName);
				dirName = dir.toString();
			}
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{out}")) {
				files.setText("");
				for (Path file2: stream) {
			    	files.append(file2.getFileName() + "\n");
			    }
			} catch (IOException | DirectoryIteratorException x) {
			    //System.err.println(x);
			}
		}
		//displays the subrules to the subrules section of the app
		private void displaySubrules(){
			subRules.setText("");
			String line;
			try{
				Scanner in = new Scanner(new File(fileDirName));
				while(in.hasNextLine()){
					line = in.nextLine();
					//if(line.matches(".*\\d+.*")){  //IF you want the subrules to just contain rules and not anything else
					subRules.append(line +"\n");
				}
				in.close();
			}catch(IOException error){
				System.out.println(error.getMessage());
			}

		}
		//converts the subrules to a array board
		private void convertSubrules(String line){
			ArrayList<Integer> row = new ArrayList<Integer>();
			ArrayList<Integer> col = new ArrayList<Integer>();
			String pieces[] = {"BK","WK","WR"};
			String newLine = ((line.replaceAll(", ", "")).replaceAll("mate[0-9]", "")).replaceAll("\n", "");
			//System.out.println(newLine);
			for(int i = newLine.indexOf("("); i < newLine.length(); i++){
				if((newLine.substring(i, i+1)).matches("\\d+")){
					row.add(Integer.parseInt(newLine.substring(i, i+1)));
					i++;  //with every row there is a column
					col.add(Integer.parseInt(newLine.substring(i, i+1)));
				}
			}
			
			int k = 0;
			boardViewer.setLayout(new GridLayout(1,1,5,15));
			for(int p=0;p<(row.size()/NUM_OF_COOR);p++){
				//initialize board
				for(int i = 0; i < BOARD_SIZE; i++){
					for(int j = 0; j < BOARD_SIZE; j++){
						board[i][j] = "~E";
					}
				}
				//assign pieces to the board
				for(int i = 0; i < NUM_OF_COOR; i++){
					board[row.get(k)][col.get(k)] = pieces[i];
					k++;
				}
				createBoard();
			}
			//number of chess boards in the display
			if (k==3){
				k=6;
			}
			boardViewer.setLayout(new GridLayout(k/3,1,5,15));
		}
		// create a board on the fly
		private void createBoard(){
			JPanel chessBoard = new JPanel(new GridLayout(4,4,0,0));
			JLabel chessTiles[][] = new JLabel[BOARD_SIZE][BOARD_SIZE];
			int color=0;
			for(int i = 0;i < BOARD_SIZE; i++){
				for(int k=0;k < BOARD_SIZE;k++){
					//creates the row
					chessTiles[i][k] = new JLabel("");
					chessTiles[i][k].setPreferredSize(new Dimension(21,45));
					chessTiles[i][k].setOpaque(true);
					chessTiles[i][k].setHorizontalAlignment(SwingConstants.CENTER);
					
					//background color of squares
					if(color==0){
						chessTiles[i][k].setBackground(new Color(205,133,63)); //peru Brown
						color=1;
					}else{
						color=0;
						chessTiles[i][k].setBackground(new Color(255,248,220)); //cornsilk cream
					}	
					chessBoard.add(chessTiles[i][k]);
					
					//assign pieces to the board
					if(board[i][k].equals("BK")){
						chessTiles[i][k].setIcon(iibk);
					}else if(board[i][k].equals("WK")){
						chessTiles[i][k].setIcon(iiwk);
					}else if(board[i][k].equals("WR")){
						chessTiles[i][k].setIcon(iiwr);
					}
				}
				//switches the color back and forth for the column
				if(color == 0)
					color=1;
				else
					color=0;
			}
			//adds the newly created board to the display
			boardViewer.add(chessBoard);
		}
		//clears the board section of the application
		private void clearBoards(){
			boardViewer.removeAll();
			boardViewer.setLayout(new GridLayout(1,1,5,15));
		}
		
}	
		
