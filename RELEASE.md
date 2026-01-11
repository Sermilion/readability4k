# Release Process

This document describes how to create a new release of Readability4K.

## Overview

Readability4K uses JitPack for distribution and GitHub Actions for automated release creation. Releases are triggered by pushing git tags.

## Release Checklist

### 1. Prepare the Release

- [ ] Ensure all changes are merged to `main` branch
- [ ] Run all tests: `./gradlew check`
- [ ] Run code formatting: `./gradlew spotlessApply`
- [ ] Review and update CHANGELOG (if exists)
- [ ] Commit any remaining changes

### 2. Update Version Number

Edit `library/build.gradle.kts` and update the version:

```kotlin
version = "0.1.X"
```

Commit the version change:

```bash
git add library/build.gradle.kts
git commit -m "Bump version to 0.1.X"
git push origin main
```

### 3. Create and Push Tag

Create an annotated tag matching the version:

```bash
git tag -a 0.1.X -m "Release 0.1.X"
git push origin 0.1.X
```

**Important**: Use annotated tags (`-a`) for better release notes generation.

### 4. Automated GitHub Release

Once the tag is pushed:

1. GitHub Actions CI will automatically run the build
2. If the build succeeds, a GitHub Release will be created automatically
3. Release notes will be generated from commit messages between tags
4. JitPack will build the artifacts when first requested

### 5. Verify the Release

1. Check GitHub Releases page: https://github.com/Sermilion/readability4k/releases
2. Verify the release notes are correct
3. Test the new version via JitPack:

```kotlin
dependencies {
  implementation("com.github.Sermilion.readability4k:readability4k:0.1.X")
}
```

4. Check JitPack build status: https://jitpack.io/#Sermilion/readability4k

### 6. Update Documentation

After the release is published:

- [ ] Update README.md with the new version number
- [ ] Update any other documentation referencing the old version
- [ ] Commit and push documentation updates

```bash
git add README.md
git commit -m "Update documentation for v0.1.X"
git push origin main
```

## Version Numbering

Readability4K follows Semantic Versioning (SemVer):

- **MAJOR** (X.0.0): Breaking API changes
- **MINOR** (0.X.0): New features, backward compatible
- **PATCH** (0.0.X): Bug fixes, backward compatible

Current version convention: `0.1.X` (pre-1.0 development)

## Release Types

### Regular Release

Standard semantic version (e.g., `0.1.5`):
- Creates a production release on GitHub
- Available immediately on JitPack

### Pre-release

Version with suffix (e.g., `0.1.5-beta`, `0.1.5-rc1`):
- Creates a pre-release on GitHub (marked as "Pre-release")
- Available on JitPack for testing
- Use for beta versions, release candidates, etc.

## Troubleshooting

### JitPack Build Failed

1. Check build logs: https://jitpack.io/com/github/Sermilion/readability4k/0.1.X/build.log
2. Common issues:
   - Gradle version incompatibility
   - Missing dependencies
   - Test failures
3. Fix issues, delete the tag, and re-release:

```bash
git tag -d 0.1.X
git push origin :refs/tags/0.1.X
# Fix issues, then create tag again
```

### GitHub Actions Failed

1. Check Actions tab: https://github.com/Sermilion/readability4k/actions
2. Review build logs for errors
3. Fix issues and re-push the tag (delete first if needed)

### Wrong Release Notes

Edit the GitHub Release manually:
1. Go to https://github.com/Sermilion/readability4k/releases
2. Click "Edit" on the release
3. Update the release notes
4. Save changes

## Release History

| Version | Date | Highlights |
|---------|------|------------|
| 0.1.5 | 2026-01-11 | Reddit comment parsing, improved title extraction, media preservation controls |
| 0.1.4 | - | Previous features |
| 0.1.3 | - | - |
| 0.1.2 | - | - |
| 0.1.1 | - | - |
| 0.1.0 | - | Initial release |

## CI/CD Configuration

The release process is automated via `.github/workflows/ci.yml`:

- **Triggers**: Tags matching pattern `*.*.*`
- **Build job**: Runs tests and builds all platforms
- **Release job**: Creates GitHub Release with auto-generated notes
- **JitPack**: Builds on first request after tag is pushed

## Manual Release (Emergency)

If automated release fails, create a manual GitHub Release:

1. Go to: https://github.com/Sermilion/readability4k/releases/new
2. Choose the tag
3. Write release notes
4. Click "Publish release"
5. JitPack will build automatically

## Post-Release Tasks

- [ ] Announce the release (if applicable)
- [ ] Update dependent projects (if any)
- [ ] Monitor JitPack download stats
- [ ] Watch for issues reported by users

## Contact

For questions about the release process, open an issue on GitHub.
