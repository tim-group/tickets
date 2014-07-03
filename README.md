Tickets - tamper-resistant strings
==================================

Often we need to send data out in the wild, and to re-interpret it
when it's handed back to us. Data stored in cookies is an almost
perfect example of this. But how to protect against that data being
tampered with before it was returned?

This library allows for simply-structured data to be formatted as a
string, with a MAC (Message Authentication Code) appended. On parsing,
this MAC is regenerated and checked before returning the unmarshalled
data to the caller.

The data consists of mulitple (character,string) pairs, which are kept
in order although pairs with the same leading character are grouped
together (and interpreted together as a single list of values).

For example:

    u1344808e-5029-489b-9f44-c95923e81da0,x5dbe5337

This ticket represents a single pair,
`{"u":"1344808e-5029-489b-9f44-c95923e81da0"}`. This could be put into
a ticket to represent a single user ID, for example. Additionally you
may want to add a set of roles:

    u1344808e-5029-489b-9f44-c95923e81da0,radmin,ruser,x4d70671e

The Ticket classes provide access to the data with a very simple API,
it is expected that you would encapsulate the Ticket inside your own
class to apply some sort of data schema.

The ticket marshalling handles escaping non-ascii characters, so you
could put any string into a ticket. So you could do your own
marshalling (e.g. to JSON) and add the result into the ticket.

The tickets above were produced with the provided HMAC generator,
using SHA-1 as the digest and putting only the first 32 bits of the
digest into the ticket. HMAC is a standard algorithm, and the encoding
can easily be reproduced in other languages.

When using tickets for security purposes, you may need to consider
that they only provide resistance to tampering: in the above example,
there is nothing to stop the above ticket being intercepted and
re-used for an unlimited amount of time.
