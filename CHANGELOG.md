# Pinguino Changelog

## Version 0.5.2

**[Tag Comparison](https://github.com/JamCoreDiscord/Pinguino/compare/v0.5.1...v0.5.2)**

- Created a dedicated `io.github.jamalam360.api` package for all the web APIs used by Pinguino.
- Cleaned up `ModuleExtension` by making registering enable/disable commands simple extensions functions.
- Randomise the profile pictures of `/quote non-user` quotes.
- There is now a config option (`/module moderation auto-save-threads`) that makes Pinguino automatically save all
  created threads
- The argument of `/thread save` now defaults to `true`
- Add documentation! Finally!

## Version 0.5.1

**[Tag Comparison](https://github.com/JamCoreDiscord/Pinguino/compare/v0.5.0...v0.5.1)**

- Moved `UtilExtension` into two separate extensions, `ModeratorUtilityExtension` and
  `UserUtilityExtension`.
- Moved `Arguments.kt` into the `io.github.jamalam360.util` package.
- Made this changelog file.
- Improve the format of message edit and delete logging.
- Message delete logging now includes any attachments that were associated with the message.
- Stop using a deprecated KTor API.
- Bump all dependency versions