package de.swa.ui.command;

import java.awt.Desktop;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JOptionPane;

import de.swa.mmfg.MMFG;
import de.swa.mmfg.builder.Flattener;
import de.swa.ui.MMFGCollection;
import de.swa.ui.Configuration;
import de.swa.ui.panels.LogPanel;

/** class to encapsulate the Export Command **/

public class ExportCommand extends AbstractCommand {
	private String type;
	
	// Map of export types to their corresponding class names
	private static final Map<String, String> EXPORT_CLASSES = new HashMap<>();
	
	static {
		// Initialize the map with export types and their corresponding class names
		EXPORT_CLASSES.put("EXP:GraphML", "de.swa.mmfg.builder.GraphMLFlattener");
		EXPORT_CLASSES.put("EXP:HTML", "de.swa.mmfg.builder.HTMLFlattener");
		EXPORT_CLASSES.put("EXP:Image", "de.swa.mmfg.builder.ImageFlattener");
		EXPORT_CLASSES.put("EXP:Json", "de.swa.mmfg.builder.JsonFlattener");
		EXPORT_CLASSES.put("EXP:Neo4J", "de.swa.mmfg.builder.Neo4JFlattener");
		EXPORT_CLASSES.put("EXP:XML", "de.swa.mmfg.builder.XMLEncodeDecode");
		EXPORT_CLASSES.put("EXP:GraphCode", "de.swa.gc.GraphCodeIO");
		EXPORT_CLASSES.put("EXP:Detection-XML", "de.swa.mmfg.builder.DetectionExporter");
		EXPORT_CLASSES.put("EXP:MPEG7", "de.swa.mmfg.builder.Mpeg7IO");
		EXPORT_CLASSES.put("EXP:RDF", "de.swa.mmfg.builder.RDFFlattener");
	}
	
	public ExportCommand(String export) {
		type = export;
	}
	
	/**
	 * Dynamically loads a Flattener implementation using reflection
	 * @param className The fully qualified class name of the Flattener implementation
	 * @return A Flattener instance or null if the class couldn't be loaded
	 */
	private Flattener loadFlattener(String className) {
		try {
			Class<?> clazz = Class.forName(className);
			return (Flattener) clazz.getDeclaredConstructor().newInstance();
		} catch (ClassNotFoundException e) {
			LogPanel.getCurrentInstance().addToLog("Export type " + type + " is not available: " + className + " not found");
		} catch (Exception e) {
			LogPanel.getCurrentInstance().addToLog("Error loading exporter " + className + ": " + e.getMessage());
		}
		return null;
	}
	
	public void execute() {
		if (!EXPORT_CLASSES.containsKey(type)) {
			LogPanel.getCurrentInstance().addToLog("Unknown export type: " + type);
			return;
		}
		
		// Try to load the flattener dynamically
		Flattener f = loadFlattener(EXPORT_CLASSES.get(type));
		
		// If the flattener couldn't be loaded, inform the user and return
		if (f == null) {
			JOptionPane.showMessageDialog(null, 
					"The export type '" + type + "' is not available.\n" +
					"The required dependency might be missing.");
			return;
		}

		String header = f.startFile();
		String footer = f.endFile();
		boolean singleFiles = false;
		if (header.equals("") && footer.equals("")) singleFiles = true;

		Vector<MMFG> toProcess = Configuration.getInstance().getSelectedItems();
		if (!singleFiles) toProcess = MMFGCollection.getInstance().getCollection();
		if (toProcess.size() == 0) {
			JOptionPane.showMessageDialog(null, "No asset selected.");
			return;
		}

		RandomAccessFile rf = null;
		try {
			if (!singleFiles) {
				rf = new RandomAccessFile(Configuration.getInstance().getExportFolder() + File.separatorChar + "export." + f.getFileExtension(), "rw");
				rf.setLength(0);
				rf.writeBytes(header);
			}
			for (MMFG m : toProcess) {
				String s = f.flatten(m);
				System.out.println(s);
				try {
					File fc = new File(Configuration.getInstance().getExportFolder() + File.separatorChar + m.getGeneralMetadata().getFileName() + "." + f.getFileExtension());
					if (singleFiles) {
						rf = new RandomAccessFile(fc, "rw");
						rf.setLength(0);
						rf.writeBytes(s);
					}
					LogPanel.getCurrentInstance().addToLog("exported " + m.getGeneralMetadata().getFileName() + " to " + f.getFileExtension());
					if (singleFiles) Desktop.getDesktop().open(fc);
				}
				catch (Exception x) {
					x.printStackTrace();
				}
			}
			if (!singleFiles) {
				rf.writeBytes(footer);
				LogPanel.getCurrentInstance().addToLog("exported complete file to to " + Configuration.getInstance().getExportFolder() + File.separatorChar + "export." + f.getFileExtension());
				Desktop.getDesktop().open(new File(Configuration.getInstance().getExportFolder() + File.separatorChar + "export." + f.getFileExtension()));
			}
		}
		catch (Exception x) {
			x.printStackTrace();
		} finally {
			if (rf != null) {
				try {
					rf.close();
				} catch (Exception e) {
					// Ignore
				}
			}
		}
	}
}
