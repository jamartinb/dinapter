 This example is a modification on the supplier-1 where, instead of an extra operation and acknowledge, we included an split operation and a data mismatch.

The results are three contracts/specifications:
 1. A correct contract.
 2. A contract where the buy procedure is split in two rules (because of the data mismatch).
 3. A third contract where, whatever the client does, it aborts the server behavior.

This example was meant to be used in WCAT'08 but it was finally replaced by supplier-1c.
