# MAC Address Changer Tool

Developed a comprehensive Java-based application to manage and manipulate MAC addresses for network interfaces. This tool leverages command-line interaction and system command execution to provide a robust solution for MAC address management.

## Key Features

- **MAC Address Retrieval and Manipulation**: Easily retrieve and change the MAC address of network interfaces.
- **Random MAC Address Generation**: Generate random MAC addresses for enhanced privacy and security.
- **MAC Flooding Capability**: Perform MAC flooding to test network security and performance.
- **User Interaction and Command-Line Parsing**: Intuitive command-line interface for easy user interaction.
- **Logging and Error Management**: Detailed logging and error management to ensure smooth operation and easy troubleshooting.

## Technical Environment

- **Programming Language**: Java
- **Technologies Used**:
  - Regular Expressions
  - BufferedReader
  - Runtime Execution
  - Logging Framework

## Screenshots

**Command-Line Usage**:
- Help:
   ```bash
   java MACChanger --help

![App Screenshot](https://github.com/vish-muskan19/MAC_Address_Changer_Tool/blob/main/MAC%20Address%20Changer%20Tool/SS/1.png?raw=true)

- Manual MAC Address Change:
   ```bash
   java MACChanger -i eth0 --manual

![App Screenshot](https://github.com/vish-muskan19/MAC_Address_Changer_Tool/blob/main/MAC%20Address%20Changer%20Tool/SS/2.png?raw=true)

- Random MAC Address Generation:
   ```bash
   java MACChanger -i eth0 --random

![App Screenshot](https://github.com/vish-muskan19/MAC_Address_Changer_Tool/blob/main/MAC%20Address%20Changer%20Tool/SS/3.png?raw=true)

- MAC Flooding:
   ```bash
   java MACChanger -i eth0 --flood

![App Screenshot](https://github.com/vish-muskan19/MAC_Address_Changer_Tool/blob/main/MAC%20Address%20Changer%20Tool/SS/4.png?raw=true)
