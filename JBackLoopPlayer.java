import java.awt.event.ActionListener;
import java.awt.TrayIcon;
import java.awt.PopupMenu;
import java.awt.MenuItem;
import java.awt.FileDialog;
import java.io.File;
import javax.sound.sampled.Clip;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Dialog;
import java.awt.SystemTray;
import java.awt.AWTException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;

public class JBackLoopPlayer implements ActionListener{
	
	TrayIcon trayicon;
	PopupMenu popupmenu;
	MenuItem reading_menuitem;
	MenuItem open_menuitem;
	MenuItem play_menuitem;
	MenuItem stop_menuitem;
	MenuItem exit_menuitem;
	FileDialog filedialog;
	File readingfile;
	Clip readingclip;
	Image icon;
	static String appname = "���������郋�[�v ver0.1";
	
	public JBackLoopPlayer(File file){
		icon = getIcon();
		initTrayicon();
	}
	
	private Image getIcon(){
		Image image;
		image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icon.png"));
		return image;
	}
	
	private void initTrayicon(){
		trayicon = new TrayIcon(icon);
		
		popupmenu = new PopupMenu();
		
		reading_menuitem = new MenuItem();
		reading_menuitem.setEnabled(false);
		popupmenu.add(reading_menuitem);
		
		popupmenu.addSeparator();
		
		open_menuitem = new MenuItem("�J��");
		open_menuitem.setActionCommand("open");
		open_menuitem.addActionListener(this);
		popupmenu.add(open_menuitem);
		
		play_menuitem = new MenuItem("�Đ�");
		play_menuitem.setActionCommand("play");
		play_menuitem.addActionListener(this);
		popupmenu.add(play_menuitem);
		
		stop_menuitem = new MenuItem("��~");
		stop_menuitem.setActionCommand("stop");
		stop_menuitem.addActionListener(this);
		popupmenu.add(stop_menuitem);
		
		popupmenu.addSeparator();

		exit_menuitem = new MenuItem("�I��");
		exit_menuitem.setActionCommand("exit");
		exit_menuitem.addActionListener(this);
		popupmenu.add(exit_menuitem);
		
		trayicon.setPopupMenu(popupmenu);
		
		filedialog = new FileDialog((Dialog)null, "�J��", FileDialog.LOAD);
		filedialog.setModal(true);
		updateLabel();
		
		try{ SystemTray.getSystemTray().add(trayicon); }catch(AWTException e){}
	}
	
	private void updateLabel(){
		if(readingfile != null){
			String state;
			if(isPlaying()){
				state = "�Đ���";
				play_menuitem.setEnabled(false);
				stop_menuitem.setEnabled(true);
			}
			else{
				state = "��~��";
				play_menuitem.setEnabled(true);
				stop_menuitem.setEnabled(false);
			}
			trayicon.setToolTip(state + " - " + appname);
			reading_menuitem.setLabel(state + ": " + readingfile.getName());
		}
		else{
			String state = "�t�@�C�����J���Ă�������";
			trayicon.setToolTip(state + " - " + appname);
			reading_menuitem.setLabel(state);
			play_menuitem.setEnabled(false);
			stop_menuitem.setEnabled(false);
		}
	}
	
	private boolean isPlaying(){
		return readingclip != null && readingclip.isRunning();
	}
	
	private void onOpen(){
		if(filedialog.isVisible()){
			filedialog.toFront();
			return;
		}
		filedialog.setVisible(true);
		if(filedialog.getFile() == null){
			return;
		}
		try{
			if(isPlaying()){
				readingclip.stop();
				readingclip.close();
			}
			readingfile = new File(filedialog.getDirectory(), filedialog.getFile());
			AudioInputStream stream = AudioSystem.getAudioInputStream(readingfile);
			readingclip = (Clip)AudioSystem.getLine(new DataLine.Info(Clip.class, stream.getFormat()));
			readingclip.open(stream);
			onPlay();//play�{�^�������������Ƃɂ���
			while(!readingclip.isRunning()){//isPlaying��true��Ԃ���悤�ɂȂ�܂ő҂�
				Thread.sleep(1);
			}
		}
		catch(Exception e){
			readingfile = null;
			readingclip = null;
			showMessageDialog("�J���Ȃ�����", e);
		}
	}
	
	private void onPlay(){
		readingclip.loop(Clip.LOOP_CONTINUOUSLY);
	}
	
	private void onStop(){
		readingclip.stop();
		readingclip.setFramePosition(0);
	}
	
	private void onExit(){
		if(readingclip != null){
			readingclip.stop();
			readingclip.close();
		}
		System.exit(0);
	}
	
	public void actionPerformed(ActionEvent evt){
		String command = evt.getActionCommand();
		
		if(command.equals("open"))
			onOpen();
		if(command.equals("play"))
			onPlay();
		if(command.equals("stop"))
			onStop();
		if(command.equals("exit"))
			onExit();
		updateLabel();
	}
	
	private static void showMessageDialog(String message, Exception error){
		JOptionPane.showMessageDialog(null, message + "\n" + error, appname, JOptionPane.ERROR_MESSAGE);
		error.printStackTrace();
	}
	
	public static void main(String[] args){
		if(!SystemTray.isSupported()){
			showMessageDialog("�V�X�e���g���C�����݂��Ȃ��悤�ł�\n���̃\�t�g�̓V�X�e���g���C���K�v�ł�", new AWTException("SystemTray is unsupported."));
			System.exit(0);
		}
		new JBackLoopPlayer(args.length != 0 ? new File(args[0]) : null);
	}
}
