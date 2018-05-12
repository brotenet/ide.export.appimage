package ide.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class ExceptionComposite extends Composite {
	private Browser txtException;
	private CLabel txtMessage;

	public ExceptionComposite(Shell shell, String message, Throwable exception) {
		super(shell, SWT.NONE);
		populate(message, getStackTrace(exception));
	}
	private void populate(String message, String exception_text) {
		setLayout(new GridLayout(2, false));
		
		CLabel lblNewLabel = new CLabel(this, SWT.NONE);
		lblNewLabel.setImage(new Image(null, ExceptionComposite.class.getResourceAsStream("/ide/exceptions/error.png")));
		lblNewLabel.setText("");
		
		txtMessage = new CLabel(this, SWT.NONE);
		txtMessage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtMessage.setText(message);
		
		txtException = new Browser(this, SWT.NONE);
		txtException.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		txtException.setText(exception_text);
		new Label(this, SWT.NONE);
		
		Button btnClose = new Button(this, SWT.NONE);
		btnClose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				getShell().close();
			}
		});
		btnClose.setImage(new Image(null, ExceptionComposite.class.getResourceAsStream("/ide/exceptions/close.png")));
		GridData gd_btnClose = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_btnClose.widthHint = 100;
		btnClose.setLayoutData(gd_btnClose);
		btnClose.setText("Close");
	}
	
	public static String getStackTrace(Throwable exception) {
	    StringWriter string_writer = new StringWriter();
	    exception.printStackTrace(new PrintWriter(string_writer));
	    return "<html><body style=\"font-size:12px;\">" + string_writer.toString() + "</body></html>";
	}
}
