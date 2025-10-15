# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.3] - 2025-01-27

### Changed
- Remove Hyperion library due to Android 15/16 incompatibility
- Switch from Beagle ui-bottom-sheet to ui-drawer variant
- Change MainActivity from ComponentActivity to FragmentActivity
- Replace reflection-based initialization with direct Beagle imports

### Added
- Add 8 debug modules: AppInfo, DeveloperOptions, DeviceInfo, KeylineOverlay, AnimationDuration, ScreenCapture, LogList, LifecycleLog

### Improved
- Simplify debug tools implementation from 92 lines to 29 lines
- Fix missing BuildConfig and LoggerUtils imports

## [0.1.2] - 2025-01-27

### Changed
- Refactor notification filter persistence and data layer architecture
- Improve database schema with notification_filter_enabled column (migration 9→10)
- Implement filter toggle persistence across app restarts
- Set default filter to "All" in notification history
- Add AppCountResult and DailyStatsResult DAO models
- Update repositories, ViewModels, and UI screens
- Enhance dashboard statistics and notification queue queries

## [0.1.1] - 2025-10-15

### Added
- Add app detail screen with advanced notification filters
- Implement per-app and global notification filtering system
- Add comprehensive filter management UI
- Add FilterDisplayUtil and NotificationFilterUtil for filter logic
- Add AppDetailScreen and AppDetailViewModel for per-app filter configuration
- Add StringListConverter for Room database type conversion
- Update FlowBellDatabase schema to support filter storage
- Extend AppPreferences and UserPreferences entities with filter fields
- Update repositories to handle filter preferences
- Integrate filtering logic into NotificationListenerService
- Update UI components to support filter management
- Update DAOs with filter queries

### Features
- Global keyword filtering across all notifications
- Per-app keyword filtering for granular control
- Case-insensitive matching
- Comma-separated keyword lists
- UI for managing filters in Settings and per-app details

### Documentation
- Update README.md with comprehensive project documentation
- Add detailed project description and feature highlights
- Include architecture overview and tech stack details
- Add installation and usage instructions
- Include webhook payload example
- Add screenshots and roadmap

## [0.1.0] - 2025-09-23

### Added
- Initial release of FlowBell
- Implement UI enhancements with animations and improved UX
- Add debug mode toggle for developer tools
- Setup release automation and build pipeline
- Improve keystore configuration and security
- Add pull-to-refresh functionality and debug tools control
- Implement comprehensive notification bridge system
- Add notification listener service
- Add webhook configuration and testing
- Add per-app notification control
- Add notification history and queue management
- Add dashboard with statistics
- Add settings and preferences management
- Add onboarding flow
- Add splash screen
- Add dark theme support
- Add debug tooling (Chucker, Beagle, Timber)
- Add Firebase Crashlytics integration
- Add WorkManager for background processing
- Add Room database for local storage
- Add Koin dependency injection
- Add Jetpack Compose UI with Material 3

### Security
- Remove hardcoded credentials and setup secure signing
- Remove JKS keystore file and add to gitignore
- Improve keystore configuration

### Documentation
- Document automation pipeline
- Refresh overview and screenshots
- Update README with completed features
- Update GitHub link
