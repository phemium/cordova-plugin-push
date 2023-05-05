const { join } = require("path");
const { existsSync, readFileSync, writeFileSync } = require("fs");
const { promisify } = require("util");
const { parseElementtreeSync } = require("cordova-common/src/util/xml-helpers");
const platform = require("cordova-android");
const _glob = require("glob");
const glob = promisify(_glob);

module.exports = async function (context) {
  context = {};
  context.opts = {};
  context.opts.projectRoot = "/Users/alex/Documents/Projects/cigna/cigna-web-app/src";
  await configurePEM(context);
  if (!isExecutable()) {
    console.log("[cordova-plugin-push::before-compile] skipping before_compile hookscript.");
    return;
  }

  const buildGradleFilePath = join(context.opts.projectRoot, "platforms/android/build.gradle");

  if (!existsSync(buildGradleFilePath)) {
    console.log('[cordova-plugin-push::before-compile] could not find "build.gradle" file.');
    return;
  }

  updateBuildGradle(context, buildGradleFilePath);
};

/**
 * This hookscript is executable only when the platform version less then 10.x
 * @returns Boolean
 */
function isExecutable() {
  const majorVersion = parseInt(platform.version(), 10);
  return majorVersion < 10 && majorVersion >= 9;
}

function getPluginKotlinVersion(context) {
  const pluginConfig = parseElementtreeSync(
    join(context.opts.projectRoot, "plugins/@phemium-costaisa/cordova-plugin-push/plugin.xml")
  );

  return pluginConfig
    .findall('./platform[@name="android"]')
    .pop()
    .findall('./config-file[@target="config.xml"]')
    .pop()
    .findall("preference")
    .filter((elem) => elem.attrib.name.toLowerCase() === "GradlePluginKotlinVersion".toLowerCase())
    .pop().attrib.value;
}

function updateBuildGradle(context, buildGradleFilePath) {
  const kotlinVersion = getPluginKotlinVersion(context);
  const updateContent = readFileSync(buildGradleFilePath, "utf8").replace(
    /ext.kotlin_version = ['"](.*)['"]/g,
    `ext.kotlin_version = '${kotlinVersion}'`
  );

  writeFileSync(buildGradleFilePath, updateContent);

  console.log(
    `[cordova-plugin-push::before-compile] updated "build.gradle" file with kotlin version set to: ${kotlinVersion}`
  );
}

async function configurePEM(context) {
  console.log("[cordova-plugin-push::before-compile] Configuring PEM integration");
  const pemDir = join(
    context.opts.projectRoot,
    "platforms/android/app/src/main/java/com/phemium/plugins/PhemiumEnduserPlugin.java"
  );
  const pemIntegration = existsSync(pemDir);
  if (pemIntegration) {
    console.log(
      "[cordova-plugin-push::before-compile] PEM detected, enabling PEM feature in plugin"
    );
  } else {
    console.log(
      "[cordova-plugin-push::before-compile] PEM not detected, disabling PEM feature in plugin"
    );
  }
  let files = await glob(
    join(context.opts.projectRoot, "platforms/android/app/src/main/java/com/adobe/phonegap/push") +
      "/**/*.kt"
  );
  for (const file of files) {
    let content = readFileSync(file, { encoding: "utf-8" });
    if (content.includes("// BEGIN PEM")) {
      let lines = content.split(/\r?\n/);

      let pemSection = null;
      for (const [index, line] of lines.entries()) {
        if (line.trim() === "// END PEM") {
          pemSection = null;
        }
        if (pemSection !== null) {
          // Is PEM section
          if (pemIntegration) {
            if (line.startsWith("// ")) {
              lines[index] = lines[index].slice(2);
            }
          } else {
            lines[index] = "// " + lines[index];
          }
        }
        if ((line.trim() === "// BEGIN PEM" || pemSection) && line.trim() !== "// END PEM") {
          pemSection = true;
        } else {
          pemSection = null;
        }
      }
      content = lines.join("\r\n");
      writeFileSync(file, content, { encoding: "utf-8" });
    }
  }
}
