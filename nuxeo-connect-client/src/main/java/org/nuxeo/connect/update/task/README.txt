Usage:

LocalPackage pkg = service.getPackage("package_to_install");

InstallTask task = service.newInstallTask(pkg);

ValidationStatus status = task.validate();
if (status.hasErrors()) {
 // task cannot be run show errors to the user
} else if(status.hasWarnings()) {
  // task can be run but there are warnings. show warnings to the user and let it decide whther or not to run the task
} else {
  try {
    task.run();
  } catch (Throwabe t) {
    task.rollback();
  }
}
