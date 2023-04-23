package de.swa.ui.panels;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.swa.fuh.explanation.InfoButton;
import de.swa.gc.GraphCode;
import de.swa.gc.GraphCodeCollection;
import de.swa.gc.GraphCodeGenerator;
import de.swa.gc.SemanticGraphCode;
import de.swa.gmaf.GMAF;
import de.swa.mmfg.MMFG;
import de.swa.ui.*;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;

/**
 * panel that shows asset details
 **/
public class AssetDetailPanel extends JPanel {
	private static AssetDetailPanel instance;
	private static MediaPlayer player;
	private boolean inMemoryOnly = false;
	private JFXPanel VFXPanel = new JFXPanel();
	private File f;
	private GraphCode gc;


	public AssetDetailPanel(boolean inMemory) {
		inMemoryOnly = inMemory;
		instance = this;
		initJFX();
		refresh();
	}

	public AssetDetailPanel() {
		instance = this;
		initJFX();
		refresh();
	}

	public static AssetDetailPanel getCurrentInstance() {
		return instance;
	}

	private static void initFX(JFXPanel fxPanel) {
		// This method is invoked on JavaFX thread
		//Scene scene = createScene();


		StackPane root = new StackPane();
		Scene scene = new Scene(root);
//
		VBox box = new VBox(new Label("A JFXLabel"));

		root.getChildren().add(box);
		fxPanel.setScene(scene);
	}

