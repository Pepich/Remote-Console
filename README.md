# Remote-Console

This is a (short? ._.) readme now I guess?

Well in short, this plugin allows staff members (well anyone with the required permission node) to connect to the server using a custom client.
This allows for reading console output, as well as chatting and running commands when the player is not online.
Permissions will be respected, displayNames will be respected if possible. Those will be updated when a player leaves.
If the player is also online at the time of the custom client sending a message/command, it will use the online player, if not it will use a clone.

Configuration is possible through the file plugins/remoteconsole/remoteconsole.properties, as well as through custom launch parameters of the spigot.jar:
To use those as launch parameters, add them to your launch script using --\<option\>.
LAUNCH PARAMETERS WILL ALWAYS OVERRIDE YOUR CONFIG! If something doesn't work as expected, check if you have put a launch parameter before complaining that an option doesn't work properly.

# DEFAULT CONFIG - The config will be located at plugins/rmc-config.properties
\# Default data folder<br/>
rmc.datafolder="plugins/remoteconsole/"<br/>
\# Path to the server logfiles. Will be used for searching the logs.<br/>
rmc.logs.path="logs/"<br/>
\# Will only allow connections from LAN IP ranges (192.168.x.x, 10.x.x.x, 127.x.x.x)<br/>
rmc.LANOnly=false<br/>
\# Will only allow connections from VM IP ranges (10.x.x.x, 127.x.x.x)<br/>
rmc.VMOnly=false<br/>
\# Will only allow connections from local IPs (127.x.x.x) - These ones are overriding bottom to top. If localOnly is set, it will override the other two settings.<br/>
rmc.localOnly=false<br/>
\# Will disable the local join override permission. If not set, 127.0.0.1 will ALWAYS be able to connect<br/>
rmc.noLocal=false<br/>
\# Will allow localhost connections to authenticate as any user without a password. Meant for debug purposes only. HANDLE WITH CARE!<br/>
rmc.localnoauth=false<br/>
\# Will only allow connections from certain IP ranges.<br/>
\# Exmaples:<br/>
\# [] - IP whitelist disabled. Allow all connections.<br/>
\# If only negative IPs are added or the entire list is prefixed with an !, the whitelist will act as a blacklist, allowing everything except the listed IPs:<br/>
\# !["127.0.0.1", "192.168.1.\*"] - Acts as blacklist, allows everyone except localhost and 192.168.1.xxx<br/>
\# ["!127.0.0.1", "!192.168.1.\*"] - Same as above, just more complicated. Useful if you want to combine white and blacklist.<br/>
\# ["192.168.1.\*"] - Allows connections from all IPs starting with 192.168.1<br/>
rmc.whitelist=[]<br/>
\# ["192.168.[1-3].\*", "192.168.5.\*", "!192.168.1.1"] - Allows for all IPs that are in the following range:<br/>
\# 192.168.1.\*, 192.168.2.\*, 192.168.3.\*, 192.168.5.\* - but blocks 192.168.1.1 specifically. Overrides right to left -> if you allow a range, then block a certain IP it will block that IP. If you block a range, then allow a certain IP it will allow it. If you allow a certain IP, then block a range it will block it.<br/>
\# ["192.168.[1-3,5].\*","!192.168.1.1"] - same as above, just a little shorter.<br/>
\# Specifies the port to bind to.<br/>
rmc.port=9000<br/>
\# Enables authentication through an ingame command when the user is online.<br/>
rmc.allowignauth=true<br/>
\# Compare the IPs and only allow ingame authentication if the IPs of the online user and the rmc user match.<br/>
rnc.ignauth.checkIP=true<br/>
\# Enables token authentication (tokens are invalidated after first use).<br/>
rmc.allowtoken=true<br/>
\# Enables google 2FA.<br/>
rmc.allow2fa=true<br/>
\# Not implemented yet. Will allow the use of private/public keypairs for authentication.<br/>
rmc.allowkey=true<br/>
\# Defines the token length. Will form a pattern of xxxx-xx.<br/>
rmc.tokenlenght=6<br/>
\# Defines the token complexity.<br/>
\# 1 = upppercase only, 2 = upper and lowercase, 3 = alphanumeric, 4 = alphanumeric and special chars.<br/>
rmc.tokencomplexity=3<br/>
\# he suffix to append to displaynames for chat and commands. Will only apply if the player is not online.<br/>
rmc.suffix="&7[C]"<br/>
\# Same as suffix, just as a prefix.<br/>
rmc.prefix=""<br/><br/>
\# ----- PERMISSIONS -----<br/><br/>
\# Required permission to do anything<br/>
rmc.perm="rmc"<br/>
\# Node to connect to the server<br/>
rmc.perm.connect="rmc.connect"<br/>
\# Node to log in - please note that people with the connect permission can still receive broadcasts that don't require a login<br/>
rmc.perm.auth="rmc.auth"<br/>
\# Node to generate a one-time token ingame<br/>
rmc.perm.gettoken="rmc.gettoken"<br/>
\# Node to list all connected users<br/>
rmc.perm.list="rmc.list"<br/>

\# ----- END OF CONFIG -----

# FURTHER INFO:

- Requires PEX
- Requires an open port to bind to
- Requires rw access to \<datafolder\>*

# WIP SECTION:

- Accepting launch parameters
- IP whitelisting
- Private/public key authentication
- SQL support
