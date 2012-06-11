These server and client are very compatible apart of their action names. This is an evolution of e003-ftp_full where the client has changed to be more compatible with the server. It include SWITCHs, PICKs and LOOPs.

It doesn't work because the way Dinapter considers a specification to be a solution. As far as the ''connected'' branch adapts every action needed it doesn't adapt the case that the client wants to download and the server rejects it. In other words, it's missing something like:

	download!, getData?, download!, getData?, ..., quit! <> rejected!
