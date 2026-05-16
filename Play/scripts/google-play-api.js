#!/usr/bin/env node

/**
 * Google Play Store Deployment Automation Script
 *
 * This script automates the deployment of Android apps to Google Play Store
 * using the Google Play Developer API v3.
 *
 * Features:
 * - Upload APK/AAB files
 * - Manage release tracks (internal, alpha, beta, production)
 * - Update app metadata and store listings
 * - Handle staged rollouts
 * - Upload screenshots and assets
 * - Multi-language support
 */

const { google } = require('googleapis');
const fs = require('fs-extra');
const path = require('path');
const { Command } = require('commander');
const chalk = require('chalk');
const ora = require('ora');
const inquirer = require('inquirer');
require('dotenv').config();

class GooglePlayPublisher {
  constructor(options = {}) {
    this.packageName = options.packageName || process.env.PACKAGE_NAME;
    this.keyFilePath = options.keyFilePath || process.env.GOOGLE_PLAY_JSON_KEY_PATH || 'credentials/google-play-service-account.json';
    this.track = options.track || 'internal';
    this.rolloutPercentage = options.rolloutPercentage || 10;

    if (!this.packageName) {
      throw new Error('Package name is required. Set PACKAGE_NAME environment variable or pass packageName option.');
    }

    this.auth = null;
    this.androidpublisher = null;
  }

  /**
   * Initialize Google Play API authentication
   */
  async authenticate() {
    const spinner = ora('Authenticating with Google Play API...').start();

    try {
      if (!await fs.pathExists(this.keyFilePath)) {
        throw new Error(`Service account key file not found: ${this.keyFilePath}`);
      }

      const keyFile = await fs.readJson(this.keyFilePath);

      this.auth = new google.auth.GoogleAuth({
        credentials: keyFile,
        scopes: ['https://www.googleapis.com/auth/androidpublisher']
      });

      this.androidpublisher = google.androidpublisher({
        version: 'v3',
        auth: this.auth
      });

      spinner.succeed('Successfully authenticated with Google Play API');
    } catch (error) {
      spinner.fail(`Authentication failed: ${error.message}`);
      throw error;
    }
  }

  /**
   * Create a new edit session
   */
  async createEdit() {
    const spinner = ora('Creating edit session...').start();

    try {
      const response = await this.androidpublisher.edits.insert({
        packageName: this.packageName
      });

      const editId = response.data.id;
      spinner.succeed(`Created edit session: ${editId}`);
      return editId;
    } catch (error) {
      spinner.fail(`Failed to create edit session: ${error.message}`);
      throw error;
    }
  }

  /**
   * Upload APK or AAB file
   */
  async uploadBundle(editId, bundlePath) {
    const spinner = ora(`Uploading bundle: ${path.basename(bundlePath)}...`).start();

    try {
      if (!await fs.pathExists(bundlePath)) {
        throw new Error(`Bundle file not found: ${bundlePath}`);
      }

      const fileExtension = path.extname(bundlePath).toLowerCase();
      const isAAB = fileExtension === '.aab';

      let response;

      if (isAAB) {
        response = await this.androidpublisher.edits.bundles.upload({
          packageName: this.packageName,
          editId: editId,
          media: {
            mimeType: 'application/octet-stream',
            body: fs.createReadStream(bundlePath)
          }
        });
      } else {
        response = await this.androidpublisher.edits.apks.upload({
          packageName: this.packageName,
          editId: editId,
          media: {
            mimeType: 'application/vnd.android.package-archive',
            body: fs.createReadStream(bundlePath)
          }
        });
      }

      const versionCode = response.data.versionCode;
      spinner.succeed(`Successfully uploaded ${isAAB ? 'AAB' : 'APK'} with version code: ${versionCode}`);
      return { versionCode, isAAB };
    } catch (error) {
      spinner.fail(`Failed to upload bundle: ${error.message}`);
      throw error;
    }
  }

