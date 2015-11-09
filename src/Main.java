

import controller.Controller;
import gnu.io.SerialPort;
import view.MainView;
import view.PortSelectorGui;

public class Main {

	public static void main(String[] args) {
		PortSelectorGui psg = new PortSelectorGui();
		SerialPort port = psg.getConnectedPort(); 
		Controller controller = new Controller();
		MainView mainView = new MainView();
//		ElectronicSpeedController esc = new ElectronicSpeedController(port);
//		controller.setView(mainView);
//		controller.setEsc(esc);
//		mainView.setController(controller);
		
//		mainView.show();
	}

}
