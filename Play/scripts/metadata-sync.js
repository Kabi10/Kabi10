#!/usr/bin/env node

/**
 * Metadata Synchronization Script
 *
 * This script manages app store metadata including:
 * - App descriptions and titles
 * - Screenshots and graphics
 * - Release notes and changelogs
 * - Multi-language content
 * - Content ratings
 */

const fs = require('fs-extra');
const path = require('path');
const { Command } = require('commander');
const chalk = require('chalk');
const ora = require('ora');
const inquirer = require('inquirer');
require('dotenv').config();

class MetadataManager {
  constructor(options = {}) {
    this.metadataDir = options.metadataDir || 'metadata';
    this.defaultLanguage = options.defaultLanguage || 'en-US';
    this.supportedLanguages = options.supportedLanguages || ['en-US', 'es-ES', 'fr-FR', 'de-DE'];
  }

  /**
   * Initialize metadata directory structure
   */
  async initializeStructure() {
    const spinner = ora('Initializing metadata structure...').start();

    try {
      // Create base directories
      await fs.ensureDir(this.metadataDir);
      await fs.ensureDir(path.join(this.metadataDir, 'screenshots'));

      // Create language-specific directories
      for (const language of this.supportedLanguages) {
        const langDir = path.join(this.metadataDir, language);
        await fs.ensureDir(langDir);

        // Create screenshot directories
        const screenshotDir = path.join(this.metadataDir, 'screenshots', language);
        await fs.ensureDir(screenshotDir);
        await fs.ensureDir(path.join(screenshotDir, 'phoneScreenshots'));
        await fs.ensureDir(path.join(screenshotDir, 'tabletScreenshots'));
        await fs.ensureDir(path.join(screenshotDir, 'tvScreenshots'));

        // Create template files if they don't exist
        await this.createTemplateFiles(langDir, language);
      }

      spinner.succeed('Metadata structure initialized');
    } catch (error) {
      spinner.fail(`Failed to initialize structure: ${error.message}`);
      throw error;
    }
  }

  /**
   * Create template metadata files
   */
  async createTemplateFiles(langDir, language) {
    const templates = {
      'title.txt': 'Your App Name',
      'short_description.txt': 'A brief description of your app (max 80 characters)',
      'full_description.txt': `Your app's full description goes here.

This is where you can provide detailed information about your app's features, benefits, and functionality.

Key Features:
• Feature 1
• Feature 2
• Feature 3

Download now and experience the difference!`,
      'video.txt': 'https://www.youtube.com/watch?v=YOUR_VIDEO_ID',
      'release_notes.txt': `What's new in this version:

• Bug fixes and performance improvements
• New features added
• Enhanced user experience

Thank you for using our app!`
    };

    for (const [filename, content] of Object.entries(templates)) {
      const filePath = path.join(langDir, filename);
      if (!await fs.pathExists(filePath)) {
        await fs.writeFile(filePath, content);
      }
    }
  }

  /**
   * Validate metadata content
   */
  async validateMetadata() {
    const spinner = ora('Validating metadata...').start();
    const issues = [];

    try {
      for (const language of this.supportedLanguages) {
        const langDir = path.join(this.metadataDir, language);

        if (!await fs.pathExists(langDir)) {
          issues.push(`Missing language directory: ${language}`);
          continue;
        }

        // Check required files
        const requiredFiles = ['title.txt', 'short_description.txt', 'full_description.txt'];

        for (const file of requiredFiles) {
          const filePath = path.join(langDir, file);

          if (!await fs.pathExists(filePath)) {
            issues.push(`Missing file: ${language}/${file}`);
            continue;
          }

          const content = await fs.readFile(filePath, 'utf8');

          // Validate content length
          switch (file) {
            case 'title.txt':
              if (content.length > 50) {
                issues.push(`Title too long in ${language}: ${content.length}/50 characters`);
              }
              break;
            case 'short_description.txt':
              if (content.length > 80) {
                issues.push(`Short description too long in ${language}: ${content.length}/80 characters`);
              }
              break;
            case 'full_description.txt':
              if (content.length > 4000) {
                issues.push(`Full description too long in ${language}: ${content.length}/4000 characters`);
              }
              break;
          }
        }

        // Check screenshots
        const screenshotDir = path.join(this.metadataDir, 'screenshots', language, 'phoneScreenshots');
        if (await fs.pathExists(screenshotDir)) {
          const screenshots = await fs.readdir(screenshotDir);
          const validScreenshots = screenshots.filter(file => file.match(/\.(png|jpg|jpeg)$/i));

          if (validScreenshots.length < 2) {
            issues.push(`Insufficient phone screenshots for ${language}: ${validScreenshots.length}/2 minimum`);
          }

          if (validScreenshots.length > 8) {
            issues.push(`Too many phone screenshots for ${language}: ${validScreenshots.length}/8 maximum`);
          }
        }
      }

      if (issues.length > 0) {
        spinner.fail('Metadata validation failed');
        console.log(chalk.red('\nValidation Issues:'));
        issues.forEach(issue => console.log(chalk.red(`  • ${issue}`)));
        return false;
      } else {
        spinner.succeed('Metadata validation passed');
        return true;
      }
    } catch (error) {
      spinner.fail(`Validation error: ${error.message}`);
      throw error;
    }
  }

