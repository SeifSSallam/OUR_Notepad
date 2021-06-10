package MyNote;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.UndoManager;

//import p1.FontChooser;
//import p1.FontDialog;
//import p1.FindDialog;
//import p1.LookAndFeelMenu;
//import p1.MyFileFilter;
/************************************/

/************************************/
class FileOperation {
	Notepad npd;

	boolean saved;
	boolean newFileFlag;
	String fileName;
	String applicationTitle = "Javapad";

	File fileRef;
	JFileChooser chooser;
	ASCIIWriter ascii = new ASCIIWriter();

/////////////////////////////
	boolean isSave() {
		return saved;
	}

	void setSave(boolean saved) {
		this.saved = saved;
	}

	String getFileName() {
		return new String(fileName);
	}

	void setFileName(String fileName) {
		this.fileName = new String(fileName);
	}

/////////////////////////
	FileOperation(Notepad npd) {
		this.npd = npd;

		saved = true;
		newFileFlag = true;
		fileName = new String("Untitled");
		fileRef = new File(fileName);
		this.npd.f.setTitle(fileName + " - " + applicationTitle);

		chooser = new JFileChooser();
		chooser.addChoosableFileFilter(new MyFileFilter(".java", "Java Source Files(*.java)"));
		chooser.addChoosableFileFilter(new MyFileFilter(".txt", "Text Files(*.txt)"));
		chooser.setCurrentDirectory(new File("."));

	}
//////////////////////////////////////
	
