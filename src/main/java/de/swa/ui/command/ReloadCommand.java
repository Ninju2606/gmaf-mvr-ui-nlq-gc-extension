package de.swa.ui.command;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Vector;

import de.swa.ui.Configuration;
import de.swa.ui.Main;
import de.swa.ui.panels.AssetListPanel;

/** class to encapsulate the Reload Command **/

public class ReloadCommand extends AbstractCommand {
	public void execute() {
		Configuration.getInstance().reload();
		if (AssetListPanel.getCurrentInstance() != null) AssetListPanel.getCurrentInstance().refresh();
	}
}
