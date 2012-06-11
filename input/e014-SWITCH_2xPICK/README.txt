In this example the client has a single SWITCH block with four branches. In the other side, the server has a PICK with an initial choice of two branches and, in these branches, we have group of two of the choices needed by the client.

The main issue in this example is that the RECEIVE action in the first PICK must be carried in the four rules required by the client's SWITCH.

It currently works only with the default properties *BUT* it needs that ''COST_ALL_OCCURRENCES_COUNT = false''.
