#!/usr/bin/env node

/**
 * Android Build Automation Script
 *
 * This script automates the Android app build process including:
 * - Version management and increment
 * - Build variants and signing
 * - APK/AAB generation
 * - Build optimization
 */

const fs = require('fs-extra');
const path = require('path');
const { execSync } = require('child_process');
const { Command } = require('commander');
const chalk = require('chalk');
const ora = require('ora');
require('dotenv').config();

class AndroidBuilder {
  constructor(options = {}) {
    this.projectDir = options.projectDir || 'android';
    this.buildType = options.buildType || 'release';
    this.flavor = options.flavor || '';
    this.outputDir = options.outputDir || 'build/outputs';
    this.gradlePath = path.join(this.projectDir, 'gradlew');

    // Windows compatibility
    if (process.platform === 'win32' && !this.gradlePath.endsWith('.bat')) {
      this.gradlePath += '.bat';
    }
  }

  /**
   * Check if Gradle wrapper exists
   */
  async checkGradleWrapper() {
    if (!await fs.pathExists(this.gradlePath)) {
      throw new Error(`Gradle wrapper not found at: ${this.gradlePath}`);
    }
  }

  /**
   * Get current version from build.gradle
   */
  async getCurrentVersion() {
    const buildGradlePath = path.join(this.projectDir, 'app', 'build.gradle');

    if (!await fs.pathExists(buildGradlePath)) {
      throw new Error(`build.gradle not found at: ${buildGradlePath}`);
    }

    const content = await fs.readFile(buildGradlePath, 'utf8');

    const versionCodeMatch = content.match(/versionCode\s+(\d+)/);
    const versionNameMatch = content.match(/versionName\s+"([^"]+)"/);