	boolean saveFile(File temp) {
		FileWriter fout = null;
		try {
			fout = new FileWriter(temp);
			fout.write(npd.ta.getText());
		} catch (IOException ioe) {
			updateStatus(temp, false);
			return false;
		} finally {
			try {
				fout.close();
			} catch (IOException excp) {
			}
		}
		updateStatus(temp, true);
		return true;
	}

////////////////////////
	boolean saveThisFile() {

		if (!newFileFlag) {
			return saveFile(fileRef);
		}

		return saveAsFile();
	}

////////////////////////////////////
	boolean saveAsFile() {
		File temp = null;
		chooser.setDialogTitle("Save As...");
		chooser.setApproveButtonText("Save Now");
		chooser.setApproveButtonMnemonic(KeyEvent.VK_S);
		chooser.setApproveButtonToolTipText("Click me to save!");

		do {
			if (chooser.showSaveDialog(this.npd.f) != JFileChooser.APPROVE_OPTION)
				return false;
			temp = chooser.getSelectedFile();
			if (!temp.exists())
				break;
			if (JOptionPane.showConfirmDialog(this.npd.f,
					"<html>" + temp.getPath() + " already exists.<br>Do you want to replace it?<html>", "Save As",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
				break;
		} while (true);

		return saveFile(temp);
	}

////////////////////////
	boolean openFile(File temp) {
		FileInputStream fin = null;
		BufferedReader din = null;

		try {
			fin = new FileInputStream(temp);
			din = new BufferedReader(new InputStreamReader(fin));
			String str = " ";
			while (str != null) {
				str = din.readLine();
				if (str == null)
					break;
				this.npd.ta.append(str + "\n");
			}

		} catch (IOException ioe) {
			updateStatus(temp, false);
			return false;
		} finally {
			try {
				din.close();
				fin.close();
			} catch (IOException excp) {
			}
		}
		updateStatus(temp, true);
		this.npd.ta.setCaretPosition(0);
		return true;
	}

///////////////////////
	void openFile() {
		if (!confirmSave())
			return;
		chooser.setDialogTitle("Open File...");
		chooser.setApproveButtonText("Open this");
		chooser.setApproveButtonMnemonic(KeyEvent.VK_O);
		chooser.setApproveButtonToolTipText("Click me to open the selected file.!");

		File temp = null;
		do {
			if (chooser.showOpenDialog(this.npd.f) != JFileChooser.APPROVE_OPTION)
				return;
			temp = chooser.getSelectedFile();

			if (temp.exists())
				break;

			JOptionPane.showMessageDialog(this.npd.f,
					"<html>" + temp.getName() + "<br>file not found.<br>"
							+ "Please verify the correct file name was given.<html>",
					"Open", JOptionPane.INFORMATION_MESSAGE);

		} while (true);
		try {
			if(ImageIO.read(temp) != null)
			{
				BufferedImage img = ImageIO.read(temp);
				this.npd.ta.setText(ascii.WriteTXT(img));
				updateStatus(temp, true);
				this.npd.ta.setCaretPosition(0);
			}
			else {
				this.npd.ta.setText("");
				if (!openFile(temp)) {
					fileName = "Untitled";
					saved = true;
					this.npd.f.setTitle(fileName + " - " + applicationTitle);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (!temp.canWrite())
			newFileFlag = true;
	}

////////////////////////
	void updateStatus(File temp, boolean saved) {
		if (saved) {
			this.saved = true;
			fileName = new String(temp.getName());
			if (!temp.canWrite()) {
				fileName += "(Read only)";
				newFileFlag = true;
			}
			fileRef = temp;
			npd.f.setTitle(fileName + " - " + applicationTitle);
			npd.statusBar.setText("File : " + temp.getPath() + " saved/opened successfully.");
			newFileFlag = false;
		} else {
			npd.statusBar.setText("Failed to save/open : " + temp.getPath());
		}
	}

///////////////////////
	boolean confirmSave() {
		String strMsg = "<html>The text in the " + fileName + " file has been changed.<br>"
				+ "Do you want to save the changes?<html>";
		if (!saved) {
			int x = JOptionPane.showConfirmDialog(this.npd.f, strMsg, applicationTitle,
					JOptionPane.YES_NO_CANCEL_OPTION);

			if (x == JOptionPane.CANCEL_OPTION)
				return false;
			if (x == JOptionPane.YES_OPTION && !saveAsFile())
				return false;
		}
		return true;
	}

///////////////////////////////////////
	void newFile1() {                   //new ordinary txt file
		if (!confirmSave())
			return;

		this.npd.ta.setText("");
		fileName = new String("Untitled");
		fileRef = new File(fileName);
		saved = true;
		newFileFlag = true;
		this.npd.f.setTitle(fileName + " - " + applicationTitle);
	}
//////////////////////////////////////

void newFile2() {                   //new C file
	if (!confirmSave())
		return;

	this.npd.ta.setText("#include <stdio.h>\n"
			+ "\n"
			+ "int main()\n"
			+ "{\n"
			+ "    printf(\"Hello World\");\n"
			+ "\n"
			+ "    return 0;\n"
			+ "}\n");
	fileName = new String("Untitled");
	fileRef = new File(fileName);
	saved = true;
	newFileFlag = true;
	this.npd.f.setTitle(fileName + " - " + applicationTitle);
}

void newFile3() {                   //new C++ file
	if (!confirmSave())
		return;

	this.npd.ta.setText("#include <stdio.h>\n"
			+ "#include <bits/stdc++.h>\n"
			+ "\n"
			+ "using namespace std;\n"
			+ "\n"
			+ "int main()\n"
			+ "{\n"
			+ "    cout << \"Hello World\\n\";\n"
			+ "\n"
			+ "    return 0;\n"
			+ "}");
	fileName = new String("Untitled");
	fileRef = new File(fileName);
	saved = true;
	newFileFlag = true;
	this.npd.f.setTitle(fileName + " - " + applicationTitle);
}

void newFile4() {                   //new JAVA file
	if (!confirmSave())
		return;

	this.npd.ta.setText("public class Main\n"
			+ "{\n"
			+ "	public static void main(String[] args) {\n"
			+ "		System.out.println(\"Hello World\");\n"
			+ "	}\n"
			+ "}\n");
	fileName = new String("Untitled");
	fileRef = new File(fileName);
	saved = true;
	newFileFlag = true;
	this.npd.f.setTitle(fileName + " - " + applicationTitle);
}

void newFile5() {                   //new HTML,JS,CSS file
	if (!confirmSave())
		return;

	this.npd.ta.setText("<html>\n"
			+ "<body>\n"
			+ "<h1>Hello World</h1>\n"
			+ "</body>\n"
			+ "</html>");
	fileName = new String("Untitled");
	fileRef = new File(fileName);
	saved = true;
	newFileFlag = true;
	this.npd.f.setTitle(fileName + " - " + applicationTitle);
}

//////////////////////////////////////
}// end defination of class FileOperation

/************************************/
public class Notepad implements ActionListener, MenuConstants {

	JFrame f;
	JTextArea ta;
	JLabel statusBar;
	UndoManager undoManager = new UndoManager();

	private String fileName = "Untitled";
	private boolean saved = true;
	String applicationName = "Javapad";

	String searchString, replaceString;
	int lastSearchIndex;
	
	Timer timer;
	
	FileOperation fileHandler;
	FontChooser fontDialog = null;
	FindDialog findReplaceDialog = null;
	JColorChooser bcolorChooser = null;
	JColorChooser fcolorChooser = null;
	JDialog backgroundDialog = null;
	JDialog foregroundDialog = null;
	JMenuItem cutItem, copyItem, deleteItem, findItem, findNextItem, replaceItem, gotoItem, selectAllItem;

	/****************************/
	Notepad() {
		f = new JFrame(fileName + " - " + applicationName);
		ta = new JTextArea(30, 60);
		statusBar = new JLabel("||       Ln 1, Col 1  ", JLabel.RIGHT);
		f.add(new JScrollPane(ta), BorderLayout.CENTER);
		f.add(statusBar, BorderLayout.SOUTH);
		f.add(new JLabel("  "), BorderLayout.EAST);
		f.add(new JLabel("  "), BorderLayout.WEST);
		createMenuBar(f);
//f.setSize(350,350);
		f.pack();
		f.setLocation(100, 50);
		f.setVisible(true);
		f.setLocation(150, 50);
		f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		fileHandler = new FileOperation(this);

/////////////////////

		ta.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				int lineNumber = 0, column = 0, pos = 0;

				try {
					pos = ta.getCaretPosition();
					lineNumber = ta.getLineOfOffset(pos);
					column = pos - ta.getLineStartOffset(lineNumber);
				} catch (Exception excp) {
				}
				if (ta.getText().length() == 0) {
					lineNumber = 0;
					column = 0;
				}
				statusBar.setText("||       Ln " + (lineNumber + 1) + ", Col " + (column + 1));
			}
		});
		
		ta.getDocument().addUndoableEditListener(
		        new UndoableEditListener() {
		          public void undoableEditHappened(UndoableEditEvent e) {
		            undoManager.addEdit(e.getEdit());
		          }
		        });
//////////////////
		DocumentListener myListener = new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				fileHandler.saved = false;
			}

			public void removeUpdate(DocumentEvent e) {
				fileHandler.saved = false;
			}

			public void insertUpdate(DocumentEvent e) {
				fileHandler.saved = false;
			}
		};
		ta.getDocument().addDocumentListener(myListener);
/////////
		WindowListener frameClose = new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				if (fileHandler.confirmSave())
					System.exit(0);
			}
		};
		f.addWindowListener(frameClose);
