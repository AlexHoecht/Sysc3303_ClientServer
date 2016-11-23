# Sysc3303_ClientServer

### Most Recent Release
23 November 2016

### Authors
- Connor Emery 100980809
- Robert Graham 100981086
- Alex Hoecht 100933730
- Saleem Karkabi 100944655
- Andrew Ward 100898624

### Summary
An implementation of the [Trivial File Tansfer Protocol (TFTP)](https://tools.ietf.org/html/rfc1350) for the Fall 2016 section of SYSC 3303.

TFTP is a very simple protocol used to transfer files.  It is from this that its name comes, Trivial File Transfer Protocol or TFTP. Each nonterminal packet is acknowledged separately.  This document describes the protocol and its types of packets.  The document also explains the reasons behind some of the design decisions.

### Instructins on Transferring Files
0. Run class Server
0. Run class ErrorSimulator, if applicable
0. Specify which error the user wished to simulate, if applicable
0. Run class Client
0. Server and Client directories will be created in the project folder if they do not already exist
0. Move desired files into the directories dending on whether the user wishes to read from or write to the server
0. Follow GUI directions and prompts from the client
0. Wait for files to be transferred
0. A copy of the file transferred will now be in the server directory if a write was performed or the client directory is a read was performed
0. Transfer more files if desired
0. Shut down the client and server via GUI popups
