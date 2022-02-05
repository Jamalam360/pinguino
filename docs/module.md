# Module Module

The module module is responsible for configuring the bot.

## Commands

### Slash

- `/module`
    - `quotes`
        - `enable`
            - Enables the quotes module.
        - `disable`
            - Disables the quotes module.
        - `set-channel`
            - Sets the channel to post quotes to.
    - `logging`
        - `enable`
            - Enables the logging module.
        - `disable`
            - Disables the logging module.
        - `set-channel`
            - Sets the channel to post logging messages to.
    - `moderation`
        - `enable`
            - Enables the moderation module.
        - `disable`
            - Disables the moderation module.
        - `set-role`
            - Set the role required to run moderator level commands.
        - `auto-save-threads`
            - Set whether threads should be prevented from archiving by default.
        - `set-public-mod-log-channel`
            - Sets the channel to post public moderation logs to.
    - `greeting`
        - `enable`
            - Enables the greeting module.
        - `disable`
            - Disables the greeting module.
        - `set-channel`
            - Sets the channel to post greetings to.
        - `set-greeting`
            - Sets the greeting message. Use `$user` to subsitute in the user's name.
        - `set-farewell`
            - Sets the farewell message. Use `$user` to subsitute in the user's name.
    - `file-paste`
        - The file paste module can automatically upload files to hastebin when they are posted in the channel, for
          easier viewing on mobile. This is helpful for devloper communities who have users posting logs often.
        - `enable`
            - Enables the file-paste module.
        - `disable`
            - Disables the file-paste module.
        - `set-url`
            - Sets the hastebin URL to use.
    - `phishing`
        - `enable`
            - Enables the phishing module.
        - `disable`
            - Disables the phishing module.
        - `set-discipline-level`
                - Sets the level of discipline to use - (delete, kick, ban).