//////////////////
		/*
		 * ta.append("Hello dear hello hi"); ta.append("\nwho are u dear mister hello");
		 * ta.append("\nhello bye hel"); ta.append("\nHello");
		 * ta.append("\nMiss u mister hello hell"); fileHandler.saved=true;
		 */
	}

////////////////////////////////////
	void goTo() {
		int lineNumber = 0;
		try {
			lineNumber = ta.getLineOfOffset(ta.getCaretPosition()) + 1;
			String tempStr = JOptionPane.showInputDialog(f, "Enter Line Number:", "" + lineNumber);
			if (tempStr == null) {
				return;
			}
			lineNumber = Integer.parseInt(tempStr);
			ta.setCaretPosition(ta.getLineStartOffset(lineNumber - 1));
		} catch (Exception e) {
		}
	}
	class myAutoSaver extends TimerTask {
		public void run() {
			fileHandler.saveThisFile();
		}
	}
///////////////////////////////////
	public void actionPerformed(ActionEvent ev) {
		String cmdText = ev.getActionCommand();
////////////////////////////////////
		if (cmdText.equals(fileP))
			fileHandler.newFile1();
		else if (cmdText.equals(fileC))
			fileHandler.newFile2();
		if (cmdText.equals(fileCC))
			fileHandler.newFile3();
		if (cmdText.equals(fileJava))
			fileHandler.newFile4();
		if (cmdText.equals(fileHtml))
			fileHandler.newFile5();
		else if (cmdText.equals(fileOpen))
			fileHandler.openFile();
////////////////////////////////////
		else if (cmdText.equals(fileSave))
			fileHandler.saveThisFile();
////////////////////////////////////
		else if (cmdText.equals(fileSaveAs))
			fileHandler.saveAsFile();
////////////////////////////////////
		else if (cmdText.equals(fileAutoSave)) {
			JCheckBoxMenuItem temp = (JCheckBoxMenuItem) ev.getSource();
			boolean checker=temp.isSelected();
			if (checker)
			{
				timer=new Timer();
				timer.schedule(new myAutoSaver(), 0 , 600000);
			}
			else 
			{
				timer.cancel();
				timer.purge();
			}
		}
///////////////////////////////////
		else if (cmdText.equals(fileExit)) {
			if (fileHandler.confirmSave())
				System.exit(0);
		}
////////////////////////////////////
		else if (cmdText.equals(filePrint))
			JOptionPane.showMessageDialog(Notepad.this.f, "Get ur printer repaired first! It seems u dont have one!",
					"Bad Printer", JOptionPane.INFORMATION_MESSAGE);
////////////////////////////////////
		else if (cmdText.equals(editCut))
			ta.cut();
////////////////////////////////////
		else if (cmdText.equals(editCopy))
			ta.copy();
////////////////////////////////////
		else if (cmdText.equals(editPaste))
			ta.paste();
////////////////////////////////////
		else if (cmdText.equals(editDelete))
			ta.replaceSelection("");
////////////////////////////////////
		else if (cmdText.equals(editFind)) {
			if (Notepad.this.ta.getText().length() == 0)
				return; // text box have no text
			if (findReplaceDialog == null)
				findReplaceDialog = new FindDialog(Notepad.this.ta);
			findReplaceDialog.showDialog(Notepad.this.f, true);// find
		}
////////////////////////////////////
		else if (cmdText.equals(editFindNext)) {
			if (Notepad.this.ta.getText().length() == 0)
				return; // text box have no text

			if (findReplaceDialog == null)
				statusBar.setText("Nothing to search for, use Find option of Edit Menu first !!!!");
			else
				findReplaceDialog.findNextWithSelection();
		}
////////////////////////////////////
		else if (cmdText.equals(editReplace)) {
			if (Notepad.this.ta.getText().length() == 0)
				return; // text box have no text

			if (findReplaceDialog == null)
				findReplaceDialog = new FindDialog(Notepad.this.ta);
			findReplaceDialog.showDialog(Notepad.this.f, false);// replace
		}
////////////////////////////////////
		else if (cmdText.equals(editGoTo)) {
			if (Notepad.this.ta.getText().length() == 0)
				return; // text box have no text
			goTo();
		}
////////////////////////////////////
		else if (cmdText.equals(editSelectAll))
			ta.selectAll();
////////////////////////////////////
		else if (cmdText.equals(defTime))
			ta.insert(new Date().toString(), ta.getSelectionStart());
		else if (cmdText.equals(dayMonth)) {
			
			Date date = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			
			ta.insert(formatter.format(date), ta.getSelectionStart());
		}else if (cmdText.equals(monthDay)) {
			
			Date date = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
			
			ta.insert(formatter.format(date), ta.getSelectionStart());
////////////////////////////////////
		}else if (cmdText.equals(formatWordWrap)) {
			JCheckBoxMenuItem temp = (JCheckBoxMenuItem) ev.getSource();
			ta.setLineWrap(temp.isSelected());
		}
////////////////////////////////////
		else if (cmdText.equals(formatFont)) {
			if (fontDialog == null)
				fontDialog = new FontChooser(ta.getFont());

			if (fontDialog.showDialog(Notepad.this.f, "Choose a font"))
				Notepad.this.ta.setFont(fontDialog.createFont());
		}
////////////////////////////////////
		else if (cmdText.equals(formatForeground))
			showForegroundColorDialog();
////////////////////////////////////
		else if (cmdText.equals(formatBackground))
			showBackgroundColorDialog();
////////////////////////////////////

		else if (cmdText.equals(viewStatusBar)) {
			JCheckBoxMenuItem temp = (JCheckBoxMenuItem) ev.getSource();
			statusBar.setVisible(temp.isSelected());
		}
////////////////////////////////////
		else if (cmdText.equals(helpAboutNotepad)) {
			JOptionPane.showMessageDialog(Notepad.this.f, aboutText, "Dedicated 2 u!", JOptionPane.INFORMATION_MESSAGE);
		}
		else if (cmdText.equals(editUndo)) {
			try {
		          undoManager.undo();
		        } catch (Exception e) {
		          System.out.println("Undo limit reached, nothing will happen.");
		        }
		}
		else if (cmdText.equals(editRedo)) {
			try {
		          undoManager.redo();
		        } catch (Exception e) {
		          System.out.println("Redo limit reached, nothing will happen.");
		        }
		}
			else
			statusBar.setText("This " + cmdText + " command is yet to be implemented");
	}// action Performed