  /**
   * Assign bundle to release track
   */
  async assignToTrack(editId, versionCode, track, rolloutPercentage = null) {
    const spinner = ora(`Assigning to ${track} track...`).start();

    try {
      const releaseBody = {
        versionCodes: [versionCode.toString()],
        status: 'completed'
      };

      // Add rollout percentage for production releases
      if (track === 'production' && rolloutPercentage) {
        releaseBody.userFraction = rolloutPercentage / 100;
        releaseBody.status = 'inProgress';
      }

      await this.androidpublisher.edits.tracks.update({
        packageName: this.packageName,
        editId: editId,
        track: track,
        requestBody: {
          releases: [releaseBody]
        }
      });

      const message = rolloutPercentage
        ? `Assigned to ${track} track with ${rolloutPercentage}% rollout`
        : `Assigned to ${track} track`;

      spinner.succeed(message);
    } catch (error) {
      spinner.fail(`Failed to assign to track: ${error.message}`);
      throw error;
    }
  }

  /**
   * Update app metadata
   */
  async updateMetadata(editId, metadataPath) {
    const spinner = ora('Updating app metadata...').start();

    try {
      const languages = await fs.readdir(metadataPath);

      for (const language of languages) {
        const langPath = path.join(metadataPath, language);
        const stat = await fs.stat(langPath);

        if (!stat.isDirectory()) continue;

        const listing = {};

        // Read metadata files
        const metadataFiles = {
          'title.txt': 'title',
          'short_description.txt': 'shortDescription',
          'full_description.txt': 'fullDescription',
          'video.txt': 'video'
        };

        for (const [filename, property] of Object.entries(metadataFiles)) {
          const filePath = path.join(langPath, filename);
          if (await fs.pathExists(filePath)) {
            listing[property] = await fs.readFile(filePath, 'utf8');
          }
        }

        if (Object.keys(listing).length > 0) {
          await this.androidpublisher.edits.listings.update({
            packageName: this.packageName,
            editId: editId,
            language: language,
            requestBody: listing
          });
        }
      }

      spinner.succeed('Successfully updated app metadata');
    } catch (error) {
      spinner.fail(`Failed to update metadata: ${error.message}`);
      throw error;
    }
  }

  /**
   * Upload screenshots
   */
  async uploadScreenshots(editId, screenshotsPath) {
    const spinner = ora('Uploading screenshots...').start();

    try {
      const languages = await fs.readdir(screenshotsPath);

      for (const language of languages) {
        const langPath = path.join(screenshotsPath, language);
        const stat = await fs.stat(langPath);

        if (!stat.isDirectory()) continue;

        const imageTypes = ['phoneScreenshots', 'tabletScreenshots', 'tvScreenshots'];

        for (const imageType of imageTypes) {
          const typePath = path.join(langPath, imageType);

          if (await fs.pathExists(typePath)) {
            const images = await fs.readdir(typePath);

            for (const image of images) {
              if (image.match(/\.(png|jpg|jpeg)$/i)) {
                const imagePath = path.join(typePath, image);

                await this.androidpublisher.edits.images.upload({
                  packageName: this.packageName,
                  editId: editId,
                  language: language,
                  imageType: imageType,
                  media: {
                    mimeType: 'image/png',
                    body: fs.createReadStream(imagePath)
                  }
                });
              }
            }
          }
        }
      }

      spinner.succeed('Successfully uploaded screenshots');
    } catch (error) {
      spinner.fail(`Failed to upload screenshots: ${error.message}`);
      throw error;
    }
  }

  /**
   * Commit the edit
   */
  async commitEdit(editId) {
    const spinner = ora('Committing changes...').start();

    try {
      await this.androidpublisher.edits.commit({
        packageName: this.packageName,
        editId: editId
      });

      spinner.succeed('Successfully committed changes to Google Play Store');
    } catch (error) {
      spinner.fail(`Failed to commit changes: ${error.message}`);
      throw error;
    }
  }

