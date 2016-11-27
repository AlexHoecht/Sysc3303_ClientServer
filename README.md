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

1. Open the project folder in a Java IDE.
2. Compile and run Server.java.
  1. A directory "Server Directory" will be created for the server in the project folder if it does not already exist.
  2. The user will be prompted via GUI popup to terminate the server.
3. Compile and run class ErrorSimulator.java, if applicable.
  1 The user will be prompted via the console to choose an error to be generated.
4. Compile and run class Client.java.
  1. The user will be prompted via GUI popup for client information.
    1. Desired directory
      1. A directory "Client Directory" will be created for the client in the project folder if it does not already exist. 
    2. Simulation mode (test/normal). Choose test mode if the user wishes to use the error simulator.
    3. Output type (quiet/verbose)
    4. File name.
      1. If performing a write, make sure the file already exists within the client's directory
    5. Request type (read/write)
5. File transfer will begin after the user has selected the transfer type (read or write).
6. Wait for the file data to be transferred (approx 1MB/min).
7. A copy of the transferred file will now exist in either the server or client directory depending on if a read or write was performed.
8. The user will be prompted via GUI after each successful transfer if they would like to transfer another file.
9. Shut down the client and server via GUI popups.

### Iteration Responsibilities
- Connor Emery
  - Client error handling, error simulator debugging.
- Robert Graham
  - Read me file, client error handling, error simulator debugging.
- Alex Hoecht
  - Client, server, and sub server error handling. Error simulator debugging
- Saleem Karkabi
  - Client, server, and sub server error handling. Error simulator error generation and debugging.
- Andrew Ward
  - Error simulator error generation and debugging.
