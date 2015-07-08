## Misc ##
  * make all logins through SSL sockets (database login, user login, user creation, and potentially even chatting etc)

## Server ##
  * make users timeout if disconnected for x minutes + add option for how long is x.
  * file lock to only allow one server instance to run at a time.
  * cleanup database on server termination
  * add some statistics at the server side

## Client ##
  * add list of commands (/cmd?)
  * add option in settings.ini for file saving locations...
  * F1 for help
  * add grayed out radio buttons for broadcast mode or client/server mode. (focus on client server for now)
  * file lock to only allow one client instance at a time
  * add entries to the file menu for different commands
  * add option to choose between direct file transfer or through the server (useful if behind NAT, default it to p2p)
  * add show IP to whois info (if offline, then show last IP?)
  * add last seen online in whois info if user is offline (or add /lastseen)
  * add logging, channel specific as well as user specific
  * add the option to join (and create) new channels
  * add show topic on join, add option to change the topic
  * add user privileges similar to ops in IRC (owndership rights to channels etc)
  * add ability to kick/ban user
  * make ignoring IP based as opposed to nickname based.
  * add /me
  * add /ping