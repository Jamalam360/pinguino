# Role Module

The role module provides a 'reaction roles' like system, but using slash commands to apply roles rather than 
reactions. The list of roles that users can add to themselves can be updated using `/module`.

## Commands

### Slash

- `/role`
    - `add`
        - Add the specified role to yourself (the role name is autocompleted. Only roles that have been configured
          using the `/module` command can be added/removed)
    - `remove`
        - Remove the specified role from yourself (the role name is autocompleted. Only roles that have been configured
          using the `/module` command can be added/removed)