  /**
   * Complete deployment process
   */
  async deploy(bundlePath, options = {}) {
    try {
      console.log(chalk.blue.bold('🚀 Starting Google Play Store deployment...\n'));

      await this.authenticate();
      const editId = await this.createEdit();

      const { versionCode } = await this.uploadBundle(editId, bundlePath);

      await this.assignToTrack(
        editId,
        versionCode,
        options.track || this.track,
        options.rolloutPercentage || this.rolloutPercentage
      );

      if (options.updateMetadata && await fs.pathExists('metadata')) {
        await this.updateMetadata(editId, 'metadata');
      }

      if (options.uploadScreenshots && await fs.pathExists('metadata/screenshots')) {
        await this.uploadScreenshots(editId, 'metadata/screenshots');
      }

      await this.commitEdit(editId);

      console.log(chalk.green.bold('\n✅ Deployment completed successfully!'));
      console.log(chalk.gray(`Package: ${this.packageName}`));
      console.log(chalk.gray(`Version Code: ${versionCode}`));
      console.log(chalk.gray(`Track: ${options.track || this.track}`));

      if (options.rolloutPercentage) {
        console.log(chalk.gray(`Rollout: ${options.rolloutPercentage}%`));
      }

    } catch (error) {
      console.error(chalk.red.bold('\n❌ Deployment failed!'));
      console.error(chalk.red(error.message));
      process.exit(1);
    }
  }
}

// CLI Interface
async function main() {
  const program = new Command();

  program
    .name('google-play-deploy')
    .description('Deploy Android apps to Google Play Store')
    .version('1.0.0');

  program
    .command('deploy')
    .description('Deploy app to Google Play Store')
    .option('-b, --bundle <path>', 'Path to APK or AAB file')
    .option('-t, --track <track>', 'Release track (internal, alpha, beta, production)', 'internal')
    .option('-r, --rollout <percentage>', 'Rollout percentage for production releases', '10')
    .option('-p, --package <name>', 'Package name')
    .option('-k, --key <path>', 'Path to service account key file')
    .option('--update-metadata', 'Update app metadata')
    .option('--upload-screenshots', 'Upload screenshots')
    .option('--interactive', 'Interactive mode')
    .action(async (options) => {
      try {
        let bundlePath = options.bundle;
        let track = options.track;
        let rolloutPercentage = parseInt(options.rollout);

        // Interactive mode
        if (options.interactive) {
          const answers = await inquirer.prompt([
            {
              type: 'input',
              name: 'bundlePath',
              message: 'Path to APK/AAB file:',
              default: bundlePath || 'android/app/build/outputs/bundle/release/app-release.aab'
            },
            {
              type: 'list',
              name: 'track',
              message: 'Select release track:',
              choices: ['internal', 'alpha', 'beta', 'production'],
              default: track
            },
            {
              type: 'number',
              name: 'rolloutPercentage',
              message: 'Rollout percentage (for production):',
              default: rolloutPercentage,
              when: (answers) => answers.track === 'production'
            },
            {
              type: 'confirm',
              name: 'updateMetadata',
              message: 'Update app metadata?',
              default: options.updateMetadata || false
            },
            {
              type: 'confirm',
              name: 'uploadScreenshots',
              message: 'Upload screenshots?',
              default: options.uploadScreenshots || false
            }
          ]);

          bundlePath = answers.bundlePath;
          track = answers.track;
          rolloutPercentage = answers.rolloutPercentage || rolloutPercentage;
          options.updateMetadata = answers.updateMetadata;
          options.uploadScreenshots = answers.uploadScreenshots;
        }

        if (!bundlePath) {
          console.error(chalk.red('Bundle path is required. Use -b option or --interactive mode.'));
          process.exit(1);
        }

        const publisher = new GooglePlayPublisher({
          packageName: options.package,
          keyFilePath: options.key,
          track: track,
          rolloutPercentage: rolloutPercentage
        });

        await publisher.deploy(bundlePath, {
          track: track,
          rolloutPercentage: rolloutPercentage,
          updateMetadata: options.updateMetadata,
          uploadScreenshots: options.uploadScreenshots
        });

      } catch (error) {
        console.error(chalk.red('Error:', error.message));
        process.exit(1);
      }
    });

  program.parse();
}

// Run if called directly
if (require.main === module) {
  main().catch(console.error);
}

module.exports = GooglePlayPublisher;