////////////////////////////////////

	void showBackgroundColorDialog() {
		if (bcolorChooser == null)
			bcolorChooser = new JColorChooser();
		if (backgroundDialog == null)
			backgroundDialog = JColorChooser.createDialog(Notepad.this.f, formatBackground, false, bcolorChooser,
					new ActionListener() {
						public void actionPerformed(ActionEvent evvv) {
							Notepad.this.ta.setBackground(bcolorChooser.getColor());
						}
					}, null);

		backgroundDialog.setVisible(true);
	}

////////////////////////////////////
	void showForegroundColorDialog() {
		if (fcolorChooser == null)
			fcolorChooser = new JColorChooser();
		if (foregroundDialog == null)
			foregroundDialog = JColorChooser.createDialog(Notepad.this.f, formatForeground, false, fcolorChooser,
					new ActionListener() {
						public void actionPerformed(ActionEvent evvv) {
							Notepad.this.ta.setForeground(fcolorChooser.getColor());
						}
					}, null);

		foregroundDialog.setVisible(true);
	}

///////////////////////////////////
	JMenuItem createMenuItem(String s, int key, JMenu toMenu, ActionListener al) {
		JMenuItem temp = new JMenuItem(s, key);
		temp.addActionListener(al);
		toMenu.add(temp);

		return temp;
	}

