package ide.export.appimage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.environment.Environment;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.json.JSONObject;

public class ExportAppImageComposite extends Composite {

	JSONObject project_information;
	String build_file_data;
	String build_file_path;
	String deployment_dir;
	String application_name;
	String appimage_assets_prefix;
	private boolean completed = false;
	private Text txtFileName;
	private Text txtTargetDir;
	private Text log;
	
	public ExportAppImageComposite(Composite parent) {
		super(parent, SWT.NONE);
		project_information = Activator.getProjectInformation();
		deployment_dir = project_information.getString("project_full_path") + Environment.getProperty(Environment.PROPERTY_FILE_SEPARATOR) + "deploy";
		build_file_data = Environment.Resources.getStringFromResource("/ide/export/appimage/build.xml");
		build_file_path = project_information.getString("project_full_path") + Environment.getProperty(Environment.PROPERTY_FILE_SEPARATOR) + "build.xml";
		appimage_assets_prefix = project_information.getString("project_full_path") + Environment.getProperty(Environment.PROPERTY_FILE_SEPARATOR);
		if(project_information.has("properties")) {
			if(project_information.getJSONObject("properties").has("projectDescription")) {
				if(project_information.getJSONObject("properties").getJSONObject("projectDescription").has("name")) {
					application_name = project_information.getJSONObject("properties").getJSONObject("projectDescription").getString("name").trim().replaceAll(" ", "_");
				}
			}
		}
		populate();
	}
	private void populate() {
		setLayout(new GridLayout(4, false));
		
		CLabel lblNewLabel = new CLabel(this, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 3));
		lblNewLabel.setImage(new Image(null, ExportAppImageComposite.class.getResourceAsStream("/ide/export/appimage/appimage-big.png")));
		lblNewLabel.setText("");
		
