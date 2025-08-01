package de.swa.ui.command;

import de.swa.gc.GraphCode;
import de.swa.nlq_gc.GraphCodeDTO;
import de.swa.nlq_gc.NlqGcService;
import de.swa.ui.MMFGCollection;
import de.swa.ui.panels.LogPanel;

import java.util.Vector;
import java.util.concurrent.ExecutionException;

public class QueryByNaturalLanguageCommand extends AbstractCommand {

    private String query;
    private String session_id;

    public QueryByNaturalLanguageCommand(String q) {
        query = q;
    }

    public void setSessionId(String session_id) {
        this.session_id = session_id;
    }

    @Override
    public void execute() {
        GraphCodeDTO dto;
        try {
            dto = new NlqGcService(query).startChecking().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        }

        GraphCode gcQuery = new GraphCode();

        gcQuery.setDictionary(new Vector<>(dto.getDictionary()));
        for (int i = 0; i < dto.getDictionary().size(); i++) {
            for (int j = 0; j < dto.getDictionary().size(); j++) {
                gcQuery.setValue(i, j, dto.getMatrix()[i][j]);
            }
        }

        if (session_id != null) MMFGCollection.getInstance(session_id).query(gcQuery);
        else MMFGCollection.getInstance().query(gcQuery);

        LogPanel.getCurrentInstance().addToLog("returning ranked result list.");
    }

}