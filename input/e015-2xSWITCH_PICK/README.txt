In this example the client has two SWITCHes inside a main SWITCH. These three SWITCHes generate a total of four possible branches that are supported by a single PICK in the server which contains four branches.

The only issue in this example is that the INVOKEs in the client's main SWITH must be ignored.
