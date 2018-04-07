[v1.0.0-rc]
- Add significant figure support to RNG
- Provide more feedback to the user if something goes wrong
- Improve user checking logic

[v1.0.0-rc1]
- `reload`: Add help text
- Add custom responses when bot is invoked without a command

[v1.0.0-rc2]
- `RNG`: Fix p=0.0 causing an infinite loop and crashing the bot
- `RNG`: Fix "negative tries" error not appearing

[v1.0.0-rc3]
- Added `changelog` command
    - Displays the changes for the most recent build(s)

[v1.0.0]
- Okay Everyone! MonikaBot is officially released!
    - Use the help command to see what you can do with her!

[v1.0.1]
- Improved wording of help text.

[v1.1.0-beta]
- Run experimental and stable features in parallel
- Add `warframe market` command
- Improve help text for many command
- Many backend improvements
- Superusers: Add `config` command

[v1.1.0-beta.1]
- Fixed bug where several commands cannot be properly invoked

[v1.1.0-beta.2]
- Various behind-the-scene changes

[v1.1.0-beta.3]
- Add experimental `trivia` command
- Update list of commands in `help`

[v1.1.0-beta.4]
- `warframe market` is now out of the experimental branch
    - `--experimental` flag is not required any more
    - Fixed bug when user does not enter an item, the same "item not found" message will appear
- `trivia`: Fixed bug where True/False questions may be skipped
- Various behind-the-scene refactoring
- Released the source code of this bot to Github

[v1.1.0-beta.5]
- Add `warframe invasions` command

[v1.1.0-beta.6]
- Add `warframe alert` and `warframe cetus` commands
- `warframe-invasion` now shows help text if unknown arguments are given

[v1.1.0-beta.6.1]
- Separate Ghoul alerts into `warframe cetus` instead of `warframe alert`

[v1.1.0-beta.7]
- Add `reminder` command
- Graduate `trivia` from experimental branch

[v1.1.0-beta.7.1]
- Fix bug where bot cannot be invoked if it has a nickname
- Massive backend refactoring

[v1.1.0-beta.7.2]
- Actually fix bug where bot cannot be invoked if it has a nickname

[v1.1.0-beta.8]
- Add `warframe sortie` command
- Superusers: Add ability to send messages to any channel

[v1.1.0-beta.9]
- Add various list of `warframe` subcommands
    - `baro`: Baro Ki'Teer Information
    - `darvo`: Darvo Sale
    - `fissures`: Ongoing fissure missions
    - `primes`: Predicts next prime(s)
    - `sale`: Ongoing market sale
    - `syndicate`: Current syndicate missions
    - `wiki`: Lookup item on Warframe Wikia

[v1.1.0-beta.9.1]
- Update help text

[v1.1.0-rc]
- Add `ping` and `warframe ping` commands
- Fixed issue where `warframe wiki` does not generate a proper link

[v1.1.0-rc.1]
- Fix issue where `warframe ping` does not use Warframe-specific ping locations
- Use a better method to detect whether Warframe's gateways are down
- Reduce `warframe ping` timeout to 5 seconds

[v1.1.0-rc.2]
- Add `issue` command for submitting bug reports and feature requests
- Add fuzzy command matching
    - Example: You can now type "w c t" to replace "warframe cetus time"
- When bot is shutting down, it will now be playing "Maintenance"
- `echo`: Now allows users to send messages to other channels of servers they are in
- `ping`: Revert to use 10s timeout
- `trivia`: Users can now use lower-case letters to answer multiple choice 
- `trivia`: Fix bot replying "incorrect answer" when given an certain invalid input

[v1.1.0-rc.3]
- `warframe market`: Add fuzzy/wildcard matching
- Fix major regression where commands would be processed sequentially instead of in parallel

[v1.1.0-rc.4]
- Updated logic to be able to run both stable and development versions 
- `warframe cetus`: Display the next 3 day/night start times
- `trivia`: Fix bug where lower-case single-character answers are always wrong

[v1.1.0-rc.5]
- Add experimental Emoticon commands
- `trivia`: Further fix various bugs

[v1.1.0-rc.6]
- ~~Add persistent ping information to Warframe servers~~ Temporarily Reverted due to issues

[v1.1.0]
- Add various commands
    - `issue`: Submit an issue for Monika
    - `ping` and `warframe ping`: Latency information
    - `reminder`: Remind yourself something in Discord
    - `trivia`: Play a trivia game with Monika
    - `warframe alert`: Ongoing alerts
    - `warframe baro`: Baro Ki'Teer Information
    - `warframe cetus`: Cetus information and day/night cycle
    - `warframe darvo`: Darvo Sale
    - `warframe fissures`: Ongoing fissure missions
    - `warframe invasions`: Ongoing invasions
    - `warframe market`: Warframe Market information
    - `warframe primes`: Predicts next prime(s)
    - `warframe sale`: Ongoing market sale
    - `warframe sortie`: Current sorties
    - `warframe syndicate`: Current syndicate missions
    - `warframe wiki`: Lookup item on Warframe Wikia
- Add fuzzy matching

[v1.1.1]
- `warframe-market`: Fix bug where perfect matches will not return the given item
- `warframe-prime`: Change default behavior such that all currently available non-vaulted primes will be shown
