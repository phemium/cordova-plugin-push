const { join } = require("path");
const { readFileSync, writeFileSync } = require("fs");
const { parseElementtreeSync } = require("cordova-common/src/util/xml-helpers");

module.exports = async function (context) {
  configureFirebaseIOS(context);
};

function configureFirebaseIOS(context) {
  console.log("[cordova-plugin-push::before-compile] Importing Firebase...");
  const appName = getAppName(context);

  const appDelegate = join(context.opts.projectRoot, "platforms/ios", `${appName}/AppDelegate.m`);
  const appDelegateContent = readFileSync(appDelegate, "utf8");
  if (!appDelegateContent.includes("[FIRApp configure]")) {
    const lines = appDelegateContent.split("\n");
    const importLine = lines.findIndex((line) => line.includes("@implementation AppDelegate"));
    if (importLine > -1) {
      lines.splice(importLine, 0, "@import FirebaseCore;");
      lines.splice(importLine + 1, 0, "");
    }
    const configureLine = lines.findIndex((line) => line.includes("self.viewController"));
    if (configureLine > -1) {
      lines.splice(configureLine + 1, 0, "\t[FIRApp configure];");
    }
    if (importLine > -1 || configureLine > -1) {
      console.log("[cordova-plugin-push::before-compile] Patching AppDelegate.m...");
      const newContent = lines.join("\n");
      writeFileSync(appDelegate, newContent, "utf8");
    }
  }
}

function getAppName(context) {
  const projectConfig = parseElementtreeSync(join(context.opts.projectRoot, "config.xml"));
  return projectConfig.find("./name").text;
}