  /**
   * Generate release notes from changelog
   */
  async generateReleaseNotes(version) {
    const spinner = ora('Generating release notes...').start();

    try {
      const changelogPath = path.join(this.metadataDir, 'CHANGELOG.md');

      if (!await fs.pathExists(changelogPath)) {
        spinner.warn('No CHANGELOG.md found, using template release notes');
        return;
      }

      const changelog = await fs.readFile(changelogPath, 'utf8');

      // Extract version-specific changes
      const versionRegex = new RegExp(`## \\[?${version}\\]?[\\s\\S]*?(?=## \\[?\\d|$)`, 'i');
      const versionMatch = changelog.match(versionRegex);

      if (!versionMatch) {
        spinner.warn(`No changelog entry found for version ${version}`);
        return;
      }

      const releaseNotes = versionMatch[0]
        .replace(/^## \[?[\d.]+\]?.*$/m, '')
        .trim();

      // Update release notes for all languages
      for (const language of this.supportedLanguages) {
        const releaseNotesPath = path.join(this.metadataDir, language, 'release_notes.txt');
        await fs.writeFile(releaseNotesPath, releaseNotes);
      }

      spinner.succeed(`Release notes generated for version ${version}`);
    } catch (error) {
      spinner.fail(`Failed to generate release notes: ${error.message}`);
      throw error;
    }
  }

  /**
   * Interactive metadata editor
   */
  async editMetadata() {
    console.log(chalk.blue.bold('📝 Interactive Metadata Editor\n'));

    const { language } = await inquirer.prompt([
      {
        type: 'list',
        name: 'language',
        message: 'Select language to edit:',
        choices: this.supportedLanguages
      }
    ]);

    const langDir = path.join(this.metadataDir, language);
    await fs.ensureDir(langDir);

    const { field } = await inquirer.prompt([
      {
        type: 'list',
        name: 'field',
        message: 'What would you like to edit?',
        choices: [
          { name: 'App Title', value: 'title.txt' },
          { name: 'Short Description', value: 'short_description.txt' },
          { name: 'Full Description', value: 'full_description.txt' },
          { name: 'Promotional Video URL', value: 'video.txt' },
          { name: 'Release Notes', value: 'release_notes.txt' }
        ]
      }
    ]);

    const filePath = path.join(langDir, field);
    let currentContent = '';

    if (await fs.pathExists(filePath)) {
      currentContent = await fs.readFile(filePath, 'utf8');
    }

    console.log(chalk.gray(`\nCurrent content:`));
    console.log(chalk.gray(currentContent || '(empty)'));

    const { newContent } = await inquirer.prompt([
      {
        type: 'editor',
        name: 'newContent',
        message: 'Edit content (will open in your default editor):',
        default: currentContent
      }
    ]);

    await fs.writeFile(filePath, newContent);
    console.log(chalk.green(`✅ Updated ${field} for ${language}`));
  }

  /**
   * Copy metadata from one language to another
   */
  async copyLanguage(fromLang, toLang) {
    const spinner = ora(`Copying metadata from ${fromLang} to ${toLang}...`).start();

    try {
      const fromDir = path.join(this.metadataDir, fromLang);
      const toDir = path.join(this.metadataDir, toLang);

      if (!await fs.pathExists(fromDir)) {
        throw new Error(`Source language directory not found: ${fromLang}`);
      }

      await fs.ensureDir(toDir);
      await fs.copy(fromDir, toDir, { overwrite: true });

      spinner.succeed(`Metadata copied from ${fromLang} to ${toLang}`);
    } catch (error) {
      spinner.fail(`Failed to copy metadata: ${error.message}`);
      throw error;
    }
  }
}

// CLI Interface
async function main() {
  const program = new Command();

  program
    .name('metadata-sync')
    .description('Manage app store metadata')
    .version('1.0.0');

  program
    .command('init')
    .description('Initialize metadata directory structure')
    .option('-d, --dir <directory>', 'Metadata directory', 'metadata')
    .option('-l, --languages <languages>', 'Supported languages (comma-separated)', 'en-US,es-ES,fr-FR,de-DE')
    .action(async (options) => {
      const languages = options.languages.split(',').map(lang => lang.trim());
      const manager = new MetadataManager({
        metadataDir: options.dir,
        supportedLanguages: languages
      });

      await manager.initializeStructure();
    });

  program
    .command('validate')
    .description('Validate metadata content')
    .option('-d, --dir <directory>', 'Metadata directory', 'metadata')
    .action(async (options) => {
      const manager = new MetadataManager({
        metadataDir: options.dir
      });

      const isValid = await manager.validateMetadata();
      process.exit(isValid ? 0 : 1);
    });

  program
    .command('edit')
    .description('Interactive metadata editor')
    .option('-d, --dir <directory>', 'Metadata directory', 'metadata')
    .action(async (options) => {
      const manager = new MetadataManager({
        metadataDir: options.dir
      });

      await manager.editMetadata();
    });

  program
    .command('release-notes')
    .description('Generate release notes from changelog')
    .argument('<version>', 'Version number')
    .option('-d, --dir <directory>', 'Metadata directory', 'metadata')
    .action(async (version, options) => {
      const manager = new MetadataManager({
        metadataDir: options.dir
      });

      await manager.generateReleaseNotes(version);
    });

  program
    .command('copy')
    .description('Copy metadata from one language to another')
    .argument('<from>', 'Source language')
    .argument('<to>', 'Target language')
    .option('-d, --dir <directory>', 'Metadata directory', 'metadata')
    .action(async (from, to, options) => {
      const manager = new MetadataManager({
        metadataDir: options.dir
      });

      await manager.copyLanguage(from, to);
    });

  program.parse();
}

// Run if called directly
if (require.main === module) {
  main().catch(console.error);
}

module.exports = MetadataManager;