    return {
      versionCode: versionCodeMatch ? parseInt(versionCodeMatch[1]) : 1,
      versionName: versionNameMatch ? versionNameMatch[1] : '1.0.0'
    };
  }

  /**
   * Increment version code
   */
  async incrementVersionCode() {
    const spinner = ora('Incrementing version code...').start();

    try {
      const buildGradlePath = path.join(this.projectDir, 'app', 'build.gradle');
      let content = await fs.readFile(buildGradlePath, 'utf8');

      const currentVersion = await this.getCurrentVersion();
      const newVersionCode = currentVersion.versionCode + 1;

      content = content.replace(
        /versionCode\s+\d+/,
        `versionCode ${newVersionCode}`
      );

      await fs.writeFile(buildGradlePath, content);

      spinner.succeed(`Version code incremented to: ${newVersionCode}`);
      return newVersionCode;
    } catch (error) {
      spinner.fail(`Failed to increment version code: ${error.message}`);
      throw error;
    }
  }

  /**
   * Update version name
   */
  async updateVersionName(versionName) {
    const spinner = ora(`Updating version name to: ${versionName}...`).start();

    try {
      const buildGradlePath = path.join(this.projectDir, 'app', 'build.gradle');
      let content = await fs.readFile(buildGradlePath, 'utf8');

      content = content.replace(
        /versionName\s+"[^"]+"/,
        `versionName "${versionName}"`
      );

      await fs.writeFile(buildGradlePath, content);

      spinner.succeed(`Version name updated to: ${versionName}`);
    } catch (error) {
      spinner.fail(`Failed to update version name: ${error.message}`);
      throw error;
    }
  }

  /**
   * Clean build directory
   */
  async clean() {
    const spinner = ora('Cleaning build directory...').start();

    try {
      const command = `${this.gradlePath} clean`;
      execSync(command, {
        cwd: this.projectDir,
        stdio: 'pipe'
      });

      spinner.succeed('Build directory cleaned');
    } catch (error) {
      spinner.fail(`Failed to clean: ${error.message}`);
      throw error;
    }
  }

  /**
   * Build APK
   */
  async buildAPK() {
    const spinner = ora(`Building ${this.buildType} APK...`).start();

    try {
      const task = this.flavor
        ? `assemble${this.flavor}${this.buildType.charAt(0).toUpperCase() + this.buildType.slice(1)}`
        : `assemble${this.buildType.charAt(0).toUpperCase() + this.buildType.slice(1)}`;

      const command = `${this.gradlePath} ${task}`;

      execSync(command, {
        cwd: this.projectDir,
        stdio: 'pipe'
      });

      const apkPath = this.getAPKPath();
      spinner.succeed(`APK built successfully: ${apkPath}`);
      return apkPath;
    } catch (error) {
      spinner.fail(`Failed to build APK: ${error.message}`);
      throw error;
    }
  }

  /**
   * Build AAB (Android App Bundle)
   */
  async buildAAB() {
    const spinner = ora(`Building ${this.buildType} AAB...`).start();

    try {
      const task = this.flavor
        ? `bundle${this.flavor}${this.buildType.charAt(0).toUpperCase() + this.buildType.slice(1)}`
        : `bundle${this.buildType.charAt(0).toUpperCase() + this.buildType.slice(1)}`;

      const command = `${this.gradlePath} ${task}`;

      execSync(command, {
        cwd: this.projectDir,
        stdio: 'pipe'
      });

      const aabPath = this.getAABPath();
      spinner.succeed(`AAB built successfully: ${aabPath}`);
      return aabPath;
    } catch (error) {
      spinner.fail(`Failed to build AAB: ${error.message}`);
      throw error;
    }
  }

  /**
   * Get APK output path
   */
  getAPKPath() {
    const flavorPath = this.flavor ? `${this.flavor}/` : '';
    return path.join(
      this.projectDir,
      'app',
      'build',
      'outputs',
      'apk',
      flavorPath + this.buildType,
      `app-${this.flavor ? this.flavor + '-' : ''}${this.buildType}.apk`
    );
  }

  /**
   * Get AAB output path
   */
  getAABPath() {
    const flavorPath = this.flavor ? `${this.flavor}/` : '';
    return path.join(
      this.projectDir,
      'app',
      'build',
      'outputs',
      'bundle',
      flavorPath + this.buildType,
      `app-${this.flavor ? this.flavor + '-' : ''}${this.buildType}.aab`
    );
  }

  /**
   * Run tests
   */
  async runTests() {
    const spinner = ora('Running tests...').start();

    try {
      const command = `${this.gradlePath} test`;
      execSync(command, {
        cwd: this.projectDir,
        stdio: 'pipe'
      });

      spinner.succeed('All tests passed');
    } catch (error) {
      spinner.fail(`Tests failed: ${error.message}`);
      throw error;
    }
  }

  /**
   * Complete build process
   */
  async build(options = {}) {
    try {
      console.log(chalk.blue.bold('🔨 Starting Android build process...\n'));

      await this.checkGradleWrapper();

      if (options.clean) {
        await this.clean();
      }

      if (options.incrementVersion) {
        await this.incrementVersionCode();
      }

      if (options.versionName) {
        await this.updateVersionName(options.versionName);
      }

      if (options.runTests) {
        await this.runTests();
      }

      let outputPath;
      if (options.buildAAB) {
        outputPath = await this.buildAAB();
      } else {
        outputPath = await this.buildAPK();
      }

      console.log(chalk.green.bold('\n✅ Build completed successfully!'));
      console.log(chalk.gray(`Output: ${outputPath}`));

      const version = await this.getCurrentVersion();
      console.log(chalk.gray(`Version: ${version.versionName} (${version.versionCode})`));

      return outputPath;

    } catch (error) {
      console.error(chalk.red.bold('\n❌ Build failed!'));
      console.error(chalk.red(error.message));
      process.exit(1);
    }
  }
}

// CLI Interface
async function main() {
  const program = new Command();

  program
    .name('android-build')
    .description('Android build automation tool')
    .version('1.0.0');

  program
    .command('build')
    .description('Build Android app')
    .option('-t, --type <type>', 'Build type (debug, release)', 'release')
    .option('-f, --flavor <flavor>', 'Build flavor')
    .option('-d, --dir <directory>', 'Android project directory', 'android')
    .option('--aab', 'Build AAB instead of APK')
    .option('--clean', 'Clean before build')
    .option('--increment-version', 'Increment version code')
    .option('--version-name <name>', 'Set version name')
    .option('--run-tests', 'Run tests before build')
    .action(async (options) => {
      const builder = new AndroidBuilder({
        projectDir: options.dir,
        buildType: options.type,
        flavor: options.flavor
      });

      await builder.build({
        buildAAB: options.aab,
        clean: options.clean,
        incrementVersion: options.incrementVersion,
        versionName: options.versionName,
        runTests: options.runTests
      });
    });

  program.parse();
}

// Run if called directly
if (require.main === module) {
  main().catch(console.error);
}

module.exports = AndroidBuilder;