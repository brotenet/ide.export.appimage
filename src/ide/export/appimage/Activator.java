package ide.export.appimage;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.core.internal.resources.File;
import org.eclipse.core.internal.resources.Folder;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.environment.Environment;
import org.eclipse.swt.environment.WindowManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.util.converters.XMLConverter;
import org.osgi.framework.BundleContext;

import ide.exceptions.ExceptionComposite;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ide.export.appimage"; //$NON-NLS-1$

	// The Eclipse workspace directory path
		public static final String WORKSPACE_DIR = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
			
		// The plugin directory path in the workspace
		public static final String PLUGIN_WORKSPACE_DIR = WORKSPACE_DIR + 
				Environment.getProperty(Environment.PROPERTY_FILE_SEPARATOR) + ".metadata" + 
				Environment.getProperty(Environment.PROPERTY_FILE_SEPARATOR) + "templates" + 
				Environment.getProperty(Environment.PROPERTY_FILE_SEPARATOR) + "RWT";
		
		// The Project type templates description file
		public static final String PROJECT_TYPES_FILE_PATH = PLUGIN_WORKSPACE_DIR + Environment.getProperty(Environment.PROPERTY_FILE_SEPARATOR) + "index.json";
		
		// The shared instance
		private static Activator plugin;
		
		/**
		 * The constructor
		 */
		public Activator() {
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
		 */
		public void start(BundleContext context) throws Exception {
			super.start(context);
			plugin = this;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
		 */
		public void stop(BundleContext context) throws Exception {
			plugin = null;
			super.stop(context);
		}

		/**
		 * Returns the shared instance
		 *
		 * @return the shared instance
		 */
		public static Activator getDefault() {
			return plugin;
		}
		
		public static ImageDescriptor getImageDescriptor(String path) {
			return imageDescriptorFromPlugin(PLUGIN_ID, path);
		}
		
		public static ErrorDialog getExceptionDialog(Shell parent, String title, String message, Throwable exception) {
			Status status = new Status(IStatus.ERROR, PLUGIN_ID, message, exception);
			return new ErrorDialog(parent, title, message, status, IStatus.ERROR);
		}
		
		public static IProject getSelectedProject() {
			try {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			    if (window != null)
			    {
			        IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
			        Object firstElement = selection.getFirstElement();
			        if (firstElement instanceof IAdaptable)
			        {
			        	return (IProject)((IAdaptable)firstElement).getAdapter(IProject.class);
			        }else {
			        	return null;
			        }
			    }else {
			    	return null;
			    }
			} catch (Exception exception) {
				return null;
			}
		}
		
		public static String getSelectedProjectName() {
			try {
				return getSelectedProject().getName();
			} catch (Exception exception) {
				return "";
			}
		}
		
		public static String getSelectedProjectWorkspacePath() {
			try {
				return String.valueOf(getSelectedProject().getFullPath());
			} catch (Exception exception) {
				return "";
			}		
		}
		
		public static String getSelectedProjectPhysicalPath() {
			try {
				return String.valueOf(getSelectedProject().getLocation());
			} catch (Exception exception) {
				return "";
			}		
		}
		
		public static String updateTagVariables(String input) {
			return input.replaceAll("#WORKSPACE_DIR#", Activator.WORKSPACE_DIR)
					.replaceAll("#PLUGIN_WORKSPACE_DIR#", Activator.PLUGIN_WORKSPACE_DIR)
					.replaceAll("#PROJECT_TYPES_FILE_PATH#", Activator.PROJECT_TYPES_FILE_PATH)
					.replaceAll("#SELECTED_PROJECT_NAME#", Activator.getSelectedProjectName())
					.replaceAll("#SELECTED_PROJECT_WORKSPACE_PATH#", Activator.getSelectedProjectWorkspacePath())
					.replaceAll("#SELECTED_PROJECT_PHYSICAL_PATH#", Activator.getSelectedProjectPhysicalPath())
					.replaceAll("#PROPERTY_FILE_SEPARATOR#", Environment.getProperty(Environment.PROPERTY_FILE_SEPARATOR))
					.replaceAll("#PROPERTY_JAVA_CLASS_PATH#", Environment.getProperty(Environment.PROPERTY_JAVA_CLASS_PATH))
					.replaceAll("#PROPERTY_JAVA_HOME#", Environment.getProperty(Environment.PROPERTY_JAVA_HOME))
					.replaceAll("#PROPERTY_JAVA_VENDOR#", Environment.getProperty(Environment.PROPERTY_JAVA_VENDOR))
					.replaceAll("#PROPERTY_JAVA_VENDOR_URL#", Environment.getProperty(Environment.PROPERTY_JAVA_VENDOR_URL))
					.replaceAll("#PROPERTY_JAVA_VERSION#", Environment.getProperty(Environment.PROPERTY_JAVA_VERSION))
					.replaceAll("#PROPERTY_LINE_SEPARATOR#", Environment.getProperty(Environment.PROPERTY_LINE_SEPARATOR))
					.replaceAll("#PROPERTY_OS_ARCH#", Environment.getProperty(Environment.PROPERTY_OS_ARCH))
					.replaceAll("#PROPERTY_OS_NAME#", Environment.getProperty(Environment.PROPERTY_OS_NAME))
					.replaceAll("#PROPERTY_OS_VERSION#", Environment.getProperty(Environment.PROPERTY_OS_VERSION))
					.replaceAll("#PROPERTY_PATH_SEPARATOR#", Environment.getProperty(Environment.PROPERTY_PATH_SEPARATOR))
					.replaceAll("#PROPERTY_USER_DIR#", Environment.getProperty(Environment.PROPERTY_USER_DIR))
					.replaceAll("#PROPERTY_USER_HOME#", Environment.getProperty(Environment.PROPERTY_USER_HOME))
					.replaceAll("#PROPERTY_USER_NAME#", Environment.getProperty(Environment.PROPERTY_USER_NAME));
		}

	public static String getSelectionPackage(Object selection) {
			if (selection == null) {
				selection = getSelection();
			}
			if (selection instanceof PackageFragment) {
				String package_name = "";
				for (String package_node : ((PackageFragment) selection).names) {
					package_name += "." + package_node;
				}
				package_name = package_name.substring(1);
				return package_name;
			} else if (selection instanceof PackageFragmentRoot) {
				return "";
			} else {
				return getSelectionPackage(((IStructuredSelection) ((IStructuredSelection) selection).getFirstElement()).getFirstElement());
			}
		}

		public static String getProjectPath() {
			Object item = getSelection();
			if(item instanceof PackageFragmentRoot) {
				return ((PackageFragmentRoot) item).getJavaProject().getProject().getLocation().toString();
			}else if(item instanceof PackageFragment) {
				return ((PackageFragment) item).getJavaProject().getProject().getLocation().toString();
			}else if(item instanceof Project) {
				return ((Project) item).getLocation().toString();
			}else if(item instanceof File) {
				return ((File) item).getProject().getLocation().toString();
			}else if(item instanceof Folder) {
				return ((Folder) item).getProject().getLocation().toString();
			}else {
				return ((IResource) item).getProject().getLocation().toString();
			}
		}

		public static IProject getProject() {
			Object item = getSelection();
			if(item instanceof PackageFragmentRoot) {
				return ((PackageFragmentRoot) item).getJavaProject().getProject();
			}else if(item instanceof PackageFragment) {
				return ((PackageFragment) item).getJavaProject().getProject();
			}else if(item instanceof Project) {
				return ((Project) item);
			}else if(item instanceof File) {
				return ((File) item).getProject();
			}else if(item instanceof Folder) {
				return ((Folder) item).getProject();
			}else {
				return ((IResource) item).getProject();
			}
		}
		
		public static Object getSelection() {
			ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
			IStructuredSelection selection = (IStructuredSelection) selectionService.getSelection();
			return selection.getFirstElement();
		}
		
		public static String getSelectionPath() {
			Object item = getSelection();
			if(item instanceof PackageFragmentRoot) {
				return ((PackageFragmentRoot) item).getJavaProject().getProject().getLocation().toString();
			}else if(item instanceof PackageFragment) {
				return ((PackageFragment) item).getJavaProject().getProject().getLocation().toString();
			}else if(item instanceof Project) {
				return ((Project) item).getLocation().toString();
			}else if(item instanceof File) {
				return ((File) item).getProject().getLocation().toString();
			}else if(item instanceof Folder) {
				return ((Folder) item).getProject().getLocation().toString();
			}else {
				return ((IResource) item).getProject().getLocation().toString();
			}
		}
		
		public static String getSelectionPath(Object item) {
			if(item instanceof PackageFragmentRoot) {
				return ((PackageFragmentRoot) item).getJavaProject().getProject().getLocation().toString();
			}else if(item instanceof PackageFragment) {
				return ((PackageFragment) item).getJavaProject().getProject().getLocation().toString();
			}else if(item instanceof Project) {
				return ((Project) item).getLocation().toString();
			}else if(item instanceof File) {
				return ((File) item).getProject().getLocation().toString();
			}else if(item instanceof Folder) {
				return ((Folder) item).getProject().getLocation().toString();
			}else {
				return ((IResource) item).getProject().getLocation().toString();
			}
		}
		
		public static JSONObject getProjectInformation() {
			JSONObject output = new JSONObject();
			try {
				String project_full_path = getProjectPath();
				output.put("properties", XMLConverter.toJSONObject(new String(Files.readAllBytes(Paths.get(project_full_path + System.getProperty("file.separator") + ".project")))));
				output.put("classpath", XMLConverter.toJSONObject(new String(Files.readAllBytes(Paths.get(project_full_path + System.getProperty("file.separator") + ".classpath")))).getJSONObject("classpath"));
				output.put("project_full_path", project_full_path);
				output.put("libraries", new JSONArray());
				for (Object entry_object : output.getJSONObject("classpath").getJSONArray("classpathentry").toArray()) {
					JSONObject entry = (JSONObject) entry_object;
					if(entry.getString("kind").equalsIgnoreCase("src")) {
						output.put("source_full_path", project_full_path + System.getProperty("file.separator") + entry.getString("path"));
					}else if(entry.getString("kind").equalsIgnoreCase("output")) {
						output.put("output_full_path", project_full_path + System.getProperty("file.separator") + entry.getString("path"));
					}else if(entry.getString("kind").equalsIgnoreCase("lib")) {
						output.getJSONArray("libraries").put(project_full_path + System.getProperty("file.separator") + entry.getString("path"));
					}
				}
			} catch (Exception ignore) {}
			return output;
		}
		
		public static IWorkspace getWorkspace() {
			return ResourcesPlugin.getWorkspace();
		}
		
		public static IWorkspaceRoot getWorkspaceRoot() {
			return ResourcesPlugin.getWorkspace().getRoot();
		}

		public static int getProjectsCount() {
			return ResourcesPlugin.getWorkspace().getRoot().getProjects().length;
		}
		
		public static void displayErrorDialog(Shell shell, Throwable exception) {
			Shell dialog = WindowManager.newShell(shell, true, false, true);
			dialog.setSize(640, 480);
			ExceptionComposite composite = new ExceptionComposite(dialog, exception.getMessage(), exception);
			WindowManager.setLocation(dialog, WindowManager.LOCATION_MIDDLE_CENTER_TO_DISPLAY);
			WindowManager.open(dialog);
		}

}
