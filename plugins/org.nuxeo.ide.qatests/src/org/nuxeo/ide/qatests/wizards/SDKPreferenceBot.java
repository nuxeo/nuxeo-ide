package org.nuxeo.ide.qatests.wizards;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.nuxeo.ide.qatests.Bot;
import org.nuxeo.ide.sdk.SDKInfo;
import org.nuxeo.ide.sdk.server.ui.SDKTableWidget;

public class SDKPreferenceBot implements Bot {

    protected final SWTWorkbenchBot workbench;

    protected final SWTBotTable sdks;

    public SDKPreferenceBot(SWTWorkbenchBot workbench) {
        this.workbench = workbench;
        this.sdks = workbench.table();
    }

    public void addSDK(final File home) throws IOException {
        final SDKInfo info = SDKInfo.loadSDK(home);
        UIThreadRunnable.asyncExec(SWTUtils.display(), new VoidResult() {
            public void run() {
                SDKTableWidget w = (SDKTableWidget) sdks.widget.getData("org.eclipse.swtbot.widget.owner");
                CheckboxTableViewer v = w.getViewer();
                v.add(info);
            }
        });

    }

    public boolean selectSDK(File home) throws IOException {
        String path = home.getPath();
        int count = sdks.rowCount();
        for (int row = 0; row < count; ++row) {
            SWTBotTableItem item = sdks.getTableItem(row);
            if (path.equals(item.getText(2))) {
                item.check();
                return true;
            }
        }
        return false;
    }

    public void addAndSelect(File home) throws IOException {
        if (selectSDK(home) == false) {
            addSDK(home);
        }
        selectSDK(home);
    }

}