		CLabel lblNewLabel_1 = new CLabel(this, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("Package Name:");
		
		txtFileName = new Text(this, SWT.BORDER | SWT.RIGHT);
		txtFileName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtFileName.setText(application_name);
		
		CLabel lblNewLabel_4 = new CLabel(this, SWT.NONE);
		lblNewLabel_4.setText(".appimage");
		
		CLabel lblNewLabel_2 = new CLabel(this, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_2.setText("Output Directory:");
		
		txtTargetDir = new Text(this, SWT.BORDER | SWT.RIGHT);
		txtTargetDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtTargetDir.setText(deployment_dir);
		
		Button btnBrowse = new Button(this, SWT.NONE);
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				try {
					txtTargetDir.setText(dialog.open());
				} catch (Exception ignore) {}
			}
		});
		btnBrowse.setText("Browse");
		new Label(this, SWT.NONE);
		
		Button txtKeepBuildFile = new Button(this, SWT.CHECK);
		txtKeepBuildFile.setSelection(true);
		txtKeepBuildFile.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		txtKeepBuildFile.setText("Preserve ANT build file.");
		
		Group group = new Group(this, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		group.setText("Log monitor:");
		group.setLayout(new GridLayout(1, false));
		
		log = new Text(group, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		log.setText("The application will be packaged with the following dependencies:" + Environment.getProperty(Environment.PROPERTY_LINE_SEPARATOR) + project_information.getJSONArray("libraries").toString(1));
		log.setEditable(false);
		log.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite composite = new Composite(this, SWT.NONE);
		GridData gd_composite = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
		gd_composite.widthHint = 118;
		composite.setLayoutData(gd_composite);
		composite.setLayout(new GridLayout(2, false));
		
		Button btnCancel = new Button(composite, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				getShell().close();
			}
		});
		GridData gd_btnCancel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnCancel.widthHint = 100;
		btnCancel.setLayoutData(gd_btnCancel);
		btnCancel.setText("Cancel");
		btnCancel.setImage(new Image(null, ExportAppImageComposite.class.getResourceAsStream("/ide/export/appimage/cancel.png")));
		
		Button btnBuild = new Button(composite, SWT.NONE);
		btnBuild.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if(txtFileName.getText().trim().length() < 1 || txtTargetDir.getText().trim().length() < 1) {
					MessageBox message = new MessageBox(getShell(), SWT.ICON_WARNING);
					message.setText("Invalid Input");
					message.setMessage("'Archive Name' and/or 'Output Directory' are invalid." + Environment.getProperty(Environment.PROPERTY_LINE_SEPARATOR) + "Please review before build.");
					message.open();
				}else {
					if(completed == false) {
						try {
							log.setText("Initializing build..");
							build_file_data = build_file_data.replaceAll("<ESC>",  "").replaceAll("</ESC>", "");
							build_file_data = build_file_data.replace("#PROJECT_PATH#", Activator.getProjectPath());
							build_file_data = build_file_data.replace("#CLASSPATH_ENTRIES#", getClassPathEntries(project_information));
							build_file_data = build_file_data.replace("#PROJECT_LIBRARIES#", getProjectLibraries(project_information));
							build_file_data = build_file_data.replace("#PACKAGE_NAME#", txtFileName.getText().trim());
							build_file_data = build_file_data.replace("#OUTPUT_DIR#", txtTargetDir.getText().trim());
							build_file_data = build_file_data.replace("#BIN_DIR#", getBinDir(project_information));
							build_file_data = build_file_data.replace("#SRC_DIR#", getSrcDir(project_information));
							log.setText(log.getText() + Environment.getProperty(Environment.PROPERTY_LINE_SEPARATOR) + "Preparing build project...");
							Project project = new Project();
							log.setText(log.getText() + Environment.getProperty(Environment.PROPERTY_LINE_SEPARATOR) + "Creating build file...");
							Environment.FileSystem.delete(build_file_path);
							Environment.FileSystem.touch(build_file_path, build_file_data);
							log.setText(log.getText() + Environment.getProperty(Environment.PROPERTY_LINE_SEPARATOR) + "Extracting AppImage assets...");
							createFileFromResource("/ide/export/appimage/.Diricon", appimage_assets_prefix + ".Diricon");
							openFilesystemPermissions( appimage_assets_prefix + ".Diricon");
							createFileFromResource("/ide/export/appimage/appimagetool-x86_64.AppImage", appimage_assets_prefix + "appimagetool-x86_64.AppImage");
							createFileFromResource("/ide/export/appimage/application.desktop", appimage_assets_prefix + "application.desktop");
							openFilesystemPermissions(appimage_assets_prefix + "application.desktop");
							createFileFromResource("/ide/export/appimage/application.png", appimage_assets_prefix + "application.png");
							openFilesystemPermissions(appimage_assets_prefix + "application.png");
							createFileFromResource("/ide/export/appimage/AppRun-x86_64", appimage_assets_prefix + "AppRun-x86_64");
							openFilesystemPermissions(appimage_assets_prefix + "AppRun-x86_64");
							createFileFromResource("/ide/export/appimage/run", appimage_assets_prefix + "run");
							openFilesystemPermissions(appimage_assets_prefix + "run");					
							log.setText(log.getText() + Environment.getProperty(Environment.PROPERTY_LINE_SEPARATOR) + "Loading build file...");
							project.setUserProperty("ant.file", build_file_path);
							project.init();
							log.setText(log.getText() + Environment.getProperty(Environment.PROPERTY_LINE_SEPARATOR) + "Creating build helper...");
							ProjectHelper helper = ProjectHelper.getProjectHelper();
							project.addReference("ant.projectHelper", helper);
							helper.parse(project, new File(build_file_path));
							log.setText(log.getText() + Environment.getProperty(Environment.PROPERTY_LINE_SEPARATOR) + "Building AppImage package...");
							project.executeTarget(project.getDefaultTarget());
							if(txtKeepBuildFile.getSelection() == false) {
								Environment.FileSystem.delete(build_file_path);
							}else {
								Environment.FileSystem.copy(build_file_path, txtTargetDir.getText().trim() + Environment.getProperty(Environment.PROPERTY_FILE_SEPARATOR) + "build.xml");
							}
							completed = true;
							btnBuild.setText("Close");
							btnBuild.setImage(Environment.Resources.getImageFromResource("/ide/export/appimage/exit.png"));
							btnCancel.setVisible(false);
						} catch (Exception exception) {
							Activator.displayErrorDialog(getShell(), exception);
						}
					}else {
						getShell().getParent().getShell().close();
					}
				}				
			}
		});
		GridData gd_btnBuild = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		gd_btnBuild.widthHint = 100;
		btnBuild.setLayoutData(gd_btnBuild);
		btnBuild.setText("Build");
		btnBuild.setImage(new Image(null, ExportAppImageComposite.class.getResourceAsStream("/ide/export/appimage/package.png")));
	}
	
	private static String getClassPathEntries(JSONObject project_information) {
		String output = "";
		for(JSONObject classpath_entry : project_information.getJSONObject("classpath").getJSONArray("classpathentry").toArray(JSONObject.class)) {
			if(classpath_entry.has("kind")) {
				if(classpath_entry.getString("kind").equalsIgnoreCase("lib")) {
					String lib_filename = classpath_entry.getString("path");
					lib_filename = lib_filename.split("/")[lib_filename.split("/").length - 1];
					output = output + "<pathelement location=\"${lib_dir}/" + lib_filename + "\" />" + Environment.getProperty(Environment.PROPERTY_LINE_SEPARATOR);
				}
			}
		}
		return output;
	}
	
	private static String getProjectLibraries(JSONObject project_information) {
		String output = "";
		for(JSONObject classpath_entry : project_information.getJSONObject("classpath").getJSONArray("classpathentry").toArray(JSONObject.class)) {
			if(classpath_entry.has("kind")) {
				if(classpath_entry.getString("kind").equalsIgnoreCase("lib")) {
					output = output + "<copy file=\"" +  classpath_entry.getString("path") + "\" todir=\"${appimage_dir}/usr/bin/${lib_dir}\" />" + Environment.getProperty(Environment.PROPERTY_LINE_SEPARATOR);
				}
			}
		}
		return output;
	}
	
	private static String getBinDir(JSONObject project_information) {
		String output = "";
		for(JSONObject classpath_entry : project_information.getJSONObject("classpath").getJSONArray("classpathentry").toArray(JSONObject.class)) {
			if(classpath_entry.has("kind")) {
				if(classpath_entry.getString("kind").equalsIgnoreCase("output")) {
					output = classpath_entry.getString("path");
				}
			}
		}
		return output;
	}
	
	private static String getSrcDir(JSONObject project_information) {
		String output = "";
		for(JSONObject classpath_entry : project_information.getJSONObject("classpath").getJSONArray("classpathentry").toArray(JSONObject.class)) {
			if(classpath_entry.has("kind")) {
				if(classpath_entry.getString("kind").equalsIgnoreCase("src")) {
					output = classpath_entry.getString("path");
				}
			}
		}
		return output;
	}
	
	private void createFileFromResource(String resource_path, String target_file_path) throws IOException {
		InputStream inputStream = getClass().getResourceAsStream(resource_path);
		OutputStream outputStream = new FileOutputStream(new File(target_file_path));
		int read = 0;
		byte[] bytes = new byte[1024];
		while ((read = inputStream.read(bytes)) != -1) {
			outputStream.write(bytes, 0, read);
		}
		outputStream.close();
		inputStream.close();
	}
	
	public void openFilesystemPermissions(String file_path) throws IOException{
	    Set<PosixFilePermission> perms = new HashSet<>();
	    perms.add(PosixFilePermission.OWNER_READ);
	    perms.add(PosixFilePermission.OWNER_WRITE);
	    perms.add(PosixFilePermission.OWNER_EXECUTE);

	    perms.add(PosixFilePermission.OTHERS_READ);
	    perms.add(PosixFilePermission.OTHERS_WRITE);
	    perms.add(PosixFilePermission.OTHERS_EXECUTE);

	    perms.add(PosixFilePermission.GROUP_READ);
	    perms.add(PosixFilePermission.GROUP_WRITE);
	    perms.add(PosixFilePermission.GROUP_EXECUTE);
	    File file = new File(file_path);
	    Files.setPosixFilePermissions(file.toPath(), perms);
	    
	}

}