////////////////////////////////////
	JMenuItem createMenuItem(String s, int key, JMenu toMenu, int aclKey, ActionListener al) {
		JMenuItem temp = new JMenuItem(s, key);
		temp.addActionListener(al);
		temp.setAccelerator(KeyStroke.getKeyStroke(aclKey, ActionEvent.CTRL_MASK));
		toMenu.add(temp);

		return temp;
	}

////////////////////////////////////
	JCheckBoxMenuItem createCheckBoxMenuItem(String s, int key, JMenu toMenu, ActionListener al) {
		JCheckBoxMenuItem temp = new JCheckBoxMenuItem(s);
		temp.setMnemonic(key);
		temp.addActionListener(al);
		temp.setSelected(false);
		toMenu.add(temp);

		return temp;
	}

////////////////////////////////////
	JMenu createMenu(String s, int key, JMenuBar toMenuBar) {
		JMenu temp = new JMenu(s);
		temp.setMnemonic(key);
		toMenuBar.add(temp);
		return temp;
	}
	
	JMenu createSubMenu(String s , int key , JMenu parentMenu) {
		JMenu temp = new JMenu(s);
		temp.setMnemonic(key);
		parentMenu.add(temp);
		return temp;
	}

	/*********************************/
	void createMenuBar(JFrame f) {
		JMenuBar mb = new JMenuBar();
		JMenuItem temp;

		JMenu fileMenu = createMenu(fileText, KeyEvent.VK_F, mb);
		JMenu editMenu = createMenu(editText, KeyEvent.VK_E, mb);
		JMenu formatMenu = createMenu(formatText, KeyEvent.VK_O, mb);
		JMenu viewMenu = createMenu(viewText, KeyEvent.VK_V, mb);
		JMenu helpMenu = createMenu(helpText, KeyEvent.VK_H, mb);
		
		////////////////////////////////
		
		JMenu newSubMenu = createSubMenu(fileNew, KeyEvent.VK_F, fileMenu);
		
		createMenuItem(fileP , KeyEvent.VK_N, newSubMenu, KeyEvent.VK_N, this);
		createMenuItem(fileC, KeyEvent.VK_Q, newSubMenu, KeyEvent.VK_Q, this);
		createMenuItem(fileCC, KeyEvent.VK_W, newSubMenu, KeyEvent.VK_W, this);
		createMenuItem(fileJava, KeyEvent.VK_E, newSubMenu, KeyEvent.VK_E, this);
		createMenuItem(fileHtml, KeyEvent.VK_R, newSubMenu, KeyEvent.VK_R, this);
		
		////////////////////////////////////////
		
		createMenuItem(fileOpen, KeyEvent.VK_O, fileMenu, KeyEvent.VK_O, this);
		createMenuItem(fileSave, KeyEvent.VK_S, fileMenu, KeyEvent.VK_S, this);
		createMenuItem(fileSaveAs, KeyEvent.VK_A, fileMenu, this);
		createCheckBoxMenuItem(fileAutoSave,KeyEvent.VK_V, fileMenu, this);
		fileMenu.addSeparator();
		temp = createMenuItem(filePageSetup, KeyEvent.VK_U, fileMenu, this);
		temp.setEnabled(false);
		createMenuItem(filePrint, KeyEvent.VK_P, fileMenu, KeyEvent.VK_P, this);
		fileMenu.addSeparator();
		createMenuItem(fileExit, KeyEvent.VK_X, fileMenu, this);

		temp = createMenuItem(editUndo, KeyEvent.VK_U, editMenu, KeyEvent.VK_Z, this);
		temp.setEnabled(true);
		temp = createMenuItem(editRedo, KeyEvent.VK_U, editMenu, KeyEvent.VK_Y, this);
		temp.setEnabled(true);
		editMenu.addSeparator();
		cutItem = createMenuItem(editCut, KeyEvent.VK_T, editMenu, KeyEvent.VK_X, this);
		copyItem = createMenuItem(editCopy, KeyEvent.VK_C, editMenu, KeyEvent.VK_C, this);
		createMenuItem(editPaste, KeyEvent.VK_P, editMenu, KeyEvent.VK_V, this);
		deleteItem = createMenuItem(editDelete, KeyEvent.VK_L, editMenu, this);
		deleteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		editMenu.addSeparator();
		findItem = createMenuItem(editFind, KeyEvent.VK_F, editMenu, KeyEvent.VK_F, this);
		findNextItem = createMenuItem(editFindNext, KeyEvent.VK_N, editMenu, this);
		findNextItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		replaceItem = createMenuItem(editReplace, KeyEvent.VK_R, editMenu, KeyEvent.VK_H, this);
		gotoItem = createMenuItem(editGoTo, KeyEvent.VK_G, editMenu, KeyEvent.VK_G, this);
		editMenu.addSeparator();
		selectAllItem = createMenuItem(editSelectAll, KeyEvent.VK_A, editMenu, KeyEvent.VK_A, this);
		
		////////////////////////////////////////////////////////////
		
		JMenu timeSubMenu = createSubMenu(editTimeDate, KeyEvent.VK_F, editMenu);
		
		createMenuItem(defTime , KeyEvent.VK_N, timeSubMenu, this);
		createMenuItem(dayMonth , KeyEvent.VK_N, timeSubMenu, this);
		createMenuItem(monthDay , KeyEvent.VK_N, timeSubMenu, this);
		
		///////////////////////////////////////////////////////////////

		createCheckBoxMenuItem(formatWordWrap, KeyEvent.VK_W, formatMenu, this);

		createMenuItem(formatFont, KeyEvent.VK_F, formatMenu, this);
		formatMenu.addSeparator();
		createMenuItem(formatForeground, KeyEvent.VK_T, formatMenu, this);
		createMenuItem(formatBackground, KeyEvent.VK_P, formatMenu, this);
	

		createCheckBoxMenuItem(viewStatusBar, KeyEvent.VK_S, viewMenu, this).setSelected(true);
		/************
		 * For Look and Feel, May not work properly on different operating environment
		 ***/
		LookAndFeelMenu.createLookAndFeelMenuItem(viewMenu, this.f);

		temp = createMenuItem(helpHelpTopic, KeyEvent.VK_H, helpMenu, this);
		temp.setEnabled(false);
		helpMenu.addSeparator();
		createMenuItem(helpAboutNotepad, KeyEvent.VK_A, helpMenu, this);

		MenuListener editMenuListener = new MenuListener() {
			public void menuSelected(MenuEvent evvvv) {
				if (Notepad.this.ta.getText().length() == 0) {
					findItem.setEnabled(false);
					findNextItem.setEnabled(false);
					replaceItem.setEnabled(false);
					selectAllItem.setEnabled(false);
					gotoItem.setEnabled(false);
				} else {
					findItem.setEnabled(true);
					findNextItem.setEnabled(true);
					replaceItem.setEnabled(true);
					selectAllItem.setEnabled(true);
					gotoItem.setEnabled(true);
				}
				if (Notepad.this.ta.getSelectionStart() == ta.getSelectionEnd()) {
					cutItem.setEnabled(false);
					copyItem.setEnabled(false);
					deleteItem.setEnabled(false);
				} else {
					cutItem.setEnabled(true);
					copyItem.setEnabled(true);
					deleteItem.setEnabled(true);
				}
			}

			public void menuDeselected(MenuEvent evvvv) {
			}

			public void menuCanceled(MenuEvent evvvv) {
			}
		};
		editMenu.addMenuListener(editMenuListener);
		f.setJMenuBar(mb);
	}

	/************* Constructor **************/
