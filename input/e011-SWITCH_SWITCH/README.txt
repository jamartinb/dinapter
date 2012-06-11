In this example we have to adapt two SWITCH blocks. All the alternatives must be supported by the generated adaptor.

It doesn't work because we don't check that every SWITCH branch is included in our solution. We only chech that the required actions are present, not that they belong to all possible branches.
