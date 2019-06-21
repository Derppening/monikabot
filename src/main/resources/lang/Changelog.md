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
- ~~Add persistent ping information to Warframe servers~~ Temporarily reverted due to issues

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

[v1.2.0-beta]
- Added `dog` command for... Displaying dogs
- Added `warframe drop` command for item/mission drop tables
- `warframe cetus`: Add Operation Plague Star information
- `reminder`: Fixed bug where "clear" would precede before list/remove

[v1.2.0-beta.1]
- `random`: Added ability to randomize entries in a list
- `warframe market`: Fix bug where information will not be displayed if codex entry is too long

[v1.2.0-beta.2]
- Add Khora to `warframe primes` prediction
- Monika now attempts to automatically reconnect when connection is down
- **Note:** This version is known to be less stable than normal builds. Please report any bugs to me and I'll try to fix them ASAP!

[v1.2.0-beta.3]
- More internal changes
- **Note:** This version is known to be less stable than normal builds. Please report any bugs to me and I'll try to fix them ASAP!

[v1.2.0-beta.4]
- Add `metar` command for displaying current weather
- `rng`: Also display minimum attempts guaranteeing 99% success rate

[v1.2.0-beta.5]
- Add `toilet` command for shits and giggles
- `warframe invasion`: Add progress for ongoing Fomorian/Razorback missions
- `warframe alert`: Fix unable to filter alerts

[v1.2.0-beta.6]
- Add `taf` command for displaying forecasted weather

[v1.2.0-beta.7]
- Remove `--experimental` flag now that it is not needed
- Add embed for displaying exceptions instead of failing silently
- Add an actual fuzzy matcher instead of the wildcard matcher we used to use

[v1.2.0-beta.8]
- Better process client disconnect events
- Improvements and fixes to logging

[v1.2.0-beta.9]
- Add more messages for debugging purposes

[v1.2.0-beta.10]
- Massive refactoring of command delegation

[v1.2.0-beta.11]
- **ADDED**: `warframe acolyte` command
- **FIXED**: Missing Limbo Prime release date

[v1.2.0-beta.12]
- No major changes

[v1.2.0-beta.13]
- Compatibility changes for Warframe Fortuna update

[v1.2.0]
- **ADDED**: New commands
    - `dog`: Displays images of dogs
    - `metar`: Displays METAR information 
    - `taf`: Displays TAF information
    - `toilet`: Format text into larger versions
    - `warframe acolyte`: Displays ongoing acolyte status (if any)
    - `warframe cetus`: Displays Cetus bounties and time
    - `warframe fortuna`: Displays Fortuna bounties and time
- **CHANGED**: Command Improvements
    - `random` can now randomize lists
    - `warframe cetus` now displays Plague Star bounty info
    - `warframe invasion` now displays Fomorian and Razorback info
- **CHANGED**: Added new prime information since last update
- **CHANGED**: Monika is now more tolerant of typos in commands
- **FIXED**: Prime deduction algorithm not properly following the order
- **FIXED**: Baro Ki'Teer information not working with >24 items