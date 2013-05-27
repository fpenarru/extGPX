package org.gvsig.gpx;

import java.awt.Component;
import java.io.File;

import org.gvsig.gpx.driver.GPXVectorialDriver;
import org.gvsig.gui.beans.swing.JFileChooser;

import com.hardcode.driverManager.DriverLoadException;
import com.hardcode.driverManager.DriverManager;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.andami.plugins.Extension;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.drivers.VectorialFileDriver;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;
import com.iver.cit.gvsig.project.documents.view.gui.IView;
import com.iver.cit.gvsig.project.documents.view.gui.View;

public class GPXExtension extends Extension{
	
	private String driversDir = "gvSIG" + File.separatorChar + "extensiones" + File.separatorChar +
	"org.gvsig.gpx" + File.separatorChar + "lib";

	public void initialize() {
		DriverManager driverManager = LayerFactory.getDM();
		driverManager.addDriver(new File(driversDir), GPXVectorialDriver.DRIVERNAME, GPXVectorialDriver.class);

	}

	public void execute(String actionCommand) {
		DriverManager driverManager = LayerFactory.getDM();
		try {
			IView v = (IView) PluginServices.getMDIManager().getActiveWindow();
			MapContext mc = v.getMapControl().getMapContext();
			GPXVectorialDriver driver = (GPXVectorialDriver) driverManager.getDriver(GPXVectorialDriver.DRIVERNAME);
			driver.setbLoadAllAsWaypoints(true);
			JFileChooser fileChooser = new JFileChooser("GPX", (File) null);
			fileChooser.setMultiSelectionEnabled(true);
			int res = fileChooser.showOpenDialog((Component) PluginServices.getMDIManager().getActiveWindow());
			if (res == JFileChooser.APPROVE_OPTION) {
				
				File[] gpxFiles = fileChooser.getSelectedFiles();
				mc.beginAtomicEvent();
				for (File f : gpxFiles) {
					FLayer lyr = LayerFactory.createLayer(f.getName(), driver, f, v.getMapControl().getProjection());
					mc.getLayers().addLayer(lyr);
				}
				mc.endAtomicEvent();
			}
			
		} catch (DriverLoadException e) {
			e.printStackTrace();
			NotificationManager.addError(e);
		}

	}

	private void addNewLayer(File selectedFile) {
		// TODO Auto-generated method stub
		
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isVisible() {
		IWindow w = PluginServices.getMDIManager().getActiveWindow();
		if (w == null) {
			return false;
		}

		if (w instanceof View) {
			return true;
		}
		return false;
	}

}
