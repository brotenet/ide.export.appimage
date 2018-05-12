package ide.export.appimage;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.environment.Environment;
import org.eclipse.swt.environment.WindowManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

public class ExportAppImageWizard extends Wizard implements IExportWizard{

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		ExportAppImageComposite composite = new ExportAppImageComposite(WindowManager.newShell(Display.getCurrent().getActiveShell(),false, false, true));
		composite.getShell().setSize(500, 420);
		composite.getShell().setText("Export AppImage");
		composite.getShell().setImage(Environment.Resources.getImageFromResource("/ide/export/appimage/application.png"));
		WindowManager.setLocation(composite.getShell(), WindowManager.LOCATION_MIDDLE_CENTER_TO_DISPLAY);
		WindowManager.open(composite.getShell());
	}

	@Override
	public boolean performFinish() {
		return false;
	}

	

}
