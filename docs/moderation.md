# Moderation

The moderation module provides traditional moderation commands (ban, mute, kick etc.).

## Commands

### Slash

- `/moderation`
    - `thread-auto-join`
        - Pinguino can optionally silently add specific roles to newly created threads (useful for moderators being
          added to all threads).
        - `add-role`
            - Adds a role to the list of roles to add to new threads.
        - `remove-role`
            - Removes a role from the list of roles to add to new threads.
    - `discipline`
        - `mute`
            - Mutes a member using Discord's timeout system
        - `unmute`
            - Unmutes a member using Discord's timeout system
        - `kick`
            - Kicks a member
        - `ban`
            - Bans a member
    - `lock`
        - Locks a channel so only moderators can talk
    - `unlock`
        - Unlocks a channel so everyone can talk