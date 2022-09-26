package com.xcal.eclipse.views;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.xcal.eclipse.Activator;
import com.xcal.eclipse.model.TraceStep;

public class PathView extends IMarkerTrackingView {
	private Table traceTable = null;
	private Composite parentElem = null;
	
	// Globals for the circle and dotted line
	final int IMAGE_HEIGHT = 100;
	final int IMAGE_WIDTH = 17;
	final int CIRCLE_SIZE = IMAGE_WIDTH - 1;
	final int CIRCLE_TOP_OFFSET = (int)((IMAGE_HEIGHT / 2) - (CIRCLE_SIZE / 2));
	
	/**
	 * Draws the dotted line and circle for a trace step
	 * @param parent The parent to add the dot to
	 * @param isStart If this is the start of the trace (won't draw the dotted line from top to the circle if true)
	 * @param isEnd If this is the last trace item (won't draw the dotted line from the circle to the bottom of the box)
	 * @return The image to display in the table
	 */
	private Image drawDot(Display parent, boolean isStart, boolean isEnd) {
		ImageData imData = new ImageData(IMAGE_WIDTH, IMAGE_HEIGHT, 24, new PaletteData(0xff0000,0x00ff00,0x0000ff));
		imData.setAlpha(0, 0, 0);
		Arrays.fill(imData.alphaData, (byte) 0);
		Image dot = new Image(parent, imData);
		GC gc = new GC(dot);
		gc.setForeground(parentElem.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		gc.setBackground(parentElem.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		gc.fillOval(0, CIRCLE_TOP_OFFSET, CIRCLE_SIZE, CIRCLE_SIZE);
		gc.setLineStyle(SWT.LINE_DOT);
		if (!isStart) {
			gc.drawLine((int)CIRCLE_SIZE/2, 0, (int)CIRCLE_SIZE/2, CIRCLE_TOP_OFFSET);
		}
		if (!isEnd) {
			gc.drawLine((int)CIRCLE_SIZE/2, CIRCLE_TOP_OFFSET, (int)CIRCLE_SIZE/2, IMAGE_HEIGHT);
		}
		gc.dispose();
		return dot;
	}
	
	/// Used in the click event on the trace table to jump to file/line of the step
	private void jumpToStepLocation(TraceStep step) {
		IProject project = Activator.getDefault().getCurrentProject();
		IFile file = project.getFile(step.filePath);
		final IWorkbenchPage workbench = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		// Get the default editor of the file type - this avoids some weird Eclipse behaviour
		IEditorDescriptor defaultEditor = IDE.getDefaultEditor(file);
		try {
			String editorId = defaultEditor == null ? EditorsUI.DEFAULT_TEXT_EDITOR_ID : defaultEditor.getId();
			IEditorPart editor = IDE.openEditor(workbench, file, editorId);
			if (!(editor instanceof ITextEditor)) {
				return;
			}
			ITextEditor textEditor = (ITextEditor) editor;
			IDocumentProvider docProvider = textEditor.getDocumentProvider();
			IDocument doc = docProvider.getDocument(textEditor.getEditorInput());
			int lineStart = doc.getLineOffset(step.lineNumber-1);
			textEditor.selectAndReveal(lineStart, 0);
			workbench.activate(textEditor);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void draw(Composite parent, IMarker marker) {
		if (traceTable == null) {
			makeTraceTable(parent);
		}
		final Activator activator = Activator.getDefault();
		if (activator == null) {
			return;
		}
		traceTable.removeAll();
		if (marker == null || marker.getAttribute("xcal-id", null) == null) {
			// The parent class should take care of this, but just in case.
			return;
		}
		TraceStep[] steps = activator.warningManager.traceForMarker(marker);
		for (int i = 0; i < steps.length; ++i) {
			TraceStep step = steps[i];
			TableItem item = new TableItem(traceTable, SWT.NONE);
			Image dot = drawDot(parentElem.getDisplay(), i == 0, i == (steps.length -1));
			item.setText(0, Integer.toString(i + 1));
			item.setImage(1, dot);
			item.setText(2, step.filePath+"\nLINE "+step.lineNumber+": "+step.message);
			item.setData(step);
		}
		traceTable.pack();
	}

	/// Draws a table containing the trace steps
	private void makeTraceTable(Composite parent) {
		traceTable = new Table(parent, SWT.NONE);
		traceTable.setBounds(10,10,350,300);
		TableColumn col1 = new TableColumn(traceTable, SWT.NONE);
		TableColumn col2 = new TableColumn(traceTable, SWT.NONE);
		TableColumn col3 = new TableColumn(traceTable, SWT.NONE);
		col1.setWidth(10);
		col2.setWidth(10);
		col3.setWidth(80);
		parentElem = parent;
		// Listen for window size changes and resize appropriately
		traceTable.addListener(SWT.MeasureItem, new Listener() {
			public void handleEvent(Event e) {
				int clientWidth = parent.getClientArea().width;
				e.height = 100;
				int tableWidth = clientWidth;
				try {
					int smallColSize = 32;
					int largeColSize = tableWidth-(smallColSize*2);
					traceTable.getColumn(0).setWidth(smallColSize);
					traceTable.getColumn(1).setWidth(smallColSize);
					traceTable.getColumn(2).setWidth(largeColSize);
				} catch (Exception ex) {
					
				}
				e.width = tableWidth;
			}
		});
		// Listen for table selection changes and jump to the selected line
		traceTable.addSelectionListener(new SelectionListener () {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (traceTable.getSelectionCount() != 1) {
					return;
				}
				Object selectedObject = traceTable.getSelection()[0].getData();
				if (selectedObject == null || selectedObject.getClass() != TraceStep.class) {
					return;
				}
				TraceStep step = (TraceStep)selectedObject;
				jumpToStepLocation(step);
				traceTable.forceFocus();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
			
		});
	}

	@Override
	protected void clean() {
		if (traceTable == null) {
			return;
		}
		traceTable.dispose();
		traceTable = null;
		
	}
}
