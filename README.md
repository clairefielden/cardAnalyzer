# cardAnalyzer Application
Multi-client chat application that allows the user to upload text files/enter card details manually

## Usage
1. Run the _CentralServer.java_ file first
    * This class simulates a peer-to-peer hybrid model. This is the Central Point for which the peers would communicate with to retrieve a list of available peers.
    * Every peer has to acknowledge himself to this server.
2. Run the _Client.java_ file second
   * This class initializes the GUI, as well as sets up a connection between the client and the server
   * Enter your IP (may be localhost) and a nickname (not mandatory) where indicated
3. The _chatApp.java_ window
   * In order to process card details, manually type into the large textbox and press _send_ when applicable
   * Alternatively, use the _Upload_ button to choose a textfile containing text to be processed
4. Results are written to the project's home directory, in "serverOutput.txt"

## Considerations and Limitations
This application takes into account multiple users, and therefore uses queuing of messages.
However, too many users could quickly overwhelm the server when sending vast amounts of information simultaneously.
This can be mitigated by having buffers and locks preventing vast amounts of threads overwhelming the server's message queue simultaneously.#

## Future Work
* Transfer the current Java Swing GUi onto a web page (HTML/CSS/JS)
* Improve the look, feel and usability
* In addition to textfiles, use image recognition to read in PDFs and PNGs of cards for input to the server
