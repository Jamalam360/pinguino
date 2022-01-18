# User Utility Module

The user utliity module provides various useful commands for users and moderators (although more commands for moderators
can be found in the moderator utility extension).

## Commands

### Slash

- `/invite`
    - Returns a link to invite the bot to your server.
- `/thread`
    - `archive`
        - Archives the current thread, if you have permission (thread owner or a moderator).
        - The `lock` argument prevents people other than moderators unarchiving the thread if set to `true`
    - `rename`
        - Renames the current thread, if you have permission (thread owner or a moderator).
    - `save`
        - Prevent the current thread from being archived, if you have permission (moderator).
- `/help`
    - Returns a link to this documentation.
- `/bugs`
    - Returns a link to the bot's issue tracker
- `/shorten-link`
    - Shortens a link using [link.jamalam.tech](https://link.jamalam.tech)
- `/paste`
    - Pastes content to Hastebin
    - `url`
        - Pastes the content from a cdn.discordapp.com link.
    - `typed`
        - Pastes the content passed in the argument.

### Context

- `Pin` (Message)
    - Pin the current message, if you have permission (thread owner or a moderator).
