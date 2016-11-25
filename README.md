# Sysc3303_ClientServer

### Version
Iteration 4

### Date
26 November 2016

### Authors
- Connor Emery
  - 100980809
- Robert Graham
  - 100981086
- Alex Hoecht
  - 100933730
- Saleem Karkabi
  - 100944655
- Andrew Ward
  - 100898624

### Summary
An implementation of the Trivial File Tansfer Protocol (TFTP) for the Fall 2016 section of SYSC 3303.

"TFTP is a very simple protocol used to transfer files. It is from this that its name comes, Trivial File Transfer Protocol or TFTP. Each nonterminal packet is acknowledged separately. This document describes the protocol and its types of packets. The document also explains the reasons behind some of the design decisions."

K. Sollins. (1992, July). The TFTP Protocol (Revision 2). MIT. Cambridge, Massachusetts. [Online]. Available: https://tools.ietf.org/html/rfc1350

### Required Files
- Client.java
  - A host able to send requests to transfer data to or from the server.
- Error.java
  - Used to keep track of an error type bye the error simulator.
- ErrorSimulator.java
  - An intermediate host between the client and server able to simulate a number of errors for testing purposes.
- ErrorType.java
  - An enumerated type to store different types of errors generatable by the error simulator.
- Server.java
  - A host that receives request packets and spawns sub servers. Only spawns if it receives a valid TFTP request.
- ServerKiller.java
  - Used to shut down the server while not halting any transfers currently in process.
- SubServer.java
  - Spawned by the server to handle data transfers between it and the client. Once the file transfer is complete, it terminates.
- .project
- .classpath

### Instructions for Transferring Files
If at any time an unexpected error occurs in any class, terminate all processes and restart from step 1.

1. Run class Server
2. Run class ErrorSimulator, if applicable
3. Specify the error to be simulated via the console, if applicable
4. Run class Client
5. Directories "Server Directory" and "Client Directory" will be created in the project folder if they do not already exist
6. Move desired files into the directories dending on whether a read from or write to the server is desired
7. Follow directions and prompts from the client via GUI popups
8. File transfer will begin after the user has selected the transfer type (read or write)
9. Wait for files to be transferred (approx 1MB/min)
10. A copy of the transferred file will now exist in either the server or client directory depending on if a read or write was performed
11. Transfer any number more files via GUI popups
12. Shut down the client and server via GUI popups
