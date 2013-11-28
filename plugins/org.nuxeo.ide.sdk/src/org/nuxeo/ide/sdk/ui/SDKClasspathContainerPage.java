package org.nuxeo.ide.sdk.ui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.nuxeo.ide.common.PostModificationTimer;
import org.nuxeo.ide.common.UI;
import org.nuxeo.ide.sdk.NuxeoSDK;
import org.nuxeo.ide.sdk.index.MavenDownloader;
import org.nuxeo.ide.sdk.index.MavenDownloader.FileRef;
import org.nuxeo.ide.sdk.java.SDKClasspathContainer;
import org.nuxeo.ide.sdk.model.Artifact;
import org.nuxeo.ide.sdk.ui.widgets.SDKTableViewerFactory.SDKContentProvider;

public class SDKClasspathContainerPage extends WizardPage implements
        IClasspathContainerPage {

	protected SDKClasspathContainer container;
	
    protected IClasspathEntry[] cp;

    protected Label srcLabel;

    protected Text filter;

    protected TableViewer tv;

    protected Set<IClasspathEntry> modified;

    protected PostModificationTimer timer;

    public SDKClasspathContainerPage() {
        this( NuxeoSDK.getDefault().mainClasspathContainer, "NuxeoSDK", null);
    }

    public SDKClasspathContainerPage(String pageName) {
        super(pageName);
    }

    public SDKClasspathContainerPage(SDKClasspathContainer container, String pageName, 
            ImageDescriptor titleImage) {
        super(pageName, container.desc, titleImage);
        this.container = container;
    }

    @Override
    public void createControl(Composite parent) {
        modified = new HashSet<IClasspathEntry>();
        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new GridLayout());
        filter = new Text(panel, SWT.SINGLE | SWT.BORDER | SWT.SEARCH
                | SWT.ICON_CANCEL);
        GridData gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        filter.setLayoutData(gd);

        tv = new TableViewer(panel, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        gd.grabExcessVerticalSpace = true;
        gd.verticalAlignment = SWT.FILL;
        gd.heightHint = 300;
        tv.getTable().setLayoutData(gd);

        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;

        Composite editPanel = new Composite(panel, SWT.NONE);
        editPanel.setLayoutData(gd);
        editPanel.setLayout(new GridLayout(2, false));
        // editPanel.setText("Edit Sources");
        srcLabel = new Label(editPanel, SWT.NONE);
        srcLabel.setText("");
        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        srcLabel.setLayoutData(gd);
        final Button btnDownload = new Button(editPanel, SWT.NONE);
        btnDownload.setText("Download");
        btnDownload.setEnabled(false);
        // final Button btnBrowse = new Button(editPanel, SWT.NONE);
        // btnBrowse.setText("Local JAR");
        // btnBrowse.setEnabled(false);

        MyContentProvider provider = new MyContentProvider();
        tv.setLabelProvider(provider);
        tv.setContentProvider(provider);

        NuxeoSDK sdk = NuxeoSDK.getDefault();
        if (sdk != null) {
            cp =  container.getClasspathEntries();
            tv.setInput(cp);
        }

        tv.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                btnDownload.setEnabled(!event.getSelection().isEmpty());
                // btnBrowse.setEnabled(!event.getSelection().isEmpty());
                if (!event.getSelection().isEmpty()) {
                    IClasspathEntry entry = (IClasspathEntry) ((IStructuredSelection) event.getSelection()).getFirstElement();
                    srcLabel.setText(entry.getSourceAttachmentPath() != null ? "Sources configured"
                            : "Sources not configured");
                }
            }
        });
        timer = new PostModificationTimer() {
            @Override
            public void run() {
                if (tv.getTable().isDisposed()) {
                    return;
                }
                tv.setInput(getFilteredClasspath(filter.getText()));
            }
        };

        filter.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                timer.touch();
            }
        });
        timer.start();

        btnDownload.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) tv.getSelection();
                if (!selection.isEmpty()) {
                    final IClasspathEntry entry = (IClasspathEntry) selection.getFirstElement();
                    try {
                        getContainer().run(false, true,
                                new IRunnableWithProgress() {
                                    @Override
                                    public void run(IProgressMonitor monitor)
                                            throws InvocationTargetException,
                                            InterruptedException {
                                        installSources(entry);
                                        monitor.done();
                                    }
                                });
                    } catch (Exception ee) {
                        UI.showError("Failed to dowload source JAR", ee);
                    }

                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });

        // btnBrowse.addSelectionListener(new SelectionListener() {
        // @Override
        // public void widgetSelected(SelectionEvent e) {
        // FileDialog dlg = new FileDialog(getContainer().getShell());
        // dlg.setFilterExtensions(new String[] { "*.jar" });
        // String path = dlg.open();
        // if (path != null) {
        // File file = new File(path);
        // FileRef ref = new FileRef(file);
        // try {
        // ref.installTo(NuxeoSDK.getDefault().getBundlesSrcDir());
        // sourcesUpdated();
        // } catch (Exception ee) {
        // UI.showError("Failed to install sources JAR", ee);
        // }
        // }
        // }
        //
        // @Override
        // public void widgetDefaultSelected(SelectionEvent e) {
        // widgetSelected(e);
        // }
        // });
        setControl(panel);

    }

    public void installSources(IClasspathEntry entry) {
        NuxeoSDK sdk = NuxeoSDK.getDefault();
        if (sdk == null) {
            UI.showError("No Nuxeo SDK configured. Cannot continue.");
            return;
        }
        Artifact artifact = Artifact.fromClassPathEntry(entry);
        if (artifact == null) {
            UI.showError("Cannot resolve JAR " + entry.getPath().lastSegment()
                    + " to Maven GAV");
            return;
        }
        try {
            FileRef ref = MavenDownloader.downloadSourceJar(artifact);
            if (ref == null) {
                UI.showError("No sources found for corresponding artifact: "
                        + artifact);
                return;
            }
            ref.installTo(sdk.getBundlesSrcDir());
            sourcesUpdated(entry);
        } catch (IOException e) {
            UI.showError("Faield to download artifact file", e);
        }
    }

    public void sourcesUpdated(IClasspathEntry entry) {
        modified.add(entry);
        tv.update(entry, null);
        srcLabel.setText("Sources configured");
    }

    public IClasspathEntry[] getFilteredClasspath(String text) {
        ArrayList<IClasspathEntry> result = new ArrayList<IClasspathEntry>();
        for (IClasspathEntry e : cp) {
            if (e.getPath().lastSegment().contains(text)) {
                result.add(e);
            }
        }
        return result.toArray(new IClasspathEntry[result.size()]);
    }

    @Override
    public void dispose() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    @Override
    public boolean finish() {
        if (!modified.isEmpty()) {
            try {
                NuxeoSDK.reload();
            } catch (Exception e) {
                UI.showError("Failed to reload projects", e);
            }
        }
        return true;
    }

    @Override
    public IClasspathEntry getSelection() {
        return null;
    }

    @Override
    public void setSelection(IClasspathEntry containerEntry) {

    }

    class MyContentProvider extends LabelProvider implements
            IStructuredContentProvider {

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement == null) {
                return UI.EMPTY_OBJECTS;
            }
            if (inputElement.getClass().isArray()) {
                return (Object[]) inputElement;
            }
            return UI.EMPTY_OBJECTS;
        }

        @Override
        public String getText(Object element) {
            if (element instanceof IClasspathEntry) {
                return ((IClasspathEntry) element).getPath().lastSegment();
            }
            return super.getText(element);
        }

        @Override
        public Image getImage(Object element) {
            if (element instanceof IClasspathEntry) {
                IClasspathEntry entry = (IClasspathEntry) element;
                return JavaUI.getSharedImages().getImage(
                        hasSources(entry) ? ISharedImages.IMG_OBJS_JAR_WITH_SOURCE
                                : ISharedImages.IMG_OBJS_JAR);
            }
            return super.getImage(element);
        }

    }

    public boolean hasSources(IClasspathEntry entry) {
        return entry.getSourceAttachmentPath() != null
                || modified.contains(entry);
    }

}
