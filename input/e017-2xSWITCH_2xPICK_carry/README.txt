In this example there are two nested SWITCHes inside a main SWITCH in the client and, in the server, there are two nested PICKs inside a main PICK. Apart from that there is another issue: even though there is a server branch for every client branch, there are two branches from the inner blocks that are switched so the solution rules must be like the following:

	a!, c! <> n?, u?
	a!, d! <> m?, v?
	b!, e! <> m?, x?
	b!, f! <> n?, y?

In the current state of Dinapter this is not feasible because rules are closed after SWITCHes and MERGEs so the following sequences are not possible:

	a!, c! <> ...
	a!, d! <> ...
	b!, e! <> ...
	b!, f! <> ...
	
Therefore, this example doesn't work.
