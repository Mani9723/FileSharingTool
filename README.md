# TCPFileSharingSystem
SWE 622 Project

## Valid Commands

- java -cp pa1.jar server start <portnumber> &
***To set up client so it knows what server to talk to:***
- export PA1_SERVER=<computername:portnumber> # On Windows: set PA1_SERVER=…..
- java -cp pa1.jar client upload <path_on_client> </path/filename/on/server>
- java -cp pa1.jar client download </path/existing_filename/on/server> <path_on_client>
- java -cp pa1.jar client dir </path/existing_directory/on/server>
- java -cp pa1.jar client mkdir </path/new_directory/on/server>
- java -cp pa1.jar client rmdir </path/existing_directory/on/server>
- java -cp pa1.jar client rm </path/existing_filename/on/server>
- java -cp pa1.jar client shutdown

