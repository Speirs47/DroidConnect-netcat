# DroidConnect-netcat

A small app for testing DroidConnect-Android (not yet public).

It allows to send and receive messages over TCP protocol. AES/CBC encryption is supported.

Messages encrypted with AES/CBC consist of concatenated IV as hexidecimal string (first 32 characters) and encrypted message in Base64 format (the rest of message).
