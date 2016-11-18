# Remote-Console

This is a (short? ._.) readme now I guess?

Well in short, this plugin allows staff members (well anyone with the required permission node) to connect to the server using a custom client.
This allows for reading console output, as well as chatting and running commands when the player is not online.
Permissions will be respected, displayNames will be respected if possible. Those will be updated when a player leaves.
If the player is also online at the time of the custom client sending a message/command, it will use the online player, if not it will use a clone.

Configuration is possible through the file plugins/remoteconsole/remoteconsole.properties, as well as through custom launch parameters of the spigot.jar:
To use those as launch parameters, add them to your launch script using --\<option\>.
LAUNCH PARAMETERS WILL ALWAYS OVERRIDE YOUR CONFIG! If something doesn't work as expected, check if you have put a launch parameter before complaining that an option doesn't work properly.

# DEFAULT CONFIG
\# Default data folder<br/>
rmc.datafolder="plugins/remoteconsole"<br/>
\# Enable logging<br/>
rmc.log=true<br/>
\# Path to the logfile. Path to the datafolder will be put in front<br/>
rmc.log.path="latest.log"<br/>
\# Allows users to read console without authentication. Sending messages will still require authentication. HANDLE WITH CARE!<br/>
rmc.noAuth=false<br/>
\# Enables permission respecting commands. If set to false, users can run anything with elevated permissions. HANDLE WITH CARE!<br/>
rmc.respectPerms=true<br/>
\# Will only allow connections from LAN IP ranges (192.168.x.x, 10.x.x.x, 127.0.0.1)
rmc.LANOnly=false<br/>
\# Will only allow connections from VM IP ranges (10.x.x.x, 127.0.0.1)<br/>
- rmc.VMOnly=false<br/>
\# Will only allow connections from local IPs (127.x.x.x) - These ones are overriding bottom to top. If localOnly is set, it will override the other two settings.<br/>
- rmc.localOnly=false<br/>
- rmc.noLocal=false            // Will disable the local join override permission. If not set, 127.0.0.1 will ALWAYS be able to connect
- rmc.localnoauth=true         // Will allow localhost connections to authenticate as any user without a password.
- rmc.whitelist=[]             // Will only allow connections from certain IP ranges. Allows for ranges and wildcards. Use "!\<IP>" to blacklist an IP
- rmc.port=9000                // Specifies the port to bind to.
- rmc.allowignauth=true        // Enables authentication through an ingame command when the user is online.
- rnc.ignauth.checkIP=true     // Compare the IPs and only allow ingame authentication if the IPs of the online user and the rmc user match.
- rmc.allowtoken=true          // Enables token authentication (tokens are invalidated after first use).
- rmc.allowpw=true             // Enables password authentication.
- rmc.allow2fa=true            // Enables google 2FA.
- rmc.allowkey=true            // Not implemented yet. Will allow the use of private/public keypairs for authentication.
- rmc.force2FA=false           // Force users to use g-auth 2FA.
- rmc.minpwlength=8            // Specifies the minimum required password length.
- rmc.pwpattern="luns"         // l/u = requires lowercase/uppercase | n = requires number | s = requires special char (like !).
- rmc.pwexpire=-1              // time in seconds until the password expires. -1 = never. Allows for use of patterns like 30d5h3s
- rmc.tokenlenght=6            // defines the token length. Will form a pattern of xxxx-xx.
- rmc.tokencomplexity=3        // 1 = upppercase only, 2 = upper and lowercase, 3 = alphanumeric, 4 = alphanumeric and special chars.
- rmc.suffix="§7[C]"           // The suffix to append to displaynames for chat and commands. Will only apply if the player is not online.
- rmc.prefix=""                // Same as suffix, just as a prefix.
- rmc.perm="rmc"                        // Required permission to do anything - NOT GRANTED BY WILDCARDS!
- rmc.perm.connect="rmc.connect"        // Node to connect to the server
- rmc.perm.auth="rmc.auth"              // Node to log in - please note that people with the connect permission can still receive broadcasts that don't require a login
- rmc.perm.read="rmc.read"              // Node to read the console output
- rmc.perm.write="rmc.write"            // Node to send any data
- rmc.perm.write.chat="rmc.write.chat"  // Node to send chat messages
- rmc.perm.write.cmd="rmc.write.cmd"    // Node to run commands
- rmc.perm.gettoken="rmc.gettoken"      // Node to generate a one-time token ingame
- rmc.perm.list="rmc.list"              // Node to list all connected users

# FURTHER INFO:

Requires PEX if respectPerms is set to true
Requires an open port to bind to
Requires rw access to plugins/remoteconsole/*

# WIP SECTION:

- Accepting launch parameters
- Debug output
- IP whitelisting
- Password patterns/expiration
- PEX-less integration when not respecting permissions
- Private/public key authentication
- Partially: permission checking
- Ingame commands:
  - list
- SQL support