	private void initJFX() {
		Platform.setImplicitExit(false);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				initFX(VFXPanel);
			}
		});

	}

	public void refresh() {
		setVisible(false);

		removeAll();
		setLayout(new GridLayout(2, 1));
		add(getAssetPanel());
		add(getGraphCodePanel());
		setVisible(true);
	}

	private JPanel getAssetPanel() {
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(1, 1));
		f = Configuration.getInstance().getSelectedAsset();

		String name = "...";
		if (f != null) {
			String extension = f.getName();
			extension = extension.substring(extension.lastIndexOf(".") + 1, extension.length());
			extension = extension.toLowerCase();
			name = f.getName();

			if (extension.equals("png") || extension.equals("gif") || extension.equals("jpg")
					|| extension.equals("jpeg") || extension.equals("bmp") || extension.equals("tiff")) {
				ImageIcon ii = new ImageIcon(f.getAbsolutePath());

				Image i = Tools.getScaledInstance(f.getName(), ii.getImage(), 500,
						Configuration.getInstance().showBoundingBox());
				JLabel l = new JLabel(new ImageIcon(i));
				p.add(l);
			} else if (extension.equals("mp4")) {


				try {

					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							updatePlayer(VFXPanel, f);
						}
					});


					p.add(VFXPanel);


				} catch (Exception e) {
					e.printStackTrace();
					JLabel l = new JLabel("Error on video display");
					p.add(l);

				}
			} else {
				try {
					RandomAccessFile rf = new RandomAccessFile(f, "r");
					String line = "";
					String content = "";
					while ((line = rf.readLine()) != null) {
						content += line + "\n";
					}

					String prettyFormat = content;
					try {
						JsonParser parser = new JsonParser();
						JsonObject json = parser.parse(content).getAsJsonObject();

						Gson gson = new GsonBuilder().setPrettyPrinting().create();
						String prettyJson = gson.toJson(json);
						prettyFormat = prettyJson;
					} catch (Exception ex) {
						System.out.println("Tried to parse file " + f.getName() + ": " + ex.getMessage());
					}

					JEditorPane ep = new JEditorPane();
					ep.setText(prettyFormat);
					p.add(new JScrollPane(ep));
					rf.close();
				} catch (Exception x) {
					x.printStackTrace();
				}
			}
		}
		JPanel q = new JPanel();
		q.setLayout(new BorderLayout());
		q.add(p, "Center");
		JLabel l = new JLabel(name);
		q.add(l, "North");
		return q;
	}

	private void updatePlayer(JFXPanel fxPanel, File f) {

		String filename = f.toURI().toString();

		javafx.scene.media.Media m = new javafx.scene.media.Media(filename);


		player = new MediaPlayer(m);


		MediaControl mediaControl = new MediaControl(player);

		mediaControl.mediaView.setFitWidth(fxPanel.getWidth());
		mediaControl.mediaView.setFitHeight(fxPanel.getHeight());

		Scene scene = new Scene(mediaControl);

		fxPanel.setScene(scene);
	}

	public GraphCode getGraphCode() {
		return gc;
	}

	

	private JPanel getGraphCodePanel() {
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(1, 1));

		MMFG mmfg = null;
		if (inMemoryOnly) {
			try {
				FileInputStream fs = new FileInputStream(f);
				byte[] bytes = fs.readAllBytes();
				mmfg = new GMAF().processAsset(bytes, f.getName(), "sw", 5, 500, f.getName(), f);
				this.gc = GraphCodeGenerator.generate(mmfg);
			} catch (Exception x) {
				x.printStackTrace();
			}
		} else {
			mmfg = MMFGCollection.getInstance().getMMFGForFile(f);
			this.gc = MMFGCollection.getInstance().getOrGenerateGraphCode(mmfg);
		}

		JTabbedPane tp = new JTabbedPane();
		tp.addTab("Similar", new SimilarAssetPanel());
		tp.addTab("Recommendation", new RecommendedAssetPanel());
		tp.addTab("Explanation", new ExplainPanel(mmfg));

		JTabbedPane gctp = new JTabbedPane();
		tp.addTab("Graph Code", gctp);
		if (gc.isCollection()) {
			GraphCode union = GraphCodeCollection.getCollectionGraphCode(gc);
			GraphCode summary = GraphCodeCollection.getSummaryGraphCode(gc, 5);
			gctp.addTab("Union GC", getGraphCodePanel(union));
			gctp.addTab("Summary GC", getGraphCodePanel(summary));
			gctp.addTab("Semantic GC", getGraphCodePanel(new SemanticGraphCode(summary)));
		} else {
			gctp.addTab("GC", getGraphCodePanel(gc));
			gctp.addTab("SGC", getGraphCodePanel(new SemanticGraphCode(gc)));
		}
		gctp.addTab("FRGC", getGraphCodePanel(GraphCodeCollection.getFeatureRelevantGraphCode(gc)));
		gctp.addTab("ESGC", new ExplainPanel(gc));
		try {
			TagCloudPanel tcp = new TagCloudPanel(gc, 500, 500);
			gctp.addTab("Tags", tcp);

			File tn = new File(
					Configuration.getInstance().getThumbnailPath() + File.separatorChar + f.getName() + "_120.png");
			System.out.println("tn: " + tn.getAbsolutePath());
			if (!tn.exists()) {
				BufferedImage bi = tcp.getPreviewImage();
				try {
					ImageIO.write(bi, "png", tn);
					System.out.println("Thumbnail for Text written");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} catch (Exception x) {
			System.out.println("Error on creating Tag Cloud");
			x.printStackTrace();
		}

		InfoButton.getInfoButtonJTabbedPane(tp, f);

		p.add(tp);
		return p;
	}

	private JPanel getGraphCodePanel(GraphCode tempGc) {
		JPanel p = new JPanel();
		JTable graphcode = new JTable();

		try {
			graphcode.setModel(new GraphCodeTableModel(tempGc));
			graphcode.getTableHeader().setDefaultRenderer(new GraphCodeRenderer(true));
			graphcode.setDefaultRenderer(String.class, new GraphCodeRenderer(false));
			graphcode.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			graphcode.getColumnModel().getColumn(0).setWidth(50);
			JTableHeader th = graphcode.getTableHeader();
			th.setPreferredSize(new Dimension(500, 80));

			for (int i = 1; i < graphcode.getColumnModel().getColumnCount(); i++)
				graphcode.getColumnModel().getColumn(i).setMaxWidth(12);
			p.setLayout(new GridLayout(1, 1));
			JScrollPane sp = new JScrollPane(graphcode);
			sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			p.add(sp);
		} catch (Exception x) {
			x.printStackTrace();
		}

		return p;
	}
}
