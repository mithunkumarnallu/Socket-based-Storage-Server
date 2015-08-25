# Socket-based-Storage-Server
Its a socket based storage server where multiple clients can connect to the server. Each connection runs on its own thread and multiple clients can read/write/list files on the server.
Clients have shared access to all the files and file read mechanism is done through Virtual memory paging techniques with LRU page replacement policy