////////////////////////////////////
	public static void main(String[] s) {
		new Notepad();
	}
}

/**************************************/
//public
interface MenuConstants {
	final String fileText = "File";
	final String editText = "Edit";
	final String formatText = "Format";
	final String viewText = "View";
	final String helpText = "Help";

	final String fileNew = "New";
	
	///////////////////////////////////
	
	final String fileP = "Pad";
	final String fileC = "C";
	final String fileCC = "C++";
	final String fileJava = "JAVA";
	final String fileHtml = "HTML,JS,CSS";
	
	//////////////////////////////////
	
	final String fileOpen = "Open...";
	final String fileSave = "Save";
	final String fileSaveAs = "Save As...";
	final String fileAutoSave = "Autosave";
	final String filePageSetup = "Page Setup...";
	final String filePrint = "Print";
	final String fileExit = "Exit";

	final String editUndo = "Undo";
	final String editRedo = "Redo";
	final String editCut = "Cut";
	final String editCopy = "Copy";
	final String editPaste = "Paste";
	final String editDelete = "Delete";
	final String editFind = "Find...";
	final String editFindNext = "Find Next";
	final String editReplace = "Replace";
	final String editGoTo = "Go To...";
	final String editSelectAll = "Select All";
	final String editTimeDate = "Time/Date";
	
	/////////////////////////////////
	
	final String defTime = "Default";
	final String dayMonth = "DD/MM/YYY";
	final String monthDay = "MM/DD/YYY";
	
	/////////////////////////////////

	final String formatWordWrap = "Word Wrap";
	final String formatFont = "Font...";
	final String formatForeground = "Set Text color...";
	final String formatBackground = "Set Pad color...";

	final String viewStatusBar = "Status Bar";

	final String helpHelpTopic = "Help Topic";
	final String helpAboutNotepad = "About Javapad";

	final String aboutText = "<html><big>Your Javapad</big><hr><hr>" + "<p align=right>Prepared by a Ducatian!"
			+ "<hr><p align=left>I Used jdk1.5 to compile the source code.<br><br>"
			+ "<strong>Thanx 4 using Javapad</strong><br>"
			+ "Ur Comments as well as bug reports r very welcome at<p align=center>"
			+ "<hr><em><big>radialgoal@gmail.com</big></em><hr><html>";
}