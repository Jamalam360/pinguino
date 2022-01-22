# Pinguino Changelog

## Version 0.6.1

**[Tag Comparison](https://github.com/JamCoreDiscord/Pinguino/compare/v0.6.0...v0.6.1)**

- Fix an error caused by an unsafe cast.
- Quotes created by reacting with `:star:` no longer quote multiple times for each reaction.
- Display `Content` on top of `Author` in `Quote sent` logging.
- Pinging the bot will now display a help message.

## Version 0.6.0

**[Tag Comparison](https://github.com/JamCoreDiscord/Pinguino/compare/v0.5.2...v0.6.0)**

- Re-licensed to GPLv3.
- Added license headers to all files, and updated GitHub actions to account for that.
- Update Git structure; development for new versions now occurs on the `develop` branch, while
  stable releases are on the `release` branch.
- Removed an arbitrary limitation saying that you couldn't quote Pinguino.
- All responses now use a standardised embed format. This also applies to logging messages.

## Version 0.5.2

**[Tag Comparison](https://github.com/JamCoreDiscord/Pinguino/compare/v0.5.1...v0.5.2)**

- Created a dedicated `io.github.jamalam360.api` package for all the web APIs used by Pinguino.
- Cleaned up `ModuleExtension` by making registering enable/disable commands simple extensions functions.
- Randomise the profile pictures of `/quote non-user` quotes.
- There is now a config option (`/module moderation auto-save-threads`) that makes Pinguino automatically save all
  created threads
- The argument of `/thread save` now defaults to `true`
- Add documentation! Finally!
- Add `/moderation discipline unmute` to unmute members

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